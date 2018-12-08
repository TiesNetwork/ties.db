package network.tiesdb.coordinator.service.impl.scope;

import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.Visitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.CountConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.PercentConsistency;
import network.tiesdb.service.scope.api.TiesServiceScopeAction.Distributed.ActionConsistency.QuorumConsistency;

public class ConsistencyArbiter {

    private static class PercentStrategy extends CountStrategy {

        public PercentStrategy(int total, int threshold) {
            super(Math.round((total * threshold) / 100f));
        }

    }

    private static class QuorumStrategy extends CountStrategy {

        public QuorumStrategy(int total) {
            super((total / 2) + 1);
        }

    }

    private static class CountStrategy extends ArbiterStrategy {

        private final int count;

        public CountStrategy(int count) {
            this.count = count;
        }

        @Override
        public boolean check(int count) {
            return count >= this.count;
        }

    }

    private static abstract class ArbiterStrategy {

        public abstract boolean check(int count);

    }

    private final ArbiterStrategy strategy;

    public ConsistencyArbiter(ActionConsistency consistency, int total) {
        this.strategy = consistency.accept(new Visitor<ArbiterStrategy>() {

            @Override
            public ArbiterStrategy on(CountConsistency countConsistency) {
                return new CountStrategy(countConsistency.getValue());
            }

            @Override
            public ArbiterStrategy on(PercentConsistency percentConsistency) {
                return new PercentStrategy(total, percentConsistency.getValue());
            }

            @Override
            public ArbiterStrategy on(QuorumConsistency quorumConsistency) {
                return new QuorumStrategy(total);
            }
        });
    }

    public <P> Set<P> getResults(Map<P, ? extends Collection<?>> partitionMap) {
        return partitionMap.entrySet().parallelStream().filter(e -> strategy.check(e.getValue().size())).map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    public static <N, T, P> Map<P, Set<N>> segregate(Map<N, T> resultMap, Function<T, P> partitioner) {
        Map<P, Set<N>> partitionMap = new HashMap<>();
        resultMap.forEach((node, result) -> {
            P pk = partitioner.apply(result);
            Set<N> partition = partitionMap.get(pk);
            if (null == partition) {
                partition = new HashSet<>();
                partitionMap.put(pk, partition);
            }
            partition.add(node);
        });
        return partitionMap;
    }

}
