package com.kirakit.gallery.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.badge.KiraBadge
import com.kirakit.components.badge.KiraBadgeVariant
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonExpandDirection
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonTooltipSide
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.button.KiraExpandableIconButton
import com.kirakit.components.button.KiraIconButtonWithTooltip
import com.kirakit.components.button.KiraLoadingButton
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.gallery.galleryHeading
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val basicVariants = listOf(
    KiraButtonVariant.Primary,
    KiraButtonVariant.Secondary,
    KiraButtonVariant.Outline,
    KiraButtonVariant.Ghost,
    KiraButtonVariant.Link,
)
private val semanticVariants = listOf(
    KiraButtonVariant.Destructive,
    KiraButtonVariant.Info,
    KiraButtonVariant.Success,
    KiraButtonVariant.Warning,
)
private val outlineVariants = listOf(
    KiraButtonVariant.DestructiveOutline,
    KiraButtonVariant.InfoOutline,
    KiraButtonVariant.SuccessOutline,
    KiraButtonVariant.WarningOutline,
)
private val ghostVariants = listOf(
    KiraButtonVariant.DestructiveGhost,
    KiraButtonVariant.InfoGhost,
    KiraButtonVariant.SuccessGhost,
    KiraButtonVariant.WarningGhost,
)

private val KiraButtonVariant.demoName: String
    get() = if (this == KiraButtonVariant.Primary) "Default" else name

internal fun LazyListScope.buttonPlaygroundItems() {
    galleryHeading("Variants & Sizes")
    galleryItem("btn-variants") { VariantsSection() }
    galleryItem("btn-sizes") { SizesSection() }

    galleryHeading("Icons")
    galleryItem("btn-with-icons") { WithIconsSection() }
    galleryItem("btn-icon-props") { IconPropsSection() }

    galleryHeading("Interactive Behaviors")
    galleryItem("btn-hold") { HoldToConfirmSection() }
    galleryItem("btn-expandable") { ExpandableSection() }
    galleryItem("btn-toggle") { OptimisticToggleSection() }

    galleryHeading("Loading, Tooltips & States")
    galleryItem("btn-loading") { LoadingSection() }
    galleryItem("btn-tooltip") { TooltipSection() }
    galleryItem("btn-disabled") { DisabledSection() }

    galleryHeading("Unified Component")
    galleryItem("btn-unified") { KiraButtonUnifiedSection() }
}

@Composable
internal fun ButtonPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.md)) {
        SectionHeading("Variants & Sizes")
        VariantsSection()
        SizesSection()

        SectionHeading("Icons")
        WithIconsSection()
        IconPropsSection()

        SectionHeading("Interactive Behaviors")
        HoldToConfirmSection()
        ExpandableSection()
        OptimisticToggleSection()

        SectionHeading("Loading, Tooltips & States")
        LoadingSection()
        TooltipSection()
        DisabledSection()

        SectionHeading("Unified Component")
        KiraButtonUnifiedSection()
    }
}

@Composable
private fun VariantsSection() {
    GalleryExample("Button Variants") {
        VariantSubSection("Basic", basicVariants)
        VariantSubSection("Semantic", semanticVariants)
        VariantSubSection("Outline", outlineVariants) { it.demoName.removeSuffix("Outline") }
        VariantSubSection("Ghost", ghostVariants) { it.demoName.removeSuffix("Ghost") }
    }
}

@Composable
private fun SizesSection() {
    GalleryExample("Size Variants") {
        SubSection("Text Buttons") {
            ButtonExampleRow {
                KiraButton("Extra Small", size = KiraButtonSize.Xs)
                KiraButton("Small", size = KiraButtonSize.Sm)
                KiraButton("Default")
                KiraButton("Large", size = KiraButtonSize.Lg)
                KiraButton("Extra Large", size = KiraButtonSize.Xl)
            }
        }
        SubSection("Icon Buttons") {
            ButtonExampleRow {
                listOf(
                    KiraButtonSize.IconXs to "Extra small",
                    KiraButtonSize.IconSm to "Small",
                    KiraButtonSize.Icon to "Default",
                    KiraButtonSize.IconLg to "Large",
                    KiraButtonSize.IconXl to "Extra large",
                ).forEach { (size, label) ->
                    KiraButton(
                        modifier = Modifier.semantics { contentDescription = "Add, ${label.lowercase()} icon" },
                        size = size,
                        variant = KiraButtonVariant.Outline,
                        leftIcon = ButtonDemoIcon.Add.slot,
                    ) {}
                }
            }
        }
        SubSection("Full Width") {
            KiraButton("Full-width button", fullWidth = true, variant = KiraButtonVariant.Outline)
        }
    }
}

@Composable
private fun WithIconsSection() {
    GalleryExample("Buttons with Icons") {
        ButtonExampleRow {
            KiraButton("Send Email", leftIcon = ButtonDemoIcon.Mail.slot)
            KiraButton("Download", variant = KiraButtonVariant.Secondary, leftIcon = ButtonDemoIcon.Download.slot)
            KiraButton("Settings", variant = KiraButtonVariant.Outline, leftIcon = ButtonDemoIcon.Settings.slot)
            KiraButton("Confirm", variant = KiraButtonVariant.Success, leftIcon = ButtonDemoIcon.Check.slot)
            KiraButton("Delete", variant = KiraButtonVariant.Destructive, leftIcon = ButtonDemoIcon.Delete.slot)
        }
    }
}

@Composable
private fun IconPropsSection() {
    GalleryExample("Icon Props API") {
        SubSection("Leading and trailing icons") {
            ButtonExampleRow {
                KiraButton("Send Email", leftIcon = ButtonDemoIcon.Mail.slot)
                KiraButton("Download", variant = KiraButtonVariant.Secondary, leftIcon = ButtonDemoIcon.Download.slot)
                KiraButton("Continue", variant = KiraButtonVariant.Outline, rightIcon = ButtonDemoIcon.Next.slot)
                KiraButton("Back", variant = KiraButtonVariant.Ghost, leftIcon = ButtonDemoIcon.Back.slot)
            }
        }
        SubSection("Both icons") {
            ButtonExampleRow {
                KiraButton(
                    "Sync & Continue",
                    leftIcon = ButtonDemoIcon.Sync.slot,
                    rightIcon = ButtonDemoIcon.Next.slot,
                )
                KiraButton(
                    "Upload Files",
                    variant = KiraButtonVariant.Secondary,
                    leftIcon = ButtonDemoIcon.Upload.slot,
                    rightIcon = ButtonDemoIcon.Next.slot,
                )
            }
        }
        SubSection("Custom content") {
            ButtonExampleRow {
                KiraButton(variant = KiraButtonVariant.Outline) { style ->
                    ButtonDemoIcon.Settings.slot(style.color, KiraButtonSize.Md.iconSize)
                    BasicText("Custom Layout", style = style)
                }
                KiraButton(variant = KiraButtonVariant.Success, leftIcon = ButtonDemoIcon.Check.slot) { style ->
                    BasicText("Success with Badge", style = style)
                    KiraBadge("3", variant = KiraBadgeVariant.Secondary)
                }
            }
        }
    }
}

@Composable
private fun HoldToConfirmSection() {
    var result by rememberSaveable { mutableStateOf("") }

    GalleryExample("Hold to Confirm") {
        ExampleNote("Press and hold, or hold Space or Enter. Release early to cancel.")
        ButtonExampleRow {
            KiraButton("Hold to Send", onHoldComplete = { result = "Sent." }, leftIcon = ButtonDemoIcon.Send.slot)
            KiraButton(
                "Hold to Approve",
                variant = KiraButtonVariant.Success,
                onHoldComplete = { result = "Approved." },
                leftIcon = ButtonDemoIcon.Check.slot,
            )
            KiraButton(
                "Hold to Delete",
                variant = KiraButtonVariant.Destructive,
                holdDurationMillis = 1_500,
                onHoldComplete = { result = "Deleted." },
                leftIcon = ButtonDemoIcon.Delete.slot,
            )
        }
        if (result.isNotEmpty()) {
            ExampleStatus(result)
        }
    }
}

@Composable
private fun ExpandableSection() {
    var result by rememberSaveable { mutableStateOf("") }

    GalleryExample("Expandable Icon Button") {
        ExampleNote("Hover or focus a button to reveal its label.")
        ButtonExampleRow {
            KiraExpandableIconButton("Settings", ButtonDemoIcon.Settings.slot, variant = KiraButtonVariant.Outline)
            KiraExpandableIconButton("Like", ButtonDemoIcon.Heart.slot, variant = KiraButtonVariant.Secondary)
            KiraExpandableIconButton("Play Video", ButtonDemoIcon.Play.slot)
            KiraExpandableIconButton(
                "Delete",
                ButtonDemoIcon.Delete.slot,
                variant = KiraButtonVariant.Destructive,
                expandDirection = KiraButtonExpandDirection.Start,
            )
        }
        SubSection("With hold to confirm") {
            ButtonExampleRow {
                KiraExpandableIconButton("Send", ButtonDemoIcon.Send.slot, onHoldComplete = { result = "Sent." })
                KiraExpandableIconButton(
                    "Delete",
                    ButtonDemoIcon.Delete.slot,
                    variant = KiraButtonVariant.Destructive,
                    holdDurationMillis = 1_500,
                    onHoldComplete = { result = "Deleted." },
                )
            }
        }
        if (result.isNotEmpty()) {
            ExampleStatus(result)
        }
    }
}

@Composable
private fun LoadingSection() {
    GalleryExample("Loading Button") {
        ButtonExampleRow {
            LoadingExample("Submit", KiraButtonVariant.Primary, ButtonDemoIcon.Send)
            LoadingExample("Save", KiraButtonVariant.Secondary, ButtonDemoIcon.Download)
            LoadingExample("Process", KiraButtonVariant.Outline, loadingText = "Processing…")
            LoadingExample("Delete", KiraButtonVariant.Destructive, ButtonDemoIcon.Delete)
        }
    }
}

@Composable
private fun LoadingExample(
    label: String,
    variant: KiraButtonVariant,
    icon: ButtonDemoIcon? = null,
    loadingText: String? = null,
) {
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    KiraLoadingButton(
        text = label,
        onClick = {
            loading = true
            scope.launch {
                delay(2_000)
                loading = false
            }
        },
        variant = variant,
        isLoading = loading,
        loadingText = loadingText,
        leftIcon = icon?.slot,
    )
}

@Composable
private fun TooltipSection() {
    GalleryExample("Icon Button with Tooltip") {
        ExampleNote("Hover, focus, or long-press an icon to see its tooltip.")
        ButtonExampleRow {
            KiraIconButtonWithTooltip(ButtonDemoIcon.Settings.slot, "Settings", variant = KiraButtonVariant.Outline)
            KiraIconButtonWithTooltip(
                ButtonDemoIcon.Heart.slot,
                "Add to favorites",
                variant = KiraButtonVariant.Secondary,
                tooltipSide = KiraButtonTooltipSide.End,
            )
            KiraIconButtonWithTooltip(ButtonDemoIcon.Download.slot, "Download file")
            KiraIconButtonWithTooltip(ButtonDemoIcon.Delete.slot, "Delete item", variant = KiraButtonVariant.Destructive)
            KiraIconButtonWithTooltip(
                ButtonDemoIcon.Info.slot,
                "More information",
                variant = KiraButtonVariant.Info,
                tooltipSide = KiraButtonTooltipSide.Bottom,
            )
            DisabledSubmit()
        }
    }
}

@Composable
private fun OptimisticToggleSection() {
    var liked by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
    var likeAttempts by rememberSaveable { mutableIntStateOf(0) }
    var saveAttempts by rememberSaveable { mutableIntStateOf(0) }

    GalleryExample("Optimistic Toggle & Disabled Reason") {
        SubSection("Optimistic toggle (flips instantly, reverts on failure)") {
            ButtonExampleRow {
                KiraButton(
                    if (liked) "Liked" else "Like",
                    variant = KiraButtonVariant.Outline,
                    toggle = true,
                    pressed = liked,
                    pressedIcon = ButtonDemoIcon.HeartFilled.slot,
                    unpressedIcon = ButtonDemoIcon.Heart.slot,
                    onToggle = { next ->
                        likeAttempts++
                        delay(600)
                        check(likeAttempts % 3 != 0)
                        liked = next
                    },
                )
                KiraButton(
                    if (saved) "Saved" else "Save",
                    variant = KiraButtonVariant.Outline,
                    toggle = true,
                    pressed = saved,
                    pressedIcon = ButtonDemoIcon.StarFilled.slot,
                    unpressedIcon = ButtonDemoIcon.Star.slot,
                    toggleErrorMessage = "Couldn't update bookmark",
                    onToggle = { next ->
                        saveAttempts++
                        delay(600)
                        check(saveAttempts % 3 != 0)
                        saved = next
                    },
                )
            }
            ExampleNote("Each button rolls back every third attempt so you can try the failure state.")
        }
        SubSection("Disabled with a reason (hover or focus the button)") {
            DisabledSubmit()
        }
    }
}

@Composable
private fun DisabledSection() {
    GalleryExample("Disabled State") {
        ButtonExampleRow {
            KiraButton("Disabled Default", enabled = false)
            KiraButton("Disabled Secondary", variant = KiraButtonVariant.Secondary, enabled = false)
            KiraButton("Disabled Outline", variant = KiraButtonVariant.Outline, enabled = false)
            KiraButton("Disabled Destructive", variant = KiraButtonVariant.Destructive, enabled = false)
        }
        SubSection("Focusable when disabled (WCAG 2.2)") {
            KiraButton("Reachable by Tab", enabled = false, focusableWhenDisabled = true)
        }
    }
}

@Composable
private fun KiraButtonUnifiedSection() {
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    GalleryExample("KiraButton — Unified Component") {
        SubSection("Icons") {
            ButtonExampleRow {
                KiraButton("Send Email", leftIcon = ButtonDemoIcon.Mail.slot)
                KiraButton("Continue", variant = KiraButtonVariant.Outline, rightIcon = ButtonDemoIcon.Next.slot)
            }
        }
        SubSection("Loading") {
            ButtonExampleRow {
                KiraButton(
                    "Run Action",
                    loading = running,
                    onClick = {
                        running = true
                        scope.launch {
                            delay(2_000)
                            running = false
                        }
                    },
                )
                KiraButton("Save", loading = true, loadingText = "Saving…", variant = KiraButtonVariant.Secondary)
            }
        }
        SubSection("All variants") {
            ButtonExampleRow {
                KiraButtonVariant.entries.forEach { variant ->
                    val icon = when (variant) {
                        KiraButtonVariant.Destructive, KiraButtonVariant.DestructiveOutline, KiraButtonVariant.DestructiveGhost ->
                            ButtonDemoIcon.Delete
                        KiraButtonVariant.Info, KiraButtonVariant.InfoOutline, KiraButtonVariant.InfoGhost ->
                            ButtonDemoIcon.Info
                        KiraButtonVariant.Success, KiraButtonVariant.SuccessOutline, KiraButtonVariant.SuccessGhost ->
                            ButtonDemoIcon.Check
                        KiraButtonVariant.Warning, KiraButtonVariant.WarningOutline, KiraButtonVariant.WarningGhost ->
                            ButtonDemoIcon.Warning
                        else -> ButtonDemoIcon.Mail
                    }
                    KiraButton(variant.demoName, variant = variant, size = KiraButtonSize.Sm, leftIcon = icon.slot)
                }
            }
        }
    }
}

@Composable
private fun DisabledSubmit() {
    KiraIconButtonWithTooltip(
        ButtonDemoIcon.Send.slot,
        "Submit",
        enabled = false,
        disabledReason = "Complete all required fields before submitting.",
    )
}

@Composable
private fun VariantSubSection(
    label: String,
    variants: List<KiraButtonVariant>,
    buttonLabel: (KiraButtonVariant) -> String = { it.demoName },
) {
    SubSection(label) {
        ButtonExampleRow {
            variants.forEach { KiraButton(buttonLabel(it), variant = it) }
        }
    }
}

@Composable
private fun SubSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(
            text = title,
            style = KiraTheme.typography.bodySmall.copy(
                color = KiraTheme.colors.mutedForeground,
                fontWeight = FontWeight.Medium,
            ),
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ButtonExampleRow(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KiraTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun ExampleStatus(text: String) {
    BasicText(
        text,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.mutedForeground),
    )
}
