package com.kirakit.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KiraThemeTest {
    @Test
    fun allElevenThemesAreRegistered() {
        assertEquals(11, KiraThemes.All.size)
        val ids = KiraThemes.All.map { it.id }.toSet()
        assertEquals(11, ids.size, "Theme IDs must be unique")
        assertTrue(ids.contains("theme-default"))
    }

    @Test
    fun motionTokensMatchExportedSpec() {
        assertEquals(150, KiraMotion.RouteFadeMs)
        assertEquals(250, KiraMotion.RouteTransitionMs)
        assertEquals(0.65f, KiraMotion.SpringDampingRatio)
    }

    @Test
    fun allFiveRadiusPresetsAreRegistered() {
        assertEquals(5, KiraRadiusPresets.All.size)
        assertEquals(0f, KiraRadiusPresets.Sharp.scale.lg.value)
        assertEquals(4f, KiraRadiusPresets.Sharp.scale.xl.value)
        assertEquals(10f, KiraRadiusPresets.Balanced.scale.lg.value)
        assertEquals(14f, KiraRadiusPresets.Balanced.scale.xl.value)
        assertEquals(20f, KiraRadiusPresets.Pill.scale.lg.value)
    }
}
