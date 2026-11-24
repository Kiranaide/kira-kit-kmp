package com.kirakit.gallery.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.datepicker.KiraDate
import com.kirakit.components.datepicker.KiraDateFormat
import com.kirakit.components.datepicker.KiraDatePicker
import com.kirakit.components.datepicker.KiraDatePickerApplyMode
import com.kirakit.components.datepicker.KiraDatePickerSize
import com.kirakit.components.datepicker.KiraDatePickerVariant
import com.kirakit.components.datepicker.KiraDateRange
import com.kirakit.components.datepicker.KiraDateRangePicker
import com.kirakit.components.datepicker.KiraDateTime
import com.kirakit.components.datepicker.KiraDateTimePicker
import com.kirakit.components.datepicker.formatKiraDate
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.timepicker.KiraTime
import com.kirakit.components.timepicker.KiraTimeGranularity
import com.kirakit.components.timepicker.formatKiraTime
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.gallery.galleryHeading
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

private val DemoToday = KiraDate(2026, 11, 24)

internal fun LazyListScope.datePickerPlaygroundItems() {
    galleryHeading("Selection Modes")
    galleryItem("dp-single") { SingleDateExample(DemoToday) }
    galleryItem("dp-range") { DateRangeExample(DemoToday) }
    galleryItem("dp-datetime") { DateTimeExample(DemoToday) }
    galleryItem("dp-datetime-form") { DateTimeFormExample(DemoToday) }

    galleryHeading("Form, Presets & States")
    galleryItem("dp-form") { FormIntegrationExample(DemoToday) }
    galleryItem("dp-presets-disabled") { WithoutPresetsAndDisabledExample(DemoToday) }

    galleryHeading("Constraints & Apply Mode")
    galleryItem("dp-constraints") { DateConstraintsExample(DemoToday) }
    galleryItem("dp-apply") { ApplyModeExample(DemoToday) }

    galleryHeading("Sizes & Style Variants")
    galleryItem("dp-sizes") { SizeVariantsExample(DemoToday) }
    galleryItem("dp-styles") { StyleVariantsExample(DemoToday) }
}

@Composable
internal fun DatePickerPlayground() {
    val gap = KiraTheme.spacing.md
    val today = remember { KiraDate(2026, 11, 24) }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Selection Modes")
        SingleDateExample(today)
        DateRangeExample(today)
        DateTimeExample(today)
        DateTimeFormExample(today)

        SectionHeading("Form, Presets & States")
        FormIntegrationExample(today)
        WithoutPresetsAndDisabledExample(today)

        SectionHeading("Constraints & Apply Mode")
        DateConstraintsExample(today)
        ApplyModeExample(today)

        SectionHeading("Sizes & Style Variants")
        SizeVariantsExample(today)
        StyleVariantsExample(today)
    }
}

@Composable
private fun SingleDateExample(today: KiraDate) {
    var date by remember { mutableStateOf<KiraDate?>(null) }

    GalleryExample("Single Date Selection") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraDatePicker(
                value = date,
                onValueChange = { date = it },
                today = today,
                placeholder = "Pick a date",
            )
            date?.let { ExampleNote("Selected: ${formatKiraDate(it, KiraDateFormat.Long)}") }
        }
    }
}

@Composable
private fun DateRangeExample(today: KiraDate) {
    var range by remember { mutableStateOf<KiraDateRange?>(null) }

    GalleryExample("Date Range Selection") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraDateRangePicker(
                value = range,
                onValueChange = { range = it },
                today = today,
                placeholder = "Select date range",
            )
            range?.from?.let { from ->
                val toText = range?.to?.let { " — To: ${formatKiraDate(it, KiraDateFormat.Long)}" } ?: ""
                ExampleNote("From: ${formatKiraDate(from, KiraDateFormat.Long)}$toText")
            }
        }
    }
}

@Composable
private fun DateTimeExample(today: KiraDate) {
    var dateTime by remember { mutableStateOf<KiraDateTime?>(null) }

    GalleryExample("Date & Time Selection") {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraDateTimePicker(
                value = dateTime,
                onValueChange = { dateTime = it },
                today = today,
                placeholder = "Select date & time",
                ariaLabel = "Starts at",
            )
            ExampleNote(
                dateTime?.let {
                    "Selected: ${formatKiraDate(it.date, KiraDateFormat.Long)} ${formatKiraTime(it.time)} " +
                        "(hours=${it.time.hour}, minutes=${it.time.minute})"
                } ?: "Day click sets the date part while keeping the popover open so you can dial in the time.",
            )
        }
    }
}

@Composable
private fun DateTimeFormExample(today: KiraDate) {
    var startsAt by remember {
        mutableStateOf<KiraDateTime?>(KiraDateTime(KiraDate(2026, 10, 26), KiraTime(14, 35, 0)))
    }

    GalleryExample("Datetime Form Integration") {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraLabel("Starts at")
            KiraDateTimePicker(
                value = startsAt,
                onValueChange = { startsAt = it },
                today = today,
                granularity = KiraTimeGranularity.Second,
                ariaLabel = "Starts at",
            )
            ExampleNote("Serialized ISO payload (starts_at): ${startsAt?.toIsoString() ?: "(empty)"}")
        }
    }
}

@Composable
private fun FormIntegrationExample(today: KiraDate) {
    val colors = KiraTheme.colors
    var dob by remember { mutableStateOf<KiraDate?>(null) }
    var stay by remember { mutableStateOf<KiraDateRange?>(null) }
    var submittedPayload by remember { mutableStateOf<String?>(null) }

    GalleryExample("Typed Entry & Form Integration") {
        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Date of birth (editable YYYY-MM-DD)")
                KiraDatePicker(
                    value = dob,
                    onValueChange = { dob = it },
                    today = today,
                    editable = true,
                    maxDate = today,
                    showPresets = false,
                    displayFormat = KiraDateFormat.Iso,
                    placeholder = "YYYY-MM-DD",
                    ariaLabel = "Date of birth",
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Stay (range)")
                KiraDateRangePicker(
                    value = stay,
                    onValueChange = { stay = it },
                    today = today,
                    placeholder = "Check-in — check-out",
                    ariaLabel = "Stay",
                )
            }
            KiraButton(
                text = "Submit",
                onClick = {
                    submittedPayload = buildString {
                        appendLine("{")
                        appendLine("  \"dob\": \"${dob?.toIsoString() ?: ""}\",")
                        appendLine("  \"stay_from\": \"${stay?.from?.toIsoString() ?: ""}\",")
                        appendLine("  \"stay_to\": \"${stay?.to?.toIsoString() ?: ""}\"")
                        append("}")
                    }
                },
            )
            if (submittedPayload != null) {
                BasicText(
                    text = submittedPayload!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KiraTheme.radius.shapeMd)
                        .background(colors.muted.copy(alpha = 0.45f))
                        .border(1.dp, colors.border, KiraTheme.radius.shapeMd)
                        .padding(10.dp),
                    style = KiraTheme.typography.code.copy(color = colors.foreground),
                )
            }
        }
    }
}

@Composable
private fun WithoutPresetsAndDisabledExample(today: KiraDate) {
    var plainDate by remember { mutableStateOf<KiraDate?>(null) }

    GalleryExample("Without Presets & Disabled") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Without Presets")
                KiraDatePicker(
                    value = plainDate,
                    onValueChange = { plainDate = it },
                    today = today,
                    showPresets = false,
                    placeholder = "No presets",
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Disabled Picker")
                KiraDatePicker(
                    value = null,
                    onValueChange = {},
                    today = today,
                    enabled = false,
                    placeholder = "Disabled picker",
                )
            }
        }
    }
}

@Composable
private fun DateConstraintsExample(today: KiraDate) {
    var future by remember { mutableStateOf<KiraDate?>(null) }
    var past by remember { mutableStateOf<KiraDate?>(null) }
    var weekWindow by remember { mutableStateOf<KiraDate?>(null) }

    GalleryExample("Date Constraints") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraDatePicker(
                value = future,
                onValueChange = { future = it },
                today = today,
                minDate = today,
                placeholder = "Select future date",
            )
            KiraDatePicker(
                value = past,
                onValueChange = { past = it },
                today = today,
                maxDate = today,
                placeholder = "Select past date",
            )
            KiraDatePicker(
                value = weekWindow,
                onValueChange = { weekWindow = it },
                today = today,
                minDate = today,
                maxDate = today.plusDays(7),
                placeholder = "Select within 7 days",
            )
        }
    }
}

@Composable
private fun ApplyModeExample(today: KiraDate) {
    var immediate by remember { mutableStateOf<KiraDateRange?>(null) }
    var confirm by remember { mutableStateOf<KiraDateRange?>(null) }

    GalleryExample("Apply Mode Variants") {
        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Immediate (default) — applies each click")
                KiraDateRangePicker(
                    value = immediate,
                    onValueChange = { immediate = it },
                    today = today,
                    applyMode = KiraDatePickerApplyMode.Immediate,
                    placeholder = "Immediate apply",
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Confirm — Apply / Cancel buttons")
                KiraDateRangePicker(
                    value = confirm,
                    onValueChange = { confirm = it },
                    today = today,
                    applyMode = KiraDatePickerApplyMode.Confirm,
                    placeholder = "Confirm apply",
                )
            }
        }
    }
}

@Composable
private fun SizeVariantsExample(today: KiraDate) {
    var sm by remember { mutableStateOf<KiraDate?>(null) }
    var def by remember { mutableStateOf<KiraDate?>(null) }
    var lg by remember { mutableStateOf<KiraDate?>(null) }

    GalleryExample("Size Variants") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraDatePicker(value = sm, onValueChange = { sm = it }, today = today, size = KiraDatePickerSize.Sm, placeholder = "Small date picker")
            KiraDatePicker(value = def, onValueChange = { def = it }, today = today, size = KiraDatePickerSize.Default, placeholder = "Default date picker")
            KiraDatePicker(value = lg, onValueChange = { lg = it }, today = today, size = KiraDatePickerSize.Lg, placeholder = "Large date picker")
        }
    }
}

@Composable
private fun StyleVariantsExample(today: KiraDate) {
    var def by remember { mutableStateOf<KiraDate?>(null) }
    var outline by remember { mutableStateOf<KiraDate?>(null) }
    var ghost by remember { mutableStateOf<KiraDate?>(null) }

    GalleryExample("Style Variants") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraDatePicker(value = def, onValueChange = { def = it }, today = today, variant = KiraDatePickerVariant.Default, placeholder = "Default variant")
            KiraDatePicker(value = outline, onValueChange = { outline = it }, today = today, variant = KiraDatePickerVariant.Outline, placeholder = "Outline variant")
            KiraDatePicker(value = ghost, onValueChange = { ghost = it }, today = today, variant = KiraDatePickerVariant.Ghost, placeholder = "Ghost variant")
        }
    }
}
