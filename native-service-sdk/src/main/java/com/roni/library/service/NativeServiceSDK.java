package com.roni.library.service;

import android.content.Context;
import android.util.Log;

import com.roni.library.service.builtins.BatteryStatusService;
import com.roni.library.service.builtins.DeviceInfoService;
import com.roni.library.service.builtins.NetworkStateService;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Public facade and lifecycle coordinator for NativeServiceSDK.
 */
public final class NativeServiceSDK {

    private static volatile NativeServiceSDK instance;
    private static final Object LOCK = new Object();

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private Context appContext;
    private NativeServiceSDKConfig config;
    private ServiceRegistry registry;

    private NativeServiceSDK() {
    }

    /**
     * Returns the singleton instance of {@link NativeServiceSDK}.
     *
     * @return singleton instance
     */
    public static NativeServiceSDK getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new NativeServiceSDK();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the SDK with application context and configuration options.
     *
     * @param context Android application context
     * @param config  SDK configuration
     */
    public synchronized void init(Context context, NativeServiceSDKConfig config) {
        if (initialized.get()) {
            return;
        }
        this.appContext = context != null ? context.getApplicationContext() : null;
        this.config = config != null ? config : new NativeServiceSDKConfig.Builder().build();
        this.registry = ServiceRegistry.getInstance();

        // Register default builtin services
        registerBuiltins();

        initialized.set(true);
        if (this.config.isDebugEnabled()) {
            Log.d(this.config.getTag(), "NativeServiceSDK initialized successfully");
        }
    }

    /**
     * Checks if the SDK has been initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Returns the current active configuration, or null if not yet initialized.
     *
     * @return current configuration
     */
    public NativeServiceSDKConfig getConfig() {
        return config;
    }

    /**
     * Returns the application context, or null if not initialized.
     *
     * @return application context
     */
    public Context getContext() {
        return appContext;
    }

    /**
     * Returns the internal {@link ServiceRegistry}.
     *
     * @return service registry
     */
    public ServiceRegistry getRegistry() {
        if (registry == null) {
            registry = ServiceRegistry.getInstance();
        }
        return registry;
    }

    /**
     * Registers core builtin services into the registry.
     */
    private void registerBuiltins() {
        if (registry == null) {
            return;
        }
        if (!registry.hasService(DeviceInfoService.class)) {
            DeviceInfoService deviceInfoService = new DeviceInfoService();
            deviceInfoService.initialize();
            deviceInfoService.start();
            registry.registerService(DeviceInfoService.class, deviceInfoService);
        }
        if (!registry.hasService(BatteryStatusService.class)) {
            BatteryStatusService batteryService = new BatteryStatusService();
            batteryService.initialize();
            batteryService.start();
            registry.registerService(BatteryStatusService.class, batteryService);
        }
        if (!registry.hasService(NetworkStateService.class)) {
            NetworkStateService networkService = new NetworkStateService();
            networkService.initialize();
            networkService.start();
            registry.registerService(NetworkStateService.class, networkService);
        }
    }

    /**
     * Releases all held resources, disposes registered services, and resets singleton state.
     */
    public synchronized void release() {
        if (registry != null) {
            registry.disposeAllServices();
            registry = null;
        }
        initialized.set(false);
        appContext = null;
        config = null;
        synchronized (LOCK) {
            instance = null;
        }
    }
}
