package org.example.Pool;

public class CamaraPool extends AbstractObjectPool {
    public CamaraPool(int min, int max, int timeoutMillis) {
        super(min, max, timeoutMillis, new CamaraFactory());
    }
}