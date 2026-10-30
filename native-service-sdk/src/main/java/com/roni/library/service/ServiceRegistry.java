package com.roni.library.service;

import com.roni.library.service.lifecycle.ServiceLifecycle;
import com.roni.library.service.lifecycle.ServiceLifecycleListener;
import com.roni.library.service.lifecycle.ServiceState;
import com.roni.library.service.provider.IServiceProvider;
import com.roni.library.service.provider.SingletonServiceProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central microkernel service registry and dependency container for NativeRuntime ecosystem.
 */
public class ServiceRegistry {

    private static volatile ServiceRegistry instance;
    private static final Object INSTANCE_LOCK = new Object();

    private final Map<Class<? extends IService>, IServiceProvider<? extends IService>> typedProviders = new ConcurrentHashMap<>();
    private final Map<String, IServiceProvider<? extends IService>> namedProviders = new ConcurrentHashMap<>();
    private final List<ServiceLifecycleListener> globalListeners = new CopyOnWriteArrayList<>();

    private final ServiceLifecycleListener internalForwarder = new ServiceLifecycleListener() {
        @Override
        public void onStateChanged(IService service, ServiceState oldState, ServiceState newState) {
            for (ServiceLifecycleListener listener : globalListeners) {
                try {
                    listener.onStateChanged(service, oldState, newState);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void onError(IService service, Throwable error) {
            for (ServiceLifecycleListener listener : globalListeners) {
                try {
                    listener.onError(service, error);
                } catch (Exception ignored) {
                }
            }
        }
    };

    public ServiceRegistry() {
    }

    /**
     * Gets the global singleton instance of the registry.
     *
     * @return singleton registry
     */
    public static ServiceRegistry getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = new ServiceRegistry();
                }
            }
        }
        return instance;
    }

    /**
     * Sets or overrides the singleton instance (primarily for testing or custom kernels).
     *
     * @param registry custom registry instance
     */
    public static void setInstance(ServiceRegistry registry) {
        synchronized (INSTANCE_LOCK) {
            instance = registry;
        }
    }

    /**
     * Resets the singleton instance and disposes existing services.
     */
    public static void reset() {
        synchronized (INSTANCE_LOCK) {
            if (instance != null) {
                instance.disposeAllServices();
                instance = null;
            }
        }
    }

    public void addGlobalLifecycleListener(ServiceLifecycleListener listener) {
        if (listener != null && !globalListeners.contains(listener)) {
            globalListeners.add(listener);
        }
    }

    public void removeGlobalLifecycleListener(ServiceLifecycleListener listener) {
        if (listener != null) {
            globalListeners.remove(listener);
        }
    }

    /**
     * Registers a pre-instantiated service instance.
     *
     * @param serviceClass    service interface or class
     * @param serviceInstance instance of the service
     * @param <T>             type of service
     */
    public <T extends IService> void registerService(Class<T> serviceClass, T serviceInstance) {
        if (serviceClass == null || serviceInstance == null) {
            throw new IllegalArgumentException("serviceClass and serviceInstance must not be null");
        }
        serviceInstance.addLifecycleListener(internalForwarder);
        SingletonServiceProvider<T> provider = SingletonServiceProvider.of(serviceInstance);
        typedProviders.put(serviceClass, provider);
        namedProviders.put(serviceInstance.getServiceId(), provider);
        namedProviders.put(serviceClass.getName(), provider);
    }

    /**
     * Registers a lazy or custom factory provider.
     *
     * @param serviceClass service interface or class
     * @param provider     service provider
     * @param <T>          type of service
     */
    public <T extends IService> void registerProvider(Class<T> serviceClass, IServiceProvider<T> provider) {
        if (serviceClass == null || provider == null) {
            throw new IllegalArgumentException("serviceClass and provider must not be null");
        }
        typedProviders.put(serviceClass, provider);
        namedProviders.put(serviceClass.getName(), provider);
    }

    /**
     * Registers a service provider by name.
     *
     * @param name     unique service name
     * @param provider service provider
     */
    public void registerProviderByName(String name, IServiceProvider<? extends IService> provider) {
        if (name == null || provider == null) {
            throw new IllegalArgumentException("name and provider must not be null");
        }
        namedProviders.put(name, provider);
    }

    /**
     * Unregisters a service by class.
     *
     * @param serviceClass class to unregister
     * @param <T>          type of service
     * @return true if found and removed
     */
    public <T extends IService> boolean unregisterService(Class<T> serviceClass) {
        if (serviceClass == null) {
            return false;
        }
        IServiceProvider<? extends IService> removed = typedProviders.remove(serviceClass);
        namedProviders.remove(serviceClass.getName());
        if (removed != null) {
            IService s = removed.get();
            if (s != null) {
                namedProviders.remove(s.getServiceId());
                s.removeLifecycleListener(internalForwarder);
            }
            return true;
        }
        return false;
    }

    /**
     * Unregisters a service by name.
     *
     * @param name name to unregister
     * @return true if found and removed
     */
    public boolean unregisterServiceByName(String name) {
        if (name == null) {
            return false;
        }
        IServiceProvider<? extends IService> removed = namedProviders.remove(name);
        if (removed != null) {
            IService s = removed.get();
            if (s != null) {
                s.removeLifecycleListener(internalForwarder);
            }
            return true;
        }
        return false;
    }

    /**
     * Resolves a service by class type.
     *
     * @param serviceClass target class
     * @param <T>          type of service
     * @return service instance or null if not registered
     */
    @SuppressWarnings("unchecked")
    public <T extends IService> T getService(Class<T> serviceClass) {
        if (serviceClass == null) {
            return null;
        }
        IServiceProvider<? extends IService> provider = typedProviders.get(serviceClass);
        if (provider != null) {
            T s = (T) provider.get();
            if (s != null) {
                s.addLifecycleListener(internalForwarder);
            }
            return s;
        }
        // Fallback search across registered services
        for (IServiceProvider<? extends IService> p : typedProviders.values()) {
            IService s = p.get();
            if (serviceClass.isInstance(s)) {
                s.addLifecycleListener(internalForwarder);
                return (T) s;
            }
        }
        return null;
    }

    /**
     * Resolves a service by name.
     *
     * @param name service name or ID
     * @return service instance or null
     */
    public IService getServiceByName(String name) {
        if (name == null) {
            return null;
        }
        IServiceProvider<? extends IService> provider = namedProviders.get(name);
        if (provider != null) {
            IService s = provider.get();
            if (s != null) {
                s.addLifecycleListener(internalForwarder);
            }
            return s;
        }
        return null;
    }

    /**
     * Checks if a service is registered.
     *
     * @param serviceClass target class
     * @return true if present
     */
    public boolean hasService(Class<? extends IService> serviceClass) {
        return serviceClass != null && (typedProviders.containsKey(serviceClass) || getService(serviceClass) != null);
    }

    /**
     * Checks if a service is registered by name.
     *
     * @param name service name
     * @return true if present
     */
    public boolean hasServiceByName(String name) {
        return name != null && namedProviders.containsKey(name);
    }

    /**
     * Returns a snapshot list of all instantiated services sorted by priority (descending).
     *
     * @return sorted list of active services
     */
    public List<IService> getAllServices() {
        List<IService> result = new ArrayList<>();
        for (IServiceProvider<? extends IService> provider : typedProviders.values()) {
            IService s = provider.get();
            if (s != null && !result.contains(s)) {
                result.add(s);
            }
        }
        for (IServiceProvider<? extends IService> provider : namedProviders.values()) {
            IService s = provider.get();
            if (s != null && !result.contains(s)) {
                result.add(s);
            }
        }
        Collections.sort(result, new Comparator<IService>() {
            @Override
            public int compare(IService o1, IService o2) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            }
        });
        return result;
    }

    /**
     * Starts all registered services in priority order.
     */
    public void startAllServices() {
        List<IService> services = getAllServices();
        for (IService service : services) {
            ServiceLifecycle.safeStart(service);
        }
    }

    /**
     * Stops all registered services in reverse priority order.
     */
    public void stopAllServices() {
        List<IService> services = getAllServices();
        Collections.reverse(services);
        for (IService service : services) {
            ServiceLifecycle.safeStop(service);
        }
    }

    /**
     * Disposes all registered services and clears registries.
     */
    public void disposeAllServices() {
        List<IService> services = getAllServices();
        Collections.reverse(services);
        for (IService service : services) {
            ServiceLifecycle.safeDispose(service);
        }
        typedProviders.clear();
        namedProviders.clear();
        globalListeners.clear();
    }
}
