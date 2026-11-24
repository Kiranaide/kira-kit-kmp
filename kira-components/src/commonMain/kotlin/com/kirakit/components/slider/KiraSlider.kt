package com.kirakit.components.slider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kirakit.components.input.KiraInputSize
import com.kirakit.components.input.KiraNumericInput
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

enum class KiraSliderSize(
    internal val trackThickness: Dp,
    internal val thumbSize: Dp,
) {
    Sm(6.dp, 14.dp),
    Default(8.dp, 20.dp),
    Lg(12.dp, 24.dp),
}

enum class KiraSliderVariant {
    Default,
    Gradient,
    Info,
    Success,
    Warning,
    Destructive,
}

enum class KiraSliderOrientation {
    Horizontal,
    Vertical,
}

enum class KiraSliderTooltipMode {
    Always,
    Hover,
    OnDrag,
    Never,
}

enum class KiraSliderTooltipPosition {
    Top,
    Bottom,
}

@Immutable
data class KiraSliderMarker(
    val value: Float,
    val label: String? = null,
)

private const val SLIDER_EPS = 1e-6f
private const val MAX_STEP_MARKERS = 50

/**
 * Clamps and snaps [raw] to [stepOptions] (when non-empty) or to the nearest [step] increment in `[min, max]`.
 */
fun snapSliderValue(
    raw: Float,
    min: Float,
    max: Float,
    step: Float = 1f,
    stepOptions: List<Float> = emptyList(),
): Float {
    val clamped = if (raw.isFinite()) raw.coerceIn(min, max) else min
    val validOptions = stepOptions.filter { it.isFinite() && it in min..max }
    if (validOptions.isNotEmpty()) {
        return validOptions.minBy { abs(it - clamped) }
    }
    if (step <= 0f || !step.isFinite()) return clamped
    val stepsFromMin = round((clamped - min) / step)
    return (min + stepsFromMin * step).coerceIn(min, max)
}

/**
 * Builds the rendered marker list matching `buildMarkers` in `slider.tsx`.
 */
fun buildSliderMarkers(
    markers: List<KiraSliderMarker>?,
    showStepMarkers: Boolean,
    stepOptions: List<Float>,
    min: Float,
    max: Float,
    step: Float,
): List<KiraSliderMarker> {
    if (markers != null) return markers
    val validOptions = stepOptions.filter { it.isFinite() && it in min..max }
    if (validOptions.isNotEmpty()) {
        return validOptions
            .filter { it - min > SLIDER_EPS && max - it > SLIDER_EPS }
            .map { KiraSliderMarker(it) }
    }
    if (!showStepMarkers || step <= 0f || max <= min) return emptyList()
    val count = floor(((max - min) / step) + SLIDER_EPS).toInt() + 1
    val raw = if (count > MAX_STEP_MARKERS) {
        val stride = (max - min) / MAX_STEP_MARKERS
        (0..MAX_STEP_MARKERS).map { KiraSliderMarker(min + it * stride) }
    } else {
        (0 until count).map { KiraSliderMarker(min + it * step) }
    }
    return raw.filter { it.value - min > SLIDER_EPS && max - it.value > SLIDER_EPS }
}

private fun defaultFormatNumber(value: Float): String {
    val rounded = round(value)
    return if (abs(value - rounded) < 0.001f) {
        rounded.toInt().toString()
    } else {
        val tenths = round(value * 10f) / 10f
        tenths.toString()
    }
}

private fun valueToFraction(value: Float, min: Float, max: Float, inverted: Boolean): Float {
    val range = max - min
    if (range <= 0f) return 0f
    val raw = ((value - min) / range).coerceIn(0f, 1f)
    return if (inverted) 1f - raw else raw
}

private fun fractionToValue(fraction: Float, min: Float, max: Float, inverted: Boolean): Float {
    val f = fraction.coerceIn(0f, 1f)
    val effective = if (inverted) 1f - f else f
    return min + effective * (max - min)
}

/**
 * Single-thumb slider aligned with kira-kit-v2's `Slider` & `KiraSlider`.
 *
 * Supports horizontal & vertical orientation, `inverted` scale, 3 sizes, 6 color variants (including
 * `Gradient`), discrete `stepOptions`, clickable step/custom `markers` & labels, value tooltips
 * (`Always`/`Hover`/`OnDrag`/`Never`), thumb label/icon decor, keyboard navigation, `readOnly`,
 * `disabledReason` tooltip, and mobile step haptics.
 */
@Composable
fun KiraSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 100f,
    step: Float = 1f,
    stepOptions: List<Float> = emptyList(),
    size: KiraSliderSize = KiraSliderSize.Default,
    variant: KiraSliderVariant = KiraSliderVariant.Default,
    orientation: KiraSliderOrientation = KiraSliderOrientation.Horizontal,
    inverted: Boolean = false,
    label: String? = null,
    description: String? = null,
    error: String? = null,
    required: Boolean = false,
    showValue: Boolean = false,
    formatValue: (Float) -> String = ::defaultFormatNumber,
    tooltipMode: KiraSliderTooltipMode = KiraSliderTooltipMode.Never,
    tooltipPosition: KiraSliderTooltipPosition = KiraSliderTooltipPosition.Top,
    renderTooltip: (@Composable (Float) -> Unit)? = null,
    markers: List<KiraSliderMarker>? = null,
    showStepMarkers: Boolean = false,
    thumbLabel: String? = null,
    thumbIcon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    disabledReason: String? = null,
    onValueCommitted: ((Float) -> Unit)? = null,
) {
    val body: @Composable () -> Unit = {
        SliderCore(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            min = min,
            max = max,
            step = step,
            stepOptions = stepOptions,
            size = size,
            variant = variant,
            orientation = orientation,
            inverted = inverted,
            label = label,
            description = description,
            error = error,
            required = required,
            showValue = showValue,
            formatValue = formatValue,
            tooltipMode = tooltipMode,
            tooltipPosition = tooltipPosition,
            renderTooltip = renderTooltip,
            markers = markers,
            showStepMarkers = showStepMarkers,
            thumbLabel = thumbLabel,
            thumbIcon = thumbIcon,
            enabled = enabled,
            readOnly = readOnly,
            onValueCommitted = onValueCommitted,
        )
    }

    if (!enabled && !disabledReason.isNullOrBlank()) {
        KiraSimpleTooltip(text = disabledReason) {
            body()
        }
    } else {
        body()
    }
}

@Composable
private fun SliderCore(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    min: Float,
    max: Float,
    step: Float,
    stepOptions: List<Float>,
    size: KiraSliderSize,
    variant: KiraSliderVariant,
    orientation: KiraSliderOrientation,
    inverted: Boolean,
    label: String?,
    description: String?,
    error: String?,
    required: Boolean,
    showValue: Boolean,
    formatValue: (Float) -> String,
    tooltipMode: KiraSliderTooltipMode,
    tooltipPosition: KiraSliderTooltipPosition,
    renderTooltip: (@Composable (Float) -> Unit)?,
    markers: List<KiraSliderMarker>?,
    showStepMarkers: Boolean,
    thumbLabel: String?,
    thumbIcon: (@Composable (Color) -> Unit)?,
    enabled: Boolean,
    readOnly: Boolean,
    onValueCommitted: ((Float) -> Unit)?,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val density = LocalDensity.current

    val interactive = enabled && !readOnly
    val current = snapSliderValue(value, min, max, step, stepOptions)
    val latestCurrent by rememberUpdatedState(current)
    val latestOnChange by rememberUpdatedState(onValueChange)
    val latestOnCommitted by rememberUpdatedState(onValueCommitted)
    val markersToRender = remember(markers, showStepMarkers, stepOptions, min, max, step) {
        buildSliderMarkers(markers, showStepMarkers, stepOptions, min, max, step)
    }

    var dragging by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val fraction = valueToFraction(current, min, max, inverted)
    val isStepped = markersToRender.isNotEmpty() ||
        (step > 0f && (max - min) > 0f && step / (max - min) >= 1f / 40f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = if (dragging && !isStepped) snap() else tween(150, easing = KiraMotion.EaseOutStrong),
        label = "SliderFraction",
    )
    val vertical = orientation == KiraSliderOrientation.Vertical
    val isError = !error.isNullOrBlank()

    fun emitValue(nextRaw: Float, commit: Boolean = false) {
        if (!interactive) return
        val snapped = snapSliderValue(nextRaw, min, max, step, stepOptions)
        if (abs(snapped - latestCurrent) > SLIDER_EPS) {
            if (stepOptions.isNotEmpty() || step >= (max - min) / 20f) {
                haptics.perform(KiraHapticFeedbackType.Selection)
            }
            latestOnChange(snapped)
        }
        if (commit) latestOnCommitted?.invoke(snapped)
    }

    val fillBrush = sliderFillBrush(variant, isError, colors, vertical)
    val thumbBorderColor = if (isError) colors.destructive else colors.primary

    val showBubble = when (tooltipMode) {
        KiraSliderTooltipMode.Always -> true
        KiraSliderTooltipMode.Hover -> hovered || focused || dragging
        KiraSliderTooltipMode.OnDrag -> dragging || focused
        KiraSliderTooltipMode.Never -> false
    }

    Column(
        modifier = modifier
            .then(if (!vertical) Modifier.fillMaxWidth() else Modifier)
            .alpha(if (enabled) 1f else 0.50f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SliderHeaderRow(
            label = label,
            required = required,
            description = description,
            error = error,
            valueText = if (showValue) formatValue(current) else null,
        )

        val keyHandler = Modifier
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .onPreviewKeyEvent { event ->
                if (!interactive || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val delta = if (step > 0f) step else (max - min) / 20f
                val stepSign = if (inverted) -1f else 1f
                when (event.key) {
                    Key.DirectionRight, Key.DirectionUp -> {
                        emitValue(latestCurrent + delta * stepSign, commit = true)
                        true
                    }
                    Key.DirectionLeft, Key.DirectionDown -> {
                        emitValue(latestCurrent - delta * stepSign, commit = true)
                        true
                    }
                    Key.MoveHome -> {
                        emitValue(min, commit = true)
                        true
                    }
                    Key.MoveEnd -> {
                        emitValue(max, commit = true)
                        true
                    }
                    else -> false
                }
            }

        if (!vertical) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                    .hoverable(interactionSource, enabled = enabled)
                    .then(keyHandler)
                    .pointerInput(interactive, min, max, step, stepOptions, inverted) {
                        if (!interactive) return@pointerInput
                        detectTapGestures { offset ->
                            val frac = (offset.x / this.size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                            emitValue(fractionToValue(frac, min, max, inverted), commit = true)
                        }
                    }
                    .pointerInput(interactive, min, max, step, stepOptions, inverted) {
                        if (!interactive) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragStart = { startOffset ->
                                dragging = true
                                val frac = (startOffset.x / this.size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                emitValue(fractionToValue(frac, min, max, inverted))
                            },
                            onDragEnd = {
                                dragging = false
                                latestOnCommitted?.invoke(latestCurrent)
                            },
                            onDragCancel = { dragging = false },
                            onHorizontalDrag = { change, _ ->
                                val frac = (change.position.x / this.size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                emitValue(fractionToValue(frac, min, max, inverted))
                            },
                        )
                    }
                    .semantics {
                        if (label != null) contentDescription = label
                        stateDescription = formatValue(current)
                        progressBarRangeInfo = ProgressBarRangeInfo(current, min..max)
                        if (!enabled) disabled()
                        if (interactive) {
                            setProgress { target ->
                                emitValue(target, commit = true)
                                true
                            }
                        }
                    },
                contentAlignment = Alignment.CenterStart,
            ) {
                val trackWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
                val thumbPx = with(density) { size.thumbSize.toPx() }
                val thumbCenterX = animatedFraction * trackWidthPx

                // Track background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(size.trackThickness)
                        .clip(CircleShape)
                        .background(colors.muted, CircleShape),
                ) {
                    // Active range fill
                    val fillStart = if (inverted) animatedFraction else 0f
                    val fillFraction = if (inverted) 1f - animatedFraction else animatedFraction
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((fillStart * trackWidthPx).roundToInt(), 0) }
                            .fillMaxWidth(fillFraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(fillBrush, CircleShape),
                    )
                }

                // Step / custom markers
                markersToRender.forEach { marker ->
                    val mf = valueToFraction(marker.value, min, max, inverted)
                    val active = if (inverted) marker.value >= current else marker.value <= current
                    val markerInteraction = remember(marker.value) { MutableInteractionSource() }
                    val markerHovered by markerInteraction.collectIsHoveredAsState()
                    val markerScale by animateFloatAsState(
                        targetValue = if (markerHovered && interactive) 1.5f else 1f,
                        animationSpec = KiraMotion.fastTween(),
                        label = "SliderMarkerScale",
                    )
                    val markerColor by animateColorAsState(
                        targetValue = if (active) colors.primaryForeground.copy(alpha = 0.85f) else colors.mutedForeground.copy(alpha = 0.45f),
                        animationSpec = KiraMotion.fastTween(),
                        label = "SliderMarkerColor",
                    )
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((mf * trackWidthPx - with(density) { 3.dp.toPx() }).roundToInt(), 0) }
                            .graphicsLayer {
                                scaleX = markerScale
                                scaleY = markerScale
                            }
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(markerColor, CircleShape)
                            .hoverable(markerInteraction, enabled = interactive)
                            .then(
                                if (interactive) {
                                    Modifier.clickable(
                                        interactionSource = markerInteraction,
                                        indication = null,
                                        onClick = { emitValue(marker.value, commit = true) },
                                    )
                                } else {
                                    Modifier
                                },
                            ),
                    )
                }

                // Thumb + optional tooltip bubble
                SliderThumbNode(
                    size = size.thumbSize,
                    borderColor = thumbBorderColor,
                    active = dragging || hovered || focused,
                    focused = focused,
                    thumbLabel = thumbLabel,
                    thumbIcon = thumbIcon,
                    showBubble = showBubble,
                    bubblePosition = tooltipPosition,
                    vertical = false,
                    bubbleContent = {
                        if (renderTooltip != null) renderTooltip(current)
                        else BasicText(
                            text = formatValue(current),
                            style = KiraTheme.typography.caption.copy(color = colors.popoverForeground, fontWeight = FontWeight.Medium),
                        )
                    },
                    modifier = Modifier
                        .zIndex(2f)
                        .offset { IntOffset((thumbCenterX - thumbPx / 2f).roundToInt(), 0) },
                )
            }

            // Horizontal marker labels
            val labeled = markersToRender.filter { it.label != null }
            if (labeled.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    labeled.forEach { marker ->
                        val active = abs(marker.value - current) <= SLIDER_EPS
                        BasicText(
                            text = marker.label.orEmpty(),
                            modifier = Modifier
                                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand).clickable { emitValue(marker.value, commit = true) } else Modifier),
                            style = KiraTheme.typography.caption.copy(
                                color = if (active) colors.primary else colors.mutedForeground,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                }
            }
        } else {
            // Vertical slider
            BoxWithConstraints(
                modifier = Modifier
                    .height(168.dp)
                    .width(44.dp)
                    .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                    .hoverable(interactionSource, enabled = enabled)
                    .then(keyHandler)
                    .pointerInput(interactive, min, max, step, stepOptions, inverted) {
                        if (!interactive) return@pointerInput
                        detectTapGestures { offset ->
                            val frac = 1f - (offset.y / this.size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                            emitValue(fractionToValue(frac, min, max, inverted), commit = true)
                        }
                    }
                    .pointerInput(interactive, min, max, step, stepOptions, inverted) {
                        if (!interactive) return@pointerInput
                        detectVerticalDragGestures(
                            onDragStart = { startOffset ->
                                dragging = true
                                val frac = 1f - (startOffset.y / this.size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                                emitValue(fractionToValue(frac, min, max, inverted))
                            },
                            onDragEnd = {
                                dragging = false
                                latestOnCommitted?.invoke(latestCurrent)
                            },
                            onDragCancel = { dragging = false },
                            onVerticalDrag = { change, _ ->
                                val frac = 1f - (change.position.y / this.size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                                emitValue(fractionToValue(frac, min, max, inverted))
                            },
                        )
                    }
                    .semantics {
                        if (label != null) contentDescription = label
                        stateDescription = formatValue(current)
                        progressBarRangeInfo = ProgressBarRangeInfo(current, min..max)
                        if (!enabled) disabled()
                    },
                contentAlignment = Alignment.TopCenter,
            ) {
                val trackHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
                val thumbPx = with(density) { size.thumbSize.toPx() }
                val thumbCenterY = (1f - animatedFraction) * trackHeightPx

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(size.trackThickness)
                        .clip(CircleShape)
                        .background(colors.muted, CircleShape),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedFraction.coerceIn(0f, 1f))
                            .clip(CircleShape)
                            .background(fillBrush, CircleShape),
                    )
                }

                SliderThumbNode(
                    size = size.thumbSize,
                    borderColor = thumbBorderColor,
                    active = dragging || hovered || focused,
                    focused = focused,
                    thumbLabel = thumbLabel,
                    thumbIcon = thumbIcon,
                    showBubble = showBubble,
                    bubblePosition = tooltipPosition,
                    vertical = true,
                    bubbleContent = {
                        if (renderTooltip != null) renderTooltip(current)
                        else BasicText(
                            text = formatValue(current),
                            style = KiraTheme.typography.caption.copy(color = colors.popoverForeground, fontWeight = FontWeight.Medium),
                        )
                    },
                    modifier = Modifier
                        .zIndex(2f)
                        .offset { IntOffset(0, (thumbCenterY - thumbPx / 2f).roundToInt()) },
                )
            }
        }
    }
}

/**
 * Dual-thumb range slider aligned with kira-kit-v2's `RangeSlider` & `KiraRangeSlider`.
 *
 * Optionally renders synced From/To `KiraNumericInput` fields underneath (`showInputs = true`).
 */
@Composable
fun KiraRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 100f,
    step: Float = 1f,
    stepOptions: List<Float> = emptyList(),
    size: KiraSliderSize = KiraSliderSize.Default,
    variant: KiraSliderVariant = KiraSliderVariant.Default,
    inverted: Boolean = false,
    label: String? = null,
    description: String? = null,
    error: String? = null,
    required: Boolean = false,
    showValue: Boolean = false,
    showInputs: Boolean = false,
    formatValue: (Float) -> String = ::defaultFormatNumber,
    tooltipMode: KiraSliderTooltipMode = KiraSliderTooltipMode.Never,
    tooltipPosition: KiraSliderTooltipPosition = KiraSliderTooltipPosition.Top,
    markers: List<KiraSliderMarker>? = null,
    showStepMarkers: Boolean = false,
    thumbLabels: Pair<String, String>? = null,
    thumbIcons: Pair<(@Composable (Color) -> Unit), (@Composable (Color) -> Unit)>? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    disabledReason: String? = null,
    onValueCommitted: ((ClosedFloatingPointRange<Float>) -> Unit)? = null,
) {
    val body: @Composable () -> Unit = {
        RangeSliderCore(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            min = min,
            max = max,
            step = step,
            stepOptions = stepOptions,
            size = size,
            variant = variant,
            inverted = inverted,
            label = label,
            description = description,
            error = error,
            required = required,
            showValue = showValue,
            showInputs = showInputs,
            formatValue = formatValue,
            tooltipMode = tooltipMode,
            tooltipPosition = tooltipPosition,
            markers = markers,
            showStepMarkers = showStepMarkers,
            thumbLabels = thumbLabels,
            thumbIcons = thumbIcons,
            enabled = enabled,
            readOnly = readOnly,
            onValueCommitted = onValueCommitted,
        )
    }

    if (!enabled && !disabledReason.isNullOrBlank()) {
        KiraSimpleTooltip(text = disabledReason) {
            body()
        }
    } else {
        body()
    }
}

@Composable
private fun RangeSliderCore(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier,
    min: Float,
    max: Float,
    step: Float,
    stepOptions: List<Float>,
    size: KiraSliderSize,
    variant: KiraSliderVariant,
    inverted: Boolean,
    label: String?,
    description: String?,
    error: String?,
    required: Boolean,
    showValue: Boolean,
    showInputs: Boolean,
    formatValue: (Float) -> String,
    tooltipMode: KiraSliderTooltipMode,
    tooltipPosition: KiraSliderTooltipPosition,
    markers: List<KiraSliderMarker>?,
    showStepMarkers: Boolean,
    thumbLabels: Pair<String, String>?,
    thumbIcons: Pair<(@Composable (Color) -> Unit), (@Composable (Color) -> Unit)>?,
    enabled: Boolean,
    readOnly: Boolean,
    onValueCommitted: ((ClosedFloatingPointRange<Float>) -> Unit)?,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val density = LocalDensity.current
    val interactive = enabled && !readOnly
    val isError = !error.isNullOrBlank()

    val startVal = snapSliderValue(value.start, min, max, step, stepOptions)
    val endVal = snapSliderValue(value.endInclusive, min, max, step, stepOptions).coerceAtLeast(startVal)
    val latestRange by rememberUpdatedState(startVal..endVal)
    val latestOnChange by rememberUpdatedState(onValueChange)
    val latestOnCommitted by rememberUpdatedState(onValueCommitted)

    val markersToRender = remember(markers, showStepMarkers, stepOptions, min, max, step) {
        buildSliderMarkers(markers, showStepMarkers, stepOptions, min, max, step)
    }

    var activeThumb by remember { mutableIntStateOf(-1) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val fStart = valueToFraction(startVal, min, max, inverted)
    val fEnd = valueToFraction(endVal, min, max, inverted)
    val isStepped = markersToRender.isNotEmpty() ||
        (step > 0f && (max - min) > 0f && step / (max - min) >= 1f / 40f)
    val animatedFStart by animateFloatAsState(
        targetValue = fStart,
        animationSpec = if (activeThumb == 0 && !isStepped) snap() else tween(150, easing = KiraMotion.EaseOutStrong),
        label = "RangeSliderStart",
    )
    val animatedFEnd by animateFloatAsState(
        targetValue = fEnd,
        animationSpec = if (activeThumb == 1 && !isStepped) snap() else tween(150, easing = KiraMotion.EaseOutStrong),
        label = "RangeSliderEnd",
    )
    val leftFrac = minOf(animatedFStart, animatedFEnd)
    val rightFrac = maxOf(animatedFStart, animatedFEnd)

    fun updateThumb(thumbIndex: Int, rawValue: Float, commit: Boolean = false) {
        if (!interactive) return
        val snapped = snapSliderValue(rawValue, min, max, step, stepOptions)
        val cur = latestRange
        val next = if (thumbIndex == 0) {
            snapped.coerceAtMost(cur.endInclusive)..cur.endInclusive
        } else {
            cur.start..snapped.coerceAtLeast(cur.start)
        }
        if (abs(next.start - cur.start) > SLIDER_EPS || abs(next.endInclusive - cur.endInclusive) > SLIDER_EPS) {
            if (stepOptions.isNotEmpty() || step >= (max - min) / 20f) {
                haptics.perform(KiraHapticFeedbackType.Selection)
            }
            latestOnChange(next)
        }
        if (commit) latestOnCommitted?.invoke(next)
    }

    val fillBrush = sliderFillBrush(variant, isError, colors, vertical = false)
    val thumbBorderColor = if (isError) colors.destructive else colors.primary
    val showBubble = when (tooltipMode) {
        KiraSliderTooltipMode.Always -> true
        KiraSliderTooltipMode.Hover -> hovered || activeThumb >= 0
        KiraSliderTooltipMode.OnDrag -> activeThumb >= 0
        KiraSliderTooltipMode.Never -> false
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.50f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SliderHeaderRow(
            label = label,
            required = required,
            description = description,
            error = error,
            valueText = if (showValue) "${formatValue(startVal)} – ${formatValue(endVal)}" else null,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .hoverable(interactionSource, enabled = enabled)
                .pointerInput(interactive, min, max, step, stepOptions, inverted) {
                    if (!interactive) return@pointerInput
                    detectTapGestures { offset ->
                        val frac = (offset.x / this.size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val tappedVal = fractionToValue(frac, min, max, inverted)
                        val cur = latestRange
                        val idx = if (abs(tappedVal - cur.start) <= abs(tappedVal - cur.endInclusive)) 0 else 1
                        updateThumb(idx, tappedVal, commit = true)
                    }
                }
                .pointerInput(interactive, min, max, step, stepOptions, inverted) {
                    if (!interactive) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { startOffset ->
                            val frac = (startOffset.x / this.size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                            val draggedVal = fractionToValue(frac, min, max, inverted)
                            val cur = latestRange
                            activeThumb = if (abs(draggedVal - cur.start) <= abs(draggedVal - cur.endInclusive)) 0 else 1
                            updateThumb(activeThumb, draggedVal)
                        },
                        onDragEnd = {
                            activeThumb = -1
                            latestOnCommitted?.invoke(latestRange)
                        },
                        onDragCancel = { activeThumb = -1 },
                        onHorizontalDrag = { change, _ ->
                            if (activeThumb >= 0) {
                                val frac = (change.position.x / this.size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                updateThumb(activeThumb, fractionToValue(frac, min, max, inverted))
                            }
                        },
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            val trackWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val thumbPx = with(density) { size.thumbSize.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size.trackThickness)
                    .clip(CircleShape)
                    .background(colors.muted, CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset((leftFrac * trackWidthPx).roundToInt(), 0) }
                        .fillMaxWidth((rightFrac - leftFrac).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(fillBrush, CircleShape),
                )
            }

            markersToRender.forEach { marker ->
                val mf = valueToFraction(marker.value, min, max, inverted)
                val inRange = marker.value in startVal..endVal
                Box(
                    modifier = Modifier
                        .offset { IntOffset((mf * trackWidthPx - with(density) { 3.dp.toPx() }).roundToInt(), 0) }
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (inRange) colors.primaryForeground.copy(alpha = 0.85f) else colors.mutedForeground.copy(alpha = 0.45f),
                            CircleShape,
                        ),
                )
            }

            SliderThumbNode(
                size = size.thumbSize,
                borderColor = thumbBorderColor,
                active = activeThumb == 0 || hovered,
                focused = activeThumb == 0,
                thumbLabel = thumbLabels?.first,
                thumbIcon = thumbIcons?.first,
                showBubble = showBubble,
                bubblePosition = tooltipPosition,
                vertical = false,
                bubbleContent = {
                    BasicText(
                        text = formatValue(startVal),
                        style = KiraTheme.typography.caption.copy(color = colors.popoverForeground, fontWeight = FontWeight.Medium),
                    )
                },
                modifier = Modifier
                    .zIndex(if (activeThumb == 0) 3f else 2f)
                    .offset { IntOffset((animatedFStart * trackWidthPx - thumbPx / 2f).roundToInt(), 0) },
            )

            SliderThumbNode(
                size = size.thumbSize,
                borderColor = thumbBorderColor,
                active = activeThumb == 1 || hovered,
                focused = activeThumb == 1,
                thumbLabel = thumbLabels?.second,
                thumbIcon = thumbIcons?.second,
                showBubble = showBubble,
                bubblePosition = tooltipPosition,
                vertical = false,
                bubbleContent = {
                    BasicText(
                        text = formatValue(endVal),
                        style = KiraTheme.typography.caption.copy(color = colors.popoverForeground, fontWeight = FontWeight.Medium),
                    )
                },
                modifier = Modifier
                    .zIndex(if (activeThumb == 1) 3f else 2f)
                    .offset { IntOffset((animatedFEnd * trackWidthPx - thumbPx / 2f).roundToInt(), 0) },
            )
        }

        if (showInputs) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KiraNumericInput(
                    value = formatValue(startVal),
                    onValueChange = { str ->
                        val parsed = str.toFloatOrNull() ?: return@KiraNumericInput
                        updateThumb(0, parsed, commit = true)
                    },
                    label = "From",
                    min = min.toDouble(),
                    max = endVal.toDouble(),
                    step = step.toDouble().coerceAtLeast(0.1),
                    size = KiraInputSize.Sm,
                    enabled = enabled,
                    readOnly = readOnly,
                    modifier = Modifier.weight(1f),
                )
                KiraNumericInput(
                    value = formatValue(endVal),
                    onValueChange = { str ->
                        val parsed = str.toFloatOrNull() ?: return@KiraNumericInput
                        updateThumb(1, parsed, commit = true)
                    },
                    label = "To",
                    min = startVal.toDouble(),
                    max = max.toDouble(),
                    step = step.toDouble().coerceAtLeast(0.1),
                    size = KiraInputSize.Sm,
                    enabled = enabled,
                    readOnly = readOnly,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SliderHeaderRow(
    label: String?,
    required: Boolean,
    description: String?,
    error: String?,
    valueText: String?,
) {
    val colors = KiraTheme.colors
    if (label == null && description == null && error == null && valueText == null) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (label != null || valueText != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (label != null) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(
                            text = label,
                            style = KiraTheme.typography.label.copy(
                                color = colors.foreground,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        if (required) {
                            BasicText(
                                text = "*",
                                style = KiraTheme.typography.label.copy(color = colors.destructive),
                            )
                        }
                    }
                }
                if (valueText != null) {
                    BasicText(
                        text = valueText,
                        style = KiraTheme.typography.bodySmall.copy(
                            color = colors.mutedForeground,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
            }
        }
        val helper = error ?: description
        if (!helper.isNullOrBlank()) {
            BasicText(
                text = helper,
                style = KiraTheme.typography.caption.copy(
                    color = if (!error.isNullOrBlank()) colors.destructive else colors.mutedForeground,
                ),
            )
        }
    }
}

@Composable
private fun SliderThumbNode(
    size: Dp,
    borderColor: Color,
    active: Boolean,
    focused: Boolean,
    thumbLabel: String?,
    thumbIcon: (@Composable (Color) -> Unit)?,
    showBubble: Boolean,
    bubblePosition: KiraSliderTooltipPosition,
    vertical: Boolean,
    bubbleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val scaleState = animateFloatAsState(
        targetValue = if (active) 1.10f else 1f,
        animationSpec = KiraMotion.fastTween(),
        label = "SliderThumbScale",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val bubbleOffset = when {
            vertical -> Modifier.offset(x = size + 16.dp)
            bubblePosition == KiraSliderTooltipPosition.Bottom -> Modifier.offset(y = size + 8.dp)
            else -> Modifier.offset(y = -(size + 8.dp))
        }
        AnimatedVisibility(
            visible = showBubble,
            enter = fadeIn(tween(120, easing = KiraMotion.EaseOutStrong)) +
                scaleIn(tween(120, easing = KiraMotion.EaseOutStrong), initialScale = 0.90f),
            exit = fadeOut(tween(100, easing = KiraMotion.EaseOutStrong)) +
                scaleOut(tween(100, easing = KiraMotion.EaseOutStrong), targetScale = 0.90f),
            modifier = bubbleOffset,
        ) {
            val shape = KiraTheme.radius.shapeMd
            Box(
                modifier = Modifier
                    .shadow(6.dp, shape, clip = false)
                    .clip(shape)
                    .background(colors.popover, shape)
                    .border(1.dp, colors.border, shape)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                bubbleContent()
            }
        }

        Box(
            modifier = Modifier
                .graphicsLayer {
                    val s = scaleState.value
                    scaleX = s
                    scaleY = s
                }
                .size(size)
                .shadow(if (active) 6.dp else 3.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(colors.background, CircleShape)
                .border(if (focused) 3.dp else 2.dp, if (focused) colors.ring else borderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (thumbIcon != null) {
                thumbIcon(borderColor)
            } else if (!thumbLabel.isNullOrEmpty()) {
                BasicText(
                    text = thumbLabel,
                    maxLines = 1,
                    style = KiraTheme.typography.caption.copy(
                        color = borderColor,
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

private fun sliderFillBrush(
    variant: KiraSliderVariant,
    isError: Boolean,
    colors: com.kirakit.theme.KiraColorScheme,
    vertical: Boolean,
): Brush {
    if (isError) return SolidColor(colors.destructive)
    return when (variant) {
        KiraSliderVariant.Default -> SolidColor(colors.primary)
        KiraSliderVariant.Gradient -> if (vertical) {
            Brush.verticalGradient(listOf(colors.info, colors.primary))
        } else {
            Brush.horizontalGradient(listOf(colors.primary, colors.info))
        }
        KiraSliderVariant.Info -> SolidColor(colors.info)
        KiraSliderVariant.Success -> SolidColor(colors.success)
        KiraSliderVariant.Warning -> SolidColor(colors.warning)
        KiraSliderVariant.Destructive -> SolidColor(colors.destructive)
    }
}
