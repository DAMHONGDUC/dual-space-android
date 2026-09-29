package com.dd.dual.space.features.workspace.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class InstalledGameTest {
    private val chat = InstalledGame("Chat Messenger", "com.example.chat", ProfileTarget.managed)
    private val maps = InstalledGame("Maps", "org.sample.navigation", ProfileTarget.managed)
    private val apps = listOf(chat, maps)

    @Test
    fun `blank query keeps every app`() {
        assertEquals(apps, apps.filterByQuery("  "))
    }

    @Test
    fun `matches name ignoring case`() {
        assertEquals(listOf(chat), apps.filterByQuery("messenger"))
    }

    @Test
    fun `matches package id`() {
        assertEquals(listOf(maps), apps.filterByQuery(" org.sample "))
    }

    @Test
    fun `returns empty list when nothing matches`() {
        assertEquals(emptyList<InstalledGame>(), apps.filterByQuery("zzz"))
    }
}
