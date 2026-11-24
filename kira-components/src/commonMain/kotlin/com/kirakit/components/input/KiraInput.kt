package com.kirakit.components.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong
import kotlin.math.sin

private val LocalInputShapeOverride = compositionLocalOf<Shape?> { null }

enum class KiraInputSize(
    val visualHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val fontSize: Int,
    internal val iconSize: Dp,
    internal val stepperIconSize: Dp,
) {
    Xs(28.dp, 8.dp, 12, 12.dp, 12.dp),
    Sm(32.dp, 12.dp, 12, 16.dp, 16.dp),
    Default(36.dp, 16.dp, 14, 16.dp, 16.dp),
    Lg(40.dp, 20.dp, 14, 16.dp, 20.dp),
    Xl(44.dp, 20.dp, 16, 16.dp, 20.dp),
}

enum class KiraInputPosition { Left, Right }

enum class KiraOtpSize(val boxSize: Dp, internal val fontSize: Int) {
    Sm(36.dp, 14),
    Default(44.dp, 18),
    Lg(56.dp, 20),
}

enum class KiraOtpVariant { Default, Outline, Filled }

enum class KiraOtpGap(val spacing: Dp) {
    Sm(6.dp),
    Default(8.dp),
    Lg(12.dp),
}

enum class KiraOtpType { Numeric, Alphanumeric, Alpha }

enum class KiraRatingSize(val iconSize: Dp) {
    Sm(16.dp),
    Default(20.dp),
    Lg(24.dp),
    Xl(32.dp),
}

enum class KiraRatingColor { Default, Yellow, Orange, Red, Pink }

enum class KiraNumberFormat { Decimal, Currency, Percentage }

@Composable
@Suppress("DEPRECATION")
fun KiraInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    size: KiraInputSize = KiraInputSize.Default,
    readOnly: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    clearable: Boolean = false,
    onClear: (() -> Unit)? = null,
    copyable: Boolean = false,
    onCopySuccess: (() -> Unit)? = null,
    maxLength: Int? = null,
    showCounter: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textAlign: TextAlign = TextAlign.Start,
    shape: Shape? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val resolvedShape = shape ?: LocalInputShapeOverride.current ?: KiraTheme.radius.shapeLg
    var isFocused by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val limit = maxLength?.coerceAtLeast(0)

    val containerBg = when {
        !enabled -> colors.input.copy(alpha = if (isDark) 0.80f else 0.50f)
        isDark -> colors.input.copy(alpha = 0.30f)
        else -> Color.Transparent
    }

    val targetBorderColor = when {
        isError -> colors.destructive
        isFocused -> colors.ring
        else -> colors.input
    }
    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = KiraMotion.fastTween(),
        label = "KiraInputBorder",
    )

    val textStyle = KiraTheme.typography.body.copy(
        color = colors.foreground,
        fontSize = size.fontSize.sp,
        lineHeight = (size.fontSize + 6).sp,
        textAlign = textAlign,
    )

    val showClearButton = clearable && enabled && !readOnly && value.isNotEmpty()

    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.50f),
        verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.xs),
    ) {
        if (label != null) {
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.label.copy(
                    color = if (isError) colors.destructive else colors.foreground,
                ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = size.visualHeight)
                .clip(resolvedShape)
                .background(containerBg, resolvedShape)
                .border(BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor), resolvedShape)
                .padding(horizontal = size.horizontalPadding, vertical = if (singleLine) 0.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            BasicTextField(
                value = value,
                onValueChange = { next ->
                    if (limit == null || next.length <= limit || next.length < value.length) {
                        onValueChange(next)
                    }
                },
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                textStyle = textStyle,
                cursorBrush = SolidColor(if (isError) colors.destructive else colors.primary),
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        if (label != null) contentDescription = label
                        if (isError) error(helperText ?: "Invalid input")
                    }
                    .onFocusChanged {
                        if (isFocused != it.isFocused) {
                            isFocused = it.isFocused
                            onFocusChange?.invoke(it.isFocused)
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = if (singleLine) size.visualHeight else 0.dp),
                        contentAlignment = if (textAlign == TextAlign.Center) Alignment.Center else Alignment.CenterStart,
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            BasicText(
                                text = placeholder,
                                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                                overflow = TextOverflow.Ellipsis,
                                style = textStyle.copy(color = colors.mutedForeground),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (showClearButton) {
                InputInlineIconButton(
                    contentDescription = "Clear input",
                    enabled = enabled,
                    onClick = {
                        onValueChange("")
                        onClear?.invoke()
                    },
                ) { color ->
                    InputIconX(color = color, size = size.iconSize)
                }
            }
            if (copyable && value.isNotEmpty()) {
                InputInlineIconButton(
                    contentDescription = if (copied) "Copied" else "Copy to clipboard",
                    enabled = enabled,
                    onClick = {
                        clipboard.setText(AnnotatedString(value))
                        copied = true
                        onCopySuccess?.invoke()
                        scope.launch {
                            delay(2_000)
                            copied = false
                        }
                    },
                ) { color ->
                    if (copied) {
                        InputIconCheck(color = colors.success, size = size.iconSize)
                    } else {
                        InputIconCopy(color = color, size = size.iconSize)
                    }
                }
            }
            trailing?.invoke()
        }

        if (helperText != null) {
            BasicText(
                text = helperText,
                style = KiraTheme.typography.caption.copy(
                    color = if (isError) colors.destructive else colors.mutedForeground,
                ),
            )
        }
        if (showCounter) {
            KiraCharacterCounter(
                currentLength = value.length,
                maxLength = limit,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
fun KiraCharacterCounter(
    currentLength: Int,
    maxLength: Int? = null,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val atLimit = maxLength != null && currentLength >= maxLength
    val nearLimit = maxLength != null && !atLimit && currentLength >= floor(0.9 * maxLength).toInt()
    val countColor = when {
        atLimit -> colors.destructive
        nearLimit -> colors.warning
        else -> colors.mutedForeground
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BasicText(
            text = currentLength.toString(),
            style = KiraTheme.typography.caption.copy(
                color = countColor,
                fontFeatureSettings = "tnum",
            ),
        )
        if (maxLength != null) {
            BasicText(
                text = "/$maxLength",
                style = KiraTheme.typography.caption.copy(
                    color = colors.mutedForeground,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

/**
 * Joined stepper numeric input matching kira-kit-v2's `NumericInput` (`[ − | value | + ]`),
 * with optional horizontal drag scrubbing (`scrub = true`) and keyboard arrow stepping.
 */
@Composable
fun KiraNumericInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    min: Double? = null,
    max: Double? = null,
    step: Double = 1.0,
    smallStep: Double = 0.1,
    largeStep: Double = 10.0,
    decimals: Int = inferDecimals(step),
    showControls: Boolean = true,
    scrub: Boolean = false,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) {
    require(min == null || max == null || min <= max)
    require(step.isFinite() && step > 0 && min?.isFinite() != false && max?.isFinite() != false)

    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val radius = KiraTheme.radius.lg
    val digits = decimals.coerceIn(0, 8)
    val factor = 10.0.pow(digits)

    fun formatValue(number: Double): String {
        val scaled = number * factor
        val rounded = if (scaled.isFinite()) round(scaled) / factor else number
        return plainDecimal(rounded).removeSuffix(".0")
    }

    fun bounded(number: Double): Double = number.coerceIn(min ?: -Double.MAX_VALUE, max ?: Double.MAX_VALUE)

    val pattern = remember(digits) { Regex(if (digits == 0) "-?[0-9]*" else "-?[0-9]*(\\.[0-9]{0,$digits})?") }
    val currentNumber = value.toDoubleOrNull() ?: 0.0
    val canDecrease = enabled && !readOnly && (min == null || currentNumber > min)
    val canIncrease = enabled && !readOnly && (max == null || currentNumber < max)

    val leftShape = RoundedCornerShape(topStart = radius, bottomStart = radius, topEnd = 0.dp, bottomEnd = 0.dp)
    val rightShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = radius, bottomEnd = radius)

    var isFocused by remember { mutableStateOf(false) }
    var scrubRemainder by remember { mutableFloatStateOf(0f) }
    val latestValue by rememberUpdatedState(value)
    val latestOnChange by rememberUpdatedState(onValueChange)

    val containerBg = when {
        !enabled -> colors.input.copy(alpha = if (isDark) 0.80f else 0.50f)
        isDark -> colors.input.copy(alpha = 0.30f)
        else -> Color.Transparent
    }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) colors.ring else colors.input,
        animationSpec = KiraMotion.fastTween(),
        label = "NumericInputBorder",
    )

    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.50f),
        verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.xs),
    ) {
        if (label != null) {
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.label.copy(color = colors.foreground),
            )
        }

        if (!showControls) {
            KiraInput(
                value = value,
                onValueChange = { if (pattern.matches(it)) onValueChange(it) },
                size = size,
                enabled = enabled,
                readOnly = readOnly,
                keyboardOptions = KeyboardOptions(keyboardType = if (digits == 0) KeyboardType.Number else KeyboardType.Decimal),
                onFocusChange = { focused ->
                    if (!focused) {
                        val parsed = value.toDoubleOrNull()
                        if (parsed != null && parsed.isFinite()) {
                            val normalized = formatValue(bounded(parsed))
                            if (normalized != value) onValueChange(normalized)
                        } else if (value.isNotEmpty()) {
                            onValueChange("")
                        }
                    }
                },
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size.visualHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NumericStepperButton(
                    contentDescription = "Decrease value",
                    enabled = canDecrease,
                    shape = leftShape,
                    size = size,
                    isDark = isDark,
                    onClick = { onValueChange(formatValue(bounded(currentNumber - step))) },
                ) { color ->
                    InputIconMinus(color = color, size = size.stepperIconSize)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(containerBg, RectangleShape)
                        .border(BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor), RectangleShape)
                        .then(
                            if (scrub && enabled && !readOnly) {
                                Modifier.pointerInput(min, max, step) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { scrubRemainder = 0f },
                                        onHorizontalDrag = { _, dragAmount ->
                                            scrubRemainder += dragAmount
                                            val steps = (scrubRemainder / 12f).toInt()
                                            if (steps != 0) {
                                                scrubRemainder -= steps * 12f
                                                val base = latestValue.toDoubleOrNull() ?: 0.0
                                                latestOnChange(formatValue(bounded(base + steps * step)))
                                            }
                                        },
                                    )
                                }
                            } else {
                                Modifier
                            },
                        )
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = { if (pattern.matches(it)) onValueChange(it) },
                        enabled = enabled,
                        readOnly = readOnly,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (digits == 0) KeyboardType.Number else KeyboardType.Decimal,
                        ),
                        textStyle = KiraTheme.typography.body.copy(
                            color = colors.foreground,
                            fontSize = size.fontSize.sp,
                            textAlign = TextAlign.Center,
                            fontFeatureSettings = "tnum",
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                isFocused = state.isFocused
                                if (!state.isFocused) {
                                    val parsed = value.toDoubleOrNull()
                                    if (parsed != null && parsed.isFinite()) {
                                        val normalized = formatValue(bounded(parsed))
                                        if (normalized != value) onValueChange(normalized)
                                    } else if (value.isNotEmpty()) {
                                        onValueChange("")
                                    }
                                }
                            }
                            .onPreviewKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown || !enabled || readOnly) return@onPreviewKeyEvent false
                                val delta = when {
                                    event.isShiftPressed -> largeStep
                                    event.isAltPressed -> smallStep
                                    else -> step
                                }
                                when (event.key) {
                                    Key.DirectionUp -> {
                                        onValueChange(formatValue(bounded(currentNumber + delta)))
                                        true
                                    }
                                    Key.DirectionDown -> {
                                        onValueChange(formatValue(bounded(currentNumber - delta)))
                                        true
                                    }
                                    else -> false
                                }
                            },
                    )
                }

                NumericStepperButton(
                    contentDescription = "Increase value",
                    enabled = canIncrease,
                    shape = rightShape,
                    size = size,
                    isDark = isDark,
                    onClick = { onValueChange(formatValue(bounded(currentNumber + step))) },
                ) { color ->
                    InputIconPlus(color = color, size = size.stepperIconSize)
                }
            }
        }
    }
}

@Composable
private fun NumericStepperButton(
    contentDescription: String,
    enabled: Boolean,
    shape: Shape,
    size: KiraInputSize,
    isDark: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val colors = KiraTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg = when {
        hovered && enabled -> if (isDark) colors.input.copy(alpha = 0.50f) else colors.muted.copy(alpha = 0.50f)
        isDark -> colors.input.copy(alpha = 0.30f)
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(size.visualHeight)
            .clip(shape)
            .background(bg, shape)
            .border(1.dp, colors.input, shape)
            .alpha(if (enabled) 1f else 0.50f)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .hoverable(interactionSource, enabled = enabled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        icon(colors.foreground)
    }
}

/**
 * Currency formatted input matching kira-kit-v2's `CurrencyInput` (auto-decimal + grouping format).
 */
@Composable
fun KiraCurrencyInput(
    value: Double?,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    currency: String = "USD",
    fractionDigits: Int = 2,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
    placeholder: String? = null,
) {
    val digits = fractionDigits.coerceIn(0, 6)
    val display = if (value == null || value.isNaN()) {
        ""
    } else {
        formatCurrencyAmount(value, currency, digits)
    }
    KiraFormattedNumberField(
        display = display,
        fractionDigits = digits,
        trailingLiteral = "",
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        size = size,
        enabled = enabled,
    )
}

/**
 * Percentage formatted input matching kira-kit-v2's `PercentageInput` (auto-decimal + `%` suffix).
 */
@Composable
fun KiraPercentageInput(
    value: Double?,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    fractionDigits: Int = 2,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
    placeholder: String? = null,
) {
    val digits = fractionDigits.coerceIn(0, 6)
    val display = if (value == null || value.isNaN()) {
        ""
    } else {
        "${formatGroupedDecimal(value, digits, useGrouping = true)}%"
    }
    KiraFormattedNumberField(
        display = display,
        fractionDigits = digits,
        trailingLiteral = "%",
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        size = size,
        enabled = enabled,
    )
}

/**
 * Fixed-fraction decimal formatted input matching kira-kit-v2's `DecimalInput`.
 */
@Composable
fun KiraDecimalInput(
    value: Double?,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    fractionDigits: Int = 2,
    useGrouping: Boolean = true,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
    placeholder: String? = null,
) {
    val digits = fractionDigits.coerceIn(0, 6)
    val display = if (value == null || value.isNaN()) {
        ""
    } else {
        formatGroupedDecimal(value, digits, useGrouping = useGrouping)
    }
    KiraFormattedNumberField(
        display = display,
        fractionDigits = digits,
        trailingLiteral = "",
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        size = size,
        enabled = enabled,
    )
}

@Composable
private fun KiraFormattedNumberField(
    display: String,
    fractionDigits: Int,
    trailingLiteral: String,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val resolvedShape = LocalInputShapeOverride.current ?: KiraTheme.radius.shapeLg
    var isFocused by remember { mutableStateOf(false) }

    val containerBg = when {
        !enabled -> colors.input.copy(alpha = if (isDark) 0.80f else 0.50f)
        isDark -> colors.input.copy(alpha = 0.30f)
        else -> Color.Transparent
    }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) colors.ring else colors.input,
        animationSpec = KiraMotion.fastTween(),
        label = "FormattedInputBorder",
    )
    val textStyle = KiraTheme.typography.body.copy(
        color = colors.foreground,
        fontSize = size.fontSize.sp,
        lineHeight = (size.fontSize + 6).sp,
        fontFeatureSettings = "tnum",
    )

    // Keep the caret at the end of the numeric portion (right before any trailing literal like '%').
    val caretIndex = if (trailingLiteral.isNotEmpty() && display.endsWith(trailingLiteral)) {
        (display.length - trailingLiteral.length).coerceAtLeast(0)
    } else {
        display.length
    }
    val tfv = TextFieldValue(text = display, selection = TextRange(caretIndex))

    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.50f),
        verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.xs),
    ) {
        if (label != null) {
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.label.copy(color = colors.foreground),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = size.visualHeight)
                .clip(resolvedShape)
                .background(containerBg, resolvedShape)
                .border(BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor), resolvedShape)
                .padding(horizontal = size.horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = tfv,
                onValueChange = { next ->
                    val raw = next.text
                    if (raw == display) return@BasicTextField
                    var digits = raw.filter { it in '0'..'9' }
                    // If user backspaced the trailing literal (e.g. '%'), drop the last digit instead.
                    if (trailingLiteral.isNotEmpty() &&
                        display.endsWith(trailingLiteral) &&
                        !raw.endsWith(trailingLiteral) &&
                        raw.length < display.length &&
                        digits == display.filter { it in '0'..'9' } &&
                        digits.isNotEmpty()
                    ) {
                        digits = digits.dropLast(1)
                    }
                    if (digits.isEmpty()) {
                        onValueChange(null)
                        return@BasicTextField
                    }
                    val intVal = digits.take(15).toLongOrNull()
                    if (intVal == null || (intVal == 0L && raw.length < display.length)) {
                        onValueChange(null)
                    } else {
                        val factor = 10.0.pow(fractionDigits)
                        onValueChange(intVal / factor)
                    }
                },
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        if (label != null) contentDescription = label
                    }
                    .onFocusChanged { isFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = size.visualHeight),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (display.isEmpty() && placeholder != null) {
                            BasicText(
                                text = placeholder,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = textStyle.copy(color = colors.mutedForeground),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

private fun formatCurrencyAmount(value: Double, currency: String, fractionDigits: Int): String {
    val symbol = when (currency.uppercase()) {
        "USD" -> "$"
        "IDR" -> "Rp"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        else -> "${currency.uppercase()} "
    }
    val useDotGrouping = currency.uppercase() in setOf("IDR", "EUR")
    val formatted = formatGroupedDecimal(
        value = value,
        fractionDigits = fractionDigits,
        useGrouping = true,
        groupSep = if (useDotGrouping) '.' else ',',
        decimalSep = if (useDotGrouping) ',' else '.',
    )
    return if (currency.uppercase() == "IDR") "$symbol $formatted" else "$symbol$formatted"
}

private fun formatGroupedDecimal(
    value: Double,
    fractionDigits: Int,
    useGrouping: Boolean,
    groupSep: Char = ',',
    decimalSep: Char = '.',
): String {
    val negative = value < 0
    val absVal = abs(value)
    val factor = 10.0.pow(fractionDigits)
    val scaled = round(absVal * factor).roundToLong()
    val divisor = factor.roundToLong().coerceAtLeast(1L)
    val whole = scaled / divisor
    val frac = scaled % divisor
    val wholeStr = whole.toString()
    val groupedWhole = if (useGrouping) {
        wholeStr.reversed().chunked(3).joinToString(groupSep.toString()).reversed()
    } else {
        wholeStr
    }
    val sign = if (negative) "-" else ""
    return if (fractionDigits == 0) {
        "$sign$groupedWhole"
    } else {
        val fracStr = frac.toString().padStart(fractionDigits, '0')
        "$sign$groupedWhole$decimalSep$fracStr"
    }
}

private fun inferDecimals(step: Double): Int {
    val text = plainDecimal(step)
    val dot = text.indexOf('.')
    return if (dot < 0) 0 else text.length - dot - 1
}

private fun plainDecimal(value: Double): String {
    if (value == 0.0) return "0"
    val text = value.toString().uppercase()
    val exponent = text.substringAfter('E', "").toIntOrNull() ?: return text
    val sign = if (value < 0) "-" else ""
    val mantissa = text.substringBefore('E').removePrefix("-")
    val digits = mantissa.replace(".", "").trimEnd('0')
    val point = mantissa.substringBefore('.').length + exponent
    return sign + when {
        point <= 0 -> "0." + "0".repeat(-point) + digits
        point >= digits.length -> digits.padEnd(point, '0')
        else -> digits.substring(0, point) + "." + digits.substring(point)
    }
}

@Composable
fun KiraInputAffix(text: String) {
    BasicText(text, style = KiraTheme.typography.body.copy(color = KiraTheme.colors.mutedForeground))
}

/**
 * Joined input + button composite matching kira-kit-v2's `InputWithButton`.
 */
@Composable
fun KiraInputWithButton(
    value: String,
    onValueChange: (String) -> Unit,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    buttonPosition: KiraInputPosition = KiraInputPosition.Right,
    buttonVariant: KiraButtonVariant = KiraButtonVariant.Primary,
    buttonIcon: (@Composable (Color, Dp) -> Unit)? = null,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
) {
    val radius = KiraTheme.radius.lg
    val leftShape = RoundedCornerShape(topStart = radius, bottomStart = radius, topEnd = 0.dp, bottomEnd = 0.dp)
    val rightShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = radius, bottomEnd = radius)
    val btnSize = when (size) {
        KiraInputSize.Xs -> KiraButtonSize.Xs
        KiraInputSize.Sm -> KiraButtonSize.Sm
        KiraInputSize.Default -> KiraButtonSize.Md
        KiraInputSize.Lg -> KiraButtonSize.Lg
        KiraInputSize.Xl -> KiraButtonSize.Xl
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.xs)) {
        if (label != null) {
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.label.copy(color = KiraTheme.colors.foreground),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (buttonPosition == KiraInputPosition.Left) {
                KiraButton(
                    text = buttonText,
                    variant = buttonVariant,
                    size = btnSize,
                    enabled = enabled,
                    leftIcon = buttonIcon,
                    shape = leftShape,
                    onClick = onButtonClick,
                )
            }
            KiraInput(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                size = size,
                enabled = enabled,
                shape = if (buttonPosition == KiraInputPosition.Left) rightShape else leftShape,
                modifier = Modifier.weight(1f),
            )
            if (buttonPosition == KiraInputPosition.Right) {
                KiraButton(
                    text = buttonText,
                    variant = buttonVariant,
                    size = btnSize,
                    enabled = enabled,
                    leftIcon = buttonIcon,
                    shape = rightShape,
                    onClick = onButtonClick,
                )
            }
        }
    }
}

/**
 * Input with trailing loading spinner matching kira-kit-v2's `InputWithSpinner`.
 */
@Composable
fun KiraInputWithSpinner(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isLoading: Boolean = false,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
) {
    val colors = KiraTheme.colors
    KiraInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        size = size,
        enabled = enabled,
        trailing = if (isLoading) {
            { InputSpinner(color = colors.mutedForeground, size = size.iconSize) }
        } else null,
    )
}

/**
 * Joined input + dropdown selector matching kira-kit-v2's `InputWithDropdown`.
 */
@Composable
fun KiraInputWithDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    dropdownPosition: KiraInputPosition = KiraInputPosition.Left,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val radius = KiraTheme.radius.lg
    val leftShape = RoundedCornerShape(topStart = radius, bottomStart = radius, topEnd = 0.dp, bottomEnd = 0.dp)
    val rightShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = radius, bottomEnd = radius)
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = options.firstOrNull { it.first == selectedOption }?.second
        ?: options.firstOrNull()?.second.orEmpty()

    val dropdownSlot: @Composable () -> Unit = {
        val triggerShape = if (dropdownPosition == KiraInputPosition.Left) leftShape else rightShape
        val interactionSource = remember { MutableInteractionSource() }
        val hovered by interactionSource.collectIsHoveredAsState()
        val triggerBg = when {
            hovered && enabled -> if (isDark) colors.input.copy(alpha = 0.50f) else colors.muted.copy(alpha = 0.50f)
            isDark -> colors.input.copy(alpha = 0.30f)
            else -> Color.Transparent
        }
        Box {
            Row(
                modifier = Modifier
                    .height(size.visualHeight)
                    .clip(triggerShape)
                    .background(triggerBg, triggerShape)
                    .border(1.dp, colors.input, triggerShape)
                    .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                    .hoverable(interactionSource, enabled = enabled)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        role = Role.DropdownList,
                        onClick = { expanded = !expanded },
                    )
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = selectedLabel,
                    style = KiraTheme.typography.body.copy(
                        color = colors.foreground,
                        fontSize = size.fontSize.sp,
                    ),
                    maxLines = 1,
                )
                InputIconChevronDown(color = colors.mutedForeground, size = 12.dp)
            }

            if (expanded) {
                Popup(
                    alignment = Alignment.BottomStart,
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(min = 120.dp)
                            .clip(KiraTheme.radius.shapeLg)
                            .background(colors.popover)
                            .border(1.dp, colors.border, KiraTheme.radius.shapeLg)
                            .padding(4.dp),
                    ) {
                        options.forEach { (optValue, optLabel) ->
                            val itemSource = remember { MutableInteractionSource() }
                            val itemHover by itemSource.collectIsHoveredAsState()
                            val isSelected = optValue == selectedOption
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(KiraTheme.radius.shapeMd)
                                    .background(if (itemHover || isSelected) colors.muted else Color.Transparent)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .hoverable(itemSource)
                                    .clickable(
                                        interactionSource = itemSource,
                                        indication = null,
                                        onClick = {
                                            onOptionSelect(optValue)
                                            expanded = false
                                        },
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                            ) {
                                BasicText(
                                    text = optLabel,
                                    style = KiraTheme.typography.body.copy(color = colors.popoverForeground),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.xs)) {
        if (label != null) {
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.label.copy(color = colors.foreground),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dropdownPosition == KiraInputPosition.Left) dropdownSlot()
            KiraInput(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                size = size,
                enabled = enabled,
                shape = if (dropdownPosition == KiraInputPosition.Left) rightShape else leftShape,
                modifier = Modifier.weight(1f),
            )
            if (dropdownPosition == KiraInputPosition.Right) dropdownSlot()
        }
    }
}

/**
 * Joined horizontal cluster of inputs matching kira-kit-v2's `InputCluster`
 * (outer corners rounded, inner seams squared).
 */
@Composable
fun KiraInputCluster(
    modifier: Modifier = Modifier,
    left: @Composable RowScope.() -> Unit,
    right: @Composable RowScope.() -> Unit,
) {
    val radius = KiraTheme.radius.lg
    val leftShape = RoundedCornerShape(topStart = radius, bottomStart = radius, topEnd = 0.dp, bottomEnd = 0.dp)
    val rightShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = radius, bottomEnd = radius)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CompositionLocalProvider(LocalInputShapeOverride provides leftShape) { left() }
        CompositionLocalProvider(LocalInputShapeOverride provides rightShape) { right() }
    }
}

/**
 * Search input with leading magnifying glass icon, debounced search, clear `×`, and loading spinner
 * matching kira-kit-v2's `SearchInput`.
 */
@Composable
fun KiraSearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    label: String? = null,
    debounceMillis: Long? = 300L,
    loading: Boolean = false,
    showClear: Boolean = true,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
    placeholder: String = "Search...",
) {
    val colors = KiraTheme.colors
    val search by rememberUpdatedState(onSearch)
    val wait = debounceMillis
    var submitted by remember { mutableStateOf(value) }

    fun submit(query: String) {
        submitted = query
        search?.invoke(query)
    }

    LaunchedEffect(value, wait, enabled) {
        if (enabled && wait != null && value != submitted) {
            delay(wait.coerceAtLeast(0))
            if (value != submitted) submit(value)
        }
    }

    KiraInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                onValueChange("")
                submit("")
                true
            } else {
                false
            }
        },
        label = label,
        placeholder = placeholder,
        size = size,
        enabled = enabled,
        clearable = showClear,
        onClear = { submit("") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { submit(value) }),
        leading = { InputIconSearch(color = colors.mutedForeground, size = size.iconSize) },
        trailing = if (loading) {
            { InputSpinner(color = colors.mutedForeground, size = size.iconSize) }
        } else null,
    )
}

/**
 * Password input with Eye/EyeOff visibility toggle and animated strength meter + requirement checklist,
 * matching kira-kit-v2's `PasswordInput`.
 */
@Composable
fun KiraPasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Enter password",
    showStrength: Boolean = false,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
) {
    val colors = KiraTheme.colors
    var visible by remember { mutableStateOf(false) }

    val criteria = listOf(
        "At least 8 characters" to (value.length >= 8),
        "Upper & lowercase letters" to (value.any(Char::isUpperCase) && value.any(Char::isLowerCase)),
        "A number" to value.any(Char::isDigit),
        "A symbol" to value.any { !it.isLetterOrDigit() && !it.isWhitespace() },
    )
    val score = if (value.isEmpty()) 0 else criteria.count { it.second }
    val strengthLabel = when (score) {
        0 -> "Very weak"
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        else -> "Strong"
    }
    val strengthColor = when (score) {
        0, 1 -> colors.destructive
        2 -> colors.warning
        3 -> colors.info
        else -> colors.success
    }
    val barFraction by animateFloatAsState(
        targetValue = score / 4f,
        animationSpec = tween(durationMillis = KiraMotion.FadeInMs, easing = KiraMotion.EaseOutStrong),
        label = "PasswordStrengthBar",
    )
    val animatedBarColor by animateColorAsState(
        targetValue = strengthColor,
        animationSpec = tween(durationMillis = KiraMotion.FadeInMs),
        label = "PasswordStrengthColor",
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KiraInput(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            size = size,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = {
                InputInlineIconButton(
                    contentDescription = if (visible) "Hide password" else "Show password",
                    enabled = enabled,
                    onClick = { visible = !visible },
                ) { color ->
                    if (visible) {
                        InputIconEyeOff(color = color, size = size.iconSize)
                    } else {
                        InputIconEye(color = color, size = size.iconSize)
                    }
                }
            },
        )
        if (showStrength) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(KiraTheme.radius.shapeFull)
                            .background(colors.muted),
                    ) {
                        if (barFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barFraction)
                                    .fillMaxHeight()
                                    .clip(KiraTheme.radius.shapeFull)
                                    .background(animatedBarColor),
                            )
                        }
                    }
                    BasicText(
                        text = strengthLabel,
                        modifier = Modifier.widthIn(min = 64.dp),
                        style = KiraTheme.typography.caption.copy(
                            color = colors.mutedForeground,
                            textAlign = TextAlign.End,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    criteria.forEach { (text, met) ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (met) {
                                InputIconCheck(color = colors.success, size = 12.dp)
                            } else {
                                InputIconX(color = colors.mutedForeground.copy(alpha = 0.50f), size = 12.dp)
                            }
                            BasicText(
                                text = text,
                                style = KiraTheme.typography.caption.copy(
                                    color = if (met) colors.success else colors.mutedForeground,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * OTP / PIN input matching kira-kit-v2's `OTPInput` (sizes, variants, gaps, separator, mask, placeholder).
 */
@Composable
fun KiraOtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    label: String? = null,
    type: KiraOtpType = KiraOtpType.Numeric,
    size: KiraOtpSize = KiraOtpSize.Default,
    variant: KiraOtpVariant = KiraOtpVariant.Default,
    gap: KiraOtpGap = KiraOtpGap.Default,
    separatorAfter: Int? = null,
    placeholder: String = "○",
    enabled: Boolean = true,
    isError: Boolean = false,
    masked: Boolean = false,
    onComplete: ((String) -> Unit)? = null,
) {
    require(length > 0)
    var focused by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf(TextRange(value.length)) }
    val cursor = TextRange(selection.start.coerceIn(0, value.length), selection.end.coerceAtMost(value.length))
    val requester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = KiraTheme.colors

    fun sanitize(text: String): String {
        val filtered = text.filter { ch ->
            when (type) {
                KiraOtpType.Numeric -> ch in '0'..'9'
                KiraOtpType.Alphanumeric -> ch.isLetterOrDigit()
                KiraOtpType.Alpha -> ch.isLetter()
            }
        }
        val normalized = if (type != KiraOtpType.Numeric) filtered.uppercase() else filtered
        return normalized.take(length)
    }

    val slotShape = when (size) {
        KiraOtpSize.Sm -> KiraTheme.radius.shapeMd
        KiraOtpSize.Default -> KiraTheme.radius.shapeLg
        KiraOtpSize.Lg -> KiraTheme.radius.shapeXl
    }

    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.50f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (label != null) {
            BasicText(
                text = label,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.label.copy(color = colors.foreground),
            )
        }
        BasicTextField(
            value = TextFieldValue(value, cursor),
            onValueChange = { raw ->
                val next = sanitize(raw.text)
                selection = TextRange(
                    sanitize(raw.text.take(raw.selection.start)).length,
                    sanitize(raw.text.take(raw.selection.end)).length,
                )
                if (next != value) {
                    onValueChange(next)
                    if (next.length == length) onComplete?.invoke(next)
                }
            },
            modifier = Modifier
                .focusRequester(requester)
                .onFocusChanged { focused = it.isFocused }
                .semantics {
                    contentDescription = label ?: "One-time password"
                    if (isError) error("Invalid verification code")
                },
            enabled = enabled,
            singleLine = true,
            visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (type == KiraOtpType.Numeric) KeyboardType.Number else KeyboardType.Text,
            ),
            decorationBox = { inner ->
                Box {
                    Box(Modifier.size(1.dp).clipToBounds()) { inner() }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap.spacing),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(length) { index ->
                            val isActiveSlot = focused && index == cursor.end.coerceAtMost(length - 1)
                            val slotBg = when (variant) {
                                KiraOtpVariant.Filled -> colors.muted
                                KiraOtpVariant.Default, KiraOtpVariant.Outline -> colors.background
                            }
                            val borderWidth = when {
                                isActiveSlot -> 2.dp
                                variant == KiraOtpVariant.Outline -> 2.dp
                                variant == KiraOtpVariant.Filled && !isError -> 0.dp
                                else -> 1.dp
                            }
                            val slotBorder = when {
                                isError -> colors.destructive
                                isActiveSlot -> colors.ring
                                variant == KiraOtpVariant.Filled -> Color.Transparent
                                else -> colors.input
                            }
                            Box(
                                modifier = Modifier
                                    .size(size.boxSize)
                                    .clip(slotShape)
                                    .background(slotBg, slotShape)
                                    .then(
                                        if (borderWidth > 0.dp) {
                                            Modifier.border(borderWidth, slotBorder, slotShape)
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .then(
                                        if (enabled) {
                                            Modifier.pointerInput(index, value.length) {
                                                detectTapGestures { point ->
                                                    requester.requestFocus()
                                                    val end = if (point.x > this.size.width / 2f) index + 1 else index
                                                    selection = TextRange(end.coerceAtMost(value.length))
                                                    keyboard?.show()
                                                }
                                            }
                                        } else {
                                            Modifier
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                val character = value.getOrNull(index)
                                val displayChar = when {
                                    character == null -> placeholder
                                    masked -> "•"
                                    else -> character.toString()
                                }
                                BasicText(
                                    text = displayChar,
                                    modifier = Modifier.clearAndSetSemantics {},
                                    style = KiraTheme.typography.label.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = size.fontSize.sp,
                                        color = when {
                                            character == null -> colors.mutedForeground.copy(alpha = 0.45f)
                                            enabled -> colors.foreground
                                            else -> colors.mutedForeground
                                        },
                                    ),
                                )
                            }
                            if (separatorAfter != null && index == separatorAfter - 1 && index < length - 1) {
                                BasicText(
                                    text = "—",
                                    modifier = Modifier.clearAndSetSemantics {},
                                    style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

/**
 * Vector star rating input matching kira-kit-v2's `Rating` (sizes, colors, half-stars, showValue, allowClear).
 */
@Composable
fun KiraRatingInput(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    max: Int = 5,
    allowHalf: Boolean = false,
    allowClear: Boolean = true,
    size: KiraRatingSize = KiraRatingSize.Default,
    color: KiraRatingColor = KiraRatingColor.Yellow,
    showValue: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
) {
    require(max > 0)
    val colors = KiraTheme.colors
    val selectable = enabled && !readOnly
    val direction = LocalLayoutDirection.current
    val update by rememberUpdatedState(onValueChange)
    var focused by remember { mutableStateOf(false) }
    val step = if (allowHalf) 0.5f else 1f

    val filledTint = when {
        !enabled -> colors.mutedForeground
        else -> resolveRatingColor(color)
    }

    fun select(next: Float) {
        val final = if (allowClear && next == value) 0f else next
        update(final.coerceIn(0f, max.toFloat()))
    }

    val keyboardMod = if (!selectable) Modifier else Modifier
        .onFocusChanged { focused = it.isFocused }
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            val forward = if (direction == LayoutDirection.Ltr) step else -step
            val next = when (event.key) {
                Key.DirectionRight -> value + forward
                Key.DirectionLeft -> value - forward
                Key.DirectionUp -> value + step
                Key.DirectionDown -> value - step
                Key.MoveHome -> 0f
                Key.MoveEnd -> max.toFloat()
                else -> return@onPreviewKeyEvent false
            }
            update(next.coerceIn(0f, max.toFloat()))
            true
        }
        .focusable()

    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.60f)
            .semantics(mergeDescendants = true) {
                contentDescription = "Rating"
                progressBarRangeInfo = ProgressBarRangeInfo(
                    value.coerceIn(0f, max.toFloat()),
                    0f..max.toFloat(),
                    (max / step).toInt() - 1,
                )
                if (selectable) {
                    setProgress { requested ->
                        update((round(requested / step) * step).coerceIn(0f, max.toFloat()))
                        true
                    }
                }
                if (!enabled) disabled()
            }
            .then(keyboardMod)
            .then(if (focused) Modifier.border(2.dp, colors.ring, KiraTheme.radius.shapeSm) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in 1..max) {
            val fraction = (value - index + 1).coerceIn(0f, 1f)
            val starInteraction = remember { MutableInteractionSource() }
            val hovered by starInteraction.collectIsHoveredAsState()
            val scale by animateFloatAsState(
                targetValue = if (selectable && hovered) 1.12f else 1f,
                animationSpec = KiraMotion.fastTween(),
                label = "RatingStarScale",
            )
            Box(
                modifier = Modifier
                    .size(size.iconSize + 4.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerHoverIcon(if (selectable) PointerIcon.Hand else PointerIcon.Default)
                    .hoverable(starInteraction, enabled = selectable)
                    .then(
                        if (selectable) {
                            Modifier.pointerInput(index, allowHalf, value, direction) {
                                detectTapGestures { point ->
                                    val isFirstHalf = if (direction == LayoutDirection.Ltr) {
                                        point.x < this.size.width / 2f
                                    } else {
                                        point.x > this.size.width / 2f
                                    }
                                    val chosen = if (allowHalf && isFirstHalf) index - 0.5f else index.toFloat()
                                    select(chosen)
                                }
                            }
                        } else {
                            Modifier
                        },
                    )
                    .clearAndSetSemantics {},
                contentAlignment = Alignment.Center,
            ) {
                RatingStarCanvas(
                    fillFraction = fraction,
                    filledColor = filledTint,
                    emptyColor = colors.mutedForeground.copy(alpha = 0.40f),
                    size = size.iconSize,
                    isRtl = direction == LayoutDirection.Rtl,
                )
            }
        }
        if (showValue) {
            val formatted = if (allowHalf) {
                val tenths = round(value * 10f).toInt()
                "${tenths / 10}.${tenths % 10}"
            } else {
                round(value).toInt().toString()
            }
            BasicText(
                text = formatted,
                modifier = Modifier.padding(start = 6.dp),
                style = KiraTheme.typography.label.copy(
                    color = colors.mutedForeground,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

/**
 * Read-only rating display with numeric score and review count, matching kira-kit-v2's `RatingDisplay`.
 */
@Composable
fun KiraRatingDisplay(
    value: Float,
    modifier: Modifier = Modifier,
    reviewCount: Int? = null,
    showFraction: Boolean = true,
    size: KiraRatingSize = KiraRatingSize.Default,
    color: KiraRatingColor = KiraRatingColor.Yellow,
) {
    val colors = KiraTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KiraRatingInput(
            value = value,
            onValueChange = {},
            allowHalf = showFraction,
            size = size,
            color = color,
            readOnly = true,
        )
        if (showFraction) {
            val tenths = round(value * 10f).toInt()
            BasicText(
                text = "${tenths / 10}.${tenths % 10}",
                style = KiraTheme.typography.label.copy(
                    color = colors.foreground,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
        if (reviewCount != null) {
            val formatted = reviewCount.toString().reversed().chunked(3).joinToString(",").reversed()
            BasicText(
                text = "($formatted)",
                style = KiraTheme.typography.body.copy(
                    color = colors.mutedForeground,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

@Composable
private fun resolveRatingColor(color: KiraRatingColor): Color = when (color) {
    KiraRatingColor.Default -> KiraTheme.colors.primary
    KiraRatingColor.Yellow -> Color(0xFFEAB308)
    KiraRatingColor.Orange -> Color(0xFFF97316)
    KiraRatingColor.Red -> Color(0xFFEF4444)
    KiraRatingColor.Pink -> Color(0xFFEC4899)
}

@Composable
private fun RatingStarCanvas(
    fillFraction: Float,
    filledColor: Color,
    emptyColor: Color,
    size: Dp,
    isRtl: Boolean,
) {
    Canvas(Modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val outer = this.size.minDimension * 0.46f
        val inner = outer * 0.45f
        val starPath = Path()
        for (i in 0 until 10) {
            val angle = (-90.0 + i * 36.0) * PI / 180.0
            val r = if (i % 2 == 0) outer else inner
            val x = cx + (r * cos(angle)).toFloat()
            val y = cy + (r * sin(angle)).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()

        val stroke = Stroke(width = (this.size.minDimension * 0.09f).coerceAtLeast(1.2.dp.toPx()), join = StrokeJoin.Round)
        drawPath(starPath, color = emptyColor, style = stroke)

        if (fillFraction > 0f) {
            val fillWidth = this.size.width * fillFraction.coerceIn(0f, 1f)
            if (!isRtl) {
                clipRect(right = fillWidth) {
                    drawPath(starPath, color = filledColor, style = Fill)
                    drawPath(starPath, color = filledColor, style = stroke)
                }
            } else {
                clipRect(left = this.size.width - fillWidth) {
                    drawPath(starPath, color = filledColor, style = Fill)
                    drawPath(starPath, color = filledColor, style = stroke)
                }
            }
        }
    }
}

/** A compact native-style file selection field. Supply the platform picker through [onBrowse]. */
@Composable
fun KiraFileInput(
    fileNames: List<String>,
    onBrowse: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "Attachments",
    helperText: String? = null,
    size: KiraInputSize = KiraInputSize.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    onClear: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val summary = when {
        fileNames.isEmpty() -> ""
        fileNames.size == 1 -> fileNames.first()
        else -> "${fileNames.size} files selected"
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        KiraInput(
            value = summary,
            onValueChange = {},
            label = label,
            placeholder = "No file chosen",
            helperText = helperText,
            size = size,
            enabled = enabled,
            isError = isError,
            readOnly = true,
            clearable = onClear != null && fileNames.isNotEmpty(),
            onClear = onClear,
            leading = {
                val interactionSource = remember { MutableInteractionSource() }
                val hovered by interactionSource.collectIsHoveredAsState()
                Box(
                    modifier = Modifier
                        .clip(KiraTheme.radius.shapeSm)
                        .background(if (hovered && enabled) colors.muted else Color.Transparent)
                        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                        .hoverable(interactionSource, enabled = enabled)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = enabled,
                            role = Role.Button,
                            onClick = onBrowse,
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    BasicText(
                        text = "Choose files",
                        style = KiraTheme.typography.label.copy(
                            color = colors.foreground,
                            fontSize = size.fontSize.sp,
                        ),
                    )
                }
            },
        )
        if (fileNames.isNotEmpty()) {
            fileNames.forEach { name ->
                BasicText(
                    text = "• $name",
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
        }
    }
}

@Composable
private fun InputInlineIconButton(
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val colors = KiraTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val tint = if (hovered || focused) colors.foreground else colors.mutedForeground

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(KiraTheme.radius.shapeSm)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .hoverable(interactionSource, enabled = enabled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        icon(tint)
    }
}

@Composable
private fun InputSpinner(color: Color, size: Dp) {
    val transition = rememberInfiniteTransition(label = "InputSpinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "InputSpinnerRotation",
    )
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val strokeWidth = (this.size.minDimension * 0.14f).coerceAtLeast(1.5.dp.toPx())
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = this.size.minDimension / 2f - strokeWidth / 2f,
            alpha = 0.25f,
            style = stroke,
        )
        val inset = strokeWidth / 2f
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
            style = stroke,
        )
    }
}

@Composable
private fun InputIconMinus(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.12f).coerceAtLeast(1.5.dp.toPx())
        val inset = this.size.width * 0.22f
        drawLine(color, Offset(inset, center.y), Offset(this.size.width - inset, center.y), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun InputIconPlus(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.12f).coerceAtLeast(1.5.dp.toPx())
        val inset = this.size.width * 0.22f
        drawLine(color, Offset(inset, center.y), Offset(this.size.width - inset, center.y), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(center.x, inset), Offset(center.x, this.size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun InputIconX(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.14f).coerceAtLeast(1.3.dp.toPx())
        val inset = this.size.minDimension * 0.22f
        drawLine(color, Offset(inset, inset), Offset(this.size.width - inset, this.size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(this.size.width - inset, inset), Offset(inset, this.size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun InputIconCheck(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = (this.size.minDimension * 0.15f).coerceAtLeast(1.3.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
            lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
            lineTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.26f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun InputIconCopy(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = (this.size.minDimension * 0.11f).coerceAtLeast(1.25.dp.toPx()), join = StrokeJoin.Round)
        drawRect(color, topLeft = Offset(w * 0.36f, h * 0.36f), size = Size(w * 0.48f, h * 0.48f), style = stroke)
        val back = Path().apply {
            moveTo(w * 0.64f, h * 0.36f)
            lineTo(w * 0.64f, h * 0.18f)
            lineTo(w * 0.18f, h * 0.18f)
            lineTo(w * 0.18f, h * 0.64f)
            lineTo(w * 0.36f, h * 0.64f)
        }
        drawPath(back, color = color, style = stroke)
    }
}

@Composable
private fun InputIconSearch(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (this.size.minDimension * 0.12f).coerceAtLeast(1.4.dp.toPx())
        drawCircle(color, radius = this.size.minDimension * 0.30f, center = Offset(w * 0.44f, h * 0.44f), style = Stroke(width = strokeWidth))
        drawLine(color, Offset(w * 0.66f, h * 0.66f), Offset(w * 0.86f, h * 0.86f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    }
}

@Composable
private fun InputIconEye(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (this.size.minDimension * 0.11f).coerceAtLeast(1.3.dp.toPx())
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val eye = Path().apply {
            moveTo(w * 0.10f, h * 0.50f)
            cubicTo(w * 0.26f, h * 0.22f, w * 0.74f, h * 0.22f, w * 0.90f, h * 0.50f)
            cubicTo(w * 0.74f, h * 0.78f, w * 0.26f, h * 0.78f, w * 0.10f, h * 0.50f)
            close()
        }
        drawPath(eye, color = color, style = stroke)
        drawCircle(color, radius = this.size.minDimension * 0.13f, center = center, style = stroke)
    }
}

@Composable
private fun InputIconEyeOff(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = (this.size.minDimension * 0.11f).coerceAtLeast(1.3.dp.toPx())
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val eye = Path().apply {
            moveTo(w * 0.10f, h * 0.50f)
            cubicTo(w * 0.26f, h * 0.22f, w * 0.74f, h * 0.22f, w * 0.90f, h * 0.50f)
            cubicTo(w * 0.74f, h * 0.78f, w * 0.26f, h * 0.78f, w * 0.10f, h * 0.50f)
            close()
        }
        drawPath(eye, color = color, style = stroke)
        drawLine(color, Offset(w * 0.16f, h * 0.16f), Offset(w * 0.84f, h * 0.84f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    }
}

@Composable
private fun InputIconChevronDown(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = (this.size.minDimension * 0.15f).coerceAtLeast(1.3.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.20f, this@Canvas.size.height * 0.36f)
            lineTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.66f)
            lineTo(this@Canvas.size.width * 0.80f, this@Canvas.size.height * 0.36f)
        }
        drawPath(path, color = color, style = stroke)
    }
}
