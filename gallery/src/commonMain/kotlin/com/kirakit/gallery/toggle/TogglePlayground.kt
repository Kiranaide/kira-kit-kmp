package com.kirakit.gallery.toggle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.toggle.KiraToggle
import com.kirakit.components.toggle.KiraToggleIndicatorPosition
import com.kirakit.components.toggle.KiraToggleRadius
import com.kirakit.components.toggle.KiraToggleSize
import com.kirakit.components.toggle.KiraToggleVariant
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TogglePlayground() {
    val gap = KiraTheme.spacing.md
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Variants")
        GalleryExample("Basic Formatting Toggles") {
            var bold by remember { mutableStateOf(true) }
            var italic by remember { mutableStateOf(false) }
            var underline by remember { mutableStateOf(false) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraToggle(pressed = bold, onPressedChange = { bold = it }, size = KiraToggleSize.Icon, contentDescription = "Toggle bold") { fg ->
                    BasicText("B", style = KiraTheme.typography.label.copy(color = fg, fontWeight = FontWeight.Bold, fontSize = 15.sp))
                }
                KiraToggle(pressed = italic, onPressedChange = { italic = it }, size = KiraToggleSize.Icon, contentDescription = "Toggle italic") { fg ->
                    BasicText("I", style = KiraTheme.typography.label.copy(color = fg, fontStyle = FontStyle.Italic, fontSize = 15.sp))
                }
                KiraToggle(pressed = underline, onPressedChange = { underline = it }, size = KiraToggleSize.Icon, contentDescription = "Toggle underline") { fg ->
                    BasicText("U", style = KiraTheme.typography.label.copy(color = fg, textDecoration = TextDecoration.Underline, fontSize = 15.sp))
                }
            }
        }

        GalleryExample("Style & Semantic Variants") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        KiraToggleVariant.Default,
                        KiraToggleVariant.Outline,
                        KiraToggleVariant.Ghost,
                        KiraToggleVariant.Solid,
                    ).forEach { variant ->
                        var on by remember(variant) { mutableStateOf(true) }
                        KiraToggle(pressed = on, onPressedChange = { on = it }, text = variant.name, variant = variant)
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    var accept by remember { mutableStateOf(true) }
                    var warn by remember { mutableStateOf(true) }
                    var del by remember { mutableStateOf(true) }
                    KiraToggle(pressed = accept, onPressedChange = { accept = it }, text = "Accept", variant = KiraToggleVariant.Success)
                    KiraToggle(pressed = warn, onPressedChange = { warn = it }, text = "Warn", variant = KiraToggleVariant.Warning)
                    KiraToggle(pressed = del, onPressedChange = { del = it }, text = "Delete", variant = KiraToggleVariant.Destructive)
                }
            }
        }

        SectionHeading("Sizes, Radius & Indicators")
        GalleryExample("Sizes & Radius") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(KiraToggleSize.Sm, KiraToggleSize.Default, KiraToggleSize.Lg, KiraToggleSize.Icon).forEach { sz ->
                        var on by remember(sz) { mutableStateOf(true) }
                        KiraToggle(
                            pressed = on,
                            onPressedChange = { on = it },
                            text = if (sz == KiraToggleSize.Icon) null else sz.name,
                            size = sz,
                            variant = KiraToggleVariant.Outline,
                            contentDescription = "Size ${sz.name}",
                        ) { fg ->
                            if (sz == KiraToggleSize.Icon) {
                                BasicText("B", style = KiraTheme.typography.label.copy(color = fg, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraToggleRadius.entries.forEach { rad ->
                        var on by remember(rad) { mutableStateOf(true) }
                        KiraToggle(
                            pressed = on,
                            onPressedChange = { on = it },
                            text = rad.name,
                            variant = KiraToggleVariant.Outline,
                            radius = rad,
                        )
                    }
                }
            }
        }

        GalleryExample("Check Indicator & Loading") {
            var enabledInd by remember { mutableStateOf(true) }
            var featureInd by remember { mutableStateOf(true) }
            var loadingClick by remember { mutableStateOf(false) }
            var loadPressed by remember { mutableStateOf(false) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                KiraToggle(
                    pressed = enabledInd,
                    onPressedChange = { enabledInd = it },
                    text = "Enabled",
                    showIndicator = true,
                )
                KiraToggle(
                    pressed = featureInd,
                    onPressedChange = { featureInd = it },
                    text = "Feature On",
                    showIndicator = true,
                    indicatorPosition = KiraToggleIndicatorPosition.Right,
                )
                KiraToggle(
                    pressed = loadPressed,
                    onPressedChange = { next ->
                        loadingClick = true
                        scope.launch {
                            delay(1_200L)
                            loadPressed = next
                            loadingClick = false
                        }
                    },
                    text = if (loadingClick) "Loading..." else "Click to Load",
                    loading = loadingClick,
                    variant = KiraToggleVariant.Outline,
                )
            }
        }

        SectionHeading("Rich Examples, Async Optimistic & Disabled Reason")
        GalleryExample("Bookmark, Favorite & Star (Pressed Icon Swap)") {
            var bookmarked by remember { mutableStateOf(false) }
            var liked by remember { mutableStateOf(true) }
            var starred by remember { mutableStateOf(false) }
            val colors = KiraTheme.colors

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                KiraToggle(
                    pressed = bookmarked,
                    onPressedChange = { bookmarked = it },
                    text = "Bookmark",
                    icon = { fg -> BookmarkGlyph(fg, filled = false) },
                    pressedIcon = { fg -> BookmarkGlyph(fg, filled = true) },
                )
                KiraToggle(
                    pressed = liked,
                    onPressedChange = { liked = it },
                    text = "Like",
                    variant = KiraToggleVariant.Outline,
                    radius = KiraToggleRadius.Pill,
                    icon = { fg -> HeartGlyph(fg, filled = false) },
                    pressedIcon = { HeartGlyph(colors.destructive, filled = true) },
                )
                KiraToggle(
                    pressed = starred,
                    onPressedChange = { starred = it },
                    variant = KiraToggleVariant.Outline,
                    size = KiraToggleSize.IconLg,
                    radius = KiraToggleRadius.Pill,
                    tooltip = "Star item",
                    contentDescription = "Toggle star",
                    icon = { fg -> StarGlyph(fg, filled = false) },
                    pressedIcon = { StarGlyph(colors.warning, filled = true) },
                )
            }
        }

        GalleryExample("KiraToggle: Async Optimistic, Shortcut, Label & Disabled Reason") {
            var pinned by remember { mutableStateOf(false) }
            var failing by remember { mutableStateOf(false) }
            var muted by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraToggle(
                        pressed = pinned,
                        onPressedChange = { pinned = it },
                        text = "Pin (async succeeds)",
                        variant = KiraToggleVariant.Outline,
                        shortcut = "⌘P",
                        onToggleAsync = {
                            delay(800L)
                            Result.success(Unit)
                        },
                    )
                    KiraToggle(
                        pressed = failing,
                        onPressedChange = { failing = it },
                        text = "Sync (fails & rolls back)",
                        variant = KiraToggleVariant.Outline,
                        errorMessage = "Sync failed — rolled back",
                        onToggleAsync = {
                            delay(800L)
                            Result.failure(IllegalStateException("Network error"))
                        },
                    )
                    KiraToggle(
                        pressed = false,
                        onPressedChange = {},
                        text = "Locked",
                        variant = KiraToggleVariant.Outline,
                        enabled = false,
                        disabledReason = "Requires Team plan",
                    )
                }

                KiraToggle(
                    pressed = muted,
                    onPressedChange = { muted = it },
                    text = "Mute",
                    variant = KiraToggleVariant.Solid,
                    label = "Audio alerts",
                    description = "Silence incoming notification sounds.",
                )
                ExampleNote("Tap 'Sync (fails & rolls back)' to see optimistic rollback + toast notification.")
            }
        }
    }
}

@Composable
private fun BookmarkGlyph(color: Color, filled: Boolean) {
    Canvas(Modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.24f, size.height * 0.14f)
            lineTo(size.width * 0.76f, size.height * 0.14f)
            lineTo(size.width * 0.76f, size.height * 0.86f)
            lineTo(size.width * 0.50f, size.height * 0.68f)
            lineTo(size.width * 0.24f, size.height * 0.86f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = if (filled) Fill else Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
private fun HeartGlyph(color: Color, filled: Boolean) {
    Canvas(Modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.84f)
            cubicTo(size.width * 0.10f, size.height * 0.56f, size.width * 0.12f, size.height * 0.22f, size.width * 0.34f, size.height * 0.22f)
            cubicTo(size.width * 0.44f, size.height * 0.22f, size.width * 0.50f, size.height * 0.30f, size.width * 0.50f, size.height * 0.36f)
            cubicTo(size.width * 0.50f, size.height * 0.30f, size.width * 0.56f, size.height * 0.22f, size.width * 0.66f, size.height * 0.22f)
            cubicTo(size.width * 0.88f, size.height * 0.22f, size.width * 0.90f, size.height * 0.56f, size.width * 0.50f, size.height * 0.84f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = if (filled) Fill else Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
private fun StarGlyph(color: Color, filled: Boolean) {
    Canvas(Modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.12f)
            lineTo(size.width * 0.62f, size.height * 0.38f)
            lineTo(size.width * 0.90f, size.height * 0.42f)
            lineTo(size.width * 0.69f, size.height * 0.61f)
            lineTo(size.width * 0.74f, size.height * 0.88f)
            lineTo(size.width * 0.50f, size.height * 0.75f)
            lineTo(size.width * 0.26f, size.height * 0.88f)
            lineTo(size.width * 0.31f, size.height * 0.61f)
            lineTo(size.width * 0.10f, size.height * 0.42f)
            lineTo(size.width * 0.38f, size.height * 0.38f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = if (filled) Fill else Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
