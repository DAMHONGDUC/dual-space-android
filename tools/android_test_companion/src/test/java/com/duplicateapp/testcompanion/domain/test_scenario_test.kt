package com.duplicateapp.testcompanion.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TestScenarioTest {
    @Test
    fun targetPackageMatchesParallelGameSpace() {
        assertEquals("com.dd.dual.space", TestScenario.targetPackage)
    }

    @Test
    fun criticalStepsSupportEnglishAndVietnamese() {
        assertTrue(TestScenario.continueLabels.containsAll(listOf("Continue", "Tiếp tục")))
        assertTrue(TestScenario.settingsLabels.containsAll(listOf("Settings", "Cài đặt")))
        assertTrue(TestScenario.aboutLabels.containsAll(listOf("About and help", "Giới thiệu và trợ giúp")))
    }
}
