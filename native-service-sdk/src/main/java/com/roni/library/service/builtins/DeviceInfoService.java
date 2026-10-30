package com.roni.library.service.builtins;

import android.os.Build;
import com.roni.library.service.BaseService;

/**
 * Built-in service providing hardware and Android platform identity metadata.
 */
public class DeviceInfoService extends BaseService {

    public static final String SERVICE_ID = "com.roni.service.device";

    private final DeviceInfoDTO cachedInfo;

    public DeviceInfoService() {
        super(SERVICE_ID, 20);
        String model = Build.MODEL != null ? Build.MODEL : "Generic";
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER : "Unknown";
        String osVersion = Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "14";
        int sdkInt = Build.VERSION.SDK_INT > 0 ? Build.VERSION.SDK_INT : 34;
        String brand = Build.BRAND != null ? Build.BRAND : "Generic";
        String device = Build.DEVICE != null ? Build.DEVICE : "GenericDevice";
        this.cachedInfo = new DeviceInfoDTO(model, manufacturer, osVersion, sdkInt, brand, device);
    }

    public DeviceInfoDTO getDeviceInfo() {
        return cachedInfo;
    }

    @Override
    protected void onInitialize() throws Exception {
    }
}
