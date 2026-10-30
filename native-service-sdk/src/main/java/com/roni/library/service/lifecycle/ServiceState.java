package com.roni.library.service.lifecycle;

/**
 * Represents the lifecycle state of an {@link com.roni.library.service.IService}.
 */
public enum ServiceState {
    CREATED,
    INITIALIZING,
    INITIALIZED,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    DISPOSED,
    FAILED;

    /**
     * Checks if the service is actively running.
     *
     * @return true if state is RUNNING
     */
    public boolean isActive() {
        return this == RUNNING;
    }

    /**
     * Checks if the service has reached a terminal state (cannot transition further).
     *
     * @return true if DISPOSED or FAILED
     */
    public boolean isTerminal() {
        return this == DISPOSED || this == FAILED;
    }
}
