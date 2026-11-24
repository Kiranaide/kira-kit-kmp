package com.kirakit.gallery.switch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.switch.KiraSwitch
import com.kirakit.components.switch.KiraSwitchColor
import com.kirakit.components.switch.KiraSwitchGroup
import com.kirakit.components.switch.KiraSwitchLabelPosition
import com.kirakit.components.switch.KiraSwitchSize
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SwitchPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Sizes")
        GalleryExample("Basic States") {
            var unchecked by remember { mutableStateOf(false) }
            var checked by remember { mutableStateOf(true) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraSwitch(checked = unchecked, onCheckedChange = { unchecked = it }, label = "Unchecked")
                KiraSwitch(checked = checked, onCheckedChange = { checked = it }, label = "Checked")
                KiraSwitch(checked = false, onCheckedChange = {}, enabled = false, label = "Disabled")
            }
        }

        GalleryExample("Sizes (Sm, Default, Lg)") {
            var sm by remember { mutableStateOf(true) }
            var def by remember { mutableStateOf(true) }
            var lg by remember { mutableStateOf(true) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraSwitch(checked = sm, onCheckedChange = { sm = it }, size = KiraSwitchSize.Sm, label = "Small")
                KiraSwitch(checked = def, onCheckedChange = { def = it }, size = KiraSwitchSize.Default, label = "Default")
                KiraSwitch(checked = lg, onCheckedChange = { lg = it }, size = KiraSwitchSize.Lg, label = "Large")
            }
        }

        SectionHeading("Labels, Descriptions & Thumb Icons")
        GalleryExample("Label Positions & Thumb Icons") {
            var notifications by remember { mutableStateOf(true) }
            var darkMode by remember { mutableStateOf(false) }
            var startPos by remember { mutableStateOf(true) }
            var endPos by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraSwitch(
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                    label = "Enable notifications",
                    description = "Receive push notifications on your device",
                )
                KiraSwitch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it },
                    label = "Dark mode",
                    description = "Switch to dark theme",
                )
                KiraSwitch(
                    checked = startPos,
                    onCheckedChange = { startPos = it },
                    label = "Label at start",
                    description = "The label appears before the switch",
                    labelPosition = KiraSwitchLabelPosition.Start,
                )
                KiraSwitch(
                    checked = endPos,
                    onCheckedChange = { endPos = it },
                    label = "Label at end (default)",
                    description = "The label appears after the switch",
                    labelPosition = KiraSwitchLabelPosition.End,
                )
                KiraSwitch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it },
                    size = KiraSwitchSize.Lg,
                    label = "Theme (Thumb Icons)",
                    thumbIcon = { MoonGlyph(it) },
                    thumbIconChecked = { SunGlyph(it) },
                )
            }
        }

        SectionHeading("States, Colors & Disabled Reason")
        GalleryExample("Loading, Error, Semantic Colors & Read-Only") {
            var errChecked by remember { mutableStateOf(true) }
            var cSuccess by remember { mutableStateOf(true) }
            var cDestructive by remember { mutableStateOf(true) }
            var cWarning by remember { mutableStateOf(true) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraSwitch(checked = false, onCheckedChange = {}, loading = true, label = "Loading (off)")
                    KiraSwitch(checked = true, onCheckedChange = {}, loading = true, label = "Loading (on)")
                    KiraSwitch(checked = true, onCheckedChange = {}, readOnly = true, label = "Read-only")
                }

                KiraSwitch(
                    checked = errChecked,
                    onCheckedChange = { errChecked = it },
                    error = true,
                    label = "Invalid styling",
                )
                KiraSwitch(
                    checked = false,
                    onCheckedChange = {},
                    error = true,
                    errorMessage = "Confirm your email address first",
                    label = "With error message",
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraSwitch(checked = cSuccess, onCheckedChange = { cSuccess = it }, color = KiraSwitchColor.Success, label = "Success")
                    KiraSwitch(checked = cDestructive, onCheckedChange = { cDestructive = it }, color = KiraSwitchColor.Destructive, label = "Destructive")
                    KiraSwitch(checked = cWarning, onCheckedChange = { cWarning = it }, color = KiraSwitchColor.Warning, label = "Warning")
                }

                KiraSwitch(
                    checked = false,
                    onCheckedChange = {},
                    enabled = false,
                    disabledReason = "Available on the Team plan and above",
                    label = "SSO enforcement",
                    description = "Hover, focus, or long-press to see why it's unavailable",
                )
            }
        }

        SectionHeading("Optimistic Commit, Group & Settings Panel")
        GalleryExample("Optimistic Async Save & Error Rollback") {
            var autoSave by remember { mutableStateOf(true) }
            var maintenance by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraSwitch(
                    checked = autoSave,
                    onCheckedChange = { autoSave = it },
                    optimistic = true,
                    label = "Auto-save documents",
                    description = "Flips instantly; the save request runs in the background",
                    commitErrorTitle = "Couldn't save auto-save",
                    onCommit = {
                        delay(1_000L)
                        Result.success(Unit)
                    },
                )
                KiraSwitch(
                    checked = maintenance,
                    onCheckedChange = { maintenance = it },
                    optimistic = true,
                    label = "Maintenance mode",
                    description = "This illustrative save fails after 1s; the change reverts with a toast",
                    commitErrorTitle = "Couldn't enable maintenance mode",
                    onCommit = {
                        delay(1_000L)
                        Result.failure(IllegalStateException("Maintenance mode is locked"))
                    },
                )
            }
        }

        GalleryExample("SwitchGroup & Settings Panel") {
            var email by remember { mutableStateOf(true) }
            var push by remember { mutableStateOf(false) }
            var sms by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                KiraSwitchGroup(label = "Notification settings") {
                    KiraSwitch(checked = email, onCheckedChange = { email = it }, label = "Email notifications")
                    KiraSwitch(checked = push, onCheckedChange = { push = it }, label = "Push notifications")
                    KiraSwitch(checked = sms, onCheckedChange = { sms = it }, label = "SMS notifications")
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KiraTheme.radius.shapeLg)
                        .border(1.dp, colors.border, KiraTheme.radius.shapeLg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    var wifi by remember { mutableStateOf(true) }
                    var sound by remember { mutableStateOf(false) }
                    SettingsRow("WiFi", "Connect to available networks", wifi) { wifi = it }
                    SettingsRow("Sound", "Play sounds for notifications", sound) { sound = it }
                }
                ExampleNote("All switches enforce 44dp touch hit areas while keeping compact visual tracks.")
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val colors = KiraTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KiraSwitch(
                checked = checked,
                onCheckedChange = onChange,
                size = KiraSwitchSize.Lg,
                contentDescription = title,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(title, style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium))
                BasicText(subtitle, style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground))
            }
        }
        BasicText(
            text = if (checked) "On" else "Off",
            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground, fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun SunGlyph(color: Color) {
    Canvas(Modifier.size(12.dp)) {
        val sw = 1.4.dp.toPx()
        drawCircle(color, radius = size.minDimension * 0.24f, style = Stroke(width = sw))
        drawLine(color, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height * 0.16f), sw, StrokeCap.Round)
        drawLine(color, Offset(size.width / 2f, size.height * 0.84f), Offset(size.width / 2f, size.height), sw, StrokeCap.Round)
        drawLine(color, Offset(0f, size.height / 2f), Offset(size.width * 0.16f, size.height / 2f), sw, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.84f, size.height / 2f), Offset(size.width, size.height / 2f), sw, StrokeCap.Round)
    }
}

@Composable
private fun MoonGlyph(color: Color) {
    Canvas(Modifier.size(12.dp)) {
        val sw = 1.4.dp.toPx()
        drawArc(
            color = color,
            startAngle = 40f,
            sweepAngle = 240f,
            useCenter = false,
            style = Stroke(width = sw, cap = StrokeCap.Round),
        )
    }
}
