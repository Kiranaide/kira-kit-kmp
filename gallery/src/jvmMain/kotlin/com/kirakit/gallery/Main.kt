package com.kirakit.gallery

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kira Kit KMP Gallery",
        state = rememberWindowState(width = 960.dp, height = 760.dp),
    ) {
        GalleryApp()
    }
}
