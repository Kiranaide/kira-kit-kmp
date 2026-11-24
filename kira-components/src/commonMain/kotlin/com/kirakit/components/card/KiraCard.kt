package com.kirakit.components.card

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraCardVariant {
    Default,
    Interactive,
    Gradient,
    Glass,
    Outline,
    Elevated,
    Bordered,
    Flat,
    Elevated2,
    Soft,
}

enum class KiraCardSize { Default, Sm }

@Composable
fun KiraCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    onClick: (() -> Unit)? = null,
    variant: KiraCardVariant = KiraCardVariant.Default,
    size: KiraCardSize = KiraCardSize.Default,
    enabled: Boolean = true,
    backdrop: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val shape = KiraTheme.radius.shapeXl
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val focused by source.collectIsFocusedAsState()
    val isInteractiveVariant = enabled && variant == KiraCardVariant.Interactive
    val isClickable = enabled && onClick != null

    val elevation by animateDpAsState(
        targetValue = when (variant) {
            KiraCardVariant.Elevated2 -> 18.dp
            KiraCardVariant.Elevated, KiraCardVariant.Glass -> 8.dp
            KiraCardVariant.Interactive -> if (isInteractiveVariant && hovered) 8.dp else 0.dp
            else -> 0.dp
        },
        animationSpec = tween(durationMillis = 200, easing = KiraMotion.EaseOutStrong),
        label = "CardElevation",
    )
    val lift by animateDpAsState(
        targetValue = if (isInteractiveVariant && hovered) (-4).dp else 0.dp,
        animationSpec = tween(durationMillis = 200, easing = KiraMotion.EaseOutStrong),
        label = "CardLift",
    )

    val fill = when (variant) {
        KiraCardVariant.Outline -> Color.Transparent
        KiraCardVariant.Glass -> colors.background.copy(alpha = if (isDark) 0.40f else 0.60f)
        KiraCardVariant.Flat -> colors.muted.copy(alpha = 0.30f)
        KiraCardVariant.Soft -> colors.muted.copy(alpha = 0.50f)
        else -> colors.card
    }
    val surface = if (variant == KiraCardVariant.Gradient) {
        Modifier.background(
            Brush.linearGradient(
                listOf(
                    colors.primary.copy(alpha = 0.10f),
                    colors.secondary.copy(alpha = 0.10f),
                    colors.primary.copy(alpha = 0.05f),
                ),
            ),
        )
    } else {
        Modifier.background(fill)
    }

    val subtleRing = colors.foreground.copy(alpha = 0.10f)
    val border = when {
        focused -> Modifier.border(2.dp, colors.ring, shape)
        variant == KiraCardVariant.Flat -> Modifier
        variant == KiraCardVariant.Outline || variant == KiraCardVariant.Bordered ->
            Modifier.border(2.dp, colors.border, shape)
        variant == KiraCardVariant.Glass || variant == KiraCardVariant.Soft ->
            Modifier.border(1.dp, colors.border.copy(alpha = 0.50f), shape)
        variant == KiraCardVariant.Gradient ->
            Modifier.border(1.dp, colors.primary.copy(alpha = 0.20f), shape)
        else -> Modifier.border(1.dp, subtleRing, shape)
    }

    val pointerMod = if (isClickable || isInteractiveVariant) {
        Modifier.pointerHoverIcon(PointerIcon.Hand)
    } else {
        Modifier
    }

    val action = when {
        onClick != null -> Modifier
            .hoverable(source, enabled = enabled)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
        isInteractiveVariant -> Modifier.hoverable(source)
        else -> Modifier
    }

    val shadowColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.16f)

    CompositionLocalProvider(LocalCardSize provides size) {
        Box(
            modifier = modifier
                .offset(y = lift)
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                )
                .clip(shape)
                .then(pointerMod)
                .then(action)
                .alpha(if (enabled) 1f else 0.55f)
                .semantics { if (!enabled) disabled() },
            propagateMinConstraints = true,
        ) {
            if (variant == KiraCardVariant.Glass && backdrop != null) {
                Box(Modifier.matchParentSize().blur(20.dp), content = backdrop)
            }
            Column(
                modifier = Modifier
                    .then(surface)
                    .then(border)
                    .padding(contentPadding ?: PaddingValues(if (size == KiraCardSize.Sm) 16.dp else 24.dp)),
                content = content,
            )
        }
    }
}

private val LocalCardSize = staticCompositionLocalOf { KiraCardSize.Default }

@Composable
fun KiraCardHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
    bottomSpacing: Boolean = true,
) {
    val compact = LocalCardSize.current == KiraCardSize.Sm
    val bottomPad = when {
        !bottomSpacing -> 0.dp
        compact -> 12.dp
        else -> 16.dp
    }
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = bottomPad),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            KiraCardTitle(title)
            if (description != null) KiraCardDescription(description)
        }
        action?.invoke()
    }
}

@Composable
fun KiraCardTitle(text: String, modifier: Modifier = Modifier) {
    val compact = LocalCardSize.current == KiraCardSize.Sm
    val style = KiraTheme.typography.heading3.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = if (compact) 14.sp else 16.sp,
        lineHeight = if (compact) 16.sp else 20.sp,
        color = KiraTheme.colors.cardForeground,
    )
    BasicText(text, modifier.semantics { heading() }, style = style)
}

@Composable
fun KiraCardDescription(text: String, modifier: Modifier = Modifier) {
    BasicText(
        text = text,
        modifier = modifier,
        style = KiraTheme.typography.body.copy(color = KiraTheme.colors.mutedForeground),
    )
}

@Composable
fun KiraCardContent(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier, content = content)
}

@Composable
fun KiraCardFooter(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val compact = LocalCardSize.current == KiraCardSize.Sm
    Row(
        modifier = modifier.fillMaxWidth().padding(top = if (compact) 16.dp else 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun KiraCardSkeleton(modifier: Modifier = Modifier, size: KiraCardSize = KiraCardSize.Default) {
    val transition = rememberInfiniteTransition(label = "Card skeleton")
    val opacity by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(tween(KiraMotion.PingOnceMs), RepeatMode.Reverse),
        label = "Skeleton opacity",
    )
    KiraCard(
        modifier = modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate },
        size = size,
    ) {
        val color = KiraTheme.colors.muted.copy(alpha = opacity)
        val shape = KiraTheme.radius.shapeSm
        val compact = size == KiraCardSize.Sm
        val lineHeight = if (compact) 10.dp else 12.dp
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.width(if (compact) 80.dp else 112.dp).height(if (compact) 12.dp else 16.dp).background(color, shape))
            Box(Modifier.width(if (compact) 128.dp else 176.dp).height(lineHeight).background(color, shape))
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth().height(lineHeight).background(color, shape))
            Box(Modifier.fillMaxWidth(0.80f).height(lineHeight).background(color, shape))
        }
    }
}
