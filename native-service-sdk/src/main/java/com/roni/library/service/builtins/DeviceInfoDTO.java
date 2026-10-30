package com.roni.library.service.builtins;

/**
 * Data transfer object representing device hardware and OS metadata.
 */
public class DeviceInfoDTO {

    private final String model;
    private final String manufacturer;
    private final String osVersion;
    private final int sdkInt;
    private final String brand;
    private final String device;

    public DeviceInfoDTO(String model, String manufacturer, String osVersion, int sdkInt, String brand, String device) {
        this.model = model;
        this.manufacturer = manufacturer;
        this.osVersion = osVersion;
        this.sdkInt = sdkInt;
        this.brand = brand;
        this.device = device;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public int getSdkInt() {
        return sdkInt;
    }

    public String getBrand() {
        return brand;
    }

    public String getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return "DeviceInfoDTO{" +
                "model='" + model + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", sdkInt=" + sdkInt +
                ", brand='" + brand + '\'' +
                ", device='" + device + '\'' +
                '}';
    }
}
