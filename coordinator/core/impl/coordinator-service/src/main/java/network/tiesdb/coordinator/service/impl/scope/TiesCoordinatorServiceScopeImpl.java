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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
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
import network.tiesdb.service.scope.api.TiesEntry;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeHealing;
import network.tiesdb.service.scope.api.TiesServiceScopeModification;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry.FieldHash;
import network.tiesdb.service.scope.api.TiesServiceScopeModification.Entry.FieldValue;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Selector.FieldSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Query.Selector.FunctionSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Result.Field.HashField;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Result.Field.RawField;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollection.Result.Field.ValueField;
import network.tiesdb.service.scope.api.TiesServiceScopeResult;
import network.tiesdb.service.scope.api.TiesServiceScopeSchema;
import network.tiesdb.service.scope.api.TiesServiceScopeSchema.FieldSchema;
import network.tiesdb.transport.api.TiesTransportClient;

public class TiesCoordinatorServiceScopeImpl implements TiesServiceScope {

    private static final String SEGREGATION_ERROR = "ERROR";

    private static final Logger LOG = LoggerFactory.getLogger(TiesCoordinatorServiceScopeImpl.class);

    private static final short ETHEREUM_NETWORK_ID = 60;

    private static final int NODE_REQUEST_TIMEOUT = 60;
    private static final TimeUnit NODE_REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static enum ModificationResultType {
        SUCCESS, MISS, FAILURE
    }

    private final TiesCoordinatorServiceImpl service;

    private final ExecutorService healingExecutor = ForkJoinPool.commonPool(); // TODO FIXME Change executor!!!

    public TiesCoordinatorServiceScopeImpl(TiesCoordinatorServiceImpl service) {
        this.service = service;
        LOG.debug(this + " is opened");
    }

    @Override
    public void close() throws IOException {
        healingExecutor.shutdown();
        try {
            healingExecutor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Failed to shut down healing process", e);
            healingExecutor.shutdownNow();
        }
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

    private static byte[] getFieldsHash(byte[] entryHash, Set<String> fieldNames, Function<String, byte[]> mapper)
            throws TiesServiceScopeException {
        Digest keyHashDigest = DigestManager.getDigest(DigestManager.KECCAK_256);
        for (String fieldName : fieldNames) {
            byte[] fieldHash = mapper.apply(fieldName);
            if (null == fieldHash) {
                throw new TiesServiceScopeException(
                        "Field " + fieldName + " was not found in entry " + DatatypeConverter.printHexBinary(entryHash));
            }
            keyHashDigest.update(fieldHash);
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

        byte[] entryKeyHash;
        {
            Map<String, FieldHash> fhs = entry.getFieldHashes();
            Map<String, FieldValue> fvs = entry.getFieldValues();
            entryKeyHash = getFieldsHash(entry.getHeader().getHash(), keyFields.stream().map(f -> f.getName()).collect(Collectors.toSet()),
                    fieldName -> {
                        byte[] hash;
                        FieldHash fh = fhs.get(fieldName);
                        hash = null == fh ? null : fh.getHash();
                        if (null == hash) {
                            FieldValue fv = fvs.get(fieldName);
                            hash = null == fv ? null : fv.getHash();
                        }
                        return hash;
                    });
        }
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

                    @Override
                    public ModificationResultType on(TiesServiceScopeHealing.Result result) throws TiesServiceScopeException {
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

        healingExecutor.execute(() -> {

            Set<String> pkFieldNames = fields.parallelStream().filter(f -> f.isPrimaryKey()).map(f -> f.getName())
                    .collect(Collectors.toSet());
            try {
                healingDetection(pkFieldNames, pkHash -> sch.getNodes(tsn, tbn, pkHash), resultWaiters);
            } catch (TiesServiceScopeException ex) {
                LOG.error("Healing failed for modification request {}", action.getMessageId(), ex);
            }
        });
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

        Map<String, Set<Node>> segregatedResults = ConsistencyArbiter.segregate(resultWaiters,
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

                                    @Override
                                    public Stream<TiesServiceScopeRecollection.Result.Entry> on(TiesServiceScopeHealing.Result result)
                                            throws TiesServiceScopeException {
                                        LOG.error("Illegal result for recollection response: {}", result);
                                        return Stream.empty();
                                    }
                                });
                    } catch (CancellationException | TiesServiceScopeException | InterruptedException | ExecutionException
                            | TimeoutException e) {
                        LOG.debug("Failed recollection result on message: {}", recollectionRequest.getMessageId(), e);
                        return Stream.empty();
                    }
                });

        LOG.debug("SegregatedResults: {}", segregatedResults);

        Set<String> arbiterEntryHashes = arbiter.results(segregatedResults).filter(p -> SEGREGATION_ERROR != p).collect(Collectors.toSet());

        LOG.debug("ArbiterEntryHashes: {}", arbiterEntryHashes);
        try {
            if (!arbiterEntryHashes.isEmpty()) {
                List<TiesServiceScopeRecollection.Result.Entry> arbiterEntries = resultWaiters.values().parallelStream() //
                        .flatMap(result -> {
                            try {
                                return result.get().get().accept(
                                        new TiesServiceScopeResult.Result.Visitor<Stream<TiesServiceScopeRecollection.Result.Entry>>() {

                                            @Override
                                            public Stream<TiesServiceScopeRecollection.Result.Entry> on(
                                                    TiesServiceScopeModification.Result result) throws TiesServiceScopeException {
                                                LOG.error("Illegal result for recollection: {}", result);
                                                return Stream.empty();
                                            }

                                            @Override
                                            public Stream<TiesServiceScopeRecollection.Result.Entry> on(
                                                    TiesServiceScopeRecollection.Result result) throws TiesServiceScopeException {
                                                return result.getEntries().parallelStream();
                                            }

                                            @Override
                                            public Stream<TiesServiceScopeRecollection.Result.Entry> on(
                                                    TiesServiceScopeHealing.Result result) throws TiesServiceScopeException {
                                                LOG.error("Illegal result for recollection: {}", result);
                                                return Stream.empty();
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

                recollectionRequest.setResult(//
                        new TiesServiceScopeRecollection.Result() {
                            @Override
                            public List<Entry> getEntries() {
                                return arbiterEntries;
                            }
                        });

            } else {
                Set<Node> failedNodes = segregatedResults.get(SEGREGATION_ERROR);
                if (null != failedNodes && !failedNodes.isEmpty()) {
                    throw new TiesServiceScopeException("Read failed for nodes " + failedNodes);
                } else {
                    recollectionRequest.setResult(//
                            new TiesServiceScopeRecollection.Result() {
                                @Override
                                public List<Entry> getEntries() {
                                    return Collections.emptyList();
                                }
                            });
                }
            }
        } finally {
            healingExecutor.execute(() -> {

                Set<String> pkFieldNames = fields.parallelStream().filter(f -> f.isPrimaryKey()).map(f -> f.getName())
                        .collect(Collectors.toSet());
                try {
                    healingDetection(pkFieldNames, pkHash -> sch.getNodes(tsn, tbn, pkHash), resultWaiters);
                } catch (TiesServiceScopeException ex) {
                    LOG.error("Healing failed for recollection request {}", recollectionRequest.getMessageId(), ex);
                }
            });
        }
    }

    private static class HealingMappingEntry<K, V> {

        private final String keyHash;
        private final String entryHash;

        private final K key;
        private final V value;

        private HealingMappingEntry(String keyHash, String entryHash, K key, V value) {
            this.keyHash = keyHash;
            this.entryHash = entryHash;
            this.key = key;
            this.value = value;
        }

    }

    private void healingDetection(Set<String> primaryKeyFieldNames, Function<byte[], Set<? extends Node>> nodesMapper,
            Map<Node, Future<CoordinatedResult<TiesServiceScopeResult.Result>>> resultWaiters) throws TiesServiceScopeException {

        Map<String, Map<String, Map<Node, TiesEntry>>> healingExpectantMap = resultWaiters.entrySet().parallelStream().flatMap(e -> {
            try {
                TiesServiceScopeResult.Result result = e.getValue().get().get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                return result.accept(new TiesServiceScopeResult.Result.Visitor<Stream<HealingMappingEntry<Node, TiesEntry>>>() {

                    @Override
                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeModification.Result result)
                            throws TiesServiceScopeException {
                        // TODO Auto-generated method stub
                        RuntimeException err = new RuntimeException("not yet implemented");
                        err.setStackTrace(new StackTraceElement[] { err.getStackTrace()[0] });
                        LOG.debug("Modification healing unimplemented", err);
                        return Stream.<HealingMappingEntry<Node, TiesEntry>>empty();
                    }

                    @Override
                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeRecollection.Result recollectionResult)
                            throws TiesServiceScopeException {
                        return recollectionResult.getEntries().parallelStream().map(entry -> {
                            try {
                                byte[] entryHash = entry.getEntryHeader().getHash();
                                Map<String, byte[]> fhs = entry.getEntryFields().parallelStream()
                                        .filter(f -> primaryKeyFieldNames.contains(f.getName()))
                                        .collect(Collectors.groupingBy(f -> f.getName(), Collectors.mapping(f -> {
                                            try {
                                                return f.accept(new TiesServiceScopeRecollection.Result.Field.Visitor<byte[]>() {

                                                    @Override
                                                    public byte[] on(HashField field) throws TiesServiceScopeException {
                                                        return field.getHash();
                                                    }

                                                    @Override
                                                    public byte[] on(RawField field) throws TiesServiceScopeException {
                                                        Digest keyHashDigest = DigestManager.getDigest(DigestManager.KECCAK_256);
                                                        keyHashDigest.update(field.getRawValue());
                                                        byte[] out = new byte[keyHashDigest.getDigestSize()];
                                                        keyHashDigest.doFinal(out);
                                                        return out;
                                                    }

                                                    @Override
                                                    public byte[] on(ValueField field) throws TiesServiceScopeException {
                                                        throw new TiesServiceScopeException(
                                                                "No TiesDB ValueFields should appear on Coordinator!");
                                                    }
                                                });
                                            } catch (TiesServiceScopeException ex) {
                                                LOG.error("Can't get field hash for field: " + f.getName(), ex);
                                                return null;
                                            }
                                        }, Collectors.collectingAndThen(Collectors.toList(), list -> {
                                            return null == list || list.isEmpty() ? null : list.get(0);
                                        }))));
                                byte[] fieldsHash = getFieldsHash(entryHash, primaryKeyFieldNames, fieldName -> {
                                    return fhs.get(fieldName);
                                });
                                return new HealingMappingEntry<Node, TiesEntry>( //
                                        DatatypeConverter.printHexBinary(fieldsHash), //
                                        DatatypeConverter.printHexBinary(entryHash), //
                                        e.getKey(), //
                                        entry);
                            } catch (TiesServiceScopeException ex) {
                                LOG.error("Result filtering failure", ex);
                                return null;
                            }
                        }).filter(m -> null != m);
                    }

                    @Override
                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeHealing.Result result)
                            throws TiesServiceScopeException {
                        throw new TiesServiceScopeException("Double healing prohibited");
                    }

                });
            } catch (Throwable th) {
                LOG.error("Result healing mapping failure", th);
                return Stream.<HealingMappingEntry<Node, TiesEntry>>empty();
            }
        }).collect(//
                Collectors.groupingBy(e -> e.keyHash, //
                        Collectors.groupingBy(e -> e.entryHash, //
                                Collectors.toMap(e -> e.key, e -> e.value))));

        LOG.debug("Healing map: {}", healingExpectantMap);

        TiesRouter router = service.getRouterService();

        healingExpectantMap.entrySet().parallelStream().forEach(pke -> {
            String pkHashStr = pke.getKey();
            Map<String, Map<Node, TiesEntry>> entryMap = pke.getValue();
            if (entryMap.size() > 1) {
                // Multiple versions
                Map<String, BigInteger> versionMap = entryMap.entrySet().parallelStream().collect(//
                        Collectors.groupingByConcurrent(e -> e.getKey(), //
                                Collectors.mapping(
                                        e -> e.getValue().values().parallelStream().limit(1).map(te -> te.getHeader().getEntryVersion())
                                                .collect(//
                                                        Collectors.collectingAndThen(Collectors.maxBy((o1, o2) -> o1.compareTo(o2)),
                                                                (Optional<BigInteger> opt) -> opt.orElse(BigInteger.ONE.negate()))),
                                        Collectors.collectingAndThen(Collectors.maxBy((o1, o2) -> o1.compareTo(o2)),
                                                (Optional<BigInteger> opt) -> opt.orElse(BigInteger.ONE.negate())))));

                LOG.debug("Version map: {}", versionMap);

                Set<BigInteger> latest = new HashSet<>(versionMap.values());
                if (latest.containsAll(versionMap.values())) {
                    entryMap.keySet().retainAll(versionMap.keySet());
                } else {
                    // Conflicting versions
                    // FIXME TODO Implement conflict resolving logic
                    entryMap.clear();
                }

            }

            Set<? extends Node> nodes = nodesMapper.apply(DatatypeConverter.parseHexBinary(pkHashStr));
            entryMap.entrySet().parallelStream().forEach(ene -> {
                String enHashStr = ene.getKey();
                Map<Node, TiesEntry> nodesEntryMap = ene.getValue();
                if (!nodesEntryMap.keySet().containsAll(nodes)) {
                    Set<Node> nodesForHealing = new HashSet<>(nodes);
                    nodesForHealing.removeAll(nodesEntryMap.keySet());
                    LOG.debug("Entry {} should be healed\n\t   to nodes: {}\n\t from nodes: {}", //
                            enHashStr, nodesForHealing, nodesEntryMap.keySet());
                    healingPropagation(router, nodesForHealing);
                }
            });

        });

    }

    private void healingPropagation(TiesRouter router, Set<Node> nodes) {

        nodes.forEach(node -> {
            CoordinatedResult<TiesServiceScopeResult.Result> coordinatedResult = service.getRequestPool().register();
            try {
                TiesTransportClient c = router.getClient(node);
                c.request(new TiesServiceScopeConsumer() {

                    @Override
                    public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                        s.heal(new TiesServiceScopeHealing() {

                            @Override
                            public BigInteger getMessageId() {
                                return coordinatedResult.getId();
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
        });
    }

    @Override
    public void heal(TiesServiceScopeHealing action) throws TiesServiceScopeException {
        // TODO Auto-generated method stub
        throw new TiesServiceScopeException("Healing delegation not implemented yet");
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
