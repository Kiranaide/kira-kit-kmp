package com.kirakit.gallery.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
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
import com.kirakit.components.card.KiraCard
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

@Composable
internal fun ButtonExamples() {
    ButtonExample("Variants") {
        VariantExamples("Basic", basicVariants)
        VariantExamples("Semantic", semanticVariants)
        VariantExamples("Outline", outlineVariants) { it.demoName.removeSuffix("Outline") }
        VariantExamples("Ghost", ghostVariants) { it.demoName.removeSuffix("Ghost") }
    }
    ButtonExample("Sizes") {
        ButtonExampleRow {
            KiraButton("Extra Small", size = KiraButtonSize.Xs)
            KiraButton("Small", size = KiraButtonSize.Sm)
            KiraButton("Default")
            KiraButton("Large", size = KiraButtonSize.Lg)
            KiraButton("Extra Large", size = KiraButtonSize.Xl)
        }
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
        KiraButton("Full-width button", fullWidth = true, variant = KiraButtonVariant.Outline)
    }
    ButtonExample("With Icons") {
        ButtonExampleRow {
            KiraButton("Send Email", leftIcon = ButtonDemoIcon.Mail.slot)
            KiraButton("Download", variant = KiraButtonVariant.Secondary, leftIcon = ButtonDemoIcon.Download.slot)
            KiraButton("Settings", variant = KiraButtonVariant.Outline, leftIcon = ButtonDemoIcon.Settings.slot)
            KiraButton("Confirm", variant = KiraButtonVariant.Success, leftIcon = ButtonDemoIcon.Check.slot)
            KiraButton("Delete", variant = KiraButtonVariant.Destructive, leftIcon = ButtonDemoIcon.Delete.slot)
        }
    }
    ButtonExample("Icon Props") { IconPropsExample() }
    ButtonExample("Hold to Confirm") { HoldExamples() }
    ButtonExample("Expandable") { ExpandableExamples() }
    ButtonExample("Loading") {
        ButtonExampleRow {
            LoadingExample("Submit", KiraButtonVariant.Primary, ButtonDemoIcon.Send)
            LoadingExample("Save", KiraButtonVariant.Secondary, ButtonDemoIcon.Download)
            LoadingExample("Process", KiraButtonVariant.Outline, loadingText = "Processing…")
            LoadingExample("Delete", KiraButtonVariant.Destructive, ButtonDemoIcon.Delete)
        }
    }
    ButtonExample("With Tooltip") { TooltipExamples() }
    ButtonExample("KiraButton") { KiraButtonExamples() }
    ButtonExample("Optimistic Toggle") { ToggleExamples() }
    ButtonExample("Disabled") { DisabledExamples() }
}

@Composable
private fun IconPropsExample() {
    ExampleLabel("Leading and trailing icons")
    ButtonExampleRow {
        KiraButton("Send Email", leftIcon = ButtonDemoIcon.Mail.slot)
        KiraButton("Download", variant = KiraButtonVariant.Secondary, leftIcon = ButtonDemoIcon.Download.slot)
        KiraButton("Continue", variant = KiraButtonVariant.Outline, rightIcon = ButtonDemoIcon.Next.slot)
        KiraButton("Back", variant = KiraButtonVariant.Ghost, leftIcon = ButtonDemoIcon.Back.slot)
    }
    ExampleLabel("Both icons")
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
    ExampleLabel("Custom content")
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

@Composable
private fun HoldExamples() {
    var result by rememberSaveable { mutableStateOf("") }
    ExampleLabel("Press and hold, or hold Space or Enter. Release early to cancel.")
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
    ExampleStatus(result)
}

@Composable
private fun ExpandableExamples() {
    var result by rememberSaveable { mutableStateOf("") }
    ExampleLabel("Hover or focus a button to reveal its label.")
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
    ExampleLabel("With hold to confirm")
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
    ExampleStatus(result)
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
private fun TooltipExamples() {
    ExampleLabel("Hover, focus, or long-press an icon to see its tooltip.")
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

@Composable
private fun KiraButtonExamples() {
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    ExampleLabel("Icons")
    ButtonExampleRow {
        KiraButton("Send Email", leftIcon = ButtonDemoIcon.Mail.slot)
        KiraButton("Continue", variant = KiraButtonVariant.Outline, rightIcon = ButtonDemoIcon.Next.slot)
    }
    ExampleLabel("Loading")
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
    ExampleLabel("All variants")
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

@Composable
private fun ToggleExamples() {
    var liked by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
    var likeAttempts by rememberSaveable { mutableStateOf(0) }
    var saveAttempts by rememberSaveable { mutableStateOf(0) }
    ExampleLabel("Each button rolls back every third attempt so you can try the failure state.")
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
    ExampleLabel("Disabled with a reason")
    DisabledSubmit()
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
private fun DisabledExamples() {
    ButtonExampleRow {
        KiraButton("Disabled Default", enabled = false)
        KiraButton("Disabled Secondary", variant = KiraButtonVariant.Secondary, enabled = false)
        KiraButton("Disabled Outline", variant = KiraButtonVariant.Outline, enabled = false)
        KiraButton("Disabled Destructive", variant = KiraButtonVariant.Destructive, enabled = false)
    }
    ExampleLabel("Focusable when disabled")
    KiraButton("Reachable by Tab", enabled = false, focusableWhenDisabled = true)
}

@Composable
private fun VariantExamples(
    label: String,
    variants: List<KiraButtonVariant>,
    buttonLabel: (KiraButtonVariant) -> String = { it.demoName },
) {
    ExampleLabel(label)
    ButtonExampleRow {
        variants.forEach { KiraButton(buttonLabel(it), variant = it) }
    }
}

@Composable
private fun ButtonExample(title: String, content: @Composable () -> Unit) {
    KiraCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.sm)) {
            BasicText(
                title,
                modifier = Modifier.semantics { heading() },
                style = KiraTheme.typography.heading3.copy(color = KiraTheme.colors.cardForeground),
            )
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ButtonExampleRow(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KiraTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.xs, Alignment.Top),
    ) {
        content()
    }
}

@Composable
private fun ExampleLabel(text: String) {
    BasicText(text, style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.mutedForeground))
}

@Composable
private fun ExampleStatus(text: String) {
    BasicText(
        text,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.mutedForeground),
    )
}
