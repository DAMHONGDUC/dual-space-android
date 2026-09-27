package com.dd.the.universe.fake.device;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

import com.dd.the.universe.TheUniverseCore;
import com.dd.the.universe.app.BActivityThread;
import com.dd.the.universe.utils.Md5Utils;
import com.dd.the.universe.utils.Slog;

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
 *
 * <p>Công thức dẫn xuất cho cùng một kết quả trên mọi máy, nên chỉ còn dùng cho virtual user đã
 * có bản clone trước khi ID được lưu. Virtual user mới nhận ID ngẫu nhiên do process server tạo
 * một lần lúc cài ({@link #ensureAndroidId}); process của guest chỉ đọc.
 */
public final class VirtualDeviceIdentity {
    private static final String PREFS_NAME = "DeviceSpoofProfiles";
    private static final String KEY_ANDROID_ID = "android_id_";
    private static final String DEFAULT_SEED_PREFIX = "bbb_default_";
    private static final String FALLBACK_ANDROID_ID = "0f0f0f0f0f0f0f0f";
    private static final int ANDROID_ID_LENGTH = 16;
    private static final String HEX_DIGITS = "0123456789abcdef";
    private static final String TAG = "VirtualDeviceIdentity";

    private VirtualDeviceIdentity() {
    }

    public static String getAndroidIdForCurrentUser() {
        int userId;
        try {
            userId = BActivityThread.getUserId();
        } catch (Throwable e) {
            // Never fall back to another user's identity; an unknown user gets a neutral constant.
            Slog.e(TAG, "getAndroidIdForCurrentUser: user unavailable", e);
            return FALLBACK_ANDROID_ID;
        }
        return getAndroidId(userId);
    }

    public static String getAndroidId(int userId) {
        String stored = prefs().getString(KEY_ANDROID_ID + userId, null);
        return isValidAndroidId(stored) ? stored : legacyAndroidId(userId);
    }

    /**
     * Persists the identity for {@code userId} if it has none. Runs in the server process only, so a
     * single owner decides the value. Users that already had copies keep the legacy derived id.
     */
    public static String ensureAndroidId(int userId, boolean userHasExistingCopies) {
        SharedPreferences preferences = prefs();
        String stored = preferences.getString(KEY_ANDROID_ID + userId, null);
        if (isValidAndroidId(stored)) {
            return stored;
        }
        String id = initialAndroidId(userId, userHasExistingCopies, new SecureRandom());
        boolean saved = preferences.edit().putString(KEY_ANDROID_ID + userId, id).commit();
        Slog.d(TAG, "ensureAndroidId: user=" + userId + " legacy=" + userHasExistingCopies + " saved=" + saved);
        return id;
    }

    static String initialAndroidId(int userId, boolean keepLegacy, Random random) {
        if (keepLegacy) {
            return legacyAndroidId(userId);
        }
        StringBuilder id = new StringBuilder(ANDROID_ID_LENGTH);
        for (int i = 0; i < ANDROID_ID_LENGTH; i++) {
            id.append(HEX_DIGITS.charAt(random.nextInt(HEX_DIGITS.length())));
        }
        return id.toString();
    }

    static String legacyAndroidId(int userId) {
        return deriveAndroidId(DEFAULT_SEED_PREFIX + userId);
    }

    public static boolean isValidAndroidId(String value) {
        return value != null && value.matches("(?i)[0-9a-f]{16}");
    }

    @SuppressWarnings("deprecation")
    private static SharedPreferences prefs() {
        return TheUniverseCore.getContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    private static String deriveAndroidId(String seed) {
        String hash = Md5Utils.md5(seed);
        if (hash == null || hash.isEmpty()) {
            return FALLBACK_ANDROID_ID;
        }
        String normalized = hash.toLowerCase(Locale.US);
        return normalized.length() >= ANDROID_ID_LENGTH
                ? normalized.substring(0, ANDROID_ID_LENGTH)
                : String.format(Locale.US, "%1$-" + ANDROID_ID_LENGTH + "s", normalized).replace(' ', '0');
    }
}
