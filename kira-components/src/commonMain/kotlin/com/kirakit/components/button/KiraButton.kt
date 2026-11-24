package com.kirakit.components.button

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.composeunstyled.AnchorSide
import com.composeunstyled.TooltipHost
import com.composeunstyled.TooltipPanel
import com.composeunstyled.UnstyledTooltip
import com.kirakit.theme.KiraColorScheme
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

enum class KiraButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Destructive,
    Link,
    DestructiveOutline,
    DestructiveGhost,
    Info,
    InfoOutline,
    InfoGhost,
    Success,
    SuccessOutline,
    SuccessGhost,
    Warning,
    WarningOutline,
    WarningGhost,
}

enum class KiraButtonSize(
    height: Int,
    horizontalPadding: Int,
    iconPadding: Int,
    icon: Int,
    spacing: Int,
    val iconOnly: Boolean = false,
) {
    Sm(32, 12, 10, 14, 6),
    Md(36, 16, 12, 16, 8),
    Lg(40, 20, 16, 20, 8),
    Xs(28, 8, 8, 12, 4),
    Xl(44, 20, 16, 20, 8),
    Icon(36, 0, 0, 16, 0, iconOnly = true),
    IconXs(28, 0, 0, 12, 0, iconOnly = true),
    IconSm(32, 0, 0, 14, 0, iconOnly = true),
    IconLg(40, 0, 0, 20, 0, iconOnly = true),
    IconXl(44, 0, 0, 20, 0, iconOnly = true),
    ;

    val minHeight = 44.dp
    val visualHeight = height.dp
    val iconSize = icon.dp
    val contentPadding = PaddingValues(horizontal = horizontalPadding.dp)
    private val paddingWithIcon = PaddingValues(horizontal = iconPadding.dp)
    internal val gap = spacing.dp

    internal fun padding(hasIcon: Boolean): PaddingValues =
        if (hasIcon) paddingWithIcon else contentPadding

    @Composable
    internal fun textStyle(): TextStyle = when (this) {
        Xs, Sm -> KiraTheme.typography.caption
        Xl -> KiraTheme.typography.heading3
        else -> KiraTheme.typography.label
    }
}

/**
 * A size-aware icon slot. Decorative icons should omit their content description.
 */
typealias KiraButtonIcon = @Composable (color: Color, size: Dp) -> Unit

/**
 * A themed action with optional loading, hold-to-confirm, or optimistic toggle behavior.
 *
 * In toggle mode, pass [pressed] for controlled state and commit the new value in [onToggle].
 * An exception rolls back the optimistic state and shows [toggleErrorMessage]. Activations
 * during an in-flight toggle are ignored. A hold action takes precedence over click and toggle.
 */
@Composable
fun KiraButton(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    variant: KiraButtonVariant = KiraButtonVariant.Primary,
    size: KiraButtonSize = KiraButtonSize.Md,
    enabled: Boolean = true,
    loading: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    fullWidth: Boolean = false,
    leftIcon: KiraButtonIcon? = null,
    rightIcon: KiraButtonIcon? = null,
    loadingText: String? = null,
    focusableWhenDisabled: Boolean = false,
    holdDurationMillis: Int = 1_000,
    onHoldComplete: (() -> Unit)? = null,
    toggle: Boolean = false,
    pressed: Boolean? = null,
    defaultPressed: Boolean = false,
    onToggle: (suspend (Boolean) -> Unit)? = null,
    pressedIcon: KiraButtonIcon? = null,
    unpressedIcon: KiraButtonIcon? = null,
    toggleErrorMessage: String = "Couldn't update",
    shape: Shape? = null,
    content: @Composable RowScope.(TextStyle) -> Unit,
) {
    var committedPressed by rememberSaveable { mutableStateOf(defaultPressed) }
    var pendingPressed by remember { mutableStateOf<Boolean?>(null) }
    var toggleError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val displayedPressed = pendingPressed ?: pressed ?: committedPressed
    val interactive = enabled && !loading
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val pressing by interactionSource.collectIsPressedAsState()
    val palette = variant.palette(KiraTheme.colors)
    val resolvedShape = shape ?: KiraTheme.radius.shapeLg
    val shape = resolvedShape
    val ring = KiraTheme.colors.ring
    var targetSize by remember { mutableStateOf(IntSize.Zero) }

    val background by animateColorAsState(
        targetValue = when {
            interactive && pressing -> palette.active
            interactive && hovered -> palette.hover
            else -> palette.background
        },
        animationSpec = KiraMotion.fastTween(),
        label = "Button background",
    )
    val scale by animateFloatAsState(
        targetValue = if (interactive && pressing) 0.96f else 1f,
        animationSpec = KiraMotion.fastTween(),
        label = "Button press",
    )

    val effectiveLeftIcon = when {
        loading -> null
        toggle && displayedPressed -> pressedIcon ?: leftIcon
        toggle -> unpressedIcon ?: leftIcon
        else -> leftIcon
    }
    val effectiveRightIcon = rightIcon.takeUnless { loading }
    val style = size.textStyle().copy(
        color = palette.foreground,
        textDecoration = if (variant == KiraButtonVariant.Link && (hovered || focused)) {
            TextDecoration.Underline
        } else {
            TextDecoration.None
        },
    )

    val activate: () -> Unit = {
        if (interactive && pendingPressed == null) {
            if (toggle) {
                val next = !displayedPressed
                val controlled = pressed != null
                toggleError = null
                pendingPressed = next
                scope.launch {
                    try {
                        onToggle?.invoke(next)
                        if (!controlled) committedPressed = next
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        toggleError = toggleErrorMessage
                    } finally {
                        pendingPressed = null
                    }
                }
            }
            onClick()
        }
    }
    val hold = rememberButtonHold(interactive, holdDurationMillis, onHoldComplete, interactionSource)
    val activationModifier = when {
        onHoldComplete != null -> hold.modifier
        interactive -> Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = activate,
        )
        else -> Modifier.semantics(mergeDescendants = true) {
            role = Role.Button
            disabled()
        }
    }
    val focusModifier = if (!interactive && focusableWhenDisabled) {
        Modifier.focusable(interactionSource = interactionSource)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minWidth = 44.dp, minHeight = size.minHeight)
            .semantics {
                if (toggle) selected = displayedPressed
                if (loading) {
                    stateDescription = loadingText ?: "Loading"
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }
            }
            .then(activationModifier)
            .then(focusModifier)
            .onSizeChanged { targetSize = it },
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true,
    ) {
        val horizontal = if (size.iconOnly && !fullWidth) {
            Modifier.wrapContentWidth().width(size.visualHeight)
        } else {
            Modifier
        }
        Row(
            modifier = horizontal
                .wrapContentHeight()
                .defaultMinSize(minHeight = size.visualHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(shape)
                .then(if (focused) Modifier.border(2.dp, ring, shape) else Modifier)
                .alpha(if (interactive) 1f else 0.5f)
                .background(background)
                .then(if (!focused && palette.border != Color.Transparent) Modifier.border(1.dp, palette.border, shape) else Modifier)
                .buttonRipple(interactionSource, palette.foreground) { targetSize }
                .drawBehind {
                    val progress = hold.progress
                    if (progress > 0f) {
                        drawRect(
                            color = palette.foreground,
                            alpha = 0.5f,
                            size = this.size.copy(width = this.size.width * progress),
                            topLeft = if (layoutDirection == LayoutDirection.Rtl) {
                                Offset(this.size.width * (1f - progress), 0f)
                            } else {
                                Offset.Zero
                            },
                        )
                    }
                }
                .padding(size.padding(loading || effectiveLeftIcon != null || effectiveRightIcon != null)),
            horizontalArrangement = Arrangement.spacedBy(size.gap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) ButtonSpinner(palette.foreground, size.iconSize)
            effectiveLeftIcon?.invoke(palette.foreground, size.iconSize)
            if (loading && loadingText != null) {
                ButtonLabel(loadingText, style)
            } else {
                content(style)
            }
            effectiveRightIcon?.invoke(palette.foreground, size.iconSize)
        }

        toggleError?.let { message ->
            LaunchedEffect(message) {
                delay(3_500)
                toggleError = null
            }
            val gap = with(LocalDensity.current) { 6.dp.roundToPx() }
            Popup(popupPositionProvider = ButtonMessagePosition(gap)) {
                val colors = KiraTheme.colors
                BasicText(
                    text = message,
                    modifier = Modifier
                        .background(colors.popover, shape)
                        .border(1.dp, colors.destructive, shape)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .semantics { liveRegion = LiveRegionMode.Assertive },
                    style = KiraTheme.typography.bodySmall.copy(color = colors.popoverForeground),
                )
            }
        }
    }
}

@Composable
fun KiraButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    variant: KiraButtonVariant = KiraButtonVariant.Primary,
    size: KiraButtonSize = KiraButtonSize.Md,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    leftIcon: KiraButtonIcon? = null,
    rightIcon: KiraButtonIcon? = null,
    loadingText: String? = null,
    focusableWhenDisabled: Boolean = false,
    holdDurationMillis: Int = 1_000,
    onHoldComplete: (() -> Unit)? = null,
    toggle: Boolean = false,
    pressed: Boolean? = null,
    defaultPressed: Boolean = false,
    onToggle: (suspend (Boolean) -> Unit)? = null,
    pressedIcon: KiraButtonIcon? = null,
    unpressedIcon: KiraButtonIcon? = null,
    toggleErrorMessage: String = "Couldn't update",
    shape: Shape? = null,
) {
    KiraButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        loading = loading,
        fullWidth = fullWidth,
        leftIcon = leftIcon,
        rightIcon = rightIcon,
        loadingText = loadingText,
        focusableWhenDisabled = focusableWhenDisabled,
        holdDurationMillis = holdDurationMillis,
        onHoldComplete = onHoldComplete,
        toggle = toggle,
        pressed = pressed,
        defaultPressed = defaultPressed,
        onToggle = onToggle,
        pressedIcon = pressedIcon,
        unpressedIcon = unpressedIcon,
        toggleErrorMessage = toggleErrorMessage,
        shape = shape,
    ) { style ->
        ButtonLabel(text, style)
    }
}

@Composable
fun KiraLoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: KiraButtonVariant = KiraButtonVariant.Primary,
    size: KiraButtonSize = KiraButtonSize.Md,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    fullWidth: Boolean = false,
    leftIcon: KiraButtonIcon? = null,
    rightIcon: KiraButtonIcon? = null,
    focusableWhenDisabled: Boolean = false,
    holdDurationMillis: Int = 1_000,
    onHoldComplete: (() -> Unit)? = null,
) {
    KiraButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        enabled = enabled,
        loading = isLoading,
        loadingText = loadingText,
        fullWidth = fullWidth,
        leftIcon = leftIcon,
        rightIcon = rightIcon,
        focusableWhenDisabled = focusableWhenDisabled,
        holdDurationMillis = holdDurationMillis,
        onHoldComplete = onHoldComplete,
    )
}

enum class KiraButtonExpandDirection { Start, End }

@Composable
fun KiraExpandableIconButton(
    label: String,
    icon: KiraButtonIcon,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    variant: KiraButtonVariant = KiraButtonVariant.Primary,
    size: KiraButtonSize = KiraButtonSize.Sm,
    enabled: Boolean = true,
    expandDirection: KiraButtonExpandDirection = KiraButtonExpandDirection.End,
    focusableWhenDisabled: Boolean = false,
    holdDurationMillis: Int = 1_000,
    onHoldComplete: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val showLabel = enabled && (hovered || focused)
    val towardsEnd = expandDirection == KiraButtonExpandDirection.End
    val anchor = if (towardsEnd) Alignment.Start else Alignment.End
    KiraButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = label },
        variant = variant,
        size = size,
        enabled = enabled,
        focusableWhenDisabled = focusableWhenDisabled,
        holdDurationMillis = holdDurationMillis,
        onHoldComplete = onHoldComplete,
        interactionSource = interactionSource,
        leftIcon = icon.takeIf { towardsEnd },
        rightIcon = icon.takeUnless { towardsEnd },
    ) { style ->
        AnimatedVisibility(
            visible = showLabel,
            enter = expandHorizontally(KiraMotion.fastTween(), expandFrom = anchor) + fadeIn(KiraMotion.fastTween()),
            exit = shrinkHorizontally(KiraMotion.fastTween(), shrinkTowards = anchor) + fadeOut(KiraMotion.fastTween()),
        ) {
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics {},
                style = style,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

enum class KiraButtonTooltipSide { Top, End, Bottom, Start }

/** Place around the screen content containing [KiraIconButtonWithTooltip]. */
@Composable
fun KiraButtonTooltipHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipHost(modifier = modifier, content = content)
}

@Composable
fun KiraIconButtonWithTooltip(
    icon: KiraButtonIcon,
    tooltip: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    variant: KiraButtonVariant = KiraButtonVariant.Primary,
    size: KiraButtonSize = KiraButtonSize.Icon,
    enabled: Boolean = true,
    tooltipSide: KiraButtonTooltipSide = KiraButtonTooltipSide.Top,
    disabledReason: String? = null,
    accessibilityLabel: String = tooltip,
    focusableWhenDisabled: Boolean = disabledReason != null,
) {
    val message = if (enabled) tooltip else disabledReason ?: tooltip
    val side = when (tooltipSide) {
        KiraButtonTooltipSide.Top -> AnchorSide.Top
        KiraButtonTooltipSide.End -> AnchorSide.End
        KiraButtonTooltipSide.Bottom -> AnchorSide.Bottom
        KiraButtonTooltipSide.Start -> AnchorSide.Start
    }
    UnstyledTooltip(
        side = side,
        sideOffset = 6.dp,
        hoverDelayMillis = 350,
        panel = {
            val colors = KiraTheme.colors
            val shape = KiraTheme.radius.shapeMd
            TooltipPanel(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .background(colors.popover, shape)
                    .border(1.dp, colors.border, shape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                BasicText(message, style = KiraTheme.typography.bodySmall.copy(color = colors.popoverForeground))
            }
        },
    ) {
        KiraButton(
            onClick = onClick,
            modifier = modifier.semantics {
                contentDescription = if (!enabled && disabledReason != null) {
                    "$accessibilityLabel. $disabledReason"
                } else {
                    accessibilityLabel
                }
            },
            size = size,
            variant = variant,
            enabled = enabled,
            focusableWhenDisabled = focusableWhenDisabled,
            leftIcon = icon,
        ) {}
    }
}

private data class ButtonPalette(
    val background: Color,
    val foreground: Color,
    val hover: Color = background,
    val active: Color = hover,
    val border: Color = Color.Transparent,
)

private fun KiraButtonVariant.palette(colors: KiraColorScheme): ButtonPalette {
    fun filled(background: Color, foreground: Color, hoverAlpha: Float = 0.9f) = ButtonPalette(
        background = background,
        foreground = foreground,
        hover = background.copy(alpha = hoverAlpha),
        active = background.copy(alpha = 0.8f),
    )

    fun semanticOutline(color: Color) = ButtonPalette(
        background = colors.background,
        foreground = color,
        border = color.copy(alpha = 0.5f),
        hover = color.copy(alpha = 0.1f).compositeOver(colors.background),
        active = color.copy(alpha = 0.2f).compositeOver(colors.background),
    )

    fun semanticGhost(color: Color) = ButtonPalette(
        background = Color.Transparent,
        foreground = color,
        hover = color.copy(alpha = 0.1f),
        active = color.copy(alpha = 0.2f),
    )

    return when (this) {
        KiraButtonVariant.Primary -> filled(colors.primary, colors.primaryForeground)
        KiraButtonVariant.Secondary -> filled(colors.secondary, colors.secondaryForeground, 0.8f)
        KiraButtonVariant.Outline -> ButtonPalette(
            colors.background,
            colors.foreground,
            hover = colors.muted,
            active = colors.accent,
            border = colors.border,
        )
        KiraButtonVariant.Ghost -> ButtonPalette(
            Color.Transparent,
            colors.foreground,
            hover = colors.muted,
            active = colors.accent,
        )
        KiraButtonVariant.Link -> ButtonPalette(Color.Transparent, colors.primary)
        KiraButtonVariant.Destructive -> filled(colors.destructive, colors.destructiveForeground)
        KiraButtonVariant.DestructiveOutline -> semanticOutline(colors.destructive)
        KiraButtonVariant.DestructiveGhost -> semanticGhost(colors.destructive)
        KiraButtonVariant.Info -> filled(colors.info, colors.infoForeground)
        KiraButtonVariant.InfoOutline -> semanticOutline(colors.info)
        KiraButtonVariant.InfoGhost -> semanticGhost(colors.info)
        KiraButtonVariant.Success -> filled(colors.success, colors.successForeground)
        KiraButtonVariant.SuccessOutline -> semanticOutline(colors.success)
        KiraButtonVariant.SuccessGhost -> semanticGhost(colors.success)
        KiraButtonVariant.Warning -> filled(colors.warning, colors.warningForeground)
        KiraButtonVariant.WarningOutline -> semanticOutline(colors.warning)
        KiraButtonVariant.WarningGhost -> semanticGhost(colors.warning)
    }
}

private class ButtonHold {
    var modifier: Modifier = Modifier
    var progress by mutableFloatStateOf(0f)
    var press by mutableStateOf<HoldPress?>(null)
}

private class HoldPress(val interaction: PressInteraction.Press, val key: Key?, val duration: Duration) {
    val start = TimeSource.Monotonic.markNow()
}

@Composable
private fun rememberButtonHold(
    enabled: Boolean,
    durationMillis: Int,
    onHoldComplete: (() -> Unit)?,
    interactionSource: MutableInteractionSource,
): ButtonHold {
    val hold = remember(enabled, onHoldComplete != null, interactionSource) { ButtonHold() }
    val completion by rememberUpdatedState(onHoldComplete)
    val currentDuration by rememberUpdatedState(durationMillis.coerceAtLeast(1).milliseconds)
    val activePress = hold.press
    LaunchedEffect(activePress) {
        hold.progress = 0f
        if (activePress != null) {
            val duration = activePress.duration
            coroutineScope {
                val animation = launch {
                    while (hold.progress < 1f) {
                        withFrameNanos {
                            hold.progress = (activePress.start.elapsedNow() / duration).toFloat().coerceIn(0f, 1f)
                        }
                    }
                }
                delay((duration - activePress.start.elapsedNow()).coerceAtLeast(0.milliseconds))
                animation.cancel()
                if (hold.press === activePress) {
                    hold.progress = 1f
                    completion?.invoke()
                }
            }
        }
    }

    fun start(position: Offset, key: Key?): HoldPress? {
        if (hold.press != null) return null
        val press = HoldPress(PressInteraction.Press(position), key, currentDuration)
        hold.press = press
        interactionSource.tryEmit(press.interaction)
        return press
    }

    fun finish(press: HoldPress, released: Boolean) {
        if (hold.press !== press) return
        hold.press = null
        hold.progress = 0f
        interactionSource.tryEmit(
            if (released) PressInteraction.Release(press.interaction) else PressInteraction.Cancel(press.interaction),
        )
    }

    DisposableEffect(hold) {
        onDispose { hold.press?.let { finish(it, released = false) } }
    }

    val semantics = Modifier.semantics(mergeDescendants = true) {
        role = Role.Button
        if (enabled) {
            onClick(label = "Confirm") {
                if (hold.press == null) {
                    completion?.invoke()
                    true
                } else {
                    false
                }
            }
        } else {
            disabled()
        }
    }
    hold.modifier = if (enabled && onHoldComplete != null) {
        semantics
            .hoverable(interactionSource)
            .onFocusChanged { focus ->
                val press = hold.press
                if (!focus.isFocused && press?.key != null) finish(press, released = false)
            }
            .onPreviewKeyEvent { event ->
                if (event.key != Key.Spacebar && event.key != Key.Enter && event.key != Key.NumPadEnter) {
                    return@onPreviewKeyEvent false
                }
                when (event.type) {
                    KeyEventType.KeyDown -> start(Offset.Unspecified, event.key)
                    KeyEventType.KeyUp -> {
                        val press = hold.press
                        if (press?.key == event.key) finish(press, released = true)
                    }
                }
                true
            }
            .focusable(interactionSource = interactionSource)
            .pointerInput(hold) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (down.type == PointerType.Mouse && !currentEvent.buttons.isPrimaryPressed) {
                        return@awaitEachGesture
                    }
                    if (down.position.x !in 0f..size.width.toFloat() || down.position.y !in 0f..size.height.toFloat()) {
                        return@awaitEachGesture
                    }
                    val press = start(down.position, key = null) ?: return@awaitEachGesture
                    down.consume()
                    var released = false
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            val outside = change.position.x !in 0f..size.width.toFloat() ||
                                change.position.y !in 0f..size.height.toFloat()
                            if (change.isConsumed || outside || event.changes.any { it.id != down.id && it.pressed }) {
                                break
                            }
                            if (!change.pressed) {
                                change.consume()
                                released = true
                                break
                            }
                            if (awaitPointerEvent(PointerEventPass.Final).changes.any { it.id == down.id && it.isConsumed }) {
                                break
                            }
                        }
                    } finally {
                        finish(press, released)
                    }
                }
            }
    } else {
        semantics
    }
    return hold
}

@Composable
private fun ButtonLabel(text: String, style: TextStyle) {
    BasicText(text, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun Modifier.buttonRipple(
    source: MutableInteractionSource,
    color: Color,
    targetSize: () -> IntSize,
): Modifier {
    val progress = remember { Animatable(1f) }
    var origin by remember { mutableStateOf(Offset.Unspecified) }
    LaunchedEffect(source) {
        source.interactions.filterIsInstance<PressInteraction.Press>().collectLatest { press ->
            if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) return@collectLatest
            origin = press.pressPosition
            progress.snapTo(0f)
            progress.animateTo(1f, tween(600, easing = KiraMotion.EaseOutStrong))
        }
    }
    return drawBehind {
        val fraction = progress.value
        if (fraction < 1f) {
            drawCircle(
                color = color,
                radius = (size.width + size.height) * fraction,
                center = if (origin == Offset.Unspecified) center else origin - Offset(
                    (targetSize().width - size.width) / 2f,
                    (targetSize().height - size.height) / 2f,
                ),
                alpha = 0.3f * (1f - fraction),
            )
        }
    }
}

@Composable
private fun ButtonSpinner(color: Color, size: Dp) {
    val transition = rememberInfiniteTransition(label = "Button loading")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_000, easing = LinearEasing)),
        label = "Spinner rotation",
    )
    Canvas(Modifier.size(size)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = this.size.minDimension / 2f - stroke.width / 2f, alpha = 0.25f, style = stroke)
        val inset = stroke.width / 2f
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 95f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(this.size.width - stroke.width, this.size.height - stroke.width),
            style = stroke,
        )
    }
}

private class ButtonMessagePosition(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val above = anchorBounds.top - popupContentSize.height - gap
        val y = if (above >= 0) above else anchorBounds.bottom + gap
        return IntOffset(
            x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)),
        )
    }
}
