package com.roni.library.service.handler;

import com.roni.library.service.IService;
import com.roni.library.service.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Command dispatcher mapping RPC actions into {@link ServiceRegistry} queries.
 */
public class ServiceCommandHandler {

    public static final String ROUTE_PREFIX = "service";

    /**
     * Request abstraction representing a native API call.
     */
    public static class NativeApiRequest {
        private final String action;
        private final Map<String, Object> params;

        public NativeApiRequest(String action, Map<String, Object> params) {
            this.action = action;
            this.params = params != null ? params : new HashMap<String, Object>();
        }

        public String getAction() {
            return action;
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }

    /**
     * Callback interface for asynchronous API response dispatch.
     */
    public interface IApiCallback {
        void onSuccess(Map<String, Object> result);
        void onError(int code, String message);
    }

    public String getRoutePrefix() {
        return ROUTE_PREFIX;
    }

    /**
     * Dispatches command request to registered services.
     *
     * @param request  incoming request
     * @param callback callback to receive response
     */
    public void handle(NativeApiRequest request, IApiCallback callback) {
        if (request == null) {
            if (callback != null) {
                callback.onError(400, "Request must not be null");
            }
            return;
        }

        String action = request.getAction();
        ServiceRegistry registry = ServiceRegistry.getInstance();
        Map<String, Object> response = new HashMap<>();

        try {
            if ("list".equalsIgnoreCase(action)) {
                for (IService service : registry.getAllServices()) {
                    response.put(service.getServiceId(), service.getState().name());
                }
                if (callback != null) {
                    callback.onSuccess(response);
                }
            } else if ("status".equalsIgnoreCase(action)) {
                String serviceId = (String) request.getParams().get("serviceId");
                IService service = registry.getServiceByName(serviceId);
                if (service != null) {
                    response.put("serviceId", service.getServiceId());
                    response.put("state", service.getState().name());
                    response.put("available", service.isAvailable());
                    response.put("priority", service.getPriority());
                    if (callback != null) {
                        callback.onSuccess(response);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(404, "Service not found: " + serviceId);
                    }
                }
            } else {
                if (callback != null) {
                    callback.onError(400, "Unknown action: " + action);
                }
            }
        } catch (Throwable t) {
            if (callback != null) {
                callback.onError(500, "Handler exception: " + t.getMessage());
            }
        }
    }
}
