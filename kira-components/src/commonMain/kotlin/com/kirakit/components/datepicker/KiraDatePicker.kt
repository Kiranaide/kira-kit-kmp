package com.kirakit.components.datepicker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
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
import androidx.compose.ui.text.style.TextDecoration
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
import com.kirakit.components.timepicker.KiraTime
import com.kirakit.components.timepicker.KiraTimeGranularity
import com.kirakit.components.timepicker.KiraTimeHourCycle
import com.kirakit.components.timepicker.KiraTimePickerPanel
import com.kirakit.components.timepicker.KiraTimePickerVariant
import com.kirakit.components.timepicker.formatKiraTime
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography

/**
 * Pure KMP Gregorian calendar date (`year`, `month` 1..12, `day` 1..31).
 */
@Immutable
data class KiraDate(
    val year: Int,
    val month: Int,
    val day: Int,
) : Comparable<KiraDate> {
    init {
        require(month in 1..12) { "month must be in 1..12, got $month" }
        val maxDay = daysInMonth(year, month)
        require(day in 1..maxDay) { "day must be in 1..$maxDay for $year-$month, got $day" }
    }

    override fun compareTo(other: KiraDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    fun toIsoString(): String =
        "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

    fun startOfMonth(): KiraDate = KiraDate(year, month, 1)

    fun endOfMonth(): KiraDate = KiraDate(year, month, daysInMonth(year, month))

    fun startOfYear(): KiraDate = KiraDate(year, 1, 1)

    fun plusDays(delta: Int): KiraDate = kiraDateFromEpochDay(toEpochDay(this) + delta.toLong())

    fun minusDays(delta: Int): KiraDate = plusDays(-delta)

    fun plusMonths(delta: Int): KiraDate {
        val totalMonths = year * 12 + (month - 1) + delta
        val newYear = MathFloorDiv(totalMonths, 12)
        val newMonth = MathFloorMod(totalMonths, 12) + 1
        val clampedDay = day.coerceAtMost(daysInMonth(newYear, newMonth))
        return KiraDate(newYear, newMonth, clampedDay)
    }

    fun minusMonths(delta: Int): KiraDate = plusMonths(-delta)
}

@Immutable
data class KiraDateRange(
    val from: KiraDate? = null,
    val to: KiraDate? = null,
) {
    val isComplete: Boolean get() = from != null && to != null
}

@Immutable
data class KiraDateTime(
    val date: KiraDate,
    val time: KiraTime = KiraTime(0, 0, 0),
) : Comparable<KiraDateTime> {
    override fun compareTo(other: KiraDateTime): Int {
        val dateCmp = date.compareTo(other.date)
        return if (dateCmp != 0) dateCmp else time.compareTo(other.time)
    }

    fun toIsoString(): String =
        "${date.toIsoString()}T${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}:${time.second.toString().padStart(2, '0')}"
}

@Immutable
data class KiraDatePreset(
    val label: String,
    val resolve: (today: KiraDate) -> KiraDate,
)

@Immutable
data class KiraDateRangePreset(
    val label: String,
    val resolve: (today: KiraDate) -> KiraDateRange,
)

enum class KiraDatePickerApplyMode {
    Immediate,
    Confirm,
}

enum class KiraDatePickerVariant {
    Default,
    Outline,
    Ghost,
}

enum class KiraDatePickerSize(
    val minHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val fontSize: Int,
    internal val iconSize: Dp,
) {
    Sm(32.dp, 10.dp, 12, 14.dp),
    Default(40.dp, 12.dp, 14, 16.dp),
    Lg(48.dp, 16.dp, 16, 20.dp),
}

enum class KiraDateFormat {
    Medium,
    Long,
    Iso,
    Slash,
}

val DefaultKiraDatePresets: List<KiraDatePreset> = listOf(
    KiraDatePreset("Today") { it },
    KiraDatePreset("Yesterday") { it.minusDays(1) },
    KiraDatePreset("A week ago") { it.minusDays(7) },
)

val DefaultKiraDateRangePresets: List<KiraDateRangePreset> = listOf(
    KiraDateRangePreset("Last 7 days") { KiraDateRange(it.minusDays(6), it) },
    KiraDateRangePreset("Last 30 days") { KiraDateRange(it.minusDays(29), it) },
    KiraDateRangePreset("Month to date") { KiraDateRange(it.startOfMonth(), it) },
    KiraDateRangePreset("Last month") {
        val prev = it.minusMonths(1)
        KiraDateRange(prev.startOfMonth(), prev.endOfMonth())
    },
    KiraDateRangePreset("Year to date") { KiraDateRange(it.startOfYear(), it) },
)

private val MONTH_NAMES_LONG = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private val MONTH_NAMES_SHORT = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

private val WEEKDAY_SHORT = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

private fun MathFloorDiv(a: Int, b: Int): Int {
    var r = a / b
    if ((a xor b) < 0 && r * b != a) r--
    return r
}

private fun MathFloorMod(a: Int, b: Int): Int = a - MathFloorDiv(a, b) * b

fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> throw IllegalArgumentException("Invalid month: $month")
}

/**
 * Converts a [KiraDate] to days since 1970-01-01 using Howard Hinnant's civil calendar formula.
 */
fun toEpochDay(date: KiraDate): Long {
    val y = date.year.toLong() - if (date.month <= 2) 1L else 0L
    val era = (if (y >= 0L) y else y - 399L) / 400L
    val yoe = y - era * 400L
    val m = date.month.toLong()
    val doy = (153L * (m + if (m > 2L) -3L else 9L) + 2L) / 5L + date.day.toLong() - 1L
    val doe = yoe * 365L + yoe / 4L - yoe / 100L + doy
    return era * 146097L + doe - 719468L
}

/**
 * Converts days since 1970-01-01 back to a [KiraDate].
 */
fun kiraDateFromEpochDay(epochDay: Long): KiraDate {
    val z = epochDay + 719468L
    val era = (if (z >= 0L) z else z - 146096L) / 146097L
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
    val y = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val d = doy - (153L * mp + 2L) / 5L + 1L
    val m = mp + if (mp < 10L) 3L else -9L
    val finalYear = y + if (m <= 2L) 1L else 0L
    return KiraDate(finalYear.toInt(), m.toInt(), d.toInt())
}

/**
 * Returns day-of-week index where `0 = Sunday .. 6 = Saturday`.
 */
fun dayOfWeekIndex(date: KiraDate): Int {
    val mod = ((toEpochDay(date) + 4L) % 7L + 7L) % 7L
    return mod.toInt()
}

fun formatKiraDate(
    date: KiraDate,
    format: KiraDateFormat = KiraDateFormat.Medium,
): String = when (format) {
    KiraDateFormat.Medium -> "${MONTH_NAMES_SHORT[date.month - 1]} ${date.day}, ${date.year}"
    KiraDateFormat.Long -> "${MONTH_NAMES_LONG[date.month - 1]} ${date.day}, ${date.year}"
    KiraDateFormat.Iso -> date.toIsoString()
    KiraDateFormat.Slash ->
        "${date.year.toString().padStart(4, '0')}/${date.month.toString().padStart(2, '0')}/${date.day.toString().padStart(2, '0')}"
}

fun formatKiraDateRange(
    range: KiraDateRange?,
    format: KiraDateFormat = KiraDateFormat.Medium,
    placeholder: String = "Select dates",
): String {
    val from = range?.from ?: return placeholder
    val to = range.to
    return if (to != null) {
        "${formatKiraDate(from, format)} – ${formatKiraDate(to, format)}"
    } else {
        "${formatKiraDate(from, format)} – …"
    }
}

/**
 * Parses typed date input in `yyyy-MM-dd`, `yyyy/MM/dd`, or `MM/dd/yyyy` format.
 */
fun parseKiraDate(text: String): KiraDate? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null

    val sep = when {
        '-' in trimmed -> '-'
        '/' in trimmed -> '/'
        else -> return null
    }
    val parts = trimmed.split(sep)
    if (parts.size != 3) return null
    val p0 = parts[0].toIntOrNull() ?: return null
    val p1 = parts[1].toIntOrNull() ?: return null
    val p2 = parts[2].toIntOrNull() ?: return null

    val (year, month, day) = if (parts[0].length == 4) {
        Triple(p0, p1, p2)
    } else if (parts[2].length == 4 && sep == '/') {
        Triple(p2, p0, p1)
    } else {
        return null
    }

    if (year !in 1..9999 || month !in 1..12) return null
    val maxDay = daysInMonth(year, month)
    if (day !in 1..maxDay) return null
    return KiraDate(year, month, day)
}

fun isKiraDateDisabled(
    date: KiraDate,
    minDate: KiraDate? = null,
    maxDate: KiraDate? = null,
    disabledDates: Set<KiraDate> = emptySet(),
): Boolean {
    if (minDate != null && date < minDate) return true
    if (maxDate != null && date > maxDate) return true
    if (date in disabledDates) return true
    return false
}

/**
 * Single-date picker aligned with kira-kit-v2's `DatePicker(mode = "single")` and `Calendar`.
 *
 * Includes segmented Month | Year caption switcher with inline `MonthYearPicker` overlay,
 * outside days, today dot indicator, strikethrough disabled days, "Today" footer action,
 * side-by-side quick-select presets, optional typed entry (`editable = true`), and
 * `Immediate`/`Confirm` apply modes.
 */
@Composable
fun KiraDatePicker(
    value: KiraDate?,
    onValueChange: (KiraDate?) -> Unit,
    modifier: Modifier = Modifier,
    today: KiraDate = KiraDate(2026, 11, 24),
    placeholder: String = "Select date",
    variant: KiraDatePickerVariant = KiraDatePickerVariant.Default,
    size: KiraDatePickerSize = KiraDatePickerSize.Default,
    displayFormat: KiraDateFormat = KiraDateFormat.Medium,
    applyMode: KiraDatePickerApplyMode = KiraDatePickerApplyMode.Immediate,
    closeOnSelect: Boolean = true,
    editable: Boolean = false,
    showPresets: Boolean = true,
    presets: List<KiraDatePreset> = DefaultKiraDatePresets,
    minDate: KiraDate? = null,
    maxDate: KiraDate? = null,
    disabledDates: Set<KiraDate> = emptySet(),
    weekStartsOn: Int = 0,
    clearable: Boolean = true,
    enabled: Boolean = true,
    invalid: Boolean = false,
    ariaLabel: String = "Date",
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()

    var expanded by remember { mutableStateOf(false) }
    var displayedMonth by remember { mutableStateOf((value ?: today).startOfMonth()) }
    var pendingValue by remember { mutableStateOf(value) }
    var typedText by remember(value, displayFormat) {
        mutableStateOf(value?.let { formatKiraDate(it, displayFormat) } ?: "")
    }
    var parseError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(expanded) {
        if (expanded) {
            pendingValue = value
            displayedMonth = (value ?: today).startOfMonth()
        }
    }

    val currentSelection = if (applyMode == KiraDatePickerApplyMode.Confirm) pendingValue else value
    val isInvalid = invalid || parseError != null

    fun commitTyped() {
        val trimmed = typedText.trim()
        if (trimmed.isEmpty()) {
            parseError = null
            onValueChange(null)
            return
        }
        val parsed = parseKiraDate(trimmed)
        if (parsed == null) {
            parseError = "Enter a date like ${today.toIsoString()}"
            return
        }
        if (isKiraDateDisabled(parsed, minDate, maxDate, disabledDates)) {
            parseError = "That date isn't available"
            return
        }
        parseError = null
        onValueChange(parsed)
        displayedMonth = parsed.startOfMonth()
        typedText = formatKiraDate(parsed, displayFormat)
    }

    fun selectDate(date: KiraDate) {
        haptics.perform(KiraHapticFeedbackType.Selection)
        parseError = null
        if (applyMode == KiraDatePickerApplyMode.Confirm) {
            pendingValue = date
        } else {
            onValueChange(date)
            if (closeOnSelect) expanded = false
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DatePickerPopoverHost(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            trigger = {
                DatePickerTriggerSurface(
                    label = value?.let { formatKiraDate(it, displayFormat) },
                    placeholder = placeholder,
                    variant = variant,
                    size = size,
                    expanded = expanded,
                    enabled = enabled,
                    invalid = isInvalid,
                    clearable = clearable && value != null,
                    editable = editable,
                    typedText = typedText,
                    onTypedTextChange = {
                        typedText = it
                        parseError = null
                    },
                    onCommitTyped = ::commitTyped,
                    ariaLabel = ariaLabel,
                    onToggle = {
                        haptics.perform(KiraHapticFeedbackType.Light)
                        expanded = !expanded
                    },
                    onClear = {
                        haptics.perform(KiraHapticFeedbackType.Light)
                        parseError = null
                        typedText = ""
                        onValueChange(null)
                    },
                )
            },
            sheetTitle = ariaLabel,
            sheetSubtitle = currentSelection?.let { formatKiraDate(it, displayFormat) } ?: placeholder,
            mobileContent = {
                val hasPresets = showPresets && presets.isNotEmpty()
                if (hasPresets) {
                    MobilePresetChipsRow(
                        items = presets.map { p ->
                            val target = p.resolve(today)
                            PresetChipItem(
                                label = p.label,
                                active = currentSelection == target,
                                enabled = !isKiraDateDisabled(target, minDate, maxDate, disabledDates),
                                onClick = {
                                    displayedMonth = target.startOfMonth()
                                    selectDate(target)
                                },
                            )
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    CalendarSurface(
                        month = displayedMonth,
                        onMonthChange = { displayedMonth = it.startOfMonth() },
                        today = today,
                        selectedDate = currentSelection,
                        selectedRange = null,
                        numberOfMonths = 1,
                        showOutsideDays = true,
                        compact = true,
                        minDate = minDate,
                        maxDate = maxDate,
                        disabledDates = disabledDates,
                        weekStartsOn = weekStartsOn,
                        onDayClick = ::selectDate,
                        onTodayClick = {
                            displayedMonth = today.startOfMonth()
                            if (!isKiraDateDisabled(today, minDate, maxDate, disabledDates)) {
                                selectDate(today)
                            }
                        },
                    )
                }
                MobileSheetFooter(
                    isConfirm = applyMode == KiraDatePickerApplyMode.Confirm,
                    canApply = pendingValue != null,
                    showClear = clearable && currentSelection != null,
                    onClear = {
                        haptics.perform(KiraHapticFeedbackType.Light)
                        if (applyMode == KiraDatePickerApplyMode.Confirm) {
                            pendingValue = null
                        } else {
                            onValueChange(null)
                        }
                    },
                    onCancel = { expanded = false },
                    onApply = {
                        onValueChange(pendingValue)
                        expanded = false
                    },
                    onDone = { expanded = false },
                )
            },
            popupContent = {
                val hasPresets = showPresets && presets.isNotEmpty()
                val popupWidth = (if (hasPresets) 149.dp else 0.dp) + 280.dp
                Column(modifier = Modifier.width(popupWidth)) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        if (hasPresets) {
                            PresetSidebar(
                                items = presets.map { p ->
                                    val target = p.resolve(today)
                                    PresetChipItem(
                                        label = p.label,
                                        active = currentSelection == target,
                                        enabled = !isKiraDateDisabled(target, minDate, maxDate, disabledDates),
                                        onClick = {
                                            displayedMonth = target.startOfMonth()
                                            selectDate(target)
                                        },
                                    )
                                },
                            )
                        }
                        CalendarSurface(
                            month = displayedMonth,
                            onMonthChange = { displayedMonth = it.startOfMonth() },
                            today = today,
                            selectedDate = currentSelection,
                            selectedRange = null,
                            numberOfMonths = 1,
                            showOutsideDays = true,
                            compact = false,
                            minDate = minDate,
                            maxDate = maxDate,
                            disabledDates = disabledDates,
                            weekStartsOn = weekStartsOn,
                            onDayClick = ::selectDate,
                            onTodayClick = {
                                displayedMonth = today.startOfMonth()
                                if (!isKiraDateDisabled(today, minDate, maxDate, disabledDates)) {
                                    selectDate(today)
                                }
                            },
                        )
                    }
                    if (applyMode == KiraDatePickerApplyMode.Confirm) {
                        ConfirmFooterBar(
                            previewText = pendingValue?.let { formatKiraDate(it, displayFormat) } ?: placeholder,
                            canApply = pendingValue != null,
                            onCancel = { expanded = false },
                            onApply = {
                                onValueChange(pendingValue)
                                expanded = false
                            },
                        )
                    }
                }
            },
        )
        if (parseError != null) {
            BasicText(
                text = parseError!!,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = KiraTheme.typography.caption.copy(color = colors.destructive),
            )
        }
    }
}

/**
 * Date range picker aligned with kira-kit-v2's `DatePicker(mode = "range")` and `Calendar(mode = "range")`.
 */
@Composable
fun KiraDateRangePicker(
    value: KiraDateRange?,
    onValueChange: (KiraDateRange?) -> Unit,
    modifier: Modifier = Modifier,
    today: KiraDate = KiraDate(2026, 11, 24),
    placeholder: String = "Select dates",
    variant: KiraDatePickerVariant = KiraDatePickerVariant.Default,
    size: KiraDatePickerSize = KiraDatePickerSize.Default,
    displayFormat: KiraDateFormat = KiraDateFormat.Medium,
    applyMode: KiraDatePickerApplyMode = KiraDatePickerApplyMode.Immediate,
    closeOnSelect: Boolean = true,
    showPresets: Boolean = true,
    presets: List<KiraDateRangePreset> = DefaultKiraDateRangePresets,
    minDate: KiraDate? = null,
    maxDate: KiraDate? = null,
    disabledDates: Set<KiraDate> = emptySet(),
    weekStartsOn: Int = 0,
    clearable: Boolean = true,
    enabled: Boolean = true,
    invalid: Boolean = false,
    ariaLabel: String = "Date range",
) {
    val haptics = rememberKiraHaptics()
    var expanded by remember { mutableStateOf(false) }
    var displayedMonth by remember { mutableStateOf((value?.from ?: today).startOfMonth()) }
    var pendingRange by remember { mutableStateOf(value) }

    LaunchedEffect(expanded) {
        if (expanded) {
            pendingRange = value
            displayedMonth = (value?.from ?: today).startOfMonth()
        }
    }

    val currentRange = if (applyMode == KiraDatePickerApplyMode.Confirm) pendingRange else value

    fun handleRangeDayClick(day: KiraDate) {
        haptics.perform(KiraHapticFeedbackType.Selection)
        val cur = currentRange
        val next = when {
            cur == null || cur.from == null || cur.to != null -> KiraDateRange(from = day, to = null)
            day < cur.from -> KiraDateRange(from = day, to = cur.from)
            else -> KiraDateRange(from = cur.from, to = day)
        }
        if (applyMode == KiraDatePickerApplyMode.Confirm) {
            pendingRange = next
        } else {
            onValueChange(next)
            if (closeOnSelect && next.isComplete) expanded = false
        }
    }

    fun selectPresetRange(range: KiraDateRange) {
        haptics.perform(KiraHapticFeedbackType.Selection)
        range.from?.let { displayedMonth = it.startOfMonth() }
        if (applyMode == KiraDatePickerApplyMode.Confirm) {
            pendingRange = range
        } else {
            onValueChange(range)
            if (closeOnSelect) expanded = false
        }
    }

    DatePickerPopoverHost(
        modifier = modifier,
        expanded = expanded && enabled,
        onDismissRequest = { expanded = false },
        trigger = {
            DatePickerTriggerSurface(
                label = if (value?.from != null) formatKiraDateRange(value, displayFormat, placeholder) else null,
                placeholder = placeholder,
                variant = variant,
                size = size,
                expanded = expanded,
                enabled = enabled,
                invalid = invalid,
                clearable = clearable && value?.from != null,
                editable = false,
                typedText = "",
                onTypedTextChange = {},
                onCommitTyped = {},
                ariaLabel = ariaLabel,
                onToggle = {
                    haptics.perform(KiraHapticFeedbackType.Light)
                    expanded = !expanded
                },
                onClear = {
                    haptics.perform(KiraHapticFeedbackType.Light)
                    onValueChange(null)
                },
            )
        },
        sheetTitle = ariaLabel,
        sheetSubtitle = formatKiraDateRange(currentRange, displayFormat, placeholder),
        mobileContent = {
            val hasPresets = showPresets && presets.isNotEmpty()
            // Live From -> To endpoint indicator cards (highlights whichever end is being picked next)
            MobileRangeStatusRow(
                range = currentRange,
                displayFormat = displayFormat,
            )
            if (hasPresets) {
                MobilePresetChipsRow(
                    items = presets.map { p ->
                        val target = p.resolve(today)
                        val blocked = (target.from != null && isKiraDateDisabled(target.from, minDate, maxDate, disabledDates)) ||
                            (target.to != null && isKiraDateDisabled(target.to, minDate, maxDate, disabledDates))
                        PresetChipItem(
                            label = p.label,
                            active = currentRange == target,
                            enabled = !blocked,
                            onClick = { selectPresetRange(target) },
                        )
                    },
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                CalendarSurface(
                    month = displayedMonth,
                    onMonthChange = { displayedMonth = it.startOfMonth() },
                    today = today,
                    selectedDate = null,
                    selectedRange = currentRange,
                    numberOfMonths = 1,
                    showOutsideDays = true,
                    compact = true,
                    minDate = minDate,
                    maxDate = maxDate,
                    disabledDates = disabledDates,
                    weekStartsOn = weekStartsOn,
                    onDayClick = ::handleRangeDayClick,
                    onTodayClick = {
                        displayedMonth = today.startOfMonth()
                    },
                )
            }
            MobileSheetFooter(
                isConfirm = applyMode == KiraDatePickerApplyMode.Confirm,
                canApply = pendingRange?.isComplete == true,
                showClear = clearable && currentRange?.from != null,
                onClear = {
                    haptics.perform(KiraHapticFeedbackType.Light)
                    if (applyMode == KiraDatePickerApplyMode.Confirm) {
                        pendingRange = null
                    } else {
                        onValueChange(null)
                    }
                },
                onCancel = { expanded = false },
                onApply = {
                    onValueChange(pendingRange)
                    expanded = false
                },
                onDone = { expanded = false },
            )
        },
        popupContent = { windowWidthDp ->
            val hasPresets = showPresets && presets.isNotEmpty()
            val showDualMonths = windowWidthDp == 0.dp || windowWidthDp >= (if (hasPresets) 740.dp else 600.dp)
            val monthCount = if (showDualMonths) 2 else 1
            val calendarWidth = if (showDualMonths) 552.dp else 280.dp
            val popupWidth = (if (hasPresets) 149.dp else 0.dp) + calendarWidth
            Column(modifier = Modifier.width(popupWidth)) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    if (hasPresets) {
                        PresetSidebar(
                            items = presets.map { p ->
                                val target = p.resolve(today)
                                val blocked = (target.from != null && isKiraDateDisabled(target.from, minDate, maxDate, disabledDates)) ||
                                    (target.to != null && isKiraDateDisabled(target.to, minDate, maxDate, disabledDates))
                                PresetChipItem(
                                    label = p.label,
                                    active = currentRange == target,
                                    enabled = !blocked,
                                    onClick = { selectPresetRange(target) },
                                )
                            },
                        )
                    }
                    CalendarSurface(
                        month = displayedMonth,
                        onMonthChange = { displayedMonth = it.startOfMonth() },
                        today = today,
                        selectedDate = null,
                        selectedRange = currentRange,
                        numberOfMonths = monthCount,
                        showOutsideDays = !showDualMonths,
                        compact = false,
                        minDate = minDate,
                        maxDate = maxDate,
                        disabledDates = disabledDates,
                        weekStartsOn = weekStartsOn,
                        onDayClick = ::handleRangeDayClick,
                        onTodayClick = {
                            displayedMonth = today.startOfMonth()
                        },
                    )
                }
                if (applyMode == KiraDatePickerApplyMode.Confirm) {
                    ConfirmFooterBar(
                        previewText = formatKiraDateRange(pendingRange, displayFormat, placeholder),
                        canApply = pendingRange?.isComplete == true,
                        onCancel = { expanded = false },
                        onApply = {
                            onValueChange(pendingRange)
                            expanded = false
                        },
                    )
                }
            }
        },
    )
}

/**
 * Date & Time picker aligned with kira-kit-v2's `DatePicker(mode = "datetime")`.
 *
 * Embeds an inline `KiraTimePickerPanel` to the right of the calendar inside one popover.
 */
@Composable
fun KiraDateTimePicker(
    value: KiraDateTime?,
    onValueChange: (KiraDateTime?) -> Unit,
    modifier: Modifier = Modifier,
    today: KiraDate = KiraDate(2026, 11, 24),
    placeholder: String = "Select date & time",
    variant: KiraDatePickerVariant = KiraDatePickerVariant.Default,
    size: KiraDatePickerSize = KiraDatePickerSize.Default,
    displayFormat: KiraDateFormat = KiraDateFormat.Medium,
    granularity: KiraTimeGranularity = KiraTimeGranularity.Minute,
    hourCycle: KiraTimeHourCycle = KiraTimeHourCycle.Hour24,
    applyMode: KiraDatePickerApplyMode = KiraDatePickerApplyMode.Immediate,
    minDate: KiraDate? = null,
    maxDate: KiraDate? = null,
    disabledDates: Set<KiraDate> = emptySet(),
    weekStartsOn: Int = 0,
    clearable: Boolean = true,
    enabled: Boolean = true,
    invalid: Boolean = false,
    ariaLabel: String = "Date and time",
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    var expanded by remember { mutableStateOf(false) }
    var displayedMonth by remember { mutableStateOf((value?.date ?: today).startOfMonth()) }
    var pending by remember { mutableStateOf(value) }

    LaunchedEffect(expanded) {
        if (expanded) {
            pending = value
            displayedMonth = (value?.date ?: today).startOfMonth()
        }
    }

    val activeValue = if (applyMode == KiraDatePickerApplyMode.Confirm) pending else value

    fun formatDateTimeLabel(dt: KiraDateTime): String =
        "${formatKiraDate(dt.date, displayFormat)} · ${formatKiraTime(dt.time, granularity, hourCycle)}"

    fun updateValue(next: KiraDateTime) {
        haptics.perform(KiraHapticFeedbackType.Selection)
        if (applyMode == KiraDatePickerApplyMode.Confirm) {
            pending = next
        } else {
            onValueChange(next)
        }
    }

    DatePickerPopoverHost(
        modifier = modifier,
        expanded = expanded && enabled,
        onDismissRequest = { expanded = false },
        trigger = {
            DatePickerTriggerSurface(
                label = value?.let(::formatDateTimeLabel),
                placeholder = placeholder,
                variant = variant,
                size = size,
                expanded = expanded,
                enabled = enabled,
                invalid = invalid,
                clearable = clearable && value != null,
                editable = false,
                typedText = "",
                onTypedTextChange = {},
                onCommitTyped = {},
                ariaLabel = ariaLabel,
                onToggle = {
                    haptics.perform(KiraHapticFeedbackType.Light)
                    expanded = !expanded
                },
                onClear = {
                    haptics.perform(KiraHapticFeedbackType.Light)
                    onValueChange(null)
                },
            )
        },
        sheetTitle = ariaLabel,
        sheetSubtitle = activeValue?.let(::formatDateTimeLabel) ?: placeholder,
        mobileContent = {
            var activeTab by remember(expanded) { mutableIntStateOf(0) } // 0 = Date, 1 = Time
            MobileDateTimeTabBar(
                activeTab = activeTab,
                dateSummary = activeValue?.date?.let { formatKiraDate(it, displayFormat) } ?: "Pick date",
                timeSummary = activeValue?.time?.let { formatKiraTime(it, granularity, hourCycle) } ?: "Pick time",
                onSelectTab = { activeTab = it },
            )
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(tween(160, easing = KiraMotion.EaseOutStrong)) togetherWith
                            fadeOut(tween(120, easing = KiraMotion.EaseOutStrong))
                    },
                    label = "MobileDateTimeTab",
                ) { tab ->
                    if (tab == 0) {
                        CalendarSurface(
                            month = displayedMonth,
                            onMonthChange = { displayedMonth = it.startOfMonth() },
                            today = today,
                            selectedDate = activeValue?.date,
                            selectedRange = null,
                            numberOfMonths = 1,
                            showOutsideDays = true,
                            compact = true,
                            minDate = minDate,
                            maxDate = maxDate,
                            disabledDates = disabledDates,
                            weekStartsOn = weekStartsOn,
                            onDayClick = { day ->
                                updateValue(KiraDateTime(day, activeValue?.time ?: KiraTime(9, 0)))
                            },
                            onTodayClick = {
                                displayedMonth = today.startOfMonth()
                                if (!isKiraDateDisabled(today, minDate, maxDate, disabledDates)) {
                                    updateValue(KiraDateTime(today, activeValue?.time ?: KiraTime(9, 0)))
                                }
                            },
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            KiraTimePickerPanel(
                                value = activeValue?.time,
                                onValueChange = { time ->
                                    updateValue(KiraDateTime(activeValue?.date ?: today, time))
                                },
                                variant = KiraTimePickerVariant.Stepper,
                                granularity = granularity,
                                hourCycle = hourCycle,
                                hourStep = 1,
                                minuteStep = 1,
                                secondStep = 1,
                                minTime = null,
                                maxTime = null,
                                disabledTimes = emptySet(),
                                presets = emptyList(),
                                enabled = enabled,
                            )
                        }
                    }
                }
            }
            MobileSheetFooter(
                isConfirm = applyMode == KiraDatePickerApplyMode.Confirm,
                canApply = pending != null,
                showClear = clearable && activeValue != null,
                onClear = {
                    haptics.perform(KiraHapticFeedbackType.Light)
                    if (applyMode == KiraDatePickerApplyMode.Confirm) {
                        pending = null
                    } else {
                        onValueChange(null)
                    }
                },
                onCancel = { expanded = false },
                onApply = {
                    onValueChange(pending)
                    expanded = false
                },
                onDone = { expanded = false },
            )
        },
        popupContent = {
            val timeWidth = when {
                granularity == KiraTimeGranularity.Second && hourCycle == KiraTimeHourCycle.Hour12 -> 336.dp
                granularity == KiraTimeGranularity.Second || hourCycle == KiraTimeHourCycle.Hour12 -> 288.dp
                else -> 248.dp
            }
            Column(
                modifier = Modifier
                    .width(280.dp + 1.dp + timeWidth)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Top,
                ) {
                    CalendarSurface(
                        month = displayedMonth,
                        onMonthChange = { displayedMonth = it.startOfMonth() },
                        today = today,
                        selectedDate = activeValue?.date,
                        selectedRange = null,
                        numberOfMonths = 1,
                        showOutsideDays = true,
                        compact = false,
                        minDate = minDate,
                        maxDate = maxDate,
                        disabledDates = disabledDates,
                        weekStartsOn = weekStartsOn,
                        onDayClick = { day ->
                            updateValue(KiraDateTime(day, activeValue?.time ?: KiraTime(9, 0)))
                        },
                        onTodayClick = {
                            displayedMonth = today.startOfMonth()
                            if (!isKiraDateDisabled(today, minDate, maxDate, disabledDates)) {
                                updateValue(KiraDateTime(today, activeValue?.time ?: KiraTime(9, 0)))
                            }
                        },
                    )
                    Box(Modifier.fillMaxHeight().width(1.dp).background(colors.border))
                    Box(
                        modifier = Modifier
                            .width(timeWidth)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        KiraTimePickerPanel(
                            value = activeValue?.time,
                            onValueChange = { time ->
                                updateValue(KiraDateTime(activeValue?.date ?: today, time))
                            },
                            variant = KiraTimePickerVariant.Stepper,
                            granularity = granularity,
                            hourCycle = hourCycle,
                            hourStep = 1,
                            minuteStep = 1,
                            secondStep = 1,
                            minTime = null,
                            maxTime = null,
                            disabledTimes = emptySet(),
                            presets = emptyList(),
                            enabled = enabled,
                        )
                    }
                }
                if (applyMode == KiraDatePickerApplyMode.Confirm) {
                    ConfirmFooterBar(
                        previewText = pending?.let(::formatDateTimeLabel) ?: placeholder,
                        canApply = pending != null,
                        onCancel = { expanded = false },
                        onApply = {
                            onValueChange(pending)
                            expanded = false
                        },
                    )
                }
            }
        },
    )
}

/*                              Presets Sidebar                               */

private data class PresetChipItem(
    val label: String,
    val active: Boolean,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun PresetSidebar(items: List<PresetChipItem>) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius

    Row(modifier = Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(148.dp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BasicText(
                text = "Quick select",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = KiraTheme.typography.caption.copy(
                    color = colors.mutedForeground,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            items.forEach { item ->
                val interaction = remember { MutableInteractionSource() }
                val hovered by interaction.collectIsHoveredAsState()
                val bg by animateColorAsState(
                    targetValue = when {
                        item.active -> colors.primary.copy(alpha = 0.12f)
                        hovered && item.enabled -> colors.muted
                        else -> Color.Transparent
                    },
                    animationSpec = KiraMotion.fastTween(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (item.enabled) 1f else 0.4f)
                        .clip(radius.shapeLg)
                        .background(bg)
                        .hoverable(interaction, enabled = item.enabled)
                        .pointerHoverIcon(if (item.enabled) PointerIcon.Hand else PointerIcon.Default)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = item.enabled,
                            role = Role.Button,
                            onClick = item.onClick,
                        )
                        .semantics {
                            selected = item.active
                            if (!item.enabled) disabled()
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicText(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = KiraTheme.typography.bodySmall.copy(
                            color = if (item.active) colors.primary else colors.popoverForeground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
        Box(Modifier.fillMaxHeight().width(1.dp).background(colors.border))
    }
}

@Composable
private fun MobilePresetChipsRow(items: List<PresetChipItem>) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val bg by animateColorAsState(
                    targetValue = if (item.active) colors.primary.copy(alpha = 0.14f) else colors.muted.copy(alpha = 0.45f),
                    animationSpec = KiraMotion.fastTween(),
                )
                val border = if (item.active) colors.primary.copy(alpha = 0.5f) else colors.border
                Box(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 44.dp)
                        .alpha(if (item.enabled) 1f else 0.4f)
                        .clip(radius.shapeFull)
                        .background(bg)
                        .border(1.dp, border, radius.shapeFull)
                        .clickable(
                            enabled = item.enabled,
                            role = Role.Button,
                            onClick = item.onClick,
                        )
                        .semantics {
                            selected = item.active
                            if (!item.enabled) disabled()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = item.label,
                        maxLines = 1,
                        style = KiraTheme.typography.label.copy(
                            color = if (item.active) colors.primary else colors.foreground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
private fun MobileRangeStatusRow(
    range: KiraDateRange?,
    displayFormat: KiraDateFormat,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val pickingEnd = range?.from != null && range.to == null

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RangeEndpointPill(
                title = "Start",
                value = range?.from?.let { formatKiraDate(it, displayFormat) } ?: "Select date",
                hasValue = range?.from != null,
                highlighted = !pickingEnd,
                modifier = Modifier.weight(1f),
            )
            BasicText(
                text = "→",
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
            )
            RangeEndpointPill(
                title = "End",
                value = range?.to?.let { formatKiraDate(it, displayFormat) } ?: "Select date",
                hasValue = range?.to != null,
                highlighted = pickingEnd,
                modifier = Modifier.weight(1f),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
private fun RangeEndpointPill(
    title: String,
    value: String,
    hasValue: Boolean,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    Column(
        modifier = modifier
            .clip(radius.shapeLg)
            .background(if (highlighted) colors.primary.copy(alpha = 0.08f) else colors.muted.copy(alpha = 0.35f))
            .border(1.dp, if (highlighted) colors.primary.copy(alpha = 0.55f) else colors.border, radius.shapeLg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BasicText(
            text = title.uppercase(),
            style = KiraTheme.typography.caption.copy(
                fontSize = 10.sp,
                color = if (highlighted) colors.primary else colors.mutedForeground,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
            ),
        )
        BasicText(
            text = value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = KiraTheme.typography.bodySmall.copy(
                color = if (hasValue) colors.foreground else colors.mutedForeground,
                fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun MobileDateTimeTabBar(
    activeTab: Int,
    dateSummary: String,
    timeSummary: String,
    onSelectTab: (Int) -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(radius.shapeLg)
                .background(colors.muted.copy(alpha = 0.5f))
                .border(1.dp, colors.border, radius.shapeLg)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf(
                Triple(0, "Date", dateSummary),
                Triple(1, "Time", timeSummary),
            ).forEach { (idx, label, summary) ->
                val isSelected = activeTab == idx
                val bg by animateColorAsState(
                    targetValue = if (isSelected) colors.background else Color.Transparent,
                    animationSpec = KiraMotion.fastTween(),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(radius.shapeMd)
                        .background(bg)
                        .clickable(role = Role.Tab, onClick = { onSelectTab(idx) })
                        .semantics { selected = isSelected }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    BasicText(
                        text = label,
                        style = KiraTheme.typography.caption.copy(
                            fontSize = 11.sp,
                            color = if (isSelected) colors.primary else colors.mutedForeground,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    BasicText(
                        text = summary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = KiraTheme.typography.bodySmall.copy(
                            color = if (isSelected) colors.foreground else colors.mutedForeground,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
private fun MobileSheetFooter(
    isConfirm: Boolean,
    canApply: Boolean,
    showClear: Boolean,
    onClear: () -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = KiraTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isConfirm) {
                KiraButton(
                    text = "Cancel",
                    variant = KiraButtonVariant.Ghost,
                    size = KiraButtonSize.Sm,
                    onClick = onCancel,
                )
                KiraButton(
                    text = "Apply",
                    size = KiraButtonSize.Sm,
                    enabled = canApply,
                    onClick = onApply,
                )
            } else {
                if (showClear) {
                    KiraButton(
                        text = "Clear",
                        variant = KiraButtonVariant.Ghost,
                        size = KiraButtonSize.Sm,
                        onClick = onClear,
                    )
                }
                KiraButton(
                    text = "Done",
                    size = KiraButtonSize.Sm,
                    onClick = onDone,
                )
            }
        }
    }
}

/*                           Calendar & MonthYearPicker                       */

private enum class MonthYearOverlayMode {
    Month,
    Year,
}

@Composable
private fun CalendarSurface(
    month: KiraDate,
    onMonthChange: (KiraDate) -> Unit,
    today: KiraDate,
    selectedDate: KiraDate?,
    selectedRange: KiraDateRange?,
    numberOfMonths: Int,
    showOutsideDays: Boolean,
    compact: Boolean,
    minDate: KiraDate?,
    maxDate: KiraDate?,
    disabledDates: Set<KiraDate>,
    weekStartsOn: Int,
    onDayClick: (KiraDate) -> Unit,
    onTodayClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    var hoveredDay by remember { mutableStateOf<KiraDate?>(null) }
    var overlayMode by remember { mutableStateOf<MonthYearOverlayMode?>(null) }
    var overlayDisplayIndex by remember { mutableIntStateOf(0) }

    val todayDisabled = isKiraDateDisabled(today, minDate, maxDate, disabledDates)
    val desktopWidth = 256.dp * numberOfMonths + 16.dp * (numberOfMonths - 1).coerceAtLeast(0) + 24.dp

    Box(
        modifier = Modifier
            .then(if (compact) Modifier.fillMaxWidth() else Modifier.width(desktopWidth))
            .padding(horizontal = if (compact) 16.dp else 12.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedContent(
                targetState = month,
                transitionSpec = {
                    val dir = if (targetState >= initialState) 1 else -1
                    (fadeIn(tween(160, easing = KiraMotion.EaseOutStrong)) +
                        slideInHorizontally(tween(160, easing = KiraMotion.EaseOutStrong)) { dir * 18 }) togetherWith
                        (fadeOut(tween(120, easing = KiraMotion.EaseOutStrong)) +
                            slideOutHorizontally(tween(120, easing = KiraMotion.EaseOutStrong)) { -dir * 18 })
                },
                label = "CalendarMonthSlide",
            ) { currentMonth ->
                Row(
                    modifier = if (compact) Modifier.fillMaxWidth() else Modifier,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    for (idx in 0 until numberOfMonths) {
                        val panelMonth = currentMonth.plusMonths(idx)
                        CalendarMonthGrid(
                            month = panelMonth,
                            today = today,
                            selectedDate = selectedDate,
                            selectedRange = selectedRange,
                            hoveredDay = hoveredDay,
                            showPrev = idx == 0,
                            showNext = idx == numberOfMonths - 1,
                            showOutsideDays = showOutsideDays,
                            compact = compact,
                            minDate = minDate,
                            maxDate = maxDate,
                            disabledDates = disabledDates,
                            weekStartsOn = weekStartsOn,
                            onPrevMonth = {
                                onMonthChange(currentMonth.minusMonths(1))
                            },
                            onNextMonth = {
                                onMonthChange(currentMonth.plusMonths(1))
                            },
                            onOpenMonthPicker = {
                                overlayDisplayIndex = idx
                                overlayMode = MonthYearOverlayMode.Month
                            },
                            onOpenYearPicker = {
                                overlayDisplayIndex = idx
                                overlayMode = MonthYearOverlayMode.Year
                            },
                            onDayHover = { hoveredDay = it },
                            onDayClick = onDayClick,
                        )
                    }
                }
            }

            // Footer "Today" button (matches calendar.tsx line 1143)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                val todayInteraction = remember { MutableInteractionSource() }
                val todayHovered by todayInteraction.collectIsHoveredAsState()
                val todayBg by animateColorAsState(
                    targetValue = if (todayHovered && !todayDisabled) colors.muted else Color.Transparent,
                    animationSpec = KiraMotion.fastTween(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = if (compact) 44.dp else 32.dp)
                        .alpha(if (todayDisabled) 0.4f else 1f)
                        .clip(radius.shapeMd)
                        .background(todayBg)
                        .hoverable(todayInteraction, enabled = !todayDisabled)
                        .pointerHoverIcon(if (!todayDisabled) PointerIcon.Hand else PointerIcon.Default)
                        .clickable(
                            interactionSource = todayInteraction,
                            indication = null,
                            enabled = !todayDisabled,
                            role = Role.Button,
                            onClick = onTodayClick,
                        )
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Today",
                        style = KiraTheme.typography.bodySmall.copy(
                            color = colors.popoverForeground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }

        // Inline Month / Year Picker Overlay (matches MonthYearPicker in calendar.tsx)
        overlayMode?.let { activeMode ->
            val anchorMonth = month.plusMonths(overlayDisplayIndex)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(radius.shapeLg)
                    .background(if (compact) colors.background else colors.popover)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                            overlayMode = null
                            true
                        } else {
                            false
                        }
                    },
            ) {
                MonthYearPickerOverlay(
                    initialMode = activeMode,
                    currentMonth = anchorMonth,
                    today = today,
                    compact = compact,
                    minDate = minDate,
                    maxDate = maxDate,
                    onPick = { pickedMonth, closeAfter ->
                        onMonthChange(pickedMonth.minusMonths(overlayDisplayIndex))
                        if (closeAfter) overlayMode = null
                    },
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    month: KiraDate,
    today: KiraDate,
    selectedDate: KiraDate?,
    selectedRange: KiraDateRange?,
    hoveredDay: KiraDate?,
    showPrev: Boolean,
    showNext: Boolean,
    showOutsideDays: Boolean,
    compact: Boolean,
    minDate: KiraDate?,
    maxDate: KiraDate?,
    disabledDates: Set<KiraDate>,
    weekStartsOn: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenMonthPicker: () -> Unit,
    onOpenYearPicker: () -> Unit,
    onDayHover: (KiraDate?) -> Unit,
    onDayClick: (KiraDate) -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val normalizedWeekStart = ((weekStartsOn % 7) + 7) % 7
    val weekdayHeaders = remember(normalizedWeekStart) {
        (0 until 7).map { WEEKDAY_SHORT[(normalizedWeekStart + it) % 7] }
    }

    val weeks = remember(month, normalizedWeekStart) {
        val firstOfMonth = month.startOfMonth()
        val firstDow = dayOfWeekIndex(firstOfMonth)
        val leadingDays = ((firstDow - normalizedWeekStart) + 7) % 7
        val totalDays = daysInMonth(month.year, month.month)
        val startCellDate = firstOfMonth.minusDays(leadingDays)
        val totalCells = ((leadingDays + totalDays + 6) / 7) * 7
        (0 until totalCells).map { offset ->
            val d = startCellDate.plusDays(offset)
            d to (d.month != month.month)
        }.chunked(7)
    }

    val pendingFrom = if (selectedRange?.from != null && selectedRange.to == null) selectedRange.from else null
    val cellHeight = if (compact) 44.dp else 36.dp
    val navSize = if (compact) 40.dp else 28.dp

    Column(
        modifier = if (compact) Modifier.fillMaxWidth() else Modifier.width(256.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 6.dp),
    ) {
        // Header: [<]  [ Nov | 2026 ]  [>]
        Row(
            modifier = Modifier.fillMaxWidth().height(if (compact) 44.dp else 36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (showPrev) {
                CalendarNavButton(isNext = false, contentDesc = "Previous month", buttonSize = navSize, onClick = onPrevMonth)
            } else {
                Box(Modifier.size(navSize))
            }

            Row(
                modifier = Modifier
                    .clip(radius.shapeLg)
                    .background(colors.muted.copy(alpha = 0.5f))
                    .border(1.dp, colors.border, radius.shapeLg)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CaptionPillButton(
                    text = MONTH_NAMES_SHORT[month.month - 1],
                    contentDesc = "Choose month, ${MONTH_NAMES_LONG[month.month - 1]}",
                    minHeight = if (compact) 36.dp else 28.dp,
                    onClick = onOpenMonthPicker,
                )
                Box(Modifier.width(1.dp).height(16.dp).background(colors.border))
                CaptionPillButton(
                    text = month.year.toString(),
                    contentDesc = "Choose year, ${month.year}",
                    minHeight = if (compact) 36.dp else 28.dp,
                    onClick = onOpenYearPicker,
                )
            }

            if (showNext) {
                CalendarNavButton(isNext = true, contentDesc = "Next month", buttonSize = navSize, onClick = onNextMonth)
            } else {
                Box(Modifier.size(navSize))
            }
        }

        // Weekday row
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayHeaders.forEach { dayLabel ->
                Box(
                    modifier = Modifier.weight(1f).height(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = dayLabel,
                        style = KiraTheme.typography.caption.copy(
                            color = colors.mutedForeground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }

        // Day rows
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { (day, isOutside) ->
                        if (isOutside && !showOutsideDays) {
                            Box(Modifier.weight(1f).height(cellHeight))
                        } else {
                            val disabled = isKiraDateDisabled(day, minDate, maxDate, disabledDates)
                            val isToday = day == today
                            val rangeFrom = selectedRange?.from
                            val rangeTo = selectedRange?.to
                            val isRangeStart = rangeFrom == day
                            val isRangeEnd = rangeTo == day
                            val isSolid = day == selectedDate || isRangeStart || isRangeEnd
                            val isRangeMiddle = rangeFrom != null &&
                                rangeTo != null &&
                                day > rangeFrom &&
                                day < rangeTo

                            val hoverBand = if (!compact && pendingFrom != null && hoveredDay != null && !disabled) {
                                val lo = minOf(pendingFrom, hoveredDay)
                                val hi = maxOf(pendingFrom, hoveredDay)
                                when {
                                    day > lo && day < hi -> "middle"
                                    day == lo && day != hi -> "start"
                                    day == hi && day != lo -> "end"
                                    else -> null
                                }
                            } else {
                                null
                            }

                            val interaction = remember(day) { MutableInteractionSource() }
                            val hovered by interaction.collectIsHoveredAsState()
                            LaunchedEffect(hovered) {
                                if (hovered) onDayHover(day)
                            }

                            val hasMultiDayRange = rangeFrom != null && rangeTo != null && rangeFrom != rangeTo
                            val cellShape = when {
                                isRangeMiddle || hoverBand == "middle" -> RectangleShape
                                isRangeStart && hasMultiDayRange ->
                                    RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
                                isRangeEnd && hasMultiDayRange ->
                                    RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                                else -> radius.shapeMd
                            }

                            val bg = when {
                                isSolid -> colors.primary
                                isRangeMiddle -> colors.primary.copy(alpha = 0.14f)
                                hoverBand != null -> colors.primary.copy(alpha = 0.10f)
                                hovered && !disabled -> colors.muted
                                else -> Color.Transparent
                            }

                            val fg = when {
                                isSolid -> colors.primaryForeground
                                disabled -> colors.mutedForeground
                                isToday -> colors.primary
                                isOutside -> colors.mutedForeground.copy(alpha = 0.45f)
                                else -> colors.popoverForeground
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(cellHeight)
                                    .alpha(if (disabled) 0.45f else 1f)
                                    .clip(cellShape)
                                    .background(bg)
                                    .hoverable(interaction, enabled = !disabled)
                                    .pointerHoverIcon(if (!disabled) PointerIcon.Hand else PointerIcon.Default)
                                    .clickable(
                                        interactionSource = interaction,
                                        indication = null,
                                        enabled = !disabled,
                                        role = Role.Button,
                                        onClick = { onDayClick(day) },
                                    )
                                    .semantics {
                                        contentDescription = formatKiraDate(day, KiraDateFormat.Long)
                                        selected = isSolid || isRangeMiddle
                                        if (disabled) this.disabled()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = day.day.toString(),
                                    style = (if (compact) KiraTheme.typography.body else KiraTheme.typography.bodySmall).copy(
                                        color = fg,
                                        fontWeight = if (isSolid || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                        textDecoration = if (disabled) TextDecoration.LineThrough else TextDecoration.None,
                                    ),
                                )
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = if (compact) 5.dp else 3.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSolid) colors.primaryForeground else colors.primary),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptionPillButton(
    text: String,
    contentDesc: String,
    active: Boolean = false,
    minHeight: Dp = 28.dp,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = when {
            active -> colors.background
            hovered -> colors.background.copy(alpha = 0.65f)
            else -> Color.Transparent
        },
        animationSpec = KiraMotion.fastTween(),
    )

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = minHeight)
            .clip(radius.shapeMd)
            .background(bg)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = contentDesc }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = KiraTheme.typography.caption.copy(
                color = if (active) colors.primary else colors.popoverForeground,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun MonthYearPickerOverlay(
    initialMode: MonthYearOverlayMode,
    currentMonth: KiraDate,
    today: KiraDate,
    compact: Boolean,
    minDate: KiraDate?,
    maxDate: KiraDate?,
    onPick: (KiraDate, closeAfter: Boolean) -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    var mode by remember { mutableStateOf(initialMode) }
    var displayYear by remember { mutableIntStateOf(currentMonth.year) }
    var startDecade by remember { mutableIntStateOf((currentMonth.year / 10) * 10) }
    val navSize = if (compact) 40.dp else 28.dp
    val cellHeight = if (compact) 46.dp else 40.dp

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 48.dp else 42.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CalendarNavButton(
                isNext = false,
                contentDesc = if (mode == MonthYearOverlayMode.Year) "Previous decade" else "Previous year",
                buttonSize = navSize,
                onClick = {
                    if (mode == MonthYearOverlayMode.Year) startDecade -= 10 else displayYear -= 1
                },
            )

            Row(
                modifier = Modifier
                    .clip(radius.shapeLg)
                    .background(colors.muted.copy(alpha = 0.5f))
                    .border(1.dp, colors.border, radius.shapeLg)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                CaptionPillButton(
                    text = "Month",
                    contentDesc = "Switch to month grid",
                    active = mode == MonthYearOverlayMode.Month,
                    minHeight = if (compact) 36.dp else 28.dp,
                    onClick = { mode = MonthYearOverlayMode.Month },
                )
                Box(Modifier.width(1.dp).height(16.dp).background(colors.border))
                CaptionPillButton(
                    text = if (mode == MonthYearOverlayMode.Year) "$startDecade–${startDecade + 9}" else displayYear.toString(),
                    contentDesc = "Switch to year grid",
                    active = mode == MonthYearOverlayMode.Year,
                    minHeight = if (compact) 36.dp else 28.dp,
                    onClick = { mode = MonthYearOverlayMode.Year },
                )
            }

            CalendarNavButton(
                isNext = true,
                contentDesc = if (mode == MonthYearOverlayMode.Year) "Next decade" else "Next year",
                buttonSize = navSize,
                onClick = {
                    if (mode == MonthYearOverlayMode.Year) startDecade += 10 else displayYear += 1
                },
            )
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            if (mode == MonthYearOverlayMode.Month) {
                (1..12).chunked(3).forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowMonths.forEach { m ->
                            val isSelected = displayYear == currentMonth.year && m == currentMonth.month
                            val isCurrent = displayYear == today.year && m == today.month
                            val monthStart = KiraDate(displayYear, m, 1)
                            val monthEnd = monthStart.endOfMonth()
                            val disabled = (minDate != null && monthEnd < minDate) || (maxDate != null && monthStart > maxDate)
                            MonthYearCell(
                                label = MONTH_NAMES_SHORT[m - 1],
                                selected = isSelected,
                                current = isCurrent,
                                outside = false,
                                disabled = disabled,
                                cellHeight = cellHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { onPick(KiraDate(displayYear, m, 1), true) },
                            )
                        }
                    }
                }
            } else {
                val years = ((startDecade - 1)..(startDecade + 10)).toList()
                years.chunked(3).forEach { rowYears ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowYears.forEach { y ->
                            val isSelected = y == currentMonth.year
                            val isCurrent = y == today.year
                            val isOutside = y < startDecade || y > startDecade + 9
                            val disabled = (minDate != null && y < minDate.year) || (maxDate != null && y > maxDate.year)
                            MonthYearCell(
                                label = y.toString(),
                                selected = isSelected,
                                current = isCurrent,
                                outside = isOutside,
                                disabled = disabled,
                                cellHeight = cellHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    displayYear = y
                                    if (initialMode == MonthYearOverlayMode.Year) {
                                        onPick(KiraDate(y, currentMonth.month, 1), true)
                                    } else {
                                        onPick(KiraDate(y, currentMonth.month, 1), false)
                                        mode = MonthYearOverlayMode.Month
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthYearCell(
    label: String,
    selected: Boolean,
    current: Boolean,
    outside: Boolean,
    disabled: Boolean,
    cellHeight: Dp = 40.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    val bg = when {
        selected -> colors.primary
        current -> colors.primary.copy(alpha = if (hovered) 0.12f else 0.06f)
        hovered && !disabled -> colors.muted
        else -> Color.Transparent
    }
    val fg = when {
        selected -> colors.primaryForeground
        current -> colors.primary
        outside -> colors.mutedForeground.copy(alpha = 0.5f)
        else -> colors.popoverForeground
    }
    val borderColor = when {
        selected -> colors.primary
        current -> colors.primary
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .height(cellHeight)
            .alpha(if (disabled) 0.4f else 1f)
            .clip(radius.shapeLg)
            .background(bg)
            .border(1.dp, borderColor, radius.shapeLg)
            .hoverable(interaction, enabled = !disabled)
            .pointerHoverIcon(if (!disabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = !disabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                this.selected = selected
                if (disabled) this.disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = KiraTheme.typography.caption.copy(
                color = fg,
                fontWeight = if (selected || current) FontWeight.Bold else FontWeight.Medium,
                textDecoration = if (disabled) TextDecoration.LineThrough else TextDecoration.None,
            ),
        )
    }
}

@Composable
private fun CalendarNavButton(
    isNext: Boolean,
    contentDesc: String,
    buttonSize: Dp = 28.dp,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered) colors.muted else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )

    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(radius.shapeMd)
            .background(bg)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center,
    ) {
        ChevronHorizontalGlyph(isRight = isNext, color = colors.mutedForeground, size = 16.dp)
    }
}

@Composable
private fun ConfirmFooterBar(
    previewText: String,
    canApply: Boolean,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    val colors = KiraTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = previewText,
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
            KiraButton(
                text = "Cancel",
                variant = KiraButtonVariant.Ghost,
                size = KiraButtonSize.Sm,
                onClick = onCancel,
            )
            KiraButton(
                text = "Apply",
                size = KiraButtonSize.Sm,
                enabled = canApply,
                onClick = onApply,
            )
        }
    }
}

@Composable
private fun DatePickerTriggerSurface(
    label: String?,
    placeholder: String,
    variant: KiraDatePickerVariant,
    size: KiraDatePickerSize,
    expanded: Boolean,
    enabled: Boolean,
    invalid: Boolean,
    clearable: Boolean,
    editable: Boolean,
    typedText: String,
    onTypedTextChange: (String) -> Unit,
    onCommitTyped: () -> Unit,
    ariaLabel: String,
    onToggle: () -> Unit,
    onClear: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val focused by interaction.collectIsFocusedAsState()

    val borderWidth = if (variant == KiraDatePickerVariant.Outline) 2.dp else 1.dp
    val borderColor by animateColorAsState(
        targetValue = when {
            invalid -> colors.destructive
            expanded || focused -> colors.ring
            variant == KiraDatePickerVariant.Ghost -> Color.Transparent
            variant == KiraDatePickerVariant.Outline && hovered -> colors.primary.copy(alpha = 0.55f)
            else -> colors.input
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            variant == KiraDatePickerVariant.Outline -> Color.Transparent
            hovered && enabled -> colors.muted.copy(alpha = 0.5f)
            variant == KiraDatePickerVariant.Ghost -> Color.Transparent
            else -> colors.background
        },
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = size.minHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(radius.shapeLg)
            .background(bgColor)
            .border(borderWidth, borderColor, radius.shapeLg)
            .hoverable(interaction, enabled = enabled)
            .then(
                if (!editable) {
                    Modifier
                        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = enabled,
                            role = Role.Button,
                            onClick = onToggle,
                        )
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = !editable) {
                contentDescription = "$ariaLabel: ${label ?: "not selected"}"
                stateDescription = if (expanded) "Expanded" else "Collapsed"
                if (!enabled) disabled()
                if (invalid) error("Invalid date")
            }
            .padding(horizontal = size.horizontalPadding, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (editable) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (typedText.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = KiraTheme.typography.body.copy(
                            fontSize = size.fontSize.sp,
                            color = colors.mutedForeground,
                        ),
                    )
                }
                BasicTextField(
                    value = typedText,
                    onValueChange = onTypedTextChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = KiraTheme.typography.body.copy(
                        fontSize = size.fontSize.sp,
                        color = colors.foreground,
                    ),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onCommitTyped() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused && typedText.isNotBlank()) onCommitTyped() }
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when {
                                    event.key == Key.Enter -> {
                                        onCommitTyped()
                                        true
                                    }
                                    event.isAltPressed && event.key == Key.DirectionDown -> {
                                        onToggle()
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
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(radius.shapeMd)
                    .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                    .clickable(enabled = enabled, role = Role.Button, onClick = onToggle)
                    .semantics { contentDescription = "Open calendar" },
                contentAlignment = Alignment.Center,
            ) {
                CalendarIcon(color = colors.mutedForeground, size = size.iconSize)
            }
        } else {
            CalendarIcon(color = colors.mutedForeground, size = size.iconSize)
            BasicText(
                text = label ?: placeholder,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = KiraTheme.typography.body.copy(
                    fontSize = size.fontSize.sp,
                    color = if (label != null) colors.foreground else colors.mutedForeground,
                ),
            )
        }

        if (clearable && enabled) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(radius.shapeFull)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(role = Role.Button, onClick = onClear)
                    .semantics { contentDescription = "Clear date" },
                contentAlignment = Alignment.Center,
            ) {
                ClearGlyph(color = colors.mutedForeground, size = 12.dp)
            }
        }
    }
}

@Composable
private fun DatePickerPopoverHost(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sheetTitle: String,
    sheetSubtitle: String,
    modifier: Modifier = Modifier,
    trigger: @Composable () -> Unit,
    mobileContent: @Composable ColumnScope.() -> Unit,
    popupContent: @Composable (windowWidthDp: Dp) -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val density = LocalDensity.current
    val windowWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    val isCompactViewport = windowWidthDp > 0.dp && windowWidthDp < 640.dp

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = expanded && !isCompactViewport

    val positionProvider = remember(density) {
        DatePopupPositionProvider(
            gapPx = with(density) { 8.dp.roundToPx() },
            marginPx = with(density) { 8.dp.roundToPx() },
        )
    }

    Box(modifier = modifier) {
        trigger()
        if (isCompactViewport) {
            KiraDrawer(
                open = expanded,
                onDismissRequest = onDismissRequest,
                side = KiraDrawerSide.Bottom,
                size = KiraDrawerSize.Lg,
                swipeToDismiss = true,
                showCloseButton = true,
                paneTitle = sheetTitle,
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
                        text = sheetTitle,
                        modifier = Modifier.semantics { heading() },
                        style = KiraTheme.typography.heading3.copy(color = colors.foreground),
                    )
                    BasicText(
                        text = sheetSubtitle,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                mobileContent()
            }
        } else if (transitionState.currentState || transitionState.targetState) {
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = fadeIn(tween(150, easing = KiraMotion.EaseOutStrong)) +
                        scaleIn(tween(150, easing = KiraMotion.EaseOutStrong), initialScale = 0.95f),
                    exit = fadeOut(tween(130, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(130, easing = KiraMotion.EaseOutStrong), targetScale = 0.95f),
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(12.dp, radius.shapeXl)
                            .clip(radius.shapeXl)
                            .background(colors.popover)
                            .border(1.dp, colors.border, radius.shapeXl)
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                                    onDismissRequest()
                                    true
                                } else {
                                    false
                                }
                            },
                    ) {
                        popupContent(windowWidthDp)
                    }
                }
            }
        }
    }
}

private class DatePopupPositionProvider(
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
private fun CalendarIcon(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.11f).coerceAtLeast(1.3.dp.toPx())
        val pad = this.size.minDimension * 0.14f
        val top = this.size.height * 0.24f
        val rectW = this.size.width - pad * 2f
        val rectH = this.size.height - top - pad
        drawRoundRect(
            color = color,
            topLeft = Offset(pad, top),
            size = Size(rectW, rectH),
            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
            style = Stroke(width = stroke),
        )
        drawLine(color, Offset(pad, top + rectH * 0.34f), Offset(this.size.width - pad, top + rectH * 0.34f), strokeWidth = stroke)
        drawLine(color, Offset(this.size.width * 0.34f, pad), Offset(this.size.width * 0.34f, top + stroke), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(this.size.width * 0.66f, pad), Offset(this.size.width * 0.66f, top + stroke), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun ChevronHorizontalGlyph(isRight: Boolean, color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = (this.size.minDimension * 0.13f).coerceAtLeast(1.4.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            if (isRight) {
                moveTo(this@Canvas.size.width * 0.36f, this@Canvas.size.height * 0.22f)
                lineTo(this@Canvas.size.width * 0.66f, this@Canvas.size.height * 0.50f)
                lineTo(this@Canvas.size.width * 0.36f, this@Canvas.size.height * 0.78f)
            } else {
                moveTo(this@Canvas.size.width * 0.64f, this@Canvas.size.height * 0.22f)
                lineTo(this@Canvas.size.width * 0.34f, this@Canvas.size.height * 0.50f)
                lineTo(this@Canvas.size.width * 0.64f, this@Canvas.size.height * 0.78f)
            }
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun ClearGlyph(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.15f).coerceAtLeast(1.3.dp.toPx())
        val pad = this.size.minDimension * 0.18f
        drawLine(color, Offset(pad, pad), Offset(this.size.width - pad, this.size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(this.size.width - pad, pad), Offset(pad, this.size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
