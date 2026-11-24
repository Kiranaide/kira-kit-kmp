package com.kirakit.gallery.togglegroup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.togglegroup.KiraToggleGroup
import com.kirakit.components.togglegroup.KiraToggleGroupColor
import com.kirakit.components.togglegroup.KiraToggleGroupItem
import com.kirakit.components.togglegroup.KiraToggleGroupOrientation
import com.kirakit.components.togglegroup.KiraToggleGroupOverflowMode
import com.kirakit.components.togglegroup.KiraToggleGroupRadius
import com.kirakit.components.togglegroup.KiraToggleGroupSemantics
import com.kirakit.components.togglegroup.KiraToggleGroupSize
import com.kirakit.components.togglegroup.KiraToggleGroupVariant
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@Composable
internal fun ToggleGroupPlayground() {
    val gap = KiraTheme.spacing.md

    val formatItems = remember {
        listOf(
            KiraToggleGroupItem(
                value = "bold",
                contentDescription = "Toggle bold",
                tooltip = "Bold",
                icon = { fg -> BasicText("B", style = KiraTheme.typography.label.copy(color = fg, fontWeight = FontWeight.Bold, fontSize = 15.sp)) },
            ),
            KiraToggleGroupItem(
                value = "italic",
                contentDescription = "Toggle italic",
                tooltip = "Italic",
                icon = { fg -> BasicText("I", style = KiraTheme.typography.label.copy(color = fg, fontStyle = FontStyle.Italic, fontSize = 15.sp)) },
            ),
            KiraToggleGroupItem(
                value = "underline",
                contentDescription = "Toggle underline",
                tooltip = "Underline",
                icon = { fg -> BasicText("U", style = KiraTheme.typography.label.copy(color = fg, textDecoration = TextDecoration.Underline, fontSize = 15.sp)) },
            ),
        )
    }

    val alignItems = remember {
        listOf(
            KiraToggleGroupItem("left", contentDescription = "Align left", icon = { AlignGlyph(it, 0) }),
            KiraToggleGroupItem("center", contentDescription = "Align center", icon = { AlignGlyph(it, 1) }),
            KiraToggleGroupItem("right", contentDescription = "Align right", icon = { AlignGlyph(it, 2) }),
            KiraToggleGroupItem("justify", contentDescription = "Align justify", icon = { AlignGlyph(it, 3) }),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Selection Modes")
        GalleryExample("Multiple & Single Selection") {
            var multi by remember { mutableStateOf(setOf("bold")) }
            var single by remember { mutableStateOf(setOf("left")) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExampleNote("Multiple selection — Selected: ${multi.joinToString(", ").ifEmpty { "None" }}")
                KiraToggleGroup(
                    items = formatItems,
                    selectedValues = multi,
                    onSelectedValuesChange = { multi = it },
                    multiple = true,
                    contentDescription = "Text formatting",
                )

                ExampleNote("Single selection (one-selected-always) — Selected: ${single.joinToString(", ")}")
                KiraToggleGroup(
                    items = alignItems,
                    selectedValues = single,
                    onSelectedValuesChange = { single = it },
                    contentDescription = "Text alignment",
                )
            }
        }

        SectionHeading("Variants & Sliding Indicator")
        GalleryExample("Outline, Segment (Sliding Pill) & Pills") {
            var outlineVal by remember { mutableStateOf(setOf("left")) }
            var segVal by remember { mutableStateOf(setOf("grid")) }
            var pillsVal by remember { mutableStateOf(setOf("all")) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExampleNote("Outline")
                KiraToggleGroup(
                    items = alignItems.take(3),
                    selectedValues = outlineVal,
                    onSelectedValuesChange = { outlineVal = it },
                    variant = KiraToggleGroupVariant.Outline,
                    contentDescription = "Text alignment",
                )

                ExampleNote("Segment with Sliding Indicator")
                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("grid", "Grid"),
                        KiraToggleGroupItem("list", "List"),
                        KiraToggleGroupItem("board", "Board"),
                    ),
                    selectedValues = segVal,
                    onSelectedValuesChange = { segVal = it },
                    variant = KiraToggleGroupVariant.Segment,
                    slidingIndicator = true,
                    contentDescription = "View mode",
                )

                ExampleNote("Pills (Multiple + Color Variants)")
                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("all", "All"),
                        KiraToggleGroupItem("active", "Active", colorVariant = KiraToggleGroupColor.Success),
                        KiraToggleGroupItem("warning", "Warning", colorVariant = KiraToggleGroupColor.Warning),
                        KiraToggleGroupItem("delete", "Delete", colorVariant = KiraToggleGroupColor.Destructive),
                    ),
                    selectedValues = pillsVal,
                    onSelectedValuesChange = { pillsVal = it },
                    variant = KiraToggleGroupVariant.Pills,
                    multiple = true,
                    contentDescription = "Filter options",
                )
            }
        }

        SectionHeading("Sizes & Vertical Orientation")
        GalleryExample("Small, Default, Large & Vertical") {
            var smVal by remember { mutableStateOf(setOf("left")) }
            var defVal by remember { mutableStateOf(setOf("center")) }
            var lgVal by remember { mutableStateOf(setOf("right")) }
            var vertVal by remember { mutableStateOf(setOf("top")) }
            val lrItems = remember {
                listOf(
                    KiraToggleGroupItem("left", "Left"),
                    KiraToggleGroupItem("center", "Center"),
                    KiraToggleGroupItem("right", "Right"),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraToggleGroup(items = lrItems, selectedValues = smVal, onSelectedValuesChange = { smVal = it }, variant = KiraToggleGroupVariant.Outline, size = KiraToggleGroupSize.Sm)
                KiraToggleGroup(items = lrItems, selectedValues = defVal, onSelectedValuesChange = { defVal = it }, variant = KiraToggleGroupVariant.Outline, size = KiraToggleGroupSize.Default)
                KiraToggleGroup(items = lrItems, selectedValues = lgVal, onSelectedValuesChange = { lgVal = it }, variant = KiraToggleGroupVariant.Outline, size = KiraToggleGroupSize.Lg)

                ExampleNote("Vertical Outline")
                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("top", "Top"),
                        KiraToggleGroupItem("middle", "Middle"),
                        KiraToggleGroupItem("bottom", "Bottom"),
                    ),
                    selectedValues = vertVal,
                    onSelectedValuesChange = { vertVal = it },
                    variant = KiraToggleGroupVariant.Outline,
                    orientation = KiraToggleGroupOrientation.Vertical,
                )
            }
        }

        SectionHeading("One-Selected-Always, AllowEmpty, Selected Icon & Shape Morph")
        GalleryExample("Segmented Control Rules & M3 Expressive Morph") {
            var rangeVal by remember { mutableStateOf(setOf("day")) }
            var filterVal by remember { mutableStateOf(emptySet<String>()) }
            var morphVal by remember { mutableStateOf(setOf("comfortable")) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExampleNote("Default single-select cannot be cleared (Selected: ${rangeVal.joinToString()})")
                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("day", "Day"),
                        KiraToggleGroupItem("week", "Week"),
                        KiraToggleGroupItem("month", "Month"),
                    ),
                    selectedValues = rangeVal,
                    onSelectedValuesChange = { rangeVal = it },
                    variant = KiraToggleGroupVariant.Segment,
                    slidingIndicator = true,
                    semanticsMode = KiraToggleGroupSemantics.Radio,
                )

                ExampleNote("allowEmpty = true lets you deselect (Selected: ${filterVal.joinToString().ifEmpty { "None" }})")
                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("starred", "Starred"),
                        KiraToggleGroupItem("mine", "Mine"),
                    ),
                    selectedValues = filterVal,
                    onSelectedValuesChange = { filterVal = it },
                    variant = KiraToggleGroupVariant.Segment,
                    allowEmpty = true,
                )

                ExampleNote("showSelectedIcon + shapeMorph + pill radius")
                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("compact", "Compact"),
                        KiraToggleGroupItem("comfortable", "Comfortable"),
                        KiraToggleGroupItem("spacious", "Spacious"),
                    ),
                    selectedValues = morphVal,
                    onSelectedValuesChange = { morphVal = it },
                    variant = KiraToggleGroupVariant.Default,
                    radius = KiraToggleGroupRadius.Pill,
                    showSelectedIcon = true,
                    shapeMorph = true,
                )
            }
        }

        SectionHeading("Disabled, Validation & Overflow Modes")
        GalleryExample("Disabled Items, Validation Error & Scrollable Overflow") {
            var disVal by remember { mutableStateOf(setOf("left")) }
            var errVal by remember { mutableStateOf(emptySet<String>()) }
            var scrollVal by remember { mutableStateOf(setOf("day")) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("left", "Left"),
                        KiraToggleGroupItem("center", "Center"),
                        KiraToggleGroupItem("right", "Right", disabled = true, disabledReason = "Right alignment is locked"),
                    ),
                    selectedValues = disVal,
                    onSelectedValuesChange = { disVal = it },
                    variant = KiraToggleGroupVariant.Outline,
                )

                KiraToggleGroup(
                    items = listOf(
                        KiraToggleGroupItem("email", "Email"),
                        KiraToggleGroupItem("sms", "SMS"),
                        KiraToggleGroupItem("push", "Push"),
                    ),
                    selectedValues = errVal,
                    onSelectedValuesChange = { errVal = it },
                    variant = KiraToggleGroupVariant.Outline,
                    allowEmpty = true,
                    invalid = errVal.isEmpty(),
                    errorMessage = "Select at least one delivery channel.",
                )

                ExampleNote("overflowMode = Scroll (constrained 260dp)")
                KiraToggleGroup(
                    items = listOf("day", "week", "month", "quarter", "year", "all").map {
                        KiraToggleGroupItem(it, it.replaceFirstChar { c -> c.uppercase() })
                    },
                    selectedValues = scrollVal,
                    onSelectedValuesChange = { scrollVal = it },
                    variant = KiraToggleGroupVariant.Segment,
                    overflowMode = KiraToggleGroupOverflowMode.Scroll,
                    modifier = Modifier.widthIn(max = 260.dp),
                )
            }
        }
    }
}

@Composable
private fun AlignGlyph(color: Color, mode: Int) {
    Canvas(Modifier.size(16.dp)) {
        val sw = 1.6.dp.toPx()
        val y1 = size.height * 0.25f
        val y2 = size.height * 0.50f
        val y3 = size.height * 0.75f
        val fullStart = size.width * 0.14f
        val fullEnd = size.width * 0.86f
        val shortStart = when (mode) {
            0 -> fullStart
            1 -> size.width * 0.26f
            2 -> size.width * 0.38f
            else -> fullStart
        }
        val shortEnd = when (mode) {
            0 -> size.width * 0.62f
            1 -> size.width * 0.74f
            2 -> fullEnd
            else -> fullEnd
        }
        drawLine(color, Offset(fullStart, y1), Offset(fullEnd, y1), sw, StrokeCap.Round)
        drawLine(color, Offset(shortStart, y2), Offset(shortEnd, y2), sw, StrokeCap.Round)
        drawLine(color, Offset(fullStart, y3), Offset(fullEnd, y3), sw, StrokeCap.Round)
    }
}
