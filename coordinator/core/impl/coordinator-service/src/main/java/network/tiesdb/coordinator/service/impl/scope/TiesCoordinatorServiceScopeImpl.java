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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.coordinator.service.impl.TiesCoordinatorServiceImpl;
import network.tiesdb.coordinator.service.impl.scope.TiesCoordinatedRequestPool.CoordinatedResult;
import network.tiesdb.coordinator.service.schema.TiesServiceSchema;
import network.tiesdb.coordinator.service.schema.TiesServiceSchema.FieldDescription;
import network.tiesdb.router.api.TiesRouter;
import network.tiesdb.router.api.TiesRouter.Node;
import network.tiesdb.router.api.TiesRoutingException;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeModification;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Selector.FieldSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Selector.FunctionSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeResult;
import network.tiesdb.service.scope.api.TiesServiceScopeSchema;
import network.tiesdb.service.scope.api.TiesServiceScopeSchema.FieldSchema;
import network.tiesdb.transport.api.TiesTransportClient;

public class TiesCoordinatorServiceScopeImpl implements TiesServiceScope {

    private static final String RECOLLECTION_ERROR = "ERROR";

    private static final Logger LOG = LoggerFactory.getLogger(TiesCoordinatorServiceScopeImpl.class);

    private static final short ETHEREUM_NETWORK_ID = 60;

    private static final int NODE_REQUEST_TIMEOUT = 60;
    private static final TimeUnit NODE_REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static enum ModificationResultType {
        SUCCESS, MISS, FAILURE
    }

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

    private static <T> Predicate<T> not(Predicate<T> p) {
        return x -> !p.test(x);
    }

    private static Entry checkEntryIsValid(Entry entry) throws TiesServiceScopeException {
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
        modification(action, (s, o) -> s.insert(o));
    }

    @Override
    public void update(TiesServiceScopeModification action) throws TiesServiceScopeException {
        modification(action, (s, o) -> s.update(o));
    }

    @Override
    public void delete(TiesServiceScopeModification action) throws TiesServiceScopeException {
        modification(action, (s, o) -> s.delete(o));
    }

    @FunctionalInterface
    interface CheckedBiFunction<T, U, R, E extends Throwable> {

        R apply(T t, U u) throws E;

        default <V> CheckedBiFunction<T, U, V, E> andThen(Function<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (T t, U u) -> after.apply(apply(t, u));
        }

    }

    @FunctionalInterface
    interface TiesServiceOperation
            extends CheckedBiFunction<TiesServiceScope, TiesServiceScopeModification, Void, TiesServiceScopeException> {

        @Override
        default Void apply(TiesServiceScope t, TiesServiceScopeModification u) throws TiesServiceScopeException {
            applyVoid(t, u);
            return null;
        }

        void applyVoid(TiesServiceScope t, TiesServiceScopeModification u) throws TiesServiceScopeException;

    }

    protected void modification(TiesServiceScopeModification action, TiesServiceOperation operation) throws TiesServiceScopeException {

        Entry entry = checkEntryIsValid(action.getEntry());
        TiesEntryHeader header = entry.getHeader();

        String tsn = entry.getTablespaceName();
        String tbn = entry.getTableName();

        TiesServiceSchema sch = service.getSchemaService();
        Set<FieldDescription> fields = sch.getFields(tsn, tbn);
        {
            Set<String> entryFieldNames = entry.getFieldValues().keySet();
            if (!fields.stream().filter(f -> f.isPrimaryKey()).map(f -> f.getName()).allMatch(entryFieldNames::contains)) {
                new TiesServiceScopeException("Missing required primary key fields");
            }
        }

        Set<Node> nodes = sch.getNodes(tsn, tbn);
        if (null == nodes || nodes.isEmpty()) {
            throw new TiesServiceScopeException("No target nodes found for request");
        }

        TiesRouter router = service.getRouterService();
        Map<Node, CompletableFuture<CoordinatedResult<TiesServiceScopeResult.Result>>> resultWaiters = new HashMap<>();

        for (Node node : nodes) {
            resultWaiters.put(node, CompletableFuture.supplyAsync(() -> {
                CoordinatedResult<TiesServiceScopeResult.Result> coordinatedResult = service.getRequestPool().register();
                try {
                    TiesTransportClient c = router.getClient(node);
                    c.request(new TiesServiceScopeConsumer() {
                        @Override
                        public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                            operation.apply(s, new TiesServiceScopeModification() {

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
                                public void setResult(Result result) throws TiesServiceScopeException {
                                    result.accept(new TiesServiceScopeModification.Result.Visitor<Void>() {

                                        @Override
                                        public Void on(TiesServiceScopeModification.Result.Success success)
                                                throws TiesServiceScopeException {
                                            LOG.trace("Node request sent successfully for: {} messageId {}", node);
                                            return null;
                                        }

                                        @Override
                                        public Void on(TiesServiceScopeModification.Result.Error error) throws TiesServiceScopeException {
                                            LOG.trace("Node request sent failed for: {} messageId {}", node, error.getError());
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
                } catch (Throwable e) {
                    LOG.warn("Node request failed for node: {} scope {}", node, e);
                    coordinatedResult.fail(e);
                }
                return coordinatedResult;
            }/* , executor */)); // TODO FIXME Add executor!!!
        }

        CompletableFuture<Void> allResultsFuture = CompletableFuture
                .allOf(resultWaiters.values().toArray(new CompletableFuture[resultWaiters.size()]));
        allResultsFuture.join();

        ConsistencyArbiter arbiter = new ConsistencyArbiter(action.getConsistency(), nodes.size());

        Map<ModificationResultType, Set<Node>> segregatedResults = ConsistencyArbiter.segregate(resultWaiters, futureResult -> {
            try {
                CoordinatedResult<TiesServiceScopeResult.Result> coordinatedResult = futureResult.get();
                TiesServiceScopeResult.Result result = coordinatedResult.get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                return result.accept(new TiesServiceScopeResult.Result.Visitor<ModificationResultType>() {

                    @Override
                    public ModificationResultType on(TiesServiceScopeModification.Result result) throws TiesServiceScopeException {
                        return Arrays.equals(result.getHeaderHash(), header.getHash()) //
                                ? ModificationResultType.SUCCESS
                                : ModificationResultType.MISS;
                    }

                    @Override
                    public ModificationResultType on(TiesServiceScopeRecollection.Result result) throws TiesServiceScopeException {
                        return ModificationResultType.FAILURE;
                    }
                });
            } catch (Throwable e) {
                return ModificationResultType.FAILURE;
            }
        });

        Set<ModificationResultType> results = arbiter.getResults(segregatedResults);
        if (results.contains(ModificationResultType.SUCCESS)) {
            action.setResult(new TiesServiceScopeModification.Result.Success() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }
            });
        } else if (results.contains(ModificationResultType.MISS)) {
            action.setResult(new TiesServiceScopeModification.Result.Error() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }

                @Override
                public Throwable getError() {
                    return new TiesServiceScopeException("Write missed for newer record");
                }
            });
        } else if (results.contains(ModificationResultType.FAILURE)) {
            Set<Node> failedNodes = segregatedResults.get(ModificationResultType.FAILURE);
            action.setResult(new TiesServiceScopeModification.Result.Error() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }

                @Override
                public Throwable getError() {
                    return new TiesServiceScopeException("Write failed for nodes " + failedNodes);
                }
            });
        } else {
            action.setResult(new TiesServiceScopeModification.Result.Error() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }

                @Override
                public Throwable getError() {
                    return new TiesServiceScopeException("No results found");
                }
            });
        }
    }

    @Override
    public void select(TiesServiceScopeRecollection action) throws TiesServiceScopeException {

        Query query = action.getQuery();

        String tsn = query.getTablespaceName();
        String tbn = query.getTableName();

        TiesServiceSchema sch = service.getSchemaService();
        Set<FieldDescription> fields = sch.getFields(tsn, tbn);
        {
            Set<String> tableFields = fields.stream().map(f -> f.getName()).collect(Collectors.toSet());
            Stream<String> queryFieldNameStream = query.getSelectors().stream().map(s -> {
                try {
                    return s.accept(new network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Selector.Visitor<String>() {

                        @Override
                        public String on(FunctionSelector s) throws TiesServiceScopeException {
                            return null;
                        }

                        @Override
                        public String on(FieldSelector s) throws TiesServiceScopeException {
                            return s.getFieldName();
                        }

                    });
                } catch (TiesServiceScopeException e) {
                    LOG.error("Failed field mapping: {}", s, e);
                    return null;
                }
            }).filter(s -> null != s);
            List<String> missingFields = queryFieldNameStream.filter(not(tableFields::contains)).collect(Collectors.toList());
            if (!missingFields.isEmpty()) {
                new TiesServiceScopeException("Missing required fields: " + missingFields);
            }
        }

        Set<Node> nodes = sch.getNodes(tsn, tbn);
        if (null == nodes || nodes.isEmpty()) {
            throw new TiesServiceScopeException("No target nodes found for request");
        }

        Map<Node, CompletableFuture<CoordinatedResult<TiesServiceScopeResult.Result>>> resultWaiters;
        {
            resultWaiters = new HashMap<>();
            TiesRouter router = service.getRouterService();
            for (Node node : nodes) {
                resultWaiters.put(node, CompletableFuture.supplyAsync(() -> {
                    CoordinatedResult<TiesServiceScopeResult.Result> coordinatedResult = service.getRequestPool().register();
                    try {
                        TiesTransportClient c = router.getClient(node);
                        c.request(new TiesServiceScopeConsumer() {
                            @Override
                            public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                                s.select(new TiesServiceScopeRecollection() {

                                    @Override
                                    public ActionConsistency getConsistency() {
                                        return action.getConsistency();
                                    }

                                    @Override
                                    public BigInteger getMessageId() {
                                        return coordinatedResult.getId();
                                    }

                                    @Override
                                    public Query getQuery() {
                                        return action.getQuery();
                                    }

                                    @Override
                                    public void setResult(Result result) throws TiesServiceScopeException {
                                        throw new TiesServiceScopeException("Coordinator should not handle ClientScope results");
                                    }
                                });
                            }
                        });
                    } catch (TiesRoutingException e) {
                        LOG.warn("Route was not found for node: {}", node, e);
                        coordinatedResult.fail(e);
                    } catch (Throwable e) {
                        LOG.warn("Node request failed for node: {} scope {}", node, e);
                        coordinatedResult.fail(e);
                    }
                    return coordinatedResult;
                }/* , executor */)); // TODO FIXME Add executor!!!
            }
            CompletableFuture<Void> allResultsFuture = CompletableFuture
                    .allOf(resultWaiters.values().toArray(new CompletableFuture[resultWaiters.size()]));
            allResultsFuture.join();
        }

        Map<String, Set<Node>> segregatedResults;
        {
            Digest digest = DigestManager.getDigest(DigestManager.KECCAK_256);
            segregatedResults = ConsistencyArbiter.segregate(resultWaiters, futureResult -> {
                try {
                    CoordinatedResult<TiesServiceScopeResult.Result> coordinatedResult = futureResult.get();
                    TiesServiceScopeResult.Result result = coordinatedResult.get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                    return result.accept(new TiesServiceScopeResult.Result.Visitor<String>() {

                        @Override
                        public String on(TiesServiceScopeModification.Result result) throws TiesServiceScopeException {
                            return RECOLLECTION_ERROR;
                        }

                        @Override
                        public String on(TiesServiceScopeRecollection.Result result) throws TiesServiceScopeException {
                            digest.reset();
                            result.getEntries().stream().map(e -> e.getEntryHeader().getHash()).forEach(digest::update);
                            byte[] out = new byte[digest.getDigestSize()];
                            digest.doFinal(out);
                            return DatatypeConverter.printHexBinary(out);
                        }
                    });
                } catch (Throwable e) {
                    return RECOLLECTION_ERROR;
                }
            });
        }
        Set<String> filteredSegregations;
        {
            filteredSegregations = new ConsistencyArbiter(action.getConsistency(), nodes.size()).getResults(segregatedResults);
        }

        Optional<String> firstResultHash = filteredSegregations.stream().filter(p -> RECOLLECTION_ERROR != p).findFirst();

        if (firstResultHash.isPresent()) {
            Set<Node> filteredNodes = segregatedResults.get(firstResultHash.get());
            try {
                TiesServiceScopeRecollection.Result result = resultWaiters.get(filteredNodes.stream().findFirst().get()).get().get()
                        .accept(new TiesServiceScopeResult.Result.Visitor<TiesServiceScopeRecollection.Result>() {

                            @Override
                            public TiesServiceScopeRecollection.Result on(TiesServiceScopeModification.Result result)
                                    throws TiesServiceScopeException {
                                throw new TiesServiceScopeException("Wrong result of recollection request");
                            }

                            @Override
                            public TiesServiceScopeRecollection.Result on(TiesServiceScopeRecollection.Result result)
                                    throws TiesServiceScopeException {
                                return result;
                            }

                        });
                action.setResult(new TiesServiceScopeRecollection.Result() {

                    @Override
                    public List<Entry> getEntries() {
                        return result.getEntries();
                    }

                });
            } catch (CancellationException | InterruptedException | ExecutionException | TimeoutException e) {
                throw new TiesServiceScopeException("Failed recollection result on message " + action.getMessageId(), e);
            }
        } else {
            throw new TiesServiceScopeException("Read failed for nodes " + segregatedResults.get(RECOLLECTION_ERROR));
        }

    }

    @Override
    public void schema(TiesServiceScopeSchema query) throws TiesServiceScopeException {
        String tsn = query.getTablespaceName();
        String tbn = query.getTableName();

        TiesServiceSchema sch = service.getSchemaService();
        Set<FieldDescription> fieldDescriptions = sch.getFields(tsn, tbn);

        query.setResult(new FieldSchema() {

            private final List<Field> fields;
            {
                fields = fieldDescriptions.stream().map(fd -> {
                    return new FieldSchema.Field() {

                        @Override
                        public boolean isPrimary() {
                            return fd.isPrimaryKey();
                        }

                        @Override
                        public String getFieldType() {
                            return fd.getType();
                        }

                        @Override
                        public String getFieldName() {
                            return fd.getName();
                        }
                    };
                }).collect(Collectors.toList());
            }

            @Override
            public List<Field> getFields() {
                return fields;
            }

        });
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
