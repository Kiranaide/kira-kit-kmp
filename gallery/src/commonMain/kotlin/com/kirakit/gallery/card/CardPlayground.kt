package com.kirakit.gallery.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.card.KiraCard
import com.kirakit.components.card.KiraCardContent
import com.kirakit.components.card.KiraCardFooter
import com.kirakit.components.card.KiraCardHeader
import com.kirakit.components.card.KiraCardSize
import com.kirakit.components.card.KiraCardSkeleton
import com.kirakit.components.card.KiraCardVariant
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CardPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic")
        KiraCard(Modifier.widthIn(max = 448.dp).fillMaxWidth()) {
            KiraCardHeader(
                title = "Card Title",
                description = "Card description with additional context.",
            )
            KiraCardContent {
                BasicText(
                    "This is the main content area of the card.",
                    style = KiraTheme.typography.body.copy(color = colors.cardForeground),
                )
            }
            KiraCardFooter {
                KiraButton("Action", size = KiraButtonSize.Sm)
            }
        }

        SectionHeading("Variants")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            listOf(
                Triple(KiraCardVariant.Interactive, "Interactive Card", "Hover to see lift effect." to "This card has a hover effect with shadow and translation."),
                Triple(KiraCardVariant.Gradient, "Gradient Card", "Beautiful gradient background." to "Gradient border and background effect."),
                Triple(KiraCardVariant.Outline, "Outline Card", "Transparent with border." to "Clean outline style with transparent background."),
                Triple(KiraCardVariant.Elevated, "Elevated Card", "Card with shadow elevation." to "Subtle shadow for depth and hierarchy."),
                Triple(KiraCardVariant.Bordered, "Bordered Card", "Card with prominent border." to "Clear border definition for separation."),
                Triple(KiraCardVariant.Soft, "Soft Card", "Muted background style." to "Subtle muted background for gentle contrast."),
            ).forEach { (variant, title, copy) ->
                KiraCard(
                    modifier = Modifier.width(260.dp),
                    variant = variant,
                ) {
                    KiraCardHeader(title = title, description = copy.first)
                    KiraCardContent {
                        ExampleNote(copy.second)
                    }
                }
            }
        }

        SectionHeading("Glass")
        val glassBackdrop = Brush.linearGradient(
            listOf(
                colors.primary.copy(alpha = 0.22f),
                colors.secondary.copy(alpha = 0.22f),
                colors.primary.copy(alpha = 0.10f),
            ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KiraTheme.radius.shapeLg)
                .background(glassBackdrop)
                .padding(32.dp),
        ) {
            KiraCard(
                modifier = Modifier.widthIn(max = 448.dp).fillMaxWidth(),
                variant = KiraCardVariant.Glass,
                backdrop = {
                    Box(Modifier.matchParentSize().background(glassBackdrop))
                },
            ) {
                KiraCardHeader(
                    title = "Glass Card",
                    description = "See the blur effect behind this card.",
                )
                KiraCardContent {
                    ExampleNote("The backdrop blur creates a frosted glass effect that blurs the colorful background behind it.")
                }
            }
        }

        SectionHeading("More Variants")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            KiraCard(Modifier.width(260.dp), variant = KiraCardVariant.Elevated2) {
                KiraCardHeader("Elevated 2", description = "Stronger shadow elevation.")
                KiraCardContent {
                    ExampleNote("More pronounced shadow for emphasis.")
                }
            }
            KiraCard(Modifier.width(260.dp), variant = KiraCardVariant.Flat) {
                KiraCardHeader("Flat Card", description = "Minimal flat design.")
                KiraCardContent {
                    ExampleNote("No borders or shadows, pure flat design.")
                }
            }
        }

        SectionHeading("Interactive")
        var activated by rememberSaveable { mutableIntStateOf(0) }
        KiraCard(
            modifier = Modifier
                .widthIn(max = 448.dp)
                .fillMaxWidth()
                .semantics { stateDescription = "Activated $activated times" },
            variant = KiraCardVariant.Interactive,
            onClick = { activated++ },
        ) {
            KiraCardHeader(
                title = "Interactive Card",
                description = "A click-target card with full keyboard operability.",
            )
            KiraCardContent {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        "Tab to focus, then press Enter or Space. Activations:",
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                    BasicText(
                        activated.toString(),
                        style = KiraTheme.typography.bodySmall.copy(
                            color = colors.foreground,
                            fontWeight = FontWeight.Medium,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
            }
        }

        SectionHeading("Skeleton")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            KiraCardSkeleton(Modifier.width(280.dp))
            KiraCardSkeleton(Modifier.width(280.dp), size = KiraCardSize.Sm)
        }
    }
}
