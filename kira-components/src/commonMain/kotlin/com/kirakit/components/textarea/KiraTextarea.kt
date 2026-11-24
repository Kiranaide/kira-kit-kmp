package com.kirakit.components.textarea

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlin.math.roundToInt

enum class KiraTextareaSize(
    internal val horizontalPadding: Dp,
    internal val verticalPadding: Dp,
    internal val minHeight: Dp,
    internal val fontSize: Int,
    internal val lineHeight: Int,
) {
    Sm(12.dp, 6.dp, 48.dp, 12, 18),
    Default(16.dp, 8.dp, 64.dp, 14, 22),
    Lg(20.dp, 10.dp, 80.dp, 16, 24),
    Xl(20.dp, 12.dp, 96.dp, 16, 26),
}

enum class KiraTextareaSubmitOn {
    ModEnter,
    Enter,
}

enum class KiraTextareaValidationMode {
    OnBlur,
    OnChange,
}

@Immutable
data class KiraTextareaCounterState(
    val current: Int,
    val max: Int,
    val remaining: Int,
    val isWarning: Boolean,
    val isOverLimit: Boolean,
)

/**
 * Evaluates textarea character counter warning/over-limit status, matching `textarea.tsx`.
 */
fun evaluateTextareaCounter(
    length: Int,
    maxLength: Int,
    warningThreshold: Float = 0.8f,
): KiraTextareaCounterState {
    val safeMax = maxLength.coerceAtLeast(1)
    val remaining = safeMax - length
    val overLimit = length > safeMax
    val warning = !overLimit && length >= safeMax * warningThreshold
    return KiraTextareaCounterState(
        current = length,
        max = safeMax,
        remaining = remaining,
        isWarning = warning,
        isOverLimit = overLimit,
    )
}

/**
 * Multi-line text input aligned with kira-kit-v2's `Textarea`, `TextareaWithCounter`, and `KiraTextarea`.
 *
 * Supports 4 size tiers (`Sm`, `Default`, `Lg`, `Xl`), `minRows`/`maxRows` & `autoSize`,
 * live character counter (`showCount` / `showRemaining` with warning threshold, soft or `hardLimit` cap,
 * and shake cue on over-limit), inline `clearable` button, `onBlur`/`onChange` validation,
 * and keyboard submit (`ModEnter` or chat-style `Enter`).
 */
@Composable
fun KiraTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    placeholder: String? = null,
    error: String? = null,
    size: KiraTextareaSize = KiraTextareaSize.Default,
    minRows: Int? = null,
    maxRows: Int? = null,
    autoSize: Boolean = true,
    maxLength: Int? = null,
    showCount: Boolean = false,
    showRemaining: Boolean = false,
    warningThreshold: Float = 0.8f,
    hardLimit: Boolean = false,
    clearable: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    required: Boolean = false,
    submitOn: KiraTextareaSubmitOn = KiraTextareaSubmitOn.ModEnter,
    onSubmit: ((String) -> Unit)? = null,
    validate: ((String) -> String?)? = null,
    validationMode: KiraTextareaValidationMode = KiraTextareaValidationMode.OnBlur,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val haptics = rememberKiraHaptics()
    val shape = KiraTheme.radius.shapeLg

    var isFocused by remember { mutableStateOf(false) }
    var hasValidated by remember { mutableStateOf(false) }
    var validateMessage by remember { mutableStateOf<String?>(null) }

    val counterState = maxLength?.let { evaluateTextareaCounter(value.length, it, warningThreshold) }
    val resolvedError = if (hasValidated && validate != null) validateMessage else error
    val isInvalid = resolvedError != null || (counterState?.isOverLimit == true)

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(counterState?.isOverLimit) {
        if (counterState?.isOverLimit == true) {
            listOf(-6f, 6f, -4f, 4f, 0f).forEach { target ->
                shakeOffset.animateTo(target, tween(45))
            }
        }
    }

    val containerBg = when {
        !enabled -> colors.input.copy(alpha = if (isDark) 0.80f else 0.50f)
        isDark -> colors.input.copy(alpha = 0.30f)
        else -> Color.Transparent
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            isInvalid -> colors.destructive
            isFocused -> colors.ring
            else -> colors.input
        },
        animationSpec = KiraMotion.fastTween(),
        label = "TextareaBorder",
    )

    val resolvedMinLines = (minRows ?: if (autoSize) 2 else 4).coerceAtLeast(1)
    val resolvedMaxLines = when {
        maxRows != null -> maxRows.coerceAtLeast(resolvedMinLines)
        !autoSize -> resolvedMinLines
        else -> Int.MAX_VALUE
    }
    val computedMinHeight = if (minRows != null) {
        (minRows * size.lineHeight).dp + size.verticalPadding * 2
    } else {
        size.minHeight
    }

    val textStyle = KiraTheme.typography.body.copy(
        color = colors.foreground,
        fontSize = size.fontSize.sp,
        lineHeight = size.lineHeight.sp,
    )

    val showClear = clearable && value.isNotEmpty() && enabled && !readOnly

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.50f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (label != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(
                    text = label,
                    style = KiraTheme.typography.label.copy(
                        color = if (isInvalid) colors.destructive else colors.foreground,
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

        if (description != null) {
            BasicText(
                text = description,
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = computedMinHeight)
                .clip(shape)
                .background(containerBg, shape)
                .border(BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor), shape),
        ) {
            BasicTextField(
                value = value,
                onValueChange = { next ->
                    val accepted = if (hardLimit && maxLength != null && next.length > maxLength) {
                        next.take(maxLength)
                    } else {
                        next
                    }
                    onValueChange(accepted)
                    if (validate != null && (validationMode == KiraTextareaValidationMode.OnChange || hasValidated)) {
                        hasValidated = true
                        validateMessage = validate(accepted)
                    }
                },
                enabled = enabled,
                readOnly = readOnly,
                minLines = resolvedMinLines,
                maxLines = resolvedMaxLines,
                keyboardOptions = keyboardOptions,
                textStyle = textStyle,
                cursorBrush = SolidColor(if (isInvalid) colors.destructive else colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        PaddingValues(
                            start = size.horizontalPadding,
                            top = size.verticalPadding,
                            end = if (showClear) size.horizontalPadding + 28.dp else size.horizontalPadding,
                            bottom = size.verticalPadding,
                        ),
                    )
                    .semantics {
                        if (label != null) contentDescription = label
                        if (isInvalid) error(resolvedError ?: "Invalid text")
                    }
                    .onFocusChanged { state ->
                        val wasFocused = isFocused
                        isFocused = state.isFocused
                        if (wasFocused && !state.isFocused && validate != null) {
                            hasValidated = true
                            validateMessage = validate(value)
                        }
                    }
                    .onPreviewKeyEvent { event ->
                        if (onSubmit == null || event.type != KeyEventType.KeyDown || event.key != Key.Enter) {
                            return@onPreviewKeyEvent false
                        }
                        val shouldSubmit = when (submitOn) {
                            KiraTextareaSubmitOn.ModEnter -> event.isCtrlPressed || event.isMetaPressed
                            KiraTextareaSubmitOn.Enter -> !event.isShiftPressed
                        }
                        if (shouldSubmit) {
                            val trimmed = value.trim()
                            if (trimmed.isNotEmpty()) {
                                haptics.perform(KiraHapticFeedbackType.Selection)
                                onSubmit(trimmed)
                            }
                            true
                        } else {
                            false
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty() && placeholder != null) {
                            BasicText(
                                text = placeholder,
                                style = textStyle.copy(color = colors.mutedForeground),
                            )
                        }
                        innerTextField()
                    }
                },
            )

            if (showClear) {
                val clearInteraction = remember { MutableInteractionSource() }
                val hovered by clearInteraction.collectIsHoveredAsState()
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(28.dp)
                        .clip(KiraTheme.radius.shapeSm)
                        .background(if (hovered) colors.muted else Color.Transparent, KiraTheme.radius.shapeSm)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .hoverable(clearInteraction)
                        .clickable(
                            interactionSource = clearInteraction,
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Light)
                                onValueChange("")
                                if (validate != null && hasValidated) {
                                    validateMessage = validate("")
                                }
                            },
                        )
                        .semantics { contentDescription = "Clear" },
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(12.dp).clearAndSetSemantics {}) {
                        val sw = 1.6.dp.toPx()
                        val p = this.size.width * 0.2f
                        drawLine(colors.mutedForeground, Offset(p, p), Offset(this.size.width - p, this.size.height - p), sw, StrokeCap.Round)
                        drawLine(colors.mutedForeground, Offset(this.size.width - p, p), Offset(p, this.size.height - p), sw, StrokeCap.Round)
                    }
                }
            }
        }

        if (showCount && counterState != null) {
            val counterColor by animateColorAsState(
                targetValue = when {
                    counterState.isOverLimit -> colors.destructive
                    counterState.isWarning -> colors.warning
                    else -> colors.mutedForeground
                },
                animationSpec = KiraMotion.fastTween(),
                label = "TextareaCounterColor",
            )
            val counterLabel = if (showRemaining) {
                "${counterState.remaining} remaining"
            } else {
                "${counterState.current}/${counterState.max}"
            }
            BasicText(
                text = counterLabel,
                modifier = Modifier
                    .align(Alignment.End)
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                style = KiraTheme.typography.caption.copy(
                    color = counterColor,
                    fontFeatureSettings = "tnum",
                ),
            )
        }

        if (!resolvedError.isNullOrBlank()) {
            BasicText(
                text = resolvedError,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                style = KiraTheme.typography.bodySmall.copy(color = colors.destructive),
            )
        }
    }
}
