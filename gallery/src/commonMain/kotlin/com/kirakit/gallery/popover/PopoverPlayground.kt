package com.kirakit.gallery.popover

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.checkbox.KiraCheckbox
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.popover.KiraPopover
import com.kirakit.components.popover.KiraPopoverAlign
import com.kirakit.components.popover.KiraPopoverFlow
import com.kirakit.components.popover.KiraPopoverFlowView
import com.kirakit.components.popover.KiraPopoverSide
import com.kirakit.components.popover.KiraPopoverSize
import com.kirakit.components.popover.KiraPopoverVariant
import com.kirakit.components.popover.rememberKiraPopoverHandle
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun LazyListScope.popoverPlaygroundItems() {
    galleryItem("pop-shared") { SharedEditorDemo() }
    galleryItem("pop-flow") { FlowDemo() }
    galleryItem("pop-kira") { KiraPopoverDemo() }
    galleryItem("pop-states") { StatesStressDemo() }
    galleryItem("pop-basic") { BasicPopoverDemo() }
    galleryItem("pop-positions") { PositionsDemo() }
    galleryItem("pop-alignments") { AlignmentsDemo() }
    galleryItem("pop-offset") { SideOffsetDemo() }
    galleryItem("pop-form") { FormPopoverDemo() }
    galleryItem("pop-custom") { CustomTriggerDemo() }
}

@Composable
internal fun PopoverPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SharedEditorDemo()
        FlowDemo()
        KiraPopoverDemo()
        StatesStressDemo()
        BasicPopoverDemo()
        PositionsDemo()
        AlignmentsDemo()
        SideOffsetDemo()
        FormPopoverDemo()
        CustomTriggerDemo()
    }
}

@Composable
private fun SharedEditorDemo() {
    val colors = KiraTheme.colors
    val scope = rememberCoroutineScope()
    val projects = remember {
        listOf(
            "atlas" to "Atlas design system",
            "harbor" to "Harbor accessibility review",
        )
    }
    val assignees = remember {
        mutableStateMapOf("atlas" to "Maya Chen", "harbor" to "Sam Rivera")
    }
    var activeProject by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var failNext by remember { mutableStateOf(true) }
    var announcement by remember { mutableStateOf("") }

    GalleryExample("Shared Editor (Draft Guard & Retry)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ExampleNote("Illustrative request: the first save fails; retry succeeds. Editing and closing prompts discard confirmation.")
            projects.forEach { (id, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText(
                            text = name,
                            style = KiraTheme.typography.label.copy(
                                color = colors.foreground,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        ExampleNote(assignees[id].orEmpty())
                    }
                    KiraPopover(
                        open = activeProject == id,
                        onOpenChange = { next ->
                            if (next) {
                                activeProject = id
                                draft = assignees[id].orEmpty()
                                error = null
                            } else {
                                activeProject = null
                                error = null
                            }
                        },
                        title = "Assign $id",
                        description = "Changes stay here until you submit or discard them.",
                        dirty = activeProject == id && draft.trim() != assignees[id].orEmpty(),
                        preventClose = pending,
                        onDiscard = {
                            draft = assignees[id].orEmpty()
                            error = null
                        },
                        useBottomSheetOnCompact = true,
                        trigger = {
                            KiraButton(
                                text = "Edit $id",
                                variant = KiraButtonVariant.Outline,
                                size = KiraButtonSize.Sm,
                                onClick = {
                                    activeProject = id
                                    draft = assignees[id].orEmpty()
                                    error = null
                                },
                            )
                        },
                    ) {
                        KiraLabel(text = "Assignee name (required)")
                        KiraInput(
                            value = draft,
                            onValueChange = {
                                draft = it
                                error = null
                            },
                            enabled = !pending,
                            isError = error != null,
                            placeholder = "Enter assignee name",
                        )
                        if (error != null) {
                            BasicText(
                                text = error.orEmpty(),
                                style = KiraTheme.typography.bodySmall.copy(color = colors.destructive),
                            )
                        } else if (pending) {
                            ExampleNote("Saving assignee…")
                        }
                        KiraButton(
                            text = when {
                                pending -> "Saving…"
                                error != null -> "Retry saving"
                                else -> "Save assignee"
                            },
                            size = KiraButtonSize.Sm,
                            loading = pending,
                            enabled = !pending,
                            onClick = {
                                val next = draft.trim()
                                if (next.isEmpty()) {
                                    error = "Enter an assignee name before saving."
                                    return@KiraButton
                                }
                                pending = true
                                error = null
                                scope.launch {
                                    delay(800L)
                                    pending = false
                                    if (failNext) {
                                        failNext = false
                                        error = "The illustrative request failed. Your draft is kept; retry saving."
                                    } else {
                                        assignees[id] = next
                                        announcement = "Assignee saved for $id ($next)."
                                        activeProject = null
                                    }
                                }
                            },
                        )
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            }
            if (announcement.isNotEmpty()) {
                ExampleNote(announcement)
            }
        }
    }
}

@Composable
private fun FlowDemo() {
    var open by remember { mutableStateOf(false) }
    var emailEnabled by remember { mutableStateOf(true) }
    var preserveView by remember { mutableStateOf(false) }
    var controlledView by remember { mutableStateOf("overview") }

    GalleryExample("Flow (Multi-View Drill-Down)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KiraCheckbox(
                    checked = preserveView,
                    onCheckedChange = { preserveView = it },
                )
                BasicText(
                    text = "Control the view (preserve it on close)",
                    style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.foreground),
                )
            }
            KiraPopoverFlow(
                open = open,
                onOpenChange = { open = it },
                initialViewId = "overview",
                currentViewId = if (preserveView) controlledView else null,
                onViewChange = { controlledView = it },
                showArrow = true,
                views = listOf(
                    KiraPopoverFlowView(id = "overview", title = "Notifications") { nav ->
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ExampleNote("Email delivery is ${if (emailEnabled) "enabled" else "disabled"}.")
                            KiraButton(
                                text = "Delivery settings",
                                size = KiraButtonSize.Sm,
                                onClick = { nav.navigate("delivery") },
                            )
                        }
                    },
                    KiraPopoverFlowView(id = "delivery", title = "Delivery settings") { nav ->
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                KiraCheckbox(
                                    checked = emailEnabled,
                                    onCheckedChange = { emailEnabled = it },
                                )
                                BasicText(
                                    text = "Email notifications",
                                    style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.foreground),
                                )
                            }
                            KiraButton(
                                text = "Back to overview",
                                variant = KiraButtonVariant.Outline,
                                size = KiraButtonSize.Sm,
                                onClick = { nav.back() },
                            )
                        }
                    },
                ),
                trigger = {
                    KiraButton(
                        text = "Notification preferences",
                        variant = KiraButtonVariant.Outline,
                        onClick = { open = !open },
                    )
                },
            )
        }
    }
}

@Composable
private fun KiraPopoverDemo() {
    var open by remember { mutableStateOf(false) }
    var notesOpened by remember { mutableIntStateOf(0) }

    GalleryExample("Kira Popover") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraPopover(
                open = open,
                onOpenChange = { open = it },
                title = "Atlas launch",
                description = "Review the release before you continue.",
                showArrow = true,
                trigger = {
                    KiraButton(
                        text = "Project details",
                        onClick = { open = !open },
                    )
                },
                footer = {
                    KiraButton(
                        text = "Open release notes",
                        variant = KiraButtonVariant.Link,
                        size = KiraButtonSize.Sm,
                        onClick = {
                            notesOpened++
                            open = false
                        },
                    )
                },
            ) {
                ExampleNote("The next review is on Thursday.")
            }
            ExampleNote(
                if (notesOpened > 0) "Atlas release notes opened ($notesOpened×): accessibility review scheduled."
                else "Atlas release notes: accessibility review scheduled.",
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatesStressDemo() {
    var modalOpen by remember { mutableStateOf(false) }
    var nestedOpen by remember { mutableStateOf(false) }
    var controlledRecord by remember { mutableStateOf<String?>(null) }
    val payloadHandle = rememberKiraPopoverHandle<String>()

    GalleryExample("States / Stress (Modal, Nested, Long Content & Shared Handle)") {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ExampleNote("Constrained 320dp container with modal backdrop, nested popup, long scrollable list, disabled trigger, and shared payload handle.")

            KiraPopover(
                open = modalOpen,
                onOpenChange = { modalOpen = it },
                title = "A release review with an unusually long project name that must wrap inside the available space",
                description = "Modal backdrop with nested details and 18 scrollable review items.",
                size = KiraPopoverSize.Xl,
                showBackdrop = true,
                showClose = false,
                useBottomSheetOnCompact = true,
                trigger = {
                    KiraButton(
                        text = "Modal and nested popup",
                        variant = KiraButtonVariant.Outline,
                        onClick = { modalOpen = true },
                    )
                },
            ) {
                KiraPopover(
                    open = nestedOpen,
                    onOpenChange = { nestedOpen = it },
                    title = "Nested details",
                    size = KiraPopoverSize.Sm,
                    trigger = {
                        KiraButton(
                            text = "Nested details",
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { nestedOpen = !nestedOpen },
                        )
                    },
                ) {
                    ExampleNote("Nested popover opened inside a modal popover.")
                }
                repeat(18) { index ->
                    ExampleNote("Review item ${index + 1}: Check accessible names and keyboard focus before releasing.")
                }
                KiraButton(
                    text = "Close review",
                    size = KiraButtonSize.Sm,
                    onClick = { modalOpen = false },
                )
            }

            KiraButton(
                text = "No project selected",
                variant = KiraButtonVariant.Outline,
                enabled = false,
                onClick = {},
            )
            ExampleNote("Select a project before editing.")

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("atlas", "harbor").forEach { record ->
                    KiraPopover(
                        open = controlledRecord == record,
                        onOpenChange = { next -> controlledRecord = if (next) record else null },
                        title = "Controlled ${record.replaceFirstChar { it.uppercase() }}",
                        description = "No notifications yet. Choose delivery settings to get started.",
                        trigger = {
                            KiraButton(
                                text = "Controlled ${record.replaceFirstChar { it.uppercase() }}",
                                variant = KiraButtonVariant.Outline,
                                size = KiraButtonSize.Sm,
                                onClick = { controlledRecord = if (controlledRecord == record) null else record },
                            )
                        },
                    ) {
                        ExampleNote("Active record: $record")
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Atlas", "Harbor").forEach { name ->
                    KiraButton(
                        text = "Preview $name",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { payloadHandle.open(name) },
                    )
                }
            }
            KiraPopover(
                open = payloadHandle.isOpen,
                onOpenChange = { if (!it) payloadHandle.close() },
                title = payloadHandle.payload ?: "No project",
                description = "Shared KiraPopoverHandle switches content between payload triggers.",
                trigger = {},
            ) {
                ExampleNote("Previewing ${payloadHandle.payload ?: "none"}.")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicPopoverDemo() {
    var openPlain by remember { mutableStateOf(false) }
    var openArrow by remember { mutableStateOf(false) }
    var openOutline by remember { mutableStateOf(false) }

    GalleryExample("Basic (Default, Arrow & Outline)") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraPopover(
                open = openPlain,
                onOpenChange = { openPlain = it },
                title = "Popover Title",
                description = "This is a basic popover with accessible title and description.",
                showClose = false,
                trigger = {
                    KiraButton(text = "Open Popover", variant = KiraButtonVariant.Outline, onClick = { openPlain = !openPlain })
                },
            ) {}

            KiraPopover(
                open = openArrow,
                onOpenChange = { openArrow = it },
                title = "With Arrow",
                description = "The arrow points to the trigger element.",
                showArrow = true,
                showClose = false,
                trigger = {
                    KiraButton(text = "With Arrow", variant = KiraButtonVariant.Outline, onClick = { openArrow = !openArrow })
                },
            ) {}

            KiraPopover(
                open = openOutline,
                onOpenChange = { openOutline = it },
                title = "Outline Style",
                description = "Both content and arrow use the outline variant.",
                variant = KiraPopoverVariant.Outline,
                showArrow = true,
                showClose = false,
                trigger = {
                    KiraButton(text = "Outline Variant", variant = KiraButtonVariant.Outline, onClick = { openOutline = !openOutline })
                },
            ) {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PositionsDemo() {
    var activeSide by remember { mutableStateOf<KiraPopoverSide?>(null) }

    GalleryExample("Positions (Top, Bottom, Left, Right)") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraPopoverSide.entries.forEach { side ->
                KiraPopover(
                    open = activeSide == side,
                    onOpenChange = { open -> activeSide = if (open) side else null },
                    title = "Position: ${side.name}",
                    description = "This popover appears on the ${side.name.lowercase()} of the trigger.",
                    side = side,
                    showArrow = true,
                    showClose = false,
                    trigger = {
                        KiraButton(
                            text = side.name,
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { activeSide = if (activeSide == side) null else side },
                        )
                    },
                ) {}
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlignmentsDemo() {
    var activeAlign by remember { mutableStateOf<KiraPopoverAlign?>(null) }

    GalleryExample("Alignments (Start, Center, End)") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraPopoverAlign.entries.forEach { align ->
                KiraPopover(
                    open = activeAlign == align,
                    onOpenChange = { open -> activeAlign = if (open) align else null },
                    title = "Align: ${align.name}",
                    description = "This popover is aligned to the ${align.name.lowercase()} of the trigger.",
                    side = KiraPopoverSide.Top,
                    align = align,
                    showArrow = true,
                    showClose = false,
                    trigger = {
                        KiraButton(
                            text = align.name,
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { activeAlign = if (activeAlign == align) null else align },
                        )
                    },
                ) {}
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SideOffsetDemo() {
    val offsets = remember { listOf(0 to "No Offset", 8 to "8dp", 16 to "16dp", 24 to "24dp") }
    var activeOffset by remember { mutableStateOf<Int?>(null) }

    GalleryExample("Side Offset (0dp, 8dp, 16dp, 24dp)") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            offsets.forEach { (offset, label) ->
                KiraPopover(
                    open = activeOffset == offset,
                    onOpenChange = { open -> activeOffset = if (open) offset else null },
                    title = "Offset: $label",
                    description = if (offset == 0) {
                        "This popover is directly adjacent to the trigger."
                    } else {
                        "This popover is ${offset}dp away from the trigger."
                    },
                    side = KiraPopoverSide.Bottom,
                    sideOffset = offset.dp,
                    showArrow = true,
                    showClose = false,
                    trigger = {
                        KiraButton(
                            text = label,
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { activeOffset = if (activeOffset == offset) null else offset },
                        )
                    },
                ) {}
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormPopoverDemo() {
    var dimOpen by remember { mutableStateOf(false) }
    var widthVal by remember { mutableStateOf("100%") }
    var heightVal by remember { mutableStateOf("auto") }

    var focusOpen by remember { mutableStateOf(false) }
    var nameVal by remember { mutableStateOf("") }
    val nameFocus = remember { FocusRequester() }

    LaunchedEffect(focusOpen) {
        if (focusOpen) {
            delay(80L)
            runCatching { nameFocus.requestFocus() }
        }
    }

    GalleryExample("Form & Initial Focus") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KiraPopover(
                open = dimOpen,
                onOpenChange = { dimOpen = it },
                title = "Dimensions",
                description = "Set the width and height of your element.",
                size = KiraPopoverSize.Md,
                showClose = false,
                useBottomSheetOnCompact = true,
                trigger = {
                    KiraButton(text = "Edit Dimensions", onClick = { dimOpen = !dimOpen })
                },
                footer = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        KiraButton(text = "Apply", size = KiraButtonSize.Sm, onClick = { dimOpen = false })
                    }
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraLabel(text = "Width", modifier = Modifier.weight(0.35f))
                    KiraInput(value = widthVal, onValueChange = { widthVal = it }, modifier = Modifier.weight(0.65f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KiraLabel(text = "Height", modifier = Modifier.weight(0.35f))
                    KiraInput(value = heightVal, onValueChange = { heightVal = it }, modifier = Modifier.weight(0.65f))
                }
            }

            KiraPopover(
                open = focusOpen,
                onOpenChange = { focusOpen = it },
                title = "Initial Focus",
                description = "Focus automatically moves to the input field when opened.",
                showClose = false,
                useBottomSheetOnCompact = true,
                trigger = {
                    KiraButton(text = "Focus on Input", variant = KiraButtonVariant.Outline, onClick = { focusOpen = !focusOpen })
                },
            ) {
                KiraLabel(text = "Name")
                KiraInput(
                    value = nameVal,
                    onValueChange = { nameVal = it },
                    placeholder = "Enter name…",
                    modifier = Modifier.focusRequester(nameFocus),
                )
                KiraButton(text = "Done", size = KiraButtonSize.Sm, onClick = { focusOpen = false })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomTriggerDemo() {
    val colors = KiraTheme.colors
    var activeTrigger by remember { mutableStateOf<String?>(null) }

    val items = listOf(
        Triple("info", "Info Button", colors.info),
        Triple("settings", "Settings", colors.primary),
        Triple("alert", "Alert", colors.warning),
    )

    GalleryExample("Custom Triggers") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items.forEach { (id, label, tint) ->
                KiraPopover(
                    open = activeTrigger == id,
                    onOpenChange = { open -> activeTrigger = if (open) id else null },
                    title = when (id) {
                        "info" -> "Custom Trigger"
                        "settings" -> "Settings"
                        else -> "Alert"
                    },
                    description = when (id) {
                        "info" -> "This popover uses a custom styled composable as the trigger."
                        "settings" -> "Configure your preferences and options."
                        else -> "Important information about your account."
                    },
                    showArrow = true,
                    showClose = false,
                    trigger = {
                        Row(
                            modifier = Modifier
                                .clip(KiraTheme.radius.shapeMd)
                                .background(tint.copy(alpha = 0.12f))
                                .border(1.dp, tint.copy(alpha = 0.25f), KiraTheme.radius.shapeMd)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(role = Role.Button) {
                                    activeTrigger = if (activeTrigger == id) null else id
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CustomTriggerIcon(kind = id, color = tint)
                            BasicText(
                                text = label,
                                style = KiraTheme.typography.label.copy(
                                    color = tint,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    },
                ) {}
            }
        }
    }
}

@Composable
private fun CustomTriggerIcon(kind: String, color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = color, radius = size.minDimension * 0.42f, style = stroke)
        when (kind) {
            "info" -> {
                drawCircle(color = color, radius = 1.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.30f))
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.46f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
            }
            "settings" -> {
                drawCircle(color = color, radius = size.minDimension * 0.16f, style = stroke)
            }
            else -> {
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.28f), Offset(size.width * 0.5f, size.height * 0.56f), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(color = color, radius = 1.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.72f))
            }
        }
    }
}
