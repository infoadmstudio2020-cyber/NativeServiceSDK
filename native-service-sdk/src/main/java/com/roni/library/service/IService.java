package com.roni.library.service;

import com.roni.library.service.lifecycle.ServiceLifecycleListener;
import com.roni.library.service.lifecycle.ServiceState;

/**
 * Universal service contract for components managed by {@link com.roni.library.service.ServiceRegistry}.
 * Inherits and standardizes the Layer 0 universal contract from {@link com.roni.library.contracts.service.IService}.
 */
public interface IService extends com.roni.library.contracts.service.IService {

    /**
     * Unique identifier for this service.
     *
     * @return unique string ID
     */
    String getServiceId();

    /**
     * Returns the service name matching {@link com.roni.library.contracts.service.IService#getServiceName()}.
     * Defaults to {@link #getServiceId()}.
     *
     * @return service name string
     */
    @Override
    default String getServiceName() {
        return getServiceId();
    }

    /**
     * Layer 0 contract hook invoked when starting the service.
     */
    @Override
    default void onStart() {
        start();
    }

    /**
     * Layer 0 contract hook invoked when stopping the service.
     */
    @Override
    default void onStop() {
        stop();
    }

    /**
     * Execution priority during batch initialization/startup. Higher values execute first.
     *
     * @return priority integer
     */
    int getPriority();

    /**
     * Checks if the service is currently available for consumers.
     *
     * @return true if available
     */
    boolean isAvailable();

    /**
     * Current lifecycle state of the service.
     *
     * @return {@link ServiceState}
     */
    ServiceState getState();

    /**
     * Checks if the service is in {@link ServiceState#RUNNING} state.
     *
     * @return true if running
     */
    boolean isRunning();

    /**
     * Checks if the service has been permanently disposed.
     *
     * @return true if disposed
     */
    boolean isDisposed();

    /**
     * Initializes the service and prepares internal resources.
     */
    void initialize();

    /**
     * Starts active operations of the service.
     */
    void start();

    /**
     * Temporarily or permanently stops the service operations.
     */
    void stop();

    /**
     * Releases all held resources and marks service as disposed.
     */
    void dispose();

    /**
     * Registers a lifecycle listener for this service.
     *
     * @param listener listener to receive callbacks
     */
    void addLifecycleListener(ServiceLifecycleListener listener);

    /**
     * Unregisters a lifecycle listener.
     *
     * @param listener listener to remove
     */
    void removeLifecycleListener(ServiceLifecycleListener listener);
}
