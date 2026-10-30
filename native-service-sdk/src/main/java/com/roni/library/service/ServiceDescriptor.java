package com.roni.library.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Metadata descriptor for services managed in the {@link ServiceRegistry}.
 */
public class ServiceDescriptor {

    private final Class<? extends IService> serviceClass;
    private final String serviceId;
    private final int priority;
    private final Set<Class<? extends IService>> dependencies;
    private final boolean lazy;

    public ServiceDescriptor(Builder builder) {
        this.serviceClass = builder.serviceClass;
        this.serviceId = builder.serviceId;
        this.priority = builder.priority;
        this.dependencies = Collections.unmodifiableSet(new HashSet<>(builder.dependencies));
        this.lazy = builder.lazy;
    }

    public Class<? extends IService> getServiceClass() {
        return serviceClass;
    }

    public String getServiceId() {
        return serviceId;
    }

    public int getPriority() {
        return priority;
    }

    public Set<Class<? extends IService>> getDependencies() {
        return dependencies;
    }

    public boolean isLazy() {
        return lazy;
    }

    public static class Builder {
        private final Class<? extends IService> serviceClass;
        private String serviceId;
        private int priority = 0;
        private final Set<Class<? extends IService>> dependencies = new HashSet<>();
        private boolean lazy = false;

        public Builder(Class<? extends IService> serviceClass) {
            if (serviceClass == null) {
                throw new IllegalArgumentException("serviceClass must not be null");
            }
            this.serviceClass = serviceClass;
            this.serviceId = serviceClass.getSimpleName();
        }

        public Builder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder dependsOn(Class<? extends IService> dependency) {
            if (dependency != null) {
                this.dependencies.add(dependency);
            }
            return this;
        }

        public Builder lazy(boolean lazy) {
            this.lazy = lazy;
            return this;
        }

        public ServiceDescriptor build() {
            return new ServiceDescriptor(this);
        }
    }
}
