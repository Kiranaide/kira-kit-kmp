package com.kirakit.gallery.contextmenu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kirakit.components.contextmenu.KiraContextMenu
import com.kirakit.components.contextmenu.KiraContextMenuCheckboxItem
import com.kirakit.components.contextmenu.KiraContextMenuItem
import com.kirakit.components.contextmenu.KiraContextMenuLabel
import com.kirakit.components.contextmenu.KiraContextMenuRadioItem
import com.kirakit.components.contextmenu.KiraContextMenuSeparator
import com.kirakit.components.contextmenu.KiraContextMenuSub
import com.kirakit.components.dropdownmenu.KiraMenuItemData
import com.kirakit.components.dropdownmenu.KiraMenuItemVariant
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun LazyListScope.contextMenuPlaygroundItems() {
    galleryItem("ctx-basic") { BasicContextMenuDemo() }
    galleryItem("ctx-submenu") { SubmenuContextMenuDemo() }
    galleryItem("ctx-icons") { IconsContextMenuDemo() }
    galleryItem("ctx-checkboxes") { CheckboxesContextMenuDemo() }
    galleryItem("ctx-radio") { RadioContextMenuDemo() }
    galleryItem("ctx-links") { LinksContextMenuDemo() }
    galleryItem("ctx-disabled") { DisabledReasonsContextMenuDemo() }
    galleryItem("ctx-async") { KiraContextMenuAsyncDemo() }
    galleryItem("ctx-states") { StatesContextMenuDemo() }
}

@Composable
internal fun ContextMenuPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicContextMenuDemo()
        SubmenuContextMenuDemo()
        IconsContextMenuDemo()
        CheckboxesContextMenuDemo()
        RadioContextMenuDemo()
        LinksContextMenuDemo()
        DisabledReasonsContextMenuDemo()
        KiraContextMenuAsyncDemo()
        StatesContextMenuDemo()
    }
}

@Composable
private fun TriggerZone(text: String, modifier: Modifier = Modifier) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            .clip(radius.shapeLg)
            .background(colors.muted.copy(alpha = 0.28f))
            .border(1.dp, colors.border, radius.shapeLg)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = KiraTheme.typography.bodySmall.copy(
                color = colors.foreground,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun BasicContextMenuDemo() {
    var lastAction by remember { mutableStateOf("Right-click, long-press, or press Shift+F10.") }

    GalleryExample("Basic") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraContextMenu(trigger = { TriggerZone("Right click or long press here") }) {
                KiraContextMenuItem(text = "Back", icon = { ArrowLeftGlyph(it) }, shortcut = "⌘[", onClick = { lastAction = "Back" })
                KiraContextMenuItem(text = "Forward", icon = { ArrowRightGlyph(it) }, shortcut = "⌘]", enabled = false, onClick = {})
                KiraContextMenuItem(text = "Reload", icon = { RefreshGlyph(it) }, shortcut = "⌘R", onClick = { lastAction = "Reload" })
                KiraContextMenuSeparator()
                KiraContextMenuItem(text = "Save Page As…", icon = { DownloadGlyph(it) }, shortcut = "⌘S", onClick = { lastAction = "Save Page As…" })
                KiraContextMenuItem(text = "Print…", icon = { FileGlyph(it) }, onClick = { lastAction = "Print…" })
            }
            ExampleNote("Last action: $lastAction")
        }
    }
}

@Composable
private fun SubmenuContextMenuDemo() {
    var lastAction by remember { mutableStateOf("No menu item selected yet.") }

    GalleryExample("Submenu") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraContextMenu(trigger = { TriggerZone("Right click or long press for nested menus") }) {
                KiraContextMenuItem(text = "Open", icon = { FileGlyph(it) }, onClick = { lastAction = "Open" })
                KiraContextMenuItem(text = "Open With", icon = { FolderGlyph(it) }, onClick = { lastAction = "Open With" })
                KiraContextMenuSeparator()
                KiraContextMenuSub(label = "Add to Playlist") {
                    KiraContextMenuItem(text = "Get Up!", onClick = { lastAction = "Added to Get Up!" })
                    KiraContextMenuItem(text = "Inside Out", onClick = { lastAction = "Added to Inside Out" })
                    KiraContextMenuItem(text = "Night Beats", onClick = { lastAction = "Added to Night Beats" })
                    KiraContextMenuSeparator()
                    KiraContextMenuItem(text = "New playlist…", icon = { PlusGlyph(it) }, onClick = { lastAction = "New playlist…" })
                }
                KiraContextMenuSub(label = "Share") {
                    KiraContextMenuItem(text = "Share via Email", icon = { ShareGlyph(it) }, onClick = { lastAction = "Shared via Email" })
                    KiraContextMenuItem(text = "Share on X", icon = { ShareGlyph(it) }, onClick = { lastAction = "Shared on X" })
                    KiraContextMenuItem(text = "Copy Link", icon = { CopyGlyph(it) }, onClick = { lastAction = "Copied Link" })
                }
                KiraContextMenuSeparator()
                KiraContextMenuItem(text = "Add to Library", onClick = { lastAction = "Added to Library" })
                KiraContextMenuItem(text = "Add to Favorites", onClick = { lastAction = "Added to Favorites" })
            }
            ExampleNote(lastAction)
        }
    }
}

@Composable
private fun IconsContextMenuDemo() {
    var lastAction by remember { mutableStateOf("Right click for file options.") }

    GalleryExample("Icons") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraContextMenu(trigger = { TriggerZone("Right click or long press for file options") }) {
                KiraContextMenuItem(text = "New File", icon = { FileGlyph(it) }, shortcut = "⌘N", onClick = { lastAction = "New File" })
                KiraContextMenuItem(text = "New Folder", icon = { FolderGlyph(it) }, shortcut = "⌘⇧N", onClick = { lastAction = "New Folder" })
                KiraContextMenuSeparator()
                KiraContextMenuItem(text = "Copy", icon = { CopyGlyph(it) }, shortcut = "⌘C", onClick = { lastAction = "Copy" })
                KiraContextMenuItem(text = "Cut", icon = { PlusGlyph(it) }, shortcut = "⌘X", onClick = { lastAction = "Cut" })
                KiraContextMenuItem(text = "Paste", icon = { DownloadGlyph(it) }, shortcut = "⌘V", onClick = { lastAction = "Paste" })
            }
            ExampleNote("Last file action: $lastAction")
        }
    }
}

@Composable
private fun CheckboxesContextMenuDemo() {
    var showToolbar by remember { mutableStateOf(true) }
    var showSidebar by remember { mutableStateOf(true) }
    var showStatusBar by remember { mutableStateOf(false) }

    GalleryExample("Checkboxes") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraContextMenu(trigger = { TriggerZone("Right click or long press for view options") }) {
                KiraContextMenuCheckboxItem(text = "Show Toolbar", checked = showToolbar, onCheckedChange = { showToolbar = it })
                KiraContextMenuCheckboxItem(text = "Show Sidebar", checked = showSidebar, onCheckedChange = { showSidebar = it })
                KiraContextMenuCheckboxItem(text = "Show Status Bar", checked = showStatusBar, onCheckedChange = { showStatusBar = it })
            }
            ExampleNote(
                "Status: Toolbar: ${if (showToolbar) "On" else "Off"}, Sidebar: ${if (showSidebar) "On" else "Off"}, Status Bar: ${if (showStatusBar) "On" else "Off"}",
            )
        }
    }
}

@Composable
private fun RadioContextMenuDemo() {
    var sortBy by remember { mutableStateOf("name") }

    GalleryExample("Radio") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraContextMenu(trigger = { TriggerZone("Right click or long press for sorting options") }) {
                KiraContextMenuLabel("Sort By", inset = true)
                KiraContextMenuSeparator()
                listOf("name" to "Name", "date" to "Date Modified", "size" to "Size", "type" to "Type").forEach { (id, label) ->
                    KiraContextMenuRadioItem(text = label, selected = sortBy == id, onClick = { sortBy = id })
                }
            }
            ExampleNote("Sorted by: $sortBy")
        }
    }
}

@Composable
private fun LinksContextMenuDemo() {
    var openedLink by remember { mutableStateOf("No link opened yet.") }

    GalleryExample("Links") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraContextMenu(trigger = { TriggerZone("Documentation links (right click or long press)") }) {
                KiraContextMenuItem(
                    text = "Open Kira Kit docs ↗",
                    icon = { ShareGlyph(it) },
                    onClick = { openedLink = "Opened Kira Kit docs" },
                )
                KiraContextMenuItem(
                    text = "Open Compose Multiplatform docs ↗",
                    icon = { ShareGlyph(it) },
                    onClick = { openedLink = "Opened Compose Multiplatform docs" },
                )
            }
            ExampleNote(openedLink)
        }
    }
}

@Composable
private fun DisabledReasonsContextMenuDemo() {
    GalleryExample("Disabled Reasons") {
        KiraContextMenu(trigger = { TriggerZone("Restricted report actions (right click or long press)") }) {
            KiraContextMenuItem(
                text = "Delete report",
                variant = KiraMenuItemVariant.Destructive,
                enabled = false,
                disabledReason = "Only the owner can delete this report.",
                onClick = {},
            )
        }
    }
}

@Composable
private fun KiraContextMenuAsyncDemo() {
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf("No export yet.") }

    GalleryExample("Kira Context Menu (Async Export & Retry)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraContextMenu(
                expanded = open,
                onExpandedChange = { next ->
                    open = next
                    if (next) errorMsg = null
                },
                errorMessage = errorMsg,
                items = listOf(
                    KiraMenuItemData.Action(
                        id = "export",
                        label = "Export report",
                        icon = { DownloadGlyph(it) },
                        loading = loading,
                        disabled = loading,
                        closeOnClick = false,
                        onSelect = {
                            loading = true
                            errorMsg = null
                            scope.launch {
                                delay(800L)
                                loading = false
                                attempts++
                                if (attempts == 1) {
                                    errorMsg = "Export failed. Select Export report to retry."
                                } else {
                                    result = "Report exported."
                                    open = false
                                }
                            }
                        },
                    ),
                ),
                trigger = {
                    TriggerZone("Right click, long press, or focus and press Shift+F10 to export")
                },
            )
            ExampleNote("$result First attempt fails; retry succeeds. Escape closes.")
        }
    }
}

@Composable
private fun StatesContextMenuDemo() {
    GalleryExample("States (Long Multilingual Labels & Empty Action List)") {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KiraContextMenu(
                maxWidth = 260.dp,
                trigger = { TriggerZone("Long labels, links & disabled explanations") },
            ) {
                KiraContextMenuItem(text = "A very long action label for a report named 年次レポート-2026-🌍", onClick = {})
                KiraContextMenuItem(
                    text = "Restricted action",
                    enabled = false,
                    disabledReason = "Request access from your workspace owner.",
                    onClick = {},
                )
                KiraContextMenuItem(text = "Open documentation ↗", onClick = {})
            }
            KiraContextMenu(
                items = emptyList(),
                trigger = { TriggerZone("Empty action list") },
            )
        }
    }
}

@Composable
private fun ArrowLeftGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(size.width * 0.8f, size.height * 0.5f), Offset(size.width * 0.2f, size.height * 0.5f), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val head = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.25f)
            lineTo(size.width * 0.20f, size.height * 0.50f)
            lineTo(size.width * 0.45f, size.height * 0.75f)
        }
        drawPath(head, color, style = stroke)
    }
}

@Composable
private fun ArrowRightGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val head = Path().apply {
            moveTo(size.width * 0.55f, size.height * 0.25f)
            lineTo(size.width * 0.80f, size.height * 0.50f)
            lineTo(size.width * 0.55f, size.height * 0.75f)
        }
        drawPath(head, color, style = stroke)
    }
}

@Composable
private fun RefreshGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawArc(color, -60f, 270f, false, Offset(size.width * 0.16f, size.height * 0.16f), Size(size.width * 0.68f, size.height * 0.68f), style = stroke)
    }
}

@Composable
private fun DownloadGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(size.width * 0.5f, size.height * 0.16f), Offset(size.width * 0.5f, size.height * 0.66f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        val v = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.48f)
            lineTo(size.width * 0.50f, size.height * 0.70f)
            lineTo(size.width * 0.72f, size.height * 0.48f)
        }
        drawPath(v, color, style = stroke)
        drawLine(color, Offset(size.width * 0.2f, size.height * 0.86f), Offset(size.width * 0.8f, size.height * 0.86f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun FileGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, Offset(size.width * 0.2f, size.height * 0.12f), Size(size.width * 0.6f, size.height * 0.76f), CornerRadius(2.dp.toPx()), style = stroke)
    }
}

@Composable
private fun FolderGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, Offset(size.width * 0.12f, size.height * 0.26f), Size(size.width * 0.76f, size.height * 0.56f), CornerRadius(2.dp.toPx()), style = stroke)
    }
}

@Composable
private fun CopyGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, Offset(size.width * 0.34f, size.height * 0.34f), Size(size.width * 0.52f, size.height * 0.52f), CornerRadius(2.dp.toPx()), style = stroke)
        drawRoundRect(color, Offset(size.width * 0.14f, size.height * 0.14f), Size(size.width * 0.52f, size.height * 0.52f), CornerRadius(2.dp.toPx()), style = stroke)
    }
}

@Composable
private fun ShareGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(color, Offset(size.width * 0.25f, size.height * 0.75f), Offset(size.width * 0.78f, size.height * 0.22f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        val corner = Path().apply {
            moveTo(size.width * 0.42f, size.height * 0.22f)
            lineTo(size.width * 0.78f, size.height * 0.22f)
            lineTo(size.width * 0.78f, size.height * 0.58f)
        }
        drawPath(corner, color, style = stroke)
    }
}

@Composable
private fun PlusGlyph(color: Color) {
    Canvas(Modifier.size(15.dp).clearAndSetSemantics {}) {
        val stroke = 1.6.dp.toPx()
        drawLine(color, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
