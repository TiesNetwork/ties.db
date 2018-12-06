package network.tiesdb.coordinator.service.impl.scope;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TiesCoordinatedRequestPool<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TiesCoordinatedRequestPool.class);

    private static final int DEFAULT_REGISTER_RETRY_COUNT = 65535;

    private final Map<BigInteger, CoordinatedResultImpl> waitingResults;
    private final AtomicLong idCounter = new AtomicLong(1);

    public static interface CoordinatedResult<T> {

        BigInteger getId();

        boolean fail(Throwable error);

        T get(int timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    }

    protected class CoordinatedResultImpl implements CoordinatedResult<T> {

        private final BigInteger id;
        private final CompletableFuture<T> futureResult;

        protected CoordinatedResultImpl(BigInteger id, CompletableFuture<T> futureResult) {
            this.id = id;
            this.futureResult = futureResult;
        }

        @Override
        public BigInteger getId() {
            return id;
        }

        public boolean complete(T result) {
            waitingResults.remove(getId());
            return futureResult.complete(result);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            waitingResults.remove(getId());
            return futureResult.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean fail(Throwable error) {
            waitingResults.remove(getId());
            return futureResult.completeExceptionally(error);
        }

        @Override
        public T get(int timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return futureResult.get(timeout, unit);
        }

        private TiesCoordinatedRequestPool<T> getOuterType() {
            return TiesCoordinatedRequestPool.this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((futureResult == null) ? 0 : futureResult.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            @SuppressWarnings("unchecked")
            CoordinatedResultImpl other = (CoordinatedResultImpl) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (futureResult == null) {
                if (other.futureResult != null)
                    return false;
            } else if (!futureResult.equals(other.futureResult))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }
    }

    public TiesCoordinatedRequestPool(int initialCapacity) {
        this.waitingResults = new ConcurrentHashMap<>(initialCapacity);
    }

    CoordinatedResult<T> register() {

        int registerRetry = DEFAULT_REGISTER_RETRY_COUNT;
        CompletableFuture<T> futureResult = new CompletableFuture<T>();
        while (registerRetry-- > 0) {
            final BigInteger id = BigInteger.valueOf(idCounter.getAndIncrement());
            final CoordinatedResultImpl result = new CoordinatedResultImpl(id, futureResult);
            if (null != waitingResults.putIfAbsent(id, result)) {
                continue;
            }
            return result;
        }
        throw new RuntimeException("Can't register CoordinatedResult. Pool is too crowded.");
    }

    void cancelAll() {
        waitingResults.values().forEach(result -> result.cancel(true));
    }

    boolean cancel(BigInteger id) {
        CoordinatedResultImpl result = waitingResults.get(id);
        return null != result && result.cancel(true);
    }

    boolean complete(BigInteger id, T resultValue) {
        CoordinatedResultImpl result = waitingResults.get(id);
        return null != result && result.complete(resultValue);
    }

}
