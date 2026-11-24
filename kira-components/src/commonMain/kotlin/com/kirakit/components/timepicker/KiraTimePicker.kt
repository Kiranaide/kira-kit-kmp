package com.kirakit.components.timepicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.drawer.KiraDrawer
import com.kirakit.components.drawer.KiraDrawerSide
import com.kirakit.components.drawer.KiraDrawerSize
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography

/**
 * Pure KMP time-of-day value aligned with kira-kit-v2's `TimePicker`.
 */
@Immutable
data class KiraTime(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
) : Comparable<KiraTime> {
    init {
        require(hour in 0..23) { "hour must be in 0..23, got $hour" }
        require(minute in 0..59) { "minute must be in 0..59, got $minute" }
        require(second in 0..59) { "second must be in 0..59, got $second" }
    }

    fun toTotalSeconds(): Int = hour * 3600 + minute * 60 + second

    fun toTotalMinutes(): Int = hour * 60 + minute

    override fun compareTo(other: KiraTime): Int = toTotalSeconds().compareTo(other.toTotalSeconds())
}

enum class KiraTimeGranularity {
    Minute,
    Second,
}

enum class KiraTimePickerVariant {
    Stepper,
    Grid,
    Full,
}

enum class KiraTimeHourCycle {
    Hour24,
    Hour12,
}

enum class KiraTimePickerSize(
    val minHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val fontSize: Int,
    internal val iconSize: Dp,
) {
    Sm(32.dp, 10.dp, 12, 14.dp),
    Default(40.dp, 12.dp, 14, 16.dp),
    Lg(48.dp, 16.dp, 16, 20.dp),
}

enum class KiraTimePickerTriggerVariant {
    Default,
    Outline,
    Ghost,
}

enum class KiraTimeUnit {
    Hour,
    Minute,
    Second,
}

val DefaultKiraTimePresets: List<KiraTime> = listOf(
    KiraTime(9, 0),
    KiraTime(12, 30),
    KiraTime(17, 0),
    KiraTime(21, 0),
)

private fun pad2(value: Int): String = value.toString().padStart(2, '0')

/**
 * Parses `"HH:mm"` or `"HH:mm:ss"` into a [KiraTime], or `null` if invalid.
 */
fun parseKiraTime(text: String): KiraTime? {
    val parts = text.trim().split(":")
    if (parts.size !in 2..3) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val s = if (parts.size == 3) parts[2].toIntOrNull() ?: return null else 0
    if (h !in 0..23 || m !in 0..59 || s !in 0..59) return null
    return KiraTime(h, m, s)
}

/**
 * Formats a [KiraTime] using 24-hour (`HH:mm[:ss]`) or 12-hour (`h:mm[:ss] AM/PM`) display.
 */
fun formatKiraTime(
    time: KiraTime,
    granularity: KiraTimeGranularity = KiraTimeGranularity.Minute,
    hourCycle: KiraTimeHourCycle = KiraTimeHourCycle.Hour24,
): String {
    val secSuffix = if (granularity == KiraTimeGranularity.Second) ":${pad2(time.second)}" else ""
    return when (hourCycle) {
        KiraTimeHourCycle.Hour24 -> "${pad2(time.hour)}:${pad2(time.minute)}$secSuffix"
        KiraTimeHourCycle.Hour12 -> {
            val isAm = time.hour < 12
            val displayHour = when (val mod = time.hour % 12) {
                0 -> 12
                else -> mod
            }
            val period = if (isAm) "AM" else "PM"
            "$displayHour:${pad2(time.minute)}$secSuffix $period"
        }
    }
}

/**
 * Checks whether [candidate] satisfies inclusive [minTime]/[maxTime] bounds and [disabledTimes].
 */
fun isKiraTimeAllowed(
    candidate: KiraTime,
    minTime: KiraTime? = null,
    maxTime: KiraTime? = null,
    disabledTimes: Set<KiraTime> = emptySet(),
    granularity: KiraTimeGranularity = KiraTimeGranularity.Minute,
): Boolean {
    fun total(t: KiraTime): Int = when (granularity) {
        KiraTimeGranularity.Minute -> t.toTotalMinutes()
        KiraTimeGranularity.Second -> t.toTotalSeconds()
    }
    val cand = total(candidate)
    if (minTime != null && cand < total(minTime)) return false
    if (maxTime != null && cand > total(maxTime)) return false
    if (disabledTimes.any { total(it) == cand }) return false
    return true
}

/**
 * Steps a single unit of [current] by [delta] * [step], skipping disallowed slots up to one full cycle.
 */
fun stepKiraTimeUnit(
    current: KiraTime?,
    unit: KiraTimeUnit,
    delta: Int,
    step: Int = 1,
    minTime: KiraTime? = null,
    maxTime: KiraTime? = null,
    disabledTimes: Set<KiraTime> = emptySet(),
    granularity: KiraTimeGranularity = KiraTimeGranularity.Minute,
): KiraTime {
    val safeStep = step.coerceAtLeast(1)
    val base = current ?: minTime ?: KiraTime(0, 0, 0)
    val modulus = when (unit) {
        KiraTimeUnit.Hour -> 24
        KiraTimeUnit.Minute -> 60
        KiraTimeUnit.Second -> 60
    }
    val maxAttempts = (modulus / safeStep).coerceAtLeast(1)
    var candidate = base
    for (attempt in 1..maxAttempts) {
        val offset = delta * safeStep * attempt
        candidate = when (unit) {
            KiraTimeUnit.Hour -> base.copy(hour = ((base.hour + offset) % 24 + 24) % 24)
            KiraTimeUnit.Minute -> base.copy(minute = ((base.minute + offset) % 60 + 60) % 60)
            KiraTimeUnit.Second -> base.copy(second = ((base.second + offset) % 60 + 60) % 60)
        }
        if (isKiraTimeAllowed(candidate, minTime, maxTime, disabledTimes, granularity)) {
            return candidate
        }
    }
    return base
}

/**
 * Accessible Time Picker aligned with kira-kit-v2's `TimePicker`.
 *
 * - Supports `Stepper` (default), `Grid` (quick presets + tappable hour/minute cells), and `Full` panels.
 * - Supports minute or second granularity, 24-hour or 12-hour (AM/PM) clock, steps, inclusive min/max bounds,
 *   and blocked `disabledTimes`.
 * - Renders in an anchored popover by default, or bare inline when `popover = false` (used by `KiraDateTimePicker`).
 */
@Composable
fun KiraTimePicker(
    value: KiraTime?,
    onValueChange: (KiraTime?) -> Unit,
    modifier: Modifier = Modifier,
    variant: KiraTimePickerVariant = KiraTimePickerVariant.Stepper,
    triggerVariant: KiraTimePickerTriggerVariant = KiraTimePickerTriggerVariant.Default,
    size: KiraTimePickerSize = KiraTimePickerSize.Default,
    granularity: KiraTimeGranularity = KiraTimeGranularity.Minute,
    hourCycle: KiraTimeHourCycle = KiraTimeHourCycle.Hour24,
    hourStep: Int = 1,
    minuteStep: Int = 1,
    secondStep: Int = 1,
    minTime: KiraTime? = null,
    maxTime: KiraTime? = null,
    disabledTimes: Set<KiraTime> = emptySet(),
    presets: List<KiraTime> = DefaultKiraTimePresets,
    placeholder: String = "Select time",
    popover: Boolean = true,
    clearable: Boolean = true,
    enabled: Boolean = true,
    required: Boolean = false,
    invalid: Boolean = false,
    ariaLabel: String = "Time",
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()

    if (!popover) {
        Box(
            modifier = modifier
                .clip(radius.shapeXl)
                .background(colors.popover)
                .border(1.dp, colors.border, radius.shapeXl)
                .semantics {
                    contentDescription = ariaLabel
                    if (!enabled) disabled()
                },
        ) {
            KiraTimePickerPanel(
                value = value,
                onValueChange = { next ->
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onValueChange(next)
                },
                variant = variant,
                granularity = granularity,
                hourCycle = hourCycle,
                hourStep = hourStep,
                minuteStep = minuteStep,
                secondStep = secondStep,
                minTime = minTime,
                maxTime = maxTime,
                disabledTimes = disabledTimes,
                presets = presets,
                enabled = enabled,
            )
        }
        return
    }

    var expanded by remember { mutableStateOf(false) }
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = expanded && enabled

    val triggerInteraction = remember { MutableInteractionSource() }
    val hovered by triggerInteraction.collectIsHoveredAsState()
    val focused by triggerInteraction.collectIsFocusedAsState()

    val borderWidth = if (triggerVariant == KiraTimePickerTriggerVariant.Outline) 2.dp else 1.dp
    val borderColor by animateColorAsState(
        targetValue = when {
            invalid -> colors.destructive
            expanded || focused -> colors.ring
            triggerVariant == KiraTimePickerTriggerVariant.Ghost -> Color.Transparent
            triggerVariant == KiraTimePickerTriggerVariant.Outline && hovered -> colors.primary.copy(alpha = 0.55f)
            else -> colors.input
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            triggerVariant == KiraTimePickerTriggerVariant.Outline -> Color.Transparent
            hovered && enabled -> colors.muted.copy(alpha = 0.5f)
            triggerVariant == KiraTimePickerTriggerVariant.Ghost -> Color.Transparent
            else -> colors.background
        },
        animationSpec = KiraMotion.fastTween(),
    )

    val formattedValue = value?.let { formatKiraTime(it, granularity, hourCycle) }
    val density = LocalDensity.current
    val windowWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    val isCompactViewport = windowWidthDp > 0.dp && windowWidthDp < 640.dp
    val popupPositionProvider = remember(density) {
        AnchoredDropdownPositionProvider(
            gapPx = with(density) { 6.dp.roundToPx() },
            marginPx = with(density) { 8.dp.roundToPx() },
        )
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = size.minHeight)
                .alpha(if (enabled) 1f else 0.5f)
                .clip(radius.shapeLg)
                .background(bgColor)
                .border(borderWidth, borderColor, radius.shapeLg)
                .hoverable(triggerInteraction, enabled = enabled)
                .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                .clickable(
                    interactionSource = triggerInteraction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Light)
                        expanded = !expanded
                    },
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = "$ariaLabel: ${formattedValue ?: "not selected"}${if (required) ", required" else ""}"
                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                    if (!enabled) disabled()
                    if (invalid) error("Invalid time")
                }
                .padding(horizontal = size.horizontalPadding, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ClockIcon(
                color = colors.mutedForeground,
                size = size.iconSize,
            )
            BasicText(
                text = formattedValue ?: placeholder,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = KiraTheme.typography.body.copy(
                    fontSize = size.fontSize.sp,
                    color = if (formattedValue != null) colors.foreground else colors.mutedForeground,
                ),
            )
            if (clearable && value != null && enabled) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(radius.shapeFull)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Light)
                                onValueChange(null)
                            },
                        )
                        .semantics { contentDescription = "Clear time" },
                    contentAlignment = Alignment.Center,
                ) {
                    CloseGlyph(color = colors.mutedForeground, size = 12.dp)
                }
            }
        }

        if (isCompactViewport) {
            KiraDrawer(
                open = expanded && enabled,
                onDismissRequest = { expanded = false },
                side = KiraDrawerSide.Bottom,
                size = KiraDrawerSize.Lg,
                swipeToDismiss = true,
                showCloseButton = true,
                paneTitle = ariaLabel,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                        .clearAndSetSemantics {},
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(radius.shapeFull)
                            .background(colors.mutedForeground.copy(alpha = 0.38f)),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 48.dp, top = 4.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    BasicText(
                        text = ariaLabel,
                        modifier = Modifier.semantics { heading() },
                        style = KiraTheme.typography.heading3.copy(color = colors.foreground),
                    )
                    BasicText(
                        text = formattedValue ?: placeholder,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    KiraTimePickerPanel(
                        value = value,
                        onValueChange = { next ->
                            haptics.perform(KiraHapticFeedbackType.Selection)
                            onValueChange(next)
                        },
                        variant = variant,
                        granularity = granularity,
                        hourCycle = hourCycle,
                        hourStep = hourStep,
                        minuteStep = minuteStep,
                        secondStep = secondStep,
                        minTime = minTime,
                        maxTime = maxTime,
                        disabledTimes = disabledTimes,
                        presets = presets,
                        enabled = enabled,
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (clearable && value != null) {
                        KiraButton(
                            text = "Clear",
                            variant = KiraButtonVariant.Ghost,
                            size = KiraButtonSize.Sm,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Light)
                                onValueChange(null)
                            },
                        )
                    }
                    KiraButton(
                        text = "Done",
                        size = KiraButtonSize.Sm,
                        onClick = { expanded = false },
                    )
                }
            }
        } else if (transitionState.currentState || transitionState.targetState) {
            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = fadeIn(tween(180, easing = KiraMotion.EaseOutStrong)) +
                        scaleIn(tween(180, easing = KiraMotion.EaseOutStrong), initialScale = 0.96f),
                    exit = fadeOut(tween(130, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(130, easing = KiraMotion.EaseOutStrong), targetScale = 0.96f),
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(10.dp, radius.shapeXl)
                            .clip(radius.shapeXl)
                            .background(colors.popover)
                            .border(1.dp, colors.border, radius.shapeXl)
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                                    expanded = false
                                    true
                                } else {
                                    false
                                }
                            },
                    ) {
                        KiraTimePickerPanel(
                            value = value,
                            onValueChange = { next ->
                                haptics.perform(KiraHapticFeedbackType.Selection)
                                onValueChange(next)
                            },
                            variant = variant,
                            granularity = granularity,
                            hourCycle = hourCycle,
                            hourStep = hourStep,
                            minuteStep = minuteStep,
                            secondStep = secondStep,
                            minTime = minTime,
                            maxTime = maxTime,
                            disabledTimes = disabledTimes,
                            presets = presets,
                            enabled = enabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun KiraTimePickerPanel(
    value: KiraTime?,
    onValueChange: (KiraTime) -> Unit,
    variant: KiraTimePickerVariant,
    granularity: KiraTimeGranularity,
    hourCycle: KiraTimeHourCycle,
    hourStep: Int,
    minuteStep: Int,
    secondStep: Int,
    minTime: KiraTime?,
    maxTime: KiraTime?,
    disabledTimes: Set<KiraTime>,
    presets: List<KiraTime>,
    enabled: Boolean,
) {
    val colors = KiraTheme.colors
    Column(modifier = Modifier.widthIn(min = 248.dp, max = 336.dp)) {
        if (variant == KiraTimePickerVariant.Stepper || variant == KiraTimePickerVariant.Full) {
            TimeStepperSection(
                value = value,
                onValueChange = onValueChange,
                granularity = granularity,
                hourCycle = hourCycle,
                hourStep = hourStep,
                minuteStep = minuteStep,
                secondStep = secondStep,
                minTime = minTime,
                maxTime = maxTime,
                disabledTimes = disabledTimes,
                enabled = enabled,
            )
        }
        if (variant == KiraTimePickerVariant.Full) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        }
        if (variant == KiraTimePickerVariant.Grid || variant == KiraTimePickerVariant.Full) {
            TimePresetGridSection(
                value = value,
                onValueChange = onValueChange,
                granularity = granularity,
                hourCycle = hourCycle,
                hourStep = hourStep,
                minuteStep = minuteStep,
                minTime = minTime,
                maxTime = maxTime,
                disabledTimes = disabledTimes,
                presets = presets,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun TimeStepperSection(
    value: KiraTime?,
    onValueChange: (KiraTime) -> Unit,
    granularity: KiraTimeGranularity,
    hourCycle: KiraTimeHourCycle,
    hourStep: Int,
    minuteStep: Int,
    secondStep: Int,
    minTime: KiraTime?,
    maxTime: KiraTime?,
    disabledTimes: Set<KiraTime>,
    enabled: Boolean,
) {
    val colors = KiraTheme.colors
    val units = remember(granularity) {
        if (granularity == KiraTimeGranularity.Second) {
            listOf(KiraTimeUnit.Hour, KiraTimeUnit.Minute, KiraTimeUnit.Second)
        } else {
            listOf(KiraTimeUnit.Hour, KiraTimeUnit.Minute)
        }
    }

    fun stepFor(unit: KiraTimeUnit): Int = when (unit) {
        KiraTimeUnit.Hour -> hourStep.coerceAtLeast(1)
        KiraTimeUnit.Minute -> minuteStep.coerceAtLeast(1)
        KiraTimeUnit.Second -> secondStep.coerceAtLeast(1)
    }

    fun bump(unit: KiraTimeUnit, delta: Int) {
        if (!enabled) return
        val next = stepKiraTimeUnit(
            current = value,
            unit = unit,
            delta = delta,
            step = stepFor(unit),
            minTime = minTime,
            maxTime = maxTime,
            disabledTimes = disabledTimes,
            granularity = granularity,
        )
        onValueChange(next)
    }

    fun commitRaw(unit: KiraTimeUnit, raw: String) {
        if (!enabled) return
        val parsed = raw.trim().toIntOrNull() ?: return
        val base = value ?: minTime ?: KiraTime(0, 0, 0)
        val candidate = when (unit) {
            KiraTimeUnit.Hour -> {
                val h = if (hourCycle == KiraTimeHourCycle.Hour12) {
                    val clamped12 = parsed.coerceIn(1, 12)
                    val isPm = base.hour >= 12
                    when {
                        clamped12 == 12 && !isPm -> 0
                        clamped12 == 12 && isPm -> 12
                        isPm -> clamped12 + 12
                        else -> clamped12
                    }
                } else {
                    parsed.coerceIn(0, 23)
                }
                base.copy(hour = h)
            }
            KiraTimeUnit.Minute -> base.copy(minute = parsed.coerceIn(0, 59))
            KiraTimeUnit.Second -> base.copy(second = parsed.coerceIn(0, 59))
        }
        if (isKiraTimeAllowed(candidate, minTime, maxTime, disabledTimes, granularity)) {
            onValueChange(candidate)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        units.forEachIndexed { idx, unit ->
            if (idx > 0) {
                BasicText(
                    text = ":",
                    modifier = Modifier.padding(top = 18.dp).clearAndSetSemantics {},
                    style = KiraTheme.typography.heading2.copy(
                        color = colors.mutedForeground,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            val displayValue = when {
                value == null -> "--"
                unit == KiraTimeUnit.Hour && hourCycle == KiraTimeHourCycle.Hour12 -> {
                    val h12 = value.hour % 12
                    pad2(if (h12 == 0) 12 else h12)
                }
                unit == KiraTimeUnit.Hour -> pad2(value.hour)
                unit == KiraTimeUnit.Minute -> pad2(value.minute)
                else -> pad2(value.second)
            }
            TimeStepperColumn(
                label = when (unit) {
                    KiraTimeUnit.Hour -> "HOUR"
                    KiraTimeUnit.Minute -> "MIN"
                    KiraTimeUnit.Second -> "SEC"
                },
                unitName = unit.name.lowercase(),
                displayText = displayValue,
                enabled = enabled,
                onIncrease = { bump(unit, +1) },
                onDecrease = { bump(unit, -1) },
                onCommit = { raw -> commitRaw(unit, raw) },
            )
        }

        if (hourCycle == KiraTimeHourCycle.Hour12) {
            val isAm = (value?.hour ?: 0) < 12
            Column(
                modifier = Modifier.padding(top = 18.dp, start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                listOf("AM" to true, "PM" to false).forEach { (period, targetAm) ->
                    val active = value != null && isAm == targetAm
                    MeridiemButton(
                        label = period,
                        active = active,
                        enabled = enabled,
                        onClick = {
                            val base = value ?: minTime ?: KiraTime(9, 0, 0)
                            val newHour = (base.hour % 12) + if (targetAm) 0 else 12
                            val cand = base.copy(hour = newHour)
                            if (isKiraTimeAllowed(cand, minTime, maxTime, disabledTimes, granularity)) {
                                onValueChange(cand)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeStepperColumn(
    label: String,
    unitName: String,
    displayText: String,
    enabled: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onCommit: (String) -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    var draft by remember(displayText) { mutableStateOf(displayText) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(displayText, isEditing) {
        if (!isEditing) draft = displayText
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BasicText(
            text = label,
            style = KiraTheme.typography.caption.copy(
                fontSize = 11.sp,
                color = colors.mutedForeground,
                letterSpacing = 0.6.sp,
            ),
        )
        StepperActionButton(
            isPlus = true,
            enabled = enabled,
            contentDesc = "Increase $unitName",
            onClick = onIncrease,
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(radius.shapeXl)
                .background(colors.background)
                .border(
                    width = 1.dp,
                    color = if (isEditing) colors.primary else colors.border,
                    shape = radius.shapeXl,
                ),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = draft,
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }.take(2)
                    draft = filtered
                },
                enabled = enabled,
                singleLine = true,
                textStyle = KiraTheme.typography.heading2.copy(
                    color = colors.foreground,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onCommit(draft)
                        isEditing = false
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            isEditing = true
                            if (draft == "--") draft = ""
                        } else if (isEditing) {
                            isEditing = false
                            onCommit(draft)
                            draft = displayText
                        }
                    }
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.DirectionUp -> {
                                    onIncrease()
                                    true
                                }
                                Key.DirectionDown -> {
                                    onDecrease()
                                    true
                                }
                                Key.Enter -> {
                                    onCommit(draft)
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
            )
        }
        StepperActionButton(
            isPlus = false,
            enabled = enabled,
            contentDesc = "Decrease $unitName",
            onClick = onDecrease,
        )
    }
}

@Composable
private fun StepperActionButton(
    isPlus: Boolean,
    enabled: Boolean,
    contentDesc: String,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered && enabled) colors.muted else colors.muted.copy(alpha = 0.45f),
        animationSpec = KiraMotion.fastTween(),
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(radius.shapeXl)
            .background(bg)
            .border(1.dp, colors.border, radius.shapeXl)
            .hoverable(interaction, enabled = enabled)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                contentDescription = contentDesc
                if (!enabled) disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        PlusMinusGlyph(isPlus = isPlus, color = colors.foreground, size = 16.dp)
    }
}

@Composable
private fun MeridiemButton(
    label: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = when {
            active -> colors.primary.copy(alpha = 0.14f)
            hovered && enabled -> colors.muted
            else -> colors.background
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val border by animateColorAsState(
        targetValue = if (active) colors.primary.copy(alpha = 0.55f) else colors.border,
        animationSpec = KiraMotion.fastTween(),
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(radius.shapeXl)
            .background(bg)
            .border(1.dp, border, radius.shapeXl)
            .hoverable(interaction, enabled = enabled)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                selected = active
                if (!enabled) disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = KiraTheme.typography.caption.copy(
                color = if (active) colors.primary else colors.foreground,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimePresetGridSection(
    value: KiraTime?,
    onValueChange: (KiraTime) -> Unit,
    granularity: KiraTimeGranularity,
    hourCycle: KiraTimeHourCycle,
    hourStep: Int,
    minuteStep: Int,
    minTime: KiraTime?,
    maxTime: KiraTime?,
    disabledTimes: Set<KiraTime>,
    presets: List<KiraTime>,
    enabled: Boolean,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val currentHour = value?.hour ?: -1
    val currentMinute = value?.minute ?: -1
    val safeHourStep = hourStep.coerceAtLeast(1)
    val safeMinStep = if (minuteStep <= 1) 5 else minuteStep

    val hours = remember(hourCycle, safeHourStep, value?.hour) {
        if (hourCycle == KiraTimeHourCycle.Hour12) {
            val isPm = (value?.hour ?: 0) >= 12
            val baseOffset = if (isPm) 12 else 0
            (0 until 12 step safeHourStep).map { baseOffset + it }
        } else {
            (0 until 24 step safeHourStep).toList()
        }
    }
    val minutes = remember(safeMinStep) {
        (0 until 60 step safeMinStep).toList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (presets.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                presets.forEach { preset ->
                    val allowed = isKiraTimeAllowed(preset, minTime, maxTime, disabledTimes, granularity)
                    val active = value != null && value.hour == preset.hour && value.minute == preset.minute
                    val itemEnabled = enabled && allowed
                    val label = formatKiraTime(preset, KiraTimeGranularity.Minute, hourCycle)
                    Box(
                        modifier = Modifier
                            .alpha(if (itemEnabled) 1f else 0.4f)
                            .clip(radius.shapeFull)
                            .background(if (active) colors.primary.copy(alpha = 0.14f) else colors.background)
                            .border(
                                1.dp,
                                if (active) colors.primary.copy(alpha = 0.5f) else colors.border,
                                radius.shapeFull,
                            )
                            .pointerHoverIcon(if (itemEnabled) PointerIcon.Hand else PointerIcon.Default)
                            .clickable(
                                enabled = itemEnabled,
                                role = Role.Button,
                                onClick = { onValueChange(preset) },
                            )
                            .semantics {
                                selected = active
                                if (!itemEnabled) disabled()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = label,
                            style = KiraTheme.typography.caption.copy(
                                color = if (active) colors.primary else colors.foreground,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "HOUR",
                    style = KiraTheme.typography.caption.copy(
                        fontSize = 11.sp,
                        color = colors.mutedForeground,
                        letterSpacing = 0.6.sp,
                    ),
                )
                if (hourCycle == KiraTimeHourCycle.Hour12) {
                    val isAm = (value?.hour ?: 0) < 12
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("AM" to true, "PM" to false).forEach { (period, targetAm) ->
                            val active = isAm == targetAm
                            Box(
                                modifier = Modifier
                                    .clip(radius.shapeMd)
                                    .background(if (active) colors.primary.copy(alpha = 0.14f) else colors.muted.copy(alpha = 0.4f))
                                    .clickable(enabled = enabled, role = Role.Button) {
                                        val base = value ?: minTime ?: KiraTime(9, 0, 0)
                                        val nextH = (base.hour % 12) + if (targetAm) 0 else 12
                                        val cand = base.copy(hour = nextH)
                                        if (isKiraTimeAllowed(cand, minTime, maxTime, disabledTimes, granularity)) {
                                            onValueChange(cand)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                BasicText(
                                    text = period,
                                    style = KiraTheme.typography.caption.copy(
                                        color = if (active) colors.primary else colors.mutedForeground,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
            TimeCellGrid(
                items = hours,
                columns = 6,
                labelOf = { h ->
                    if (hourCycle == KiraTimeHourCycle.Hour12) {
                        val mod = h % 12
                        pad2(if (mod == 0) 12 else mod)
                    } else {
                        pad2(h)
                    }
                },
                isSelected = { h -> h == currentHour },
                isEnabled = { h ->
                    val minuteProbe = if (currentMinute >= 0) currentMinute else 0
                    enabled && isKiraTimeAllowed(
                        KiraTime(h, minuteProbe, value?.second ?: 0),
                        minTime,
                        maxTime,
                        disabledTimes,
                        granularity,
                    )
                },
                onSelect = { h ->
                    val nextMin = if (currentMinute >= 0) currentMinute else 0
                    onValueChange(KiraTime(h, nextMin, value?.second ?: 0))
                },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BasicText(
                text = "MINUTE",
                style = KiraTheme.typography.caption.copy(
                    fontSize = 11.sp,
                    color = colors.mutedForeground,
                    letterSpacing = 0.6.sp,
                ),
            )
            TimeCellGrid(
                items = minutes,
                columns = 6,
                labelOf = { m -> pad2(m) },
                isSelected = { m -> m == currentMinute },
                isEnabled = { m ->
                    val hourProbe = if (currentHour >= 0) currentHour else (minTime?.hour ?: 0)
                    enabled && isKiraTimeAllowed(
                        KiraTime(hourProbe, m, value?.second ?: 0),
                        minTime,
                        maxTime,
                        disabledTimes,
                        granularity,
                    )
                },
                onSelect = { m ->
                    val nextHour = if (currentHour >= 0) currentHour else (minTime?.hour ?: 0)
                    onValueChange(KiraTime(nextHour, m, value?.second ?: 0))
                },
            )
        }
    }
}

@Composable
private fun TimeCellGrid(
    items: List<Int>,
    columns: Int,
    labelOf: (Int) -> String,
    isSelected: (Int) -> Boolean,
    isEnabled: (Int) -> Boolean,
    onSelect: (Int) -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val rows = remember(items, columns) { items.chunked(columns) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rowItems.forEach { item ->
                    val selected = isSelected(item)
                    val itemEnabled = isEnabled(item)
                    val interaction = remember { MutableInteractionSource() }
                    val hovered by interaction.collectIsHoveredAsState()
                    val bg = when {
                        selected -> colors.primary.copy(alpha = 0.14f)
                        hovered && itemEnabled -> colors.muted
                        else -> Color.Transparent
                    }
                    val border = when {
                        selected -> colors.primary.copy(alpha = 0.45f)
                        hovered && itemEnabled -> colors.border
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 36.dp)
                            .alpha(if (itemEnabled) 1f else 0.35f)
                            .clip(radius.shapeMd)
                            .background(bg)
                            .border(1.dp, border, radius.shapeMd)
                            .hoverable(interaction, enabled = itemEnabled)
                            .pointerHoverIcon(if (itemEnabled) PointerIcon.Hand else PointerIcon.Default)
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                                enabled = itemEnabled,
                                role = Role.Button,
                                onClick = { onSelect(item) },
                            )
                            .semantics {
                                this.selected = selected
                                if (!itemEnabled) disabled()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = labelOf(item),
                            style = KiraTheme.typography.caption.copy(
                                color = if (selected) colors.primary else colors.foreground,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            ),
                        )
                    }
                }
                repeat(columns - rowItems.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}

private class AnchoredDropdownPositionProvider(
    private val gapPx: Int,
    private val marginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            anchorBounds.left
        } else {
            anchorBounds.right - popupContentSize.width
        }.coerceIn(marginPx, (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx))

        val belowY = anchorBounds.bottom + gapPx
        val aboveY = anchorBounds.top - popupContentSize.height - gapPx
        val y = when {
            belowY + popupContentSize.height + marginPx <= windowSize.height -> belowY
            aboveY >= marginPx -> aboveY
            else -> belowY.coerceIn(marginPx, (windowSize.height - popupContentSize.height - marginPx).coerceAtLeast(marginPx))
        }
        return IntOffset(x, y)
    }
}

@Composable
private fun ClockIcon(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.11f).coerceAtLeast(1.3.dp.toPx())
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCircle(
            color = color,
            radius = this.size.minDimension * 0.42f,
            center = center,
            style = Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, this.size.height * 0.28f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(this.size.width * 0.68f, this.size.height * 0.58f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun PlusMinusGlyph(isPlus: Boolean, color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.12f).coerceAtLeast(1.5.dp.toPx())
        val pad = this.size.minDimension * 0.22f
        val midY = this.size.height / 2f
        drawLine(color, Offset(pad, midY), Offset(this.size.width - pad, midY), strokeWidth = stroke, cap = StrokeCap.Round)
        if (isPlus) {
            val midX = this.size.width / 2f
            drawLine(color, Offset(midX, pad), Offset(midX, this.size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun CloseGlyph(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.15f).coerceAtLeast(1.3.dp.toPx())
        val pad = this.size.minDimension * 0.18f
        drawLine(color, Offset(pad, pad), Offset(this.size.width - pad, this.size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(this.size.width - pad, pad), Offset(pad, this.size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
