package com.kirakit.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.kirakit.components.accordion.KiraAccordionItemData
import com.kirakit.components.accordion.filterAccordionItems
import com.kirakit.components.accordion.toggleAccordionValue
import com.kirakit.components.alert.formatAlertRelativeSeconds
import com.kirakit.components.alertdialog.isAlertDialogConfirmEnabled
import com.kirakit.components.avatar.getAvatarInitials
import com.kirakit.components.badge.KiraBadgeCountFormat
import com.kirakit.components.badge.formatBadgeCount
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.checkbox.KiraCheckboxState
import com.kirakit.components.checkbox.resolveSelectAllState
import com.kirakit.components.contextmenu.resolveContextMenuOffset
import com.kirakit.components.datepicker.KiraDate
import com.kirakit.components.datepicker.KiraDateFormat
import com.kirakit.components.datepicker.dayOfWeekIndex
import com.kirakit.components.datepicker.daysInMonth
import com.kirakit.components.datepicker.formatKiraDate
import com.kirakit.components.datepicker.isKiraDateDisabled
import com.kirakit.components.datepicker.isLeapYear
import com.kirakit.components.datepicker.parseKiraDate
import com.kirakit.components.drawer.resolveDrawerSnapIndex
import com.kirakit.components.dropdownmenu.KiraMenuItemData
import com.kirakit.components.dropdownmenu.filterKiraMenuItems
import com.kirakit.components.label.formatLabelCounter
import com.kirakit.components.menubar.KiraMenubarMenuData
import com.kirakit.components.menubar.collectMenubarCommands
import com.kirakit.components.navigationmenu.KiraNavigationItemData
import com.kirakit.components.navigationmenu.findActiveNavigationGroupId
import com.kirakit.components.navigationmenu.flattenNavigationLinks
import com.kirakit.components.popover.KiraPopoverSide
import com.kirakit.components.popover.resolvePopoverSide
import com.kirakit.components.popover.shouldGuardPopoverDismissal
import com.kirakit.components.progress.KiraProgressPhase
import com.kirakit.components.progress.KiraProgressSegment
import com.kirakit.components.progress.computeWeightedPhaseProgress
import com.kirakit.components.progress.normalizeProgress
import com.kirakit.components.progress.normalizeProgressSegments
import com.kirakit.components.radiogroup.KiraRadioOption
import com.kirakit.components.radiogroup.resolveRadioSelection
import com.kirakit.components.select.matchesSelectQuery
import com.kirakit.components.separator.normalizeSeparatorCount
import com.kirakit.components.sheet.KiraSheetSide
import com.kirakit.components.sheet.resolveSheetSide
import com.kirakit.components.skeleton.clampSkeletonCount
import com.kirakit.components.slider.buildSliderMarkers
import com.kirakit.components.slider.snapSliderValue
import com.kirakit.components.spotlight.KiraSpotlightItem
import com.kirakit.components.spotlight.filterSpotlightItems
import com.kirakit.components.spotlight.recordSpotlightRecent
import com.kirakit.components.stepper.KiraStepState
import com.kirakit.components.stepper.calculateStepperProgress
import com.kirakit.components.stepper.canNavigateToStep
import com.kirakit.components.stepper.resolveStepState
import com.kirakit.components.tabs.KiraTabItem
import com.kirakit.components.tabs.resolveNextTabAfterClose
import com.kirakit.components.textarea.evaluateTextareaCounter
import com.kirakit.components.timepicker.KiraTime
import com.kirakit.components.timepicker.KiraTimeGranularity
import com.kirakit.components.timepicker.KiraTimeHourCycle
import com.kirakit.components.timepicker.KiraTimeUnit
import com.kirakit.components.timepicker.formatKiraTime
import com.kirakit.components.timepicker.isKiraTimeAllowed
import com.kirakit.components.timepicker.parseKiraTime
import com.kirakit.components.timepicker.stepKiraTimeUnit
import com.kirakit.components.toast.KiraToastController
import com.kirakit.components.toast.KiraToastDurations
import com.kirakit.components.toast.KiraToastVariant
import com.kirakit.components.toast.resolveToastTimeoutMillis
import com.kirakit.components.togglegroup.resolveToggleGroupSelection
import com.kirakit.components.tooltip.KiraTooltipSide
import com.kirakit.components.tooltip.resolveTooltipSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KiraComponentsTest {
    @Test
    fun buttonSizesMeetMinimumTouchTarget() {
        KiraButtonSize.entries.forEach { size ->
            assertTrue(
                size.minHeight.value >= 44f,
                "Expected ${size.name} minHeight >= 44.dp, got ${size.minHeight}",
            )
        }
    }

    @Test
    fun badgeCountFormatsDefaultShortAndCommas() {
        assertEquals("42", formatBadgeCount(42))
        assertEquals("99+", formatBadgeCount(150, max = 99))
        assertEquals("1.5k+", formatBadgeCount(1_500, format = KiraBadgeCountFormat.Short))
        assertEquals("2M+", formatBadgeCount(2_000_000, format = KiraBadgeCountFormat.Short))
        assertEquals("1,250", formatBadgeCount(1_250, format = KiraBadgeCountFormat.Commas, max = 9_999))
        assertEquals("9,999+", formatBadgeCount(12_000, format = KiraBadgeCountFormat.Commas, max = 9_999))
    }

    @Test
    fun labelCounterCapsAtMax() {
        assertEquals("12", formatLabelCounter(12))
        assertEquals("99", formatLabelCounter(99))
        assertEquals("99+", formatLabelCounter(150))
        assertEquals("9+", formatLabelCounter(10, max = 9))
    }

    @Test
    fun checkboxSelectAllResolvesTriState() {
        assertEquals(KiraCheckboxState.Unchecked, resolveSelectAllState(0, 3))
        assertEquals(KiraCheckboxState.Indeterminate, resolveSelectAllState(1, 3))
        assertEquals(KiraCheckboxState.Checked, resolveSelectAllState(3, 3))
        assertEquals(KiraCheckboxState.Unchecked, resolveSelectAllState(0, 0))
    }

    @Test
    fun accordionToggleAndRecursiveSearchFilterWork() {
        assertEquals(setOf("a"), toggleAccordionValue(emptySet(), "a", multiple = false))
        assertEquals(emptySet(), toggleAccordionValue(setOf("a"), "a", multiple = false, collapsible = true))
        assertEquals(setOf("a"), toggleAccordionValue(setOf("a"), "a", multiple = false, collapsible = false))
        assertEquals(setOf("a", "b"), toggleAccordionValue(setOf("a"), "b", multiple = true))

        val items = listOf(
            KiraAccordionItemData(
                value = "parent",
                title = "Getting Started",
                content = {},
                children = listOf(
                    KiraAccordionItemData(value = "child-1", title = "Design Tokens", content = {}),
                    KiraAccordionItemData(value = "child-2", title = "Installation", content = {}),
                ),
            ),
            KiraAccordionItemData(value = "other", title = "Accessibility", content = {}),
        )
        val filtered = filterAccordionItems(items, "token")
        assertEquals(1, filtered.size)
        assertEquals("parent", filtered.first().value)
        assertEquals(1, filtered.first().children.size)
        assertEquals("child-1", filtered.first().children.first().value)
    }

    @Test
    fun drawerSnapIndexResolvesNearestAndDismissesBelowThreshold() {
        val snaps = listOf(0.4f, 0.85f)
        assertEquals(-1, resolveDrawerSnapIndex(0.15f, snaps))
        assertEquals(0, resolveDrawerSnapIndex(0.45f, snaps))
        assertEquals(1, resolveDrawerSnapIndex(0.75f, snaps))
    }

    @Test
    fun tooltipSideFlipsWhenInsufficientSpace() {
        val window = IntSize(400, 800)
        val popup = IntSize(120, 40)
        assertEquals(
            KiraTooltipSide.Bottom,
            resolveTooltipSide(
                preferred = KiraTooltipSide.Top,
                anchorBounds = IntRect(150, 10, 250, 50),
                windowSize = window,
                popupSize = popup,
                gapPx = 8,
                paddingPx = 8,
            ),
        )
        assertEquals(
            KiraTooltipSide.Top,
            resolveTooltipSide(
                preferred = KiraTooltipSide.Top,
                anchorBounds = IntRect(150, 200, 250, 240),
                windowSize = window,
                popupSize = popup,
                gapPx = 8,
                paddingPx = 8,
            ),
        )
    }

    @Test
    fun alertRelativeTimeFormatsCorrectly() {
        assertEquals("just now", formatAlertRelativeSeconds(12))
        assertEquals("5m ago", formatAlertRelativeSeconds(300))
        assertEquals("2h ago", formatAlertRelativeSeconds(7_200))
        assertEquals("3d ago", formatAlertRelativeSeconds(259_200))
    }

    @Test
    fun alertDialogConfirmGateChecksTextDelayAndLoading() {
        assertFalse(isAlertDialogConfirmEnabled("DELETE", "DEL", 0, false))
        assertFalse(isAlertDialogConfirmEnabled("DELETE", "DELETE", 2, false))
        assertFalse(isAlertDialogConfirmEnabled("DELETE", "DELETE", 0, true))
        assertTrue(isAlertDialogConfirmEnabled("DELETE", "DELETE", 0, false))
        assertTrue(isAlertDialogConfirmEnabled(null, "", 0, false))
    }

    @Test
    fun tabsResolveNextSelectionAfterClose() {
        val tabs = listOf(
            KiraTabItem("a", "A"),
            KiraTabItem("b", "B"),
            KiraTabItem("c", "C"),
        )
        assertEquals("c", resolveNextTabAfterClose(tabs, closingValue = "b", activeValue = "b"))
        assertEquals("b", resolveNextTabAfterClose(tabs, closingValue = "c", activeValue = "c"))
        assertEquals("a", resolveNextTabAfterClose(tabs, closingValue = "c", activeValue = "a"))
    }

    @Test
    fun selectMatchesExactAndFuzzySubsequence() {
        assertTrue(matchesSelectQuery("Kotlin Multiplatform", "kmp", fuzzy = true))
        assertFalse(matchesSelectQuery("Kotlin Multiplatform", "kmp", fuzzy = false))
        assertTrue(matchesSelectQuery("Kotlin Multiplatform", "multi", fuzzy = false))
        assertFalse(matchesSelectQuery("SwiftUI", "kmp", fuzzy = true))
    }

    @Test
    fun avatarInitialsHandleSingleMultiAndBlankNames() {
        assertEquals("JD", getAvatarInitials("John Doe"))
        assertEquals("AL", getAvatarInitials("Alice"))
        assertEquals("MK", getAvatarInitials("Mary Jane Watson Kelly"))
        assertEquals("?", getAvatarInitials("   "))
    }

    @Test
    fun toggleGroupSelectionEnforcesSingleAndAllowEmptyRules() {
        // One-selected-always by default
        assertEquals(setOf("day"), resolveToggleGroupSelection(setOf("day"), "day", multiple = false, allowEmpty = false))
        assertEquals(setOf("week"), resolveToggleGroupSelection(setOf("day"), "week", multiple = false, allowEmpty = false))
        // Opt-in allowEmpty
        assertEquals(emptySet(), resolveToggleGroupSelection(setOf("day"), "day", multiple = false, allowEmpty = true))
        // Multi-select
        assertEquals(setOf("bold", "italic"), resolveToggleGroupSelection(setOf("bold"), "italic", multiple = true))
        assertEquals(emptySet(), resolveToggleGroupSelection(setOf("bold"), "bold", multiple = true))
    }

    @Test
    fun toastControllerDeduplicatesUpdatesAndResolvesTimeouts() {
        assertEquals(KiraToastDurations.Success, resolveToastTimeoutMillis(KiraToastVariant.Success, null))
        assertEquals(0L, resolveToastTimeoutMillis(KiraToastVariant.Loading, null))
        assertEquals(0L, resolveToastTimeoutMillis(KiraToastVariant.Warning, Long.MAX_VALUE))
        assertEquals(2_000L, resolveToastTimeoutMillis(KiraToastVariant.Default, 2_000L))

        val controller = KiraToastController()
        val id = controller.loading("Uploading…", id = "upload-1")
        assertEquals(1, controller.toasts.size)
        controller.update(id, title = "Uploaded!", variant = KiraToastVariant.Success)
        assertEquals(1, controller.toasts.size)
        assertEquals("Uploaded!", controller.toasts.first().title)
        assertEquals(KiraToastVariant.Success, controller.toasts.first().variant)
        assertEquals(KiraToastDurations.Success, controller.toasts.first().durationMillis)
        controller.dismiss(id)
        assertTrue(controller.toasts.isEmpty())
    }

    @Test
    fun textareaCounterEvaluatesWarningAndOverLimit() {
        val ok = evaluateTextareaCounter(length = 50, maxLength = 100)
        assertFalse(ok.isWarning)
        assertFalse(ok.isOverLimit)
        assertEquals(50, ok.remaining)

        val warn = evaluateTextareaCounter(length = 85, maxLength = 100, warningThreshold = 0.8f)
        assertTrue(warn.isWarning)
        assertFalse(warn.isOverLimit)

        val over = evaluateTextareaCounter(length = 105, maxLength = 100)
        assertFalse(over.isWarning)
        assertTrue(over.isOverLimit)
        assertEquals(-5, over.remaining)
    }

    @Test
    fun sliderSnapsValuesAndBuildsStepMarkers() {
        assertEquals(25f, snapSliderValue(23f, min = 0f, max = 100f, step = 25f))
        assertEquals(40f, snapSliderValue(32f, min = 0f, max = 100f, stepOptions = listOf(0f, 10f, 40f, 90f, 100f)))
        assertEquals(100f, snapSliderValue(150f, min = 0f, max = 100f, step = 10f))

        val autoMarkers = buildSliderMarkers(null, showStepMarkers = true, stepOptions = emptyList(), min = 0f, max = 100f, step = 25f)
        assertEquals(listOf(25f, 50f, 75f), autoMarkers.map { it.value })

        val sparseMarkers = buildSliderMarkers(null, showStepMarkers = false, stepOptions = listOf(0f, 10f, 40f, 90f, 100f), min = 0f, max = 100f, step = 1f)
        assertEquals(listOf(10f, 40f, 90f), sparseMarkers.map { it.value })
    }

    @Test
    fun datePickerCalendarArithmeticParsingAndConstraintsWork() {
        assertTrue(isLeapYear(2024))
        assertFalse(isLeapYear(2026))
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(28, daysInMonth(2026, 2))

        val date = KiraDate(2026, 11, 24)
        assertEquals(2, dayOfWeekIndex(date)) // Tuesday
        assertEquals(KiraDate(2026, 12, 1), date.plusDays(7))
        assertEquals(KiraDate(2026, 10, 24), date.minusMonths(1))
        assertEquals("2026-11-24", formatKiraDate(date, KiraDateFormat.Iso))
        assertEquals("Nov 24, 2026", formatKiraDate(date, KiraDateFormat.Medium))
        assertEquals(date, parseKiraDate("2026-11-24"))
        assertEquals(date, parseKiraDate("11/24/2026"))
        assertEquals(null, parseKiraDate("2026-02-30"))

        assertTrue(isKiraDateDisabled(KiraDate(2026, 11, 23), minDate = date))
        assertFalse(isKiraDateDisabled(date, minDate = date, maxDate = date.plusDays(7)))
        assertTrue(isKiraDateDisabled(date, disabledDates = setOf(date)))
    }

    @Test
    fun timePickerParsingFormattingConstraintsAndSteppingWork() {
        val parsed = parseKiraTime("14:35:08")
        assertEquals(KiraTime(14, 35, 8), parsed)
        assertEquals(null, parseKiraTime("25:00"))
        assertEquals("14:35", formatKiraTime(KiraTime(14, 35), KiraTimeGranularity.Minute, KiraTimeHourCycle.Hour24))
        assertEquals("2:35:08 PM", formatKiraTime(KiraTime(14, 35, 8), KiraTimeGranularity.Second, KiraTimeHourCycle.Hour12))

        val blocked = setOf(KiraTime(12, 0))
        assertFalse(isKiraTimeAllowed(KiraTime(8, 30), minTime = KiraTime(9, 0), maxTime = KiraTime(18, 0)))
        assertFalse(isKiraTimeAllowed(KiraTime(12, 0), minTime = KiraTime(9, 0), maxTime = KiraTime(18, 0), disabledTimes = blocked))
        assertTrue(isKiraTimeAllowed(KiraTime(12, 15), minTime = KiraTime(9, 0), maxTime = KiraTime(18, 0), disabledTimes = blocked))

        // Stepping skips disabled 12:00 and jumps to 14:00 with hourStep = 2
        val stepped = stepKiraTimeUnit(
            current = KiraTime(10, 0),
            unit = KiraTimeUnit.Hour,
            delta = 1,
            step = 2,
            minTime = KiraTime(9, 0),
            maxTime = KiraTime(18, 0),
            disabledTimes = blocked,
        )
        assertEquals(KiraTime(14, 0), stepped)
    }

    @Test
    fun dropdownMenuFilterAndMenubarCommandCollectionWork() {
        val menuItems = listOf(
            KiraMenuItemData.Action("copy", "Copy link"),
            KiraMenuItemData.Submenu(
                id = "export",
                label = "Export",
                items = listOf(
                    KiraMenuItemData.Action("pdf", "Export PDF"),
                    KiraMenuItemData.Action("csv", "Export CSV"),
                ),
            ),
            KiraMenuItemData.Separator("sep"),
        )
        val filtered = filterKiraMenuItems(menuItems, "csv")
        assertEquals(1, filtered.size)
        val sub = filtered.first() as KiraMenuItemData.Submenu
        assertEquals(1, sub.items.size)
        assertEquals("csv", sub.items.first().id)

        val menubar = listOf(
            KiraMenubarMenuData(id = "file", label = "File", items = menuItems),
            KiraMenubarMenuData(
                id = "disabled",
                label = "Admin",
                disabled = true,
                items = listOf(KiraMenuItemData.Action("wipe", "Wipe")),
            ),
        )
        val commands = collectMenubarCommands(menubar)
        assertEquals(1, commands.size)
        assertEquals("commands/file", commands.first().id)
    }

    @Test
    fun navigationMenuResolvesActiveGroupAndFlattensLinks() {
        val nav = listOf(
            KiraNavigationItemData.Group(
                id = "product",
                label = "Product",
                items = listOf(
                    KiraNavigationItemData.Group(
                        id = "work",
                        label = "Work",
                        items = listOf(
                            KiraNavigationItemData.Link("projects", "Projects", "#projects"),
                            KiraNavigationItemData.Link("docs", "Documents", "#docs"),
                        ),
                    ),
                ),
            ),
            KiraNavigationItemData.Link("changelog", "Changelog", "#changelog"),
        )
        assertEquals(listOf("projects", "docs", "changelog"), flattenNavigationLinks(nav).map { it.id })
        assertEquals("product", findActiveNavigationGroupId(nav, "#docs"))
        assertEquals(null, findActiveNavigationGroupId(nav, "#changelog"))
    }

    @Test
    fun progressNormalizesBoundsSegmentsAndWeightedPhases() {
        val clamped = normalizeProgress(value = 150f, min = 0f, max = 100f)
        assertEquals(100f, clamped.value)
        assertEquals(1f, clamped.fraction)
        assertEquals("100%", clamped.formatted)
        assertTrue(clamped.isComplete)

        val indeterminate = normalizeProgress(value = null)
        assertEquals(null, indeterminate.value)
        assertEquals(null, indeterminate.formatted)
        assertFalse(indeterminate.isComplete)

        val segments = normalizeProgressSegments(
            listOf(
                KiraProgressSegment("a", 60f),
                KiraProgressSegment("b", 60f),
            ),
            total = 100f,
        )
        assertEquals(0.6f, segments[0].second)
        assertEquals(0.4f, segments[1].second)

        val weighted = computeWeightedPhaseProgress(
            listOf(
                KiraProgressPhase("p1", "Receive", weight = 1f, value = 100f),
                KiraProgressPhase("p2", "Transform", weight = 3f, value = 50f),
                KiraProgressPhase("p3", "Write", weight = 1f, value = 0f),
            ),
        )
        assertEquals(50f, weighted)
        assertEquals(
            null,
            computeWeightedPhaseProgress(
                listOf(
                    KiraProgressPhase("p1", "Receive", weight = 1f, value = 100f),
                    KiraProgressPhase("p2", "Transform", weight = 3f, value = null),
                ),
            ),
        )
    }

    @Test
    fun radioGroupSeparatorSkeletonAndSheetHelpersWork() {
        val options = listOf(
            KiraRadioOption("a", "A"),
            KiraRadioOption("b", "B", disabled = true),
            KiraRadioOption("c", "C", disabledReason = "Requires plan"),
        )
        assertEquals("a", resolveRadioSelection(options, null, "a"))
        assertEquals("a", resolveRadioSelection(options, "a", "b"))
        assertEquals("a", resolveRadioSelection(options, "a", "c"))

        assertEquals(0, normalizeSeparatorCount(-5))
        assertEquals(12, normalizeSeparatorCount(12))
        assertEquals(null, normalizeSeparatorCount(null))

        assertEquals(0, clampSkeletonCount(-3))
        assertEquals(100, clampSkeletonCount(500))
        assertEquals(4, clampSkeletonCount(4))

        assertEquals(
            KiraSheetSide.Bottom,
            resolveSheetSide(390f, responsive = true, side = KiraSheetSide.Right, mobileSide = KiraSheetSide.Bottom),
        )
        assertEquals(
            KiraSheetSide.Right,
            resolveSheetSide(1024f, responsive = true, side = KiraSheetSide.Right, mobileSide = KiraSheetSide.Bottom),
        )
        assertEquals(
            KiraSheetSide.Left,
            resolveSheetSide(390f, responsive = false, side = KiraSheetSide.Left, mobileSide = KiraSheetSide.Bottom),
        )
    }

    @Test
    fun popoverContextMenuStepperAndSpotlightHelpersWork() {
        assertTrue(shouldGuardPopoverDismissal(nextOpen = false, dirty = true, preventClose = false))
        assertTrue(shouldGuardPopoverDismissal(nextOpen = false, dirty = false, preventClose = true))
        assertFalse(shouldGuardPopoverDismissal(nextOpen = false, dirty = true, preventClose = false, bypass = true))
        assertEquals(
            KiraPopoverSide.Top,
            resolvePopoverSide(
                preferred = KiraPopoverSide.Bottom,
                anchorBounds = IntRect(100, 740, 220, 780),
                windowSize = IntSize(400, 800),
                popupSize = IntSize(200, 120),
                gapPx = 8,
                marginPx = 8,
            ),
        )

        val ctxOffset = resolveContextMenuOffset(
            anchorBounds = IntRect(100, 100, 300, 200),
            localClickOffset = IntOffset(280, 90),
            windowSize = IntSize(400, 300),
            popupSize = IntSize(180, 140),
            marginPx = 8,
        )
        assertEquals(IntOffset(212, 152), ctxOffset)

        assertEquals(KiraStepState.Loading, resolveStepState(1, 1, loadingSteps = setOf(1)))
        assertEquals(KiraStepState.Error, resolveStepState(0, 1, errorSteps = setOf(0)))
        assertEquals(KiraStepState.Completed, resolveStepState(0, 2))
        assertTrue(canNavigateToStep(1, activeStep = 0, totalSteps = 3, completedSteps = setOf(0), linear = true))
        assertFalse(canNavigateToStep(2, activeStep = 0, totalSteps = 3, completedSteps = setOf(0), linear = true))
        assertEquals(0.5f, calculateStepperProgress(1, 3))

        val items = listOf(
            KiraSpotlightItem<String>("roadmap", "Q4 roadmap", scope = "projects", keywords = listOf("launch")),
            KiraSpotlightItem<String>("guide", "Onboarding guide", scope = "docs"),
        )
        assertEquals(listOf("roadmap"), filterSpotlightItems(items, "launch", scope = "projects").map { it.id })
        val recents = recordSpotlightRecent(listOf(items[0], items[1]), items[1], maxRecent = 2)
        assertEquals(listOf("guide", "roadmap"), recents.map { it.id })
    }
}
