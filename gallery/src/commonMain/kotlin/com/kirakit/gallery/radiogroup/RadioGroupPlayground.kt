package com.kirakit.gallery.radiogroup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.checkbox.KiraCheckbox
import com.kirakit.components.radiogroup.KiraRadioButtonGroup
import com.kirakit.components.radiogroup.KiraRadioColor
import com.kirakit.components.radiogroup.KiraRadioGroup
import com.kirakit.components.radiogroup.KiraRadioGroupLayout
import com.kirakit.components.radiogroup.KiraRadioGroupOrientation
import com.kirakit.components.radiogroup.KiraRadioItem
import com.kirakit.components.radiogroup.KiraRadioOption
import com.kirakit.components.radiogroup.KiraRadioSize
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

internal fun LazyListScope.radioGroupPlaygroundItems() {
    galleryItem("rg-basic") { BasicRadioDemo() }
    galleryItem("rg-labels") { LabelsRadioDemo() }
    galleryItem("rg-variants") { VariantsRadioDemo() }
    galleryItem("rg-cards") { CardsRadioDemo() }
    galleryItem("rg-segmented") { SegmentedRadioDemo() }
    galleryItem("rg-motion") { MotionRadioDemo() }
    galleryItem("rg-kira") { KiraRadioDemo() }
    galleryItem("rg-form") { FormRadioDemo() }
    galleryItem("rg-states") { StatesRadioDemo() }
    galleryItem("rg-stress") { StressRadioDemo() }
}

@Composable
internal fun RadioGroupPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicRadioDemo()
        LabelsRadioDemo()
        VariantsRadioDemo()
        CardsRadioDemo()
        SegmentedRadioDemo()
        MotionRadioDemo()
        KiraRadioDemo()
        FormRadioDemo()
        StatesRadioDemo()
        StressRadioDemo()
    }
}

@Composable
private fun BasicRadioDemo() {
    val colors = KiraTheme.colors
    var selected by remember { mutableStateOf("daily") }

    GalleryExample("Basic") {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BasicText(
                text = "Notification schedule",
                style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium),
            )
            listOf(
                "daily" to "Daily summary",
                "weekly" to "Weekly summary",
                "off" to "Only when mentioned",
            ).forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KiraRadioItem(
                        selected = selected == value,
                        onClick = { selected = value },
                        contentDescription = label,
                    )
                    BasicText(label, style = KiraTheme.typography.body.copy(color = colors.foreground))
                }
            }
        }
    }
}

@Composable
private fun LabelsRadioDemo() {
    var selected by remember { mutableStateOf("review") }

    GalleryExample("Labels") {
        KiraRadioGroup(
            selectedValue = selected,
            onValueChange = { selected = it },
            options = listOf(
                KiraRadioOption("review", "Requests for review", "Get a message when someone needs your feedback."),
                KiraRadioOption("all", "All activity", "Include comments and status changes."),
                KiraRadioOption(
                    value = "sms",
                    label = "SMS notifications",
                    description = "Receive urgent requests on your phone.",
                    disabled = true,
                    disabledReason = "Add a verified phone number in settings.",
                ),
            ),
        )
    }
}

@Composable
private fun VariantsRadioDemo() {
    var channel by remember { mutableStateOf("email") }

    GalleryExample("Variants (Sizes, Semantic Colors & Horizontal)") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ExampleNote("Sizes")
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(
                    KiraRadioSize.Sm to "Small",
                    KiraRadioSize.Default to "Default",
                    KiraRadioSize.Lg to "Large",
                ).forEach { (sz, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        KiraRadioItem(selected = true, onClick = {}, size = sz, contentDescription = label)
                        BasicText(label, style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.foreground))
                    }
                }
            }

            ExampleNote("Semantic colors")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(
                    KiraRadioColor.Default to "Primary",
                    KiraRadioColor.Success to "Success",
                    KiraRadioColor.Warning to "Warning",
                    KiraRadioColor.Destructive to "Destructive",
                ).forEach { (tone, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        KiraRadioItem(selected = true, onClick = {}, color = tone, contentDescription = label)
                        BasicText(label, style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.foreground))
                    }
                }
            }

            KiraRadioGroup(
                label = "Horizontal choices wrap",
                orientation = KiraRadioGroupOrientation.Horizontal,
                selectedValue = channel,
                onValueChange = { channel = it },
                options = listOf(
                    KiraRadioOption("email", "Email"),
                    KiraRadioOption("inbox", "Workspace inbox"),
                    KiraRadioOption("desktop", "Desktop notifications"),
                ),
            )
        }
    }
}

@Composable
private fun CardsRadioDemo() {
    var format by remember { mutableStateOf("pdf") }

    GalleryExample("Cards") {
        KiraRadioGroup(
            label = "Report format",
            layout = KiraRadioGroupLayout.Cards,
            selectedValue = format,
            onValueChange = { format = it },
            options = listOf(
                KiraRadioOption(
                    value = "pdf",
                    label = "PDF report",
                    description = "Share a readable snapshot with your team.",
                    metadata = "Recommended for review",
                    icon = { c -> RadioCardDocIcon(c) },
                ),
                KiraRadioOption(
                    value = "csv",
                    label = "CSV spreadsheet",
                    description = "Work with the data in a spreadsheet.",
                    metadata = "All rows included",
                    icon = { c -> RadioCardTableIcon(c) },
                ),
                KiraRadioOption(
                    value = "json",
                    label = "JSON data",
                    description = "Keep the structured values for another tool.",
                    icon = { c -> RadioCardCodeIcon(c) },
                ),
            ),
        )
    }
}

@Composable
private fun SegmentedRadioDemo() {
    var frequency by remember { mutableStateOf("weekly") }
    var length by remember { mutableStateOf("concise") }
    var workflow by remember { mutableStateOf("draft") }

    GalleryExample("Segmented") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ExampleNote("Send a summary (Large)")
            KiraRadioButtonGroup(
                size = KiraRadioSize.Lg,
                selectedValue = frequency,
                onValueChange = { frequency = it },
                options = listOf(
                    KiraRadioOption("daily", "Daily"),
                    KiraRadioOption("weekly", "Weekly"),
                    KiraRadioOption("monthly", "Monthly"),
                ),
            )
            ExampleNote("Current preference: $frequency.")

            ExampleNote("Small group; item sizes are inherited")
            KiraRadioButtonGroup(
                size = KiraRadioSize.Sm,
                selectedValue = length,
                onValueChange = { length = it },
                options = listOf(
                    KiraRadioOption("concise", "Concise"),
                    KiraRadioOption("detailed", "Detailed"),
                ),
            )

            KiraRadioGroup(
                label = "Review workflow",
                description = "Details stay below the choices as you switch modes.",
                layout = KiraRadioGroupLayout.Segmented,
                selectedValue = workflow,
                onValueChange = { workflow = it },
                options = listOf(
                    KiraRadioOption("suggest", "Suggest", "Prepare an outline before making changes."),
                    KiraRadioOption("draft", "Draft", "Prepare a draft for your feedback."),
                    KiraRadioOption("background", "Background", disabled = true, disabledReason = "Unavailable in local preview."),
                ),
            )
        }
    }
}

@Composable
private fun MotionRadioDemo() {
    var mode by remember { mutableStateOf("review") }

    GalleryExample("Motion (Synchronized Across Layouts)") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(
                KiraRadioGroupLayout.List to "Labeled choices",
                KiraRadioGroupLayout.Cards to "Choice cards",
                KiraRadioGroupLayout.Segmented to "Segments",
            ).forEach { (layout, heading) ->
                KiraRadioGroup(
                    label = heading,
                    layout = layout,
                    selectedValue = mode,
                    onValueChange = { mode = it },
                    options = listOf(
                        KiraRadioOption(
                            value = "notes",
                            label = if (layout == KiraRadioGroupLayout.Segmented) "Suggest" else "Suggest an approach",
                            description = "Prepare notes before drafting.",
                        ),
                        KiraRadioOption(
                            value = "review",
                            label = if (layout == KiraRadioGroupLayout.Segmented) "Draft" else "Draft for review",
                            description = "Prepare a draft for your feedback.",
                        ),
                    ),
                )
            }
            ExampleNote("All three layouts share the selected value. Dots settle with bouncy spring physics; the segmented pill glides smoothly.")
        }
    }
}

@Composable
private fun KiraRadioDemo() {
    var workflow by remember { mutableStateOf("review") }
    var includeChecklist by remember { mutableStateOf(true) }

    GalleryExample("Kira (Cards + Interactive Details)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KiraRadioGroup(
                label = "Prepare a draft",
                description = "Choose how you want to review the next draft.",
                layout = KiraRadioGroupLayout.Cards,
                selectedValue = workflow,
                onValueChange = { workflow = it },
                options = listOf(
                    KiraRadioOption(
                        value = "notes",
                        label = "Suggest an approach",
                        description = "Prepare notes before drafting.",
                        metadata = "No draft changes",
                        details = { ExampleNote("You receive an outline to review first.") },
                    ),
                    KiraRadioOption(
                        value = "review",
                        label = "Draft for review",
                        description = "Prepare a draft and wait for your feedback.",
                        metadata = "Recommended",
                        details = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                KiraCheckbox(
                                    checked = includeChecklist,
                                    onCheckedChange = { includeChecklist = it },
                                )
                                BasicText(
                                    text = "Include a review checklist",
                                    style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.foreground),
                                )
                            }
                        },
                    ),
                    KiraRadioOption(
                        value = "background",
                        label = "Queue a background draft",
                        description = "Continue preparing while you work on other tasks.",
                        disabled = true,
                        disabledReason = "Background jobs are not enabled in this workspace.",
                    ),
                ),
            )
            ExampleNote("Local preference preview. This demo does not create drafts or background jobs.")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormRadioDemo() {
    var selected by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    GalleryExample("Form Validation & Reset") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraRadioGroup(
                label = "Review reminder",
                description = "Choose one schedule before saving.",
                selectedValue = selected,
                onValueChange = {
                    selected = it
                    showError = false
                },
                isError = showError && selected == null,
                errorMessage = "Choose a reminder schedule, then save again.",
                options = listOf(
                    KiraRadioOption("daily", "Every day", "A daily reminder for open reviews."),
                    KiraRadioOption("weekly", "Every week", "One weekly summary of open reviews."),
                ),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = "Save reminder",
                    size = KiraButtonSize.Sm,
                    onClick = {
                        if (selected == null) {
                            showError = true
                        } else {
                            saved = selected.orEmpty()
                            showError = false
                        }
                    },
                )
                KiraButton(
                    text = "Reset",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        selected = null
                        saved = ""
                        showError = false
                    },
                )
            }
            ExampleNote(
                if (saved.isNotEmpty()) "Reminder saved locally: $saved."
                else "This form saves only this demo's local preference.",
            )
        }
    }
}

@Composable
private fun StatesRadioDemo() {
    var state by remember { mutableStateOf("ready") }
    var destination by remember { mutableStateOf("inbox") }

    GalleryExample("States (Ready, Disabled, Read-only, Loading, Empty, Invalid)") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            KiraRadioButtonGroup(
                size = KiraRadioSize.Sm,
                selectedValue = state,
                onValueChange = { state = it },
                options = listOf("ready", "disabled", "read-only", "loading", "empty", "invalid").map {
                    KiraRadioOption(it, it)
                },
            )
            KiraRadioGroup(
                label = "Review destination",
                description = when (state) {
                    "read-only" -> "Your workspace administrator has fixed this preference."
                    "disabled" -> "You need access to edit this preference."
                    else -> "Choose where the review request is delivered."
                },
                layout = KiraRadioGroupLayout.Cards,
                selectedValue = destination,
                onValueChange = { destination = it },
                readOnly = state == "read-only",
                enabled = state != "disabled",
                loading = state == "loading",
                loadingLabel = "Loading destinations…",
                isError = state == "invalid",
                errorMessage = "The saved destination is unavailable. Choose a different destination.",
                emptyMessage = "No review destinations are connected. Add one in workspace settings.",
                options = if (state == "loading" || state == "empty") {
                    emptyList()
                } else {
                    listOf(
                        KiraRadioOption("inbox", "Workspace inbox", "Keep requests with your team's work."),
                        KiraRadioOption("email", "Email", "Deliver a request to your account email."),
                    )
                },
            )
        }
    }
}

@Composable
private fun StressRadioDemo() {
    var mode by remember { mutableStateOf("demo") }
    var selected by remember { mutableStateOf("primary") }
    val worst = mode == "worst"

    GalleryExample("Stress (Long Wrapping, Email, Thai & Narrow Container)") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KiraRadioButtonGroup(
                size = KiraRadioSize.Sm,
                selectedValue = mode,
                onValueChange = { mode = it },
                options = listOf(
                    KiraRadioOption("demo", "Demo data"),
                    KiraRadioOption("worst", "Worst case"),
                ),
            )
            KiraRadioGroup(
                label = "Review workspace",
                layout = KiraRadioGroupLayout.Cards,
                selectedValue = selected,
                onValueChange = { selected = it },
                options = listOf(
                    KiraRadioOption(
                        value = "primary",
                        label = if (worst) "Northwind international customer support and accessibility research workspace" else "Northwind support",
                        description = if (worst) "research-and-accessibility-reviews@northwind-international.example.com" else "Weekly customer support reviews",
                        metadata = if (worst) "1,234,567 open records" else "24 open records",
                    ),
                    KiraRadioOption(
                        value = "thai",
                        label = if (worst) "พื้นที่ทำงานสำหรับการตรวจสอบเอกสารและการเข้าถึงของทีมวิจัย" else "Research",
                        description = if (worst) "ตรวจสอบรายละเอียดก่อนส่งคำขอและเลือกพื้นที่ทำงานที่เหมาะสม" else null,
                    ),
                    KiraRadioOption(
                        value = "short",
                        label = if (worst) "Å" else "Personal",
                    ),
                ),
            )
        }
    }
}

@Composable
private fun RadioCardDocIcon(color: Color) {
    Canvas(Modifier.size(18.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(color, Offset(size.width * 0.2f, size.height * 0.1f), Size(size.width * 0.6f, size.height * 0.8f), CornerRadius(2.dp.toPx()), style = stroke)
        drawLine(color, Offset(size.width * 0.34f, size.height * 0.42f), Offset(size.width * 0.66f, size.height * 0.42f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.34f, size.height * 0.62f), Offset(size.width * 0.66f, size.height * 0.62f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun RadioCardTableIcon(color: Color) {
    Canvas(Modifier.size(18.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(color, Offset(size.width * 0.12f, size.height * 0.18f), Size(size.width * 0.76f, size.height * 0.64f), CornerRadius(2.dp.toPx()), style = stroke)
        drawLine(color, Offset(size.width * 0.12f, size.height * 0.42f), Offset(size.width * 0.88f, size.height * 0.42f), strokeWidth = 1.3.dp.toPx())
        drawLine(color, Offset(size.width * 0.45f, size.height * 0.18f), Offset(size.width * 0.45f, size.height * 0.82f), strokeWidth = 1.3.dp.toPx())
    }
}

@Composable
private fun RadioCardCodeIcon(color: Color) {
    Canvas(Modifier.size(18.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val left = Path().apply {
            moveTo(size.width * 0.36f, size.height * 0.28f)
            lineTo(size.width * 0.16f, size.height * 0.50f)
            lineTo(size.width * 0.36f, size.height * 0.72f)
        }
        val right = Path().apply {
            moveTo(size.width * 0.64f, size.height * 0.28f)
            lineTo(size.width * 0.84f, size.height * 0.50f)
            lineTo(size.width * 0.64f, size.height * 0.72f)
        }
        drawPath(left, color, style = stroke)
        drawPath(right, color, style = stroke)
    }
}
