package com.kirakit.gallery.separator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.separator.KiraSectionSeparator
import com.kirakit.components.separator.KiraSeparator
import com.kirakit.components.separator.KiraSeparatorAction
import com.kirakit.components.separator.KiraSeparatorColor
import com.kirakit.components.separator.KiraSeparatorLabel
import com.kirakit.components.separator.KiraSeparatorLabelAlign
import com.kirakit.components.separator.KiraSeparatorLineStyle
import com.kirakit.components.separator.KiraSeparatorOrientation
import com.kirakit.components.separator.KiraSeparatorSize
import com.kirakit.components.separator.KiraSeparatorSpacing
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

internal fun LazyListScope.separatorPlaygroundItems() {
    galleryItem("sep-basic") { BasicSeparatorDemo() }
    galleryItem("sep-layout") { LayoutSeparatorDemo() }
    galleryItem("sep-styles") { StylesSeparatorDemo() }
    galleryItem("sep-sizes") { SizesSeparatorDemo() }
    galleryItem("sep-labels") { LabelsSeparatorDemo() }
    galleryItem("sep-insets") { InsetsSeparatorDemo() }
    galleryItem("sep-actions") { ActionsSeparatorDemo() }
    galleryItem("sep-disclosure") { DisclosureSeparatorDemo() }
    galleryItem("sep-kira") { KiraSeparatorDemo() }
    galleryItem("sep-keyboard") { KeyboardSeparatorDemo() }
    galleryItem("sep-states") { StatesSeparatorDemo() }
}

@Composable
internal fun SeparatorPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicSeparatorDemo()
        LayoutSeparatorDemo()
        StylesSeparatorDemo()
        SizesSeparatorDemo()
        LabelsSeparatorDemo()
        InsetsSeparatorDemo()
        ActionsSeparatorDemo()
        DisclosureSeparatorDemo()
        KiraSeparatorDemo()
        KeyboardSeparatorDemo()
        StatesSeparatorDemo()
    }
}

@Composable
private fun BasicSeparatorDemo() {
    val colors = KiraTheme.colors
    GalleryExample("Basic") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText("Workspace", style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium))
                ExampleNote("Atlas support team")
            }
            KiraSeparator(contentDescription = "Workspace settings")
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText("Notifications", style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium))
                ExampleNote("Email and in-app updates")
            }
            KiraSeparator(decorative = true)
            ExampleNote("Purely visual rules can be hidden from assistive technology.")
        }
    }
}

@Composable
private fun LayoutSeparatorDemo() {
    val colors = KiraTheme.colors
    GalleryExample("Layout (Vertical Toolbar & Labeled)") {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KiraButton(text = "Overview", variant = KiraButtonVariant.Ghost, size = KiraButtonSize.Sm, onClick = {})
                KiraSeparator(orientation = KiraSeparatorOrientation.Vertical, decorative = true)
                KiraButton(text = "Activity", variant = KiraButtonVariant.Ghost, size = KiraButtonSize.Sm, onClick = {})
                KiraSeparator(orientation = KiraSeparatorOrientation.Vertical, decorative = true)
                KiraButton(text = "Members", variant = KiraButtonVariant.Ghost, size = KiraButtonSize.Sm, onClick = {})
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("Source", style = KiraTheme.typography.bodySmall.copy(color = colors.foreground))
                KiraSeparator(orientation = KiraSeparatorOrientation.Vertical, label = "Then")
                BasicText("Preview", style = KiraTheme.typography.bodySmall.copy(color = colors.foreground))
            }
            KiraSeparator(
                label = "Next section",
                labelAlign = KiraSeparatorLabelAlign.Start,
                lineStyle = KiraSeparatorLineStyle.Dashed,
            )
        }
    }
}

@Composable
private fun StylesSeparatorDemo() {
    GalleryExample("Styles (Line Styles, Semantic Colors & Vertical Gradients)") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            KiraSeparatorLineStyle.entries.forEach { style ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExampleNote(style.name)
                    KiraSeparator(lineStyle = style, color = KiraSeparatorColor.Primary)
                }
            }
            KiraSeparator(decorative = true, spacing = KiraSeparatorSpacing.Sm)
            KiraSeparatorColor.entries.forEach { tone ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExampleNote(tone.name)
                    KiraSeparator(color = tone, size = KiraSeparatorSize.Md)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            ) {
                KiraSeparator(
                    orientation = KiraSeparatorOrientation.Vertical,
                    lineStyle = KiraSeparatorLineStyle.Gradient,
                    color = KiraSeparatorColor.Primary,
                    size = KiraSeparatorSize.Lg,
                    contentDescription = "Vertical primary fade",
                )
                KiraSeparator(
                    orientation = KiraSeparatorOrientation.Vertical,
                    lineStyle = KiraSeparatorLineStyle.Gradient,
                    color = KiraSeparatorColor.Success,
                    size = KiraSeparatorSize.Lg,
                    contentDescription = "Vertical success fade",
                )
            }
        }
    }
}

@Composable
private fun SizesSeparatorDemo() {
    GalleryExample("Sizes") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraSeparatorSize.entries.forEach { sz ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExampleNote("${sz.name} (${sz.thickness.value.toInt()}dp)")
                    KiraSeparator(size = sz)
                }
            }
            KiraSeparator(size = KiraSeparatorSize.Lg, lineStyle = KiraSeparatorLineStyle.Dashed, label = "Thick dashed rule")
            KiraSeparator(size = KiraSeparatorSize.Xl, lineStyle = KiraSeparatorLineStyle.Dotted, label = "Thick dotted rule")
        }
    }
}

@Composable
private fun LabelsSeparatorDemo() {
    GalleryExample("Labels & Icons") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            KiraSeparator(label = "Billing details", labelAlign = KiraSeparatorLabelAlign.Start)
            KiraSeparator(label = "Or continue with email")
            KiraSeparator(label = "End of results", labelAlign = KiraSeparatorLabelAlign.End)
            KiraSeparator(
                label = "Verified changes",
                color = KiraSeparatorColor.Success,
                labelIcon = { c -> ShieldCheckGlyph(c) },
            )
            KiraSeparator(
                label = "Team activity",
                labelIcon = { c -> MessageBubbleGlyph(c) },
                labelIconAtEnd = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KiraSeparator(decorative = true, modifier = Modifier.weight(1f))
                KiraSeparatorLabel(text = "Custom content group")
                KiraSeparator(decorative = true, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InsetsSeparatorDemo() {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    GalleryExample("Insets & Spacing") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(radius.shapeLg)
                    .border(1.dp, colors.border, radius.shapeLg)
                    .padding(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    "Contact details",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium),
                )
                KiraSeparator(inset = KiraSeparatorSpacing.Default)
                BasicText(
                    "lee@example.com",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
                KiraSeparator(inset = KiraSeparatorSpacing.Lg, lineStyle = KiraSeparatorLineStyle.Dashed)
                BasicText(
                    "Related details share an inset.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
            }
            KiraSeparator(label = "Separate sections", spacing = KiraSeparatorSpacing.Lg)
            KiraSectionSeparator(
                label = "Recent activity",
                labelAlign = KiraSeparatorLabelAlign.Start,
                count = 12,
                countLabel = "updates",
                inset = KiraSeparatorSpacing.Sm,
                spacing = KiraSeparatorSpacing.None,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionsSeparatorDemo() {
    var unread by remember { mutableIntStateOf(3) }
    var pending by remember { mutableStateOf(false) }

    GalleryExample("Actions (Pending & Disabled Reason)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExampleNote("Illustrative delivery states; the application owns work and pending state.")
            KiraSectionSeparator(
                label = "New updates",
                count = unread,
                countLabel = "updates",
                color = KiraSeparatorColor.Info,
                spacing = KiraSeparatorSpacing.None,
                action = KiraSeparatorAction(
                    label = "Mark as read",
                    onClick = { unread = 0 },
                    pending = pending,
                    disabled = unread == 0,
                    disabledReason = if (unread == 0) "All updates are already read" else null,
                ),
            )
            ExampleNote(if (unread > 0) "$unread unread updates" else "All updates read")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = if (pending) "Finish pending work" else "Show pending state",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { pending = !pending },
                )
                KiraButton(
                    text = "Reset",
                    variant = KiraButtonVariant.Ghost,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        unread = 3
                        pending = false
                    },
                )
            }
            KiraSectionSeparator(
                label = "Protected activity",
                count = 2,
                countLabel = "updates",
                spacing = KiraSeparatorSpacing.Sm,
                action = KiraSeparatorAction(
                    label = "Archive",
                    onClick = {},
                    disabled = true,
                    disabledReason = "Only project owners can archive this activity",
                ),
            )
        }
    }
}

@Composable
private fun DisclosureSeparatorDemo() {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    var open by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("Confirm the duplicates with Morgan") }

    GalleryExample("Disclosure (Controlled Collapsible)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraSectionSeparator(
                label = "Import diagnostics",
                count = 2,
                countLabel = "checks",
                labelAlign = KiraSeparatorLabelAlign.Start,
                collapsible = true,
                expanded = open,
                onExpandedChange = { open = it },
                description = "Hide the details without losing your review note.",
                spacing = KiraSeparatorSpacing.None,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(radius.shapeLg)
                        .border(1.dp, colors.border, radius.shapeLg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ExampleNote("24 contacts passed validation. Two duplicates were left out.")
                    KiraLabel(text = "Review note")
                    KiraInput(value = note, onValueChange = { note = it })
                }
            }
            KiraButton(
                text = if (open) "Hide from outside" else "Open from outside",
                variant = KiraButtonVariant.Outline,
                size = KiraButtonSize.Sm,
                onClick = { open = !open },
            )
        }
    }
}

@Composable
private fun KiraSeparatorDemo() {
    val updates = remember {
        listOf(
            "Morgan approved the contact mapping.",
            "Lee corrected two duplicate addresses.",
            "The local directory preview is ready.",
        )
    }
    var unread by remember { mutableIntStateOf(updates.size) }

    GalleryExample("Kira (Activity Feed)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ExampleNote("Activity preview for the Atlas contact import.")
            KiraSectionSeparator(
                label = "New activity",
                count = unread,
                countLabel = "updates",
                color = KiraSeparatorColor.Info,
                labelIcon = { c -> MessageBubbleGlyph(c) },
                spacing = KiraSeparatorSpacing.None,
                action = KiraSeparatorAction(
                    label = "Mark as read",
                    onClick = { unread = 0 },
                    disabled = unread == 0,
                    disabledReason = if (unread == 0) "You're caught up" else null,
                ),
            )
            updates.forEach { item ->
                BasicText(item, style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.foreground))
            }
            KiraSectionSeparator(
                label = "Earlier activity",
                count = 2,
                countLabel = "updates",
                collapsible = true,
                spacing = KiraSeparatorSpacing.Sm,
            ) {
                ExampleNote("The export was added to the workspace.")
                ExampleNote("Lee started the contact review.")
            }
        }
    }
}

@Composable
private fun KeyboardSeparatorDemo() {
    var activations by remember { mutableIntStateOf(0) }
    var draft by remember { mutableStateOf("Keep this draft") }

    GalleryExample("Keyboard & Disabled Collapsible") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraSectionSeparator(
                label = "Unread changes",
                count = 3,
                countLabel = "changes",
                spacing = KiraSeparatorSpacing.None,
                action = KiraSeparatorAction(
                    label = "Review changes",
                    onClick = { activations++ },
                ),
            )
            ExampleNote("Review activated $activations times.")
            KiraSectionSeparator(
                label = "Review note",
                collapsible = true,
                spacing = KiraSeparatorSpacing.Sm,
            ) {
                KiraLabel(text = "Note")
                KiraInput(value = draft, onValueChange = { draft = it })
            }
            KiraSectionSeparator(
                label = "Archived details",
                collapsible = true,
                disabled = true,
                disabledReason = "Ask a project owner for access",
                spacing = KiraSeparatorSpacing.None,
            ) {
                ExampleNote("Restricted history")
            }
        }
    }
}

@Composable
private fun StatesSeparatorDemo() {
    GalleryExample("States / Stress (Wrapping, Zero & Large Counts)") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KiraSeparator(label = "Changes from the international customer-directory import need an additional permissions review")
            KiraSeparator(label = "0")
            KiraSectionSeparator(label = "Resolved alerts", count = 0, countLabel = "alerts", spacing = KiraSeparatorSpacing.None)
            KiraSectionSeparator(
                label = "Archived customer-directory permission checks",
                count = 12_480_567,
                countLabel = "checks",
                collapsible = true,
                spacing = KiraSeparatorSpacing.None,
            ) {
                ExampleNote("Large counts and long labels wrap inside the available space.")
            }
            KiraSeparator(label = "", contentDescription = "Empty label becomes a plain rule")
            KiraSectionSeparator(
                label = "Unavailable section",
                collapsible = true,
                disabled = true,
                disabledReason = "The import is still being reviewed",
                spacing = KiraSeparatorSpacing.None,
            ) {
                ExampleNote("Pending review")
            }
        }
    }
}

@Composable
private fun ShieldCheckGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val shield = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.85f, size.height * 0.24f)
            lineTo(size.width * 0.85f, size.height * 0.54f)
            lineTo(size.width * 0.5f, size.height * 0.9f)
            lineTo(size.width * 0.15f, size.height * 0.54f)
            lineTo(size.width * 0.15f, size.height * 0.24f)
            close()
        }
        drawPath(shield, color, style = stroke)
        val check = Path().apply {
            moveTo(size.width * 0.34f, size.height * 0.50f)
            lineTo(size.width * 0.46f, size.height * 0.62f)
            lineTo(size.width * 0.68f, size.height * 0.38f)
        }
        drawPath(check, color, style = stroke)
    }
}

@Composable
private fun MessageBubbleGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.12f, size.height * 0.16f),
            size = Size(size.width * 0.76f, size.height * 0.56f),
            cornerRadius = CornerRadius(3.dp.toPx()),
            style = stroke,
        )
        drawLine(color, Offset(size.width * 0.28f, size.height * 0.72f), Offset(size.width * 0.22f, size.height * 0.88f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}
