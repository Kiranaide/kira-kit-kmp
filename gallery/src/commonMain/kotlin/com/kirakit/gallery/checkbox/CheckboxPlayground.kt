package com.kirakit.gallery.checkbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kirakit.components.checkbox.KiraCheckbox
import com.kirakit.components.checkbox.KiraCheckboxCard
import com.kirakit.components.checkbox.KiraCheckboxColor
import com.kirakit.components.checkbox.KiraCheckboxGroup
import com.kirakit.components.checkbox.KiraCheckboxGroupOrientation
import com.kirakit.components.checkbox.KiraCheckboxLabelPlacement
import com.kirakit.components.checkbox.KiraCheckboxOption
import com.kirakit.components.checkbox.KiraCheckboxSize
import com.kirakit.components.checkbox.KiraCheckboxState
import com.kirakit.components.checkbox.KiraCheckboxVariant
import com.kirakit.components.checkbox.KiraCheckboxWithLabel
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CheckboxPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Sizes")
        GalleryExample("With Label & Sizes") {
            var accepted by rememberSaveable { mutableStateOf(false) }
            KiraCheckboxWithLabel(
                checked = accepted,
                onCheckedChange = { accepted = it },
                label = "Accept terms and conditions",
                description = "You agree to our Terms of Service and Privacy Policy.",
            )

            var sm by rememberSaveable { mutableStateOf(true) }
            var def by rememberSaveable { mutableStateOf(true) }
            var lg by rememberSaveable { mutableStateOf(true) }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraCheckboxWithLabel(checked = sm, onCheckedChange = { sm = it }, label = "Small", size = KiraCheckboxSize.Sm)
                KiraCheckboxWithLabel(checked = def, onCheckedChange = { def = it }, label = "Default", size = KiraCheckboxSize.Default)
                KiraCheckboxWithLabel(checked = lg, onCheckedChange = { lg = it }, label = "Large", size = KiraCheckboxSize.Lg)
            }
        }

        SectionHeading("Style Variants & Semantic Colors")
        GalleryExample("Default, Outline, Filled, Soft (Unchecked / Checked / Indeterminate)") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraCheckboxVariant.entries.forEach { variant ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = variant.name,
                            style = KiraTheme.typography.label.copy(color = colors.foreground),
                            modifier = Modifier.weight(1f),
                        )
                        var u by remember { mutableStateOf(false) }
                        KiraCheckbox(checked = u, onCheckedChange = { u = it }, variant = variant)
                        KiraCheckbox(state = KiraCheckboxState.Checked, onClick = {}, variant = variant)
                        KiraCheckbox(state = KiraCheckboxState.Indeterminate, onClick = {}, variant = variant)
                    }
                }
            }
        }

        GalleryExample("Colors & States (Loading, Error, Disabled Reason)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraCheckboxColor.entries.forEach { color ->
                    var c by remember { mutableStateOf(true) }
                    KiraCheckboxWithLabel(
                        checked = c,
                        onCheckedChange = { c = it },
                        label = color.name,
                        color = color,
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraCheckboxWithLabel(
                    checked = true,
                    onCheckedChange = {},
                    label = "Loading",
                    loading = true,
                )
                KiraCheckboxWithLabel(
                    checked = false,
                    onCheckedChange = {},
                    label = "Error",
                    error = true,
                )
                KiraCheckboxWithLabel(
                    checked = true,
                    onCheckedChange = {},
                    label = "Disabled",
                    enabled = false,
                    disabledReason = "Managed by organization policy",
                )
                var startPlaced by rememberSaveable { mutableStateOf(true) }
                KiraCheckboxWithLabel(
                    checked = startPlaced,
                    onCheckedChange = { startPlaced = it },
                    label = "Label on start",
                    labelPlacement = KiraCheckboxLabelPlacement.Start,
                )
            }
        }

        SectionHeading("Checkbox Group")
        GalleryExample("Select All + Live Progress") {
            val options = remember {
                listOf(
                    KiraCheckboxOption("email", "Email notifications", "Receive weekly summaries and alerts."),
                    KiraCheckboxOption("push", "Push notifications", "Real-time mobile updates."),
                    KiraCheckboxOption("sms", "SMS alerts", "Critical security notifications only."),
                    KiraCheckboxOption("legacy", "Fax digest", "Deprecated channel", disabled = true),
                )
            }
            var selected by remember { mutableStateOf(setOf("email", "push")) }

            KiraCheckboxGroup(
                options = options,
                selectedValues = selected,
                onSelectedValuesChange = { selected = it },
                label = "Notification Channels",
                description = "Choose how you'd like to hear from us.",
                showSelectAll = true,
                showProgress = true,
                orientation = KiraCheckboxGroupOrientation.Vertical,
            )
        }

        SectionHeading("Checkbox Cards & Optimistic Commit")
        GalleryExample("Interactive Cards") {
            var analytics by rememberSaveable { mutableStateOf(true) }
            var marketing by rememberSaveable { mutableStateOf(false) }
            var optimisticSync by rememberSaveable { mutableStateOf(false) }
            var commitStatus by remember { mutableStateOf("Tap the optimistic card to simulate a network save.") }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraCheckboxCard(
                    checked = analytics,
                    onCheckedChange = { analytics = it },
                    label = "Usage Analytics",
                    description = "Help improve Kira Kit by sharing anonymous crash diagnostics.",
                    modifier = Modifier.fillMaxWidth(),
                )
                KiraCheckboxCard(
                    checked = marketing,
                    onCheckedChange = { marketing = it },
                    label = "Release Announcements",
                    description = "Get notified when major KMP and Compose releases ship.",
                    showIndicator = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                KiraCheckboxCard(
                    checked = optimisticSync,
                    onCheckedChange = {
                        optimisticSync = it
                        commitStatus = "Saved preference: ${if (it) "Enabled" else "Disabled"}"
                    },
                    label = "Cloud Sync (Optimistic Async)",
                    description = "Flips immediately, awaits async save, and reverts on failure.",
                    onCommit = { next ->
                        commitStatus = "Saving ${if (next) "enabled" else "disabled"}…"
                        delay(700)
                        Result.success(Unit)
                    },
                    onCommitError = {
                        commitStatus = "Save failed — reverted."
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                ExampleNote(commitStatus)
            }
        }
    }
}
