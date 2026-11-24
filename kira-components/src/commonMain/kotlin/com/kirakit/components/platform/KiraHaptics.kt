package com.kirakit.components.platform

import androidx.compose.runtime.Composable

enum class KiraHapticFeedbackType {
    Light,
    Medium,
    Heavy,
    Selection,
}

interface KiraHaptics {
    fun perform(type: KiraHapticFeedbackType)
}

/**
 * Selective native escape-hatch example:
 * - iOS: `UIImpactFeedbackGenerator` / `UISelectionFeedbackGenerator`
 * - Android: `View.performHapticFeedback`
 * - JVM Desktop: No-op
 */
@Composable
expect fun rememberKiraHaptics(): KiraHaptics
