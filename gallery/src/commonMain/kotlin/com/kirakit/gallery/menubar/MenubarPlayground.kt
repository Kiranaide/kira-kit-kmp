package com.kirakit.gallery.menubar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.kirakit.components.dropdownmenu.KiraDropdownMenuCheckboxItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuLabel
import com.kirakit.components.dropdownmenu.KiraDropdownMenuRadioItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuSeparator
import com.kirakit.components.dropdownmenu.KiraDropdownMenuSub
import com.kirakit.components.dropdownmenu.KiraMenuItemData
import com.kirakit.components.dropdownmenu.KiraMenuItemVariant
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.menubar.KiraMenubar
import com.kirakit.components.menubar.KiraMenubarDensity
import com.kirakit.components.menubar.KiraMenubarMenu
import com.kirakit.components.menubar.KiraMenubarMenuData
import com.kirakit.components.menubar.KiraMenubarVariant
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

internal fun LazyListScope.menubarPlaygroundItems() {
    galleryHeading("Basic & Variants")
    galleryItem("mb-basic") { BasicMenubarExample() }
    galleryItem("mb-variants") { VariantsAndDensityExample() }

    galleryHeading("Items, Preferences & Submenus")
    galleryItem("mb-items") { ItemsAndShortcutsExample() }
    galleryItem("mb-prefs") { PreferencesExample() }
    galleryItem("mb-submenu") { SubmenuExample() }

    galleryHeading("Command Search, Data-Driven & States")
    galleryItem("mb-search") { CommandSearchExample() }
    galleryItem("mb-async") { KiraMenubarAsyncExample() }
    galleryItem("mb-states") { StatesExample() }
}

@Composable
internal fun MenubarPlayground() {
    val gap = KiraTheme.spacing.md

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Variants")
        BasicMenubarExample()
        VariantsAndDensityExample()

        SectionHeading("Items, Preferences & Submenus")
        ItemsAndShortcutsExample()
        PreferencesExample()
        SubmenuExample()

        SectionHeading("Command Search, Data-Driven & States")
        CommandSearchExample()
        KiraMenubarAsyncExample()
        StatesExample()
    }
}

@Composable
private fun BasicMenubarExample() {
    var notice by remember { mutableStateOf("Click File or Edit, then hover across triggers to switch menus.") }

    GalleryExample("Basic Menubar") {
        KiraMenubar(ariaLabel = "File actions") {
            KiraMenubarMenu(id = "file", label = "File") {
                KiraDropdownMenuLabel("File")
                KiraDropdownMenuItem("New document", onClick = { notice = "New document selected." })
                KiraDropdownMenuItem("Open…", onClick = { notice = "Open selected." })
                KiraDropdownMenuItem("Close", onClick = { notice = "Close selected." })
            }
            KiraMenubarMenu(id = "edit", label = "Edit") {
                KiraDropdownMenuLabel("Edit")
                KiraDropdownMenuItem("Undo", onClick = { notice = "Undo selected." })
                KiraDropdownMenuItem("Redo", onClick = { notice = "Redo selected." })
            }
        }
        ExampleNote(notice)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VariantsAndDensityExample() {
    GalleryExample("Variants & Density") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("default")
                KiraMenubar(variant = KiraMenubarVariant.Default, ariaLabel = "Default menubar") {
                    KiraMenubarMenu(id = "f-def", label = "File") {
                        KiraDropdownMenuItem("New", onClick = {})
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("outline")
                KiraMenubar(variant = KiraMenubarVariant.Outline, ariaLabel = "Outline menubar") {
                    KiraMenubarMenu(id = "f-out", label = "File") {
                        KiraDropdownMenuItem("New", onClick = {})
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("soft · compact")
                KiraMenubar(
                    variant = KiraMenubarVariant.Soft,
                    density = KiraMenubarDensity.Compact,
                    ariaLabel = "Soft compact menubar",
                ) {
                    KiraMenubarMenu(id = "f-soft", label = "File") {
                        KiraDropdownMenuItem("New", onClick = {})
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsAndShortcutsExample() {
    var notice by remember { mutableStateOf("Inspect icons, shortcuts, disabledReason, and destructive rows.") }

    GalleryExample("Items, Shortcuts & Disabled Reasons") {
        KiraMenubar(ariaLabel = "Document items") {
            KiraMenubarMenu(id = "file-items", label = "File") {
                KiraDropdownMenuItem(
                    text = "New document",
                    icon = { tint -> PlusGlyph(tint) },
                    shortcut = "Ctrl+N",
                    onClick = { notice = "New document created." },
                )
                KiraDropdownMenuItem(
                    text = "Save draft",
                    icon = { tint -> SaveGlyph(tint) },
                    shortcut = "Ctrl+S",
                    onClick = { notice = "Draft saved." },
                )
                KiraDropdownMenuItem(
                    text = "Publish",
                    enabled = false,
                    disabledReason = "Only workspace owners can publish documents.",
                    onClick = {},
                )
                KiraDropdownMenuSeparator()
                KiraDropdownMenuItem(
                    text = "Delete document",
                    variant = KiraMenuItemVariant.Destructive,
                    onClick = { notice = "Document deleted." },
                )
            }
        }
        ExampleNote(notice)
    }
}

@Composable
private fun PreferencesExample() {
    var details by remember { mutableStateOf(true) }
    var density by remember { mutableStateOf("comfortable") }

    GalleryExample("View Preferences (Checkbox & Radio)") {
        KiraMenubar(ariaLabel = "View preferences") {
            KiraMenubarMenu(id = "view", label = "View") {
                KiraDropdownMenuCheckboxItem(
                    text = "Document details",
                    checked = details,
                    onCheckedChange = { details = it },
                )
                KiraDropdownMenuSeparator()
                KiraDropdownMenuLabel("Menu density")
                KiraDropdownMenuRadioItem(
                    text = "Comfortable",
                    selected = density == "comfortable",
                    onClick = { density = "comfortable" },
                )
                KiraDropdownMenuRadioItem(
                    text = "Compact",
                    selected = density == "compact",
                    onClick = { density = "compact" },
                )
            }
        }
        ExampleNote("details: $details · density: $density")
    }
}

@Composable
private fun SubmenuExample() {
    var notice by remember { mutableStateOf("Nothing exported yet.") }

    GalleryExample("Submenus & Disabled Reasons") {
        KiraMenubar(ariaLabel = "Export commands") {
            KiraMenubarMenu(id = "export-file", label = "File") {
                KiraDropdownMenuSub(label = "Export") {
                    KiraDropdownMenuItem(
                        text = "Markdown",
                        onClick = { notice = "Markdown export selected." },
                    )
                    KiraDropdownMenuItem(
                        text = "Plain text",
                        onClick = { notice = "Plain text export selected." },
                    )
                }
                KiraDropdownMenuItem(
                    text = "Print document",
                    enabled = false,
                    disabledReason = "Save a draft before printing.",
                    onClick = {},
                )
            }
        }
        ExampleNote(notice)
    }
}

@Composable
private fun CommandSearchExample() {
    var notice by remember { mutableStateOf("Open Commands (or press Ctrl/⌘ K) to search actions across every menu.") }
    val menus = remember {
        listOf(
            KiraMenubarMenuData(
                id = "workspace",
                label = "Workspace",
                searchable = true,
                items = listOf(
                    KiraMenuItemData.Action("new", "New document", onSelect = { notice = "New document" }),
                    KiraMenuItemData.Action("copy", "Copy link", onSelect = { notice = "Link copied" }),
                    KiraMenuItemData.Action("archive", "Archive project", onSelect = { notice = "Project archived" }),
                ),
            ),
            KiraMenubarMenuData(
                id = "settings",
                label = "Settings",
                items = listOf(
                    KiraMenuItemData.Action("members", "Manage members", onSelect = { notice = "Manage members" }),
                    KiraMenuItemData.Action("billing", "Billing & plans", onSelect = { notice = "Billing & plans" }),
                ),
            ),
        )
    }

    GalleryExample("Command Search (Ctrl/⌘ K)") {
        KiraMenubar(
            menus = menus,
            commandSearch = true,
            ariaLabel = "Workspace commands",
        )
        ExampleNote(notice)
    }
}

@Composable
private fun KiraMenubarAsyncExample() {
    val scope = rememberCoroutineScope()
    var notice by remember { mutableStateOf("No command run yet.") }
    var saving by remember { mutableStateOf(false) }

    val menus = listOf(
        KiraMenubarMenuData(
            id = "file",
            label = "File",
            items = listOf(
                KiraMenuItemData.Action(
                    id = "new",
                    label = "New document",
                    icon = { tint -> PlusGlyph(tint) },
                    onSelect = { notice = "New document ready." },
                ),
                KiraMenuItemData.Action(
                    id = "save",
                    label = if (saving) "Saving draft..." else "Save draft",
                    icon = { tint -> SaveGlyph(tint) },
                    loading = saving,
                    closeOnClick = false,
                    onSelect = {
                        saving = true
                        notice = "Saving draft..."
                        scope.launch {
                            delay(1200L)
                            saving = false
                            notice = "Draft saved on this device."
                        }
                    },
                ),
            ),
        ),
        KiraMenubarMenuData(
            id = "edit",
            label = "Edit",
            items = listOf(
                KiraMenuItemData.Action(
                    id = "copy",
                    label = "Copy as text",
                    onSelect = { notice = "Document copied." },
                ),
                KiraMenuItemData.Action(
                    id = "undo",
                    label = "Undo new document",
                    onSelect = { notice = "Previous document restored." },
                ),
            ),
        ),
    )

    GalleryExample("Data-Driven KiraMenubar & Async Actions") {
        KiraMenubar(
            menus = menus,
            ariaLabel = "Document commands",
        )
        ExampleNote(notice)
    }
}

@Composable
private fun StatesExample() {
    val menus = remember {
        listOf(
            KiraMenubarMenuData(
                id = "workspace",
                label = "Workspace",
                searchable = true,
                items = listOf(
                    KiraMenuItemData.Action(
                        id = "long",
                        label = "Export the complete November release notes and review history",
                    ),
                    KiraMenuItemData.Action(
                        id = "restricted",
                        label = "Transfer ownership",
                        disabled = true,
                        disabledReason = "Only the current owner can transfer this workspace.",
                    ),
                ),
            ),
        )
    }

    GalleryExample("States (Long Labels, Read-Only & Empty)") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Long labels and permissions")
                KiraMenubar(menus = menus, commandSearch = true)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Read-only workspace")
                KiraMenubar(menus = menus, enabled = false, ariaLabel = "Read-only commands")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("No available commands")
                KiraMenubar(menus = emptyList())
            }
        }
    }
}

@Composable
private fun PlusGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = 1.6.dp.toPx()
        drawLine(color, Offset(size.width * 0.22f, size.height * 0.50f), Offset(size.width * 0.78f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.50f, size.height * 0.22f), Offset(size.width * 0.50f, size.height * 0.78f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun SaveGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.16f, size.height * 0.16f),
            size = Size(size.width * 0.68f, size.height * 0.68f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = stroke,
        )
        drawRect(
            color = color,
            topLeft = Offset(size.width * 0.34f, size.height * 0.16f),
            size = Size(size.width * 0.32f, size.height * 0.22f),
            style = stroke,
        )
    }
}
