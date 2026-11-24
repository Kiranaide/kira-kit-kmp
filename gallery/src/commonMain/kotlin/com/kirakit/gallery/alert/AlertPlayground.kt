package com.kirakit.gallery.alert

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.alert.KiraAlert
import com.kirakit.components.alert.KiraAlertAction
import com.kirakit.components.alert.KiraAlertSize
import com.kirakit.components.alert.KiraAlertVariant
import com.kirakit.components.alert.formatAlertRelativeSeconds
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@Composable
internal fun AlertPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic")
        GalleryExample("Basic Alert") {
            KiraAlert(
                title = "Heads up!",
                description = "You can add components to your app using the cli.",
            )
        }

        SectionHeading("Variants")
        GalleryExample("Standard, Soft & Outline (All 14 Variants)") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraAlert(
                    title = "Default",
                    description = "This is a default alert with neutral styling.",
                    variant = KiraAlertVariant.Default,
                )
                KiraAlert(
                    title = "Information",
                    description = "This is an informational alert with helpful context.",
                    variant = KiraAlertVariant.Info,
                )
                KiraAlert(
                    title = "Success",
                    description = "Your action was completed successfully.",
                    variant = KiraAlertVariant.Success,
                )
                KiraAlert(
                    title = "Warning",
                    description = "Please review this information carefully before proceeding.",
                    variant = KiraAlertVariant.Warning,
                )
                KiraAlert(
                    title = "Error",
                    description = "Something went wrong. Please try again later.",
                    variant = KiraAlertVariant.Destructive,
                )

                SubLabel("Soft")
                KiraAlert(
                    title = "Information",
                    description = "Softer, tinted background.",
                    variant = KiraAlertVariant.InfoSoft,
                )
                KiraAlert(
                    title = "Saved",
                    description = "Your changes were saved.",
                    variant = KiraAlertVariant.SuccessSoft,
                )
                KiraAlert(
                    title = "Heads up",
                    description = "Something needs attention.",
                    variant = KiraAlertVariant.WarningSoft,
                )
                KiraAlert(
                    title = "Error",
                    description = "Something went wrong.",
                    variant = KiraAlertVariant.DestructiveSoft,
                )

                SubLabel("Outline")
                KiraAlert(
                    title = "Default",
                    description = "Outlined, no fill.",
                    variant = KiraAlertVariant.OutlineDefault,
                )
                KiraAlert(
                    title = "Information",
                    description = "Outlined informational alert.",
                    variant = KiraAlertVariant.OutlineInfo,
                )
                KiraAlert(
                    title = "Success",
                    description = "Outlined success alert.",
                    variant = KiraAlertVariant.OutlineSuccess,
                )
                KiraAlert(
                    title = "Warning",
                    description = "Outlined warning alert.",
                    variant = KiraAlertVariant.OutlineWarning,
                )
                KiraAlert(
                    title = "Error",
                    description = "Outlined error alert.",
                    variant = KiraAlertVariant.OutlineDestructive,
                )
            }
        }

        SectionHeading("Icons")
        GalleryExample("Automatic, Hidden & Custom Icons") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraAlert(
                    title = "Saved",
                    description = "Icons are chosen automatically from the variant and carry the tone colour.",
                    variant = KiraAlertVariant.Success,
                )
                KiraAlert(
                    title = "No icon",
                    description = "Pass showIcon = false to hide the icon entirely.",
                    variant = KiraAlertVariant.Warning,
                    showIcon = false,
                )
                KiraAlert(
                    title = "Custom icon",
                    description = "Any composable replaces the variant default.",
                    variant = KiraAlertVariant.Default,
                    icon = { SparkleGlyph(colors.primary) },
                )
            }
        }

        SectionHeading("Banner & Left Accent")
        GalleryExample("Edge-to-Edge Banner & Left Accent Border") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SubLabel("Banner (edge-to-edge)")
                KiraAlert(
                    description = "Scheduled maintenance: Sunday 02:00–03:00 UTC.",
                    variant = KiraAlertVariant.Warning,
                    banner = true,
                )
                KiraAlert(
                    description = "New in this release: dark mode and saved filters.",
                    variant = KiraAlertVariant.Info,
                    banner = true,
                )

                SubLabel("Left accent")
                KiraAlert(
                    title = "Information",
                    description = "With a coloured left accent border.",
                    variant = KiraAlertVariant.Info,
                    leftAccent = true,
                )
                KiraAlert(
                    title = "Error",
                    description = "With a coloured left accent border.",
                    variant = KiraAlertVariant.Destructive,
                    leftAccent = true,
                )
            }
        }

        SectionHeading("Dismissible & Undo Dismiss")
        GalleryExample("Closable Alerts with Reset") {
            var resetKey by remember { mutableIntStateOf(0) }
            var status by remember { mutableStateOf("Click X to dismiss an alert.") }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    KiraButton(
                        text = "Reset Alerts",
                        size = KiraButtonSize.Sm,
                        variant = KiraButtonVariant.Outline,
                        onClick = {
                            resetKey++
                            status = "Alerts restored."
                        },
                    )
                }
                key(resetKey) {
                    KiraAlert(
                        title = "Dismissible Alert",
                        description = "Click the X button to dismiss this alert.",
                        variant = KiraAlertVariant.Info,
                        dismissible = true,
                        onClose = { status = "Dismissed informational alert." },
                    )
                    KiraAlert(
                        title = "Another Closable",
                        description = "This one is also dismissible.",
                        variant = KiraAlertVariant.Warning,
                        dismissible = true,
                        onClose = { status = "Dismissed warning alert." },
                    )
                }
                ExampleNote(status)
            }
        }

        GalleryExample("Undo Dismiss") {
            var undoResetKey by remember { mutableIntStateOf(0) }
            var undoStatus by remember {
                mutableStateOf("Dismiss the alert above — the Undo strip stays visible for 5 seconds.")
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                key(undoResetKey) {
                    KiraAlert(
                        title = "3 items archived",
                        description = "Archive completed. You can undo this for a few seconds after dismissing.",
                        variant = KiraAlertVariant.Warning,
                        dismissible = true,
                        undoDismiss = true,
                        undoDurationMillis = 5_000L,
                        onClose = { undoStatus = "Undo window expired; alert permanently dismissed." },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExampleNote(undoStatus)
                    KiraButton(
                        text = "Restore",
                        size = KiraButtonSize.Xs,
                        variant = KiraButtonVariant.Ghost,
                        onClick = {
                            undoResetKey++
                            undoStatus = "Alert restored."
                        },
                    )
                }
            }
        }

        SectionHeading("Action Row & Expandable")
        GalleryExample("Alerts with Actions") {
            var actionNote by remember { mutableStateOf("Tap an action button below.") }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraAlert(
                    title = "New update available",
                    description = "A new version of the application is ready to install.",
                    variant = KiraAlertVariant.Info,
                    actions = listOf(
                        KiraAlertAction("Update", onClick = { actionNote = "Updating…" }),
                        KiraAlertAction("Later", variant = KiraButtonVariant.Outline, onClick = { actionNote = "Snoozed update." }),
                    ),
                )
                KiraAlert(
                    title = "Delete confirmation",
                    description = "Are you sure? This action cannot be undone.",
                    variant = KiraAlertVariant.Destructive,
                    actions = listOf(
                        KiraAlertAction("Delete", variant = KiraButtonVariant.Destructive, onClick = { actionNote = "Deleted." }),
                        KiraAlertAction("Cancel", variant = KiraButtonVariant.Outline, onClick = { actionNote = "Cancelled." }),
                    ),
                )
                ExampleNote(actionNote)
            }
        }

        GalleryExample("Expandable Alerts") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraAlert(
                    title = "Deployment failed",
                    description = "Build step exited with code 1.",
                    variant = KiraAlertVariant.Destructive,
                    expandable = true,
                    expandedContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KiraTheme.radius.shapeSm)
                                .background(colors.muted.copy(alpha = 0.6f))
                                .padding(8.dp),
                        ) {
                            BasicText(
                                text = "Error: missing env var DATABASE_URL\n  at loadConfig (config.ts:42)\n  at bootstrap (index.ts:11)",
                                style = KiraTheme.typography.caption.copy(
                                    color = colors.foreground,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                ),
                            )
                        }
                    },
                )
                KiraAlert(
                    title = "3 items need review",
                    description = "Open the review queue to approve or reject pending changes.",
                    variant = KiraAlertVariant.Info,
                    expandable = true,
                    expandedContent = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText(
                                text = "• #482 — migration on users table (Maria)",
                                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                            )
                            BasicText(
                                text = "• #483 — copy change on pricing page (Sam)",
                                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                            )
                            BasicText(
                                text = "• #487 — dependency bump: react 19.3.1 (Priya)",
                                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                            )
                        }
                    },
                )
            }
        }

        SectionHeading("Copy, Timestamp & All-in-One")
        GalleryExample("Copy & Timestamp") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraAlert(
                    title = "API key rotated",
                    description = "[REDACTED:stripe]",
                    variant = KiraAlertVariant.Warning,
                    copyable = true,
                    timestampLabel = formatAlertRelativeSeconds(120),
                )
                KiraAlert(
                    title = "New sign-in detected",
                    description = "Chrome on macOS · San Francisco, CA",
                    variant = KiraAlertVariant.Info,
                    copyable = true,
                    copyText = "New sign-in: Chrome on macOS, San Francisco",
                    timestampLabel = formatAlertRelativeSeconds(90 * 60),
                )
            }
        }

        GalleryExample("KiraAlert (All Features Composed)") {
            var allInOneKey by remember { mutableIntStateOf(0) }
            var receiptNote by remember { mutableStateOf("Composed: custom icon, actions, copy, timestamp, dismiss + undo.") }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                key(allInOneKey) {
                    KiraAlert(
                        title = "Payment successful",
                        description = "Your payment of $29.99 has been processed. A receipt has been sent to your email address.",
                        variant = KiraAlertVariant.Success,
                        icon = { SparkleGlyph(colors.success) },
                        actions = listOf(
                            KiraAlertAction("View receipt", onClick = { receiptNote = "Viewing receipt #KR-004821." }),
                            KiraAlertAction("Download", variant = KiraButtonVariant.Outline, onClick = { receiptNote = "Downloading PDF receipt…" }),
                        ),
                        copyable = true,
                        copyText = "Receipt #KR-004821",
                        timestampLabel = formatAlertRelativeSeconds(0),
                        dismissible = true,
                        undoDismiss = true,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExampleNote(receiptNote)
                    KiraButton(
                        text = "Reset",
                        size = KiraButtonSize.Xs,
                        variant = KiraButtonVariant.Ghost,
                        onClick = { allInOneKey++ },
                    )
                }
            }
        }

        SectionHeading("Sizes & Stress Cases")
        GalleryExample("Size Tiers (Xs, Sm, Default, Lg)") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraAlertSize.entries.forEach { sz ->
                    KiraAlert(
                        title = "Size: ${sz.name}",
                        description = "Padding, icon size, and typography scale together.",
                        variant = KiraAlertVariant.Info,
                        size = sz,
                    )
                }
            }
        }

        GalleryExample("Stress Cases (Narrow 320dp Column)") {
            Column(
                modifier = Modifier.widthIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KiraAlert(
                    title = "Record-breaking monsoon rainfall disrupts metropolitan transit schedules across the entire coastal region",
                    description = "Transport authorities reported platform flooding at twelve stations during Tuesday evening's commute, with service restored incrementally overnight and residual delays expected through Thursday. Long content wraps instead of overflowing the narrow column.",
                    variant = KiraAlertVariant.Destructive,
                )
                KiraAlert(
                    title = "Empty description",
                    variant = KiraAlertVariant.Success,
                )
                KiraAlert(
                    title = "Everything at once",
                    description = "Copy, timestamp, and dismiss share one narrow column.",
                    variant = KiraAlertVariant.Info,
                    copyable = true,
                    timestampLabel = formatAlertRelativeSeconds(180),
                    dismissible = true,
                    expandable = true,
                    expandedContent = {
                        BasicText(
                            text = "Extra detail stays reachable behind Show more.",
                            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SubLabel(text: String) {
    BasicText(
        text = text,
        modifier = Modifier.padding(top = 6.dp),
        style = KiraTheme.typography.label.copy(
            color = KiraTheme.colors.mutedForeground,
            fontWeight = FontWeight.Medium,
        ),
    )
}

@Composable
private fun SparkleGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.42f
        val inner = r * 0.34f
        val path = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + inner, cy - inner)
            lineTo(cx + r, cy)
            lineTo(cx + inner, cy + inner)
            lineTo(cx, cy + r)
            lineTo(cx - inner, cy + inner)
            lineTo(cx - r, cy)
            lineTo(cx - inner, cy - inner)
            close()
        }
        drawPath(path, color, style = stroke)
        drawCircle(color, radius = 1.dp.toPx(), center = Offset(size.width * 0.82f, size.height * 0.18f))
    }
}
