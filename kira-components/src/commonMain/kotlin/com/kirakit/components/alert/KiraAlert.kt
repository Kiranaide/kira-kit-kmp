package com.kirakit.components.alert

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class KiraAlertVariant {
    Default,
    Info,
    Success,
    Warning,
    Destructive,
    InfoSoft,
    SuccessSoft,
    WarningSoft,
    DestructiveSoft,
    OutlineDefault,
    OutlineInfo,
    OutlineSuccess,
    OutlineWarning,
    OutlineDestructive,
}

enum class KiraAlertSize(
    internal val padding: PaddingValues,
    internal val iconSize: Dp,
    internal val fontSize: Int,
) {
    Xs(PaddingValues(8.dp), 12.dp, 12),
    Sm(PaddingValues(12.dp), 14.dp, 13),
    Default(PaddingValues(16.dp), 16.dp, 14),
    Lg(PaddingValues(20.dp), 20.dp, 16),
}

@Immutable
data class KiraAlertAction(
    val label: String,
    val onClick: () -> Unit,
    val variant: KiraButtonVariant? = null,
)

/**
 * Formats an elapsed duration in seconds into a compact relative label matching `alert.tsx`.
 */
fun formatAlertRelativeSeconds(elapsedSeconds: Long): String {
    val s = elapsedSeconds.coerceAtLeast(0L)
    if (s < 60L) return "just now"
    val m = s / 60L
    if (m < 60L) return "${m}m ago"
    val h = m / 60L
    if (h < 24L) return "${h}h ago"
    val d = h / 24L
    return "${d}d ago"
}

/**
 * Accessible alert banner / callout aligned with kira-kit-v2's `Alert` & `KiraAlert`.
 *
 * Supports all 14 visual variants, 4 size tiers, edge-to-edge `banner` mode, `leftAccent` bar,
 * automatic semantic icons, `expandable` disclosure, `copyable` clipboard action with checkmark flash,
 * `timestampLabel`, inline `actions`, and `dismissible` with optional `undoDismiss` recovery strip.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
@Suppress("DEPRECATION")
fun KiraAlert(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    variant: KiraAlertVariant = KiraAlertVariant.Default,
    size: KiraAlertSize = KiraAlertSize.Default,
    banner: Boolean = false,
    leftAccent: Boolean = false,
    showIcon: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    dismissible: Boolean = false,
    undoDismiss: Boolean = false,
    undoDurationMillis: Long = 5_000L,
    onClose: (() -> Unit)? = null,
    copyable: Boolean = false,
    copyText: String? = null,
    timestampLabel: String? = null,
    expandable: Boolean = false,
    expandedContent: (@Composable () -> Unit)? = null,
    actions: List<KiraAlertAction> = emptyList(),
    content: (@Composable () -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var dismissed by remember { mutableStateOf(false) }
    var undoExpired by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(dismissed, undoDismiss, undoDurationMillis) {
        if (dismissed && undoDismiss && !undoExpired) {
            delay(undoDurationMillis.coerceAtLeast(1_000L))
            undoExpired = true
            onClose?.invoke()
        }
    }

    if (dismissed && (!undoDismiss || undoExpired)) return

    if (dismissed && undoDismiss) {
        val stripShape = if (banner) RectangleShape else KiraTheme.radius.shapeLg
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(stripShape)
                .background(colors.muted.copy(alpha = 0.55f), stripShape)
                .border(1.dp, colors.border, stripShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "Alert dismissed",
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
            KiraButton(
                text = "Undo",
                size = KiraButtonSize.Xs,
                variant = KiraButtonVariant.Outline,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    dismissed = false
                    undoExpired = false
                },
            )
        }
        return
    }

    val toneColor = when (variant) {
        KiraAlertVariant.Info, KiraAlertVariant.InfoSoft, KiraAlertVariant.OutlineInfo -> colors.info
        KiraAlertVariant.Success, KiraAlertVariant.SuccessSoft, KiraAlertVariant.OutlineSuccess -> colors.success
        KiraAlertVariant.Warning, KiraAlertVariant.WarningSoft, KiraAlertVariant.OutlineWarning -> colors.warning
        KiraAlertVariant.Destructive, KiraAlertVariant.DestructiveSoft, KiraAlertVariant.OutlineDestructive -> colors.destructive
        KiraAlertVariant.Default, KiraAlertVariant.OutlineDefault -> colors.border
    }

    val containerBg = when (variant) {
        KiraAlertVariant.Default -> colors.background
        KiraAlertVariant.Info, KiraAlertVariant.InfoSoft -> colors.info.copy(alpha = 0.10f)
        KiraAlertVariant.Success, KiraAlertVariant.SuccessSoft -> colors.success.copy(alpha = 0.10f)
        KiraAlertVariant.Warning, KiraAlertVariant.WarningSoft -> colors.warning.copy(alpha = 0.10f)
        KiraAlertVariant.Destructive, KiraAlertVariant.DestructiveSoft -> colors.destructive.copy(alpha = 0.10f)
        KiraAlertVariant.OutlineDefault,
        KiraAlertVariant.OutlineInfo,
        KiraAlertVariant.OutlineSuccess,
        KiraAlertVariant.OutlineWarning,
        KiraAlertVariant.OutlineDestructive,
        -> Color.Transparent
    }

    val borderColor = when (variant) {
        KiraAlertVariant.Default, KiraAlertVariant.OutlineDefault -> colors.border
        KiraAlertVariant.Info -> colors.info.copy(alpha = 0.30f)
        KiraAlertVariant.Success -> colors.success.copy(alpha = 0.30f)
        KiraAlertVariant.Warning -> colors.warning.copy(alpha = 0.30f)
        KiraAlertVariant.Destructive -> colors.destructive.copy(alpha = 0.30f)
        KiraAlertVariant.OutlineInfo -> colors.info
        KiraAlertVariant.OutlineSuccess -> colors.success
        KiraAlertVariant.OutlineWarning -> colors.warning
        KiraAlertVariant.OutlineDestructive -> colors.destructive
        KiraAlertVariant.InfoSoft,
        KiraAlertVariant.SuccessSoft,
        KiraAlertVariant.WarningSoft,
        KiraAlertVariant.DestructiveSoft,
        -> Color.Transparent
    }

    val hasAutoIcon = variant != KiraAlertVariant.Default && variant != KiraAlertVariant.OutlineDefault
    val renderIcon = showIcon && (icon != null || hasAutoIcon)
    val shape = if (banner) RectangleShape else KiraTheme.radius.shapeLg
    val isAssertive = variant == KiraAlertVariant.Destructive ||
        variant == KiraAlertVariant.DestructiveSoft ||
        variant == KiraAlertVariant.OutlineDestructive

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerBg, shape)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier
                },
            )
            .then(
                if (leftAccent) {
                    Modifier.drawBehind {
                        drawRect(
                            color = toneColor,
                            topLeft = Offset.Zero,
                            size = Size(4.dp.toPx(), this.size.height),
                        )
                    }
                } else {
                    Modifier
                },
            )
            .padding(size.padding)
            .then(if (leftAccent) Modifier.padding(start = 4.dp) else Modifier)
            .semantics {
                liveRegion = if (isAssertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (renderIcon) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(size.iconSize),
                    contentAlignment = Alignment.Center,
                ) {
                    if (icon != null) {
                        icon()
                    } else {
                        AlertSemanticIcon(variant = variant, color = toneColor, size = size.iconSize)
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (title != null) {
                    KiraAlertTitle(text = title, fontSize = size.fontSize)
                }
                if (description != null) {
                    KiraAlertDescription(text = description, fontSize = size.fontSize)
                }
                if (content != null) {
                    content()
                }

                if (expandable && expandedContent != null) {
                    val chevronRotationState = animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = KiraMotion.fastTween(),
                        label = "AlertExpandChevron",
                    )
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(tween(200, easing = KiraMotion.EaseOutStrong)) + fadeIn(tween(150)),
                        exit = shrinkVertically(tween(180, easing = KiraMotion.EaseOutStrong)) + fadeOut(tween(120)),
                    ) {
                        Box(Modifier.padding(top = 6.dp)) {
                            expandedContent()
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(KiraTheme.radius.shapeSm)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    haptics.perform(KiraHapticFeedbackType.Selection)
                                    expanded = !expanded
                                },
                            )
                            .padding(vertical = 4.dp)
                            .semantics {
                                stateDescription = if (expanded) "Expanded" else "Collapsed"
                            },
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = if (expanded) "Show less" else "Show more",
                            style = KiraTheme.typography.caption.copy(
                                color = colors.foreground,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        Canvas(
                            modifier = Modifier
                                .size(12.dp)
                                .graphicsLayer { rotationZ = chevronRotationState.value }
                                .clearAndSetSemantics {},
                        ) {
                            val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            val path = Path().apply {
                                moveTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.35f)
                                lineTo(this@Canvas.size.width * 0.5f, this@Canvas.size.height * 0.68f)
                                lineTo(this@Canvas.size.width * 0.8f, this@Canvas.size.height * 0.35f)
                            }
                            drawPath(path, colors.foreground, style = stroke)
                        }
                    }
                }

                if (actions.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        actions.forEachIndexed { index, action ->
                            KiraButton(
                                text = action.label,
                                size = KiraButtonSize.Sm,
                                variant = action.variant ?: if (index == 0) KiraButtonVariant.Primary else KiraButtonVariant.Outline,
                                onClick = action.onClick,
                            )
                        }
                    }
                }
            }

            if (timestampLabel != null || copyable || dismissible) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (timestampLabel != null) {
                        BasicText(
                            text = timestampLabel,
                            style = KiraTheme.typography.caption.copy(
                                color = colors.mutedForeground,
                            ),
                        )
                    }
                    if (copyable) {
                        val source = copyText ?: description ?: title ?: ""
                        AlertHeaderIconButton(
                            contentDescription = if (copied) "Copied" else "Copy alert text",
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Light)
                                clipboard.setText(AnnotatedString(source))
                                copied = true
                                scope.launch {
                                    delay(1_500L)
                                    copied = false
                                }
                            },
                        ) {
                            if (copied) {
                                AlertCheckGlyph(color = colors.success)
                            } else {
                                AlertCopyGlyph(color = colors.foreground)
                            }
                        }
                    }
                    if (dismissible) {
                        AlertHeaderIconButton(
                            contentDescription = "Close alert",
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Light)
                                dismissed = true
                                if (!undoDismiss) onClose?.invoke()
                            },
                        ) {
                            AlertCloseGlyph(color = colors.foreground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KiraAlertTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
) {
    BasicText(
        text = text,
        modifier = modifier.semantics { heading() },
        style = KiraTheme.typography.label.copy(
            color = KiraTheme.colors.foreground,
            fontSize = fontSize.sp,
            lineHeight = (fontSize + 5).sp,
            fontWeight = FontWeight.SemiBold,
        ),
    )
}

@Composable
fun KiraAlertDescription(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = KiraTheme.typography.body.copy(
            color = KiraTheme.colors.foreground.copy(alpha = 0.88f),
            fontSize = fontSize.sp,
            lineHeight = (fontSize + 6).sp,
        ),
    )
}

@Composable
private fun AlertHeaderIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = KiraTheme.radius.shapeMd

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
            .clip(shape)
            .background(if (hovered) KiraTheme.colors.muted.copy(alpha = 0.7f) else Color.Transparent, shape)
            .alpha(if (hovered) 1f else 0.72f)
            .pointerHoverIcon(PointerIcon.Hand)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

@Composable
private fun AlertSemanticIcon(variant: KiraAlertVariant, color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val r = this.size.minDimension / 2f - stroke.width
        when (variant) {
            KiraAlertVariant.Info, KiraAlertVariant.InfoSoft, KiraAlertVariant.OutlineInfo -> {
                drawCircle(color, radius = r, style = stroke)
                drawCircle(color, radius = 0.9.dp.toPx(), center = Offset(this.size.width / 2f, this.size.height * 0.34f))
                drawLine(color, Offset(this.size.width / 2f, this.size.height * 0.48f), Offset(this.size.width / 2f, this.size.height * 0.72f), stroke.width, StrokeCap.Round)
            }
            KiraAlertVariant.Success, KiraAlertVariant.SuccessSoft, KiraAlertVariant.OutlineSuccess -> {
                drawCircle(color, radius = r, style = stroke)
                val path = Path().apply {
                    moveTo(this@Canvas.size.width * 0.30f, this@Canvas.size.height * 0.52f)
                    lineTo(this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.67f)
                    lineTo(this@Canvas.size.width * 0.72f, this@Canvas.size.height * 0.36f)
                }
                drawPath(path, color, style = stroke)
            }
            KiraAlertVariant.Warning, KiraAlertVariant.WarningSoft, KiraAlertVariant.OutlineWarning -> {
                val path = Path().apply {
                    moveTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.14f)
                    lineTo(this@Canvas.size.width * 0.88f, this@Canvas.size.height * 0.84f)
                    lineTo(this@Canvas.size.width * 0.12f, this@Canvas.size.height * 0.84f)
                    close()
                }
                drawPath(path, color, style = stroke)
                drawLine(color, Offset(this.size.width / 2f, this.size.height * 0.40f), Offset(this.size.width / 2f, this.size.height * 0.62f), stroke.width, StrokeCap.Round)
                drawCircle(color, radius = 0.9.dp.toPx(), center = Offset(this.size.width / 2f, this.size.height * 0.74f))
            }
            KiraAlertVariant.Destructive, KiraAlertVariant.DestructiveSoft, KiraAlertVariant.OutlineDestructive -> {
                drawCircle(color, radius = r, style = stroke)
                drawLine(color, Offset(this.size.width / 2f, this.size.height * 0.28f), Offset(this.size.width / 2f, this.size.height * 0.56f), stroke.width, StrokeCap.Round)
                drawCircle(color, radius = 0.9.dp.toPx(), center = Offset(this.size.width / 2f, this.size.height * 0.72f))
            }
            else -> Unit
        }
    }
}

@Composable
private fun AlertCopyGlyph(color: Color) {
    Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.4.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.32f, size.height * 0.32f),
            size = Size(size.width * 0.56f, size.height * 0.56f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = stroke,
        )
        drawLine(color, Offset(size.width * 0.16f, size.height * 0.62f), Offset(size.width * 0.16f, size.height * 0.16f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.16f, size.height * 0.16f), Offset(size.width * 0.62f, size.height * 0.16f), stroke.width, StrokeCap.Round)
    }
}

@Composable
private fun AlertCheckGlyph(color: Color) {
    Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.52f)
            lineTo(size.width * 0.42f, size.height * 0.76f)
            lineTo(size.width * 0.82f, size.height * 0.26f)
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun AlertCloseGlyph(color: Color) {
    Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
        val strokeWidth = 1.7.dp.toPx()
        val pad = size.width * 0.2f
        drawLine(color, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeWidth, StrokeCap.Round)
    }
}
