package com.roni.library.service.builtins;

/**
 * Data transfer object representing battery telemetry.
 */
public class BatteryStatusDTO {

    private final int level;
    private final boolean charging;
    private final String plugType;
    private final float temperatureCelsius;

    public BatteryStatusDTO(int level, boolean charging, String plugType, float temperatureCelsius) {
        this.level = level;
        this.charging = charging;
        this.plugType = plugType;
        this.temperatureCelsius = temperatureCelsius;
    }

    public int getLevel() {
        return level;
    }

    public boolean isCharging() {
        return charging;
    }

    public String getPlugType() {
        return plugType;
    }

    public float getTemperatureCelsius() {
        return temperatureCelsius;
    }

    @Override
    public String toString() {
        return "BatteryStatusDTO{" +
                "level=" + level +
                ", charging=" + charging +
                ", plugType='" + plugType + '\'' +
                ", temperatureCelsius=" + temperatureCelsius +
                '}';
    }
}
