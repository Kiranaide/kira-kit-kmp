package com.kirakit.gallery.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.drawer.KiraDrawer
import com.kirakit.components.drawer.KiraDrawerHandle
import com.kirakit.components.drawer.KiraDrawerSide
import com.kirakit.components.drawer.KiraDrawerSize
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DrawerPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md

    var showBasic by remember { mutableStateOf(false) }
    var activeSide by remember { mutableStateOf<KiraDrawerSide?>(null) }
    var activeRightSize by remember { mutableStateOf<KiraDrawerSize?>(null) }
    var activeBottomSize by remember { mutableStateOf<KiraDrawerSize?>(null) }
    var parentOpen by remember { mutableStateOf(false) }
    var childOpen by remember { mutableStateOf(false) }
    var controlledOpen by remember { mutableStateOf(false) }
    var showSnapSheet by remember { mutableStateOf(false) }
    var snapIndex by remember { mutableIntStateOf(0) }
    val snapPoints = remember { listOf(0.55f, 1.0f) }
    var showEmptyOrder by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Positions")
        GalleryExample("Basic Drawer") {
            KiraButton(
                text = "Open",
                onClick = { showBasic = true },
            )
        }

        GalleryExample("Positions (Slide From Any Edge)") {
            ExampleNote("Drag along the drawer's edge to dismiss smoothly with dynamic scrim fade.")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraDrawerSide.entries.forEach { side ->
                    KiraButton(
                        text = side.name,
                        variant = KiraButtonVariant.Outline,
                        onClick = { activeSide = side },
                    )
                }
            }
        }

        SectionHeading("Size Variants")
        GalleryExample("Width Variants (Right) & Height Variants (Bottom)") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExampleNote("Width Variants (Right)")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraDrawerSize.entries.forEach { size ->
                        KiraButton(
                            text = size.name,
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { activeRightSize = size },
                        )
                    }
                }

                ExampleNote("Height Variants (Bottom)")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraDrawerSize.entries.forEach { size ->
                        KiraButton(
                            text = size.name,
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { activeBottomSize = size },
                        )
                    }
                }
            }
        }

        SectionHeading("Nested & Controlled")
        GalleryExample("Nested Drawers") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "Open Parent Drawer",
                    onClick = { parentOpen = true },
                )
            }
        }

        GalleryExample("Controlled State") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraButton(
                        text = "Toggle",
                        onClick = { controlledOpen = !controlledOpen },
                    )
                    KiraButton(
                        text = "Open Programmatically",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { controlledOpen = true },
                    )
                    KiraButton(
                        text = "Close Programmatically",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { controlledOpen = false },
                    )
                }
                ExampleNote("isOpen: $controlledOpen")
            }
        }

        SectionHeading("Snap Points & States")
        GalleryExample("Order Summary Sheet (Snap Points: 55% / 100%)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = "Review order",
                    onClick = {
                        snapIndex = 0
                        showSnapSheet = true
                    },
                )
                ExampleNote("Tap the top handle pill or drag vertically to snap between 55% and 100% height, or swipe down to dismiss.")
            }
        }

        GalleryExample("Empty State & Long Title") {
            KiraButton(
                text = "Open empty order",
                variant = KiraButtonVariant.Outline,
                onClick = { showEmptyOrder = true },
            )
        }
    }

    // 1. Basic
    KiraDrawer(
        open = showBasic,
        onDismissRequest = { showBasic = false },
        title = "Title",
        description = "Description",
        footer = {
            KiraButton(
                text = "Cancel",
                variant = KiraButtonVariant.Outline,
                onClick = { showBasic = false },
            )
            KiraButton(text = "Save", onClick = { showBasic = false })
        },
    ) {
        BasicText(
            text = "You can put any content here. The drawer is scrollable if content overflows.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }

    // 2. Positions
    activeSide?.let { side ->
        val (subtitle, copy) = when (side) {
            KiraDrawerSide.Left -> "Slides from left" to "Often used for navigation menus."
            KiraDrawerSide.Right -> "Slides from right (default)" to "Common for settings panels."
            KiraDrawerSide.Top -> "Slides from top" to "Useful for notifications."
            KiraDrawerSide.Bottom -> "Slides from bottom" to "Common on mobile for action sheets."
        }
        KiraDrawer(
            open = true,
            onDismissRequest = { activeSide = null },
            title = "${side.name} Drawer",
            description = subtitle,
            side = side,
        ) {
            BasicText(
                text = copy,
                style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
            )
        }
    }

    // 3. Width Variants (Right)
    activeRightSize?.let { size ->
        KiraDrawer(
            open = true,
            onDismissRequest = { activeRightSize = null },
            title = "${size.name} Right Drawer",
            description = "This drawer has size = KiraDrawerSize.${size.name}.",
            side = KiraDrawerSide.Right,
            size = size,
            footer = {
                KiraButton(text = "Close", onClick = { activeRightSize = null })
            },
        ) {
            SizePlaceholderBox("Content (${size.name})")
        }
    }

    // 4. Height Variants (Bottom)
    activeBottomSize?.let { size ->
        KiraDrawer(
            open = true,
            onDismissRequest = { activeBottomSize = null },
            title = "${size.name} Bottom Drawer",
            description = "This drawer has size = KiraDrawerSize.${size.name}.",
            side = KiraDrawerSide.Bottom,
            size = size,
            footer = {
                KiraButton(text = "Close", onClick = { activeBottomSize = null })
            },
        ) {
            SizePlaceholderBox("Content (${size.name})")
        }
    }

    // 5. Nested Drawers
    KiraDrawer(
        open = parentOpen,
        onDismissRequest = {
            childOpen = false
            parentOpen = false
        },
        title = "Parent Drawer",
        description = "Contains a nested drawer",
        size = KiraDrawerSize.Lg,
        footer = {
            KiraButton(
                text = "Close",
                variant = KiraButtonVariant.Outline,
                onClick = { parentOpen = false },
            )
        },
    ) {
        BasicText(
            text = "This is the parent drawer. You can open another drawer from within it.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
        KiraButton(
            text = "Open Child Drawer",
            onClick = { childOpen = true },
        )
    }

    KiraDrawer(
        open = childOpen,
        onDismissRequest = { childOpen = false },
        title = "Child Drawer",
        description = "Nested inside parent",
        side = KiraDrawerSide.Bottom,
        footer = {
            KiraButton(text = "Close Child", onClick = { childOpen = false })
        },
    ) {
        BasicText(
            text = "Multiple drawers can be open simultaneously.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }

    // 6. Controlled Drawer
    KiraDrawer(
        open = controlledOpen,
        onDismissRequest = { controlledOpen = false },
        title = "Controlled Drawer",
        description = "State managed externally",
        footer = {
            KiraButton(
                text = "Close",
                variant = KiraButtonVariant.Outline,
                onClick = { controlledOpen = false },
            )
        },
    ) {
        BasicText(
            text = "This drawer's open state is controlled by the parent composable's state.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KiraTheme.radius.shapeMd)
                .background(colors.muted.copy(alpha = 0.5f))
                .border(1.dp, colors.border, KiraTheme.radius.shapeMd)
                .padding(12.dp),
        ) {
            BasicText(
                text = "isOpen: $controlledOpen",
                style = KiraTheme.typography.bodySmall.copy(
                    color = colors.foreground,
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }
    }

    // 7. Snap Points (Order Summary)
    val isExpanded = snapIndex == snapPoints.lastIndex
    KiraDrawer(
        open = showSnapSheet,
        onDismissRequest = { showSnapSheet = false },
        title = "Order summary",
        description = "Order #4821 · ready for review (${(snapPoints[snapIndex] * 100).toInt()}%)",
        side = KiraDrawerSide.Bottom,
        snapPoints = snapPoints,
        activeSnapIndex = snapIndex,
        onSnapIndexChange = { snapIndex = it },
        handle = {
            KiraDrawerHandle(
                expanded = isExpanded,
                onExpandedChange = { expanded ->
                    snapIndex = if (expanded) snapPoints.lastIndex else 0
                },
            )
        },
        footer = {
            KiraButton(
                text = if (isExpanded) "Collapse order" else "Expand order",
                variant = KiraButtonVariant.Outline,
                onClick = { snapIndex = if (isExpanded) 0 else snapPoints.lastIndex },
            )
            KiraButton(text = "Close order", onClick = { showSnapSheet = false })
        },
    ) {
        BasicText("2 items · $68.00", style = KiraTheme.typography.body.copy(color = colors.foreground))
        BasicText("Canvas carryall — natural", style = KiraTheme.typography.body.copy(color = colors.foreground))
        BasicText("Everyday water bottle — slate", style = KiraTheme.typography.body.copy(color = colors.foreground))
        BasicText(
            text = "Delivery to 42 Market Street. Estimated arrival: Friday.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }

    // 8. Empty State & Long Title
    KiraDrawer(
        open = showEmptyOrder,
        onDismissRequest = { showEmptyOrder = false },
        title = "A particularly long order description for a narrow screen",
        description = "No items have been added yet.",
        side = KiraDrawerSide.Bottom,
        footer = {
            KiraButton(text = "Continue shopping", onClick = { showEmptyOrder = false })
        },
    ) {
        BasicText(
            text = "Your order is empty. Add an item to see it here.",
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }
}

@Composable
private fun SizePlaceholderBox(label: String) {
    val colors = KiraTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(KiraTheme.radius.shapeLg)
            .background(colors.muted.copy(alpha = 0.25f))
            .border(1.dp, colors.border, KiraTheme.radius.shapeLg),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
    }
}
