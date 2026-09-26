package com.dd.dual.space.features.virtualization.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchConfirmationTest {
    @Test
    fun `dispatch without guest acknowledgement times out`() {
        var now: Long = 0L
        val confirmation = LaunchConfirmation({ now }, { now += it })
        assertFalse(confirmation.await(250L) { false })
        assertTrue(now == 250L)
    }

    @Test
    fun `only a new guest acknowledgement confirms launch`() {
        var now: Long = 0L
        val confirmation = LaunchConfirmation({ now }, { now += it })
        assertTrue(confirmation.await(250L) { now >= 100L })
        assertTrue(now == 100L)
    }
}
