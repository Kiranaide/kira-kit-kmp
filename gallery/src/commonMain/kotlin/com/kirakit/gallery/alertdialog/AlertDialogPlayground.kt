package com.kirakit.gallery.alertdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.kirakit.components.alertdialog.KiraAlertDialog
import com.kirakit.components.alertdialog.KiraAlertDialogFooterLayout
import com.kirakit.components.alertdialog.KiraAlertDialogList
import com.kirakit.components.alertdialog.KiraAlertDialogListVariant
import com.kirakit.components.alertdialog.KiraAlertDialogSize
import com.kirakit.components.alertdialog.KiraAlertDialogVariant
import com.kirakit.components.alertdialog.KiraAlertDialogWarning
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AlertDialogPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    var showBasic by remember { mutableStateOf(false) }
    var showDestructive by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }
    var showTypeToConfirm by remember { mutableStateOf(false) }
    var showTimedConfirm by remember { mutableStateOf(false) }
    var showAsyncConfirm by remember { mutableStateOf(false) }
    var showAsyncRecovery by remember { mutableStateOf(false) }
    var recoveryAttempts by remember { mutableIntStateOf(0) }
    var showScrollableFull by remember { mutableStateOf(false) }
    var showCloseButton by remember { mutableStateOf(false) }
    var showReviewRemoval by remember { mutableStateOf(false) }
    var showReviewInfo by remember { mutableStateOf(false) }
    var activeVariant by remember { mutableStateOf<KiraAlertDialogVariant?>(null) }
    var showStacked by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Choose an alert dialog example above.") }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic, Destructive & Custom")
        GalleryExample("Core Confirmation Patterns") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "Show Dialog",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showBasic = true },
                )
                KiraButton(
                    text = "Delete Account",
                    variant = KiraButtonVariant.Destructive,
                    size = KiraButtonSize.Sm,
                    onClick = { showDestructive = true },
                )
                KiraButton(
                    text = "Custom Styled",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showCustom = true },
                )
            }
        }

        SectionHeading("Safety Gates & Async")
        GalleryExample("Type-to-Confirm, Timed Cooldown & Async Confirm") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "Delete project",
                    variant = KiraButtonVariant.Destructive,
                    size = KiraButtonSize.Sm,
                    onClick = { showTypeToConfirm = true },
                )
                KiraButton(
                    text = "Wipe cache",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showTimedConfirm = true },
                )
                KiraButton(
                    text = "Save changes",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showAsyncConfirm = true },
                )
            }
        }

        GalleryExample("Async Recovery (Retry Failed Publish)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExampleNote("Illustrative service: the first request fails; retry succeeds. Attempts: $recoveryAttempts")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraButton(
                        text = "Publish release",
                        size = KiraButtonSize.Sm,
                        onClick = { showAsyncRecovery = true },
                    )
                    if (recoveryAttempts > 0) {
                        KiraButton(
                            text = "Reset Attempts",
                            variant = KiraButtonVariant.Ghost,
                            size = KiraButtonSize.Sm,
                            onClick = { recoveryAttempts = 0 },
                        )
                    }
                }
            }
        }

        SectionHeading("Scrollable Full, Close Button & States")
        GalleryExample("Full-Screen Terms, Standalone Close & Confirmation States") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "Open full-screen terms",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showScrollableFull = true },
                )
                KiraButton(
                    text = "Open with close button",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showCloseButton = true },
                )
                KiraButton(
                    text = "Review removal",
                    variant = KiraButtonVariant.Destructive,
                    size = KiraButtonSize.Sm,
                    onClick = { showReviewRemoval = true },
                )
                KiraButton(
                    text = "Review information",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showReviewInfo = true },
                )
                KiraButton(
                    text = "Centered & Stacked",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { showStacked = true },
                )
            }
        }

        SectionHeading("All Variants")
        GalleryExample("Default, Info, Success, Warning, Destructive") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraAlertDialogVariant.entries.forEach { variant ->
                    KiraButton(
                        text = variant.name,
                        size = KiraButtonSize.Sm,
                        variant = if (variant == KiraAlertDialogVariant.Destructive) {
                            KiraButtonVariant.Destructive
                        } else {
                            KiraButtonVariant.Outline
                        },
                        onClick = { activeVariant = variant },
                    )
                }
            }
            ExampleNote(status)
        }
    }

    // 1. Basic
    KiraAlertDialog(
        open = showBasic,
        onDismissRequest = { showBasic = false },
        title = "Are you sure?",
        description = "This action cannot be undone. This will permanently delete your account and remove your data from our servers.",
        confirmLabel = "Continue",
        cancelLabel = "Cancel",
        onConfirm = { status = "Confirmed basic dialog." },
        onCancel = { status = "Cancelled basic dialog." },
    )

    // 2. Destructive
    KiraAlertDialog(
        open = showDestructive,
        onDismissRequest = { showDestructive = false },
        variant = KiraAlertDialogVariant.Destructive,
        title = "Delete Account",
        description = "Are you sure you want to delete your account? All of your data will be permanently removed. This action cannot be undone.",
        confirmLabel = "Yes, delete",
        cancelLabel = "Cancel",
        onConfirm = { status = "Account deletion confirmed." },
    )

    // 3. Custom Styled (Media + Warning + List)
    KiraAlertDialog(
        open = showCustom,
        onDismissRequest = { showCustom = false },
        variant = KiraAlertDialogVariant.Warning,
        size = KiraAlertDialogSize.Lg,
        title = "Confirm Deletion",
        description = "Before you proceed, please note the following warning.",
        confirmLabel = "Delete Anyway",
        cancelLabel = "Go Back",
        onConfirm = { status = "Custom styled deletion confirmed." },
    ) {
        KiraAlertDialogWarning(
            text = "This action will permanently delete all associated data and cannot be undone.",
            variant = KiraAlertDialogVariant.Warning,
        )
        KiraAlertDialogList(
            items = listOf(
                "All project files and assets",
                "Team member access and permissions",
                "API keys and integrations",
                "Custom domain configurations",
            ),
            variant = KiraAlertDialogListVariant.Bullet,
        )
    }

    // 4. Type-to-Confirm
    KiraAlertDialog(
        open = showTypeToConfirm,
        onDismissRequest = { showTypeToConfirm = false },
        variant = KiraAlertDialogVariant.Destructive,
        title = "Delete project",
        description = "This permanently deletes the project and all its data.",
        confirmText = "DELETE",
        confirmLabel = "Delete project",
        onConfirm = { status = "Project deleted after typing DELETE." },
    )

    // 5. Timed Confirm
    KiraAlertDialog(
        open = showTimedConfirm,
        onDismissRequest = { showTimedConfirm = false },
        variant = KiraAlertDialogVariant.Warning,
        title = "Wipe cache?",
        description = "The confirm button unlocks after a short cooldown.",
        confirmDelaySeconds = 3,
        confirmLabel = "Wipe now",
        onConfirm = { status = "Cache wiped after 3s cooldown." },
    )

    // 6. Async Confirm
    KiraAlertDialog(
        open = showAsyncConfirm,
        onDismissRequest = { showAsyncConfirm = false },
        variant = KiraAlertDialogVariant.Info,
        title = "Save changes?",
        description = "Shows a pending state before closing.",
        confirmLabel = "Save",
        onConfirmAsync = {
            delay(1_200L)
            status = "Changes saved."
            Result.success(Unit)
        },
    )

    // 7. Async Recovery
    KiraAlertDialog(
        open = showAsyncRecovery,
        onDismissRequest = { showAsyncRecovery = false },
        variant = KiraAlertDialogVariant.Info,
        title = "Publish release?",
        description = "Notify everyone following the November release.",
        confirmLabel = "Publish release",
        onConfirmAsync = {
            val attempt = recoveryAttempts
            recoveryAttempts++
            delay(900L)
            if (attempt == 0) {
                Result.failure(IllegalStateException("The publishing service is unavailable. Try publishing again."))
            } else {
                status = "Release published on attempt #${attempt + 1}."
                Result.success(Unit)
            }
        },
    )

    // 8. Scrollable Full-Screen
    KiraAlertDialog(
        open = showScrollableFull,
        onDismissRequest = { showScrollableFull = false },
        size = KiraAlertDialogSize.Full,
        title = "Terms of Service",
        description = "Scroll to review the full terms.",
        cancelLabel = "Decline",
        confirmLabel = "Accept",
        onConfirm = { status = "Accepted Terms of Service." },
        onCancel = { status = "Declined Terms of Service." },
    ) {
        repeat(20) { index ->
            BasicText(
                text = "${index + 1}. Shared project access ends when a member leaves the team. Review active invitations and exported research before confirming. This is illustrative policy text for the scrolling demo.",
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
        }
    }

    // 9. Standalone Close Button
    KiraAlertDialog(
        open = showCloseButton,
        onDismissRequest = { showCloseButton = false },
        showCloseButton = true,
        showCancel = false,
        title = "Standalone close",
        description = "The X in the corner is an AlertDialogClose. showCloseButton adds it automatically.",
        confirmLabel = "Dismiss",
        onConfirm = { status = "Dismissed standalone close dialog." },
    )

    // 10. Confirmation States: Long title + empty list + typed REMOVE
    KiraAlertDialog(
        open = showReviewRemoval,
        onDismissRequest = { showReviewRemoval = false },
        variant = KiraAlertDialogVariant.Destructive,
        title = "Remove the quarterly customer research project and all its shared findings?",
        description = "Review the empty attachment list, then type REMOVE to enable the action.",
        confirmText = "REMOVE",
        confirmLabel = "Remove project and shared findings",
        onConfirm = { status = "Removed quarterly customer research project." },
    ) {
        BasicText(
            text = "No attachments. Export the project first if you need a copy.",
            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
        )
    }

    // 11. Confirmation States: Success info
    KiraAlertDialog(
        open = showReviewInfo,
        onDismissRequest = { showReviewInfo = false },
        variant = KiraAlertDialogVariant.Success,
        title = "Your export is ready",
        description = "The export contains the current project findings.",
        showCancel = false,
        confirmLabel = "Got it",
        onConfirm = { status = "Acknowledged export ready." },
    )

    // 12. Centered & Stacked layout
    KiraAlertDialog(
        open = showStacked,
        onDismissRequest = { showStacked = false },
        variant = KiraAlertDialogVariant.Warning,
        centered = true,
        footerLayout = KiraAlertDialogFooterLayout.Stacked,
        title = "Reset All Settings?",
        description = "Centered layout with full-width stacked footer buttons for mobile ergonomics.",
        confirmLabel = "Reset Now",
        onConfirm = { status = "Settings reset." },
    )

    // 13. All Variants launcher
    activeVariant?.let { variant ->
        KiraAlertDialog(
            open = true,
            onDismissRequest = { activeVariant = null },
            title = "${variant.name} Confirmation",
            description = "This action cannot be undone. Please confirm that you want to proceed.",
            variant = variant,
            confirmLabel = if (variant == KiraAlertDialogVariant.Destructive) "Delete" else "Confirm",
            onConfirm = { status = "Confirmed ${variant.name} dialog." },
            onCancel = { status = "Cancelled ${variant.name} dialog." },
        )
    }
}
