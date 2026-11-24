package com.kirakit.gallery.toast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.toast.KiraToastAction
import com.kirakit.components.toast.LocalKiraToastController
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ToastPlayground() {
    val gap = KiraTheme.spacing.md
    val toast = LocalKiraToastController.current
    val scope = rememberCoroutineScope()

    var promiseRun by remember { mutableIntStateOf(0) }
    var activeLoadingId by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Variants")
        GalleryExample("Basic Toast") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "Show Toast",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { toast.show("Event has been created") },
                )
                KiraButton(
                    text = "With Description",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        toast.message(
                            title = "Event has been created",
                            description = "Sunday, December 03, 2023 at 9:00 AM",
                        )
                    },
                )
            }
        }

        GalleryExample("All 6 Variants") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraButton(
                        text = "Default",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { toast.show("Event has been created") },
                    )
                    KiraButton(
                        text = "Success",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { toast.success("Event has been created") },
                    )
                    KiraButton(
                        text = "Error",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { toast.error("Event has not been created") },
                    )
                    KiraButton(
                        text = "Warning",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { toast.warning("Event start time cannot be in the past") },
                    )
                    KiraButton(
                        text = "Info",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { toast.info("New message received") },
                    )
                    KiraButton(
                        text = "Loading",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { toast.loading("Processing your request...") },
                    )
                }
                ExampleNote("Accessibility: Success/Info use Polite live regions; Error/Warning use Assertive live regions.")
            }
        }

        SectionHeading("Actions & Async Promise")
        GalleryExample("Toast with Action Button") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "With Undo Action",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        toast.show(
                            title = "Event has been deleted",
                            action = KiraToastAction("Undo") { toast.success("Event restored") },
                        )
                    },
                )
                KiraButton(
                    text = "Error with Retry",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        toast.error(
                            title = "Failed to save changes",
                            action = KiraToastAction("Retry") { toast.loading("Retrying...") },
                        )
                    },
                )
                KiraButton(
                    text = "Success with Action",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        toast.success(
                            title = "Payment successful",
                            description = "Your order #12345 has been confirmed.",
                            action = KiraToastAction("View Order") { toast.info("Navigating to order...") },
                        )
                    },
                )
            }
        }

        GalleryExample("Promise & Programmatic Lifecycle") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraButton(
                        text = "Promise Toast",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = {
                            val run = ++promiseRun
                            scope.launch {
                                toast.promise(
                                    loading = "Creating event...",
                                    success = { name -> "$name has been created" },
                                    error = { err -> "Failed: ${err.message}" },
                                ) {
                                    delay(1_500L)
                                    if (run % 3 == 0) error("Network error")
                                    "Event #$run"
                                }
                            }
                        },
                    )
                    KiraButton(
                        text = if (activeLoadingId != null) "Processing..." else "Start Loading Toast",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        enabled = activeLoadingId == null,
                        onClick = {
                            val id = toast.loading("Processing your request...")
                            activeLoadingId = id
                            scope.launch {
                                delay(2_500L)
                                if (activeLoadingId == id) {
                                    toast.dismiss(id)
                                    toast.success("Processing complete!")
                                    activeLoadingId = null
                                }
                            }
                        },
                    )
                    if (activeLoadingId != null) {
                        KiraButton(
                            text = "Cancel",
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = {
                                activeLoadingId?.let { toast.dismiss(it) }
                                toast.error("Operation cancelled")
                                activeLoadingId = null
                            },
                        )
                    }
                }
            }
        }

        SectionHeading("Durations, Dismiss & Stacking")
        GalleryExample("Duration & Dismiss Control") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KiraButton(
                    text = "2 second toast",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { toast.show("Quick notification", durationMillis = 2_000L) },
                )
                KiraButton(
                    text = "10 second toast",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { toast.info("This will stay longer", durationMillis = 10_000L) },
                )
                KiraButton(
                    text = "Persistent toast",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { toast.warning("This won't auto-dismiss", durationMillis = 0L) },
                )
                KiraButton(
                    text = "Dismiss by ID (2s)",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        val id = toast.show("This toast will be dismissed in 2 seconds")
                        scope.launch {
                            delay(2_000L)
                            toast.dismiss(id)
                        }
                    },
                )
            }
        }

        GalleryExample("Burst & Collapsed Stacking") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraButton(
                        text = "Show a burst of toasts",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = {
                            toast.success("File uploaded")
                            toast.info("Processing…")
                            toast.show("Almost done")
                            toast.success("Complete")
                        },
                    )
                    KiraButton(
                        text = "Dismiss All",
                        variant = KiraButtonVariant.Destructive,
                        size = KiraButtonSize.Sm,
                        onClick = { toast.dismiss() },
                    )
                }
                ExampleNote("Toasts stack collapsed at bottom-right (max 3 visible). Hover or tap the stack to expand and pause timers; swipe horizontally to dismiss.")
            }
        }
    }
}
