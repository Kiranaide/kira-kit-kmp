package com.kirakit.gallery.sidebar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.card.KiraCard
import com.kirakit.components.sidebar.KiraSidebarCollapsible
import com.kirakit.components.sidebar.KiraSidebarScaffold
import com.kirakit.components.sidebar.KiraSidebarVariant
import com.kirakit.components.sidebar.LocalKiraSidebarState
import com.kirakit.gallery.Choices
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@Composable
internal fun SidebarPlayground(
    variant: KiraSidebarVariant,
    collapsible: KiraSidebarCollapsible,
    onVariantChange: (KiraSidebarVariant) -> Unit,
    onCollapsibleChange: (KiraSidebarCollapsible) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val spacing = KiraTheme.spacing
    val colors = KiraTheme.colors
    val sidebarState = LocalKiraSidebarState.current

    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SectionHeading("Sidebar")
        BasicText(
            text = "Responsive KiraSidebarScaffold: slide-over drawer with swipe-to-dismiss on mobile (< 768dp) and persistent collapsible rail on tablet/desktop.",
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
            }
        }
    }
}
