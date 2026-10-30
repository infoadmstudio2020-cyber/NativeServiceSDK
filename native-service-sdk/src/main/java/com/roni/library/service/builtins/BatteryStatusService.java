package com.roni.library.service.builtins;

import com.roni.library.service.BaseService;

/**
 * Built-in service managing device battery status and telemetry.
 */
public class BatteryStatusService extends BaseService {

    public static final String SERVICE_ID = "com.roni.service.battery";

    private volatile BatteryStatusDTO currentStatus;

    public BatteryStatusService() {
        super(SERVICE_ID, 10);
        this.currentStatus = new BatteryStatusDTO(100, false, "UNPLUGGED", 25.0f);
    }

    public synchronized void updateBatteryStatus(int level, boolean isCharging, String plugType, float temperatureCelsius) {
        this.currentStatus = new BatteryStatusDTO(level, isCharging, plugType, temperatureCelsius);
    }

    public BatteryStatusDTO getBatteryStatus() {
        return currentStatus;
    }

    @Override
    protected void onInitialize() throws Exception {
        // Ready for battery state updates
    }
}
