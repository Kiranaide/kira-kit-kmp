package com.kirakit.gallery.modal

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.dialog.KiraDialogSize
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.modal.KiraModal
import com.kirakit.components.modal.KiraModalSize
import com.kirakit.components.textarea.KiraTextarea
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ModalPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md

    var showBasic by remember { mutableStateOf(false) }
    var activeSize by remember { mutableStateOf<KiraModalSize?>(null) }
    var showCentered by remember { mutableStateOf(false) }
    var showControlled by remember { mutableStateOf(false) }
    var showScrollable by remember { mutableStateOf(false) }
    var showProjectForm by remember { mutableStateOf(false) }

    var projectName by rememberSaveable { mutableStateOf("") }
    var projectDescription by rememberSaveable { mutableStateOf("") }
    var lastCreatedProject by rememberSaveable { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Sizes")
        GalleryExample("Basic Modal") {
            KiraButton(
                text = "Open Modal",
                onClick = { showBasic = true },
            )
        }

        GalleryExample("Size Variants") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraDialogSize.entries.forEach { size ->
                    KiraButton(
                        text = size.name,
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { activeSize = size },
                    )
                }
            }
        }

        SectionHeading("Centered & Controlled")
        GalleryExample("Centered Modal") {
            KiraButton(
                text = "Open Centered Modal",
                variant = KiraButtonVariant.Outline,
                onClick = { showCentered = true },
            )
        }

        GalleryExample("Controlled State") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = "Open Controlled Modal",
                    onClick = { showControlled = true },
                )
                ExampleNote("Modal is currently: ${if (showControlled) "open" else "closed"}")
            }
        }

        SectionHeading("Scrollable & Dialog Form")
        GalleryExample("Scrollable Content") {
            KiraButton(
                text = "View Terms",
                variant = KiraButtonVariant.Outline,
                onClick = { showScrollable = true },
            )
        }

        GalleryExample("Dialog Form") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = "Create Project",
                    onClick = { showProjectForm = true },
                )
                lastCreatedProject?.let { ExampleNote("Created project: $it") }
            }
        }
    }

    // 1. Basic Modal
    KiraModal(
        open = showBasic,
        onDismissRequest = { showBasic = false },
        title = "Modal Title",
        description = "This is a basic modal with title and description.",
        footer = {
            KiraButton(
                text = "Cancel",
                variant = KiraButtonVariant.Outline,
                onClick = { showBasic = false },
            )
            KiraButton(
                text = "Confirm",
                onClick = { showBasic = false },
            )
        },
    ) {
        BasicText(
            text = "Modal content goes here. Modals are used to focus user attention on a specific task or information.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }

    // 2. Size Variants
    activeSize?.let { size ->
        KiraModal(
            open = true,
            onDismissRequest = { activeSize = null },
            title = "${size.name} Modal",
            description = "Modal with size = KiraModalSize.${size.name}",
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
                    text = "Content area (${size.name})",
                    style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                )
            }
        }
    }

    // 3. Centered Modal
    KiraModal(
        open = showCentered,
        onDismissRequest = { showCentered = false },
        title = "Centered Modal",
        description = "This modal is vertically and horizontally centered by default.",
        footer = {
            KiraButton(text = "Close", onClick = { showCentered = false })
        },
    ) {
        BasicText(
            text = "The modal is automatically centered within the viewport with safe-area and IME padding.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }

    // 4. Controlled Modal
    KiraModal(
        open = showControlled,
        onDismissRequest = { showControlled = false },
        title = "Controlled Modal",
        description = "This modal's state is controlled by Compose state.",
        footer = {
            KiraButton(
                text = "Close",
                variant = KiraButtonVariant.Outline,
                onClick = { showControlled = false },
            )
            KiraButton(
                text = "Confirm",
                onClick = { showControlled = false },
            )
        },
    ) {
        BasicText(
            text = "Use the open and onDismissRequest parameters to control the modal's visibility programmatically.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }

    // 5. Scrollable Terms
    KiraModal(
        open = showScrollable,
        onDismissRequest = { showScrollable = false },
        title = "Terms and Conditions",
        description = "Please read and accept our terms to continue.",
        size = KiraDialogSize.Lg,
        scrollable = true,
        modifier = Modifier.heightIn(max = 460.dp),
        footer = {
            KiraButton(
                text = "Decline",
                variant = KiraButtonVariant.Outline,
                onClick = { showScrollable = false },
            )
            KiraButton(
                text = "Accept Terms",
                onClick = { showScrollable = false },
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(8) { index ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        text = "Section ${index + 1}",
                        style = KiraTheme.typography.label.copy(
                            color = colors.foreground,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    BasicText(
                        text = "Project collaborators can view shared documentation while their workspace membership remains active. Update permissions at any time from workspace settings.",
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                }
            }
        }
    }

    // 6. Create Project Dialog Form
    KiraModal(
        open = showProjectForm,
        onDismissRequest = { showProjectForm = false },
        title = "Create New Project",
        description = "Fill in the details below to create a new project.",
        size = KiraDialogSize.Lg,
        footer = {
            KiraButton(
                text = "Cancel",
                variant = KiraButtonVariant.Outline,
                onClick = { showProjectForm = false },
            )
            KiraButton(
                text = "Create Project",
                onClick = {
                    val trimmed = projectName.trim().ifEmpty { "Untitled Project" }
                    lastCreatedProject = trimmed
                    showProjectForm = false
                },
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Project Name", required = true)
                KiraInput(
                    value = projectName,
                    onValueChange = { projectName = it },
                    placeholder = "My Awesome Project",
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Description")
                KiraTextarea(
                    value = projectDescription,
                    onValueChange = { projectDescription = it },
                    placeholder = "Brief description of your project...",
                    minRows = 3,
                )
            }
        }
    }
}
