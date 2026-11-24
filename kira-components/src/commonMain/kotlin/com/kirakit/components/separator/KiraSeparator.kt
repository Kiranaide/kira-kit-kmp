package com.kirakit.components.separator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraSeparatorOrientation {
    Horizontal,
    Vertical,
}

enum class KiraSeparatorSize(val thickness: Dp) {
    Xs(1.dp),
    Sm(1.dp),
    Default(1.dp),
    Md(2.dp),
    Lg(4.dp),
    Xl(6.dp),
}

enum class KiraSeparatorLineStyle {
    Solid,
    Dashed,
    Dotted,
    Gradient,
}

enum class KiraSeparatorColor {
    Default,
    Primary,
    Secondary,
    Muted,
    Destructive,
    Success,
    Warning,
    Info,
}

enum class KiraSeparatorLabelAlign {
    Start,
    Center,
    End,
}

enum class KiraSeparatorSpacing(val value: Dp) {
    None(0.dp),
    Sm(8.dp),
    Default(16.dp),
    Lg(24.dp),
}

@Immutable
data class KiraSeparatorAction(
    val label: String,
    val onClick: () -> Unit,
    val disabled: Boolean = false,
    val disabledReason: String? = null,
    val pending: Boolean = false,
)

/**
 * Normalizes a non-negative badge count for [KiraSectionSeparator].
 */
fun normalizeSeparatorCount(count: Int?): Int? =
    count?.coerceAtLeast(0)

/**
 * Visual & semantic divider aligned with kira-kit-v2's `Separator`.
 *
 * Supports horizontal and vertical orientations, 6 thickness tiers, 4 line styles
 * (`Solid`, `Dashed`, `Dotted`, `Gradient`), 8 semantic colors, optional aligned labels
 * with leading/trailing icons, spacing/inset helpers, and decorative accessibility mode.
 */
@Composable
fun KiraSeparator(
    modifier: Modifier = Modifier,
    orientation: KiraSeparatorOrientation = KiraSeparatorOrientation.Horizontal,
    size: KiraSeparatorSize = KiraSeparatorSize.Default,
    lineStyle: KiraSeparatorLineStyle = KiraSeparatorLineStyle.Solid,
    color: KiraSeparatorColor = KiraSeparatorColor.Default,
    label: String? = null,
    labelAlign: KiraSeparatorLabelAlign = KiraSeparatorLabelAlign.Center,
    labelIcon: (@Composable (Color) -> Unit)? = null,
    labelIconAtEnd: Boolean = false,
    spacing: KiraSeparatorSpacing = KiraSeparatorSpacing.None,
    inset: KiraSeparatorSpacing = KiraSeparatorSpacing.None,
    decorative: Boolean = false,
    contentDescription: String? = null,
) {
    val resolvedColor = resolveSeparatorColor(color)
    val hasLabel = !label.isNullOrEmpty()
    val horizontal = orientation == KiraSeparatorOrientation.Horizontal

    val spacingMod = when {
        spacing == KiraSeparatorSpacing.None && inset == KiraSeparatorSpacing.None -> Modifier
        horizontal -> Modifier.padding(vertical = spacing.value, horizontal = inset.value)
        else -> Modifier.padding(horizontal = spacing.value, vertical = inset.value)
    }

    val semanticMod = if (decorative) {
        Modifier.clearAndSetSemantics {}
    } else {
        Modifier.semantics(mergeDescendants = true) {
            val desc = contentDescription ?: label ?: "Separator"
            this.contentDescription = desc
        }
    }

    if (!hasLabel) {
        SeparatorRule(
            orientation = orientation,
            thickness = size.thickness,
            lineStyle = lineStyle,
            color = resolvedColor,
            modifier = modifier
                .then(spacingMod)
                .then(semanticMod)
                .then(if (horizontal) Modifier.fillMaxWidth() else Modifier.fillMaxHeight()),
        )
        return
    }

    if (horizontal) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .then(spacingMod)
                .then(semanticMod),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (labelAlign != KiraSeparatorLabelAlign.Start) {
                SeparatorRule(
                    orientation = KiraSeparatorOrientation.Horizontal,
                    thickness = size.thickness,
                    lineStyle = lineStyle,
                    color = resolvedColor,
                    modifier = Modifier.weight(1f),
                )
            }
            KiraSeparatorLabel(
                text = label,
                icon = labelIcon,
                iconAtEnd = labelIconAtEnd,
            )
            if (labelAlign != KiraSeparatorLabelAlign.End) {
                SeparatorRule(
                    orientation = KiraSeparatorOrientation.Horizontal,
                    thickness = size.thickness,
                    lineStyle = lineStyle,
                    color = resolvedColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .then(spacingMod)
                .then(semanticMod),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (labelAlign != KiraSeparatorLabelAlign.Start) {
                SeparatorRule(
                    orientation = KiraSeparatorOrientation.Vertical,
                    thickness = size.thickness,
                    lineStyle = lineStyle,
                    color = resolvedColor,
                    modifier = Modifier.weight(1f),
                )
            }
            KiraSeparatorLabel(
                text = label,
                icon = labelIcon,
                iconAtEnd = labelIconAtEnd,
            )
            if (labelAlign != KiraSeparatorLabelAlign.End) {
                SeparatorRule(
                    orientation = KiraSeparatorOrientation.Vertical,
                    thickness = size.thickness,
                    lineStyle = lineStyle,
                    color = resolvedColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Label badge displayed inline inside a [KiraSeparator].
 */
@Composable
fun KiraSeparatorLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = KiraTheme.colors.mutedForeground,
    icon: (@Composable (Color) -> Unit)? = null,
    iconAtEnd: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null && !iconAtEnd) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(color)
            }
        }
        BasicText(
            text = text,
            style = KiraTheme.typography.caption.copy(
                color = color,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (icon != null && iconAtEnd) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(color)
            }
        }
    }
}

/**
 * Rich section divider aligned with kira-kit-v2's `KiraSeparator`.
 *
 * Supports an optional count pill, description text, disabled reason, inline action button,
 * or collapsible disclosure section with animated reveal.
 */
@Composable
fun KiraSectionSeparator(
    label: String,
    modifier: Modifier = Modifier,
    labelAlign: KiraSeparatorLabelAlign = KiraSeparatorLabelAlign.Start,
    labelIcon: (@Composable (Color) -> Unit)? = null,
    labelIconAtEnd: Boolean = false,
    size: KiraSeparatorSize = KiraSeparatorSize.Default,
    lineStyle: KiraSeparatorLineStyle = KiraSeparatorLineStyle.Solid,
    color: KiraSeparatorColor = KiraSeparatorColor.Default,
    spacing: KiraSeparatorSpacing = KiraSeparatorSpacing.Default,
    inset: KiraSeparatorSpacing = KiraSeparatorSpacing.None,
    count: Int? = null,
    countLabel: String = "items",
    description: String? = null,
    disabled: Boolean = false,
    disabledReason: String? = null,
    action: KiraSeparatorAction? = null,
    collapsible: Boolean = false,
    expanded: Boolean? = null,
    defaultExpanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val resolvedLineColor = resolveSeparatorColor(color)
    val validCount = normalizeSeparatorCount(count)
    val isDisabled = disabled || action?.disabled == true
    val reason = disabledReason ?: action?.disabledReason

    var internalExpanded by remember { mutableStateOf(defaultExpanded) }
    val isExpanded = expanded ?: internalExpanded
    fun toggleExpanded() {
        if (disabled) return
        val next = !isExpanded
        if (expanded == null) internalExpanded = next
        onExpandedChange?.invoke(next)
        haptics.perform(KiraHapticFeedbackType.Selection)
    }

    val chevronRotationState = if (collapsible) {
        animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            animationSpec = KiraMotion.fastTween(),
            label = "SeparatorChevron",
        )
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.value, horizontal = inset.value),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (labelAlign != KiraSeparatorLabelAlign.Start) {
                SeparatorRule(
                    orientation = KiraSeparatorOrientation.Horizontal,
                    thickness = size.thickness,
                    lineStyle = lineStyle,
                    color = resolvedLineColor,
                    modifier = Modifier.weight(1f),
                )
            }

            if (collapsible) {
                val interaction = remember { MutableInteractionSource() }
                val hovered by interaction.collectIsHoveredAsState()
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(radius.shapeMd)
                        .background(if (hovered && !disabled) colors.muted.copy(alpha = 0.6f) else Color.Transparent)
                        .alpha(if (disabled) 0.55f else 1f)
                        .hoverable(interaction, enabled = !disabled)
                        .pointerHoverIcon(if (disabled) PointerIcon.Default else PointerIcon.Hand)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = !disabled,
                            role = Role.Button,
                            onClick = ::toggleExpanded,
                        )
                        .semantics(mergeDescendants = true) {
                            heading()
                            stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                            if (disabled) disabled()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SectionLabelContent(
                        label = label,
                        labelIcon = labelIcon,
                        labelIconAtEnd = labelIconAtEnd,
                        validCount = validCount,
                        countLabel = countLabel,
                    )
                    SeparatorChevronGlyph(
                        color = colors.mutedForeground,
                        modifier = Modifier.graphicsLayer { rotationZ = chevronRotationState?.value ?: 0f },
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SectionLabelContent(
                        label = label,
                        labelIcon = labelIcon,
                        labelIconAtEnd = labelIconAtEnd,
                        validCount = validCount,
                        countLabel = countLabel,
                    )
                    if (action != null) {
                        KiraButton(
                            text = action.label,
                            variant = KiraButtonVariant.Ghost,
                            size = KiraButtonSize.Sm,
                            enabled = !isDisabled && !action.pending,
                            loading = action.pending,
                            onClick = action.onClick,
                        )
                    }
                }
            }

            if (labelAlign != KiraSeparatorLabelAlign.End) {
                SeparatorRule(
                    orientation = KiraSeparatorOrientation.Horizontal,
                    thickness = size.thickness,
                    lineStyle = lineStyle,
                    color = resolvedLineColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (description != null) {
            BasicText(
                text = description,
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
        }
        if (isDisabled && reason != null) {
            BasicText(
                text = reason,
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
        }

        if (collapsible && content != null) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(KiraMotion.RouteTransitionMs, easing = KiraMotion.EaseOutStrong)) +
                    fadeIn(tween(KiraMotion.RouteFadeMs)),
                exit = shrinkVertically(tween(180, easing = KiraMotion.EaseOutStrong)) +
                    fadeOut(tween(120)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun SectionLabelContent(
    label: String,
    labelIcon: (@Composable (Color) -> Unit)?,
    labelIconAtEnd: Boolean,
    validCount: Int?,
    countLabel: String,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (labelIcon != null && !labelIconAtEnd) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                labelIcon(colors.foreground)
            }
        }
        BasicText(
            text = label,
            modifier = Modifier.semantics { heading() },
            style = KiraTheme.typography.label.copy(
                color = colors.foreground,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (validCount != null) {
            Box(
                modifier = Modifier
                    .clip(radius.shapeMd)
                    .background(colors.muted)
                    .semantics { contentDescription = "$validCount $countLabel" }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                BasicText(
                    text = validCount.toString(),
                    style = KiraTheme.typography.caption.copy(
                        color = colors.mutedForeground,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
        if (labelIcon != null && labelIconAtEnd) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                labelIcon(colors.foreground)
            }
        }
    }
}

@Composable
private fun SeparatorRule(
    orientation: KiraSeparatorOrientation,
    thickness: Dp,
    lineStyle: KiraSeparatorLineStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val horizontal = orientation == KiraSeparatorOrientation.Horizontal
    val sizeMod = if (horizontal) Modifier.height(thickness) else Modifier.width(thickness)

    when (lineStyle) {
        KiraSeparatorLineStyle.Solid -> {
            Box(modifier = modifier.then(sizeMod).background(color))
        }
        KiraSeparatorLineStyle.Gradient -> {
            val brush = remember(horizontal, color) {
                if (horizontal) {
                    Brush.horizontalGradient(listOf(Color.Transparent, color, Color.Transparent))
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, color, Color.Transparent))
                }
            }
            Box(modifier = modifier.then(sizeMod).background(brush))
        }
        KiraSeparatorLineStyle.Dashed, KiraSeparatorLineStyle.Dotted -> {
            Canvas(modifier = modifier.then(sizeMod).clearAndSetSemantics {}) {
                val strokePx = thickness.toPx().coerceAtLeast(1f)
                val isDotted = lineStyle == KiraSeparatorLineStyle.Dotted
                val dashLen = if (isDotted) strokePx else (strokePx * 4f).coerceAtLeast(6.dp.toPx())
                val gapLen = if (isDotted) (strokePx * 2.2f).coerceAtLeast(3.dp.toPx()) else (strokePx * 2.5f).coerceAtLeast(4.dp.toPx())
                val effect = PathEffect.dashPathEffect(floatArrayOf(dashLen, gapLen), 0f)
                val cap = if (isDotted) StrokeCap.Round else StrokeCap.Butt

                if (horizontal) {
                    val y = size.height / 2f
                    drawLine(
                        color = color,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokePx,
                        cap = cap,
                        pathEffect = effect,
                    )
                } else {
                    val x = size.width / 2f
                    drawLine(
                        color = color,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokePx,
                        cap = cap,
                        pathEffect = effect,
                    )
                }
            }
        }
    }
}

@Composable
private fun resolveSeparatorColor(color: KiraSeparatorColor): Color {
    val c = KiraTheme.colors
    return when (color) {
        KiraSeparatorColor.Default -> c.border
        KiraSeparatorColor.Primary -> c.primary
        KiraSeparatorColor.Secondary -> c.secondary
        KiraSeparatorColor.Muted -> c.muted
        KiraSeparatorColor.Destructive -> c.destructive
        KiraSeparatorColor.Success -> c.success
        KiraSeparatorColor.Warning -> c.warning
        KiraSeparatorColor.Info -> c.info
    }
}

@Composable
private fun SeparatorChevronGlyph(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(14.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.36f)
            lineTo(size.width * 0.50f, size.height * 0.66f)
            lineTo(size.width * 0.78f, size.height * 0.36f)
        }
        drawPath(path, color = color, style = stroke)
    }
}
