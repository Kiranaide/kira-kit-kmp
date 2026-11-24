package com.kirakit.gallery.slider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.slider.KiraRangeSlider
import com.kirakit.components.slider.KiraSlider
import com.kirakit.components.slider.KiraSliderMarker
import com.kirakit.components.slider.KiraSliderOrientation
import com.kirakit.components.slider.KiraSliderSize
import com.kirakit.components.slider.KiraSliderTooltipMode
import com.kirakit.components.slider.KiraSliderVariant
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlin.math.roundToInt

@Composable
internal fun SliderPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic, Sizes & Variants")
        GalleryExample("Basic Slider") {
            var volume by remember { mutableFloatStateOf(50f) }
            KiraSlider(
                value = volume,
                onValueChange = { volume = it },
                label = "Volume",
                showValue = true,
            )
        }

        GalleryExample("Size Tiers (Sm, Default, Lg)") {
            var sm by remember { mutableFloatStateOf(30f) }
            var def by remember { mutableFloatStateOf(50f) }
            var lg by remember { mutableFloatStateOf(70f) }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                KiraSlider(value = sm, onValueChange = { sm = it }, size = KiraSliderSize.Sm, label = "Small")
                KiraSlider(value = def, onValueChange = { def = it }, size = KiraSliderSize.Default, label = "Default")
                KiraSlider(value = lg, onValueChange = { lg = it }, size = KiraSliderSize.Lg, label = "Large")
            }
        }

        GalleryExample("Color Variants (Default, Gradient, Info, Success, Warning, Destructive)") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraSliderVariant.entries.forEach { variant ->
                    var v by remember(variant) { mutableFloatStateOf(65f) }
                    KiraSlider(
                        value = v,
                        onValueChange = { v = it },
                        variant = variant,
                        label = variant.name,
                        showValue = true,
                    )
                }
            }
        }

        SectionHeading("Steps, Sparse Stops & Custom Markers")
        GalleryExample("Steps, Step Markers, Sparse Stops & Labeled Markers") {
            var stepVal by remember { mutableFloatStateOf(25f) }
            var zoomVal by remember { mutableFloatStateOf(50f) }
            var sparseVal by remember { mutableFloatStateOf(10f) }
            var intensityVal by remember { mutableFloatStateOf(2f) }

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                KiraSlider(
                    value = stepVal,
                    onValueChange = { stepVal = it },
                    step = 25f,
                    showValue = true,
                    label = "Step Slider (0, 25, 50, 75, 100)",
                )
                KiraSlider(
                    value = zoomVal,
                    onValueChange = { zoomVal = it },
                    step = 10f,
                    showStepMarkers = true,
                    showValue = true,
                    label = "Zoom Level (Step Markers)",
                )
                KiraSlider(
                    value = sparseVal,
                    onValueChange = { sparseVal = it },
                    stepOptions = listOf(0f, 10f, 40f, 90f, 100f),
                    showValue = true,
                    label = "Sparse Stops (0, 10, 40, 90, 100)",
                )
                KiraSlider(
                    value = intensityVal,
                    onValueChange = { intensityVal = it },
                    min = 0f,
                    max = 4f,
                    step = 1f,
                    markers = listOf(
                        KiraSliderMarker(0f, "Low"),
                        KiraSliderMarker(1f, "Medium"),
                        KiraSliderMarker(2f, "High"),
                        KiraSliderMarker(3f, "Very High"),
                        KiraSliderMarker(4f, "Extreme"),
                    ),
                    showValue = true,
                    label = "Intensity (Clickable Custom Markers)",
                )
            }
        }

        SectionHeading("Range Slider & Synced Numeric Inputs")
        GalleryExample("RangeSlider, Thumb Labels & KiraRangeSlider with From/To Inputs") {
            var priceRange by remember { mutableStateOf(20f..80f) }
            var budgetRange by remember { mutableStateOf(20f..80f) }
            var inputRange by remember { mutableStateOf(10f..90f) }
            var committed by remember { mutableStateOf(10f..90f) }

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                KiraRangeSlider(
                    value = priceRange,
                    onValueChange = { priceRange = it },
                    showValue = true,
                    label = "Price Range",
                )
                KiraRangeSlider(
                    value = budgetRange,
                    onValueChange = { budgetRange = it },
                    showValue = true,
                    thumbLabels = "Min" to "Max",
                    label = "Budget Range (With Thumb Labels)",
                )
                KiraRangeSlider(
                    value = inputRange,
                    onValueChange = { inputRange = it },
                    stepOptions = listOf(0f, 10f, 40f, 90f, 100f),
                    showInputs = true,
                    label = "Budget (KiraRangeSlider + Numeric Inputs)",
                    onValueCommitted = { committed = it },
                )
                ExampleNote("Committed: ${committed.start.roundToInt()} – ${committed.endInclusive.roundToInt()}")
            }
        }

        SectionHeading("Inverted, Vertical & Custom Tooltips")
        GalleryExample("Inverted Gradient Range, Vertical Slider & Custom Formatting") {
            var invRange by remember { mutableStateOf(20f..80f) }
            var vertVal by remember { mutableFloatStateOf(40f) }
            var priceVal by remember { mutableFloatStateOf(150f) }
            var tempVal by remember { mutableFloatStateOf(22f) }

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                KiraRangeSlider(
                    value = invRange,
                    onValueChange = { invRange = it },
                    inverted = true,
                    tooltipMode = KiraSliderTooltipMode.Always,
                    showValue = true,
                    variant = KiraSliderVariant.Gradient,
                    label = "Inverted Budget",
                )

                KiraSlider(
                    value = priceVal,
                    onValueChange = { priceVal = it },
                    min = 0f,
                    max = 1000f,
                    step = 10f,
                    showValue = true,
                    tooltipMode = KiraSliderTooltipMode.Always,
                    formatValue = { "$${it.roundToInt()}" },
                    label = "Price (Currency Format)",
                )

                KiraSlider(
                    value = tempVal,
                    onValueChange = { tempVal = it },
                    min = 0f,
                    max = 40f,
                    showValue = true,
                    tooltipMode = KiraSliderTooltipMode.Always,
                    formatValue = { "${it.roundToInt()}°C" },
                    renderTooltip = { v ->
                        BasicText(
                            text = "${v.roundToInt()}°C",
                            style = KiraTheme.typography.caption.copy(
                                color = colors.popoverForeground,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                            ),
                        )
                    },
                    label = "Temperature",
                )

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KiraSlider(
                        value = vertVal,
                        onValueChange = { vertVal = it },
                        orientation = KiraSliderOrientation.Vertical,
                        label = "Vertical volume",
                        showValue = true,
                        tooltipMode = KiraSliderTooltipMode.OnDrag,
                    )
                }
            }
        }

        SectionHeading("Validation, Disabled Reason & Stress Cases")
        GalleryExample("Error, Required, Read-Only, Disabled Reason & Narrow 260dp") {
            var cpu by remember { mutableFloatStateOf(80f) }
            var priority by remember { mutableFloatStateOf(50f) }
            var longLabelVal by remember { mutableFloatStateOf(0f) }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                KiraSlider(
                    value = cpu,
                    onValueChange = { cpu = it },
                    error = "Value exceeds recommended limit",
                    label = "CPU Usage",
                    showValue = true,
                )
                KiraSlider(
                    value = priority,
                    onValueChange = { priority = it },
                    required = true,
                    label = "Priority Level",
                    description = "Required field - set a priority",
                )
                KiraSlider(
                    value = 50f,
                    onValueChange = {},
                    readOnly = true,
                    label = "Read-only Value",
                    showValue = true,
                )
                KiraSlider(
                    value = 60f,
                    onValueChange = {},
                    enabled = false,
                    disabledReason = "Unlock the track to adjust its volume.",
                    label = "Locked track volume",
                    showValue = true,
                )
                Column(modifier = Modifier.widthIn(max = 260.dp)) {
                    KiraSlider(
                        value = longLabelVal,
                        onValueChange = { longLabelVal = it },
                        label = "A very long multilingual setting label 音量調整 🌍",
                        showValue = true,
                    )
                }
            }
        }
    }
}
