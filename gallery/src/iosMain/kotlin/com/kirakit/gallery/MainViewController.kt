package com.kirakit.gallery

import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIUserInterfaceStyle

/**
 * Entry point hosted by `iosApp/iosApp/ContentView.swift`.
 * Compose Multiplatform 1.11+ enables concurrent Skia/Metal rendering by default on iOS.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val controller = LocalUIViewController.current
    GalleryApp { dark, followsSystem ->
        val style = when {
            followsSystem -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            dark -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            else -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
        }
        controller.view.window?.overrideUserInterfaceStyle = style
    }
}
