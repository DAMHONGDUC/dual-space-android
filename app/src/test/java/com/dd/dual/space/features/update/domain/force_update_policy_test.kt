package com.dd.dual.space.features.update.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForceUpdatePolicyTest {
    @Test
    fun `high priority immediate update is required`() {
        assertTrue(ForceUpdatePolicy.requiresUpdate(true, true, 4))
    }

    @Test
    fun `minor update does not block the app`() {
        assertFalse(ForceUpdatePolicy.requiresUpdate(true, true, 3))
    }

    @Test
    fun `unsupported immediate update does not block the app`() {
        assertFalse(ForceUpdatePolicy.requiresUpdate(true, false, 5))
    }
}
