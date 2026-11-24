package com.kirakit.gallery.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.dialog.KiraDialog
import com.kirakit.components.dialog.KiraDialogSize
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.select.KiraSelect
import com.kirakit.components.select.KiraSelectOption
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ProjectSaveState {
    Idle,
    Pending,
    Saved,
    Invalid,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DialogPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md
    val scope = rememberCoroutineScope()

    var showBasic by remember { mutableStateOf(false) }
    var activeSize by remember { mutableStateOf<KiraDialogSize?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var showScrollable by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showNonModal by remember { mutableStateOf(false) }
    var outsideClicks by remember { mutableIntStateOf(0) }
    var showComplexForm by remember { mutableStateOf(false) }
    var showNoClose by remember { mutableStateOf(false) }
    var showShareProject by remember { mutableStateOf(false) }

    var profileName by rememberSaveable { mutableStateOf("John Doe") }
    var profileEmail by rememberSaveable { mutableStateOf("john@example.com") }

    var projectName by rememberSaveable { mutableStateOf("Customer research") }
    var savedProjectName by rememberSaveable { mutableStateOf("Customer research") }
    var saveState by remember { mutableStateOf(ProjectSaveState.Idle) }

    var meetingTitle by rememberSaveable { mutableStateOf("") }
    var meetingParticipant by rememberSaveable { mutableStateOf<String?>(null) }
    var meetingAgenda by rememberSaveable { mutableStateOf("") }
    val participants = remember {
        listOf(
            KiraSelectOption("alice", "Alice Johnson"),
            KiraSelectOption("bob", "Bob Smith"),
            KiraSelectOption("charlie", "Charlie Brown"),
            KiraSelectOption("diana", "Diana Prince"),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Sizes")
        GalleryExample("Basic Dialog") {
            KiraButton(
                text = "Open Dialog",
                variant = KiraButtonVariant.Outline,
                onClick = { showBasic = true },
            )
        }

        GalleryExample("Size Tiers (Sm, Default, Lg, Xl, Full)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraDialogSize.entries.forEach { size ->
                    KiraButton(
                        text = "${size.name} (${if (size == KiraDialogSize.Full) "Fullscreen" else "Pop-up"})",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { activeSize = size },
                    )
                }
            }
        }

        SectionHeading("Form, Async Save & Scrollable")
        GalleryExample("Edit Profile & Scrollable Terms") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(text = "Edit Profile", onClick = { showForm = true })
                KiraButton(
                    text = "Open Scrollable Dialog",
                    variant = KiraButtonVariant.Outline,
                    onClick = { showScrollable = true },
                )
            }
            ExampleNote("Saved profile: $profileName ($profileEmail)")
        }

        GalleryExample("Async Save & Validation") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExampleNote("Current project: $savedProjectName")
                KiraButton(
                    text = "Edit project",
                    onClick = {
                        projectName = savedProjectName
                        saveState = ProjectSaveState.Idle
                        showSaveDialog = true
                    },
                )
            }
        }

        SectionHeading("Non-Modal, Custom & States")
        GalleryExample("Non-Modal (Keep the Page Interactive)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraButton(
                        text = if (showNonModal) "Close details" else "Open details",
                        variant = KiraButtonVariant.Outline,
                        onClick = { showNonModal = !showNonModal },
                    )
                    KiraButton(
                        text = if (outsideClicks == 0) "Outside action" else "Clicked ${outsideClicks}×",
                        variant = KiraButtonVariant.Outline,
                        onClick = { outsideClicks++ },
                    )
                }
                ExampleNote("Non-modal dialogs stay open while you work with the page. Close with Esc or ✕.")
            }
        }

        GalleryExample("Complex Fullscreen Form, No Close Button & Empty State") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "Open Complex Form",
                    onClick = { showComplexForm = true },
                )
                KiraButton(
                    text = "No Close Button",
                    variant = KiraButtonVariant.Outline,
                    onClick = { showNoClose = true },
                )
                KiraButton(
                    text = "Share project",
                    variant = KiraButtonVariant.Outline,
                    onClick = { showShareProject = true },
                )
            }
        }
    }

    // 1. Basic
    KiraDialog(
        open = showBasic,
        onDismissRequest = { showBasic = false },
        title = "Welcome to Kira Kit",
        description = "This is a basic dialog with a title and description.",
        footer = {
            KiraButton(
                text = "Cancel",
                variant = KiraButtonVariant.Outline,
                onClick = { showBasic = false },
            )
            KiraButton(text = "Continue", onClick = { showBasic = false })
        },
    ) {
        BasicText(
            text = "You can put any content here including forms, images, or other components.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }

    // 2. Sizes
    activeSize?.let { size ->
        KiraDialog(
            open = true,
            onDismissRequest = { activeSize = null },
            title = "${size.name} Dialog",
            description = "This is a dialog with size = KiraDialogSize.${size.name}.",
            size = size,
            footer = {
                KiraButton(text = "Close", onClick = { activeSize = null })
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(KiraTheme.radius.shapeLg)
                    .background(colors.muted.copy(alpha = 0.25f))
                    .border(1.dp, colors.border, KiraTheme.radius.shapeLg),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Content Area (${size.name})",
                    style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                )
            }
        }
    }

    // 3. Edit Profile Form
    KiraDialog(
        open = showForm,
        onDismissRequest = { showForm = false },
        title = "Edit Profile",
        description = "Make changes to your profile here. Click save when done.",
        footer = {
            KiraButton(
                text = "Cancel",
                variant = KiraButtonVariant.Outline,
                onClick = { showForm = false },
            )
            KiraButton(text = "Save Changes", onClick = { showForm = false })
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel(text = "Name", required = true)
                KiraInput(value = profileName, onValueChange = { profileName = it })
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel(text = "Email", required = true)
                KiraInput(value = profileEmail, onValueChange = { profileEmail = it })
            }
        }
    }

    // 4. Scrollable Terms
    KiraDialog(
        open = showScrollable,
        onDismissRequest = { showScrollable = false },
        title = "Terms of Service",
        description = "Please read and accept our terms of service.",
        size = KiraDialogSize.Lg,
        scrollable = true,
        modifier = Modifier.heightIn(max = 460.dp),
        footer = {
            KiraButton(
                text = "Decline",
                variant = KiraButtonVariant.Outline,
                onClick = { showScrollable = false },
            )
            KiraButton(text = "Accept", onClick = { showScrollable = false })
        },
    ) {
        repeat(10) { i ->
            BasicText(
                text = "Section ${i + 1}: Project members can access shared research while their invitation is active. Remove access from project settings when a collaborator leaves the team. This sample policy is illustrative demo content.",
                style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
            )
        }
    }

    // 5. Async Save & Validation
    val isSaving = saveState == ProjectSaveState.Pending
    KiraDialog(
        open = showSaveDialog,
        onDismissRequest = { if (!isSaving) showSaveDialog = false },
        title = "Edit project",
        description = "Save a project name locally for this demo. Use Alert Dialog for irreversible confirmations.",
        scrollable = true,
        dismissOnBackPress = !isSaving,
        dismissOnClickOutside = !isSaving,
        footer = {
            KiraButton(
                text = "Done",
                variant = KiraButtonVariant.Outline,
                enabled = !isSaving,
                onClick = { showSaveDialog = false },
            )
            KiraButton(
                text = if (isSaving) "Saving..." else "Save project",
                loading = isSaving,
                enabled = !isSaving,
                onClick = {
                    if (isSaving) return@KiraButton
                    if (projectName.isBlank()) {
                        saveState = ProjectSaveState.Invalid
                        return@KiraButton
                    }
                    saveState = ProjectSaveState.Pending
                    scope.launch {
                        delay(600L)
                        savedProjectName = projectName.trim()
                        saveState = ProjectSaveState.Saved
                    }
                },
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraLabel(text = "Project name")
            KiraInput(
                value = projectName,
                onValueChange = {
                    projectName = it
                    saveState = ProjectSaveState.Idle
                },
                enabled = !isSaving,
                isError = saveState == ProjectSaveState.Invalid,
            )
            val statusText = when (saveState) {
                ProjectSaveState.Invalid -> "Enter a project name, then save again."
                ProjectSaveState.Saved -> "Project saved."
                ProjectSaveState.Pending -> "Saving your project."
                ProjectSaveState.Idle -> "Your changes stay in this demo."
            }
            BasicText(
                text = statusText,
                style = KiraTheme.typography.bodySmall.copy(
                    color = if (saveState == ProjectSaveState.Invalid) colors.destructive else colors.mutedForeground,
                ),
            )
        }
    }

    // 6. Non-Modal
    KiraDialog(
        open = showNonModal,
        onDismissRequest = { showNonModal = false },
        title = "Details",
        description = "Stays open while you use the page. Close with Esc or ✕.",
        size = KiraDialogSize.Sm,
        modal = false,
        footer = {
            KiraButton(
                text = "Done",
                size = KiraButtonSize.Sm,
                onClick = { showNonModal = false },
            )
        },
    ) {
        BasicText(
            text = "Click the Outside action button — the dialog keeps its state (clicks: $outsideClicks).",
            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
        )
    }

    // 7. Complex Fullscreen Form
    KiraDialog(
        open = showComplexForm,
        onDismissRequest = { showComplexForm = false },
        title = "Schedule Meeting",
        description = "Fill out the form below to schedule a new meeting.",
        size = KiraDialogSize.Full,
        scrollable = true,
        footer = {
            KiraButton(
                text = "Cancel",
                variant = KiraButtonVariant.Outline,
                onClick = { showComplexForm = false },
            )
            KiraButton(text = "Schedule Meeting", onClick = { showComplexForm = false })
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel(text = "Meeting Title")
                KiraInput(
                    value = meetingTitle,
                    onValueChange = { meetingTitle = it },
                    placeholder = "e.g. Q4 Planning",
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel(text = "Participants")
                KiraSelect(
                    options = participants,
                    value = meetingParticipant,
                    onValueChange = { meetingParticipant = it },
                    placeholder = "Select a participant",
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel(text = "Description")
                KiraInput(
                    value = meetingAgenda,
                    onValueChange = { meetingAgenda = it },
                    placeholder = "Meeting agenda...",
                )
            }
        }
    }

    // 8. No Close Button
    KiraDialog(
        open = showNoClose,
        onDismissRequest = { showNoClose = false },
        title = "Important Notice",
        description = "This dialog has no X button. Use the Close button below to dismiss.",
        showCloseButton = false,
        footer = {
            KiraButton(
                text = "Close",
                variant = KiraButtonVariant.Outline,
                onClick = { showNoClose = false },
            )
        },
    ) {
        BasicText(
            text = "Setting showCloseButton = false hides the top-right close icon.",
            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
        )
    }

    // 9. Share Project (Long title + Empty state)
    KiraDialog(
        open = showShareProject,
        onDismissRequest = { showShareProject = false },
        title = "Share the quarterly customer research project",
        description = "Choose who can access this project.",
        footer = {
            KiraButton(text = "Done", onClick = { showShareProject = false })
        },
    ) {
        BasicText(
            text = "No collaborators yet. Invite someone to get started.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }
}
