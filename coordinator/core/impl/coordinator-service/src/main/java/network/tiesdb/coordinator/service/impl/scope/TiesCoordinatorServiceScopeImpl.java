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

import static network.tiesdb.util.Hex.UPPERCASE_HEX;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tiesdb.lib.crypto.digest.DigestManager.getDigest;
import static com.tiesdb.lib.crypto.digest.DigestManager.KECCAK_256;
import com.tiesdb.lib.crypto.digest.api.Digest;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.coordinator.service.impl.TiesCoordinatorServiceImpl;
import network.tiesdb.coordinator.service.impl.scope.TiesCoordinatedRequestPool.CoordinatedResult;
import network.tiesdb.coordinator.service.schema.TiesServiceSchema;
import network.tiesdb.coordinator.service.schema.TiesServiceSchema.FieldDescription;
import network.tiesdb.router.api.TiesRouter;
import network.tiesdb.router.api.TiesRouter.Node;
import network.tiesdb.router.api.TiesRoutingException;
import network.tiesdb.service.scope.api.TiesCheque;
import network.tiesdb.service.scope.api.TiesEntry;
import network.tiesdb.service.scope.api.TiesEntryHeader;
import network.tiesdb.service.scope.api.TiesServiceScope;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.service.scope.api.TiesServiceScopeHealingAction;
import network.tiesdb.service.scope.api.TiesServiceScopeModificationAction;
import network.tiesdb.service.scope.api.TiesServiceScopeModificationAction.Result.Error;
import network.tiesdb.service.scope.api.TiesServiceScopeModificationAction.Result.Success;
import network.tiesdb.service.scope.api.TiesEntryExtended;
import network.tiesdb.service.scope.api.TiesEntryExtended.TypedHashField;
import network.tiesdb.service.scope.api.TiesEntryExtended.TypedValueField;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeBillingAction;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Partial;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Query;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Query.Selector.FieldSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Query.Selector.FunctionSelector;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Result.Entry;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Result.Field.HashField;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Result.Field.RawField;
import network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Result.Field.ValueField;
import network.tiesdb.service.scope.api.TiesServiceScopeResultAction;
import network.tiesdb.service.scope.api.TiesServiceScopeSchemaAction;
import network.tiesdb.service.scope.api.TiesServiceScopeSchemaAction.FieldSchema;
import network.tiesdb.transport.api.TiesTransportClient;

public class TiesCoordinatorServiceScopeImpl implements TiesServiceScope {

    private static final Logger LOG = LoggerFactory.getLogger(TiesCoordinatorServiceScopeImpl.class);

    private static final String DEFAULT_HASH_ALG = KECCAK_256;

    private static final String SEGREGATION_ERROR = "ERROR";

    private static final short ETHEREUM_NETWORK_ID = 60;

    private static final int NODE_REQUEST_TIMEOUT = 60;
    private static final TimeUnit NODE_REQUEST_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static enum ModificationResultType {
        SUCCESS, MISS, FAILURE, ERROR
    }

    private static final ActionConsistency CONSISTENCY_COUNT_ONE = new ActionConsistency.CountConsistency() {
        @Override
        public Integer getValue() {
            return 1;
        }
    };

    private final TiesCoordinatorServiceImpl service;

    private final ExecutorService healingExecutor = ForkJoinPool.commonPool(); // TODO FIXME Change executor!!!

    private final Random random;

    {
        Random roamingRandom;
        try {
            roamingRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Failed to get secure random generator. Fallback to non secure random generator!", e);
            roamingRandom = new Random();
        }
        random = roamingRandom;
    }

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

    private static TiesEntryExtended checkEntryIsValid(TiesEntryExtended entry) throws TiesServiceScopeException {
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
        Digest keyHashDigest = getDigest(DEFAULT_HASH_ALG);
        for (String fieldName : fieldNames) {
            byte[] fieldHash = mapper.apply(fieldName);
            if (null == fieldHash) {
                throw new TiesServiceScopeException(
                        "Field " + fieldName + " was not found in entry " + UPPERCASE_HEX.printHexBinary(entryHash));
            }
            if (LOG.isTraceEnabled())
                LOG.trace("AddedFieldHash for {}: {}", fieldName, UPPERCASE_HEX.printHexBinary(fieldHash));
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
    public void insert(TiesServiceScopeModificationAction action) throws TiesServiceScopeException {
        modification(action, (s, o) -> s.insert(o));
    }

    @Override
    public void update(TiesServiceScopeModificationAction action) throws TiesServiceScopeException {
        modification(action, (s, o) -> s.update(o));
    }

    @Override
    public void delete(TiesServiceScopeModificationAction action) throws TiesServiceScopeException {
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
            extends CheckedBiFunction<TiesServiceScope, TiesServiceScopeModificationAction, Void, TiesServiceScopeException> {

        @Override
        default Void apply(TiesServiceScope t, TiesServiceScopeModificationAction u) throws TiesServiceScopeException {
            applyVoid(t, u);
            return null;
        }

        void applyVoid(TiesServiceScope t, TiesServiceScopeModificationAction u) throws TiesServiceScopeException;

    }

    protected void modification(TiesServiceScopeModificationAction action, TiesServiceOperation operation) throws TiesServiceScopeException {

        TiesEntryExtended entry = checkEntryIsValid(action.getEntry());
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

        byte[] pkFieldsHash;
        {
            Map<String, TypedHashField> fhs = entry.getFieldHashes();
            Map<String, TypedValueField> fvs = entry.getFieldValues();
            Set<String> pkFieldsNames = keyFields.stream().map(f -> f.getName()).collect(Collectors.toSet());
            pkFieldsHash = getFieldsHash(entry.getHeader().getHash(), pkFieldsNames, fieldName -> {
                byte[] hash;
                TypedHashField fh = fhs.get(fieldName);
                hash = null == fh ? null : fh.getHash();
                if (null == hash) {
                    TypedValueField fv = fvs.get(fieldName);
                    hash = null == fv ? null : fv.getHash();
                }
                return hash;
            });
        }
        LOG.trace("PrimaryKeyFieldHash: {}", UPPERCASE_HEX.printHexBinary(pkFieldsHash));
        Set<? extends Node> nodes = sch.getNodes(tsn, tbn, pkFieldsHash);
        LOG.debug("CoordinatedModification {} nodes: {}", UPPERCASE_HEX.printHexBinary(pkFieldsHash), nodes);
        if (null == nodes || nodes.isEmpty()) {
            throw new TiesServiceScopeException("No target nodes found for request");
        }

        Map<Node, Future<CoordinatedResult<TiesServiceScopeResultAction.Result>>> resultWaiters;
        CompletionService<CoordinatedResult<TiesServiceScopeResultAction.Result>> completionService = new ExecutorCompletionService<>(
                ForkJoinPool.commonPool()); // TODO FIXME Change executor!!!
        {
            resultWaiters = new HashMap<>();
            TiesRouter router = service.getRouterService();
            for (Node node : nodes) {
                resultWaiters.put(node, completionService.submit(() -> {
                    CoordinatedResult<TiesServiceScopeResultAction.Result> coordinatedResult = service.getRequestPool().register();
                    try {
                        TiesTransportClient c = router.getClient(node);
                        c.request(new TiesServiceScopeConsumer() {
                            @Override
                            public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                                operation.apply(s, new TiesServiceScopeModificationAction() {

                                    @Override
                                    public ActionConsistency getConsistency() {
                                        return action.getConsistency();
                                    }

                                    @Override
                                    public BigInteger getMessageId() {
                                        return coordinatedResult.getId();
                                    }

                                    @Override
                                    public TiesEntryExtended getEntry() throws TiesServiceScopeException {
                                        return action.getEntry();
                                    }

                                    @Override
                                    public void setResult(Result result) throws TiesServiceScopeException {
                                        result.accept(new TiesServiceScopeModificationAction.Result.Visitor<Void>() {

                                            @Override
                                            public Void on(TiesServiceScopeModificationAction.Result.Success success)
                                                    throws TiesServiceScopeException {
                                                LOG.trace("Node request sent successfully for: {} messageId {}", node);
                                                return null;
                                            }

                                            @Override
                                            public Void on(TiesServiceScopeModificationAction.Result.Error error)
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

        for (Future<CoordinatedResult<TiesServiceScopeResultAction.Result>> result : resultWaiters.values()) {
            try {
                result.get().get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
            } catch (Throwable e) {
                LOG.error("Node request failed", e);
            }
        }
        HashSet<String> segregatedErrors = new HashSet<String>() {

            private static final long serialVersionUID = 3987824221222046320L;

            @Override
            public String toString() {
                Iterator<?> it = iterator();
                if (! it.hasNext())
                    return "";

                StringBuilder sb = new StringBuilder();
                sb.append(':').append(' ');
                for (;;) {
                    Object e = it.next();
                    sb.append(e == this ? "..." : e);
                    if (! it.hasNext())
                        return sb.toString();
                    sb.append(',').append(' ');
                }
            }
        };
        Map<ModificationResultType, Set<Node>> segregatedResults = ConsistencyArbiter.segregate(resultWaiters, futureResult -> {
            try {
                CoordinatedResult<TiesServiceScopeResultAction.Result> coordinatedResult = futureResult.get();
                TiesServiceScopeResultAction.Result result = coordinatedResult.get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                return result.accept(new TiesServiceScopeResultAction.Result.Visitor<ModificationResultType>() {

                    @Override
                    public ModificationResultType on(TiesServiceScopeModificationAction.Result result) throws TiesServiceScopeException {
                        return !Arrays.equals(result.getHeaderHash(), header.getHash()) //
                                ? ModificationResultType.MISS
                                : result.accept(new TiesServiceScopeModificationAction.Result.Visitor<ModificationResultType>() {
                                    @Override
                                    public ModificationResultType on(Success success) throws TiesServiceScopeException {
                                        return ModificationResultType.SUCCESS;
                                    }

                                    @Override
                                    public ModificationResultType on(Error error) throws TiesServiceScopeException {
                                        segregatedErrors.add(error.getError().getMessage().intern());
                                        return ModificationResultType.ERROR;
                                    }
                                });
                    }

                    @Override
                    public ModificationResultType on(TiesServiceScopeRecollectionAction.Result result) throws TiesServiceScopeException {
                        return ModificationResultType.FAILURE;
                    }

                    @Override
                    public ModificationResultType on(TiesServiceScopeHealingAction.Result result) throws TiesServiceScopeException {
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
            action.setResult(new TiesServiceScopeModificationAction.Result.Success() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }
            });
        } else if (results.contains(ModificationResultType.ERROR)) {
            Throwable error = new Throwable("Write was impossible" + segregatedErrors);
            action.setResult(new TiesServiceScopeModificationAction.Result.Error() {
                @Override
                public byte[] getHeaderHash() {
                    return header.getHash();
                }

                @Override
                public Throwable getError() {
                    return error;
                }
            });
        } else if (results.contains(ModificationResultType.MISS)) {
            Set<Node> missedNodes = segregatedResults.get(ModificationResultType.FAILURE);
            action.setResult(new TiesServiceScopeModificationAction.Result.Error() {
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
            action.setResult(new TiesServiceScopeModificationAction.Result.Error() {
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
            action.setResult(new TiesServiceScopeModificationAction.Result.Error() {
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
                healingDetection(pkFieldNames, pkHash -> sch.getNodes(tsn, tbn, pkHash), resultWaiters, tsn, tbn, fields);
            } catch (TiesServiceScopeException ex) {
                LOG.error("Healing failed for modification request {}", action.getMessageId(), ex);
            }
        });
    }

    @Override
    public void select(TiesServiceScopeRecollectionAction recollectionRequest) throws TiesServiceScopeException {

        Query query = recollectionRequest.getQuery();

        String tsn = query.getTablespaceName();
        String tbn = query.getTableName();

        TiesServiceSchema sch = service.getSchemaService();
        Set<FieldDescription> fields = sch.getFields(tsn, tbn);
        {
            Set<String> tableFields = fields.stream().map(f -> f.getName()).collect(Collectors.toSet());
            Stream<String> queryFieldNameStream = query.getSelectors().stream().map(s -> {
                try {
                    return s.accept(new network.tiesdb.service.scope.api.TiesServiceScopeRecollectionAction.Query.Selector.Visitor<String>() {

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

        Map<Node, Future<CoordinatedResult<TiesServiceScopeResultAction.Result>>> resultWaiters;
        CompletionService<CoordinatedResult<TiesServiceScopeResultAction.Result>> completionService = new ExecutorCompletionService<>(
                ForkJoinPool.commonPool()); // TODO FIXME Change executor!!!
        {
            resultWaiters = new HashMap<>();
            TiesRouter router = service.getRouterService();
            for (Node node : nodes) {
                resultWaiters.put(node, completionService.submit(() -> {
                    CoordinatedResult<TiesServiceScopeResultAction.Result> coordinatedResult = service.getRequestPool().register();
                    try {
                        TiesTransportClient c = router.getClient(node);
                        c.request(new TiesServiceScopeConsumer() {

                            @Override
                            public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                                s.select(new TiesServiceScopeRecollectionAction() {

                                    @Override
                                    public ActionConsistency getConsistency() {
                                        return recollectionRequest.getConsistency();
                                    }

                                    @Override
                                    public BigInteger getMessageId() {
                                        return coordinatedResult.getId();
                                    }

                                    @Override
                                    public Query getQuery() throws TiesServiceScopeException {
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
            for (Future<CoordinatedResult<TiesServiceScopeResultAction.Result>> result : resultWaiters.values()) {
                try {
                    result.get().get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                } catch (Throwable e) {
                    LOG.error("Node request failed", e);
                }
            }
        }

        ConsistencyArbiter arbiter = new ConsistencyArbiter(recollectionRequest.getConsistency(), sch.getReplicationFactor(tsn, tbn));

        LinkedList<Throwable> resultErrors = new LinkedList<>();
        Map<String, Set<Node>> segregatedResults = ConsistencyArbiter.segregate(resultWaiters,
                e -> UPPERCASE_HEX.printHexBinary(e.getEntryHeader().getHash()), result -> {
                    try {
                        return result.get().get()
                                .accept(new TiesServiceScopeResultAction.Result.Visitor<Stream<TiesServiceScopeRecollectionAction.Result.Entry>>() {

                                    @Override
                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(TiesServiceScopeModificationAction.Result result)
                                            throws TiesServiceScopeException {
                                        LOG.error("Illegal result for recollection response: {}", result);
                                        return Stream.empty();
                                    }

                                    @Override
                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(TiesServiceScopeHealingAction.Result result)
                                            throws TiesServiceScopeException {
                                        LOG.error("Illegal result for recollection response: {}", result);
                                        return Stream.empty();
                                    }

                                    @Override
                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(TiesServiceScopeRecollectionAction.Result result)
                                            throws TiesServiceScopeException {
                                        return result.accept(
                                                new TiesServiceScopeRecollectionAction.Result.Visitor<Stream<TiesServiceScopeRecollectionAction.Result.Entry>>() {
                                                    @Override
                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(
                                                            TiesServiceScopeRecollectionAction.Success result) throws TiesServiceScopeException {
                                                        return result.getEntries().parallelStream();
                                                    }

                                                    @Override
                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(
                                                            TiesServiceScopeRecollectionAction.Error result) throws TiesServiceScopeException {
                                                        LOG.error("Error result for recollection response: {}", result);
                                                        resultErrors.addAll(result.getErrors());
                                                        return Stream.empty();
                                                    }

                                                    @Override
                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(Partial result)
                                                            throws TiesServiceScopeException {
                                                        LOG.error("Partial result for recollection response: {}", result);
                                                        resultErrors.addAll(result.getErrors());
                                                        return result.getEntries().parallelStream();
                                                    }
                                                });
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
            Set<Node> failedNodes = segregatedResults.get(SEGREGATION_ERROR);
            if (arbiterEntryHashes.isEmpty() && null != failedNodes && !failedNodes.isEmpty()) {
                resultErrors.add(new TiesServiceScopeException("Read failed for nodes " + failedNodes));
                recollectionRequest.setResult(//
                        new TiesServiceScopeRecollectionAction.Error() {
                            @Override
                            public List<Throwable> getErrors() {
                                return resultErrors;
                            }
                        });
            } else {
                List<TiesServiceScopeRecollectionAction.Result.Entry> arbiterEntries = arbiterEntryHashes.isEmpty() //
                        ? Collections.emptyList()
                        : resultWaiters.values().parallelStream() //
                                .flatMap(result -> {
                                    try {
                                        return result.get().get().accept(
                                                new TiesServiceScopeResultAction.Result.Visitor<Stream<TiesServiceScopeRecollectionAction.Result.Entry>>() {

                                                    @Override
                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(
                                                            TiesServiceScopeModificationAction.Result result) throws TiesServiceScopeException {
                                                        LOG.error("Illegal result for recollection: {}", result);
                                                        return Stream.empty();
                                                    }

                                                    @Override
                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(
                                                            TiesServiceScopeRecollectionAction.Result result) throws TiesServiceScopeException {
                                                        return result.accept(
                                                                new TiesServiceScopeRecollectionAction.Result.Visitor<Stream<TiesServiceScopeRecollectionAction.Result.Entry>>() {
                                                                    @Override
                                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(
                                                                            TiesServiceScopeRecollectionAction.Success result)
                                                                            throws TiesServiceScopeException {
                                                                        return result.getEntries().parallelStream();
                                                                    }

                                                                    @Override
                                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(
                                                                            TiesServiceScopeRecollectionAction.Error result)
                                                                            throws TiesServiceScopeException {
                                                                        LOG.error("Error result for recollection response: {}", result);
                                                                        return Stream.empty();
                                                                    }

                                                                    @Override
                                                                    public Stream<Entry> on(Partial result)
                                                                            throws TiesServiceScopeException {
                                                                        LOG.error("Partial result for recollection response: {}", result);
                                                                        return result.getEntries().parallelStream();
                                                                    }
                                                                });
                                                    }

                                                    @Override
                                                    public Stream<TiesServiceScopeRecollectionAction.Result.Entry> on(
                                                            TiesServiceScopeHealingAction.Result result) throws TiesServiceScopeException {
                                                        LOG.error("Illegal result for recollection: {}", result);
                                                        return Stream.empty();
                                                    }
                                                });
                                    } catch (CancellationException | TiesServiceScopeException | InterruptedException | ExecutionException
                                            | TimeoutException e) {
                                    }
                                    return Stream.empty();
                                }) //
                                .filter(e -> arbiterEntryHashes.contains(UPPERCASE_HEX.printHexBinary(e.getEntryHeader().getHash()))) //
                                .filter(distinct(e -> UPPERCASE_HEX.printHexBinary(e.getEntryHeader().getHash()))) //
                                .collect(Collectors.toList());

                recollectionRequest.setResult(resultErrors.isEmpty()//
                        ? new TiesServiceScopeRecollectionAction.Success() {
                            @Override
                            public List<Entry> getEntries() {
                                return arbiterEntries;
                            }
                        }
                        : new TiesServiceScopeRecollectionAction.Partial() {
                            @Override
                            public List<Throwable> getErrors() {
                                return resultErrors;
                            }

                            @Override
                            public List<Entry> getEntries() {
                                return arbiterEntries;
                            }
                        });
            }
        } finally {
            healingExecutor.execute(() -> {

                Set<String> pkFieldNames = fields.parallelStream().filter(f -> f.isPrimaryKey()).map(f -> f.getName())
                        .collect(Collectors.toSet());
                try {
                    healingDetection(pkFieldNames, pkHash -> sch.getNodes(tsn, tbn, pkHash), resultWaiters, tsn, tbn, fields);
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
            Map<Node, Future<CoordinatedResult<TiesServiceScopeResultAction.Result>>> resultWaiters, String tablespaceName, String tableName,
            Set<FieldDescription> fields) throws TiesServiceScopeException {

        Map<String, Map<String, Map<Node, TiesEntry>>> healingExpectantMap = resultWaiters.entrySet().parallelStream().flatMap(e -> {
            try {
                TiesServiceScopeResultAction.Result result = e.getValue().get().get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT);
                return result.accept(new TiesServiceScopeResultAction.Result.Visitor<Stream<HealingMappingEntry<Node, TiesEntry>>>() {

                    @Override
                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeModificationAction.Result result)
                            throws TiesServiceScopeException {
                        // TODO Auto-generated method stub
                        RuntimeException err = new RuntimeException("not yet implemented");
                        err.setStackTrace(new StackTraceElement[] { err.getStackTrace()[0] });
                        LOG.debug("Modification healing not yet implemented", err);
                        return Stream.<HealingMappingEntry<Node, TiesEntry>>empty();
                    }

                    @Override
                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeRecollectionAction.Result recollectionResult)
                            throws TiesServiceScopeException {
                        return recollectionResult
                                .accept(new TiesServiceScopeRecollectionAction.Result.Visitor<Stream<HealingMappingEntry<Node, TiesEntry>>>() {

                                    @Override
                                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeRecollectionAction.Success success)
                                            throws TiesServiceScopeException {
                                        return success.getEntries().parallelStream().map(entry -> {
                                            try {
                                                byte[] entryHash = entry.getEntryHeader().getHash();
                                                Map<String, byte[]> fhs = entry.getEntryFields().parallelStream()
                                                        .filter(f -> primaryKeyFieldNames.contains(f.getName()))
                                                        .collect(Collectors.toMap(f -> f.getName(), f -> {
                                                            try {
                                                                return f.accept(
                                                                        new TiesServiceScopeRecollectionAction.Result.Field.Visitor<byte[]>() {

                                                                            @Override
                                                                            public byte[] on(HashField field)
                                                                                    throws TiesServiceScopeException {
                                                                                return field.getHash();
                                                                            }

                                                                            @Override
                                                                            public byte[] on(RawField field)
                                                                                    throws TiesServiceScopeException {
                                                                                byte[] hash = field.getHash();
                                                                                if (null == hash) {
                                                                                    Digest d = getDigest(DEFAULT_HASH_ALG);
                                                                                    d.update(field.getRawValue());
                                                                                    hash = new byte[d.getDigestSize()];
                                                                                    d.doFinal(hash);
                                                                                }
                                                                                return hash;
                                                                            }

                                                                            @Override
                                                                            public byte[] on(ValueField field)
                                                                                    throws TiesServiceScopeException {
                                                                                throw new TiesServiceScopeException(
                                                                                        "No TiesDB ValueFields should appear on Coordinator!");
                                                                            }
                                                                        });
                                                            } catch (TiesServiceScopeException ex) {
                                                                LOG.error("Can't get field hash for field: " + f.getName(), ex);
                                                                return null;
                                                            }
                                                        }));
                                                byte[] pkFieldsHash = getFieldsHash(entryHash, primaryKeyFieldNames, fieldName -> {
                                                    return fhs.get(fieldName);
                                                });
                                                LOG.trace("PrimaryKeyFieldHash: {}", UPPERCASE_HEX.printHexBinary(pkFieldsHash));
                                                return new HealingMappingEntry<Node, TiesEntry>( //
                                                        UPPERCASE_HEX.printHexBinary(pkFieldsHash), //
                                                        UPPERCASE_HEX.printHexBinary(entryHash), //
                                                        e.getKey(), //
                                                        entry);
                                            } catch (TiesServiceScopeException ex) {
                                                LOG.error("Result filtering failure", ex);
                                                return null;
                                            }
                                        }).filter(m -> null != m);
                                    }

                                    @Override
                                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeRecollectionAction.Error error)
                                            throws TiesServiceScopeException {
                                        LOG.trace("Recollection error should not be healed: {}", error);
                                        return Stream.<HealingMappingEntry<Node, TiesEntry>>empty();
                                    }

                                    @Override
                                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(Partial partial)
                                            throws TiesServiceScopeException {
                                        LOG.warn("Recollection partial result");
                                        return on((TiesServiceScopeRecollectionAction.Success) partial);
                                    }

                                });
                    }

                    @Override
                    public Stream<HealingMappingEntry<Node, TiesEntry>> on(TiesServiceScopeHealingAction.Result result)
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
            String pkFieldsHash = pke.getKey();
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

            Set<? extends Node> nodes = nodesMapper.apply(UPPERCASE_HEX.parseHexBinary(pkFieldsHash));
            Map<String, String> typeMap = Collections
                    .unmodifiableMap(fields.parallelStream().collect(Collectors.toMap(f -> f.getName(), f -> f.getType())));
            entryMap.entrySet().parallelStream().forEach(ene -> {
                String enHashStr = ene.getKey();
                Map<Node, TiesEntry> nodesEntryMap = ene.getValue();
                if (!nodesEntryMap.keySet().containsAll(nodes)) {
                    Set<Node> nodesForHealing = new HashSet<>(nodes);
                    nodesForHealing.removeAll(nodesEntryMap.keySet());
                    LOG.debug("Entry {} should be healed\n\t   to nodes: {}\n\t from nodes: {}", //
                            enHashStr, nodesForHealing, nodesEntryMap.keySet());
                    TiesEntry entry = nodesEntryMap.values().iterator().next();
                    CoordinatedResult<TiesServiceScopeResultAction.Result> coordinatedResult = service.getRequestPool().register();
                    Node donorNode = getRandom(nodesEntryMap.keySet());
                    try {
                        Set<String> keyFieldNames = Collections.unmodifiableSet(
                                fields.stream().filter(f -> f.isPrimaryKey()).map(f -> f.getName()).collect(Collectors.toSet()));
                        router.getClient(donorNode).request(new TiesServiceScopeConsumer() {
                            @Override
                            public void accept(TiesServiceScope serviceScope) throws TiesServiceScopeException {
                                serviceScope.select(new TiesServiceScopeRecollectionAction() {

                                    private final Query query = new Query() {

                                        private final List<Selector> selectors = entry.getFields().parallelStream() //
                                                .filter(f -> f instanceof TiesEntry.HashField) //
                                                .map(f -> new Selector.FieldSelector() {
                                                    @Override
                                                    public String getFieldName() {
                                                        return f.getName();
                                                    }
                                                }) //
                                                .collect(Collectors.toList());

                                        private final List<Filter> filters = entry.getFields().parallelStream() //
                                                .filter(f -> f instanceof TiesEntry.ValueField) //
                                                .filter(f -> keyFieldNames.contains(f.getName())) //
                                                .map(f -> new Filter() {

                                                    private final List<Argument> args = Collections
                                                            .singletonList(new Argument.ValueArgument() {

                                                                @Override
                                                                public String getType() {
                                                                    return typeMap.get(f.getName());
                                                                }

                                                                @Override
                                                                public byte[] getRawValue() {
                                                                    return ((TiesEntry.ValueField) f).getValue();
                                                                }

                                                                @Override
                                                                public Object getValue() throws TiesServiceScopeException {
                                                                    throw new TiesServiceScopeException(
                                                                            "No serialization should be used on healing");
                                                                }

                                                            });

                                                    @Override
                                                    public String getFieldName() {
                                                        return f.getName();
                                                    }

                                                    @Override
                                                    public String getName() {
                                                        return "=";
                                                    }

                                                    @Override
                                                    public List<Argument> getArguments() {
                                                        return args;
                                                    }
                                                }) //
                                                .collect(Collectors.toList());

                                        @Override
                                        public String getTablespaceName() {
                                            return tablespaceName;
                                        }

                                        @Override
                                        public String getTableName() {
                                            return tableName;
                                        }

                                        @Override
                                        public List<Selector> getSelectors() {
                                            return selectors;
                                        }

                                        @Override
                                        public List<Filter> getFilters() {
                                            return filters;
                                        }

                                        @Override
                                        public List<? extends TiesCheque> getCheques() {
                                            return Collections.emptyList(); // TODO FIXME Add cheque creation for healing!
                                        }
                                    };

                                    @Override
                                    public ActionConsistency getConsistency() {
                                        return CONSISTENCY_COUNT_ONE;
                                    }

                                    @Override
                                    public BigInteger getMessageId() {
                                        return coordinatedResult.getId();
                                    }

                                    @Override
                                    public void setResult(TiesServiceScopeRecollectionAction.Result result) throws TiesServiceScopeException {
                                        throw new TiesServiceScopeException("Coordinator should not handle ClientScope results");
                                    }

                                    @Override
                                    public Query getQuery() {
                                        return query;
                                    }
                                });
                            }
                        });
                    } catch (TiesRoutingException e) {
                        LOG.warn("Route was not found for node: {}", donorNode, e);
                        coordinatedResult.fail(e);
                    } catch (Throwable e) {
                        LOG.warn("Node request failed for node: {} scope {}", donorNode, e);
                        coordinatedResult.fail(e);
                    }
                    try {
                        coordinatedResult.get(NODE_REQUEST_TIMEOUT, NODE_REQUEST_TIMEOUT_UNIT) //
                                .accept(new TiesServiceScopeResultAction.Result.Visitor<Void>() {

                                    @Override
                                    public Void on(TiesServiceScopeModificationAction.Result result) throws TiesServiceScopeException {
                                        throw new TiesServiceScopeException("Illegal resutl for recollection request: " + result);
                                    }

                                    @Override
                                    public Void on(TiesServiceScopeHealingAction.Result result) throws TiesServiceScopeException {
                                        throw new TiesServiceScopeException("Illegal resutl for recollection request: " + result);
                                    }

                                    @Override
                                    public Void on(TiesServiceScopeRecollectionAction.Result result) throws TiesServiceScopeException {
                                        return result.accept(new TiesServiceScopeRecollectionAction.Result.Visitor<Void>() {

                                            private final <T extends TiesServiceScopeRecollectionAction.Success> void propagate(T result) {
                                                result.getEntries().forEach(resultEntry -> {
                                                    healingPropagation(router, nodesForHealing, mergeEntries(entry, resultEntry),
                                                            tablespaceName, tableName, typeMap);
                                                });
                                            }

                                            @Override
                                            public Void on(TiesServiceScopeRecollectionAction.Success success) throws TiesServiceScopeException {
                                                LOG.trace("Recollection success propagate");
                                                propagate(success);
                                                LOG.trace("Recollection success result propagated");
                                                return null;
                                            }

                                            @Override
                                            public Void on(Partial partial) throws TiesServiceScopeException {
                                                LOG.trace("Recollection partial propagate");
                                                propagate(partial);
                                                LOG.trace("Recollection partial result propagated");
                                                return null;
                                            }

                                            @Override
                                            public Void on(TiesServiceScopeRecollectionAction.Error error) throws TiesServiceScopeException {
                                                LOG.trace("Recollection error should not propagate");
                                                return null;
                                            }
                                        });
                                    }
                                });
                    } catch (Throwable e) {
                        LOG.error("Node request failed", e);
                    }
                }
            });
        });

    }

    private static final TiesEntry mergeEntries(TiesEntry entry, Entry resultEntry) throws IllegalArgumentException {
        if (!Arrays.equals(entry.getHeader().getHash(), resultEntry.getHeader().getHash())) {
            throw new IllegalArgumentException("Entries does not match");
        }
        List<? extends TiesEntry.Field> fields = Stream
                .concat(entry.getFields().parallelStream(), resultEntry.getEntryFields().parallelStream())
                .filter(f -> f instanceof TiesEntry.ValueField)
                .collect(Collectors.toMap(f -> f.getName(), f -> f, (a, b) -> a instanceof TiesEntry.ValueField ? a : b)).values()
                .parallelStream().collect(Collectors.toList());

        return new TiesEntry() {

            @Override
            public TiesEntryHeader getHeader() {
                return entry.getHeader();
            }

            @Override
            public List<? extends Field> getFields() {
                return fields;
            }

        };
    }

    private final <T> T getRandom(Collection<T> items) {
        if (items.size() == 0) {
            throw new IllegalArgumentException("Failed to get item from empty collection");
        } else if (items.size() == 1) {
            return items.iterator().next();
        }
        int i = random.nextInt(items.size());
        return items.parallelStream().skip(i).findFirst().get();
    }

    private void healingPropagation(TiesRouter router, Set<Node> nodes, TiesEntry entry, String tablespaceName, String tableName,
            Map<String, String> typeMap) {

        nodes.forEach(node -> {
            CoordinatedResult<TiesServiceScopeResultAction.Result> coordinatedResult = service.getRequestPool().register();
            try {
                TiesTransportClient c = router.getClient(node);
                c.request(new TiesServiceScopeConsumer() {

                    private final TiesEntryExtended extEntry = new TiesEntryExtended() {

                        private final Map<String, TypedHashField> hashFields;
                        private final Map<String, TypedValueField> valueFields;

                        {
                            List<TypedField> allFields = entry.getFields().parallelStream().map(f -> {
                                try {
                                    return f.accept(new TiesEntry.Field.Visitor<TiesEntryExtended.TypedField>() {
                                        @Override
                                        public TiesEntryExtended.TypedHashField on(TiesEntry.HashField hashField)
                                                throws TiesServiceScopeException {
                                            return new TiesEntryExtended.TypedHashField() {

                                                @Override
                                                public String getName() {
                                                    return hashField.getName();
                                                }

                                                @Override
                                                public byte[] getHash() {
                                                    return hashField.getHash();
                                                }

                                                @Override
                                                public String getType() {
                                                    return typeMap.get(getName());
                                                }

                                            };
                                        }

                                        @Override
                                        public TiesEntryExtended.TypedValueField on(TiesEntry.ValueField valueField)
                                                throws TiesServiceScopeException {
                                            return new TiesEntryExtended.TypedValueField() {

                                                @Override
                                                public String getName() {
                                                    return valueField.getName();
                                                }

                                                @Override
                                                public byte[] getValue() {
                                                    return valueField.getValue();
                                                }

                                                @Override
                                                public byte[] getHash() {
                                                    return valueField.getHash();
                                                }

                                                @Override
                                                public Object getObject() {
                                                    throw new IllegalStateException("Healing should not be used with object serialization");
                                                }

                                                @Override
                                                public String getType() {
                                                    return typeMap.get(getName());
                                                }

                                            };
                                        }

                                    });
                                } catch (TiesServiceScopeException e) {
                                    LOG.error("Failed to convert field for healing: {}", f, c);
                                    return null;
                                }
                            }).filter(f -> null != f).collect(Collectors.toList());
                            hashFields = allFields.parallelStream().filter(f -> f instanceof TypedHashField)
                                    .collect(Collectors.toMap(f -> f.getName(), f -> (TypedHashField) f));
                            valueFields = allFields.parallelStream().filter(f -> f instanceof TypedValueField)
                                    .collect(Collectors.toMap(f -> f.getName(), f -> (TypedValueField) f));
                        }

                        @Override
                        public TiesEntryHeader getHeader() {
                            return entry.getHeader();
                        }

                        @Override
                        public String getTablespaceName() {
                            return tablespaceName;
                        }

                        @Override
                        public String getTableName() {
                            return tableName;
                        }

                        @Override
                        public Map<String, TypedHashField> getFieldHashes() {
                            return hashFields;
                        }

                        @Override
                        public Map<String, TypedValueField> getFieldValues() {
                            return valueFields;
                        }

                        @Override
                        public List<? extends TiesCheque> getCheques() {
                            // FIXME!!! Cheques erasure!!!
                            return Collections.emptyList();
                        }

                    };

                    @Override
                    public void accept(TiesServiceScope s) throws TiesServiceScopeException {
                        s.heal(new TiesServiceScopeHealingAction() {

                            @Override
                            public BigInteger getMessageId() {
                                return coordinatedResult.getId();
                            }

                            @Override
                            public void setResult(Result result) throws TiesServiceScopeException {
                                throw new TiesServiceScopeException("Coordinator should not handle ClientScope results");
                            }

                            @Override
                            public TiesEntryExtended getEntry() {
                                return extEntry;
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
    public void heal(TiesServiceScopeHealingAction action) throws TiesServiceScopeException {
        // TODO Auto-generated method stub
        throw new TiesServiceScopeException("Healing delegation not implemented yet");
    }

    @Override
    public void schema(TiesServiceScopeSchemaAction schemaRequest) throws TiesServiceScopeException {
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
    public void result(TiesServiceScopeResultAction result) throws TiesServiceScopeException {
        if (!service.getRequestPool().complete(result.getMessageId(), result.getResult())) {
            throw new TiesServiceScopeException("Request completion failed for messageId: " + result.getMessageId());
        }
    }

    @Override
    public void billing(TiesServiceScopeBillingAction billingRequest) throws TiesServiceScopeException {
        throw new TiesServiceScopeException("Ties Coordinator should not handle billing request");
    }

}
