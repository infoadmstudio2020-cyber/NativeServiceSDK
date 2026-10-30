# NativeServiceSDK

Microkernel service registry and dependency injection container for the NativeRuntime Android ecosystem.

[![JitPack](https://jitpack.io/v/infoadmstudio2020-cyber/NativeServiceSDK.svg)](https://jitpack.io/#infoadmstudio2020-cyber/NativeServiceSDK)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-Apache-2.0-blue.svg)](LICENSE)

---

## Overview

`NativeServiceSDK` provides a decoupled, thread-safe microkernel architecture for Android applications and modular SDK ecosystems. It manages service discovery, lifecycle state machines, prioritized dependency dispatch, and built-in hardware/OS telemetry.

### Key Capabilities
- **Thread-Safe Service Registry**: Concurrently register, resolve, and manage services by interface type or name.
- **Hierarchical Lifecycle Management**: Automated transitions across `CREATED`, `INITIALIZED`, `RUNNING`, `STOPPED`, and `DISPOSED`.
- **Dependency Injection**: Seamless field injection using `@ServiceInjector.InjectService`.
- **Built-in Telemetry Services**: Battery status, hardware device identity, and network state monitoring.
- **Command Handling**: RPC dispatcher via `ServiceCommandHandler` for hybrid and remote calls.

---

## Installation

### Step 1 — Add JitPack repository
In your `settings.gradle` (or root `build.gradle`):
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2 — Add dependency
In your module's `build.gradle`:
```groovy
dependencies {
    // NativeServiceSDK (Layer 1)
    implementation 'com.github.infoadmstudio2020-cyber:NativeServiceSDK:1.0.1'

    // Shared Contracts (Layer 0)
    api 'com.github.infoadmstudio2020-cyber:NativeRuntimeContracts:v1.0.1'

    // Internal Logging (Layer 1)
    implementation 'com.github.infoadmstudio2020-cyber:NativeLoggerSDK:1.0.1'
}
```

---

## Quick Start

### 1. Initialize the SDK
```java
NativeServiceSDKConfig config = new NativeServiceSDKConfig.Builder()
    .debugEnabled(BuildConfig.DEBUG)
    .tag("NativeServiceSDK")
    .timeout(30000)
    .build();

NativeServiceSDK.getInstance().init(context, config);
```

### 2. Register & Resolve Services
```java
// Custom Service definition
public class AnalyticsService extends BaseService {
    public AnalyticsService() {
        super("com.example.service.analytics", 50); // ID and priority
    }
}

// Registration
ServiceRegistry registry = ServiceRegistry.getInstance();
registry.registerService(AnalyticsService.class, new AnalyticsService());

// Resolution
AnalyticsService analytics = registry.getService(AnalyticsService.class);
```

### 3. Automated Field Injection
```java
public class MyFeaturePresenter {
    @ServiceInjector.InjectService
    private BatteryStatusService batteryService;

    public MyFeaturePresenter() {
        ServiceInjector.inject(this);
    }
}
```

### 4. Built-in Services
- **`DeviceInfoService`**: Access hardware model, manufacturer, brand, and Android SDK level.
- **`BatteryStatusService`**: Read real-time battery percentage, charging state, and temperature.
- **`NetworkStateService`**: Query connectivity and connection type (WiFi, Cellular, Ethernet, etc.).

---

## API Reference

### `NativeServiceSDK`
| Method | Description |
|---|---|
| `getInstance()` | Returns global SDK singleton |
| `init(Context, NativeServiceSDKConfig)` | Initializes runtime and built-in services |
| `isInitialized()` | Returns true if initialized |
| `getRegistry()` | Returns active `ServiceRegistry` instance |
| `release()` | Disposes all services and resets state |

### `ServiceRegistry`
| Method | Description |
|---|---|
| `getInstance()` | Returns global service registry |
| `registerService(Class<T>, T)` | Registers service instance |
| `registerProvider(Class<T>, IServiceProvider<T>)` | Registers lazy or custom factory provider |
| `getService(Class<T>)` | Resolves registered service by class |
| `getServiceByName(String)` | Resolves registered service by ID/name |
| `getAllServices()` | Returns list of all services sorted by priority |
| `startAllServices()` | Boots all registered services |
| `disposeAllServices()` | Safely stops and cleans up all services |

---

## Requirements
- Android API 21+ (minSdk 21, compileSdk 34)
- JDK 17
- Android Gradle Plugin 8.3.2+

---

## License
Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

