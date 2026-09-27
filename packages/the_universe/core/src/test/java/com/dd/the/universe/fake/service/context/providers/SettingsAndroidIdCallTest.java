package com.dd.the.universe.fake.service.context.providers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SettingsAndroidIdCallTest {
    @Test
    public void matchesModernAttributionSourceLayout() {
        assertTrue(SettingsAndroidIdCall.matches(new Object[]{new Object(), "settings", "GET_secure", "android_id", null}));
    }

    @Test
    public void matchesLegacyCallingPackageLayout() {
        assertTrue(SettingsAndroidIdCall.matches(new Object[]{"com.example.game", "GET_secure", "android_id", null}));
    }

    @Test
    public void ignoresOtherSecureKeysAndTables() {
        assertFalse(SettingsAndroidIdCall.matches(new Object[]{"settings", "GET_secure", "default_input_method", null}));
        assertFalse(SettingsAndroidIdCall.matches(new Object[]{"settings", "GET_global", "android_id", null}));
        assertFalse(SettingsAndroidIdCall.matches(null));
    }
}
