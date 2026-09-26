package com.dd.the.universe.fake.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Locale;

import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.app.BActivityThread;
import com.dd.the.universe.utils.Md5Utils;

/**
 * Cấp cho mỗi virtual user một Android ID riêng và ổn định.
 *
 * <p>Đây là cách ly giữa các bản clone, không phải giả mạo thiết bị: nếu mọi bản clone
 * dùng chung Android ID của máy thật thì ứng dụng đích nối được các tài khoản với nhau,
 * đúng thứ người dùng cài app này để tránh. Các thuộc tính thiết bị khác
 * (manufacturer, model, fingerprint, serial) được giữ nguyên như máy thật.
 *
 * <p>Tên preferences và công thức dẫn xuất phải giữ nguyên vĩnh viễn: đổi chúng sẽ cấp
 * Android ID mới cho các bản clone đang tồn tại, và người dùng bị đăng xuất khỏi game.
 */
public final class VirtualDeviceIdentity {
    private static final String PREFS_NAME = "DeviceSpoofProfiles";
    private static final String KEY_ANDROID_ID = "android_id_";
    private static final String DEFAULT_SEED_PREFIX = "bbb_default_";
    private static final String FALLBACK_ANDROID_ID = "0f0f0f0f0f0f0f0f";
    private static final int ANDROID_ID_LENGTH = 16;

    private VirtualDeviceIdentity() {
    }

    public static String getAndroidIdForCurrentUser() {
        int userId;
        try {
            userId = BActivityThread.getUserId();
        } catch (Throwable e) {
            userId = 0;
        }
        return getAndroidId(userId);
    }

    public static String getAndroidId(int userId) {
        String stored = prefs().getString(KEY_ANDROID_ID + userId, null);
        return TextUtils.isEmpty(stored) ? deriveAndroidId(DEFAULT_SEED_PREFIX + userId) : stored;
    }

    public static boolean isValidAndroidId(String value) {
        return !TextUtils.isEmpty(value) && value.matches("(?i)[0-9a-f]{16}");
    }

    @SuppressWarnings("deprecation")
    private static SharedPreferences prefs() {
        return TheUniverseCore.getContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    private static String deriveAndroidId(String seed) {
        String hash = Md5Utils.md5(seed);
        if (TextUtils.isEmpty(hash)) {
            return FALLBACK_ANDROID_ID;
        }
        String normalized = hash.toLowerCase(Locale.US);
        return normalized.length() >= ANDROID_ID_LENGTH
                ? normalized.substring(0, ANDROID_ID_LENGTH)
                : String.format(Locale.US, "%1$-" + ANDROID_ID_LENGTH + "s", normalized).replace(' ', '0');
    }
}
