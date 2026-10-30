
package com.roni.library.service.handler;

import com.roni.library.contracts.api.IApiHandler;
import com.roni.library.contracts.api.IApiCallback;
import com.roni.library.contracts.api.NativeApiError;
import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
import com.roni.library.logger.NativeLogger;
import com.roni.library.service.IService;
import com.roni.library.service.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Command dispatcher mapping RPC actions into ServiceRegistry queries.
 *
 * Implements Layer 0 IApiHandler and uses Layer 0 callback contracts.
 */
public class ServiceCommandHandler implements IApiHandler {

    public static final String ROUTE_PREFIX = "service";

    @Override
    public String getPath() {
        return ROUTE_PREFIX;
    }

    @Override
    public String getAction() {
        return "*";
    }

    public String getRoutePrefix() {
        return ROUTE_PREFIX;
    }

    /**
     * Executes a service command and returns a structured response.
     */
    @Override
    public NativeApiResponse handleRequest(NativeApiRequest request) {

        if (request == null) {
            return NativeApiResponse.error(
                    UUID.randomUUID().toString(),
                    400,
                    new NativeApiError(
                            "400",
                            "Request must not be null"
                    )
            );
        }

        String reqId = request.getRequestId();
        String action = request.getAction();

        ServiceRegistry registry = ServiceRegistry.getInstance();

        Map<String, Object> responseData = new HashMap<>();

        try {

            if ("list".equalsIgnoreCase(action)) {

                for (IService service : registry.getAllServices()) {
                    responseData.put(
                            service.getServiceId(),
                            service.getState().name()
                    );
                }

                NativeLogger.getInstance().d(
                        "ServiceCommandHandler",
                        "Handled list request: "
                                + responseData.size()
                                + " services found."
                );

                return NativeApiResponse.success(
                        reqId,
                        responseData
                );

            } else if ("status".equalsIgnoreCase(action)) {

                String serviceId = request.getStringParam(
                        "serviceId",
                        null
                );

                IService service = serviceId != null
                        ? registry.getServiceByName(serviceId)
                        : null;

                if (service != null) {

                    responseData.put(
                            "serviceId",
                            service.getServiceId()
                    );

                    responseData.put(
                            "state",
                            service.getState().name()
                    );

                    responseData.put(
                            "available",
                            service.isAvailable()
                    );

                    responseData.put(
                            "priority",
                            service.getPriority()
                    );

                    NativeLogger.getInstance().d(
                            "ServiceCommandHandler",
                            "Handled status request for: " + serviceId
                    );

                    return NativeApiResponse.success(
                            reqId,
                            responseData
                    );

                } else {

                    NativeLogger.getInstance().w(
                            "ServiceCommandHandler",
                            "Service not found: " + serviceId
                    );

                    return NativeApiResponse.error(
                            reqId,
                            404,
                            new NativeApiError(
                                    "404",
                                    "Service not found: " + serviceId
                            )
                    );
                }

            } else {

                NativeLogger.getInstance().w(
                        "ServiceCommandHandler",
                        "Unknown action requested: " + action
                );

                return NativeApiResponse.error(
                        reqId,
                        400,
                        new NativeApiError(
                                "400",
                                "Unknown action: " + action
                        )
                );
            }

        } catch (Throwable t) {

            NativeLogger.getInstance().e(
                    "ServiceCommandHandler",
                    "Handler exception: " + t.getMessage(),
                    t
            );

            return NativeApiResponse.error(
                    reqId,
                    500,
                    new NativeApiError(
                            "500",
                            "Handler exception: " + t.getMessage()
                    )
            );
        }
    }

    /**
     * Dispatches a request using the official Contracts callback.
     *
     * This method is a synchronous dispatch with callback notification.
     */
    public void handle(
            NativeApiRequest request,
            IApiCallback callback
    ) {

        if (callback == null) {
            return;
        }

        if (request == null) {

            callback.onError(
                    new NativeApiError(
                            "400",
                            "Request must not be null"
                    )
            );

            return;
        }

        NativeApiResponse response = handleRequest(request);

        if (response.isSuccess()) {

            callback.onResponse(response);

        } else {

            NativeApiError error = response.getError();

            callback.onError(
                    error != null
                            ? error
                            : new NativeApiError(
                            String.valueOf(response.getStatusCode()),
                            "Unknown service error"
                    )
            );
        }
    }
}
