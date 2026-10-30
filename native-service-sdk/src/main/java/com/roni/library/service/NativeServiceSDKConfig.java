package com.roni.library.service;

/**
 * Configuration options for {@link NativeServiceSDK}.
 */
public class NativeServiceSDKConfig {

    private final boolean debugEnabled;
    private final String tag;
    private final int timeout;

    private NativeServiceSDKConfig(Builder builder) {
        this.debugEnabled = builder.debugEnabled;
        this.tag = builder.tag;
        this.timeout = builder.timeout;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public String getTag() {
        return tag;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * Builder for {@link NativeServiceSDKConfig}.
     */
    public static class Builder {
        private boolean debugEnabled = false;
        private String tag = "NativeServiceSDK";
        private int timeout = 30_000;

        public Builder debugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            return this;
        }

        public Builder tag(String tag) {
            if (tag != null && !tag.trim().isEmpty()) {
                this.tag = tag;
            }
            return this;
        }

        public Builder timeout(int timeout) {
            if (timeout > 0) {
                this.timeout = timeout;
            }
            return this;
        }

        public NativeServiceSDKConfig build() {
            return new NativeServiceSDKConfig(this);
        }
    }
}
