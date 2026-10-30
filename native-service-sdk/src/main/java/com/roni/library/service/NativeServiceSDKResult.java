package com.roni.library.service;

/**
 * Standardized result wrapper for SDK operations.
 *
 * @param <T> payload type
 */
public class NativeServiceSDKResult<T> {

    private final boolean success;
    private final T data;
    private final int errorCode;
    private final String errorMessage;

    private NativeServiceSDKResult(boolean success, T data, int errorCode, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static <T> NativeServiceSDKResult<T> success(T data) {
        return new NativeServiceSDKResult<>(true, data, 0, null);
    }

    public static <T> NativeServiceSDKResult<T> error(int errorCode, String errorMessage) {
        return new NativeServiceSDKResult<>(false, null, errorCode, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
