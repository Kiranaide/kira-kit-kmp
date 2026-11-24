package com.kirakit.gallery.select

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.select.KiraMultiSelect
import com.kirakit.components.select.KiraSelect
import com.kirakit.components.select.KiraSelectGroup
import com.kirakit.components.select.KiraSelectOption
import com.kirakit.components.select.KiraSelectSize
import com.kirakit.components.select.KiraSelectVariant
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import kotlinx.coroutines.delay

@Composable
internal fun SelectPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    val fruits = remember {
        listOf(
            KiraSelectOption("apple", "Apple"),
            KiraSelectOption("banana", "Banana"),
            KiraSelectOption("orange", "Orange"),
            KiraSelectOption("grape", "Grape"),
            KiraSelectOption("mango", "Mango"),
            KiraSelectOption("kiwi", "Kiwi"),
            KiraSelectOption("pineapple", "Pineapple"),
            KiraSelectOption("strawberry", "Strawberry"),
        )
    }

    val groupedFruits = remember {
        listOf(
            KiraSelectGroup(
                "Citrus",
                listOf(
                    KiraSelectOption("orange", "Orange"),
                    KiraSelectOption("lemon", "Lemon"),
                    KiraSelectOption("lime", "Lime"),
                ),
            ),
            KiraSelectGroup(
                "Berries",
                listOf(
                    KiraSelectOption("strawberry", "Strawberry"),
                    KiraSelectOption("blueberry", "Blueberry"),
                    KiraSelectOption("raspberry", "Raspberry"),
                ),
            ),
            KiraSelectGroup(
                "Tropical",
                listOf(
                    KiraSelectOption("mango", "Mango"),
                    KiraSelectOption("pineapple", "Pineapple"),
                    KiraSelectOption("papaya", "Papaya"),
                ),
            ),
        )
    }

    val longLabelOptions = remember {
        listOf(
            KiraSelectOption("short", "Short label"),
            KiraSelectOption(
                "long",
                "A very long option label that keeps going well past eighty characters to prove truncation behaves inside the trigger and the popup list",
            ),
        )
    }

    val usersCatalog = remember {
        listOf(
            KiraSelectOption("1", "John Doe"),
            KiraSelectOption("2", "Jane Smith"),
            KiraSelectOption("3", "Bob Johnson"),
            KiraSelectOption("4", "Alice Williams"),
            KiraSelectOption("5", "Charlie Brown"),
            KiraSelectOption("6", "Diana Prince"),
            KiraSelectOption("7", "Edward Norton"),
            KiraSelectOption("8", "Fiona Green"),
        )
    }

    var basicValue by remember { mutableStateOf<String?>(null) }
    var multiSelected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var autocompleteValue by remember { mutableStateOf<String?>(null) }
    var asyncValue by remember { mutableStateOf<String?>(null) }
    var creatableOptions by remember { mutableStateOf(fruits) }
    var creatableValue by remember { mutableStateOf<String?>(null) }
    var groupedValue by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Multi Select")
        GalleryExample("Basic Select") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraSelect(
                    options = fruits,
                    value = basicValue,
                    onValueChange = { basicValue = it },
                    clearable = true,
                    placeholder = "Select a fruit",
                )
                basicValue?.let { ExampleNote("Selected: $it") }
            }
        }

        GalleryExample("Multi Select") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraMultiSelect(
                    options = fruits,
                    selectedValues = multiSelected,
                    onSelectedValuesChange = { multiSelected = it },
                    placeholder = "Select fruits...",
                    maxDisplayedTags = 3,
                )
                if (multiSelected.isNotEmpty()) {
                    ExampleNote("Selected: ${multiSelected.joinToString(", ")}")
                }
            }
        }

        SectionHeading("Autocomplete, Async & Creatable")
        GalleryExample("Autocomplete (Searchable)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraSelect(
                    options = fruits,
                    value = autocompleteValue,
                    onValueChange = { autocompleteValue = it },
                    searchable = true,
                    clearable = true,
                    placeholder = "Search fruits...",
                )
                autocompleteValue?.let { ExampleNote("Selected: $it") }
            }
        }

        GalleryExample("Async Select") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraSelect(
                    options = emptyList(),
                    value = asyncValue,
                    onValueChange = { asyncValue = it },
                    searchable = true,
                    clearable = true,
                    placeholder = "Search users...",
                    asyncSearch = { q ->
                        delay(500L)
                        if (q.isBlank()) usersCatalog else usersCatalog.filter {
                            it.label.contains(q, ignoreCase = true)
                        }
                    },
                )
                asyncValue?.let { ExampleNote("Selected user ID: $it") }
            }
        }

        GalleryExample("Creatable Select") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraSelect(
                    options = creatableOptions,
                    value = creatableValue,
                    onValueChange = { creatableValue = it },
                    searchable = true,
                    creatable = true,
                    clearable = true,
                    placeholder = "Select or create...",
                    onCreateOption = { input ->
                        val newOpt = KiraSelectOption(input.lowercase(), input)
                        creatableOptions = creatableOptions + newOpt
                        creatableValue = newOpt.value
                    },
                )
                creatableValue?.let { ExampleNote("Selected: $it") }
            }
        }

        SectionHeading("Grouped + Search")
        GalleryExample("Grouped Options with Search") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraSelect(
                    options = emptyList(),
                    groups = groupedFruits,
                    value = groupedValue,
                    onValueChange = { groupedValue = it },
                    searchable = true,
                    clearable = true,
                    placeholder = "Filter grouped options...",
                )
                groupedValue?.let {
                    ExampleNote("Selected: $it — group labels stay visible while filtering.")
                }
            }
        }

        SectionHeading("Trigger Variants & Disabled")
        GalleryExample("Default, Ghost, Outline, Left Icon, Invalid & No Chevron") {
            var vDefault by remember { mutableStateOf<String?>(null) }
            var vGhost by remember { mutableStateOf<String?>(null) }
            var vOutline by remember { mutableStateOf<String?>(null) }
            var vIcon by remember { mutableStateOf<String?>(null) }
            var vInvalid by remember { mutableStateOf<String?>(null) }
            var vNoChevron by remember { mutableStateOf<String?>(null) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("Default") {
                    KiraSelect(
                        options = fruits,
                        value = vDefault,
                        onValueChange = { vDefault = it },
                        searchable = true,
                        placeholder = "Default",
                    )
                }
                LabeledField("Ghost") {
                    KiraSelect(
                        options = fruits,
                        value = vGhost,
                        onValueChange = { vGhost = it },
                        searchable = true,
                        variant = KiraSelectVariant.Ghost,
                        placeholder = "Ghost",
                    )
                }
                LabeledField("Outline") {
                    KiraSelect(
                        options = fruits,
                        value = vOutline,
                        onValueChange = { vOutline = it },
                        searchable = true,
                        variant = KiraSelectVariant.Outline,
                        placeholder = "Outline",
                    )
                }
                LabeledField("With Left Icon") {
                    KiraSelect(
                        options = fruits,
                        value = vIcon,
                        onValueChange = { vIcon = it },
                        searchable = true,
                        leftIcon = { UserGlyph(colors.mutedForeground) },
                        placeholder = "Select user...",
                    )
                }
                LabeledField("Invalid State") {
                    KiraSelect(
                        options = fruits,
                        value = vInvalid,
                        onValueChange = { vInvalid = it },
                        searchable = true,
                        invalid = true,
                        placeholder = "Error state",
                    )
                }
                LabeledField("No Chevron") {
                    KiraSelect(
                        options = fruits,
                        value = vNoChevron,
                        onValueChange = { vNoChevron = it },
                        searchable = true,
                        hideChevron = true,
                        placeholder = "No chevron",
                    )
                }
                LabeledField("Disabled") {
                    KiraSelect(
                        options = fruits,
                        value = null,
                        onValueChange = {},
                        enabled = false,
                        placeholder = "Disabled — archive view",
                    )
                }
            }
        }

        SectionHeading("Sizes & Stress Cases")
        GalleryExample("Size Ladder (Xs / Sm / Default / Lg / Xl)") {
            var sizeVal by remember { mutableStateOf<String?>(null) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraSelect(options = fruits, value = sizeVal, onValueChange = { sizeVal = it }, size = KiraSelectSize.Xs, placeholder = "Extra small")
                KiraSelect(options = fruits, value = sizeVal, onValueChange = { sizeVal = it }, size = KiraSelectSize.Sm, placeholder = "Small")
                KiraSelect(options = fruits, value = sizeVal, onValueChange = { sizeVal = it }, size = KiraSelectSize.Default, placeholder = "Default")
                KiraSelect(options = fruits, value = sizeVal, onValueChange = { sizeVal = it }, size = KiraSelectSize.Lg, placeholder = "Large")
                KiraSelect(options = fruits, value = sizeVal, onValueChange = { sizeVal = it }, size = KiraSelectSize.Xl, placeholder = "Extra large")
            }
        }

        GalleryExample("Stress Cases (Long Label, Narrow 320dp & Empty Options)") {
            var longVal by remember { mutableStateOf<String?>("long") }
            var narrowVal by remember { mutableStateOf<String?>(null) }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("Very long label (truncation)") {
                    KiraSelect(
                        options = longLabelOptions,
                        value = longVal,
                        onValueChange = { longVal = it },
                        searchable = true,
                    )
                }
                LabeledField("Narrow container (320dp)") {
                    KiraSelect(
                        options = fruits,
                        value = narrowVal,
                        onValueChange = { narrowVal = it },
                        searchable = true,
                        placeholder = "Narrow...",
                        modifier = Modifier.widthIn(max = 320.dp),
                    )
                }
                LabeledField("Empty options") {
                    KiraSelect(
                        options = emptyList(),
                        value = null,
                        onValueChange = {},
                        placeholder = "No options",
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        KiraLabel(text = label)
        content()
    }
}

@Composable
private fun UserGlyph(color: Color) {
    Canvas(Modifier.size(16.dp)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.20f,
            center = Offset(size.width * 0.5f, size.height * 0.30f),
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.16f, size.height * 0.54f),
            size = Size(size.width * 0.68f, size.height * 0.52f),
            style = stroke,
        )
    }
}
