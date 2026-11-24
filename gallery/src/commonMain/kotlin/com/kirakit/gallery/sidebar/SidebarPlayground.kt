package com.kirakit.gallery.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.card.KiraCard
import com.kirakit.components.checkbox.KiraCheckboxWithLabel
import com.kirakit.components.sidebar.KiraSidebarCollapsible
import com.kirakit.components.sidebar.KiraSidebarItem
import com.kirakit.components.sidebar.KiraSidebarSkeleton
import com.kirakit.components.sidebar.KiraSidebarSubItem
import com.kirakit.components.sidebar.KiraSidebarVariant
import com.kirakit.components.sidebar.LocalKiraSidebarState
import com.kirakit.gallery.Choices
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@Composable
internal fun SidebarPlayground(
    variant: KiraSidebarVariant,
    collapsible: KiraSidebarCollapsible,
    hoverExpand: Boolean,
    onVariantChange: (KiraSidebarVariant) -> Unit,
    onCollapsibleChange: (KiraSidebarCollapsible) -> Unit,
    onHoverExpandChange: (Boolean) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val spacing = KiraTheme.spacing
    val colors = KiraTheme.colors
    val sidebarState = LocalKiraSidebarState.current

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionHeading("Sidebar Shell")
        BasicText(
            text = "Responsive KiraSidebarScaffold: slide-over drawer with swipe-to-dismiss on mobile (< 768dp), persistent collapsible rail with collapsed icon tooltips and optional hover-expand on tablet/desktop.",
            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
        )
        KiraCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = if (sidebarState.isMobile) "Mode: Mobile Drawer" else "Mode: Desktop Panel",
                        style = KiraTheme.typography.label.copy(color = colors.cardForeground),
                    )
                    KiraButton(
                        text = if (sidebarState.isMobile) "Open Sidebar" else "Toggle Collapse",
                        size = KiraButtonSize.Sm,
                        onClick = onOpenDrawer,
                    )
                }

                BasicText("Desktop Variant", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
                Choices(
                    options = KiraSidebarVariant.entries,
                    selected = variant,
                    label = { it.name },
                    onSelect = onVariantChange,
                )

                BasicText("Collapsible Mode", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
                Choices(
                    options = KiraSidebarCollapsible.entries,
                    selected = collapsible,
                    label = { it.name },
                    onSelect = onCollapsibleChange,
                )

                KiraCheckboxWithLabel(
                    checked = hoverExpand,
                    onCheckedChange = onHoverExpandChange,
                    label = "Hover to expand when collapsed",
                    description = "On desktop, hovering the collapsed icon rail temporarily expands the sidebar.",
                )
            }
        }

        SectionHeading("Nested Sub-Items & Loading Skeleton")
        GalleryExample("KiraSidebarSubItem & KiraSidebarSkeleton") {
            var activeSub by remember { mutableStateOf("tokens") }
            var showSkeleton by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExampleNote("Sub-items indent cleanly under parent sections; toggle skeleton to preview loading state.")
                    KiraButton(
                        text = if (showSkeleton) "Show Items" else "Show Skeleton",
                        size = KiraButtonSize.Xs,
                        variant = KiraButtonVariant.Outline,
                        onClick = { showSkeleton = !showSkeleton },
                    )
                }

                if (showSkeleton) {
                    KiraSidebarSkeleton(count = 4)
                } else {
                    KiraSidebarItem(
                        label = "Design System",
                        leadingMonogram = "DS",
                        selected = false,
                        closeMobileOnClick = false,
                        onClick = {},
                    )
                    KiraSidebarSubItem(
                        label = "Color Tokens",
                        selected = activeSub == "tokens",
                        onClick = { activeSub = "tokens" },
                    )
                    KiraSidebarSubItem(
                        label = "Typography",
                        selected = activeSub == "type",
                        badge = "9",
                        onClick = { activeSub = "type" },
                    )
                    KiraSidebarSubItem(
                        label = "Motion Curves",
                        selected = activeSub == "motion",
                        onClick = { activeSub = "motion" },
                    )
                }
            }
        }
    }
}
