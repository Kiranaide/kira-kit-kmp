package com.kirakit.gallery.accordion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kirakit.components.accordion.KiraAccordion
import com.kirakit.components.accordion.KiraAccordionChevronPosition
import com.kirakit.components.accordion.KiraAccordionContent
import com.kirakit.components.accordion.KiraAccordionItem
import com.kirakit.components.accordion.KiraAccordionItemData
import com.kirakit.components.accordion.KiraAccordionList
import com.kirakit.components.accordion.KiraAccordionSize
import com.kirakit.components.accordion.KiraAccordionTrigger
import com.kirakit.components.accordion.KiraAccordionVariant
import com.kirakit.components.accordion.rememberKiraAccordionState
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun AccordionPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic")
        GalleryExample("Single Selection (Collapsible)") {
            val state = rememberKiraAccordionState(initialExpanded = setOf("item-1"))
            KiraAccordion(state = state) {
                KiraAccordionItem("item-1") {
                    KiraAccordionTrigger("What is Kira Kit?")
                    KiraAccordionContent {
                        BodyCopy("Kira Kit is a modern multiplatform component library sharing design tokens across Web and KMP.")
                    }
                }
                KiraAccordionItem("item-2") {
                    KiraAccordionTrigger("How do I install it?")
                    KiraAccordionContent {
                        BodyCopy("Add :kira-theme and :kira-components to your commonMain dependencies — no Material 3 required.")
                    }
                }
                KiraAccordionItem("item-3") {
                    KiraAccordionTrigger("Is it accessible?")
                    KiraAccordionContent {
                        BodyCopy("Yes! Every trigger exposes expanded/collapsed semantics, focus rings, haptics, and 44dp+ touch targets.")
                    }
                }
            }
        }

        SectionHeading("Multiple & Without Arrow")
        GalleryExample("Multiple Open Items") {
            val multiState = rememberKiraAccordionState(
                initialExpanded = setOf("m-1", "m-2"),
                multiple = true,
            )
            KiraAccordion(state = multiState) {
                KiraAccordionItem("m-1") {
                    KiraAccordionTrigger("First Item (open by default)")
                    KiraAccordionContent { BodyCopy("Multiple items can be open simultaneously.") }
                }
                KiraAccordionItem("m-2") {
                    KiraAccordionTrigger("Second Item (open by default)")
                    KiraAccordionContent { BodyCopy("Tap any header to toggle without closing others.") }
                }
                KiraAccordionItem("m-3") {
                    KiraAccordionTrigger("Without Arrow", showArrow = false)
                    KiraAccordionContent { BodyCopy("Clean header without the default chevron indicator.") }
                }
                KiraAccordionItem("m-4") {
                    KiraAccordionTrigger(
                        "Leading Chevron",
                        chevronPosition = KiraAccordionChevronPosition.Start,
                    )
                    KiraAccordionContent(inset = true) {
                        BodyCopy("Chevron positioned at the start with inset content alignment.")
                    }
                }
            }
        }

        SectionHeading("Variants & Sizes")
        GalleryExample("Semantic Header Colors") {
            val colorState = rememberKiraAccordionState(multiple = true)
            KiraAccordion(state = colorState) {
                KiraAccordionItem("info") {
                    KiraAccordionTrigger("Information", variant = KiraAccordionVariant.Info, icon = { DotIcon(colors.info) })
                    KiraAccordionContent { BodyCopy("Informational section with tinted info header.") }
                }
                KiraAccordionItem("success") {
                    KiraAccordionTrigger("Success", variant = KiraAccordionVariant.Success, icon = { DotIcon(colors.success) })
                    KiraAccordionContent { BodyCopy("Indicates a completed or healthy state.") }
                }
                KiraAccordionItem("warning") {
                    KiraAccordionTrigger("Warning", variant = KiraAccordionVariant.Warning, icon = { DotIcon(colors.warning) })
                    KiraAccordionContent { BodyCopy("Highlights something that needs attention.") }
                }
                KiraAccordionItem("destructive") {
                    KiraAccordionTrigger("Destructive", variant = KiraAccordionVariant.Destructive, icon = { DotIcon(colors.destructive) })
                    KiraAccordionContent { BodyCopy("Danger zone or irreversible actions.") }
                }
            }
        }

        GalleryExample("Sizes & Disabled Reason") {
            val sizeState = rememberKiraAccordionState(multiple = true)
            KiraAccordion(state = sizeState) {
                KiraAccordionItem("xs") {
                    KiraAccordionTrigger("Extra Small (xs)", size = KiraAccordionSize.Xs)
                    KiraAccordionContent(size = KiraAccordionSize.Xs) { BodyCopy("Compact padding with accessible 44dp touch height.") }
                }
                KiraAccordionItem("sm") {
                    KiraAccordionTrigger("Small (sm)", size = KiraAccordionSize.Sm)
                    KiraAccordionContent(size = KiraAccordionSize.Sm) { BodyCopy("Small tier typography and spacing.") }
                }
                KiraAccordionItem("lg") {
                    KiraAccordionTrigger("Large (lg)", size = KiraAccordionSize.Lg)
                    KiraAccordionContent(size = KiraAccordionSize.Lg) { BodyCopy("Spacious padding for primary FAQ sections.") }
                }
                KiraAccordionItem(
                    value = "locked",
                    disabled = true,
                    disabledReason = "Upgrade to Pro to access audit logs",
                ) {
                    KiraAccordionTrigger("Disabled with Tooltip Reason")
                    KiraAccordionContent { BodyCopy("Hidden content.") }
                }
            }
        }

        SectionHeading("Async Loading, Error & Retry")
        GalleryExample("Simulated Async Panel") {
            var loading by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            var loadedText by remember { mutableStateOf<String?>(null) }
            var attempts by remember { mutableStateOf(0) }

            fun loadData() {
                loading = true
                error = null
                attempts++
                scope.launch {
                    delay(900)
                    loading = false
                    if (attempts % 2 == 1) {
                        error = "Network timeout on attempt #$attempts. Tap retry."
                    } else {
                        loadedText = "Loaded 42 deployment records successfully on attempt #$attempts."
                    }
                }
            }

            val asyncState = rememberKiraAccordionState()
            KiraAccordion(state = asyncState) {
                KiraAccordionItem(
                    value = "async",
                    onOpen = { if (loadedText == null && !loading) loadData() },
                ) {
                    KiraAccordionTrigger(
                        title = "Recent Deployments (fails first, succeeds on retry)",
                        loading = loading,
                        error = error,
                        onRetry = ::loadData,
                    )
                    KiraAccordionContent(
                        loading = loading,
                        error = error,
                        onRetry = ::loadData,
                        skeletonLines = 3,
                    ) {
                        BodyCopy(loadedText ?: "Open to load data.")
                    }
                }
            }
        }

        SectionHeading("Searchable, Expand All, Nested & Reorderable")
        GalleryExample("KiraAccordionList") {
            ExampleNote("Filter sections with live match highlighting, expand/collapse all, or reorder items.")
            var items by remember {
                mutableStateOf(
                    listOf(
                        KiraAccordionItemData(
                            value = "getting-started",
                            title = "Getting Started",
                            content = { BodyCopy("Configure your theme and tokens in seconds.") },
                            children = listOf(
                                KiraAccordionItemData(
                                    value = "install",
                                    title = "Installation",
                                    content = { BodyCopy("Add the Gradle dependency to commonMain.") },
                                ),
                                KiraAccordionItemData(
                                    value = "tokens",
                                    title = "Design Tokens",
                                    content = { BodyCopy("Export tokens directly from kira-kit-v2.") },
                                ),
                            ),
                        ),
                        KiraAccordionItemData(
                            value = "accessibility",
                            title = "Accessibility & Haptics",
                            content = { BodyCopy("Built-in semantics, focus rings, and native mobile haptics.") },
                        ),
                        KiraAccordionItemData(
                            value = "theming",
                            title = "Multi-Theme Support",
                            content = { BodyCopy("Switch color schemes and corner radius presets dynamically.") },
                        ),
                    ),
                )
            }
            KiraAccordionList(
                items = items,
                searchable = true,
                showExpandAll = true,
                reorderable = true,
                onReorder = { items = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BodyCopy(text: String) {
    BasicText(
        text = text,
        style = KiraTheme.typography.body.copy(color = KiraTheme.colors.mutedForeground),
    )
}

@Composable
private fun DotIcon(color: Color) {
    Canvas(Modifier.size(12.dp)) {
        drawCircle(color = color, radius = size.minDimension / 2.5f)
        drawCircle(
            color = color.copy(alpha = 0.35f),
            radius = size.minDimension / 2f,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
            center = Offset(size.width / 2f, size.height / 2f),
        )
    }
}
