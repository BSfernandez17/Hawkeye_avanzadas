package org.example.Pool;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class AbstractObjectPool implements IObjectPool {

    private final int max;
    private final int min;
    private final int timeoutMillis;
    private final IObjectFactory factory;

    private final Deque<IPoolableObject> available = new ArrayDeque<>();
    private final Deque<IPoolableObject> inUse = new ArrayDeque<>();

    protected AbstractObjectPool(int min, int max, int timeoutMillis, IObjectFactory factory) {
        if (min < 0 || max < 1 || min > max) {
            throw new IllegalArgumentException("Invalid pool size configuration");
        }
        this.min = min;
        this.max = max;
        this.timeoutMillis = timeoutMillis;
        this.factory = factory;
        preallocate();
    }

    private void preallocate() {
        for (int i = 0; i < min; i++) {
            available.add(factory.createNew());
        }
    }

    @Override
    public synchronized IPoolableObject getObject() throws Exception {
        Instant start = Instant.now();
        while (available.isEmpty()) {
            if (totalObjects() < max) {
                available.add(factory.createNew());
                break;
            }
            long waited = Duration.between(start, Instant.now()).toMillis();
            if (waited >= timeoutMillis) {
                throw new RuntimeException("Timeout waiting for pooled object");
            }
            wait(50); // small pause before re-checking
        }
        IPoolableObject obj = available.poll();
        inUse.add(obj);
        return obj;
    }

    @Override
    public synchronized void releaseObject(IPoolableObject object) {
        if (object == null) return;
        if (inUse.remove(object)) {
            object.operation(); // reset state
            available.add(object);
            notifyAll();
        }
    }

    private int totalObjects() {
        return available.size() + inUse.size();
    }
}