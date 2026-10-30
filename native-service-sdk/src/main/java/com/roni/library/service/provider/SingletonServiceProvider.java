package com.roni.library.service.provider;

import com.roni.library.service.IService;

/**
 * Thread-safe singleton service provider wrapper supporting both pre-existing instances and lazy factories.
 *
 * @param <T> type of service
 */
public class SingletonServiceProvider<T extends IService> implements IServiceProvider<T> {

    private final IServiceProvider<T> factory;
    private volatile T instance;
    private final Object lock = new Object();

    private SingletonServiceProvider(T existingInstance, IServiceProvider<T> factory) {
        this.instance = existingInstance;
        this.factory = factory;
    }

    /**
     * Creates a provider with an already instantiated service instance.
     *
     * @param existingInstance the existing instance
     * @param <T>              type of service
     * @return singleton provider
     */
    public static <T extends IService> SingletonServiceProvider<T> of(T existingInstance) {
        if (existingInstance == null) {
            throw new IllegalArgumentException("existingInstance must not be null");
        }
        return new SingletonServiceProvider<>(existingInstance, null);
    }

    /**
     * Creates a lazy provider that invokes the factory on first access.
     *
     * @param factory provider factory
     * @param <T>     type of service
     * @return lazy singleton provider
     */
    public static <T extends IService> SingletonServiceProvider<T> lazy(IServiceProvider<T> factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null");
        }
        return new SingletonServiceProvider<>(null, factory);
    }

    @Override
    public T get() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    if (factory != null) {
                        instance = factory.get();
                    }
                }
            }
        }
        return instance;
    }
}
