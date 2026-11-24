package com.kirakit.gallery.timepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.timepicker.KiraTime
import com.kirakit.components.timepicker.KiraTimeGranularity
import com.kirakit.components.timepicker.KiraTimeHourCycle
import com.kirakit.components.timepicker.KiraTimePicker
import com.kirakit.components.timepicker.KiraTimePickerSize
import com.kirakit.components.timepicker.KiraTimePickerVariant
import com.kirakit.components.timepicker.formatKiraTime
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.gallery.galleryHeading
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing

internal fun LazyListScope.timePickerPlaygroundItems() {
    galleryHeading("Popover & Inline")
    galleryItem("tp-popover") { PopoverLiveExample() }
    galleryItem("tp-inline") { InlinePanelExample() }

    galleryHeading("Panel Variants & Granularity")
    galleryItem("tp-variants") { PanelVariantsExample() }
    galleryItem("tp-granularity") { GranularityExample() }

    galleryHeading("Hour Cycle & Constraints")
    galleryItem("tp-cycle") { HourCycleExample() }
    galleryItem("tp-steps") { StepsAndConstraintsExample() }

    galleryHeading("Sizes & States")
    galleryItem("tp-sizes") { SizesExample() }
    galleryItem("tp-states") { StatesExample() }
}

@Composable
internal fun TimePickerPlayground() {
    val gap = KiraTheme.spacing.md

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Popover & Inline")
        PopoverLiveExample()
        InlinePanelExample()

        SectionHeading("Panel Variants & Granularity")
        PanelVariantsExample()
        GranularityExample()

        SectionHeading("Hour Cycle & Constraints")
        HourCycleExample()
        StepsAndConstraintsExample()

        SectionHeading("Sizes & States")
        SizesExample()
        StatesExample()
    }
}

@Composable
private fun PopoverLiveExample() {
    var time by remember { mutableStateOf<KiraTime?>(null) }

    GalleryExample("Popover Time Picker") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraTimePicker(
                value = time,
                onValueChange = { time = it },
                ariaLabel = "Appointment time",
            )
            time?.let { ExampleNote("Selected: ${formatKiraTime(it)}") }
        }
    }
}

@Composable
private fun InlinePanelExample() {
    var time by remember { mutableStateOf<KiraTime?>(KiraTime(9, 30)) }

    GalleryExample("Inline Panel") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraTimePicker(
                value = time,
                onValueChange = { time = it },
                popover = false,
            )
            ExampleNote("Selected: ${time?.let { formatKiraTime(it) } ?: "none"}")
        }
    }
}

@Composable
private fun PanelVariantsExample() {
    var stepper by remember { mutableStateOf<KiraTime?>(KiraTime(10, 15)) }
    var grid by remember { mutableStateOf<KiraTime?>(KiraTime(12, 30)) }
    var full by remember { mutableStateOf<KiraTime?>(KiraTime(17, 0)) }

    GalleryExample("Panel Variants") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("variant = Stepper — type or step (default)")
                KiraTimePicker(
                    value = stepper,
                    onValueChange = { stepper = it },
                    popover = false,
                    variant = KiraTimePickerVariant.Stepper,
                )
                ExampleNote("Stepper: ${stepper?.let { formatKiraTime(it) } ?: "none"}")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("variant = Grid — tap a preset chip or cell")
                KiraTimePicker(
                    value = grid,
                    onValueChange = { grid = it },
                    popover = false,
                    variant = KiraTimePickerVariant.Grid,
                )
                ExampleNote("Grid: ${grid?.let { formatKiraTime(it) } ?: "none"}")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("variant = Full — stepper stacked above preset grid")
                KiraTimePicker(
                    value = full,
                    onValueChange = { full = it },
                    popover = false,
                    variant = KiraTimePickerVariant.Full,
                )
                ExampleNote("Full: ${full?.let { formatKiraTime(it) } ?: "none"}")
            }
        }
    }
}

@Composable
private fun GranularityExample() {
    var minute by remember { mutableStateOf<KiraTime?>(KiraTime(14, 20)) }
    var second by remember { mutableStateOf<KiraTime?>(KiraTime(14, 20, 45)) }

    GalleryExample("Minute vs Second") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Minute granularity")
                KiraTimePicker(
                    value = minute,
                    onValueChange = { minute = it },
                    granularity = KiraTimeGranularity.Minute,
                )
                ExampleNote("Value: ${minute?.let { formatKiraTime(it) } ?: "none"}")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Second granularity")
                KiraTimePicker(
                    value = second,
                    onValueChange = { second = it },
                    granularity = KiraTimeGranularity.Second,
                )
                ExampleNote("Value: ${second?.let { formatKiraTime(it, KiraTimeGranularity.Second) } ?: "none"}")
            }
        }
    }
}

@Composable
private fun HourCycleExample() {
    var h24 by remember { mutableStateOf<KiraTime?>(KiraTime(15, 45)) }
    var h12 by remember { mutableStateOf<KiraTime?>(KiraTime(15, 45)) }

    GalleryExample("24-hour vs 12-hour") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("24-hour (default)")
                KiraTimePicker(
                    value = h24,
                    onValueChange = { h24 = it },
                    hourCycle = KiraTimeHourCycle.Hour24,
                )
                ExampleNote("Value: ${h24?.let { formatKiraTime(it, hourCycle = KiraTimeHourCycle.Hour24) } ?: "none"}")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("12-hour with AM/PM")
                KiraTimePicker(
                    value = h12,
                    onValueChange = { h12 = it },
                    hourCycle = KiraTimeHourCycle.Hour12,
                )
                ExampleNote("Value: ${h12?.let { formatKiraTime(it, hourCycle = KiraTimeHourCycle.Hour12) } ?: "none"}")
            }
        }
    }
}

@Composable
private fun StepsAndConstraintsExample() {
    var time by remember { mutableStateOf<KiraTime?>(null) }
    val lunchBlocked = remember {
        setOf(KiraTime(12, 0), KiraTime(12, 15), KiraTime(12, 30), KiraTime(12, 45))
    }

    GalleryExample("Steps, Window and Disabled Times") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraTimePicker(
                value = time,
                onValueChange = { time = it },
                variant = KiraTimePickerVariant.Full,
                hourStep = 2,
                minuteStep = 15,
                minTime = KiraTime(9, 0),
                maxTime = KiraTime(18, 0),
                disabledTimes = lunchBlocked,
                ariaLabel = "Meeting slot",
            )
            ExampleNote(
                "Even hours, quarter-hour slots, 09:00–18:00, lunch blocked." +
                    (time?.let { " Selected: ${formatKiraTime(it)}" } ?: ""),
            )
        }
    }
}

@Composable
private fun SizesExample() {
    var sm by remember { mutableStateOf<KiraTime?>(null) }
    var def by remember { mutableStateOf<KiraTime?>(null) }
    var lg by remember { mutableStateOf<KiraTime?>(null) }

    GalleryExample("Size Variants") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraTimePicker(value = sm, onValueChange = { sm = it }, size = KiraTimePickerSize.Sm, placeholder = "Small")
            KiraTimePicker(value = def, onValueChange = { def = it }, size = KiraTimePickerSize.Default, placeholder = "Default")
            KiraTimePicker(value = lg, onValueChange = { lg = it }, size = KiraTimePickerSize.Lg, placeholder = "Large")
        }
    }
}

@Composable
private fun StatesExample() {
    var empty by remember { mutableStateOf<KiraTime?>(null) }
    var filled by remember { mutableStateOf<KiraTime?>(KiraTime(14, 5)) }

    GalleryExample("Component States") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Disabled")
                KiraTimePicker(value = filled, onValueChange = {}, enabled = false)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Invalid")
                KiraTimePicker(value = filled, onValueChange = { filled = it }, invalid = true)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Required", required = true)
                KiraTimePicker(value = empty, onValueChange = { empty = it }, required = true)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Clearable")
                KiraTimePicker(value = filled, onValueChange = { filled = it }, clearable = true)
            }
        }
    }
}
