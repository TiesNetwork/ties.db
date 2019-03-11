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
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
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
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry.FieldHash;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry.FieldValue;
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

    private static byte[] getEntryKeyHash(Entry entry, Set<FieldDescription> keyFields) throws TiesServiceScopeException {
        Digest keyHashDigest = DigestManager.getDigest(DigestManager.KECCAK_256);
        Map<String, FieldHash> fhs = entry.getFieldHashes();
        Map<String, FieldValue> fvs = entry.getFieldValues();
        for (FieldDescription field : keyFields) {
            FieldHash fieldHash = fhs.get(field.getName());
            if (null == fieldHash) {
                fieldHash = fvs.get(field.getName());
            }
            if (null == fieldHash) {
                throw new TiesServiceScopeException("Key field " + field.getName() + " was not found in entry "
                        + DatatypeConverter.printHexBinary(entry.getHeader().getHash()));
            }
            keyHashDigest.update(fieldHash.getHash());
        }
        byte[] out = new byte[keyHashDigest.getDigestSize()];
        keyHashDigest.doFinal(out);
        return out;
    }

    private static <T> Predicate<T> distinct(Function<? super T, ?> extractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(extractor.apply(t));
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
        Set<FieldDescription> keyFields = fields.stream().filter(f -> f.isPrimaryKey()).collect(Collectors.toSet());
        {
            Set<String> entryFieldNames = entry.getFieldValues().keySet();
            if (!keyFields.stream().map(f -> f.getName()).allMatch(entryFieldNames::contains)) {
                new TiesServiceScopeException("Missing required primary key fields");
            }
        }

        byte[] entryKeyHash = getEntryKeyHash(entry, keyFields);
        Set<? extends Node> nodes = sch.getNodes(tsn, tbn, entryKeyHash);
        LOG.debug("CoordinatedModification {} nodes: {}", DatatypeConverter.printHexBinary(entryKeyHash), nodes);
        if (null == nodes || nodes.isEmpty()) {
            throw new TiesServiceScopeException("No target nodes found for request");
        }

        Map<Node, Future<CoordinatedResult<TiesServiceScopeResult.Result>>> resultWaiters;
        CompletionService<CoordinatedResult<TiesServiceScopeResult.Result>> completionService = new ExecutorCompletionService<>(
                ForkJoinPool.commonPool()); // TODO FIXME Change executor!!!
        {
            resultWaiters = new HashMap<>();
            TiesRouter router = service.getRouterService();
            for (Node node : nodes) {
                resultWaiters.put(node, completionService.submit(() -> {
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
                                            public Void on(TiesServiceScopeModification.Result.Error error)
                                                    throws TiesServiceScopeException {
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
                }));
            }
        }
        ConsistencyArbiter arbiter = new ConsistencyArbiter(action.getConsistency(), sch.getReplicationFactor(tsn, tbn));

        for (Future<CoordinatedResult<TiesServiceScopeResult.Result>> result : resultWaiters.values()) {
            try {
                result.get().get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
            } catch (Throwable e) {
                LOG.error("Node request failed", e);
            }
        }

        Map<ModificationResultType, Set<Node>> segregatedResults = ConsistencyArbiter.segregate(resultWaiters, new HashMap<>(),
                futureResult -> {
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
                        LOG.error("Result filtering failure", e);
                        return ModificationResultType.FAILURE;
                    }
                });

        Set<ModificationResultType> results = arbiter.results(segregatedResults).collect(Collectors.toSet());
        if (results.contains(ModificationResultType.SUCCESS)) {
            action.setResult(new TiesServiceScopeModification.Result.Success() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }
            });
        } else if (results.contains(ModificationResultType.MISS)) {
            Set<Node> missedNodes = segregatedResults.get(ModificationResultType.FAILURE);
            action.setResult(new TiesServiceScopeModification.Result.Error() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }

                @Override
                public Throwable getError() {
                    return new TiesServiceScopeException("Write missed for newer record " + missedNodes);
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
    public void select(TiesServiceScopeRecollection recollectionRequest) throws TiesServiceScopeException {

        Query query = recollectionRequest.getQuery();

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

        Set<? extends Node> nodes = sch.getNodes(tsn, tbn);
        if (null == nodes || nodes.isEmpty()) {
            throw new TiesServiceScopeException("No target nodes found for request");
        }

        Map<Node, Future<CoordinatedResult<TiesServiceScopeResult.Result>>> resultWaiters;
        CompletionService<CoordinatedResult<TiesServiceScopeResult.Result>> completionService = new ExecutorCompletionService<>(
                ForkJoinPool.commonPool()); // TODO FIXME Change executor!!!
        {
            resultWaiters = new HashMap<>();
            TiesRouter router = service.getRouterService();
            for (Node node : nodes) {
                resultWaiters.put(node, completionService.submit(() -> {
                    CoordinatedResult<TiesServiceScopeResult.Result> coordinatedResult = service.getRequestPool().register();
                    try {
                        TiesTransportClient c = router.getClient(node);
                        c.request(new TiesServiceScopeConsumer() {
                            @Override
                            public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                                s.select(new TiesServiceScopeRecollection() {

                                    @Override
                                    public ActionConsistency getConsistency() {
                                        return recollectionRequest.getConsistency();
                                    }

                                    @Override
                                    public BigInteger getMessageId() {
                                        return coordinatedResult.getId();
                                    }

                                    @Override
                                    public Query getQuery() {
                                        return recollectionRequest.getQuery();
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
                }));
            }
            for (Future<CoordinatedResult<TiesServiceScopeResult.Result>> result : resultWaiters.values()) {
                try {
                    result.get().get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                } catch (Throwable e) {
                    LOG.error("Node request failed", e);
                }
            }
        }

        ConsistencyArbiter arbiter = new ConsistencyArbiter(recollectionRequest.getConsistency(), sch.getReplicationFactor(tsn, tbn));

        Map<String, Set<Node>> segregatedResults = ConsistencyArbiter.segregate(resultWaiters, new HashMap<>(),
                e -> DatatypeConverter.printHexBinary(e.getEntryHeader().getHash()), result -> {
                    try {
                        return result.get().get()
                                .accept(new TiesServiceScopeResult.Result.Visitor<Stream<TiesServiceScopeRecollection.Result.Entry>>() {

                                    @Override
                                    public Stream<TiesServiceScopeRecollection.Result.Entry> on(TiesServiceScopeModification.Result result)
                                            throws TiesServiceScopeException {
                                        LOG.error("Illegal result for recollection response: {}", result);
                                        return Stream.empty();
                                    }

                                    @Override
                                    public Stream<TiesServiceScopeRecollection.Result.Entry> on(TiesServiceScopeRecollection.Result result)
                                            throws TiesServiceScopeException {
                                        return result.getEntries().parallelStream();
                                    }
                                });
                    } catch (CancellationException | TiesServiceScopeException | InterruptedException | ExecutionException
                            | TimeoutException e) {
                        LOG.debug("Failed recollection result on message: {}", recollectionRequest.getMessageId(), e);
                        return Stream.empty();
                    }
                });

        Set<String> arbiterEntryHashes = arbiter.results(segregatedResults).filter(p -> RECOLLECTION_ERROR != p)
                .collect(Collectors.toSet());

        if (!arbiterEntryHashes.isEmpty()) {
            List<TiesServiceScopeRecollection.Result.Entry> arbiterEntries = resultWaiters.values().parallelStream() //
                    .flatMap(result -> {
                        try {
                            return result.get().get()
                                    .accept(new TiesServiceScopeResult.Result.Visitor<Stream<TiesServiceScopeRecollection.Result.Entry>>() {

                                        @Override
                                        public Stream<TiesServiceScopeRecollection.Result.Entry> on(
                                                TiesServiceScopeModification.Result result) throws TiesServiceScopeException {
                                            return Stream.empty();
                                        }

                                        @Override
                                        public Stream<TiesServiceScopeRecollection.Result.Entry> on(
                                                TiesServiceScopeRecollection.Result result) throws TiesServiceScopeException {
                                            return result.getEntries().parallelStream();
                                        }
                                    });
                        } catch (CancellationException | TiesServiceScopeException | InterruptedException | ExecutionException
                                | TimeoutException e) {
                        }
                        return Stream.empty();
                    }) //
                    .filter(e -> arbiterEntryHashes.contains(DatatypeConverter.printHexBinary(e.getEntryHeader().getHash()))) //
                    .filter(distinct(e -> DatatypeConverter.printHexBinary(e.getEntryHeader().getHash()))) //
                    .collect(Collectors.toList());

            recollectionRequest.setResult(new TiesServiceScopeRecollection.Result() {

                @Override
                public List<Entry> getEntries() {
                    return arbiterEntries;
                }

            });

        } else {
            throw new TiesServiceScopeException("Read failed for nodes " + segregatedResults.get(RECOLLECTION_ERROR));
        }

    }

    @Override
    public void schema(TiesServiceScopeSchema schemaRequest) throws TiesServiceScopeException {
        String tsn = schemaRequest.getTablespaceName();
        String tbn = schemaRequest.getTableName();

        TiesServiceSchema sch = service.getSchemaService();
        Set<FieldDescription> fieldDescriptions = sch.getFields(tsn, tbn);

        schemaRequest.setResult(new FieldSchema() {

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
