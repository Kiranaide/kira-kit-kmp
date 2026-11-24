package com.kirakit.gallery.tokens

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
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kirakit.components.card.KiraCard
import com.kirakit.gallery.Choices
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraRadiusPresets
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

private val TokenRadiusOptions: List<Pair<String, String>> =
    listOf("auto" to "Auto") + KiraRadiusPresets.All.map { it.id to it.title }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TokensAndRadiusPreview(
    selectedRadiusId: String,
    onSelectRadius: (String) -> Unit,
) {
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing
    val radius = KiraTheme.radius

    Column(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
        SectionHeading("Radius Scale")
        KiraCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                val selectedOption = remember(selectedRadiusId) {
                    TokenRadiusOptions.firstOrNull { it.first == selectedRadiusId } ?: TokenRadiusOptions.first()
                }
                Choices(
                    options = TokenRadiusOptions,
                    selected = selectedOption,
                    label = { it.second },
                    onSelect = { onSelectRadius(it.first) },
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    RadiusChip("sm", "${radius.sm.value.toInt()}dp", radius.shapeSm)
                    RadiusChip("md", "${radius.md.value.toInt()}dp", radius.shapeMd)
                    RadiusChip("lg (Btn/In)", "${radius.lg.value.toInt()}dp", radius.shapeLg)
                    RadiusChip("xl (Card)", "${radius.xl.value.toInt()}dp", radius.shapeXl)
                }
            }
        }

        SectionHeading("Semantic Colors")
        val swatches = listOf(
            "Primary" to colors.primary,
            "Secondary" to colors.secondary,
            "Accent" to colors.accent,
            "Muted" to colors.muted,
            "Destructive" to colors.destructive,
            "Info" to colors.info,
            "Success" to colors.success,
            "Warning" to colors.warning,
            "Sidebar" to colors.sidebar,
            "Sidebar Primary" to colors.sidebarPrimary,
            "Sidebar Accent" to colors.sidebarAccent,
        )
        KiraCard(Modifier.fillMaxWidth()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                swatches.forEach { (name, color) -> Swatch(name, color) }
            }
        }
    }
}

@Composable
private fun RadiusChip(label: String, value: String, shape: androidx.compose.ui.graphics.Shape) {
    val colors = KiraTheme.colors
    Row(
        modifier = Modifier
            .clip(shape)
            .background(colors.muted, shape)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(label, style = KiraTheme.typography.caption.copy(color = colors.foreground))
        BasicText(value, style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing
    val shape = KiraTheme.radius.shapeMd
    Row(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .padding(spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(16.dp).clip(KiraTheme.radius.shapeSm).background(color).border(1.dp, colors.border, KiraTheme.radius.shapeSm))
        BasicText(name, style = KiraTheme.typography.bodySmall.copy(color = colors.cardForeground))
    }
}
