package com.kirakit.gallery.navigationmenu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.navigationmenu.KiraNavigationItemData
import com.kirakit.components.navigationmenu.KiraNavigationMenu
import com.kirakit.components.navigationmenu.KiraNavigationMenuFooter
import com.kirakit.components.navigationmenu.KiraNavigationMenuHeader
import com.kirakit.components.navigationmenu.KiraNavigationMenuLink
import com.kirakit.components.navigationmenu.KiraNavigationMenuLinkCard
import com.kirakit.components.navigationmenu.KiraNavigationMenuPanel
import com.kirakit.components.navigationmenu.KiraNavigationMenuSection
import com.kirakit.components.navigationmenu.KiraNavigationMenuTrigger
import com.kirakit.components.navigationmenu.KiraNavigationMobileMode
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.gallery.galleryHeading
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

internal fun LazyListScope.navigationMenuPlaygroundItems() {
    galleryHeading("Basic & Active State")
    galleryItem("nm-basic") { BasicNavigationExample() }
    galleryItem("nm-active") { ActiveLinkExample() }

    galleryHeading("Mega Panel & Nested Navigation")
    galleryItem("nm-mega") { MegaPanelExample() }
    galleryItem("nm-nested") { NestedNavigationExample() }

    galleryHeading("Data-Driven, Mobile Drawer & States")
    galleryItem("nm-data") { DataDrivenKiraNavExample() }
    galleryItem("nm-mobile") { MobileDrawerExample() }
    galleryItem("nm-states") { StatesExample() }
}

@Composable
internal fun NavigationMenuPlayground() {
    val gap = KiraTheme.spacing.md

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Active State")
        BasicNavigationExample()
        ActiveLinkExample()

        SectionHeading("Mega Panel & Nested Navigation")
        MegaPanelExample()
        NestedNavigationExample()

        SectionHeading("Data-Driven, Mobile Drawer & States")
        DataDrivenKiraNavExample()
        MobileDrawerExample()
        StatesExample()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicNavigationExample() {
    var open by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("Click Learn or Changelog.") }

    GalleryExample("Basic Navigation Menu") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                KiraNavigationMenuTrigger(
                    label = "Learn",
                    expanded = open,
                    onClick = { open = !open },
                )
                KiraNavigationMenuPanel(
                    expanded = open,
                    onDismissRequest = { open = false },
                    minWidth = 264.dp,
                ) {
                    KiraNavigationMenuLinkCard(
                        title = "The workspace handbook",
                        description = "Clear guidance for everyday work.",
                        icon = { tint -> BookGlyph(tint) },
                        onClick = {
                            destination = "#handbook"
                            open = false
                        },
                    )
                    KiraNavigationMenuLinkCard(
                        title = "API documentation",
                        description = "Build the connections your team needs.",
                        icon = { tint -> CodeGlyph(tint) },
                        onClick = {
                            destination = "#api"
                            open = false
                        },
                    )
                }
            }
            KiraNavigationMenuLink(
                label = "Changelog",
                onClick = { destination = "#changelog" },
            )
        }
        ExampleNote("Destination: $destination")
    }
}

@Composable
private fun ActiveLinkExample() {
    var open by remember { mutableStateOf(false) }
    var activeHref by remember { mutableStateOf("#handbook") }

    GalleryExample("Active Link State") {
        Box {
            KiraNavigationMenuTrigger(
                label = "Learn",
                expanded = open,
                active = true,
                onClick = { open = !open },
            )
            KiraNavigationMenuPanel(
                expanded = open,
                onDismissRequest = { open = false },
                minWidth = 240.dp,
            ) {
                KiraNavigationMenuLinkCard(
                    title = "Handbook",
                    active = activeHref == "#handbook",
                    onClick = {
                        activeHref = "#handbook"
                        open = false
                    },
                )
                KiraNavigationMenuLinkCard(
                    title = "API documentation",
                    active = activeHref == "#api",
                    onClick = {
                        activeHref = "#api"
                        open = false
                    },
                )
            }
        }
        ExampleNote("Current destination: $activeHref (active link uses weight + underline, never color alone)")
    }
}

@Composable
private fun MegaPanelExample() {
    val colors = KiraTheme.colors
    var open by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("Open Explore resources for a multi-column mega panel.") }

    GalleryExample("Mega Menu Panel") {
        Box {
            KiraNavigationMenuTrigger(
                label = "Explore resources",
                expanded = open,
                onClick = { open = !open },
            )
            KiraNavigationMenuPanel(
                expanded = open,
                onDismissRequest = { open = false },
                megaMenu = true,
                minWidth = 340.dp,
                maxWidth = 560.dp,
            ) {
                KiraNavigationMenuHeader(
                    title = "Resources for your next step",
                    description = "Learn the essentials, then make the workspace your own.",
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.weight(1.1f).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        KiraNavigationMenuSection(title = "Learn and build") {
                            KiraNavigationMenuLinkCard(
                                title = "The workspace handbook",
                                description = "Clear guidance for everyday work.",
                                icon = { tint -> BookGlyph(tint) },
                                onClick = {
                                    destination = "#handbook"
                                    open = false
                                },
                            )
                            KiraNavigationMenuLinkCard(
                                title = "API documentation",
                                description = "Build the connections your team needs.",
                                icon = { tint -> CodeGlyph(tint) },
                                onClick = {
                                    destination = "#api"
                                    open = false
                                },
                            )
                            KiraNavigationMenuLinkCard(
                                title = "For your team",
                                description = "Set up a space everyone can use.",
                                icon = { tint -> GridGlyph(tint) },
                                onClick = {
                                    destination = "#team"
                                    open = false
                                },
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .background(colors.muted.copy(alpha = 0.3f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        BasicText(
                            text = "Make the next launch feel easier.",
                            style = KiraTheme.typography.label.copy(
                                color = colors.popoverForeground,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                        BasicText(
                            text = "A practical checklist for bringing a release together, from the first draft to the final review.",
                            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                        )
                        KiraNavigationMenuLink(
                            label = "Read the launch guide ↗",
                            active = true,
                            onClick = {
                                destination = "#launch-guide"
                                open = false
                            },
                        )
                    }
                }
                KiraNavigationMenuFooter {
                    BasicText(
                        text = "Made for thoughtful teams.",
                        style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                    )
                    KiraNavigationMenuLink(
                        label = "All resources ↗",
                        onClick = {
                            destination = "#all-resources"
                            open = false
                        },
                    )
                }
            }
        }
        ExampleNote("Destination: $destination")
    }
}

@Composable
private fun NestedNavigationExample() {
    var rootOpen by remember { mutableStateOf(false) }
    var mediaOpen by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("Open Workspace files → Media library.") }

    GalleryExample("Nested Navigation") {
        Box {
            KiraNavigationMenuTrigger(
                label = "Workspace files",
                expanded = rootOpen,
                icon = { tint -> FolderGlyph(tint) },
                onClick = { rootOpen = !rootOpen },
            )
            KiraNavigationMenuPanel(
                expanded = rootOpen,
                onDismissRequest = {
                    mediaOpen = false
                    rootOpen = false
                },
                minWidth = 260.dp,
            ) {
                KiraNavigationMenuLinkCard(
                    title = "Documents",
                    description = "Briefs, notes and decisions.",
                    icon = { tint -> BookGlyph(tint) },
                    onClick = {
                        destination = "#documents"
                        rootOpen = false
                    },
                )
                KiraNavigationMenuTrigger(
                    label = "Media library",
                    expanded = mediaOpen,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { mediaOpen = !mediaOpen },
                )
                if (mediaOpen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                            .clip(KiraTheme.radius.shapeMd)
                            .background(KiraTheme.colors.muted.copy(alpha = 0.35f))
                            .border(1.dp, KiraTheme.colors.border, KiraTheme.radius.shapeMd)
                            .padding(4.dp),
                    ) {
                        KiraNavigationMenuLinkCard(
                            title = "Images",
                            onClick = {
                                destination = "#images"
                                mediaOpen = false
                                rootOpen = false
                            },
                        )
                        KiraNavigationMenuLinkCard(
                            title = "Videos",
                            onClick = {
                                destination = "#videos"
                                mediaOpen = false
                                rootOpen = false
                            },
                        )
                    }
                }
                KiraNavigationMenuLinkCard(
                    title = "File settings",
                    onClick = {
                        destination = "#settings"
                        rootOpen = false
                    },
                )
            }
        }
        ExampleNote("Destination: $destination")
    }
}

private val demoNavigationItems: List<KiraNavigationItemData> = listOf(
    KiraNavigationItemData.Group(
        id = "product",
        label = "Product",
        description = "A shared place for your team's work.",
        items = listOf(
            KiraNavigationItemData.Group(
                id = "work",
                label = "Work together",
                items = listOf(
                    KiraNavigationItemData.Link(
                        id = "projects",
                        label = "Projects",
                        href = "#projects",
                        description = "Turn the plan into a clear next step.",
                        icon = { tint -> GridGlyph(tint) },
                    ),
                    KiraNavigationItemData.Link(
                        id = "documents",
                        label = "Documents",
                        href = "#documents",
                        description = "Give ideas and decisions a home.",
                        icon = { tint -> BookGlyph(tint) },
                    ),
                ),
            ),
            KiraNavigationItemData.Group(
                id = "manage",
                label = "Keep things moving",
                items = listOf(
                    KiraNavigationItemData.Link(
                        id = "people",
                        label = "People",
                        href = "#people",
                        description = "Bring everyone into the conversation.",
                    ),
                    KiraNavigationItemData.Link(
                        id = "insights",
                        label = "Insights",
                        href = "#insights",
                        description = "See where the work is heading.",
                    ),
                ),
            ),
        ),
    ),
    KiraNavigationItemData.Link(
        id = "changelog",
        label = "Changelog",
        href = "#changelog",
    ),
)

@Composable
private fun DataDrivenKiraNavExample() {
    var activeHref by remember { mutableStateOf("#changelog") }

    GalleryExample("KiraNavigationMenu (Data-Driven)") {
        KiraNavigationMenu(
            label = "Explore",
            items = demoNavigationItems,
            activeHref = activeHref,
            mobileMode = KiraNavigationMobileMode.Inline,
            onNavigate = { activeHref = it.href },
        )
        ExampleNote("Current destination: $activeHref")
    }
}

@Composable
private fun MobileDrawerExample() {
    var activeHref by remember { mutableStateOf("#projects") }

    GalleryExample("Mobile Drawer Mode") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExampleNote("Below the breakpoint (previewed at 280dp width), KiraNavigationMenu switches to a labelled right-side Drawer with collapsible groups.")
            Box(modifier = Modifier.width(280.dp)) {
                KiraNavigationMenu(
                    label = "Explore",
                    mobileLabel = "Open site navigation",
                    items = demoNavigationItems,
                    activeHref = activeHref,
                    mobileMode = KiraNavigationMobileMode.Drawer,
                    mobileBreakpoint = 400.dp,
                    onNavigate = { activeHref = it.href },
                )
            }
            ExampleNote("Selected from drawer: $activeHref")
        }
    }
}

@Composable
private fun StatesExample() {
    val longAndEmptyItems = remember {
        listOf(
            KiraNavigationItemData.Group(
                id = "library",
                label = "Library",
                description = "Content stays readable when names and descriptions grow.",
                items = listOf(
                    KiraNavigationItemData.Link(
                        id = "guide",
                        label = "Deployment and workspace administration for distributed product teams",
                        href = "#administration",
                        description = "A complete guide to permissions and release reviews.",
                    ),
                    KiraNavigationItemData.Group(
                        id = "archived",
                        label = "Archived collections",
                        items = emptyList(),
                    ),
                ),
            ),
        )
    }

    GalleryExample("States (Long Names, Empty & Disabled)") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Long names and empty sections")
                KiraNavigationMenu(
                    label = "Workspace library",
                    items = longAndEmptyItems,
                    mobileMode = KiraNavigationMobileMode.Inline,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("No navigation items")
                KiraNavigationMenu(
                    label = "Empty workspace",
                    items = emptyList(),
                    mobileMode = KiraNavigationMobileMode.Inline,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraLabel("Unavailable destination")
                KiraNavigationMenuTrigger(
                    label = "Administration",
                    expanded = false,
                    enabled = false,
                    onClick = {},
                )
                ExampleNote("Administration is only available to workspace owners.")
            }
        }
    }
}

@Composable
private fun BookGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(color, topLeft = Offset(size.width * 0.18f, size.height * 0.14f), size = Size(size.width * 0.64f, size.height * 0.72f), cornerRadius = CornerRadius(2.dp.toPx()), style = stroke)
        drawLine(color, Offset(size.width * 0.34f, size.height * 0.36f), Offset(size.width * 0.66f, size.height * 0.36f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.34f, size.height * 0.56f), Offset(size.width * 0.58f, size.height * 0.56f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun CodeGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = 1.5.dp.toPx()
        drawLine(color, Offset(size.width * 0.36f, size.height * 0.28f), Offset(size.width * 0.16f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.16f, size.height * 0.50f), Offset(size.width * 0.36f, size.height * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.64f, size.height * 0.28f), Offset(size.width * 0.84f, size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.84f, size.height * 0.50f), Offset(size.width * 0.64f, size.height * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun GridGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.4.dp.toPx())
        val box = size.minDimension * 0.28f
        drawRect(color, Offset(size.width * 0.16f, size.height * 0.16f), Size(box, box), style = stroke)
        drawRect(color, Offset(size.width * 0.56f, size.height * 0.16f), Size(box, box), style = stroke)
        drawRect(color, Offset(size.width * 0.16f, size.height * 0.56f), Size(box, box), style = stroke)
        drawRect(color, Offset(size.width * 0.56f, size.height * 0.56f), Size(box, box), style = stroke)
    }
}

@Composable
private fun FolderGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx())
        drawRoundRect(color, topLeft = Offset(size.width * 0.14f, size.height * 0.26f), size = Size(size.width * 0.72f, size.height * 0.54f), cornerRadius = CornerRadius(2.dp.toPx()), style = stroke)
        drawLine(color, Offset(size.width * 0.18f, size.height * 0.26f), Offset(size.width * 0.44f, size.height * 0.26f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}
