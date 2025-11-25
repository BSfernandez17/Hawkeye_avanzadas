package org.example.Pool;

public interface IObjectPool {
    IPoolableObject getObject() throws Exception;
    void releaseObject(IPoolableObject object);
}