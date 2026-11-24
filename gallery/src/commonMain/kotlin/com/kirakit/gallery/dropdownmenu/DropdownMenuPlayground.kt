package com.kirakit.gallery.dropdownmenu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.dropdownmenu.KiraDropdownMenu
import com.kirakit.components.dropdownmenu.KiraDropdownMenuAlign
import com.kirakit.components.dropdownmenu.KiraDropdownMenuCheckboxItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuLabel
import com.kirakit.components.dropdownmenu.KiraDropdownMenuRadioItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuSeparator
import com.kirakit.components.dropdownmenu.KiraDropdownMenuSub
import com.kirakit.components.dropdownmenu.KiraMenuItemData
import com.kirakit.components.dropdownmenu.KiraMenuItemVariant
import com.kirakit.components.dropdownmenu.rememberKiraDropdownMenuHandle
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.gallery.galleryHeading
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun LazyListScope.dropdownMenuPlaygroundItems() {
    galleryHeading("Basic & Triggers")
    galleryItem("dm-basic") { BasicDropdownExample() }
    galleryItem("dm-icon-destructive") { IconTriggerAndDestructiveExample() }
    galleryItem("dm-shortcuts") { ShortcutsExample() }

    galleryHeading("Selection & Submenus")
    galleryItem("dm-checkbox") { CheckboxItemsExample() }
    galleryItem("dm-radio") { RadioGroupExample() }
    galleryItem("dm-submenu") { NestedSubmenuExample() }

    galleryHeading("Async, Backdrop & Arrow")
    galleryItem("dm-async") { LoadingAndAsyncExample() }
    galleryItem("dm-backdrop") { BackdropAndArrowExample() }

    galleryHeading("Data-Driven & Shared Handle")
    galleryItem("dm-data") { DataDrivenWrapperAndSearchExample() }
    galleryItem("dm-shared") { SharedHandleRowsExample() }
}

@Composable
internal fun DropdownMenuPlayground() {
    val gap = KiraTheme.spacing.md

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Triggers")
        BasicDropdownExample()
        IconTriggerAndDestructiveExample()
        ShortcutsExample()

        SectionHeading("Selection & Submenus")
        CheckboxItemsExample()
        RadioGroupExample()
        NestedSubmenuExample()

        SectionHeading("Async, Backdrop & Arrow")
        LoadingAndAsyncExample()
        BackdropAndArrowExample()

        SectionHeading("Data-Driven & Shared Handle")
        DataDrivenWrapperAndSearchExample()
        SharedHandleRowsExample()
    }
}

@Composable
private fun BasicDropdownExample() {
    var expanded by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("No action selected.") }

    GalleryExample("Basic Dropdown Menu") {
        KiraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            trigger = {
                KiraButton(
                    text = "Open Menu",
                    variant = KiraButtonVariant.Outline,
                    onClick = { expanded = !expanded },
                )
            },
        ) {
            KiraDropdownMenuLabel("My Account")
            KiraDropdownMenuSeparator()
            KiraDropdownMenuItem(
                text = "Profile",
                icon = { tint -> UserIcon(tint) },
                onClick = { lastAction = "Profile opened." },
            )
            KiraDropdownMenuItem(
                text = "Settings",
                icon = { tint -> GearIcon(tint) },
                onClick = { lastAction = "Settings opened." },
            )
            KiraDropdownMenuSeparator()
            KiraDropdownMenuItem(
                text = "Logout",
                icon = { tint -> LogoutIcon(tint) },
                onClick = { lastAction = "Signed out." },
            )
        }
        ExampleNote(lastAction)
    }
}

@Composable
private fun IconTriggerAndDestructiveExample() {
    var expanded by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("Choose an item action.") }

    GalleryExample("Icon Trigger & Destructive") {
        KiraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            align = KiraDropdownMenuAlign.End,
            trigger = {
                KiraButton(
                    variant = KiraButtonVariant.Ghost,
                    size = KiraButtonSize.Icon,
                    modifier = Modifier.semantics { contentDescription = "Open actions" },
                    onClick = { expanded = !expanded },
                ) { style ->
                    DotsVerticalIcon(style.color)
                }
            },
        ) {
            KiraDropdownMenuItem("Edit", onClick = { lastAction = "Edit clicked." })
            KiraDropdownMenuItem("Duplicate", onClick = { lastAction = "Duplicate clicked." })
            KiraDropdownMenuItem("Archive", onClick = { lastAction = "Archive clicked." })
            KiraDropdownMenuSeparator()
            KiraDropdownMenuItem(
                text = "Delete",
                variant = KiraMenuItemVariant.Destructive,
                icon = { tint -> TrashIcon(tint) },
                onClick = { lastAction = "Delete requested." },
            )
        }
        ExampleNote(lastAction)
    }
}

@Composable
private fun ShortcutsExample() {
    var expanded by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("Trigger a file action.") }

    GalleryExample("Keyboard Shortcuts") {
        KiraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            trigger = {
                KiraButton(
                    text = "File",
                    variant = KiraButtonVariant.Outline,
                    onClick = { expanded = !expanded },
                )
            },
        ) {
            KiraDropdownMenuItem(
                text = "New File",
                icon = { tint -> PlusIcon(tint) },
                shortcut = "⌘N",
                onClick = { lastAction = "New File created." },
            )
            KiraDropdownMenuItem(
                text = "Duplicate",
                shortcut = "⌘D",
                onClick = { lastAction = "File duplicated." },
            )
            KiraDropdownMenuSeparator()
            KiraDropdownMenuItem(
                text = "Save",
                shortcut = "⌘S",
                onClick = { lastAction = "File saved." },
            )
        }
        ExampleNote(lastAction)
    }
}

@Composable
private fun CheckboxItemsExample() {
    var expanded by remember { mutableStateOf(false) }
    var showToolbar by remember { mutableStateOf(true) }
    var showSidebar by remember { mutableStateOf(false) }

    GalleryExample("Checkbox Items") {
        KiraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            trigger = {
                KiraButton(
                    text = "Preferences",
                    variant = KiraButtonVariant.Outline,
                    onClick = { expanded = !expanded },
                )
            },
        ) {
            KiraDropdownMenuLabel("View Options")
            KiraDropdownMenuSeparator()
            KiraDropdownMenuCheckboxItem(
                text = "Show Toolbar",
                checked = showToolbar,
                onCheckedChange = { showToolbar = it },
            )
            KiraDropdownMenuCheckboxItem(
                text = "Show Sidebar",
                checked = showSidebar,
                onCheckedChange = { showSidebar = it },
            )
            KiraDropdownMenuSeparator()
            KiraDropdownMenuCheckboxItem(
                text = "Disabled Option",
                checked = true,
                enabled = false,
                onCheckedChange = {},
            )
        }
        ExampleNote("Toolbar: $showToolbar · Sidebar: $showSidebar (menu stays open while toggling)")
    }
}

@Composable
private fun RadioGroupExample() {
    var expanded by remember { mutableStateOf(false) }
    var themeChoice by remember { mutableStateOf("light") }

    GalleryExample("Radio Group") {
        KiraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            trigger = {
                KiraButton(
                    text = "Theme: ${themeChoice.replaceFirstChar { it.uppercase() }}",
                    variant = KiraButtonVariant.Outline,
                    onClick = { expanded = !expanded },
                )
            },
        ) {
            KiraDropdownMenuLabel("Appearance")
            KiraDropdownMenuSeparator()
            listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (value, label) ->
                KiraDropdownMenuRadioItem(
                    text = label,
                    selected = themeChoice == value,
                    onClick = { themeChoice = value },
                )
            }
        }
        ExampleNote("Selected appearance: $themeChoice")
    }
}

@Composable
private fun NestedSubmenuExample() {
    var expanded by remember { mutableStateOf(false) }
    var lastAction by remember { mutableStateOf("Open Settings submenu inside More Options.") }

    GalleryExample("Nested Submenu") {
        KiraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            minWidth = 248.dp,
            trigger = {
                KiraButton(
                    text = "More Options",
                    variant = KiraButtonVariant.Outline,
                    onClick = { expanded = !expanded },
                )
            },
        ) {
            KiraDropdownMenuItem("Dashboard", onClick = { lastAction = "Dashboard selected." })
            KiraDropdownMenuItem("Analytics", onClick = { lastAction = "Analytics selected." })
            KiraDropdownMenuSeparator()
            KiraDropdownMenuSub(
                label = "Settings",
                icon = { tint -> GearIcon(tint) },
            ) {
                KiraDropdownMenuLabel("Account")
                KiraDropdownMenuItem("Profile", onClick = { lastAction = "Account → Profile" })
                KiraDropdownMenuItem("Billing", onClick = { lastAction = "Account → Billing" })
                KiraDropdownMenuSeparator()
                KiraDropdownMenuLabel("Security")
                KiraDropdownMenuItem("Password", onClick = { lastAction = "Security → Password" })
                KiraDropdownMenuItem("2FA", onClick = { lastAction = "Security → 2FA" })
            }
            KiraDropdownMenuSeparator()
            KiraDropdownMenuItem(
                text = "Logout",
                variant = KiraMenuItemVariant.Destructive,
                icon = { tint -> LogoutIcon(tint) },
                onClick = { lastAction = "Logged out." },
            )
        }
        ExampleNote(lastAction)
    }
}

@Composable
private fun LoadingAndAsyncExample() {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var publishAttempts by remember { mutableIntStateOf(0) }
    var isPublishing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Run an async action from the menu.") }

    GalleryExample("Loading & Async Actions") {
        KiraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { if (!isExporting && !isPublishing) expanded = false },
            minWidth = 264.dp,
            trigger = {
                KiraButton(
                    text = "Project actions",
                    variant = KiraButtonVariant.Outline,
                    onClick = { expanded = !expanded },
                )
            },
        ) {
            KiraDropdownMenuItem(
                text = if (isExporting) "Exporting report..." else "Export report",
                shortcut = "⌘E",
                loading = isExporting,
                closeOnClick = false,
                onClick = {
                    isExporting = true
                    status = "Exporting report..."
                    scope.launch {
                        delay(1200L)
                        isExporting = false
                        expanded = false
                        status = "Report exported."
                    }
                },
            )
            KiraDropdownMenuItem(
                text = if (isPublishing) "Publishing..." else "Publish (fails first)",
                loading = isPublishing,
                closeOnClick = false,
                onClick = {
                    isPublishing = true
                    status = "Publishing..."
                    scope.launch {
                        delay(800L)
                        publishAttempts++
                        isPublishing = false
                        status = if (publishAttempts == 1) {
                            "Could not publish. Try again."
                        } else {
                            "Published successfully on attempt #$publishAttempts."
                        }
                    }
                },
            )
            KiraDropdownMenuItem(
                text = "Rename (sync)",
                onClick = { status = "Renamed." },
            )
            KiraDropdownMenuSeparator()
            KiraDropdownMenuItem(
                text = "Delete project",
                variant = KiraMenuItemVariant.Destructive,
                icon = { tint -> TrashIcon(tint) },
                enabled = false,
                disabledReason = "Deleting is disabled in the demo workspace.",
                onClick = {},
            )
        }
        ExampleNote(status)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackdropAndArrowExample() {
    var showBackdropMenu by remember { mutableStateOf(false) }
    var showArrowMenu by remember { mutableStateOf(false) }
    var lastStatus by remember { mutableStateOf("Try backdrop modal menu or arrow pointer menu.") }

    GalleryExample("Backdrop Overlay & Menu with Arrow") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraDropdownMenu(
                expanded = showBackdropMenu,
                onDismissRequest = { showBackdropMenu = false },
                showBackdrop = true,
                minWidth = 260.dp,
                trigger = {
                    KiraButton(
                        text = "Modal Menu",
                        variant = KiraButtonVariant.Outline,
                        onClick = { showBackdropMenu = !showBackdropMenu },
                    )
                },
            ) {
                KiraDropdownMenuLabel("Confirm Action")
                KiraDropdownMenuItem(
                    text = "This menu has a backdrop overlay. Click outside to close.",
                    closeOnClick = false,
                    onClick = {},
                )
                KiraDropdownMenuSeparator()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    KiraButton(
                        text = "Cancel",
                        variant = KiraButtonVariant.Ghost,
                        size = KiraButtonSize.Sm,
                        onClick = {
                            showBackdropMenu = false
                            lastStatus = "Cancelled."
                        },
                    )
                    KiraButton(
                        text = "Confirm",
                        size = KiraButtonSize.Sm,
                        onClick = {
                            showBackdropMenu = false
                            lastStatus = "Confirmed."
                        },
                    )
                }
            }

            KiraDropdownMenu(
                expanded = showArrowMenu,
                onDismissRequest = { showArrowMenu = false },
                showArrow = true,
                align = KiraDropdownMenuAlign.Center,
                trigger = {
                    KiraButton(
                        text = "Arrow Menu",
                        variant = KiraButtonVariant.Outline,
                        onClick = { showArrowMenu = !showArrowMenu },
                    )
                },
            ) {
                KiraDropdownMenuItem("Item 1", onClick = { lastStatus = "Arrow Item 1" })
                KiraDropdownMenuItem("Item 2", onClick = { lastStatus = "Arrow Item 2" })
                KiraDropdownMenuItem("Item 3", onClick = { lastStatus = "Arrow Item 3" })
            }
        }
        ExampleNote(lastStatus)
    }
}

@Composable
private fun DataDrivenWrapperAndSearchExample() {
    var selected by remember { mutableStateOf("No action selected.") }

    val items = remember {
        listOf(
            KiraMenuItemData.Action(
                id = "copy",
                label = "Copy link",
                onSelect = { selected = "Link copied." },
            ),
            KiraMenuItemData.Action(
                id = "rename",
                label = "Rename project",
                onSelect = { selected = "Project renamed." },
            ),
            KiraMenuItemData.Submenu(
                id = "export",
                label = "Export",
                items = listOf(
                    KiraMenuItemData.Action("pdf", "Export PDF", onSelect = { selected = "PDF exported." }),
                    KiraMenuItemData.Action("csv", "Export CSV", onSelect = { selected = "CSV exported." }),
                ),
            ),
            KiraMenuItemData.Separator("sep-1"),
            KiraMenuItemData.Action(
                id = "delete",
                label = "Delete project",
                variant = KiraMenuItemVariant.Destructive,
                disabled = true,
                disabledReason = "Only workspace owners can delete projects.",
            ),
        )
    }

    GalleryExample("Searchable KiraDropdownMenu & Disabled Reasons") {
        KiraDropdownMenu(
            items = items,
            searchable = true,
            searchPlaceholder = "Filter actions…",
            trigger = { onToggle ->
                KiraButton(
                    text = "Search actions",
                    variant = KiraButtonVariant.Outline,
                    onClick = onToggle,
                )
            },
        )
        ExampleNote(selected)
    }
}

private data class DeploymentRow(val id: String, val name: String, val env: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SharedHandleRowsExample() {
    val initialRows = remember {
        listOf(
            DeploymentRow("atlas", "Atlas", "production"),
            DeploymentRow("boreal", "Boreal", "staging"),
        )
    }
    var rows by remember { mutableStateOf(initialRows) }
    var resolved by remember { mutableStateOf("none") }
    val handle = rememberKiraDropdownMenuHandle<DeploymentRow>()

    GalleryExample("Shared Menu Handle (Row Payloads)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExampleNote("Last resolved: $resolved")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rows.forEach { row ->
                    KiraDropdownMenu(
                        expanded = handle.payload?.id == row.id,
                        onDismissRequest = { handle.close() },
                        trigger = {
                            KiraButton(
                                text = "${row.name} (${row.env})",
                                variant = KiraButtonVariant.Outline,
                                size = KiraButtonSize.Sm,
                                onClick = { handle.open(row) },
                            )
                        },
                    ) {
                        val activeRow = handle.payload ?: row
                        KiraDropdownMenuLabel("${activeRow.name} · ${activeRow.env}")
                        KiraDropdownMenuItem(
                            text = "Open ${activeRow.name}",
                            onClick = { resolved = "Opened ${activeRow.name}" },
                        )
                        KiraDropdownMenuSeparator()
                        KiraDropdownMenuItem(
                            text = "Archive ${activeRow.name}",
                            variant = KiraMenuItemVariant.Destructive,
                            icon = { tint -> TrashIcon(tint) },
                            onClick = {
                                rows = rows.filterNot { it.id == activeRow.id }
                                resolved = "Archived ${activeRow.name}"
                            },
                        )
                    }
                }
                if (rows.size < initialRows.size) {
                    KiraButton(
                        text = "Restore rows",
                        variant = KiraButtonVariant.Ghost,
                        size = KiraButtonSize.Sm,
                        onClick = {
                            rows = initialRows
                            resolved = "none"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UserIcon(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = size.minDimension * 0.20f, center = Offset(size.width * 0.5f, size.height * 0.30f), style = stroke)
        drawArc(color, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(size.width * 0.16f, size.height * 0.54f), size = Size(size.width * 0.68f, size.height * 0.52f), style = stroke)
    }
}

@Composable
private fun GearIcon(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = size.minDimension * 0.36f, style = stroke)
        drawCircle(color, radius = size.minDimension * 0.13f, style = stroke)
    }
}

@Composable
private fun LogoutIcon(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = 1.5.dp.toPx()
        drawLine(color, Offset(size.width * 0.22f, size.height * 0.22f), Offset(size.width * 0.22f, size.height * 0.78f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.42f, size.height * 0.50f), Offset(size.width * 0.82f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.64f, size.height * 0.32f), Offset(size.width * 0.82f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.64f, size.height * 0.68f), Offset(size.width * 0.82f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun TrashIcon(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = 1.5.dp.toPx()
        drawLine(color, Offset(size.width * 0.20f, size.height * 0.28f), Offset(size.width * 0.80f, size.height * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawRoundRect(color, topLeft = Offset(size.width * 0.26f, size.height * 0.28f), size = Size(size.width * 0.48f, size.height * 0.54f), cornerRadius = CornerRadius(2.dp.toPx()), style = Stroke(stroke))
    }
}

@Composable
private fun PlusIcon(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = 1.6.dp.toPx()
        drawLine(color, Offset(size.width * 0.22f, size.height * 0.50f), Offset(size.width * 0.78f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.50f, size.height * 0.22f), Offset(size.width * 0.50f, size.height * 0.78f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun DotsVerticalIcon(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val r = 1.4.dp.toPx()
        val cx = size.width * 0.50f
        drawCircle(color, radius = r, center = Offset(cx, size.height * 0.22f))
        drawCircle(color, radius = r, center = Offset(cx, size.height * 0.50f))
        drawCircle(color, radius = r, center = Offset(cx, size.height * 0.78f))
    }
}
