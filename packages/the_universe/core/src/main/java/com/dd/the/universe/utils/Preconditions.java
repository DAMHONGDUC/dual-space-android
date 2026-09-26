package com.dd.the.universe.utils;

/**
 * Thay cho androidx.core.util.Preconditions. Engine chỉ dùng checkArgument, nên
 * không đáng để kéo về cả một thư viện androidx cho một phương thức.
 */
public final class Preconditions {
    private Preconditions() {
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }
}
