package com.kirakit.components.avatar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraAvatarSize(
    val diameter: Dp,
    internal val fontSize: Int,
    internal val statusSize: Dp,
    internal val badgeSize: Dp,
    internal val badgeFontSize: Int,
) {
    Xs(24.dp, 10, 10.dp, 12.dp, 8),
    Sm(32.dp, 12, 12.dp, 16.dp, 10),
    Default(40.dp, 14, 14.dp, 20.dp, 11),
    Lg(48.dp, 16, 16.dp, 24.dp, 12),
    Xl(64.dp, 18, 20.dp, 28.dp, 13),
    Xxl(80.dp, 20, 24.dp, 32.dp, 14),
    Xxxl(96.dp, 24, 28.dp, 40.dp, 15),
    Xxxxl(128.dp, 30, 32.dp, 48.dp, 18),
}

enum class KiraAvatarShape {
    Circle,
    Rounded,
    Square,
}

enum class KiraAvatarRing {
    Default,
    Primary,
    Secondary,
    Destructive,
    Success,
    Warning,
    Info,
}

enum class KiraAvatarStatus {
    Online,
    Offline,
    Busy,
    Away,
    OfflineGray,
}

enum class KiraAvatarCornerPosition {
    TopRight,
    TopLeft,
    BottomRight,
    BottomLeft,
}

enum class KiraAvatarGradient {
    Default,
    Blue,
    Purple,
    Green,
    Orange,
    Pink,
}

enum class KiraAvatarBadgeVariant {
    Default,
    Secondary,
    Destructive,
    Success,
    Warning,
    Info,
    Outline,
}

@Immutable
data class KiraAvatarGroupMember(
    val name: String,
    val fallback: String? = null,
    val painter: Painter? = null,
    val status: KiraAvatarStatus? = null,
    val gradient: KiraAvatarGradient? = null,
)

/**
 * Extracts 1–2 uppercase initials from a full name, matching `getInitials` in `avatar.tsx`.
 */
fun getAvatarInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "?"
    if (parts.size == 1) return parts[0].take(2).uppercase()
    return "${parts.first().first()}${parts.last().first()}".uppercase()
}

/**
 * Versatile avatar component aligned with kira-kit-v2's `Avatar` & `KiraAvatar`.
 *
 * Supports all 8 size tiers (`Xs`..`Xxxxl`), 3 shapes (`Circle`, `Rounded`, `Square`), semantic rings,
 * gradient fallbacks, unclipped corner status dots (with optional online pulse & accessible `statusLabel`),
 * unclipped corner badges, `lastSeen` hover/long-press tooltip, and interactive button mode.
 */
@Composable
fun KiraAvatar(
    modifier: Modifier = Modifier,
    name: String? = null,
    fallback: String? = null,
    painter: Painter? = null,
    imageContent: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    size: KiraAvatarSize = KiraAvatarSize.Default,
    shape: KiraAvatarShape = KiraAvatarShape.Circle,
    ring: KiraAvatarRing = KiraAvatarRing.Default,
    gradient: KiraAvatarGradient? = null,
    status: KiraAvatarStatus? = null,
    statusPosition: KiraAvatarCornerPosition = KiraAvatarCornerPosition.BottomRight,
    statusPulse: Boolean = false,
    statusLabel: String? = null,
    badge: (@Composable () -> Unit)? = null,
    badgeText: String? = null,
    badgeVariant: KiraAvatarBadgeVariant = KiraAvatarBadgeVariant.Destructive,
    badgePosition: KiraAvatarCornerPosition = KiraAvatarCornerPosition.TopRight,
    lastSeen: String? = null,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = name,
) {
    val body: @Composable () -> Unit = {
        AvatarCore(
            modifier = modifier,
            name = name,
            fallback = fallback,
            painter = painter,
            imageContent = imageContent,
            icon = icon,
            size = size,
            shape = shape,
            ring = ring,
            gradient = gradient,
            status = status,
            statusPosition = statusPosition,
            statusPulse = statusPulse,
            statusLabel = statusLabel,
            badge = badge,
            badgeText = badgeText,
            badgeVariant = badgeVariant,
            badgePosition = badgePosition,
            onClick = onClick,
            contentDescription = contentDescription,
        )
    }

    if (lastSeen != null) {
        KiraSimpleTooltip(text = "Last seen $lastSeen") {
            body()
        }
    } else {
        body()
    }
}

@Composable
private fun AvatarCore(
    modifier: Modifier,
    name: String?,
    fallback: String?,
    painter: Painter?,
    imageContent: (@Composable () -> Unit)?,
    icon: (@Composable () -> Unit)?,
    size: KiraAvatarSize,
    shape: KiraAvatarShape,
    ring: KiraAvatarRing,
    gradient: KiraAvatarGradient?,
    status: KiraAvatarStatus?,
    statusPosition: KiraAvatarCornerPosition,
    statusPulse: Boolean,
    statusLabel: String?,
    badge: (@Composable () -> Unit)?,
    badgeText: String?,
    badgeVariant: KiraAvatarBadgeVariant,
    badgePosition: KiraAvatarCornerPosition,
    onClick: (() -> Unit)?,
    contentDescription: String?,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val isInteractive = onClick != null
    val interactionSource = if (isInteractive) remember { MutableInteractionSource() } else null
    val hovered = if (interactionSource != null) interactionSource.collectIsHoveredAsState().value else false
    val pressed = if (interactionSource != null) interactionSource.collectIsPressedAsState().value else false
    val focused = if (interactionSource != null) interactionSource.collectIsFocusedAsState().value else false

    val scaleState = if (isInteractive) {
        animateFloatAsState(
            targetValue = if (pressed) 0.95f else 1f,
            animationSpec = KiraMotion.fastTween(),
            label = "AvatarScale",
        )
    } else {
        null
    }

    val composeShape = when (shape) {
        KiraAvatarShape.Circle -> CircleShape
        KiraAvatarShape.Rounded -> KiraTheme.radius.shapeLg
        KiraAvatarShape.Square -> RectangleShape
    }

    val ringColor = when (ring) {
        KiraAvatarRing.Default -> Color.Transparent
        KiraAvatarRing.Primary -> colors.primary
        KiraAvatarRing.Secondary -> colors.secondary
        KiraAvatarRing.Destructive -> colors.destructive
        KiraAvatarRing.Success -> colors.success
        KiraAvatarRing.Warning -> colors.warning
        KiraAvatarRing.Info -> colors.info
    }

    val fallbackBrush = remember(gradient, colors.primary) {
        gradient?.let { avatarGradientBrush(it, colors.primary) }
    }
    val resolvedInitials = remember(fallback, name) {
        fallback ?: name?.let { getAvatarInitials(it) } ?: "?"
    }

    val accessibleText = buildString {
        val base = contentDescription ?: if (isInteractive) "Avatar" else ""
        append(base)
        if (!statusLabel.isNullOrBlank()) {
            if (isNotEmpty()) append(", ")
            append(statusLabel)
        }
    }

    Box(modifier = modifier) {
        val ringPad = if (ring != KiraAvatarRing.Default) 4.dp else 0.dp
        Box(
            modifier = Modifier
                .then(
                    if (scaleState != null) {
                        Modifier.graphicsLayer {
                            val s = scaleState.value
                            scaleX = s
                            scaleY = s
                        }
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (ring != KiraAvatarRing.Default) {
                        Modifier
                            .border(2.dp, ringColor, composeShape)
                            .padding(ringPad)
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (isInteractive && focused) {
                        Modifier.border(2.dp, colors.ring, composeShape)
                    } else {
                        Modifier
                    },
                )
                .size(size.diameter)
                .then(if (isInteractive && hovered) Modifier.graphicsLayer { alpha = 0.90f } else Modifier)
                .clip(composeShape)
                .then(
                    if (fallbackBrush != null) {
                        Modifier.background(fallbackBrush, composeShape)
                    } else {
                        Modifier.background(colors.muted, composeShape)
                    },
                )
                .then(
                    if (isInteractive && interactionSource != null) {
                        Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .hoverable(interactionSource)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                role = Role.Button,
                                onClick = {
                                    haptics.perform(KiraHapticFeedbackType.Selection)
                                    onClick()
                                },
                            )
                    } else {
                        Modifier
                    },
                )
                .semantics {
                    if (accessibleText.isNotEmpty()) this.contentDescription = accessibleText
                },
            contentAlignment = Alignment.Center,
        ) {
            when {
                imageContent != null -> imageContent()
                painter != null -> Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size.diameter),
                )
                icon != null -> icon()
                else -> BasicText(
                    text = resolvedInitials,
                    maxLines = 1,
                    style = KiraTheme.typography.label.copy(
                        color = if (fallbackBrush != null) colors.foreground else colors.mutedForeground,
                        fontSize = size.fontSize.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }

        if (status != null) {
            AvatarStatusDot(
                status = status,
                size = size.statusSize,
                pulse = statusPulse && status == KiraAvatarStatus.Online,
                modifier = Modifier.align(statusPosition.toAlignment()),
            )
        }

        if (badge != null) {
            Box(modifier = Modifier.align(badgePosition.toAlignment())) {
                badge()
            }
        } else if (badgeText != null) {
            KiraAvatarBadge(
                text = badgeText,
                variant = badgeVariant,
                size = size,
                modifier = Modifier
                    .align(badgePosition.toAlignment())
                    .offset(
                        x = if (badgePosition == KiraAvatarCornerPosition.TopRight || badgePosition == KiraAvatarCornerPosition.BottomRight) 4.dp else (-4).dp,
                        y = if (badgePosition == KiraAvatarCornerPosition.TopRight || badgePosition == KiraAvatarCornerPosition.TopLeft) (-4).dp else 4.dp,
                    ),
            )
        }
    }
}

@Composable
fun KiraAvatarBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: KiraAvatarBadgeVariant = KiraAvatarBadgeVariant.Destructive,
    size: KiraAvatarSize = KiraAvatarSize.Default,
) {
    val colors = KiraTheme.colors
    val (bg, fg, borderColor) = when (variant) {
        KiraAvatarBadgeVariant.Default -> Triple(colors.primary, colors.primaryForeground, colors.background)
        KiraAvatarBadgeVariant.Secondary -> Triple(colors.secondary, colors.secondaryForeground, colors.background)
        KiraAvatarBadgeVariant.Destructive -> Triple(colors.destructive, colors.destructiveForeground, colors.background)
        KiraAvatarBadgeVariant.Success -> Triple(colors.success, colors.successForeground, colors.background)
        KiraAvatarBadgeVariant.Warning -> Triple(colors.warning, colors.warningForeground, colors.background)
        KiraAvatarBadgeVariant.Info -> Triple(colors.info, colors.infoForeground, colors.background)
        KiraAvatarBadgeVariant.Outline -> Triple(colors.background, colors.foreground, colors.border)
    }

    Box(
        modifier = modifier
            .size(size.badgeSize)
            .clip(CircleShape)
            .background(bg, CircleShape)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            maxLines = 1,
            style = KiraTheme.typography.caption.copy(
                color = fg,
                fontSize = size.badgeFontSize.sp,
                lineHeight = size.badgeFontSize.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

/**
 * Three-dot animated typing presence indicator matching `AvatarTyping` in `avatar.tsx`.
 */
@Composable
fun KiraAvatarTyping(modifier: Modifier = Modifier) {
    val colors = KiraTheme.colors
    val transition = rememberInfiniteTransition(label = "AvatarTyping")
    Row(
        modifier = modifier.semantics { contentDescription = "typing" },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            val phaseState = transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 550, delayMillis = i * 140, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "TypingDot$i",
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .graphicsLayer { alpha = phaseState.value }
                    .clip(CircleShape)
                    .background(colors.mutedForeground, CircleShape),
            )
        }
    }
}

/**
 * Overlapped avatar group with `+N` overflow popover, per-avatar tooltips, and optional `+` add-member control.
 */
@Composable
fun KiraAvatarGroup(
    members: List<KiraAvatarGroupMember>,
    modifier: Modifier = Modifier,
    max: Int = 3,
    overlap: Dp = 8.dp,
    size: KiraAvatarSize = KiraAvatarSize.Default,
    tooltips: Boolean = false,
    onAddMember: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val visible = members.take(max.coerceAtLeast(1))
    val hidden = members.drop(visible.size)
    var showOverflow by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.semantics { contentDescription = "Avatar group" },
        horizontalArrangement = Arrangement.spacedBy(-overlap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visible.forEachIndexed { index, member ->
            val avatarNode: @Composable () -> Unit = {
                Box(
                    modifier = Modifier
                        .zIndex((visible.size - index).toFloat())
                        .border(2.dp, colors.background, CircleShape)
                        .clip(CircleShape),
                ) {
                    KiraAvatar(
                        name = member.name,
                        fallback = member.fallback,
                        painter = member.painter,
                        status = member.status,
                        gradient = member.gradient,
                        size = size,
                    )
                }
            }
            if (tooltips) {
                KiraSimpleTooltip(text = member.name) {
                    avatarNode()
                }
            } else {
                avatarNode()
            }
        }

        if (hidden.isNotEmpty()) {
            Box(modifier = Modifier.zIndex(0f)) {
                Box(
                    modifier = Modifier
                        .size(size.diameter)
                        .clip(CircleShape)
                        .background(colors.muted, CircleShape)
                        .border(2.dp, colors.background, CircleShape)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Selection)
                                showOverflow = !showOverflow
                            },
                        )
                        .semantics { contentDescription = "Show ${hidden.size} more" },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "+${hidden.size}",
                        style = KiraTheme.typography.caption.copy(
                            color = colors.mutedForeground,
                            fontSize = size.fontSize.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }

                if (showOverflow) {
                    Popup(
                        alignment = Alignment.TopCenter,
                        onDismissRequest = { showOverflow = false },
                        properties = PopupProperties(focusable = true),
                    ) {
                        val popShape = KiraTheme.radius.shapeLg
                        Column(
                            modifier = Modifier
                                .padding(top = size.diameter + 6.dp)
                                .widthIn(min = 140.dp, max = 240.dp)
                                .shadow(10.dp, popShape, clip = false)
                                .clip(popShape)
                                .background(colors.popover, popShape)
                                .border(1.dp, colors.border, popShape)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            hidden.forEach { member ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    KiraAvatar(
                                        name = member.name,
                                        fallback = member.fallback,
                                        painter = member.painter,
                                        gradient = member.gradient,
                                        size = KiraAvatarSize.Sm,
                                    )
                                    BasicText(
                                        text = member.name,
                                        style = KiraTheme.typography.bodySmall.copy(color = colors.popoverForeground),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (onAddMember != null) {
            Box(
                modifier = Modifier
                    .zIndex(-1f)
                    .size(size.diameter)
                    .clip(CircleShape)
                    .background(colors.muted.copy(alpha = 0.7f), CircleShape)
                    .border(1.dp, colors.border, CircleShape)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            haptics.perform(KiraHapticFeedbackType.Selection)
                            onAddMember()
                        },
                    )
                    .semantics { contentDescription = "Add member" },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
                    val sw = 1.6.dp.toPx()
                    val p = this.size.width * 0.18f
                    drawLine(colors.mutedForeground, Offset(this.size.width / 2f, p), Offset(this.size.width / 2f, this.size.height - p), sw, StrokeCap.Round)
                    drawLine(colors.mutedForeground, Offset(p, this.size.height / 2f), Offset(this.size.width - p, this.size.height / 2f), sw, StrokeCap.Round)
                }
            }
        }
    }
}

/**
 * Horizontally scrollable overlapped avatar stack with optional total count badge (`AvatarStack`).
 */
@Composable
fun KiraAvatarStack(
    members: List<KiraAvatarGroupMember>,
    modifier: Modifier = Modifier,
    size: KiraAvatarSize = KiraAvatarSize.Default,
    showCount: Boolean = true,
) {
    val colors = KiraTheme.colors
    val overlap = (size.diameter / 3f).coerceAtMost(14.dp)

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(-overlap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        members.forEachIndexed { index, member ->
            Box(
                modifier = Modifier
                    .zIndex((members.size - index).toFloat())
                    .border(2.dp, colors.background, CircleShape)
                    .clip(CircleShape),
            ) {
                KiraAvatar(
                    name = member.name,
                    fallback = member.fallback,
                    painter = member.painter,
                    gradient = member.gradient,
                    size = size,
                )
            }
        }
        if (showCount && members.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .zIndex(0f)
                    .size(size.diameter)
                    .clip(CircleShape)
                    .background(colors.muted, CircleShape)
                    .border(2.dp, colors.background, CircleShape)
                    .semantics { contentDescription = "${members.size} members" },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = members.size.toString(),
                    style = KiraTheme.typography.caption.copy(
                        color = colors.mutedForeground,
                        fontSize = size.fontSize.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AvatarStatusDot(
    status: KiraAvatarStatus,
    size: Dp,
    pulse: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val dotColor = when (status) {
        KiraAvatarStatus.Online -> colors.success
        KiraAvatarStatus.Offline -> colors.mutedForeground.copy(alpha = 0.55f)
        KiraAvatarStatus.Busy -> colors.destructive
        KiraAvatarStatus.Away -> colors.warning
        KiraAvatarStatus.OfflineGray -> colors.muted
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (pulse) {
            val transition = rememberInfiniteTransition(label = "AvatarStatusPulse")
            val pulseScaleState = transition.animateFloat(
                initialValue = 1f,
                targetValue = 1.9f,
                animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing)),
                label = "StatusPulseScale",
            )
            val pulseAlphaState = transition.animateFloat(
                initialValue = 0.65f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing)),
                label = "StatusPulseAlpha",
            )
            Box(
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        val s = pulseScaleState.value
                        scaleX = s
                        scaleY = s
                        alpha = pulseAlphaState.value
                    }
                    .clip(CircleShape)
                    .background(dotColor, CircleShape),
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(dotColor, CircleShape)
                .border(2.dp, colors.background, CircleShape),
        )
    }
}

private fun KiraAvatarCornerPosition.toAlignment(): Alignment = when (this) {
    KiraAvatarCornerPosition.TopRight -> Alignment.TopEnd
    KiraAvatarCornerPosition.TopLeft -> Alignment.TopStart
    KiraAvatarCornerPosition.BottomRight -> Alignment.BottomEnd
    KiraAvatarCornerPosition.BottomLeft -> Alignment.BottomStart
}

private fun avatarGradientBrush(gradient: KiraAvatarGradient, primary: Color): Brush {
    val base = when (gradient) {
        KiraAvatarGradient.Default -> primary
        KiraAvatarGradient.Blue -> Color(0xFF60A5FA)
        KiraAvatarGradient.Purple -> Color(0xFFC084FC)
        KiraAvatarGradient.Green -> Color(0xFF4ADE80)
        KiraAvatarGradient.Orange -> Color(0xFFFB923C)
        KiraAvatarGradient.Pink -> Color(0xFFF472B6)
    }
    return Brush.linearGradient(
        colors = listOf(base.copy(alpha = 0.28f), base.copy(alpha = 0.14f), base.copy(alpha = 0.06f)),
    )
}
