package com.kirakit.gallery.tabs

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kirakit.components.card.KiraCard
import com.kirakit.components.card.KiraCardSize
import com.kirakit.components.tabs.KiraTabItem
import com.kirakit.components.tabs.KiraTabs
import com.kirakit.components.tabs.KiraTabsColor
import com.kirakit.components.tabs.KiraTabsOrientation
import com.kirakit.components.tabs.KiraTabsVariant
import com.kirakit.components.tabs.resolveNextTabAfterClose
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@Composable
internal fun TabsPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic")
        GalleryExample("Default Tabs with Card Panels") {
            var basicSelected by remember { mutableStateOf("overview") }
            val basicTabs = remember {
                listOf(
                    KiraTabItem("overview", "Overview") {
                        TabCardPanel(
                            title = "Overview",
                            subtitle = "The default variant renders a muted track with a raised active tab.",
                            body = "Panels fade in smoothly across Android, iOS, and Desktop.",
                        )
                    },
                    KiraTabItem("analytics", "Analytics") {
                        TabCardPanel(
                            title = "Analytics",
                            subtitle = "Second panel.",
                            body = "Use KiraTabs with any Composable panel content.",
                        )
                    },
                    KiraTabItem("settings", "Settings") {
                        TabCardPanel(
                            title = "Settings",
                            subtitle = "Third panel.",
                            body = "Colors, variants, and orientation flow through a single component.",
                        )
                    },
                )
            }
            KiraTabs(
                tabs = basicTabs,
                selectedValue = basicSelected,
                onSelectedValueChange = { basicSelected = it },
            )
        }

        SectionHeading("Variants")
        GalleryExample("All 7 Visual Variants") {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                KiraTabsVariant.entries.forEach { variant ->
                    var sel by remember(variant) { mutableStateOf("one") }
                    val items = remember(variant) {
                        listOf(
                            KiraTabItem("one", "One") { TabPanelCopy("${variant.name.lowercase()} panel.") },
                            KiraTabItem("two", "Two") { TabPanelCopy("${variant.name.lowercase()} panel 2.") },
                            KiraTabItem("three", "Disabled", disabled = true) { TabPanelCopy("Disabled.") },
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MonoCaption("variant = ${variant.name}")
                        KiraTabs(
                            tabs = items,
                            selectedValue = sel,
                            onSelectedValueChange = { sel = it },
                            variant = variant,
                        )
                    }
                }
            }
        }

        SectionHeading("Semantic Colors")
        GalleryExample("All 5 Theme-Aware Colors (Pills)") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                KiraTabsColor.entries.forEach { color ->
                    var sel by remember(color) { mutableStateOf("one") }
                    val items = remember(color) {
                        listOf(
                            KiraTabItem("one", "Active") {
                                TabPanelCopy("${color.name.lowercase()} panel — semantic token from KiraTheme.")
                            },
                            KiraTabItem("two", "Inactive") {
                                TabPanelCopy("Second panel for ${color.name.lowercase()}.")
                            },
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MonoCaption("color = ${color.name}")
                        KiraTabs(
                            tabs = items,
                            selectedValue = sel,
                            onSelectedValueChange = { sel = it },
                            variant = KiraTabsVariant.Pills,
                            color = color,
                        )
                    }
                }
            }
        }

        SectionHeading("Segment & Vertical")
        GalleryExample("Sliding Pill Segment") {
            var seg by remember { mutableStateOf("day") }
            val segTabs = remember {
                listOf(
                    KiraTabItem("day", "Day") {
                        TabPanelCopy("The active-tab pill slides smoothly and interrupts cleanly when tapping another tab mid-flight.")
                    },
                    KiraTabItem("week", "Week") {
                        TabPanelCopy("Rapid multi-click: the pill lands cleanly on the last selected tab.")
                    },
                    KiraTabItem("month", "Month") {
                        TabPanelCopy("Segment works with every color theme (the pill takes the token color).")
                    },
                )
            }
            KiraTabs(
                tabs = segTabs,
                selectedValue = seg,
                onSelectedValueChange = { seg = it },
                variant = KiraTabsVariant.Segment,
                color = KiraTabsColor.Primary,
            )
        }

        GalleryExample("Vertical Orientation") {
            var vert by remember { mutableStateOf("account") }
            val vertTabs = remember {
                listOf(
                    KiraTabItem("account", "Account") {
                        TabPanelCopy("Vertical list; underline vertical draws the marker on the inline-end edge.")
                    },
                    KiraTabItem("profile", "Profile") { TabPanelCopy("Profile panel.") },
                    KiraTabItem("notifications", "Notifications") { TabPanelCopy("Notifications panel.") },
                )
            }
            KiraTabs(
                tabs = vertTabs,
                selectedValue = vert,
                onSelectedValueChange = { vert = it },
                variant = KiraTabsVariant.Underline,
                orientation = KiraTabsOrientation.Vertical,
            )
        }

        SectionHeading("Icons, Badges & Closable")
        GalleryExample("Leading Icons, Badges & Closable Triggers") {
            var iconSelected by remember { mutableStateOf("home") }
            var iconTabs by remember {
                mutableStateOf(
                    listOf(
                        KiraTabItem(
                            value = "home",
                            label = "Home",
                            icon = { HomeGlyph(colors.foreground) },
                        ) { TabPanelCopy("Icons, badges, and closable triggers.") },
                        KiraTabItem(
                            value = "inbox",
                            label = "Inbox",
                            icon = { BellGlyph(colors.foreground) },
                            badge = "5",
                        ) { TabPanelCopy("Inbox panel.") },
                        KiraTabItem(
                            value = "profile",
                            label = "Profile",
                            icon = { UserGlyph(colors.foreground) },
                            badge = "new",
                        ) { TabPanelCopy("Profile panel.") },
                        KiraTabItem(
                            value = "settings",
                            label = "Settings",
                            icon = { GearGlyph(colors.foreground) },
                            closable = true,
                        ) { TabPanelCopy("Settings panel.") },
                    ),
                )
            }
            KiraTabs(
                tabs = iconTabs,
                selectedValue = iconSelected,
                onSelectedValueChange = { iconSelected = it },
                onCloseTab = { closing ->
                    val next = resolveNextTabAfterClose(iconTabs, closing, iconSelected)
                    iconTabs = iconTabs.filter { it.value != closing }
                    if (next != null) iconSelected = next
                },
            )
        }

        SectionHeading("Scrollable & Dynamic Editor")
        GalleryExample("Scrollable Tabs (Constrained 320dp)") {
            val reportKeys = remember {
                listOf("dashboard", "reports", "billing", "team", "integrations", "api-keys", "webhooks", "audit-log")
            }
            var scrollSel by remember { mutableStateOf("dashboard") }
            val scrollTabs = remember(reportKeys) {
                reportKeys.map { key ->
                    KiraTabItem(key, key) {
                        TabPanelCopy("Panel for $key. Edge chevrons appear while tabs overflow and hide at the bounds.")
                    }
                }
            }
            Box(Modifier.widthIn(max = 320.dp)) {
                KiraTabs(
                    tabs = scrollTabs,
                    selectedValue = scrollSel,
                    onSelectedValueChange = { scrollSel = it },
                    scrollable = true,
                )
            }
        }

        GalleryExample("File Editor (Closable + Addable)") {
            var nextId by remember { mutableIntStateOf(3) }
            var editorTabs by remember {
                mutableStateOf(
                    listOf(
                        KiraTabItem("index", "index.ts", closable = true) {
                            CodeSnippet("export const one = 1")
                        },
                        KiraTabItem("utils", "utils.ts", closable = true) {
                            CodeSnippet("export const two = 2")
                        },
                        KiraTabItem("types", "types.d.ts", closable = true) {
                            CodeSnippet("export type two = number")
                        },
                    ),
                )
            }
            var activeFile by remember { mutableStateOf("index") }

            KiraTabs(
                tabs = editorTabs,
                selectedValue = activeFile,
                onSelectedValueChange = { activeFile = it },
                variant = KiraTabsVariant.Card,
                scrollable = true,
                onCloseTab = { closing ->
                    val next = resolveNextTabAfterClose(editorTabs, closing, activeFile)
                    editorTabs = editorTabs.filter { it.value != closing }
                    if (next != null) activeFile = next
                },
                onAddTab = {
                    val id = nextId++
                    val newTab = KiraTabItem(
                        value = "file-$id",
                        label = "module-$id.ts",
                        closable = true,
                    ) {
                        CodeSnippet("export const module = $id")
                    }
                    editorTabs = editorTabs + newTab
                    activeFile = newTab.value
                },
            )
        }

        SectionHeading("Stress Cases")
        GalleryExample("Long Label, Empty Panel & Icon-Only") {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonoCaption("Very long label — truncates gracefully")
                    var longSel by remember { mutableStateOf("long") }
                    val longTabs = remember {
                        listOf(
                            KiraTabItem("long", "An extremely long tab label that must truncate gracefully") {
                                TabPanelCopy("Truncated label panel.")
                            },
                            KiraTabItem("short", "Short") { TabPanelCopy("Short panel.") },
                        )
                    }
                    Box(Modifier.widthIn(max = 360.dp)) {
                        KiraTabs(
                            tabs = longTabs,
                            selectedValue = longSel,
                            onSelectedValueChange = { longSel = it },
                            scrollable = true,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonoCaption("Empty panel state")
                    var emptySel by remember { mutableStateOf("unassigned") }
                    val emptyTabs = remember {
                        listOf(
                            KiraTabItem("unassigned", "Unassigned reports") {
                                Box(Modifier.fillMaxWidth().heightIn(min = 48.dp))
                            },
                            KiraTabItem("resolved", "Resolved reports") {
                                TabPanelCopy("Resolved reports remain available here.")
                            },
                        )
                    }
                    KiraTabs(
                        tabs = emptyTabs,
                        selectedValue = emptySel,
                        onSelectedValueChange = { emptySel = it },
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonoCaption("Icon-only triggers")
                    var iconOnlySel by remember { mutableStateOf("a") }
                    val iconOnlyTabs = remember {
                        listOf(
                            KiraTabItem("a", "", icon = { HomeGlyph(colors.foreground) }) {
                                TabPanelCopy("Home panel.")
                            },
                            KiraTabItem("b", "", icon = { BellGlyph(colors.foreground) }) {
                                TabPanelCopy("Bell panel.")
                            },
                        )
                    }
                    KiraTabs(
                        tabs = iconOnlyTabs,
                        selectedValue = iconOnlySel,
                        onSelectedValueChange = { iconOnlySel = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun TabCardPanel(title: String, subtitle: String, body: String) {
    val colors = KiraTheme.colors
    KiraCard(Modifier.fillMaxWidth(), size = KiraCardSize.Sm) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                text = title,
                style = KiraTheme.typography.heading3.copy(color = colors.cardForeground),
            )
            BasicText(
                text = subtitle,
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
            BasicText(
                text = body,
                modifier = Modifier.padding(top = 6.dp),
                style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
            )
        }
    }
}

@Composable
private fun TabPanelCopy(text: String) {
    BasicText(
        text = text,
        style = KiraTheme.typography.body.copy(color = KiraTheme.colors.mutedForeground),
    )
}

@Composable
private fun MonoCaption(text: String) {
    BasicText(
        text = text,
        style = KiraTheme.typography.caption.copy(
            color = KiraTheme.colors.mutedForeground,
            fontFamily = FontFamily.Monospace,
        ),
    )
}

@Composable
private fun CodeSnippet(code: String) {
    val colors = KiraTheme.colors
    val shape = KiraTheme.radius.shapeMd
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.muted.copy(alpha = 0.35f), shape)
            .border(1.dp, colors.border, shape)
            .padding(12.dp),
    ) {
        BasicText(
            text = code,
            style = KiraTheme.typography.bodySmall.copy(
                color = colors.foreground,
                fontFamily = FontFamily.Monospace,
            ),
        )
    }
}

@Composable
private fun HomeGlyph(color: Color) {
    Canvas(Modifier.size(15.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.50f)
            lineTo(size.width * 0.50f, size.height * 0.16f)
            lineTo(size.width * 0.86f, size.height * 0.50f)
            moveTo(size.width * 0.24f, size.height * 0.44f)
            lineTo(size.width * 0.24f, size.height * 0.84f)
            lineTo(size.width * 0.76f, size.height * 0.84f)
            lineTo(size.width * 0.76f, size.height * 0.44f)
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun BellGlyph(color: Color) {
    Canvas(Modifier.size(15.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.72f)
            lineTo(size.width * 0.78f, size.height * 0.72f)
            lineTo(size.width * 0.72f, size.height * 0.58f)
            lineTo(size.width * 0.72f, size.height * 0.40f)
            moveTo(size.width * 0.28f, size.height * 0.40f)
            lineTo(size.width * 0.28f, size.height * 0.58f)
            lineTo(size.width * 0.22f, size.height * 0.72f)
        }
        drawPath(path, color, style = stroke)
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.28f, size.height * 0.18f),
            size = Size(size.width * 0.44f, size.height * 0.44f),
            style = stroke,
        )
        drawCircle(color, radius = 1.1.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.85f))
    }
}

@Composable
private fun UserGlyph(color: Color) {
    Canvas(Modifier.size(15.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.20f,
            center = Offset(size.width * 0.5f, size.height * 0.30f),
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.16f, size.height * 0.54f),
            size = Size(size.width * 0.68f, size.height * 0.52f),
            style = stroke,
        )
    }
}

@Composable
private fun GearGlyph(color: Color) {
    Canvas(Modifier.size(15.dp)) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = size.minDimension * 0.22f, style = stroke)
        drawCircle(color, radius = size.minDimension * 0.38f, style = stroke)
    }
}
