package com.kirakit.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberKiraHaptics(): KiraHaptics = remember {
    object : KiraHaptics {
        override fun perform(type: KiraHapticFeedbackType) = Unit
    }
}
