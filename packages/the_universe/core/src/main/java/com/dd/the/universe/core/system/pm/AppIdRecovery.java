package com.dd.the.universe.core.system.pm;

import java.util.Collection;

/** Keeps the uid allocator ahead of every app id already persisted in package settings. */
final class AppIdRecovery {
    private AppIdRecovery() {
    }

    static int nextOffsetFloor(int currentOffset, Collection<Integer> assignedAppIds, int firstApplicationUid) {
        int floor = currentOffset;
        for (Integer appId : assignedAppIds) {
            if (appId != null && appId >= firstApplicationUid) {
                floor = Math.max(floor, appId - firstApplicationUid);
            }
        }
        return floor;
    }
}
