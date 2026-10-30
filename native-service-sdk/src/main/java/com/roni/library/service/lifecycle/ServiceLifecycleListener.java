package com.roni.library.service.lifecycle;

import com.roni.library.service.IService;

/**
 * Listener interface to observe lifecycle changes and failures of {@link IService}.
 */
public interface ServiceLifecycleListener {

    /**
     * Called when an {@link IService} transitions from one {@link ServiceState} to another.
     *
     * @param service  the service changing state
     * @param oldState previous state
     * @param newState new state
     */
    void onStateChanged(IService service, ServiceState oldState, ServiceState newState);

    /**
     * Called when an unhandled exception occurs in an {@link IService}.
     *
     * @param service the affected service
     * @param error   the error or exception thrown
     */
    void onError(IService service, Throwable error);
}
