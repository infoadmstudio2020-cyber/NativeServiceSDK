package com.roni.library.service.lifecycle;

import com.roni.library.service.IService;

/**
 * Utility helper for orchestrating service lifecycle transitions safely.
 */
public final class ServiceLifecycle {

    private ServiceLifecycle() {
        // utility class
    }

    /**
     * Safely initializes a service if it is in {@link ServiceState#CREATED} state.
     *
     * @param service target service
     */
    public static void safeInitialize(IService service) {
        if (service != null && service.getState() == ServiceState.CREATED) {
            try {
                service.initialize();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Safely starts a service.
     *
     * @param service target service
     */
    public static void safeStart(IService service) {
        if (service != null && !service.isRunning() && !service.isDisposed()) {
            try {
                if (service.getState() == ServiceState.CREATED) {
                    service.initialize();
                }
                service.start();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Safely stops a service.
     *
     * @param service target service
     */
    public static void safeStop(IService service) {
        if (service != null && service.isRunning()) {
            try {
                service.stop();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Safely disposes a service.
     *
     * @param service target service
     */
    public static void safeDispose(IService service) {
        if (service != null && !service.isDisposed()) {
            try {
                if (service.isRunning()) {
                    service.stop();
                }
                service.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
