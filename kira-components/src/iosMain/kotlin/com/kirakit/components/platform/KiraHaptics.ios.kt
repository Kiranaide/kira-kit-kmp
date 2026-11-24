package com.kirakit.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
actual fun rememberKiraHaptics(): KiraHaptics = remember {
    object : KiraHaptics {
        override fun perform(type: KiraHapticFeedbackType) {
            when (type) {
                KiraHapticFeedbackType.Light ->
                    UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
                KiraHapticFeedbackType.Medium ->
                    UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
                KiraHapticFeedbackType.Heavy ->
                    UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
                KiraHapticFeedbackType.Selection ->
                    UISelectionFeedbackGenerator().selectionChanged()
            }
        }
    }
}
