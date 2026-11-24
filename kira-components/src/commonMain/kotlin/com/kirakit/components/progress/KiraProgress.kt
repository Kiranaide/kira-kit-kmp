package com.kirakit.components.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography
import kotlin.math.roundToInt

enum class KiraProgressSize(
    val barHeight: Dp,
    val circleDiameter: Dp,
    val circleStroke: Dp,
) {
    Sm(barHeight = 4.dp, circleDiameter = 48.dp, circleStroke = 3.dp),
    Default(barHeight = 8.dp, circleDiameter = 64.dp, circleStroke = 4.dp),
    Lg(barHeight = 12.dp, circleDiameter = 80.dp, circleStroke = 5.dp),
    Xl(barHeight = 16.dp, circleDiameter = 96.dp, circleStroke = 6.dp),
}

enum class KiraProgressVariant {
    Default,
    Info,
    Success,
    Warning,
    Destructive,
}

enum class KiraProgressStatus(val displayLabel: String) {
    Idle("Ready"),
    Running("Running"),
    Paused("Paused"),
    Error("Failed"),
    Complete("Complete"),
    Canceled("Canceled"),
}

@Immutable
data class KiraNormalizedProgress(
    val value: Float?,
    val min: Float,
    val max: Float,
    val fraction: Float,
    val formatted: String?,
    val isComplete: Boolean,
)

@Immutable
data class KiraProgressSegment(
    val id: String,
    val value: Float,
    val variant: KiraProgressVariant? = null,
    val label: String? = null,
)

@Immutable
data class KiraProgressPhase(
    val id: String,
    val label: String,
    val weight: Float,
    val value: Float? = null,
    val min: Float = 0f,
    val max: Float = 100f,
)

/**
 * Normalizes a progress reading into bounded `min..max` and `0f..1f` fraction.
 * Invalid bounds fall back to `0f..100f`. `null` or non-finite values represent indeterminate progress.
 */
fun normalizeProgress(
    value: Float?,
    min: Float = 0f,
    max: Float = 100f,
): KiraNormalizedProgress {
    val validBounds = min.isFinite() && max.isFinite() && max > min && (max - min).isFinite()
    val safeMin = if (validBounds) min else 0f
    val safeMax = if (validBounds) max else 100f
    val measured = if (value != null && value.isFinite()) value.coerceIn(safeMin, safeMax) else null
    val fraction = if (measured == null) 0f else ((measured - safeMin) / (safeMax - safeMin)).coerceIn(0f, 1f)
    val formatted = measured?.let { "${(fraction * 100f).roundToInt()}%" }
    return KiraNormalizedProgress(
        value = measured,
        min = safeMin,
        max = safeMax,
        fraction = fraction,
        formatted = formatted,
        isComplete = measured != null && measured >= safeMax,
    )
}

/**
 * Clamps and normalizes stacked segments so their cumulative sum never exceeds [total].
 */
fun normalizeProgressSegments(
    segments: List<KiraProgressSegment>,
    total: Float,
): List<Pair<KiraProgressSegment, Float>> {
    val safeTotal = if (total.isFinite() && total > 0f) total else 100f
    var consumed = 0f
    return segments.map { segment ->
        val raw = if (segment.value.isFinite() && segment.value > 0f) segment.value else 0f
        val clamped = raw.coerceAtMost((safeTotal - consumed).coerceAtLeast(0f))
        consumed += clamped
        segment to (clamped / safeTotal).coerceIn(0f, 1f)
    }
}

/**
 * Computes overall `0..100` weighted percentage across multiple [KiraProgressPhase] entries.
 * Returns `null` (indeterminate) if any phase is indeterminate or has an invalid/duplicate weight.
 */
fun computeWeightedPhaseProgress(phases: List<KiraProgressPhase>): Float? {
    if (phases.isEmpty()) return null
    val seen = mutableSetOf<String>()
    var weightedSum = 0f
    var totalWeight = 0f
    for (phase in phases) {
        if (phase.id.isBlank() || !seen.add(phase.id) || !phase.weight.isFinite() || phase.weight <= 0f) {
            return null
        }
        val normalized = normalizeProgress(phase.value, phase.min, phase.max)
        if (normalized.value == null) return null
        weightedSum += phase.weight * normalized.fraction
        totalWeight += phase.weight
    }
    return if (totalWeight > 0f && totalWeight.isFinite()) (weightedSum / totalWeight) * 100f else null
}

/**
 * Linear progress bar aligned with kira-kit-v2's `Progress`.
 *
 * Supports determinate & indeterminate (`value = null`) modes, secondary `bufferValue`,
 * diagonal stripes (`striped`, `animated`), pause state, semantic color variants,
 * completion callback, and accessibility `progressBarRangeInfo`.
 */
@Composable
fun KiraProgress(
    value: Float?,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 100f,
    size: KiraProgressSize = KiraProgressSize.Default,
    variant: KiraProgressVariant = KiraProgressVariant.Default,
    showLabel: Boolean = false,
    striped: Boolean = false,
    animated: Boolean = false,
    paused: Boolean = false,
    bufferValue: Float? = null,
    contentDescription: String = "Progress",
    onComplete: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val pillShape = KiraTheme.radius.shapeFull
    val fillColor = resolveProgressColor(variant)
    val range = normalizeProgress(value, min, max)
    val bufferRange = bufferValue?.let { normalizeProgress(it, range.min, range.max) }

    var notifiedComplete by remember { mutableStateOf(false) }
    LaunchedEffect(range.isComplete) {
        if (range.isComplete && !notifiedComplete) {
            notifiedComplete = true
            onComplete?.invoke()
        } else if (!range.isComplete) {
            notifiedComplete = false
        }
    }

    val animatedFractionState = animateFloatAsState(
        targetValue = range.fraction,
        animationSpec = KiraMotion.standardTween(),
        label = "ProgressFraction",
    )
    val animatedBufferState = if (bufferValue != null) {
        animateFloatAsState(
            targetValue = if (range.value != null && bufferRange?.value != null) {
                maxOf(range.fraction, bufferRange.fraction)
            } else {
                0f
            },
            animationSpec = KiraMotion.standardTween(),
            label = "ProgressBuffer",
        )
    } else {
        null
    }

    val isIndeterminate = range.value == null
    val animateIndeterminate = isIndeterminate && !paused
    val animateStripes = !isIndeterminate && striped && animated && !paused

    val indeterminatePhaseState = if (animateIndeterminate) {
        val infinite = rememberInfiniteTransition(label = "ProgressIndeterminate")
        infinite.animateFloat(
            initialValue = -0.35f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = KiraMotion.EaseOutStrong),
                repeatMode = RepeatMode.Restart,
            ),
            label = "IndeterminatePhase",
        )
    } else {
        null
    }

    val stripeShiftState = if (animateStripes) {
        val infinite = rememberInfiniteTransition(label = "ProgressStripes")
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "StripeShift",
        )
    } else {
        null
    }

    val bufferColor = fillColor.copy(alpha = 0.25f)
    val stripeColor = colors.background.copy(alpha = 0.24f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(size.barHeight)
                .clip(pillShape)
                .background(colors.muted)
                .semantics {
                    this.contentDescription = contentDescription
                    progressBarRangeInfo = if (range.value == null) {
                        ProgressBarRangeInfo.Indeterminate
                    } else {
                        ProgressBarRangeInfo(range.value, range.min..range.max)
                    }
                    stateDescription = range.formatted ?: "Amount unknown"
                }
                .drawWithCache {
                    val stripeGap = 14.dp.toPx()
                    val stripeWidth = 5.dp.toPx()
                    onDrawBehind {
                        if (isIndeterminate) {
                            val barW = this.size.width * 0.34f
                            val startX = if (paused || indeterminatePhaseState == null) {
                                this.size.width * 0.15f
                            } else {
                                indeterminatePhaseState.value * this.size.width
                            }
                            drawRoundRect(
                                color = fillColor,
                                topLeft = Offset(startX, 0f),
                                size = Size(barW, this.size.height),
                            )
                        } else {
                            val bufFrac = animatedBufferState?.value ?: 0f
                            if (bufFrac > 0f) {
                                drawRect(
                                    color = bufferColor,
                                    topLeft = Offset.Zero,
                                    size = Size((this.size.width * bufFrac).coerceAtLeast(0f), this.size.height),
                                )
                            }
                            val fillW = (this.size.width * animatedFractionState.value).coerceAtLeast(0f)
                            if (fillW > 0f) {
                                drawRect(
                                    color = fillColor,
                                    topLeft = Offset.Zero,
                                    size = Size(fillW, this.size.height),
                                )
                                if (striped) {
                                    val offset = if (stripeShiftState != null) stripeShiftState.value * stripeGap else 0f
                                    var x = -this.size.height + offset
                                    while (x < fillW + this.size.height) {
                                        drawLine(
                                            color = stripeColor,
                                            start = Offset(x, this.size.height),
                                            end = Offset(x + this.size.height, 0f),
                                            strokeWidth = stripeWidth,
                                        )
                                        x += stripeGap
                                    }
                                }
                            }
                        }
                    }
                },
        )

        if (showLabel) {
            BasicText(
                text = range.formatted ?: "Amount unknown",
                modifier = Modifier.align(Alignment.End),
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
        }
    }
}

/**
 * Multi-segment stacked progress bar / meter aligned with kira-kit-v2's `SegmentedProgress`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraSegmentedProgress(
    segments: List<KiraProgressSegment>,
    total: Float,
    modifier: Modifier = Modifier,
    size: KiraProgressSize = KiraProgressSize.Default,
    defaultVariant: KiraProgressVariant = KiraProgressVariant.Default,
    showLabels: Boolean = false,
    contentDescription: String = "Segmented progress",
) {
    val colors = KiraTheme.colors
    val pillShape = KiraTheme.radius.shapeFull
    val normalized = normalizeProgressSegments(segments, total)
    val totalFraction = normalized.sumOf { it.second.toDouble() }.toFloat().coerceIn(0f, 1f)
    val summary = normalized.joinToString(", ") { (seg, frac) ->
        "${seg.label ?: seg.id}: ${(frac * 100f).roundToInt()}%"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(size.barHeight)
                .clip(pillShape)
                .background(colors.muted)
                .semantics {
                    this.contentDescription = contentDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(totalFraction, 0f..1f)
                    stateDescription = summary.ifEmpty { "Empty" }
                },
        ) {
            normalized.forEach { (segment, fraction) ->
                if (fraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(fraction)
                            .fillMaxHeight()
                            .background(resolveProgressColor(segment.variant ?: defaultVariant)),
                    )
                }
            }
            val remaining = (1f - totalFraction).coerceAtLeast(0f)
            if (remaining > 0f) {
                Box(Modifier.weight(remaining).fillMaxHeight())
            }
        }

        if (showLabels && normalized.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                normalized.forEach { (segment, fraction) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(pillShape)
                                .background(resolveProgressColor(segment.variant ?: defaultVariant)),
                        )
                        BasicText(
                            text = "${segment.label ?: segment.id}: ${(fraction * 100f).roundToInt()}%",
                            style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular ring progress indicator aligned with kira-kit-v2's `CircularProgress`.
 */
@Composable
fun KiraCircularProgress(
    value: Float?,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 100f,
    size: KiraProgressSize = KiraProgressSize.Default,
    variant: KiraProgressVariant = KiraProgressVariant.Default,
    showLabel: Boolean = false,
    indeterminate: Boolean = false,
    contentDescription: String = "Circular progress",
    onComplete: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val strokeColor = resolveProgressColor(variant)
    val range = normalizeProgress(if (indeterminate) null else value, min, max)
    val isUnknown = range.value == null

    var notifiedComplete by remember { mutableStateOf(false) }
    LaunchedEffect(range.isComplete) {
        if (range.isComplete && !notifiedComplete) {
            notifiedComplete = true
            onComplete?.invoke()
        } else if (!range.isComplete) {
            notifiedComplete = false
        }
    }

    val animatedSweepState = animateFloatAsState(
        targetValue = range.fraction * 360f,
        animationSpec = KiraMotion.standardTween(),
        label = "CircularProgressSweep",
    )

    val spinAngleState = if (isUnknown) {
        val infinite = rememberInfiniteTransition(label = "CircularProgressSpin")
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "SpinAngle",
        )
    } else {
        null
    }

    Box(
        modifier = modifier
            .size(size.circleDiameter)
            .semantics {
                this.contentDescription = contentDescription
                progressBarRangeInfo = if (range.value == null) {
                    ProgressBarRangeInfo.Indeterminate
                } else {
                    ProgressBarRangeInfo(range.value, range.min..range.max)
                }
                stateDescription = range.formatted ?: "In progress"
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .then(
                    if (spinAngleState != null) {
                        Modifier.graphicsLayer { rotationZ = spinAngleState.value }
                    } else {
                        Modifier
                    },
                )
                .clearAndSetSemantics {}
                .drawWithCache {
                    val strokePx = size.circleStroke.toPx()
                    val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)
                    val inset = strokePx / 2f
                    val topLeft = Offset(inset, inset)
                    val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
                    onDrawBehind {
                        drawArc(
                            color = colors.muted,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke,
                        )
                        drawArc(
                            color = strokeColor,
                            startAngle = -90f,
                            sweepAngle = if (isUnknown) 260f else animatedSweepState.value,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke,
                        )
                    }
                },
        )

        if (showLabel) {
            BasicText(
                text = range.formatted ?: "…",
                style = KiraTheme.typography.caption.copy(
                    color = colors.foreground,
                    fontWeight = FontWeight.Medium,
                    fontSize = if (size == KiraProgressSize.Sm) 10.sp else 12.sp,
                ),
            )
        }
    }
}

/**
 * Full lifecycle progress tracker aligned with kira-kit-v2's `KiraProgress`.
 *
 * Displays label, description, linear progress bar (or weighted multi-phase calculation),
 * live lifecycle status row with semantic icon, and contextual Pause / Resume / Cancel / Retry actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraProgressTracker(
    label: String,
    status: KiraProgressStatus,
    modifier: Modifier = Modifier,
    value: Float? = null,
    min: Float = 0f,
    max: Float = 100f,
    description: String? = null,
    size: KiraProgressSize = KiraProgressSize.Default,
    variant: KiraProgressVariant? = null,
    striped: Boolean = false,
    animated: Boolean = false,
    bufferValue: Float? = null,
    phases: List<KiraProgressPhase>? = null,
    currentPhaseId: String? = null,
    onPause: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val effectiveValue = if (phases != null) computeWeightedPhaseProgress(phases) else value
    val effectiveMin = if (phases != null) 0f else min
    val effectiveMax = if (phases != null) 100f else max
    val currentPhase = phases?.firstOrNull { it.id == currentPhaseId }

    val resolvedVariant = variant ?: when (status) {
        KiraProgressStatus.Error -> KiraProgressVariant.Destructive
        KiraProgressStatus.Complete -> KiraProgressVariant.Success
        KiraProgressStatus.Paused -> KiraProgressVariant.Warning
        else -> KiraProgressVariant.Default
    }

    val statusColor = when (status) {
        KiraProgressStatus.Error -> colors.destructive
        KiraProgressStatus.Complete -> colors.success
        KiraProgressStatus.Paused -> colors.warning
        KiraProgressStatus.Running -> colors.primary
        else -> colors.mutedForeground
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(
            text = label,
            style = KiraTheme.typography.label.copy(
                color = colors.foreground,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (description != null) {
            BasicText(
                text = description,
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
        }
        KiraProgress(
            value = effectiveValue,
            min = effectiveMin,
            max = effectiveMax,
            size = size,
            variant = resolvedVariant,
            showLabel = true,
            striped = striped,
            animated = animated && status == KiraProgressStatus.Running,
            paused = status != KiraProgressStatus.Running,
            bufferValue = if (phases != null) null else bufferValue,
            contentDescription = label,
            onComplete = onComplete,
        )

        Row(
            modifier = Modifier.semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ProgressLifecycleGlyph(status = status, color = statusColor)
            val statusText = buildString {
                append(status.displayLabel)
                if (currentPhase != null) {
                    append(" — ")
                    append(currentPhase.label)
                }
            }
            BasicText(
                text = statusText,
                style = KiraTheme.typography.bodySmall.copy(color = colors.foreground),
            )
        }

        val actions = buildList {
            when (status) {
                KiraProgressStatus.Running -> {
                    if (onPause != null) add("Pause" to onPause)
                    if (onCancel != null) add("Cancel" to onCancel)
                }
                KiraProgressStatus.Paused -> {
                    if (onResume != null) add("Resume" to onResume)
                    if (onCancel != null) add("Cancel" to onCancel)
                }
                KiraProgressStatus.Error, KiraProgressStatus.Canceled -> {
                    if (onRetry != null) add("Retry" to onRetry)
                }
                KiraProgressStatus.Idle, KiraProgressStatus.Complete -> Unit
            }
        }
        if (actions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                actions.forEach { (actionLabel, handler) ->
                    KiraButton(
                        text = actionLabel,
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = handler,
                    )
                }
            }
        }
    }
}

@Composable
private fun resolveProgressColor(variant: KiraProgressVariant): Color {
    val c = KiraTheme.colors
    return when (variant) {
        KiraProgressVariant.Default -> c.primary
        KiraProgressVariant.Info -> c.info
        KiraProgressVariant.Success -> c.success
        KiraProgressVariant.Warning -> c.warning
        KiraProgressVariant.Destructive -> c.destructive
    }
}

@Composable
private fun ProgressLifecycleGlyph(
    status: KiraProgressStatus,
    color: Color,
) {
    val angleState = if (status == KiraProgressStatus.Running) {
        val infinite = rememberInfiniteTransition(label = "ProgressLifecycleSpin")
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "LifecycleAngle",
        )
    } else {
        null
    }

    Box(
        Modifier
            .size(16.dp)
            .then(
                if (angleState != null) {
                    Modifier.graphicsLayer { rotationZ = angleState.value }
                } else {
                    Modifier
                },
            )
            .clearAndSetSemantics {}
            .drawWithCache {
                val strokeWidth = 1.8.dp.toPx()
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                val pad = strokeWidth
                val inner = Size(size.width - pad * 2f, size.height - pad * 2f)
                val checkPath = if (status == KiraProgressStatus.Complete) {
                    Path().apply {
                        moveTo(size.width * 0.20f, size.height * 0.52f)
                        lineTo(size.width * 0.42f, size.height * 0.74f)
                        lineTo(size.width * 0.82f, size.height * 0.28f)
                    }
                } else {
                    null
                }
                val barW = 2.dp.toPx()
                val dotRadius = 1.1.dp.toPx()
                onDrawBehind {
                    when (status) {
                        KiraProgressStatus.Idle -> {
                            drawCircle(color = color, radius = size.minDimension * 0.36f, style = stroke)
                        }
                        KiraProgressStatus.Running -> {
                            drawArc(
                                color = color.copy(alpha = 0.25f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(pad, pad),
                                size = inner,
                                style = stroke,
                            )
                            drawArc(
                                color = color,
                                startAngle = -90f,
                                sweepAngle = 120f,
                                useCenter = false,
                                topLeft = Offset(pad, pad),
                                size = inner,
                                style = stroke,
                            )
                        }
                        KiraProgressStatus.Paused -> {
                            drawLine(color, Offset(size.width * 0.35f, size.height * 0.24f), Offset(size.width * 0.35f, size.height * 0.76f), strokeWidth = barW, cap = StrokeCap.Round)
                            drawLine(color, Offset(size.width * 0.65f, size.height * 0.24f), Offset(size.width * 0.65f, size.height * 0.76f), strokeWidth = barW, cap = StrokeCap.Round)
                        }
                        KiraProgressStatus.Error -> {
                            drawCircle(color = color, radius = size.minDimension * 0.42f, style = stroke)
                            drawLine(color, Offset(size.width * 0.5f, size.height * 0.28f), Offset(size.width * 0.5f, size.height * 0.56f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            drawCircle(color = color, radius = dotRadius, center = Offset(size.width * 0.5f, size.height * 0.72f))
                        }
                        KiraProgressStatus.Complete -> {
                            checkPath?.let { drawPath(it, color = color, style = stroke) }
                        }
                        KiraProgressStatus.Canceled -> {
                            val p = size.width * 0.25f
                            drawLine(color, Offset(p, p), Offset(size.width - p, size.height - p), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            drawLine(color, Offset(size.width - p, p), Offset(p, size.height - p), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                        }
                    }
                }
            },
    )
}
