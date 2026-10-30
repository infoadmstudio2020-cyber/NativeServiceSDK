package com.roni.library.service.builtins;

import com.roni.library.service.BaseService;

import java.util.HashMap;
import java.util.Map;

/**
 * Built-in service tracking network connectivity and network interface type.
 */
public class NetworkStateService extends BaseService {

    public static final String SERVICE_ID = "com.roni.service.network";

    private volatile boolean connected = true;
    private volatile NetworkType networkType = NetworkType.WIFI;

    public NetworkStateService() {
        super(SERVICE_ID, 15);
    }

    public boolean isConnected() {
        return connected;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public synchronized void updateNetworkState(boolean connected, NetworkType type) {
        this.connected = connected;
        this.networkType = type != null ? type : NetworkType.NONE;
    }

    public Map<String, Object> getNetworkStatusMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("connected", connected);
        map.put("type", networkType.name());
        map.put("available", isAvailable());
        return map;
    }

    @Override
    protected void onInitialize() throws Exception {
    }
}
