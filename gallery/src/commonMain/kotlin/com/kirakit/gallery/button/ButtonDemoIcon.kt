package com.kirakit.gallery.button

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import com.kirakit.components.button.KiraButtonIcon

internal enum class ButtonDemoIcon(outline: String, filled: Boolean = false) {
    Add("M12 5v14 M5 12h14"),
    Download("M12 3v12 M8 11l4 4l4 -4 M4 16v4h16v-4"),
    Delete("M4 6h16 M9 6V4h6v2 M6 6l1 14h10l1 -14 M10 10v6 M14 10v6"),
    Mail("M4 6h16v12H4z M4 7l8 6l8 -6"),
    Send("M3 10l18 -7l-7 18l-3 -8z M11 13l10 -10"),
    Settings("M10 3h4l1 3l3 1l3 2l-1 4l-2 2l-1 4l-4 2l-3 -2l-4 -1l-1 -4l-2 -3l2 -4l4 -1z M15 12a3 3 0 1 1 -6 0a3 3 0 1 1 6 0"),
    Heart("M12 20l-7 -7c-6 -6 3 -12 7 -6c4 -6 13 0 7 6z"),
    HeartFilled("M12 20l-7 -7c-6 -6 3 -12 7 -6c4 -6 13 0 7 6z", filled = true),
    Star("M12 3l3 6l7 1l-5 5l1 7l-6 -3l-6 3l1 -7l-5 -5l7 -1z"),
    StarFilled("M12 3l3 6l7 1l-5 5l1 7l-6 -3l-6 3l1 -7l-5 -5l7 -1z", filled = true),
    Play("M7 4l14 8l-14 8z"),
    Info("M21 12a9 9 0 1 1 -18 0a9 9 0 1 1 18 0 M12 11v5 M12 7.5v.1"),
    Check("M21 12a9 9 0 1 1 -18 0a9 9 0 1 1 18 0 M8 12l2.5 2.5l5.5 -5"),
    Next("M4 12h16 M14 6l6 6l-6 6"),
    Back("M20 12H4 M10 6l-6 6l6 6"),
    Sync("M20 9a8 8 0 0 0 -14 -3l-2 3 M4 4v5h5 M4 15a8 8 0 0 0 14 3l2 -3 M15 15h5v5"),
    Upload("M12 16V4 M8 8l4 -4l4 4 M4 16v4h16v-4"),
    Warning("M21 12a9 9 0 1 1 -18 0a9 9 0 1 1 18 0 M12 7v6 M12 16v.1"),
    ;

    val slot: KiraButtonIcon = { color, size ->
        Canvas(Modifier.size(size)) {
            scale(this.size.width / 24f, this.size.height / 24f, pivot = Offset.Zero) {
                if (filled) {
                    drawPath(path, color)
                } else {
                    drawPath(path, color, style = Stroke(1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
    }

    private val path: Path = PathParser().parsePathString(outline).toPath()
}
