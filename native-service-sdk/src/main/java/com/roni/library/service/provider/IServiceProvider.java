package com.roni.library.service.provider;

import com.roni.library.service.IService;

/**
 * Generic factory provider interface for resolving {@link IService} instances.
 *
 * @param <T> type of service
 */
public interface IServiceProvider<T extends IService> {

    /**
     * Resolves or produces an instance of the service.
     *
     * @return service instance
     */
    T get();
}
