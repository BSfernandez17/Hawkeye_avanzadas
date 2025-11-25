package org.example.Pool;

import org.example.Model.Camara;

public class CamaraFactory implements IObjectFactory {
    @Override
    public IPoolableObject createNew() {
        return new Camara();
    }
}