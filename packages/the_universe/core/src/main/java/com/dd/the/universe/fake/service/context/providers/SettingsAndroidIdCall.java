package com.dd.the.universe.fake.service.context.providers;

/**
 * Recognises the settings provider call behind {@code Settings.Secure.getString(ANDROID_ID)}.
 * The argument layout differs across API levels, so the method name is located by value.
 */
final class SettingsAndroidIdCall {
    static final String GET_SECURE = "GET_secure";
    static final String ANDROID_ID = "android_id";

    private SettingsAndroidIdCall() {
    }

    static boolean matches(Object[] args) {
        if (args == null) {
            return false;
        }
        for (int i = 0; i < args.length - 1; i++) {
            if (GET_SECURE.equals(args[i]) && args[i + 1] instanceof String) {
                return ANDROID_ID.equalsIgnoreCase((String) args[i + 1]);
            }
        }
        return false;
    }
}
