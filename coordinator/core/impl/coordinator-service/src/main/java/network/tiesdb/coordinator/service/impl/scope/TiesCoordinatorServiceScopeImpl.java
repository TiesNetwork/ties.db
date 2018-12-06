/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.coordinator.service.impl.scope;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.coordinator.service.impl.TiesCoordinatorServiceImpl;
import network.tiesdb.coordinator.service.impl.scope.TiesCoordinatedRequestPool.CoordinatedResult;
import network.tiesdb.coordinator.service.schema.TiesServiceSchema;
import network.tiesdb.coordinator.service.schema.TiesServiceSchema.FieldDescription;
import network.tiesdb.exception.TiesException;
import network.tiesdb.router.api.TiesRouter;
import network.tiesdb.router.api.TiesRouter.Node;
import network.tiesdb.router.api.TiesRoutingException;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeAction;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.CountConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.PercentConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.QuorumConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeModification;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Result;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Result.Error;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Result.Success;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection;
import network.tiesdb.service.scope.api.TiesServiceScopeResult;
import network.tiesdb.service.scope.api.TiesServiceScopeSchema;
import network.tiesdb.transport.api.TiesTransportClient;

public class TiesCoordinatorServiceScopeImpl implements TiesServiceScope {

    private static final Logger LOG = LoggerFactory.getLogger(TiesCoordinatorServiceScopeImpl.class);

    private static final short ETHEREUM_NETWORK_ID = 60;

    private static final int NODE_REQUEST_TIMEOUT = 60;
    private static final TimeUnit NODE_REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final TiesCoordinatorServiceImpl service;

    public TiesCoordinatorServiceScopeImpl(TiesCoordinatorServiceImpl service) {
        this.service = service;
        LOG.debug(this + " is opened");
    }

    @Override
    public void close() throws IOException {
        LOG.debug(this + " is closed");
    }

    @Override
    public TiesVersion getServiceVersion() {
        return service.getVersion();
    }

    private Entry checkEntryIsValid(Entry entry) throws TiesServiceScopeException {
        TiesEntryHeader header = entry.getHeader();
        {
            short networkId = header.getEntryNetwork();
            if (networkId != ETHEREUM_NETWORK_ID) {
                throw new TiesServiceScopeException("Unknown network id " + Integer.toHexString(networkId));
            }
        }
        return entry;
    }

    @Override
    public void insert(TiesServiceScopeModification action) throws TiesServiceScopeException {

        BigInteger messageId = action.getMessageId();
        Entry entry = checkEntryIsValid(action.getEntry());
        TiesEntryHeader header = entry.getHeader();

        String tsn = entry.getTablespaceName();
        String tbn = entry.getTableName();

        TiesServiceSchema sch = service.getSchemaService();
        // Set<FieldDescription> fields = sch.getFields(tsn, tbn);
        //
        // {
        // Set<String> entryFieldNames = entry.getFieldValues().keySet();
        // if (!fields.stream().filter(f -> f.isPrimaryKey()).map(f ->
        // f.getName()).allMatch(entryFieldNames::contains)) {
        // new TiesServiceScopeException("Missing required primary key fields");
        // }
        // }

        Set<Node> nodes = sch.getNodes(tsn, tbn);

        TiesRouter router = service.getRouterService();
        Map<Node, CoordinatedResult<TiesServiceScopeResult.Result>> resultWaiters = new HashMap<>();

        for (Node node : nodes) {
            CoordinatedResult<TiesServiceScopeResult.Result> coordinatedResult = service.getRequestPool().register();
            try {
                TiesTransportClient c = router.getClient(node);
                c.request(new TiesServiceScopeConsumer() {
                    @Override
                    public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                        s.insert(new TiesServiceScopeModification() {

                            @Override
                            public ActionConsistency getConsistency() {
                                return action.getConsistency();
                            }

                            @Override
                            public BigInteger getMessageId() {
                                return coordinatedResult.getId();
                            }

                            @Override
                            public Entry getEntry() {
                                return action.getEntry();
                            }

                            @Override
                            public void addResult(Result result) throws TiesServiceScopeException {
                                result.accept(new TiesServiceScopeModification.Result.Visitor<Void>() {

                                    @Override
                                    public Void on(Success success) throws TiesServiceScopeException {
                                        LOG.trace("Node request sent successfully for: {} messageId {}", node, messageId);
                                        resultWaiters.put(node, coordinatedResult);
                                        return null;
                                    }

                                    @Override
                                    public Void on(Error error) throws TiesServiceScopeException {
                                        LOG.trace("Node request sent failed for: {} messageId {}", node, messageId, error.getError());
                                        coordinatedResult.fail(error.getError());
                                        return null;
                                    }
                                });
                            }

                        });
                    }
                });
            } catch (TiesRoutingException e) {
                LOG.warn("Route was not found for node: {}", node, e);
                coordinatedResult.fail(e);
            } catch (TiesException e) {
                LOG.warn("Node request failed for node: {} scope {}", node, e);
                coordinatedResult.fail(e);
            }
            if (!resultWaiters.containsValue(coordinatedResult)) {
                coordinatedResult.fail(new TiesException("Unexpected error on node request. Request was not sent."));
            }
        }

        Map<Node, TiesServiceScopeModification.Result> collectedResults = new HashMap<>(resultWaiters.size());
        for (Map.Entry<Node, CoordinatedResult<TiesServiceScopeResult.Result>> waiterEntry : resultWaiters.entrySet()) {
            Node node = waiterEntry.getKey();
            CoordinatedResult<TiesServiceScopeResult.Result> futureResult = waiterEntry.getValue();
            try {
                try {
                    TiesServiceScopeResult.Result result = futureResult.get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                    LOG.debug("Result for node {}: {}", node, result);
                    collectedResults.put(node,
                            result.accept(new TiesServiceScopeResult.Result.Visitor<TiesServiceScopeModification.Result>() {

                                @Override
                                public Result on(Result result) {
                                    return result;
                                }

                                @Override
                                public Result on(TiesServiceScopeRecollection.Result result) throws TiesServiceScopeException {
                                    throw new TiesServiceScopeException("Illegal result returned from node " + node + ": " + result);
                                }

                            }));
                } catch (TimeoutException e) {
                    throw new TiesServiceScopeException("Node request timed out for node " + node, e);
                } catch (InterruptedException | ExecutionException e) {
                    throw new TiesServiceScopeException("Node request failed for node " + node, e);
                } catch (Throwable e) {
                    throw new TiesServiceScopeException("Node request collapsed for node " + node, e);
                }
            } catch (TiesServiceScopeException e) {
                LOG.error("Error request result for node {}", node, e);
                futureResult.fail(e);
                collectedResults.put(node, new TiesServiceScopeModification.Result.Error() {

                    @Override
                    public byte[] getHeaderHash() {
                        return header.getHash();
                    }

                    @Override
                    public Throwable getError() {
                        return e;
                    }
                });
            }
        }
        action.addResult(collectedResults.entrySet().stream().filter(e -> Arrays.equals(e.getValue().getHeaderHash(), header.getHash()))
                .findFirst().map(e -> e.getValue())//
                .orElse(new TiesServiceScopeModification.Result.Error() {

                    @Override
                    public byte[] getHeaderHash() {
                        return header.getHash();
                    }

                    @Override
                    public Throwable getError() {
                        return new TiesServiceScopeException("No aplicable results from nodes");
                    }
                }));
    }

    @Override
    public void update(TiesServiceScopeModification action) throws TiesServiceScopeException {
        Entry entry = checkEntryIsValid(action.getEntry());
        // TODO Auto-generated method stub
        throw new TiesServiceScopeException("Not yet implemented");
    }

    @Override
    public void delete(TiesServiceScopeModification action) throws TiesServiceScopeException {
        Entry entry = checkEntryIsValid(action.getEntry());
        // TODO Auto-generated method stub
        throw new TiesServiceScopeException("Not yet implemented");
    }

    @Override
    public void select(TiesServiceScopeRecollection query) throws TiesServiceScopeException {
        // TODO Auto-generated method stub
        throw new TiesServiceScopeException("Not yet implemented");
    }

    @Override
    public void schema(TiesServiceScopeSchema query) throws TiesServiceScopeException {
        // TODO Auto-generated method stub
        throw new TiesServiceScopeException("Not yet implemented");
    }

    @Override
    public String toString() {
        return "TiesCoordinatorServiceScopeImpl [service=" + service + "]";
    }

    @Override
    public void result(TiesServiceScopeResult result) throws TiesServiceScopeException {
        if (!service.getRequestPool().complete(result.getMessageId(), result.getResult())) {
            throw new TiesServiceScopeException("Request completion failed for messageId: " + result.getMessageId());
        }
    }

}
