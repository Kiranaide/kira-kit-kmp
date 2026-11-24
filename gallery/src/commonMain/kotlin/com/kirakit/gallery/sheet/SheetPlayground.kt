package com.kirakit.gallery.sheet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.sheet.KiraSheet
import com.kirakit.components.sheet.KiraSheetBody
import com.kirakit.components.sheet.KiraSheetFooter
import com.kirakit.components.sheet.KiraSheetHandle
import com.kirakit.components.sheet.KiraSheetHeader
import com.kirakit.components.sheet.KiraSheetSide
import com.kirakit.components.sheet.KiraSheetSize
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

internal fun LazyListScope.sheetPlaygroundItems() {
    galleryItem("sheet-basic") { BasicSheetDemo() }
    galleryItem("sheet-edges") { EdgesSheetDemo() }
    galleryItem("sheet-sizes") { SizesSheetDemo() }
    galleryItem("sheet-drafts") { DraftsSheetDemo() }
    galleryItem("sheet-detents") { DetentsSheetDemo() }
    galleryItem("sheet-inspector") { InspectorSheetDemo() }
    galleryItem("sheet-history") { ScrollingHistorySheetDemo() }
    galleryItem("sheet-kira") { KiraEditSheetDemo() }
    galleryItem("sheet-states") { StatesSheetDemo() }
}

@Composable
internal fun SheetPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicSheetDemo()
        EdgesSheetDemo()
        SizesSheetDemo()
        DraftsSheetDemo()
        DetentsSheetDemo()
        InspectorSheetDemo()
        ScrollingHistorySheetDemo()
        KiraEditSheetDemo()
        StatesSheetDemo()
    }
}

@Composable
private fun BasicSheetDemo() {
    val colors = KiraTheme.colors
    var open by remember { mutableStateOf(false) }
    var reviewed by remember { mutableStateOf(false) }

    GalleryExample("Basic") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KiraButton(text = "Review release", variant = KiraButtonVariant.Outline, onClick = { open = true })
            ExampleNote(if (reviewed) "October release reviewed." else "The release is waiting for review.")
        }

        KiraSheet(
            open = open,
            onDismissRequest = { open = false },
            title = "October release",
            description = "Local release-planning fixture.",
            footer = {
                KiraButton(text = "Close", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { open = false })
                KiraButton(
                    text = "Mark reviewed",
                    size = KiraButtonSize.Sm,
                    onClick = {
                        reviewed = true
                        open = false
                    },
                )
            },
        ) {
            BasicText("Owner", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
            BasicText("Maya Chen", style = KiraTheme.typography.body.copy(color = colors.foreground, fontWeight = FontWeight.Medium))
            BasicText("Scope", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
            ExampleNote("Permissions, audit history, and export improvements.")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EdgesSheetDemo() {
    var activeSide by remember { mutableStateOf<KiraSheetSide?>(null) }

    GalleryExample("Edges (Left, Right, Top, Bottom)") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraSheetSide.entries.forEach { side ->
                KiraButton(
                    text = side.name.lowercase(),
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { activeSide = side },
                )
            }
        }
        activeSide?.let { side ->
            KiraSheet(
                open = true,
                onDismissRequest = { activeSide = null },
                title = "Release notes · ${side.name.lowercase()}",
                description = "Local release-planning fixture.",
                side = side,
                responsive = false,
            ) {
                ExampleNote("The root owns the edge. Content owns the size, focus targets, and scrolling.")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SizesSheetDemo() {
    var activeSize by remember { mutableStateOf<KiraSheetSize?>(null) }

    GalleryExample("Sizes (Sm, Default, Lg, Xl, Full)") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraSheetSize.entries.forEach { sz ->
                KiraButton(
                    text = sz.name.lowercase(),
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { activeSize = sz },
                )
            }
        }
        activeSize?.let { sz ->
            KiraSheet(
                open = true,
                onDismissRequest = { activeSize = null },
                title = "Release inspector · ${sz.name.lowercase()}",
                description = "Local release-planning fixture.",
                size = sz,
            ) {
                ExampleNote("Use a narrow inspector for metadata and a larger panel for longer editing tasks.")
            }
        }
    }
}

@Composable
private fun DraftsSheetDemo() {
    var open by remember { mutableStateOf(false) }
    var draftNote by remember { mutableStateOf("Document the permission changes") }

    GalleryExample("Drafts (Retained Unsaved Input)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraButton(text = "Open local draft", variant = KiraButtonVariant.Outline, onClick = { open = true })
            ExampleNote("Draft value: $draftNote")
        }

        KiraSheet(
            open = open,
            onDismissRequest = { open = false },
            title = "Draft release note",
            description = "This unsaved field is retained across open and close cycles.",
            footer = {
                KiraButton(text = "Close draft", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { open = false })
            },
        ) {
            KiraLabel(text = "Release note")
            KiraInput(value = draftNote, onValueChange = { draftNote = it })
        }
    }
}

@Composable
private fun DetentsSheetDemo() {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    var open by remember { mutableStateOf(false) }
    var snapIndex by remember { mutableIntStateOf(0) }

    GalleryExample("Detents (Bottom Snap Points & Handle)") {
        KiraButton(text = "Open release actions", variant = KiraButtonVariant.Outline, onClick = { open = true })

        KiraSheet(
            open = open,
            onDismissRequest = { open = false },
            side = KiraSheetSide.Bottom,
            responsive = false,
            snapPoints = listOf(0.5f, 1.0f),
            activeSnapIndex = snapIndex,
            onSnapIndexChange = { snapIndex = it },
        ) {
            KiraSheetHandle(
                expanded = snapIndex == 1,
                onExpandedChange = { expanded -> snapIndex = if (expanded) 1 else 0 },
            )
            KiraSheetHeader(title = "Release actions", description = "Tap the handle or swipe to toggle 50% ↔ 100% height.")
            KiraSheetBody(modifier = Modifier.weight(1f, fill = false)) {
                ExampleNote("The handle is an accessible button providing a tap/keyboard alternative to swiping.")
                listOf("Review access rules", "Confirm the changelog", "Prepare the rollout").forEach { task ->
                    BasicText(
                        text = task,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(radius.shapeLg)
                            .border(1.dp, colors.border, radius.shapeLg)
                            .padding(14.dp),
                        style = KiraTheme.typography.bodySmall.copy(color = colors.foreground),
                    )
                }
            }
            KiraSheetFooter {
                KiraButton(text = "Close actions", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { open = false })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InspectorSheetDemo() {
    val colors = KiraTheme.colors
    var open by remember { mutableStateOf(false) }
    var queued by remember { mutableIntStateOf(0) }

    GalleryExample("Inspector") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(text = "Inspect release", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { open = true })
                KiraButton(text = "Queue next item", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { queued++ })
            }
            ExampleNote("$queued items queued.")
        }

        KiraSheet(
            open = open,
            onDismissRequest = { open = false },
            title = "October release",
            description = "Read-only inspector panel.",
            size = KiraSheetSize.Sm,
            footer = {
                KiraButton(text = "Queue +1", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { queued++ })
                KiraButton(text = "Done", size = KiraButtonSize.Sm, onClick = { open = false })
            },
        ) {
            BasicText("Owner", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
            BasicText("Maya Chen", style = KiraTheme.typography.body.copy(color = colors.foreground))
            BasicText("Queue", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
            BasicText("$queued items", style = KiraTheme.typography.body.copy(color = colors.foreground, fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
private fun ScrollingHistorySheetDemo() {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    var open by remember { mutableStateOf(false) }

    GalleryExample("Scrolling (24 Revisions with Pinned Header & Footer)") {
        KiraButton(text = "Read release history", variant = KiraButtonVariant.Outline, onClick = { open = true })

        KiraSheet(
            open = open,
            onDismissRequest = { open = false },
            title = "Release history",
            description = "Local release-planning fixture.",
            size = KiraSheetSize.Lg,
            footer = {
                KiraButton(text = "Done reading", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { open = false })
            },
        ) {
            repeat(24) { index ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(radius.shapeLg)
                        .border(1.dp, colors.border, radius.shapeLg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BasicText(
                        "Revision ${index + 1}",
                        style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium),
                    )
                    ExampleNote("Access-rule review and rollout notes for the local fixture.")
                }
            }
        }
    }
}

@Composable
private fun KiraEditSheetDemo() {
    var open by remember { mutableStateOf(false) }
    var record by remember { mutableStateOf("October release") }
    var draft by remember { mutableStateOf("October release") }
    var error by remember { mutableStateOf<String?>(null) }
    val dirty = draft != record

    GalleryExample("Kira (Dirty Close Guard & Validation)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KiraButton(text = "Edit release", variant = KiraButtonVariant.Outline, onClick = { open = true })
            ExampleNote("Saved release: $record")
        }

        KiraSheet(
            open = open,
            onDismissRequest = { open = false },
            title = "Edit release",
            description = "Changes affect only this local fixture.",
            closeDisabledReason = if (dirty) "Save the changes or discard this draft before closing." else null,
            error = error,
            footer = {
                KiraButton(
                    text = "Discard draft",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        draft = record
                        error = null
                        open = false
                    },
                )
                KiraButton(
                    text = "Save changes",
                    size = KiraButtonSize.Sm,
                    onClick = {
                        val trimmed = draft.trim()
                        if (trimmed.isEmpty()) {
                            error = "Enter a release name before saving."
                        } else {
                            record = trimmed
                            draft = trimmed
                            error = null
                            open = false
                        }
                    },
                )
            },
        ) {
            KiraLabel(text = "Release name")
            KiraInput(
                value = draft,
                onValueChange = {
                    draft = it
                    error = null
                },
                isError = error != null,
                placeholder = "Enter release name",
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatesSheetDemo() {
    var open by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf("ready") }
    var note by remember { mutableStateOf("Review the access rules") }

    GalleryExample("States (Loading, Error, Empty, Read-only & Long Title)") {
        KiraButton(text = "Open content states", variant = KiraButtonVariant.Outline, onClick = { open = true })

        KiraSheet(
            open = open,
            onDismissRequest = { open = false },
            title = "release-permissions-audit-history-and-migration-notes",
            description = "A long title, local loading, recovery, empty content, and read-only details.",
            loading = state == "loading",
            loadingLabel = "Loading the local revision fixture…",
            closeDisabledReason = if (state == "loading") "Cancel loading before closing." else null,
            error = if (state == "error") "Revision details are unavailable. Try loading them again." else null,
            onRetry = { state = "ready" },
            footer = {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KiraButton(
                        text = if (state == "loading") "Cancel loading" else "Show loading",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { state = if (state == "loading") "ready" else "loading" },
                    )
                    KiraButton(text = "Error", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { state = "error" })
                    KiraButton(text = "Empty", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { state = "empty" })
                    KiraButton(text = "Read-only", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { state = "read-only" })
                    KiraButton(text = "Ready", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { state = "ready" })
                }
            },
        ) {
            if (state == "empty") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExampleNote("No revisions yet.")
                    KiraButton(text = "Create local revision", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { state = "ready" })
                }
            } else {
                KiraLabel(text = "Owner note")
                KiraInput(
                    value = note,
                    onValueChange = { note = it },
                    readOnly = state == "read-only",
                )
                ExampleNote(
                    if (state == "read-only") "This snapshot is read-only. Request edit access from the owner."
                    else "A loading refresh keeps this field mounted.",
                )
            }
        }
    }
}
