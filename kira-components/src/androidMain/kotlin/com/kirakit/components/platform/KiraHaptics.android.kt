package com.kirakit.components.platform

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
actual fun rememberKiraHaptics(): KiraHaptics {
    val view = LocalView.current
    return remember(view) {
        object : KiraHaptics {
            override fun perform(type: KiraHapticFeedbackType) {
                val constant = when (type) {
                    KiraHapticFeedbackType.Light -> HapticFeedbackConstants.CLOCK_TICK
                    KiraHapticFeedbackType.Medium -> HapticFeedbackConstants.CONTEXT_CLICK
                    KiraHapticFeedbackType.Heavy -> HapticFeedbackConstants.LONG_PRESS
                    KiraHapticFeedbackType.Selection -> HapticFeedbackConstants.TEXT_HANDLE_MOVE
                }
                view.performHapticFeedback(constant)
            }
        }
    }
}
