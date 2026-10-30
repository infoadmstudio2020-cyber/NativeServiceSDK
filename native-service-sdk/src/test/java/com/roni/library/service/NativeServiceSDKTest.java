package com.roni.library.service;

import com.roni.library.service.builtins.BatteryStatusDTO;
import com.roni.library.service.builtins.BatteryStatusService;
import com.roni.library.service.builtins.DeviceInfoDTO;
import com.roni.library.service.builtins.DeviceInfoService;
import com.roni.library.service.builtins.NetworkStateService;
import com.roni.library.service.builtins.NetworkType;
import com.roni.library.service.handler.ServiceCommandHandler;
import com.roni.library.service.lifecycle.ServiceLifecycleListener;
import com.roni.library.service.lifecycle.ServiceState;
import com.roni.library.service.provider.SingletonServiceProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Comprehensive Unit tests for {@link NativeServiceSDK} and its subsystem components.
 *
 * @author infoadmstudio2020-cyber
 */
public class NativeServiceSDKTest {

    @Before
    public void setUp() {
        NativeServiceSDK.getInstance().release();
        ServiceRegistry.reset();
    }

    @After
    public void tearDown() {
        NativeServiceSDK.getInstance().release();
        ServiceRegistry.reset();
    }

    @Test
    public void getInstance_returnsSameInstance() {
        NativeServiceSDK a = NativeServiceSDK.getInstance();
        NativeServiceSDK b = NativeServiceSDK.getInstance();
        assertSame(a, b);
    }

    @Test
    public void isInitialized_falseBeforeInit() {
        NativeServiceSDK lib = NativeServiceSDK.getInstance();
        assertFalse("Should not be initialized before init()", lib.isInitialized());
    }

    @Test
    public void config_builder_defaults() {
        NativeServiceSDKConfig config = new NativeServiceSDKConfig.Builder().build();
        assertFalse(config.isDebugEnabled());
        assertEquals("NativeServiceSDK", config.getTag());
        assertEquals(30_000, config.getTimeout());
    }

    @Test
    public void config_builder_customValues() {
        NativeServiceSDKConfig config = new NativeServiceSDKConfig.Builder()
                .debugEnabled(true)
                .tag("CustomTag")
                .timeout(5000)
                .build();
        assertTrue(config.isDebugEnabled());
        assertEquals("CustomTag", config.getTag());
        assertEquals(5000, config.getTimeout());
    }

    @Test
    public void result_successAndErrorWrappers() {
        NativeServiceSDKResult<String> success = NativeServiceSDKResult.success("Payload");
        assertTrue(success.isSuccess());
        assertEquals("Payload", success.getData());
        assertEquals(0, success.getErrorCode());
        assertNull(success.getErrorMessage());

        NativeServiceSDKResult<String> error = NativeServiceSDKResult.error(404, "Not Found");
        assertFalse(error.isSuccess());
        assertNull(error.getData());
        assertEquals(404, error.getErrorCode());
        assertEquals("Not Found", error.getErrorMessage());
    }

    @Test
    public void baseService_lifecycleStateTransitions() {
        TestService service = new TestService("test.service", 5);
        assertEquals("test.service", service.getServiceId());
        assertEquals(5, service.getPriority());
        assertEquals(ServiceState.CREATED, service.getState());
        assertFalse(service.isRunning());
        assertFalse(service.isDisposed());

        final AtomicBoolean stateChanged = new AtomicBoolean(false);
        service.addLifecycleListener(new ServiceLifecycleListener() {
            @Override
            public void onStateChanged(IService s, ServiceState oldState, ServiceState newState) {
                stateChanged.set(true);
            }

            @Override
            public void onError(IService s, Throwable error) {
            }
        });

        service.initialize();
        assertEquals(ServiceState.INITIALIZED, service.getState());
        assertTrue(stateChanged.get());

        service.start();
        assertEquals(ServiceState.RUNNING, service.getState());
        assertTrue(service.isRunning());
        assertTrue(service.isAvailable());

        service.stop();
        assertEquals(ServiceState.STOPPED, service.getState());
        assertFalse(service.isRunning());

        service.dispose();
        assertEquals(ServiceState.DISPOSED, service.getState());
        assertTrue(service.isDisposed());
    }

    @Test
    public void serviceRegistry_registrationAndLookup() {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        TestService service1 = new TestService("svc.one", 10);
        TestService service2 = new TestService("svc.two", 20);

        registry.registerService(TestService.class, service1);
        registry.registerProviderByName("svc.two", SingletonServiceProvider.of(service2));

        assertTrue(registry.hasService(TestService.class));
        assertTrue(registry.hasServiceByName("svc.two"));

        assertSame(service1, registry.getService(TestService.class));
        assertSame(service2, registry.getServiceByName("svc.two"));

        List<IService> all = registry.getAllServices();
        assertTrue(all.contains(service1));
        assertTrue(all.contains(service2));
        // Priority ordering: service2 (priority 20) should be before service1 (priority 10)
        assertEquals(service2, all.get(0));

        assertTrue(registry.unregisterService(TestService.class));
        assertFalse(registry.hasService(TestService.class));
    }

    @Test
    public void serviceInjector_injectsAnnotatedFields() {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        TestService service = new TestService("test.injected", 1);
        registry.registerService(TestService.class, service);

        SampleClient client = new SampleClient();
        assertNull(client.injectedService);

        ServiceInjector.inject(client);
        assertNotNull(client.injectedService);
        assertSame(service, client.injectedService);
    }

    @Test
    public void serviceDescriptor_builder() {
        ServiceDescriptor desc = new ServiceDescriptor.Builder(TestService.class)
                .serviceId("custom.id")
                .priority(50)
                .lazy(true)
                .build();

        assertEquals(TestService.class, desc.getServiceClass());
        assertEquals("custom.id", desc.getServiceId());
        assertEquals(50, desc.getPriority());
        assertTrue(desc.isLazy());
    }

    @Test
    public void builtinServices_telemetryVerification() {
        BatteryStatusService batteryService = new BatteryStatusService();
        batteryService.initialize();
        batteryService.updateBatteryStatus(85, true, "AC", 28.5f);
        BatteryStatusDTO battery = batteryService.getBatteryStatus();
        assertEquals(85, battery.getLevel());
        assertTrue(battery.isCharging());
        assertEquals("AC", battery.getPlugType());
        assertEquals(28.5f, battery.getTemperatureCelsius(), 0.01f);

        DeviceInfoService deviceService = new DeviceInfoService();
        deviceService.initialize();
        DeviceInfoDTO device = deviceService.getDeviceInfo();
        assertNotNull(device.getModel());
        assertNotNull(device.getManufacturer());

        NetworkStateService networkService = new NetworkStateService();
        networkService.initialize();
        networkService.updateNetworkState(true, NetworkType.CELLULAR);
        assertTrue(networkService.isConnected());
        assertEquals(NetworkType.CELLULAR, networkService.getNetworkType());
        Map<String, Object> statusMap = networkService.getNetworkStatusMap();
        assertEquals(true, statusMap.get("connected"));
        assertEquals("CELLULAR", statusMap.get("type"));
    }

    @Test
    public void serviceCommandHandler_dispatchesCommands() {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        TestService service = new TestService("command.service", 1);
        registry.registerService(TestService.class, service);
        service.initialize();
        service.start();

        ServiceCommandHandler handler = new ServiceCommandHandler();
        assertEquals("service", handler.getRoutePrefix());

        final AtomicBoolean success = new AtomicBoolean(false);
        handler.handle(new ServiceCommandHandler.NativeApiRequest("list", null), new ServiceCommandHandler.IApiCallback() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                assertTrue(result.containsKey("command.service"));
                success.set(true);
            }

            @Override
            public void onError(int code, String message) {
                fail("Handler returned error: " + message);
            }
        });
        assertTrue(success.get());

        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", "command.service");
        final AtomicBoolean statusSuccess = new AtomicBoolean(false);
        handler.handle(new ServiceCommandHandler.NativeApiRequest("status", params), new ServiceCommandHandler.IApiCallback() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                assertEquals("command.service", result.get("serviceId"));
                assertEquals("RUNNING", result.get("state"));
                statusSuccess.set(true);
            }

            @Override
            public void onError(int code, String message) {
                fail("Handler returned error: " + message);
            }
        });
        assertTrue(statusSuccess.get());
    }

    private static class TestService extends BaseService {
        public TestService(String serviceId, int priority) {
            super(serviceId, priority);
        }
    }

    private static class SampleClient {
        @ServiceInjector.InjectService
        private TestService injectedService;
    }
}

