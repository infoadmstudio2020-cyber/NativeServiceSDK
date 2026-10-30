package com.roni.library.service.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.roni.library.contracts.api.IApiCallback;
import com.roni.library.contracts.api.NativeApiError;
import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
import com.roni.library.logger.NativeLogger;
import com.roni.library.service.BaseService;
import com.roni.library.service.IService;
import com.roni.library.service.NativeServiceSDK;
import com.roni.library.service.NativeServiceSDKConfig;
import com.roni.library.service.ServiceRegistry;
import com.roni.library.service.builtins.BatteryStatusDTO;
import com.roni.library.service.builtins.BatteryStatusService;
import com.roni.library.service.builtins.DeviceInfoDTO;
import com.roni.library.service.builtins.DeviceInfoService;
import com.roni.library.service.builtins.NetworkStateService;
import com.roni.library.service.handler.ServiceCommandHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interactive demonstration activity exercising the NativeServiceSDK features.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        Button btnInit = findViewById(R.id.btn_init);
        Button btnRefresh = findViewById(R.id.btn_refresh);
        Button btnAddService = findViewById(R.id.btn_add_service);
        Button btnDispatch = findViewById(R.id.btn_dispatch);
        Button btnRelease = findViewById(R.id.btn_release);

        btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSDK();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshServices();
            }
        });

        btnAddService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerCustomService();
            }
        });

        btnDispatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCommand();
            }
        });

        btnRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseSDK();
            }
        });

        // Auto-initialize on activity startup
        initSDK();
    }

    private void log(String message) {
        String timestamp = dateFormat.format(new Date());
        NativeLogger.getInstance().i("MainActivity", "[" + timestamp + "] " + message);
        if (tvStatus != null) {
            String current = tvStatus.getText().toString();
            tvStatus.setText(current + "[" + timestamp + "] " + message + "\n");
        }
    }

    private void initSDK() {
        NativeServiceSDK sdk = NativeServiceSDK.getInstance();
        if (sdk.isInitialized()) {
            log("SDK already initialized.");
            return;
        }

        NativeServiceSDKConfig config = new NativeServiceSDKConfig.Builder()
                .debugEnabled(true)
                .tag("NativeServiceDemo")
                .timeout(15000)
                .build();

        sdk.init(getApplicationContext(), config);
        log("NativeServiceSDK initialized successfully!");

        // Read and log built-in services info
        ServiceRegistry registry = sdk.getRegistry();
        DeviceInfoService deviceInfoService = registry.getService(DeviceInfoService.class);
        if (deviceInfoService != null) {
            DeviceInfoDTO dev = deviceInfoService.getDeviceInfo();
            log("Device: " + dev.getManufacturer() + " " + dev.getModel() + " (SDK " + dev.getSdkInt() + ")");
        }

        BatteryStatusService batteryService = registry.getService(BatteryStatusService.class);
        if (batteryService != null) {
            BatteryStatusDTO bat = batteryService.getBatteryStatus();
            log("Battery: " + bat.getLevel() + "% (" + bat.getPlugType() + ")");
        }

        NetworkStateService networkService = registry.getService(NetworkStateService.class);
        if (networkService != null) {
            log("Network: " + networkService.getNetworkType().name() + " (Connected: " + networkService.isConnected() + ")");
        }
    }

    private void refreshServices() {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        List<IService> services = registry.getAllServices();
        log("--- Registered Services (" + services.size() + ") ---");
        for (IService s : services) {
            log("-> " + s.getServiceId() + " [State: " + s.getState() + ", Priority: " + s.getPriority() + "]");
        }
    }

    private void registerCustomService() {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        String customId = "com.roni.service.custom." + System.currentTimeMillis() % 1000;
        DemoService demo = new DemoService(customId, 50);
        demo.initialize();
        demo.start();
        registry.registerService(DemoService.class, demo);
        log("Registered custom service: " + customId + " (Priority: 50)");
    }


    private void dispatchCommand() {

        ServiceCommandHandler handler = new ServiceCommandHandler();

        NativeApiRequest request = new NativeApiRequest.Builder(
                "service",
                "list"
        ).build();

        handler.handle(request, new IApiCallback() {

            @Override
            public void onResponse(NativeApiResponse response) {

                Map<String, Object> result = response.getData();

                log("RPC Command 'list' returned "
                        + result.size()
                        + " active services.");
            }

            @Override
            public void onError(NativeApiError error) {

                log("RPC Error (" + error.getCode() + "): "
                        + error.getMessage());
            }
        });
    }


    private void releaseSDK() {
        NativeServiceSDK.getInstance().release();
        log("NativeServiceSDK released all resources and disposed services.");
    }

    public static class DemoService extends BaseService {
        public DemoService(String serviceId, int priority) {
            super(serviceId, priority);
        }
    }
}
