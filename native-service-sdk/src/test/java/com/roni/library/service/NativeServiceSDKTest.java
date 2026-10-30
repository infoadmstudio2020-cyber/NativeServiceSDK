package com.roni.library.service;

import com.roni.library.contracts.api.IApiCallback;
import com.roni.library.contracts.api.NativeApiError;
import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
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

        registry.registerProviderByName(
                "svc.two",
                SingletonServiceProvider.of(service2)
        );

        assertTrue(registry.hasService(TestService.class));
        assertTrue(registry.hasServiceByName("svc.two"));

        assertSame(service1, registry.getService(TestService.class));
        assertSame(service2, registry.getServiceByName("svc.two"));

        List<IService> all = registry.getAllServices();

        assertTrue(all.contains(service1));
        assertTrue(all.contains(service2));

        // Priority ordering: higher priority first.
        assertEquals(service2, all.get(0));

        // Unregister service.
        registry.unregisterService(TestService.class);

        // Verify removal.
        assertFalse(registry.hasService(TestService.class));

        // Verify named service remains registered.
        assertTrue(registry.hasServiceByName("svc.two"));
        assertSame(service2, registry.getServiceByName("svc.two"));
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


        AtomicBoolean success = new AtomicBoolean(false);

        NativeApiRequest request = new NativeApiRequest.Builder(
                "service",
                "list"
        ).build();

        handler.handle(request, new IApiCallback() {

            @Override
            public void onResponse(NativeApiResponse response) {

                assertTrue(
                        "Response should be successful",
                        response.isSuccess()
                );

                Map<String, Object> result = response.getData();

                assertTrue(
                        "Expected command.service in response",
                        result.containsKey("command.service")
                );

                success.set(true);
            }

            @Override
            public void onError(NativeApiError error) {

                fail("Handler returned error: " + error.getMessage());
            }
        });

        assertTrue("Handler callback was not successful", success.get());



        Map<String, Object> params = new HashMap<>();
        params.put("serviceId", "command.service");

        AtomicBoolean statusSuccess = new AtomicBoolean(false);

        NativeApiRequest listRequest = new NativeApiRequest.Builder(
                "service",
                "list"
        ).build();

        handler.handle(listRequest, new IApiCallback() {
            @Override
            public void onResponse(NativeApiResponse response) {
                assertTrue(response.isSuccess());

                Map<String, Object> result = response.getData();
                assertTrue(result.containsKey("command.service"));

                success.set(true);
            }

            @Override
            public void onError(NativeApiError error) {
                fail("Handler returned error: " + error.getMessage());
            }
        });

        handler.handle(request, new IApiCallback() {

            @Override
            public void onResponse(NativeApiResponse response) {

                assertTrue(
                        "Status response should be successful",
                        response.isSuccess()
                );

                Map<String, Object> result = response.getData();

                assertEquals(
                        "command.service",
                        result.get("serviceId")
                );

                assertEquals(
                        "RUNNING",
                        result.get("state")
                );

                statusSuccess.set(true);
            }

            @Override
            public void onError(NativeApiError error) {

                fail("Handler returned error: " + error.getMessage());
            }
        });

        assertTrue(
                "Status callback was not successful",
                statusSuccess.get()
        );

    }

    @Test
    public void nativeRuntimeContracts_compliance() {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        TestService service = new TestService("contract.service", 15);

        // Test Layer 0 IService methods
        assertEquals("contract.service", service.getServiceName());
        assertFalse(service.isRunning());
        service.onStart();
        assertTrue(service.isRunning());
        service.onStop();
        assertFalse(service.isRunning());

        // Test Layer 0 IServiceRegistry methods
        com.roni.library.contracts.service.IServiceRegistry contractRegistry = registry;
        contractRegistry.registerService(TestService.class, service);
        assertTrue(contractRegistry.hasService(TestService.class));
        assertSame(service, contractRegistry.getService(TestService.class));
        contractRegistry.unregisterService(TestService.class);
        assertFalse(contractRegistry.hasService(TestService.class));

        // Test Layer 0 RuntimeState and IDisposable
        assertEquals(com.roni.library.contracts.common.RuntimeState.READY, registry.getRuntimeState());
        registry.dispose();
        assertTrue(registry.isDisposed());
        assertEquals(com.roni.library.contracts.common.RuntimeState.TERMINATED, registry.getRuntimeState());
    }

    @Test
    public void contractDTOs_and_apiHandler() {
        BatteryStatusService batteryService = new BatteryStatusService();
        batteryService.updateBatteryStatus(90, true, "USB", 27.0f);
        com.roni.library.contracts.platform.BatteryStatusDTO batDTO = batteryService.toContractDTO();
        assertEquals(90, batDTO.getLevel());
        assertTrue(batDTO.isCharging());
        assertEquals("USB", batDTO.getPluggedType());

        DeviceInfoService deviceService = new DeviceInfoService();
        com.roni.library.contracts.platform.DeviceInfoDTO devDTO = deviceService.toContractDTO();
        assertNotNull(devDTO.getManufacturer());
        assertNotNull(devDTO.getModel());

        ServiceCommandHandler handler = new ServiceCommandHandler();
        assertEquals("service", handler.getPath());
        assertEquals("*", handler.getAction());

        com.roni.library.contracts.api.NativeApiRequest req = new com.roni.library.contracts.api.NativeApiRequest(
                "req-1", "service", "list", new HashMap<String, Object>(), System.currentTimeMillis()
        );
        com.roni.library.contracts.api.NativeApiResponse resp = handler.handleRequest(req);
        assertTrue(resp.isSuccess());

        com.roni.library.logger.NativeLogger.getInstance().i("Test", "Completed contractDTOs_and_apiHandler test");
    }

    @Test
    public void nativeLogger_integration() {
        com.roni.library.logger.NativeLogger logger = com.roni.library.logger.NativeLogger.getInstance();
        assertNotNull(logger);
        logger.d("TestTag", "Testing NativeLoggerSDK message output");
        logger.i("TestTag", "Informational log entry");
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

