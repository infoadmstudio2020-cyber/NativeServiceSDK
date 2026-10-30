package com.roni.library.service;

import com.roni.library.contracts.common.IDisposable;
import com.roni.library.logger.NativeLogger;
import com.roni.library.service.lifecycle.ServiceLifecycleListener;
import com.roni.library.service.lifecycle.ServiceState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract base class providing core lifecycle management and observer dispatch for {@link IService}.
 * Implements Layer 0 {@link IDisposable} and uses Layer 1 {@link NativeLogger} for diagnostic logging.
 */
public abstract class BaseService implements IService, IDisposable {

    private final String serviceId;
    private final int priority;
    private volatile ServiceState state = ServiceState.CREATED;
    private final List<ServiceLifecycleListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs a base service with default priority (0).
     *
     * @param serviceId unique service identifier
     */
    protected BaseService(String serviceId) {
        this(serviceId, 0);
    }

    /**
     * Constructs a base service with specified priority.
     *
     * @param serviceId unique service identifier
     * @param priority  priority value
     */
    protected BaseService(String serviceId, int priority) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("serviceId must not be null or empty");
        }
        this.serviceId = serviceId;
        this.priority = priority;
    }

    @Override
    public String getServiceName() {
        return getServiceId();
    }

    @Override
    public void onStart() {
        start();
    }

    @Override
    public void onStop() {
        stop();
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isAvailable() {
        return state == ServiceState.RUNNING || state == ServiceState.INITIALIZED;
    }

    @Override
    public ServiceState getState() {
        return state;
    }

    @Override
    public boolean isRunning() {
        return state.isActive();
    }

    @Override
    public boolean isDisposed() {
        return state == ServiceState.DISPOSED;
    }

    @Override
    public void addLifecycleListener(ServiceLifecycleListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(ServiceLifecycleListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public synchronized void initialize() {
        if (state == ServiceState.INITIALIZED || state == ServiceState.RUNNING) {
            return;
        }
        if (state == ServiceState.DISPOSED) {
            throw new IllegalStateException("Service " + serviceId + " is disposed and cannot be initialized.");
        }
        ServiceState old = state;
        transitionState(ServiceState.INITIALIZING);
        try {
            onInitialize();
            transitionState(ServiceState.INITIALIZED);
            NativeLogger.getInstance().d("BaseService", "Service [" + serviceId + "] initialized.");
        } catch (Throwable t) {
            transitionState(ServiceState.FAILED);
            notifyError(t);
            NativeLogger.getInstance().e("BaseService", "Failed to initialize service: " + serviceId, t);
            throw new RuntimeException("Failed to initialize service: " + serviceId, t);
        }
    }

    @Override
    public synchronized void start() {
        if (state == ServiceState.RUNNING) {
            return;
        }
        if (state == ServiceState.CREATED) {
            initialize();
        }
        if (state == ServiceState.DISPOSED) {
            throw new IllegalStateException("Service " + serviceId + " is disposed and cannot be started.");
        }
        transitionState(ServiceState.STARTING);
        try {
            onServiceStart();
            transitionState(ServiceState.RUNNING);
            NativeLogger.getInstance().d("BaseService", "Service [" + serviceId + "] started.");
        } catch (Throwable t) {
            transitionState(ServiceState.FAILED);
            notifyError(t);
            NativeLogger.getInstance().e("BaseService", "Failed to start service: " + serviceId, t);
            throw new RuntimeException("Failed to start service: " + serviceId, t);
        }
    }

    @Override
    public synchronized void stop() {
        if (state == ServiceState.STOPPED || state == ServiceState.CREATED || state == ServiceState.INITIALIZED) {
            return;
        }
        if (state == ServiceState.DISPOSED) {
            return;
        }
        transitionState(ServiceState.STOPPING);
        try {
            onServiceStop();
            transitionState(ServiceState.STOPPED);
            NativeLogger.getInstance().d("BaseService", "Service [" + serviceId + "] stopped.");
        } catch (Throwable t) {
            transitionState(ServiceState.FAILED);
            notifyError(t);
            NativeLogger.getInstance().e("BaseService", "Failed to stop service: " + serviceId, t);
            throw new RuntimeException("Failed to stop service: " + serviceId, t);
        }
    }

    @Override
    public synchronized void dispose() {
        if (state == ServiceState.DISPOSED) {
            return;
        }
        if (isRunning()) {
            try {
                stop();
            } catch (Exception ignored) {
            }
        }
        try {
            onDispose();
            NativeLogger.getInstance().d("BaseService", "Service [" + serviceId + "] disposed.");
        } catch (Throwable t) {
            notifyError(t);
            NativeLogger.getInstance().e("BaseService", "Error during service dispose: " + serviceId, t);
        } finally {
            transitionState(ServiceState.DISPOSED);
            listeners.clear();
        }
    }

    private void transitionState(ServiceState newState) {
        ServiceState oldState = this.state;
        this.state = newState;
        notifyStateChanged(oldState, newState);
    }

    protected void notifyStateChanged(ServiceState oldState, ServiceState newState) {
        for (ServiceLifecycleListener listener : listeners) {
            try {
                listener.onStateChanged(this, oldState, newState);
            } catch (Exception ignored) {
            }
        }
    }

    protected void notifyError(Throwable error) {
        for (ServiceLifecycleListener listener : listeners) {
            try {
                listener.onError(this, error);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Subclass hook for initialization logic.
     */
    protected void onInitialize() throws Exception {
    }

    /**
     * Subclass hook for start logic.
     */
    protected void onServiceStart() throws Exception {
    }

    /**
     * Subclass hook for stop logic.
     */
    protected void onServiceStop() throws Exception {
    }

    /**
     * Subclass hook for teardown and cleanup logic.
     */
    protected void onDispose() throws Exception {
    }
}
