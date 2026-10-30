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

    /**
     * Converts to Layer 0 {@link com.roni.library.contracts.platform.DeviceInfoDTO}.
     *
     * @return Layer 0 device info DTO
     */
    public com.roni.library.contracts.platform.DeviceInfoDTO toContractDTO() {
        DeviceInfoDTO d = cachedInfo;
        long totalMemory = Runtime.getRuntime().totalMemory();
        String uuid = java.util.UUID.nameUUIDFromBytes((d.getManufacturer() + ":" + d.getModel()).getBytes()).toString();
        return new com.roni.library.contracts.platform.DeviceInfoDTO(
                d.getManufacturer(),
                d.getModel(),
                d.getOsVersion(),
                d.getSdkInt(),
                uuid,
                totalMemory
        );
    }

    @Override
    protected void onInitialize() throws Exception {
    }
}
