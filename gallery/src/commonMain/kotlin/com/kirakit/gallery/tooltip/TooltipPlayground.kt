package com.kirakit.gallery.tooltip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.checkbox.KiraCheckboxWithLabel
import com.kirakit.components.tooltip.KiraSharedTooltip
import com.kirakit.components.tooltip.KiraSharedTooltipTrigger
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.components.tooltip.KiraTooltip
import com.kirakit.components.tooltip.KiraTooltipProvider
import com.kirakit.components.tooltip.KiraTooltipSide
import com.kirakit.components.tooltip.KiraTooltipSize
import com.kirakit.components.tooltip.KiraTooltipVariant
import com.kirakit.components.tooltip.rememberKiraSharedTooltipHandle
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TooltipPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    KiraTooltipProvider(delayMillis = 300L, closeDelayMillis = 150L) {
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            ExampleNote("Desktop: hover or focus any trigger. Mobile: press & hold a button to reveal its tooltip with haptic feedback.")

            SectionHeading("Basic & Positions")
            GalleryExample("Simple Tooltip") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraSimpleTooltip(text = "Save your changes") {
                        KiraButton(text = "Hover me", variant = KiraButtonVariant.Outline)
                    }
                    KiraSimpleTooltip(
                        text = "This will delete the item",
                        side = KiraTooltipSide.Right,
                    ) {
                        KiraButton(text = "Delete", variant = KiraButtonVariant.Destructive)
                    }
                }
            }

            GalleryExample("Positioning (Top, Right, Bottom, Left)") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraTooltipSide.entries.forEach { side ->
                        KiraSimpleTooltip(
                            text = "${side.name} tooltip",
                            side = side,
                        ) {
                            KiraButton(text = side.name, variant = KiraButtonVariant.Outline)
                        }
                    }
                }
            }

            SectionHeading("Custom Delay")
            GalleryExample("0ms, 200ms, 500ms & 1000ms Delays") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraSimpleTooltip(text = "Instant tooltip", delayMillis = 0L) {
                        KiraButton(text = "No delay (0ms)", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm)
                    }
                    KiraSimpleTooltip(text = "Quick tooltip", delayMillis = 200L) {
                        KiraButton(text = "Quick (200ms)", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm)
                    }
                    KiraSimpleTooltip(text = "Default delay", delayMillis = 500L) {
                        KiraButton(text = "Default (500ms)", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm)
                    }
                    KiraSimpleTooltip(text = "Slow tooltip", delayMillis = 1_000L) {
                        KiraButton(text = "Slow (1000ms)", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm)
                    }
                }
            }

            SectionHeading("Variants, Sizes & Arrow")
            GalleryExample("Style Variants") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraTooltipVariant.entries.forEach { variant ->
                        KiraSimpleTooltip(
                            text = if (variant == KiraTooltipVariant.Destructive) "Destructive action!" else "${variant.name} tooltip",
                            variant = variant,
                        ) {
                            KiraButton(
                                text = variant.name,
                                variant = if (variant == KiraTooltipVariant.Destructive) {
                                    KiraButtonVariant.Destructive
                                } else {
                                    KiraButtonVariant.Outline
                                },
                                size = KiraButtonSize.Sm,
                            )
                        }
                    }
                }
            }

            GalleryExample("Size Variants") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraSimpleTooltip(text = "Small tooltip", size = KiraTooltipSize.Sm) {
                        KiraButton(text = "Small", size = KiraButtonSize.Sm, variant = KiraButtonVariant.Outline)
                    }
                    KiraSimpleTooltip(text = "Default tooltip", size = KiraTooltipSize.Default) {
                        KiraButton(text = "Default", variant = KiraButtonVariant.Outline)
                    }
                    KiraSimpleTooltip(text = "Large tooltip text", size = KiraTooltipSize.Lg) {
                        KiraButton(text = "Large", size = KiraButtonSize.Lg, variant = KiraButtonVariant.Outline)
                    }
                }
            }

            GalleryExample("Arrow Options") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraSimpleTooltip(text = "Tooltip with arrow", showArrow = true) {
                        KiraButton(text = "With arrow", variant = KiraButtonVariant.Outline)
                    }
                    KiraSimpleTooltip(text = "Tooltip without arrow", showArrow = false) {
                        KiraButton(text = "Without arrow", variant = KiraButtonVariant.Outline)
                    }
                    KiraSimpleTooltip(text = "Arrow follows the side", side = KiraTooltipSide.Right) {
                        KiraButton(text = "Right-side arrow", variant = KiraButtonVariant.Outline)
                    }
                }
            }

            SectionHeading("Icon Buttons, Shortcuts & Shared Toolbar")
            GalleryExample("Icon Buttons with Tooltips") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraSimpleTooltip(text = "Add new item") {
                        KiraButton(
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Icon,
                            modifier = Modifier.semantics { contentDescription = "Add new item" },
                            onClick = {},
                        ) { PlusGlyph(colors.foreground) }
                    }
                    KiraSimpleTooltip(text = "Edit") {
                        KiraButton(
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Icon,
                            modifier = Modifier.semantics { contentDescription = "Edit" },
                            onClick = {},
                        ) { PencilGlyph(colors.foreground) }
                    }
                    KiraSimpleTooltip(text = "Settings") {
                        KiraButton(
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Icon,
                            modifier = Modifier.semantics { contentDescription = "Settings" },
                            onClick = {},
                        ) { GearGlyph(colors.foreground) }
                    }
                    KiraSimpleTooltip(text = "Delete", variant = KiraTooltipVariant.Destructive) {
                        KiraButton(
                            variant = KiraButtonVariant.Ghost,
                            size = KiraButtonSize.Icon,
                            modifier = Modifier.semantics { contentDescription = "Delete" },
                            onClick = {},
                        ) { TrashGlyph(colors.destructive) }
                    }
                }
            }

            GalleryExample("Keyboard Shortcuts") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraSimpleTooltip(text = "Bold", shortcut = listOf("⌘", "B")) {
                        KiraButton(text = "Bold", variant = KiraButtonVariant.Outline)
                    }
                    KiraSimpleTooltip(text = "Insert link", shortcut = listOf("⌘", "K")) {
                        KiraButton(text = "Insert link", variant = KiraButtonVariant.Outline)
                    }
                    KiraSimpleTooltip(text = "Search", shortcut = listOf("⌘", "K")) {
                        KiraButton(text = "Search", variant = KiraButtonVariant.Outline)
                    }
                }
            }

            GalleryExample("Shared Toolbar Tooltip (One Popup, Many Triggers)") {
                val handle = rememberKiraSharedTooltipHandle()
                KiraSharedTooltip(handle = handle, side = KiraTooltipSide.Bottom)

                Row(
                    modifier = Modifier
                        .clip(KiraTheme.radius.shapeMd)
                        .background(colors.background)
                        .border(1.dp, colors.border, KiraTheme.radius.shapeMd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(
                        Triple("B", "Bold", listOf("⌘", "B")),
                        Triple("I", "Italic", listOf("⌘", "I")),
                        Triple("U", "Underline", listOf("⌘", "U")),
                        Triple("⌘", "Insert link", listOf("⌘", "K")),
                    ).forEach { (glyph, tip, keys) ->
                        KiraSharedTooltipTrigger(
                            handle = handle,
                            label = tip,
                            shortcut = keys,
                        ) {
                            KiraButton(
                                text = glyph,
                                size = KiraButtonSize.Sm,
                                variant = KiraButtonVariant.Ghost,
                                modifier = Modifier.semantics { contentDescription = tip },
                                onClick = {},
                            )
                        }
                    }
                }
            }

            SectionHeading("Rich Content, Controlled & Stress Cases")
            GalleryExample("Rich Content (Compound KiraTooltip)") {
                KiraTooltip(
                    variant = KiraTooltipVariant.Outline,
                    side = KiraTooltipSide.Top,
                    delayMillis = 300L,
                    tooltip = { fg ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText(
                                text = "Advanced Tooltip",
                                style = KiraTheme.typography.label.copy(
                                    color = fg,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                            BasicText(
                                text = "With custom content structure",
                                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                            )
                        }
                    },
                ) {
                    KiraButton(text = "Rich Content", variant = KiraButtonVariant.Outline)
                }
            }

            GalleryExample("States & Stress Cases") {
                var checked by remember { mutableStateOf(true) }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        KiraSimpleTooltip(text = "Enabled trigger") {
                            KiraButton(text = "Enabled", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm)
                        }
                        KiraSimpleTooltip(
                            text = "Disabled tooltip",
                            disabled = true,
                        ) {
                            KiraButton(text = "Disabled", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, enabled = false)
                        }
                        KiraSimpleTooltip(
                            text = "A tooltip with a very long content string that must wrap instead of overflowing the viewport. It respects the maxWidth cap and the available width.",
                            maxWidth = 200.dp,
                        ) {
                            KiraButton(text = "Long content", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .clip(KiraTheme.radius.shapeMd)
                            .border(1.dp, colors.border, KiraTheme.radius.shapeMd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ExampleNote("Narrow container (~320dp): the tooltip never overflows — it is capped by the viewport width.")
                        KiraSimpleTooltip(
                            text = "Capped by viewport width, not container width",
                            side = KiraTooltipSide.Bottom,
                        ) {
                            KiraButton(
                                text = "Narrow container trigger",
                                variant = KiraButtonVariant.Outline,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    KiraSimpleTooltip(text = "Works on focusable non-button elements too") {
                        KiraCheckboxWithLabel(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            label = "Non-button trigger (hover or long-press)",
                        )
                    }
                }
            }

            GalleryExample("Controlled Tooltip") {
                var open by remember { mutableStateOf(false) }
                KiraSimpleTooltip(
                    text = "Controlled tooltip state!",
                    open = open,
                    onOpenChange = { open = it },
                ) {
                    KiraButton(
                        text = if (open) "Hide Tooltip" else "Show Tooltip",
                        variant = KiraButtonVariant.Outline,
                        onClick = { open = !open },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlusGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val sw = 1.7.dp.toPx()
        val p = size.width * 0.22f
        drawLine(color, Offset(size.width / 2f, p), Offset(size.width / 2f, size.height - p), sw, StrokeCap.Round)
        drawLine(color, Offset(p, size.height / 2f), Offset(size.width - p, size.height / 2f), sw, StrokeCap.Round)
    }
}

@Composable
private fun PencilGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val sw = 1.6.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.20f, size.height * 0.80f)
            lineTo(size.width * 0.34f, size.height * 0.56f)
            lineTo(size.width * 0.72f, size.height * 0.18f)
            lineTo(size.width * 0.84f, size.height * 0.30f)
            lineTo(size.width * 0.46f, size.height * 0.68f)
            close()
        }
        drawPath(path, color, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun GearGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val sw = 1.6.dp.toPx()
        drawCircle(color, radius = size.minDimension * 0.18f, style = Stroke(width = sw))
        drawCircle(color, radius = size.minDimension * 0.36f, style = Stroke(width = sw))
    }
}

@Composable
private fun TrashGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val sw = 1.6.dp.toPx()
        drawLine(color, Offset(size.width * 0.20f, size.height * 0.28f), Offset(size.width * 0.80f, size.height * 0.28f), sw, StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.28f)
            lineTo(size.width * 0.32f, size.height * 0.82f)
            lineTo(size.width * 0.68f, size.height * 0.82f)
            lineTo(size.width * 0.72f, size.height * 0.28f)
        }
        drawPath(path, color, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
