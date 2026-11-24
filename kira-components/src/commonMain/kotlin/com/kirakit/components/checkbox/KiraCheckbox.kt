package com.kirakit.components.checkbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.launch

enum class KiraCheckboxState {
    Unchecked,
    Checked,
    Indeterminate;

    fun toToggleableState(): ToggleableState = when (this) {
        Unchecked -> ToggleableState.Off
        Checked -> ToggleableState.On
        Indeterminate -> ToggleableState.Indeterminate
    }
}

enum class KiraCheckboxSize(
    val boxSize: Dp,
    val iconSize: Dp,
    val cardPadding: PaddingValues,
) {
    Sm(16.dp, 12.dp, PaddingValues(12.dp)),
    Default(20.dp, 14.dp, PaddingValues(16.dp)),
    Lg(24.dp, 16.dp, PaddingValues(20.dp)),
}

enum class KiraCheckboxVariant {
    Default,
    Outline,
    Filled,
    Soft,
}

enum class KiraCheckboxColor {
    Default,
    Success,
    Destructive,
    Warning,
}

enum class KiraCheckboxLabelPlacement {
    End,
    Start,
}

enum class KiraCheckboxGroupOrientation {
    Vertical,
    Horizontal,
}

@Immutable
data class KiraCheckboxOption(
    val value: String,
    val label: String,
    val description: String? = null,
    val disabled: Boolean = false,
)

fun resolveSelectAllState(selectedCount: Int, totalCount: Int): KiraCheckboxState = when {
    totalCount <= 0 || selectedCount <= 0 -> KiraCheckboxState.Unchecked
    selectedCount >= totalCount -> KiraCheckboxState.Checked
    else -> KiraCheckboxState.Indeterminate
}

@Composable
fun KiraCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: KiraCheckboxSize = KiraCheckboxSize.Default,
    variant: KiraCheckboxVariant = KiraCheckboxVariant.Default,
    color: KiraCheckboxColor = KiraCheckboxColor.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    loading: Boolean = false,
    error: Boolean = false,
    disabledReason: String? = null,
    customIcon: (@Composable () -> Unit)? = null,
) {
    KiraCheckbox(
        state = if (checked) KiraCheckboxState.Checked else KiraCheckboxState.Unchecked,
        onClick = onCheckedChange?.let { cb -> { cb(!checked) } },
        modifier = modifier,
        size = size,
        variant = variant,
        color = color,
        enabled = enabled,
        readOnly = readOnly,
        loading = loading,
        error = error,
        disabledReason = disabledReason,
        customIcon = customIcon,
    )
}

/**
 * Tri-state accessible checkbox aligned with kira-kit-v2's `Checkbox`.
 *
 * Expands its touch target to at least `44.dp × 44.dp` (WCAG 2.5.8) while keeping the visual
 * indicator at `16/20/24.dp`, animates a crisp pop + ripple on toggle (honoring reduced motion),
 * triggers tactile selection haptics on mobile, and explains disabled states via `disabledReason`.
 */
@Composable
fun KiraCheckbox(
    state: KiraCheckboxState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    size: KiraCheckboxSize = KiraCheckboxSize.Default,
    variant: KiraCheckboxVariant = KiraCheckboxVariant.Default,
    color: KiraCheckboxColor = KiraCheckboxColor.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    loading: Boolean = false,
    error: Boolean = false,
    disabledReason: String? = null,
    customIcon: (@Composable () -> Unit)? = null,
    expandTouchTarget: Boolean = true,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val isMarked = state != KiraCheckboxState.Unchecked
    val interactive = enabled && !readOnly && !loading && onClick != null
    val shape: Shape = if (size == KiraCheckboxSize.Lg) KiraTheme.radius.shapeMd else KiraTheme.radius.shapeSm

    val accentColor = when {
        error -> colors.destructive
        color == KiraCheckboxColor.Success -> colors.success
        color == KiraCheckboxColor.Destructive -> colors.destructive
        color == KiraCheckboxColor.Warning -> colors.warning
        else -> colors.primary
    }
    val accentForeground = when {
        error -> colors.destructiveForeground
        color == KiraCheckboxColor.Success -> colors.successForeground
        color == KiraCheckboxColor.Destructive -> colors.destructiveForeground
        color == KiraCheckboxColor.Warning -> colors.warningForeground
        else -> colors.primaryForeground
    }

    val targetContainer = when {
        isMarked && variant == KiraCheckboxVariant.Outline && !error && color == KiraCheckboxColor.Default ->
            colors.primary.copy(alpha = if (hovered) 0.15f else 0.10f)
        isMarked -> accentColor.copy(alpha = if (hovered && interactive) 0.90f else 1f)
        variant == KiraCheckboxVariant.Filled -> colors.muted.copy(alpha = if (hovered && interactive) 0.80f else 1f)
        variant == KiraCheckboxVariant.Soft -> colors.primary.copy(alpha = if (hovered && interactive) 0.30f else 0.20f)
        variant == KiraCheckboxVariant.Outline -> if (hovered && interactive) colors.muted.copy(alpha = 0.50f) else Color.Transparent
        else -> Color.Transparent
    }

    val targetBorder = when {
        error -> colors.destructive
        isMarked -> accentColor
        variant == KiraCheckboxVariant.Outline -> colors.input
        variant == KiraCheckboxVariant.Default -> colors.primary.copy(alpha = if (hovered && interactive) 0.80f else 1f)
        else -> Color.Transparent
    }

    val targetIndicator = when {
        isMarked && variant == KiraCheckboxVariant.Outline && !error && color == KiraCheckboxColor.Default -> colors.primary
        else -> accentForeground
    }

    val containerColorState = animateColorAsState(
        targetValue = targetContainer,
        animationSpec = KiraMotion.fastTween(),
        label = "CheckboxBg",
    )
    val borderColor by animateColorAsState(
        targetValue = targetBorder,
        animationSpec = KiraMotion.fastTween(),
        label = "CheckboxBorder",
    )
    val indicatorScaleState = animateFloatAsState(
        targetValue = if (isMarked && !loading) 1f else 0f,
        animationSpec = KiraMotion.bouncySpring(),
        label = "CheckboxIndicatorScale",
    )

    val boxPop = remember { Animatable(1f) }
    val rippleProgress = remember { Animatable(1f) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (!initialized) {
            initialized = true
            return@LaunchedEffect
        }
        if (coroutineContext[MotionDurationScale]?.scaleFactor == 0f) return@LaunchedEffect
        launch {
            boxPop.snapTo(0.86f)
            boxPop.animateTo(1f, KiraMotion.bouncySpring())
        }
        launch {
            rippleProgress.snapTo(0f)
            rippleProgress.animateTo(1f, tween(340, easing = KiraMotion.EaseOutStrong))
        }
    }

    fun triggerToggle() {
        haptics.perform(KiraHapticFeedbackType.Selection)
        onClick?.invoke()
    }

    val borderWidth = if (variant == KiraCheckboxVariant.Outline) 2.dp else 1.dp
    val showRipple by remember { derivedStateOf { rippleProgress.value < 1f } }
    val showIndicator by remember { derivedStateOf { indicatorScaleState.value > 0.01f } }

    val checkboxBox: @Composable () -> Unit = {
        Box(
            modifier = modifier
                .then(if (expandTouchTarget) Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp) else Modifier)
                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .hoverable(interactionSource, enabled = enabled)
                .then(
                    if (onClick != null) {
                        Modifier.triStateToggleable(
                            state = state.toToggleableState(),
                            enabled = interactive,
                            role = Role.Checkbox,
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = ::triggerToggle,
                        )
                    } else {
                        Modifier.focusable(enabled = enabled, interactionSource = interactionSource)
                    },
                )
                .semantics {
                    if (!enabled) disabled()
                    if (error) error("Invalid selection")
                    if (loading) stateDescription = "Loading"
                    else if (!enabled && !disabledReason.isNullOrBlank()) stateDescription = disabledReason
                },
            contentAlignment = Alignment.Center,
        ) {
            // Expanding toggle ripple (drawn via graphicsLayer with 0 per-frame recompositions)
            if (showRipple) {
                Box(
                    modifier = Modifier
                        .size(size.boxSize)
                        .graphicsLayer {
                            val p = rippleProgress.value
                            val s = 0.5f + p * 1.4f
                            scaleX = s
                            scaleY = s
                            alpha = 0.35f * (1f - p)
                        }
                        .background(accentColor, shape),
                )
            }

            // Focus / error outer ring
            val ringColor = when {
                focused -> colors.ring
                error -> colors.destructive.copy(alpha = 0.25f)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val p = boxPop.value
                        scaleX = p
                        scaleY = p
                        if (!enabled) alpha = 0.5f
                    }
                    .then(
                        if (ringColor != Color.Transparent) {
                            Modifier
                                .border(3.dp, ringColor, shape)
                                .padding(1.dp)
                        } else {
                            Modifier
                        },
                    )
                    .size(size.boxSize)
                    .clip(shape)
                    .drawBehind {
                        val c = containerColorState.value
                        if (c.alpha > 0f) drawRect(c)
                    }
                    .then(
                        if (borderColor != Color.Transparent) {
                            Modifier.border(borderWidth, borderColor, shape)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (loading) {
                    CheckboxSpinner(
                        color = if (isMarked) targetIndicator else colors.mutedForeground,
                        size = size.iconSize,
                    )
                } else if (showIndicator) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            val s = indicatorScaleState.value
                            scaleX = s
                            scaleY = s
                            alpha = s
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (customIcon != null) {
                            customIcon()
                        } else {
                            CheckboxGlyph(
                                indeterminate = state == KiraCheckboxState.Indeterminate,
                                color = targetIndicator,
                                size = size.iconSize,
                            )
                        }
                    }
                }
            }
        }
    }

    if (!enabled && !disabledReason.isNullOrBlank()) {
        KiraSimpleTooltip(text = disabledReason) {
            checkboxBox()
        }
    } else {
        checkboxBox()
    }
}

@Composable
fun KiraCheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    labelPlacement: KiraCheckboxLabelPlacement = KiraCheckboxLabelPlacement.End,
    size: KiraCheckboxSize = KiraCheckboxSize.Default,
    variant: KiraCheckboxVariant = KiraCheckboxVariant.Default,
    color: KiraCheckboxColor = KiraCheckboxColor.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    loading: Boolean = false,
    error: Boolean = false,
    disabledReason: String? = null,
    customIcon: (@Composable () -> Unit)? = null,
) {
    KiraCheckboxWithLabel(
        state = if (checked) KiraCheckboxState.Checked else KiraCheckboxState.Unchecked,
        onClick = { onCheckedChange(!checked) },
        label = label,
        modifier = modifier,
        description = description,
        labelPlacement = labelPlacement,
        size = size,
        variant = variant,
        color = color,
        enabled = enabled,
        readOnly = readOnly,
        loading = loading,
        error = error,
        disabledReason = disabledReason,
        customIcon = customIcon,
    )
}

@Composable
fun KiraCheckboxWithLabel(
    state: KiraCheckboxState,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    labelPlacement: KiraCheckboxLabelPlacement = KiraCheckboxLabelPlacement.End,
    size: KiraCheckboxSize = KiraCheckboxSize.Default,
    variant: KiraCheckboxVariant = KiraCheckboxVariant.Default,
    color: KiraCheckboxColor = KiraCheckboxColor.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    loading: Boolean = false,
    error: Boolean = false,
    disabledReason: String? = null,
    customIcon: (@Composable () -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val interactive = enabled && !readOnly && !loading
    val inactive = !enabled || readOnly

    val textBlock: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(top = 1.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BasicText(
                text = label,
                style = KiraTheme.typography.label.copy(
                    color = if (error) colors.destructive else colors.foreground,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.alpha(if (inactive) 0.7f else 1f),
            )
            if (description != null) {
                BasicText(
                    text = description,
                    style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                    modifier = Modifier.alpha(if (inactive) 0.7f else 1f),
                )
            }
        }
    }

    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = modifier
                .heightIn(min = 44.dp)
                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .triStateToggleable(
                    state = state.toToggleableState(),
                    enabled = interactive,
                    role = Role.Checkbox,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Selection)
                        onClick()
                    },
                )
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = if (description == null) Alignment.CenterVertically else Alignment.Top,
        ) {
            if (labelPlacement == KiraCheckboxLabelPlacement.Start) {
                Box(Modifier.weight(1f, fill = false)) { textBlock() }
            }
            KiraCheckbox(
                state = state,
                onClick = null,
                size = size,
                variant = variant,
                color = color,
                enabled = enabled,
                readOnly = readOnly,
                loading = loading,
                error = error,
                customIcon = customIcon,
                expandTouchTarget = false,
            )
            if (labelPlacement == KiraCheckboxLabelPlacement.End) {
                Box(Modifier.weight(1f, fill = false)) { textBlock() }
            }
        }
    }

    if (!enabled && !disabledReason.isNullOrBlank()) {
        KiraSimpleTooltip(text = disabledReason) { rowContent() }
    } else {
        rowContent()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraCheckboxGroup(
    options: List<KiraCheckboxOption>,
    selectedValues: Set<String>,
    onSelectedValuesChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    showSelectAll: Boolean = false,
    selectAllLabel: String = "Select all",
    showProgress: Boolean = false,
    orientation: KiraCheckboxGroupOrientation = KiraCheckboxGroupOrientation.Vertical,
    size: KiraCheckboxSize = KiraCheckboxSize.Default,
    variant: KiraCheckboxVariant = KiraCheckboxVariant.Default,
    color: KiraCheckboxColor = KiraCheckboxColor.Default,
    enabled: Boolean = true,
    error: Boolean = false,
) {
    val colors = KiraTheme.colors
    val enabledOptions = remember(options, enabled) {
        if (!enabled) emptyList() else options.filter { !it.disabled }
    }
    val allOptionValues = remember(options) { options.map { it.value }.toSet() }
    val selectedCount = selectedValues.count { it in allOptionValues }
    val enabledSelectedCount = enabledOptions.count { it.value in selectedValues }
    val selectAllState = resolveSelectAllState(enabledSelectedCount, enabledOptions.size)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (label != null || description != null) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (label != null) {
                    BasicText(
                        text = label,
                        style = KiraTheme.typography.label.copy(
                            color = if (error) colors.destructive else colors.foreground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                if (description != null) {
                    BasicText(
                        text = description,
                        style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                    )
                }
            }
        }

        if (showProgress) {
            BasicText(
                text = "$selectedCount/${options.size} selected",
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = KiraTheme.typography.caption.copy(
                    color = colors.mutedForeground,
                    fontFeatureSettings = "tnum",
                ),
            )
        }

        if (showSelectAll) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraCheckboxWithLabel(
                    state = selectAllState,
                    onClick = {
                        val enabledSet = enabledOptions.map { it.value }.toSet()
                        val next = if (selectAllState == KiraCheckboxState.Checked) {
                            selectedValues - enabledSet
                        } else {
                            selectedValues + enabledSet
                        }
                        onSelectedValuesChange(next)
                    },
                    label = selectAllLabel,
                    size = size,
                    variant = variant,
                    color = color,
                    enabled = enabled && enabledOptions.isNotEmpty(),
                    error = error,
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            }
        }

        val itemRenderer: @Composable (KiraCheckboxOption) -> Unit = { option ->
            val checked = option.value in selectedValues
            KiraCheckboxWithLabel(
                checked = checked,
                onCheckedChange = { isNowChecked ->
                    val next = if (isNowChecked) selectedValues + option.value else selectedValues - option.value
                    onSelectedValuesChange(next)
                },
                label = option.label,
                description = option.description,
                size = size,
                variant = variant,
                color = color,
                enabled = enabled && !option.disabled,
                error = error,
            )
        }

        if (orientation == KiraCheckboxGroupOrientation.Vertical) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                options.forEach { itemRenderer(it) }
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { itemRenderer(it) }
            }
        }
    }
}

@Composable
fun KiraCheckboxCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    size: KiraCheckboxSize = KiraCheckboxSize.Default,
    variant: KiraCheckboxVariant = KiraCheckboxVariant.Outline,
    color: KiraCheckboxColor = KiraCheckboxColor.Default,
    showIndicator: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: Boolean = false,
    disabledReason: String? = null,
    onCommit: (suspend (Boolean) -> Result<Unit>)? = null,
    onCommitError: ((Throwable) -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    var pendingChecked by remember { mutableStateOf<Boolean?>(null) }
    val shownChecked = pendingChecked ?: checked
    val isPending = pendingChecked != null
    val interactive = enabled && !readOnly && !isPending
    val shape = KiraTheme.radius.shapeLg

    val borderColor by animateColorAsState(
        targetValue = when {
            error -> colors.destructive
            focused -> colors.ring
            shownChecked -> colors.primary
            else -> colors.input
        },
        animationSpec = KiraMotion.fastTween(),
        label = "CheckboxCardBorder",
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            shownChecked -> colors.primary.copy(alpha = 0.06f)
            hovered && interactive -> colors.muted.copy(alpha = 0.50f)
            else -> Color.Transparent
        },
        animationSpec = KiraMotion.fastTween(),
        label = "CheckboxCardBg",
    )

    fun handleToggle() {
        val next = !shownChecked
        haptics.perform(KiraHapticFeedbackType.Selection)
        if (onCommit == null) {
            onCheckedChange(next)
            return
        }
        pendingChecked = next
        scope.launch {
            val result = runCatching { onCommit(next).getOrThrow() }
            pendingChecked = null
            result.onSuccess { onCheckedChange(next) }
                .onFailure { onCommitError?.invoke(it) }
        }
    }

    val card: @Composable () -> Unit = {
        Row(
            modifier = modifier
                .alpha(if (!enabled) 0.5f else 1f)
                .clip(shape)
                .background(bgColor, shape)
                .border(2.dp, borderColor, shape)
                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .hoverable(interactionSource, enabled = enabled)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = interactive,
                    role = Role.Checkbox,
                    onClick = ::handleToggle,
                )
                .padding(size.cardPadding)
                .semantics {
                    if (!enabled) disabled()
                    if (isPending) stateDescription = "Saving"
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (showIndicator) {
                KiraCheckbox(
                    checked = shownChecked,
                    onCheckedChange = null,
                    size = size,
                    variant = variant,
                    color = color,
                    enabled = enabled,
                    readOnly = readOnly,
                    loading = isPending,
                    error = error,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            if (icon != null) {
                Box(
                    modifier = Modifier.padding(top = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    icon()
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
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
                        style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                    )
                }
            }
        }
    }

    if (!enabled && !disabledReason.isNullOrBlank()) {
        KiraSimpleTooltip(text = disabledReason) { card() }
    } else {
        card()
    }
}

@Composable
private fun CheckboxGlyph(
    indeterminate: Boolean,
    color: Color,
    size: Dp,
) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(
            width = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        if (indeterminate) {
            drawLine(
                color = color,
                start = Offset(this.size.width * 0.18f, this.size.height * 0.5f),
                end = Offset(this.size.width * 0.82f, this.size.height * 0.5f),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round,
            )
        } else {
            val path = Path().apply {
                moveTo(this@Canvas.size.width * 0.16f, this@Canvas.size.height * 0.52f)
                lineTo(this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.76f)
                lineTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.26f)
            }
            drawPath(path = path, color = color, style = stroke)
        }
    }
}

@Composable
private fun CheckboxSpinner(color: Color, size: Dp) {
    val transition = rememberInfiniteTransition(label = "CheckboxSpinner")
    val rotationState = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "CheckboxSpinnerRotation",
    )
    Box(
        Modifier
            .size(size)
            .clearAndSetSemantics {}
            .drawWithCache {
                val strokeWidth = 2.dp.toPx()
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                val inset = strokeWidth / 2f
                val radius = this.size.minDimension / 2f - inset
                val topLeft = Offset(inset, inset)
                val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
                val trackColor = color.copy(alpha = 0.25f)
                onDrawBehind {
                    drawCircle(color = trackColor, radius = radius, style = stroke)
                    drawArc(
                        color = color,
                        startAngle = rotationState.value - 90f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke,
                    )
                }
            },
    )
}
