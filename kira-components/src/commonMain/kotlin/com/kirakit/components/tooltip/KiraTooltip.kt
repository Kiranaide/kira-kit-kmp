package com.kirakit.components.tooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class KiraTooltipVariant {
    Default,
    Secondary,
    Outline,
    Muted,
    Destructive,
}

enum class KiraTooltipSize(
    internal val padding: PaddingValues,
    internal val fontSize: Int,
) {
    Sm(PaddingValues(horizontal = 8.dp, vertical = 4.dp), 12),
    Default(PaddingValues(horizontal = 12.dp, vertical = 6.dp), 14),
    Lg(PaddingValues(horizontal = 16.dp, vertical = 8.dp), 16),
}

enum class KiraTooltipSide {
    Top,
    Right,
    Bottom,
    Left,
}

enum class KiraTooltipAlign {
    Start,
    Center,
    End,
}

@Immutable
data class KiraTooltipProviderConfig(
    val delayMillis: Long = 500L,
    val closeDelayMillis: Long = 200L,
    val touchAutoDismissMillis: Long = 2_400L,
)

private val LocalKiraTooltipConfig = compositionLocalOf { KiraTooltipProviderConfig() }

@Composable
fun KiraTooltipProvider(
    delayMillis: Long = 500L,
    closeDelayMillis: Long = 200L,
    touchAutoDismissMillis: Long = 2_400L,
    content: @Composable () -> Unit,
) {
    val config = remember(delayMillis, closeDelayMillis, touchAutoDismissMillis) {
        KiraTooltipProviderConfig(
            delayMillis = delayMillis.coerceAtLeast(0L),
            closeDelayMillis = closeDelayMillis.coerceAtLeast(0L),
            touchAutoDismissMillis = touchAutoDismissMillis.coerceAtLeast(600L),
        )
    }
    CompositionLocalProvider(LocalKiraTooltipConfig provides config, content = content)
}

@Composable
fun KiraTooltipShortcut(
    keys: List<String>,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    if (keys.isEmpty()) return
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keys.forEach { key ->
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .widthIn(min = 20.dp)
                    .background(contentColor.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                    .border(1.dp, contentColor.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = key,
                    style = KiraTheme.typography.caption.copy(
                        color = contentColor,
                        fontSize = 11.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

/**
 * Accessible, multi-platform tooltip aligned with kira-kit-v2's `SimpleTooltip`.
 *
 * - **Desktop / Keyboard**: opens on pointer hover (with configurable delay) or keyboard focus;
 *   pressing `Escape` dismisses immediately.
 * - **Mobile / Touch**: long-press opens with subtle haptic feedback and auto-dismisses or closes
 *   when tapping outside.
 */
@Composable
fun KiraSimpleTooltip(
    text: String,
    modifier: Modifier = Modifier,
    variant: KiraTooltipVariant = KiraTooltipVariant.Default,
    size: KiraTooltipSize = KiraTooltipSize.Default,
    side: KiraTooltipSide = KiraTooltipSide.Top,
    align: KiraTooltipAlign = KiraTooltipAlign.Center,
    sideOffset: Dp = 8.dp,
    collisionPadding: Dp = 8.dp,
    maxWidth: Dp = 250.dp,
    showArrow: Boolean = true,
    shortcut: List<String> = emptyList(),
    delayMillis: Long? = null,
    closeDelayMillis: Long? = null,
    disabled: Boolean = false,
    open: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    KiraTooltip(
        tooltip = { contentColor ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = text,
                    style = KiraTheme.typography.body.copy(
                        color = contentColor,
                        fontSize = size.fontSize.sp,
                        lineHeight = (size.fontSize + 5).sp,
                    ),
                )
                if (shortcut.isNotEmpty()) {
                    KiraTooltipShortcut(keys = shortcut, contentColor = contentColor)
                }
            }
        },
        modifier = modifier,
        variant = variant,
        size = size,
        side = side,
        align = align,
        sideOffset = sideOffset,
        collisionPadding = collisionPadding,
        maxWidth = maxWidth,
        showArrow = showArrow,
        delayMillis = delayMillis,
        closeDelayMillis = closeDelayMillis,
        disabled = disabled,
        open = open,
        onOpenChange = onOpenChange,
        content = content,
    )
}

@Composable
fun KiraTooltip(
    tooltip: @Composable (contentColor: Color) -> Unit,
    modifier: Modifier = Modifier,
    variant: KiraTooltipVariant = KiraTooltipVariant.Default,
    size: KiraTooltipSize = KiraTooltipSize.Default,
    side: KiraTooltipSide = KiraTooltipSide.Top,
    align: KiraTooltipAlign = KiraTooltipAlign.Center,
    sideOffset: Dp = 8.dp,
    collisionPadding: Dp = 8.dp,
    maxWidth: Dp = 250.dp,
    showArrow: Boolean = true,
    delayMillis: Long? = null,
    closeDelayMillis: Long? = null,
    disabled: Boolean = false,
    open: Boolean? = null,
    onOpenChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val config = LocalKiraTooltipConfig.current
    val resolvedDelay = (delayMillis ?: config.delayMillis).coerceAtLeast(0L)
    val resolvedCloseDelay = (closeDelayMillis ?: config.closeDelayMillis).coerceAtLeast(0L)
    val haptics = rememberKiraHaptics()

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    var internalOpen by remember { mutableStateOf(false) }
    var touchTriggered by remember { mutableStateOf(false) }
    val isControlled = open != null
    val visible = !disabled && (open ?: internalOpen)

    fun setVisible(next: Boolean) {
        if (!isControlled) internalOpen = next
        onOpenChange?.invoke(next)
    }

    LaunchedEffect(hovered, focused, disabled, resolvedDelay, resolvedCloseDelay) {
        if (disabled) {
            touchTriggered = false
            setVisible(false)
            return@LaunchedEffect
        }
        if (hovered || focused) {
            if (!visible) {
                if (resolvedDelay > 0L) delay(resolvedDelay)
                setVisible(true)
            }
        } else if (!touchTriggered && visible) {
            if (resolvedCloseDelay > 0L) delay(resolvedCloseDelay)
            setVisible(false)
        }
    }

    LaunchedEffect(touchTriggered, visible) {
        if (touchTriggered && visible) {
            delay(config.touchAutoDismissMillis)
            touchTriggered = false
            setVisible(false)
        }
    }

    val density = LocalDensity.current
    var resolvedSide by remember(side) { mutableStateOf(side) }
    val positionProvider = remember(side, align, sideOffset, collisionPadding, density) {
        KiraTooltipPositionProvider(
            preferredSide = side,
            align = align,
            sideOffsetPx = with(density) { sideOffset.roundToPx() },
            collisionPaddingPx = with(density) { collisionPadding.roundToPx() },
            onSideResolved = { resolvedSide = it },
        )
    }

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = visible

    Box(
        modifier = modifier
            .hoverable(interactionSource, enabled = !disabled)
            .onPreviewKeyEvent { event ->
                if (visible && event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                    touchTriggered = false
                    setVisible(false)
                    true
                } else {
                    false
                }
            }
            .pointerInput(disabled) {
                if (disabled) return@pointerInput
                detectTapGestures(
                    onLongPress = {
                        haptics.perform(KiraHapticFeedbackType.Light)
                        touchTriggered = true
                        setVisible(true)
                    },
                )
            },
    ) {
        content()

        if (transitionState.currentState || transitionState.targetState) {
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = {
                    touchTriggered = false
                    setVisible(false)
                },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = touchTriggered,
                ),
            ) {
                TooltipAnimatedContainer(
                    visibleState = transitionState,
                    side = resolvedSide,
                ) {
                    TooltipSurface(
                        variant = variant,
                        size = size,
                        side = resolvedSide,
                        maxWidth = maxWidth,
                        showArrow = showArrow,
                        content = tooltip,
                    )
                }
            }
        }
    }
}

@Immutable
data class KiraSharedTooltipPayload(
    val label: String,
    val shortcut: List<String> = emptyList(),
)

@Stable
class KiraSharedTooltipHandle {
    internal var activePayload by mutableStateOf<KiraSharedTooltipPayload?>(null)
    internal var anchorBounds by mutableStateOf<IntRect?>(null)
    internal var open by mutableStateOf(false)
    internal var pendingJob: Job? = null

    fun dismiss() {
        pendingJob?.cancel()
        open = false
        activePayload = null
    }
}

@Composable
fun rememberKiraSharedTooltipHandle(): KiraSharedTooltipHandle =
    remember { KiraSharedTooltipHandle() }

private class TooltipCoordsRef {
    var value: LayoutCoordinates? = null
}

@Composable
fun KiraSharedTooltipTrigger(
    handle: KiraSharedTooltipHandle,
    label: String,
    modifier: Modifier = Modifier,
    shortcut: List<String> = emptyList(),
    delayMillis: Long = 200L,
    closeDelayMillis: Long = 180L,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptics = rememberKiraHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    // Non-state ref prevents recomposition on every scroll/layout pass.
    val coordsRef = remember { TooltipCoordsRef() }
    val payload = remember(label, shortcut) { KiraSharedTooltipPayload(label, shortcut) }

    LaunchedEffect(hovered, focused, payload) {
        if (hovered || focused) {
            handle.pendingJob?.cancel()
            val alreadyOpen = handle.open
            handle.pendingJob = scope.launch {
                if (!alreadyOpen && delayMillis > 0L) delay(delayMillis)
                coordsRef.value?.takeIf { it.isAttached }?.boundsInWindow()?.let { rect ->
                    handle.anchorBounds = IntRect(
                        left = rect.left.toInt(),
                        top = rect.top.toInt(),
                        right = rect.right.toInt(),
                        bottom = rect.bottom.toInt(),
                    )
                }
                handle.activePayload = payload
                handle.open = true
            }
        } else if (handle.activePayload == payload) {
            handle.pendingJob?.cancel()
            handle.pendingJob = scope.launch {
                if (closeDelayMillis > 0L) delay(closeDelayMillis)
                if (handle.activePayload == payload) {
                    handle.open = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordsRef.value = it }
            .hoverable(interactionSource)
            .pointerInput(payload) {
                detectTapGestures(
                    onLongPress = {
                        haptics.perform(KiraHapticFeedbackType.Light)
                        coordsRef.value?.takeIf { it.isAttached }?.boundsInWindow()?.let { rect ->
                            handle.anchorBounds = IntRect(
                                left = rect.left.toInt(),
                                top = rect.top.toInt(),
                                right = rect.right.toInt(),
                                bottom = rect.bottom.toInt(),
                            )
                        }
                        handle.activePayload = payload
                        handle.open = true
                    },
                )
            },
    ) {
        content()
    }
}

@Composable
fun KiraSharedTooltip(
    handle: KiraSharedTooltipHandle,
    variant: KiraTooltipVariant = KiraTooltipVariant.Default,
    size: KiraTooltipSize = KiraTooltipSize.Default,
    side: KiraTooltipSide = KiraTooltipSide.Top,
    align: KiraTooltipAlign = KiraTooltipAlign.Center,
    sideOffset: Dp = 8.dp,
    collisionPadding: Dp = 8.dp,
    maxWidth: Dp = 250.dp,
    showArrow: Boolean = true,
) {
    val transitionState = remember { MutableTransitionState(false) }
    var lastPayload by remember { mutableStateOf<KiraSharedTooltipPayload?>(null) }
    var lastAnchor by remember { mutableStateOf<IntRect?>(null) }

    if (handle.activePayload != null) lastPayload = handle.activePayload
    if (handle.anchorBounds != null) lastAnchor = handle.anchorBounds
    transitionState.targetState = handle.open && handle.activePayload != null && handle.anchorBounds != null

    val payload = lastPayload ?: return
    val anchor = lastAnchor ?: return
    if (!transitionState.currentState && !transitionState.targetState) return

    val density = LocalDensity.current
    var resolvedSide by remember(side) { mutableStateOf(side) }
    val provider = remember(anchor, side, align, sideOffset, collisionPadding, density) {
        val delegate = KiraTooltipPositionProvider(
            preferredSide = side,
            align = align,
            sideOffsetPx = with(density) { sideOffset.roundToPx() },
            collisionPaddingPx = with(density) { collisionPadding.roundToPx() },
            onSideResolved = { resolvedSide = it },
        )
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset = delegate.calculatePosition(anchor, windowSize, layoutDirection, popupContentSize)
        }
    }

    Popup(
        popupPositionProvider = provider,
        onDismissRequest = { handle.dismiss() },
        properties = PopupProperties(focusable = false, dismissOnBackPress = true),
    ) {
        TooltipAnimatedContainer(
            visibleState = transitionState,
            side = resolvedSide,
        ) {
            TooltipSurface(
                variant = variant,
                size = size,
                side = resolvedSide,
                maxWidth = maxWidth,
                showArrow = showArrow,
            ) { contentColor ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = payload.label,
                        style = KiraTheme.typography.body.copy(
                            color = contentColor,
                            fontSize = size.fontSize.sp,
                            lineHeight = (size.fontSize + 5).sp,
                        ),
                    )
                    if (payload.shortcut.isNotEmpty()) {
                        KiraTooltipShortcut(keys = payload.shortcut, contentColor = contentColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun TooltipAnimatedContainer(
    visibleState: MutableTransitionState<Boolean>,
    side: KiraTooltipSide,
    content: @Composable () -> Unit,
) {
    val enterTween = tween<Float>(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)
    val slideOffsetPx = with(LocalDensity.current) { 5.dp.roundToPx() }
    val slideEnter = when (side) {
        KiraTooltipSide.Top -> slideInVertically(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) { slideOffsetPx }
        KiraTooltipSide.Bottom -> slideInVertically(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) { -slideOffsetPx }
        KiraTooltipSide.Left -> slideInHorizontally(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) { slideOffsetPx }
        KiraTooltipSide.Right -> slideInHorizontally(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) { -slideOffsetPx }
    }
    val slideExit = when (side) {
        KiraTooltipSide.Top -> slideOutVertically(tween(120, easing = KiraMotion.EaseOutStrong)) { slideOffsetPx }
        KiraTooltipSide.Bottom -> slideOutVertically(tween(120, easing = KiraMotion.EaseOutStrong)) { -slideOffsetPx }
        KiraTooltipSide.Left -> slideOutHorizontally(tween(120, easing = KiraMotion.EaseOutStrong)) { slideOffsetPx }
        KiraTooltipSide.Right -> slideOutHorizontally(tween(120, easing = KiraMotion.EaseOutStrong)) { -slideOffsetPx }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        modifier = Modifier.padding(6.dp),
        enter = fadeIn(enterTween) + scaleIn(enterTween, initialScale = 0.92f) + slideEnter,
        exit = fadeOut(tween(120, easing = KiraMotion.EaseOutStrong)) +
            scaleOut(tween(120, easing = KiraMotion.EaseOutStrong), targetScale = 0.94f) + slideExit,
    ) {
        content()
    }
}

@Composable
private fun TooltipSurface(
    variant: KiraTooltipVariant,
    size: KiraTooltipSize,
    side: KiraTooltipSide,
    maxWidth: Dp,
    showArrow: Boolean,
    content: @Composable (Color) -> Unit,
) {
    val colors = KiraTheme.colors
    val containerColor = when (variant) {
        KiraTooltipVariant.Default -> colors.primary
        KiraTooltipVariant.Secondary -> colors.secondary
        KiraTooltipVariant.Outline -> colors.background
        KiraTooltipVariant.Muted -> colors.muted
        KiraTooltipVariant.Destructive -> colors.destructive
    }
    val contentColor = when (variant) {
        KiraTooltipVariant.Default -> colors.primaryForeground
        KiraTooltipVariant.Secondary -> colors.secondaryForeground
        KiraTooltipVariant.Outline -> colors.foreground
        KiraTooltipVariant.Muted -> colors.mutedForeground
        KiraTooltipVariant.Destructive -> colors.destructiveForeground
    }
    val borderColor = if (variant == KiraTooltipVariant.Outline) colors.border else null
    val shape = KiraTheme.radius.shapeMd

    val body: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .shadow(6.dp, shape, clip = false)
                .background(containerColor, shape)
                .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
                .padding(size.padding)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            content(contentColor)
        }
    }

    if (!showArrow) {
        body()
        return
    }

    when (side) {
        KiraTooltipSide.Top -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            body()
            TooltipArrowCanvas(side = KiraTooltipSide.Top, fill = containerColor, stroke = borderColor)
        }
        KiraTooltipSide.Bottom -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TooltipArrowCanvas(side = KiraTooltipSide.Bottom, fill = containerColor, stroke = borderColor)
            body()
        }
        KiraTooltipSide.Left -> Row(verticalAlignment = Alignment.CenterVertically) {
            body()
            TooltipArrowCanvas(side = KiraTooltipSide.Left, fill = containerColor, stroke = borderColor)
        }
        KiraTooltipSide.Right -> Row(verticalAlignment = Alignment.CenterVertically) {
            TooltipArrowCanvas(side = KiraTooltipSide.Right, fill = containerColor, stroke = borderColor)
            body()
        }
    }
}

@Composable
private fun TooltipArrowCanvas(
    side: KiraTooltipSide,
    fill: Color,
    stroke: Color?,
) {
    val isHorizontal = side == KiraTooltipSide.Left || side == KiraTooltipSide.Right
    Canvas(
        modifier = Modifier
            .size(width = if (isHorizontal) 6.dp else 12.dp, height = if (isHorizontal) 12.dp else 6.dp)
            .clearAndSetSemantics {},
    ) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            when (side) {
                // Tooltip is above anchor -> arrow points down
                KiraTooltipSide.Top -> {
                    moveTo(0f, 0f)
                    lineTo(w / 2f, h)
                    lineTo(w, 0f)
                    close()
                }
                // Tooltip is below anchor -> arrow points up
                KiraTooltipSide.Bottom -> {
                    moveTo(0f, h)
                    lineTo(w / 2f, 0f)
                    lineTo(w, h)
                    close()
                }
                // Tooltip is left of anchor -> arrow points right
                KiraTooltipSide.Left -> {
                    moveTo(0f, 0f)
                    lineTo(w, h / 2f)
                    lineTo(0f, h)
                    close()
                }
                // Tooltip is right of anchor -> arrow points left
                KiraTooltipSide.Right -> {
                    moveTo(w, 0f)
                    lineTo(0f, h / 2f)
                    lineTo(w, h)
                    close()
                }
            }
        }
        drawPath(path = path, color = fill)
        if (stroke != null) {
            drawPath(path = path, color = stroke, style = Stroke(width = 1.dp.toPx()))
        }
    }
}

internal fun resolveTooltipSide(
    preferred: KiraTooltipSide,
    anchorBounds: IntRect,
    windowSize: IntSize,
    popupSize: IntSize,
    gapPx: Int,
    paddingPx: Int,
): KiraTooltipSide = when (preferred) {
    KiraTooltipSide.Top ->
        if (anchorBounds.top - popupSize.height - gapPx >= paddingPx) KiraTooltipSide.Top else KiraTooltipSide.Bottom
    KiraTooltipSide.Bottom ->
        if (anchorBounds.bottom + popupSize.height + gapPx <= windowSize.height - paddingPx) KiraTooltipSide.Bottom else KiraTooltipSide.Top
    KiraTooltipSide.Left ->
        if (anchorBounds.left - popupSize.width - gapPx >= paddingPx) KiraTooltipSide.Left else KiraTooltipSide.Right
    KiraTooltipSide.Right ->
        if (anchorBounds.right + popupSize.width + gapPx <= windowSize.width - paddingPx) KiraTooltipSide.Right else KiraTooltipSide.Left
}

private class KiraTooltipPositionProvider(
    private val preferredSide: KiraTooltipSide,
    private val align: KiraTooltipAlign,
    private val sideOffsetPx: Int,
    private val collisionPaddingPx: Int,
    private val onSideResolved: (KiraTooltipSide) -> Unit,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val side = resolveTooltipSide(
            preferred = preferredSide,
            anchorBounds = anchorBounds,
            windowSize = windowSize,
            popupSize = popupContentSize,
            gapPx = sideOffsetPx,
            paddingPx = collisionPaddingPx,
        )
        onSideResolved(side)

        val rawX = when (side) {
            KiraTooltipSide.Top, KiraTooltipSide.Bottom -> when (align) {
                KiraTooltipAlign.Start -> anchorBounds.left
                KiraTooltipAlign.Center -> anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
                KiraTooltipAlign.End -> anchorBounds.right - popupContentSize.width
            }
            KiraTooltipSide.Left -> anchorBounds.left - popupContentSize.width - sideOffsetPx
            KiraTooltipSide.Right -> anchorBounds.right + sideOffsetPx
        }

        val rawY = when (side) {
            KiraTooltipSide.Top -> anchorBounds.top - popupContentSize.height - sideOffsetPx
            KiraTooltipSide.Bottom -> anchorBounds.bottom + sideOffsetPx
            KiraTooltipSide.Left, KiraTooltipSide.Right -> when (align) {
                KiraTooltipAlign.Start -> anchorBounds.top
                KiraTooltipAlign.Center -> anchorBounds.top + (anchorBounds.height - popupContentSize.height) / 2
                KiraTooltipAlign.End -> anchorBounds.bottom - popupContentSize.height
            }
        }

        val maxX = (windowSize.width - popupContentSize.width - collisionPaddingPx).coerceAtLeast(collisionPaddingPx)
        val maxY = (windowSize.height - popupContentSize.height - collisionPaddingPx).coerceAtLeast(collisionPaddingPx)

        return IntOffset(
            x = rawX.coerceIn(collisionPaddingPx.coerceAtMost(maxX), maxX),
            y = rawY.coerceIn(collisionPaddingPx.coerceAtMost(maxY), maxY),
        )
    }
}
