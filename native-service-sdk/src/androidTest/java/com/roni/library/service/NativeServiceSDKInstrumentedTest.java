package com.roni.library.service;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented tests for {@link NativeServiceSDK}.
 * Run on a real device or emulator.
 *
 * @author infoadmstudio2020-cyber
 */
@RunWith(AndroidJUnit4.class)
public class NativeServiceSDKInstrumentedTest {

    @Test
    public void useAppContext() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.roni.library.service.test", ctx.getPackageName());
    }

    @Test
    public void init_withContext_succeeds() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        NativeServiceSDKConfig config = new NativeServiceSDKConfig.Builder()
            .debugEnabled(true)
            .build();
        NativeServiceSDK lib = NativeServiceSDK.getInstance();
        lib.init(ctx, config);
        assertTrue(lib.isInitialized());
        lib.release();
    }
}
