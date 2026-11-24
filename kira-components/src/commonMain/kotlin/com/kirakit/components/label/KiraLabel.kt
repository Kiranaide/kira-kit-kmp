package com.kirakit.components.label

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography

enum class KiraLabelSize(
    internal val fontSize: Int,
    internal val lineHeight: Int,
    internal val hintFontSize: Int,
) {
    Sm(fontSize = 12, lineHeight = 16, hintFontSize = 10),
    Md(fontSize = 14, lineHeight = 20, hintFontSize = 12),
    Lg(fontSize = 16, lineHeight = 22, hintFontSize = 14),
}

enum class KiraLabelValidationState {
    Valid,
    Invalid,
    Validating,
}

fun formatLabelCounter(count: Int, max: Int = 99): String =
    if (count > max) "${max}+" else count.toString()

/**
 * Form label aligned with kira-kit-v2's `Label` & `KiraLabel`.
 *
 * Supports sizes (`Sm`/`Md`/`Lg`), leading/trailing icons, required/optional indicators,
 * badge counter (`99+`), inline tooltip info button, hint text, live character counter
 * (`count`/`maxCount`), description, and accessible live-region validation feedback.
 */
@Composable
fun KiraLabel(
    text: String,
    modifier: Modifier = Modifier,
    size: KiraLabelSize = KiraLabelSize.Md,
    required: Boolean = false,
    requiredIndicator: String = "*",
    optional: Boolean = false,
    optionalText: String = "optional",
    hint: String? = null,
    error: Boolean = false,
    disabled: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    iconAfter: (@Composable () -> Unit)? = null,
    tooltip: String? = null,
    counter: Int? = null,
    counterMax: Int = 99,
    truncate: Boolean = false,
    description: String? = null,
    count: Int? = null,
    maxCount: Int? = null,
    validationState: KiraLabelValidationState? = null,
    validationMessage: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val isInvalid = error || validationState == KiraLabelValidationState.Invalid
    val textColor = if (isInvalid) colors.destructive else colors.foreground
    val hasLiveCount = count != null

    val rowModifier = Modifier
        .then(if (hasLiveCount) Modifier.fillMaxWidth() else Modifier)
        .alpha(if (disabled) 0.5f else 1f)
        .then(
            if (onClick != null && !disabled) {
                Modifier
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
            } else {
                Modifier
            },
        )
        .semantics(mergeDescendants = false) {
            if (disabled) disabled()
        }

    val labelRow: @Composable () -> Unit = {
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier.size(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    icon()
                }
            }

            BasicText(
                text = text,
                modifier = if (truncate) Modifier.weight(1f, fill = false) else Modifier,
                maxLines = if (truncate) 1 else Int.MAX_VALUE,
                overflow = if (truncate) TextOverflow.Ellipsis else TextOverflow.Clip,
                style = KiraTheme.typography.label.copy(
                    color = textColor,
                    fontSize = size.fontSize.sp,
                    lineHeight = size.lineHeight.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )

            if (required) {
                KiraLabelRequired(indicator = requiredIndicator)
            } else if (optional) {
                KiraLabelOptional(text = optionalText)
            }

            if (counter != null) {
                KiraLabelCounter(count = counter, max = counterMax)
            }

            if (iconAfter != null) {
                Box(
                    modifier = Modifier.size(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    iconAfter()
                }
            }

            if (tooltip != null) {
                KiraSimpleTooltip(text = tooltip) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                enabled = !disabled,
                                role = Role.Button,
                                onClick = {},
                            )
                            .padding(2.dp)
                            .semantics {
                                contentDescription = "More information: $tooltip"
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        LabelInfoIcon(color = colors.mutedForeground)
                    }
                }
            }

            if (hint != null) {
                KiraLabelHint(text = hint, size = size)
            }

            if (count != null) {
                Spacer(Modifier.weight(1f))
                KiraLabelCharCount(count = count, maxCount = maxCount)
            }
        }
    }

    if (description == null && validationMessage == null) {
        Box(modifier = modifier) {
            labelRow()
        }
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        labelRow()
        if (description != null) {
            KiraLabelDescription(text = description)
        }
        if (validationMessage != null) {
            val validationColor = when {
                isInvalid -> colors.destructive
                validationState == KiraLabelValidationState.Valid -> colors.success
                else -> colors.mutedForeground
            }
            BasicText(
                text = validationMessage,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = KiraTheme.typography.caption.copy(
                    color = validationColor,
                    fontWeight = FontWeight.Normal,
                ),
            )
        }
    }
}

@Composable
fun KiraLabelRequired(
    indicator: String = "*",
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = indicator,
        modifier = modifier.clearAndSetSemantics {},
        style = KiraTheme.typography.label.copy(
            color = KiraTheme.colors.destructive,
            fontWeight = FontWeight.Normal,
        ),
    )
}

@Composable
fun KiraLabelOptional(
    text: String = "optional",
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = "($text)",
        modifier = modifier.clearAndSetSemantics {},
        style = KiraTheme.typography.caption.copy(
            color = KiraTheme.colors.mutedForeground,
            fontWeight = FontWeight.Normal,
        ),
    )
}

@Composable
fun KiraLabelHint(
    text: String,
    modifier: Modifier = Modifier,
    size: KiraLabelSize = KiraLabelSize.Md,
) {
    BasicText(
        text = text,
        modifier = modifier.padding(start = 2.dp),
        style = KiraTheme.typography.caption.copy(
            color = KiraTheme.colors.mutedForeground,
            fontSize = size.hintFontSize.sp,
            lineHeight = (size.hintFontSize + 4).sp,
            fontWeight = FontWeight.Normal,
        ),
    )
}

@Composable
fun KiraLabelCounter(
    count: Int,
    modifier: Modifier = Modifier,
    max: Int = 99,
) {
    val colors = KiraTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(KiraTheme.radius.shapeFull)
            .background(colors.muted)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = formatLabelCounter(count, max),
            style = KiraTheme.typography.caption.copy(
                color = colors.mutedForeground,
                fontWeight = FontWeight.Medium,
                fontFeatureSettings = "tnum",
            ),
        )
    }
}

@Composable
fun KiraLabelDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = KiraTheme.typography.body.copy(
            color = KiraTheme.colors.mutedForeground,
            fontWeight = FontWeight.Normal,
        ),
    )
}

@Composable
fun KiraLabelGroup(
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    required: Boolean = false,
    optional: Boolean = false,
    error: Boolean = false,
    disabled: Boolean = false,
    size: KiraLabelSize = KiraLabelSize.Md,
    tooltip: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        KiraLabel(
            text = label,
            size = size,
            required = required,
            optional = optional,
            error = error,
            disabled = disabled,
            tooltip = tooltip,
            description = description,
        )
        content()
    }
}

@Composable
private fun KiraLabelCharCount(
    count: Int,
    maxCount: Int?,
) {
    val colors = KiraTheme.colors
    val over = maxCount != null && count > maxCount
    val near = maxCount != null && !over && count >= maxCount * 0.8f
    val color = when {
        over -> colors.destructive
        near -> colors.warning
        else -> colors.mutedForeground
    }
    val text = if (maxCount != null) "$count/$maxCount" else count.toString()
    BasicText(
        text = text,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = KiraTheme.typography.caption.copy(
            color = color,
            fontWeight = if (over) FontWeight.Medium else FontWeight.Normal,
            fontFeatureSettings = "tnum",
        ),
    )
}

@Composable
private fun LabelInfoIcon(color: Color) {
    Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        val radius = size.minDimension / 2f - stroke.width
        drawCircle(color = color, radius = radius, style = stroke)
        // Dot
        drawCircle(
            color = color,
            radius = 0.85.dp.toPx(),
            center = Offset(size.width / 2f, size.height * 0.34f),
        )
        // Stem
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.48f),
            end = Offset(size.width / 2f, size.height * 0.72f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
    }
}
