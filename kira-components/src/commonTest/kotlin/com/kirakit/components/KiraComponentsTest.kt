package com.kirakit.components

import com.kirakit.components.badge.KiraBadgeCountFormat
import com.kirakit.components.badge.formatBadgeCount
import com.kirakit.components.button.KiraButtonSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KiraComponentsTest {
    @Test
    fun buttonSizesMeetMinimumTouchTarget() {
        KiraButtonSize.entries.forEach { size ->
            assertTrue(
                size.minHeight.value >= 44f,
                "Expected ${size.name} minHeight >= 44.dp, got ${size.minHeight}",
            )
        }
    }

    @Test
    fun badgeCountFormatsDefaultShortAndCommas() {
        assertEquals("42", formatBadgeCount(42))
        assertEquals("99+", formatBadgeCount(150, max = 99))
        assertEquals("1.5k+", formatBadgeCount(1_500, format = KiraBadgeCountFormat.Short))
        assertEquals("2M+", formatBadgeCount(2_000_000, format = KiraBadgeCountFormat.Short))
        assertEquals("1,250", formatBadgeCount(1_250, format = KiraBadgeCountFormat.Commas, max = 9_999))
        assertEquals("9,999+", formatBadgeCount(12_000, format = KiraBadgeCountFormat.Commas, max = 9_999))
    }
}
