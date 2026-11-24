package com.kirakit.components.stepper

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography
import kotlin.math.roundToInt

enum class KiraStepperOrientation {
    Horizontal,
    Vertical,
}

enum class KiraStepperSize(
    val indicatorSize: Dp,
    val fontSize: Int,
) {
    Sm(indicatorSize = 28.dp, fontSize = 12),
    Default(indicatorSize = 36.dp, fontSize = 14),
    Lg(indicatorSize = 44.dp, fontSize = 16),
}

enum class KiraStepState {
    Pending,
    Active,
    Completed,
    Error,
    Loading,
    Skipped,
}

@Immutable
data class KiraStepItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val optional: Boolean = false,
    val disabled: Boolean = false,
    val icon: (@Composable (Color) -> Unit)? = null,
)

/**
 * Resolves the display state of step [index] given [activeStep] and explicit state sets.
 */
fun resolveStepState(
    index: Int,
    activeStep: Int,
    completedSteps: Set<Int> = emptySet(),
    errorSteps: Set<Int> = emptySet(),
    loadingSteps: Set<Int> = emptySet(),
    skippedSteps: Set<Int> = emptySet(),
): KiraStepState = when {
    index in loadingSteps -> KiraStepState.Loading
    index in errorSteps -> KiraStepState.Error
    index in skippedSteps -> KiraStepState.Skipped
    index in completedSteps -> KiraStepState.Completed
    index == activeStep -> KiraStepState.Active
    index < activeStep -> KiraStepState.Completed
    else -> KiraStepState.Pending
}

/**
 * Determines whether clicking step [targetIndex] is permitted under linear/editable/disabled rules.
 */
fun canNavigateToStep(
    targetIndex: Int,
    activeStep: Int,
    totalSteps: Int,
    disabledSteps: Set<Int> = emptySet(),
    completedSteps: Set<Int> = emptySet(),
    loadingSteps: Set<Int> = emptySet(),
    linear: Boolean = false,
    editable: Boolean = true,
): Boolean {
    if (targetIndex !in 0 until totalSteps) return false
    if (targetIndex in disabledSteps || activeStep in loadingSteps) return false
    if (targetIndex == activeStep) return true
    if (targetIndex < activeStep) return editable
    if (!linear) return true
    val nextEnabled = (activeStep + 1 until totalSteps).firstOrNull { it !in disabledSteps }
    return targetIndex == nextEnabled && activeStep in completedSteps
}

/**
 * Calculates `0f..1f` completion fraction for [KiraStepperProgress].
 */
fun calculateStepperProgress(currentStep: Int, totalSteps: Int): Float {
    if (totalSteps <= 1) return if (totalSteps == 1 && currentStep >= 0) 1f else 0f
    return (currentStep.toFloat() / (totalSteps - 1).toFloat()).coerceIn(0f, 1f)
}

/**
 * Multi-step workflow indicator & wizard container aligned with kira-kit-v2's `Stepper` & `KiraStepper`.
 *
 * - Supports horizontal & vertical orientations, 3 size tiers, automatic mobile stacking
 *   (`mobileStacked = true` below `640.dp`), linear vs non-linear navigation, optional/skippable steps,
 *   error/loading/completed/skipped states, progress bar, step panel transitions, and navigation controls.
 */
@Composable
fun KiraStepper(
    steps: List<KiraStepItem>,
    activeStep: Int,
    onStepChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    orientation: KiraStepperOrientation = KiraStepperOrientation.Horizontal,
    size: KiraStepperSize = KiraStepperSize.Default,
    linear: Boolean = false,
    editable: Boolean = true,
    mobileStacked: Boolean = true,
    completedSteps: Set<Int> = emptySet(),
    errorSteps: Set<Int> = emptySet(),
    loadingSteps: Set<Int> = emptySet(),
    skippedSteps: Set<Int> = emptySet(),
    showProgress: Boolean = false,
    showNavigation: Boolean = false,
    backLabel: String = "Back",
    nextLabel: String = "Continue",
    skipLabel: String = "Skip for now",
    finishLabel: String = "Finish",
    navigationError: String? = null,
    nextDisabled: Boolean = false,
    onNext: (() -> Boolean)? = null,
    onSkip: ((Int) -> Unit)? = null,
    onFinish: (() -> Unit)? = null,
    stepContent: (@Composable (Int) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val windowWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    val isCompact = windowWidthDp > 0.dp && windowWidthDp < 640.dp
    val effectiveOrientation = if (mobileStacked && isCompact && orientation == KiraStepperOrientation.Horizontal) {
        KiraStepperOrientation.Vertical
    } else {
        orientation
    }

    val disabledSet = remember(steps) {
        steps.mapIndexedNotNull { idx, item -> if (item.disabled) idx else null }.toSet()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (effectiveOrientation == KiraStepperOrientation.Horizontal) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                steps.forEachIndexed { index, step ->
                    val state = resolveStepState(index, activeStep, completedSteps, errorSteps, loadingSteps, skippedSteps)
                    val prevCompleted = index > 0 && resolveStepState(
                        index = index - 1,
                        activeStep = activeStep,
                        completedSteps = completedSteps,
                        errorSteps = errorSteps,
                        loadingSteps = loadingSteps,
                        skippedSteps = skippedSteps,
                    ) == KiraStepState.Completed
                    val navigable = canNavigateToStep(
                        targetIndex = index,
                        activeStep = activeStep,
                        totalSteps = steps.size,
                        disabledSteps = disabledSet,
                        completedSteps = completedSteps,
                        loadingSteps = loadingSteps,
                        linear = linear,
                        editable = editable,
                    )
                    HorizontalStepItem(
                        step = step,
                        index = index,
                        isLast = index == steps.lastIndex,
                        state = state,
                        prevCompleted = prevCompleted,
                        size = size,
                        navigable = navigable,
                        onClick = {
                            if (navigable && index != activeStep) onStepChange(index)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                steps.forEachIndexed { index, step ->
                    val state = resolveStepState(index, activeStep, completedSteps, errorSteps, loadingSteps, skippedSteps)
                    val navigable = canNavigateToStep(
                        targetIndex = index,
                        activeStep = activeStep,
                        totalSteps = steps.size,
                        disabledSteps = disabledSet,
                        completedSteps = completedSteps,
                        loadingSteps = loadingSteps,
                        linear = linear,
                        editable = editable,
                    )
                    VerticalStepItem(
                        step = step,
                        index = index,
                        isLast = index == steps.lastIndex,
                        state = state,
                        size = size,
                        navigable = navigable,
                        onClick = {
                            if (navigable && index != activeStep) onStepChange(index)
                        },
                    )
                }
            }
        }

        if (showProgress && steps.isNotEmpty()) {
            KiraStepperProgress(
                currentStep = activeStep,
                totalSteps = steps.size,
            )
        }

        if (stepContent != null && activeStep in steps.indices) {
            AnimatedContent(
                targetState = activeStep,
                transitionSpec = {
                    fadeIn(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) togetherWith
                        fadeOut(tween(110))
                },
                label = "KiraStepperPanel",
            ) { stepIdx ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    stepContent(stepIdx)
                }
            }
        }

        if (showNavigation && steps.isNotEmpty()) {
            val prevEnabled = (activeStep - 1 downTo 0).firstOrNull { it !in disabledSet }
            val nextEnabled = (activeStep + 1 until steps.size).firstOrNull { it !in disabledSet }
            val isLast = nextEnabled == null
            val currentItem = steps.getOrNull(activeStep)
            val isBusy = activeStep in loadingSteps

            KiraStepperNavigation(
                canGoBack = prevEnabled != null && editable,
                canSkip = currentItem?.optional == true && !isLast,
                isLastStep = isLast,
                isLoading = isBusy,
                nextDisabled = nextDisabled,
                backLabel = backLabel,
                nextLabel = nextLabel,
                skipLabel = skipLabel,
                finishLabel = finishLabel,
                errorText = navigationError,
                onBack = { if (prevEnabled != null) onStepChange(prevEnabled) },
                onSkip = {
                    onSkip?.invoke(activeStep)
                    if (nextEnabled != null) onStepChange(nextEnabled)
                },
                onNext = {
                    val allowed = onNext?.invoke() ?: true
                    if (allowed) {
                        if (nextEnabled != null) onStepChange(nextEnabled) else onFinish?.invoke()
                    }
                },
                onFinish = {
                    val allowed = onNext?.invoke() ?: true
                    if (allowed) onFinish?.invoke()
                },
            )
        }
    }
}

@Composable
private fun HorizontalStepItem(
    step: KiraStepItem,
    index: Int,
    isLast: Boolean,
    state: KiraStepState,
    prevCompleted: Boolean,
    size: KiraStepperSize,
    navigable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    val prevLineColor by animateColorAsState(
        targetValue = if (prevCompleted) colors.primary.copy(alpha = 0.55f) else colors.border,
        animationSpec = KiraMotion.fastTween(),
    )
    val nextLineColor by animateColorAsState(
        targetValue = if (state == KiraStepState.Completed) colors.primary.copy(alpha = 0.55f) else colors.border,
        animationSpec = KiraMotion.fastTween(),
    )

    Box(modifier = modifier) {
        val indicatorRowHeight = size.indicatorSize + 14.dp
        if (index > 0 || !isLast) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(indicatorRowHeight)
                    .clearAndSetSemantics {},
            ) {
                val centerY = this.size.height / 2f
                val halfWidth = this.size.width / 2f
                val gapFromCenter = (this.size.height / 2f) + 6.dp.toPx()
                val strokePx = 1.5.dp.toPx()

                if (index > 0) {
                    val leftEnd = (halfWidth - gapFromCenter).coerceAtLeast(0f)
                    if (leftEnd > 0f) {
                        drawLine(
                            color = prevLineColor,
                            start = Offset(0f, centerY),
                            end = Offset(leftEnd, centerY),
                            strokeWidth = strokePx,
                            cap = StrokeCap.Butt,
                        )
                    }
                }
                if (!isLast) {
                    val rightStart = (halfWidth + gapFromCenter).coerceAtMost(this.size.width)
                    if (rightStart < this.size.width) {
                        drawLine(
                            color = nextLineColor,
                            start = Offset(rightStart, centerY),
                            end = Offset(this.size.width, centerY),
                            strokeWidth = strokePx,
                            cap = StrokeCap.Butt,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .alpha(if (step.disabled) 0.5f else 1f)
                .clip(radius.shapeLg)
                .background(if (hovered && navigable) colors.muted.copy(alpha = 0.45f) else Color.Transparent)
                .hoverable(interaction, enabled = navigable)
                .pointerHoverIcon(if (navigable) PointerIcon.Hand else PointerIcon.Default)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = navigable,
                    role = Role.Button,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Selection)
                        onClick()
                    },
                )
                .semantics(mergeDescendants = true) {
                    selected = state == KiraStepState.Active
                    stateDescription = state.name
                    if (!navigable) disabled()
                }
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StepIndicatorBadge(
                index = index,
                state = state,
                size = size,
                icon = step.icon,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BasicText(
                    text = step.title,
                    style = KiraTheme.typography.label.copy(
                        color = when (state) {
                            KiraStepState.Pending -> colors.mutedForeground
                            KiraStepState.Error -> colors.destructive
                            else -> colors.foreground
                        },
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                )
                if (step.optional) {
                    BasicText(
                        text = "Optional",
                        style = KiraTheme.typography.caption.copy(
                            color = colors.mutedForeground,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
                if (step.description != null) {
                    BasicText(
                        text = step.description,
                        style = KiraTheme.typography.caption.copy(
                            color = if (state == KiraStepState.Error) colors.destructive else colors.mutedForeground,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalStepItem(
    step: KiraStepItem,
    index: Int,
    isLast: Boolean,
    state: KiraStepState,
    size: KiraStepperSize,
    navigable: Boolean,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    val lineColor by animateColorAsState(
        targetValue = if (state == KiraStepState.Completed) colors.primary.copy(alpha = 0.55f) else colors.border,
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .alpha(if (step.disabled) 0.5f else 1f)
            .clip(radius.shapeLg)
            .background(if (hovered && navigable) colors.muted.copy(alpha = 0.45f) else Color.Transparent)
            .hoverable(interaction, enabled = navigable)
            .pointerHoverIcon(if (navigable) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = navigable,
                role = Role.Button,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onClick()
                },
            )
            .semantics(mergeDescendants = true) {
                selected = state == KiraStepState.Active
                stateDescription = state.name
                if (!navigable) disabled()
            }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
        ) {
            StepIndicatorBadge(
                index = index,
                state = state,
                size = size,
                icon = step.icon,
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(1.5.dp)
                        .weight(1f)
                        .defaultMinSize(minHeight = 20.dp)
                        .background(lineColor),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 44.dp)
                .padding(top = 4.dp, bottom = if (isLast) 4.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BasicText(
                    text = step.title,
                    style = KiraTheme.typography.label.copy(
                        color = when (state) {
                            KiraStepState.Pending -> colors.mutedForeground
                            KiraStepState.Error -> colors.destructive
                            else -> colors.foreground
                        },
                        fontWeight = FontWeight.Medium,
                    ),
                )
                if (step.optional) {
                    BasicText(
                        text = "(Optional)",
                        style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                    )
                }
            }
            if (step.description != null) {
                BasicText(
                    text = step.description,
                    style = KiraTheme.typography.caption.copy(
                        color = if (state == KiraStepState.Error) colors.destructive else colors.mutedForeground,
                    ),
                )
            }
        }
    }
}

@Composable
private fun StepIndicatorBadge(
    index: Int,
    state: KiraStepState,
    size: KiraStepperSize,
    icon: (@Composable (Color) -> Unit)?,
) {
    val colors = KiraTheme.colors
    val bg by animateColorAsState(
        targetValue = when (state) {
            KiraStepState.Active -> colors.primary
            KiraStepState.Completed -> colors.primary.copy(alpha = 0.12f)
            KiraStepState.Error -> colors.destructive.copy(alpha = 0.12f)
            else -> colors.background
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            KiraStepState.Active -> colors.primary
            KiraStepState.Completed -> colors.primary.copy(alpha = 0.30f)
            KiraStepState.Error -> colors.destructive.copy(alpha = 0.45f)
            KiraStepState.Loading -> colors.primary.copy(alpha = 0.35f)
            else -> colors.border
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val fg by animateColorAsState(
        targetValue = when (state) {
            KiraStepState.Active -> colors.primaryForeground
            KiraStepState.Completed, KiraStepState.Loading -> colors.primary
            KiraStepState.Error -> colors.destructive
            else -> colors.mutedForeground
        },
        animationSpec = KiraMotion.fastTween(),
    )

    val haloMod = if (state == KiraStepState.Active) {
        Modifier
            .clip(CircleShape)
            .background(colors.primary.copy(alpha = 0.15f))
            .padding(3.dp)
    } else {
        Modifier.padding(3.dp)
    }

    Box(modifier = haloMod, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size.indicatorSize)
                .clip(CircleShape)
                .background(bg)
                .then(
                    if (state == KiraStepState.Skipped) {
                        Modifier
                    } else {
                        Modifier.border(1.5.dp, borderColor, CircleShape)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (state == KiraStepState.Skipped) {
                Canvas(Modifier.size(size.indicatorSize).clearAndSetSemantics {}) {
                    val strokePx = 1.5.dp.toPx()
                    drawCircle(
                        color = borderColor,
                        radius = (this.size.minDimension - strokePx) / 2f,
                        style = Stroke(
                            width = strokePx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()), 0f),
                        ),
                    )
                }
            }

            when (state) {
                KiraStepState.Completed -> StepCheckGlyph(color = fg)
                KiraStepState.Error -> StepErrorGlyph(color = fg)
                KiraStepState.Loading -> StepSpinnerGlyph(color = fg)
                KiraStepState.Skipped -> StepSkipGlyph(color = fg)
                KiraStepState.Active, KiraStepState.Pending -> {
                    if (icon != null) {
                        Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                            icon(fg)
                        }
                    } else {
                        BasicText(
                            text = (index + 1).toString(),
                            style = KiraTheme.typography.label.copy(
                                color = fg,
                                fontSize = size.fontSize.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact step progress bar aligned with kira-kit-v2's `StepperProgress`.
 */
@Composable
fun KiraStepperProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    labels: List<String> = emptyList(),
) {
    val colors = KiraTheme.colors
    val pill = KiraTheme.radius.shapeFull
    val safeTotal = totalSteps.coerceAtLeast(0)
    val displayStep = (currentStep + 1).coerceIn(0, safeTotal)
    val fraction = calculateStepperProgress(currentStep, safeTotal)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(220, easing = KiraMotion.EaseOutStrong),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "Step $displayStep of $safeTotal",
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
            BasicText(
                text = "${(fraction * 100f).roundToInt()}%",
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(pill)
                .background(colors.muted)
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    stateDescription = "Step $displayStep of $safeTotal"
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(pill)
                    .background(colors.primary),
            )
        }

        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                labels.forEachIndexed { idx, label ->
                    BasicText(
                        text = label,
                        style = KiraTheme.typography.caption.copy(
                            color = if (idx == currentStep) colors.foreground else colors.mutedForeground,
                            fontWeight = if (idx == currentStep) FontWeight.Medium else FontWeight.Normal,
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Navigation footer aligned with kira-kit-v2's `StepperNavigation`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraStepperNavigation(
    canGoBack: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    canSkip: Boolean = false,
    onSkip: (() -> Unit)? = null,
    isLastStep: Boolean = false,
    isLoading: Boolean = false,
    nextDisabled: Boolean = false,
    showBack: Boolean = true,
    showNext: Boolean = true,
    backLabel: String = "Back",
    nextLabel: String = "Continue",
    skipLabel: String = "Skip for now",
    finishLabel: String = "Finish",
    errorText: String? = null,
) {
    val colors = KiraTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBack) {
                KiraButton(
                    text = backLabel,
                    variant = KiraButtonVariant.Ghost,
                    size = KiraButtonSize.Sm,
                    enabled = canGoBack && !isLoading,
                    onClick = onBack,
                )
            } else {
                Box(Modifier.size(1.dp))
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (canSkip && onSkip != null && !isLastStep) {
                    KiraButton(
                        text = skipLabel,
                        variant = KiraButtonVariant.Ghost,
                        size = KiraButtonSize.Sm,
                        enabled = !isLoading,
                        onClick = onSkip,
                    )
                }
                if (showNext) {
                    KiraButton(
                        text = if (isLastStep) finishLabel else nextLabel,
                        variant = KiraButtonVariant.Primary,
                        size = KiraButtonSize.Sm,
                        enabled = !nextDisabled && !isLoading,
                        loading = isLoading,
                        onClick = if (isLastStep) onFinish else onNext,
                    )
                }
            }
        }
        if (!errorText.isNullOrBlank()) {
            BasicText(
                text = errorText,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                style = KiraTheme.typography.bodySmall.copy(color = colors.destructive),
            )
        }
    }
}

@Composable
private fun StepCheckGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.20f, size.height * 0.52f)
            lineTo(size.width * 0.42f, size.height * 0.74f)
            lineTo(size.width * 0.82f, size.height * 0.28f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun StepErrorGlyph(color: Color) {
    Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
        val stroke = 1.9.dp.toPx()
        val pad = size.width * 0.22f
        drawLine(color, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun StepSkipGlyph(color: Color) {
    Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.24f)
            lineTo(size.width * 0.58f, size.height * 0.50f)
            lineTo(size.width * 0.22f, size.height * 0.76f)
            close()
        }
        drawPath(path, color = color, style = stroke)
        drawLine(color, Offset(size.width * 0.76f, size.height * 0.24f), Offset(size.width * 0.76f, size.height * 0.76f), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun StepSpinnerGlyph(color: Color) {
    val infinite = rememberInfiniteTransition(label = "StepSpinner")
    val angleState = infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "StepSpinnerAngle",
    )
    Canvas(Modifier.size(16.dp).graphicsLayer { rotationZ = angleState.value }.clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val pad = stroke.width
        val inner = Size(size.width - pad * 2f, size.height - pad * 2f)
        drawArc(color.copy(alpha = 0.25f), 0f, 360f, false, Offset(pad, pad), inner, style = stroke)
        drawArc(color, -90f, 120f, false, Offset(pad, pad), inner, style = stroke)
    }
}
