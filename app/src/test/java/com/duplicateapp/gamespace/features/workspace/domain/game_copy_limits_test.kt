package com.duplicateapp.gamespace.features.workspace.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameCopyLimitsTest {
    @Test
    fun `allocates both copy slots in order`() {
        assertEquals(1, GameCopyLimits.nextAvailableVirtualUserId(emptySet()))
        assertEquals(2, GameCopyLimits.nextAvailableVirtualUserId(setOf(1)))
    }

    @Test
    fun `reuses a free slot`() {
        assertEquals(1, GameCopyLimits.nextAvailableVirtualUserId(setOf(2)))
    }

    @Test
    fun `rejects a third copy`() {
        assertNull(GameCopyLimits.nextAvailableVirtualUserId(setOf(1, 2)))
    }
}
