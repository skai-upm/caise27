package es.upm.amusementpark.business.controller;

import java.io.IOException;

/**
 * Holds the single instance of a controller for the current execution. The
 * instance is created once at startup by {@link #initialize}; later accesses go
 * through {@link #get}. {@link #reset} ends the execution's instance.
 */
final class SingletonSlot<T> {

    /** Creates the controller instance; may load data. */
    interface Factory<T> {
        T create() throws IOException;
    }

    private final String name;
    private T instance;

    SingletonSlot(String name) {
        this.name = name;
    }

    synchronized T initialize(Factory<T> factory) throws IOException {
        if (instance != null) {
            throw new IllegalStateException(name + " is already initialized");
        }
        instance = factory.create();
        return instance;
    }

    synchronized T get() {
        if (instance == null) {
            throw new IllegalStateException(name + " is not initialized");
        }
        return instance;
    }

    synchronized void reset() {
        instance = null;
    }
}
