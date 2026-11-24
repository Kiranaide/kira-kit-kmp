package com.kirakit.gallery.spotlight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.input.KiraInput
import com.kirakit.components.spotlight.KiraSpotlight
import com.kirakit.components.spotlight.KiraSpotlightGroup
import com.kirakit.components.spotlight.KiraSpotlightItem
import com.kirakit.components.spotlight.KiraSpotlightScope
import com.kirakit.components.spotlight.KiraSpotlightTrigger
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

private val DemoSpotlightResources: List<KiraSpotlightItem<String>> = listOf(
    KiraSpotlightItem(
        id = "roadmap",
        label = "Q4 product roadmap",
        description = "Projects · Product team",
        icon = { c -> FolderGlyph(c) },
        scope = "projects",
        shortcut = listOf("G", "R"),
        keywords = listOf("planning", "launch"),
        data = "roadmap",
    ),
    KiraSpotlightItem(
        id = "guide",
        label = "Customer onboarding guide",
        description = "Documents · Updated today",
        icon = { c -> FileGlyph(c) },
        scope = "documents",
        shortcut = listOf("G", "D"),
        keywords = listOf("help", "support"),
        data = "guide",
    ),
    KiraSpotlightItem(
        id = "maya",
        label = "Maya Chen",
        description = "People · Workspace owner",
        icon = { c -> UserGlyph(c) },
        scope = "people",
        keywords = listOf("admin", "owner"),
        data = "maya",
    ),
    KiraSpotlightItem(
        id = "archive",
        label = "Archived financial report",
        description = "Documents · Restricted",
        icon = { c -> FileGlyph(c) },
        scope = "documents",
        disabled = true,
        disabledReason = "Ask Finance for access.",
        data = "archive",
    ),
)

private val DemoSpotlightScopes: List<KiraSpotlightScope> = listOf(
    KiraSpotlightScope("projects", "Projects"),
    KiraSpotlightScope("documents", "Documents"),
    KiraSpotlightScope("people", "People"),
)

internal fun LazyListScope.spotlightPlaygroundItems() {
    galleryItem("spot-basic") { BasicSpotlightDemo(DemoSpotlightResources) }
    galleryItem("spot-groups") { GroupsSpotlightDemo(DemoSpotlightResources) }
    galleryItem("spot-filter") { FilterSpotlightDemo(DemoSpotlightResources) }
    galleryItem("spot-scopes") { ScopesSpotlightDemo(DemoSpotlightResources, DemoSpotlightScopes) }
    galleryItem("spot-remote") { RemoteSpotlightDemo(DemoSpotlightResources, DemoSpotlightScopes) }
    galleryItem("spot-recents") { RecentsSpotlightDemo(DemoSpotlightResources) }
    galleryItem("spot-preview") { PreviewSpotlightDemo(DemoSpotlightResources) }
    galleryItem("spot-composition") { CompositionSpotlightDemo() }
    galleryItem("spot-kira") { KiraSpotlightDemo(DemoSpotlightResources, DemoSpotlightScopes) }
    galleryItem("spot-states") { StatesSpotlightDemo() }
}

@Composable
internal fun SpotlightPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicSpotlightDemo(DemoSpotlightResources)
        GroupsSpotlightDemo(DemoSpotlightResources)
        FilterSpotlightDemo(DemoSpotlightResources)
        ScopesSpotlightDemo(DemoSpotlightResources, DemoSpotlightScopes)
        RemoteSpotlightDemo(DemoSpotlightResources, DemoSpotlightScopes)
        RecentsSpotlightDemo(DemoSpotlightResources)
        PreviewSpotlightDemo(DemoSpotlightResources)
        CompositionSpotlightDemo()
        KiraSpotlightDemo(DemoSpotlightResources, DemoSpotlightScopes)
        StatesSpotlightDemo()
    }
}

@Composable
private fun BasicSpotlightDemo(resources: List<KiraSpotlightItem<String>>) {
    var open by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("Nothing opened yet.") }

    GalleryExample("Basic") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KiraSpotlightTrigger(text = "Search workspace", showShortcut = false, onClick = { open = true })
            ExampleNote(destination)
        }
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            items = resources,
            onSelect = { item -> destination = "Opened ${item.label}" },
        )
    }
}

@Composable
private fun GroupsSpotlightDemo(resources: List<KiraSpotlightItem<String>>) {
    var open by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("Choose a grouped item.") }

    GalleryExample("Groups") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KiraSpotlightTrigger(text = "Search grouped resources", showShortcut = false, onClick = { open = true })
            ExampleNote(destination)
        }
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            groups = listOf(
                KiraSpotlightGroup("projects", "Projects", resources.filter { it.scope == "projects" }),
            ),
            items = resources.filter { it.scope != "projects" },
            onSelect = { destination = "Opened ${it.label}" },
        )
    }
}

@Composable
private fun FilterSpotlightDemo(resources: List<KiraSpotlightItem<String>>) {
    var open by remember { mutableStateOf(false) }

    GalleryExample("Filtering (Custom ID Prefix Filter)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExampleNote("Try typing roadmap, guide, or maya.")
            KiraSpotlightTrigger(text = "Search by resource ID", showShortcut = false, onClick = { open = true })
        }
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            items = resources,
            filter = { item, query -> item.id.startsWith(query.trim().lowercase()) },
        )
    }
}

@Composable
private fun ScopesSpotlightDemo(
    resources: List<KiraSpotlightItem<String>>,
    scopes: List<KiraSpotlightScope>,
) {
    var open by remember { mutableStateOf(false) }

    GalleryExample("Scopes") {
        KiraSpotlightTrigger(text = "Search by team or resource", showShortcut = false, onClick = { open = true })
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            items = resources,
            scopes = scopes,
        )
    }
}

@Composable
private fun RemoteSpotlightDemo(
    resources: List<KiraSpotlightItem<String>>,
    scopes: List<KiraSpotlightScope>,
) {
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf("all") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var remoteResults by remember { mutableStateOf<List<KiraSpotlightItem<String>>>(emptyList()) }

    LaunchedEffect(open, query, scope) {
        if (!open) return@LaunchedEffect
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            loading = false
            error = null
            remoteResults = emptyList()
            return@LaunchedEffect
        }
        loading = true
        error = null
        delay(200L) // debounce
        delay(450L) // simulated network
        loading = false
        if (trimmed.equals("error", ignoreCase = true)) {
            error = "Search service is unavailable. Try again or change your query."
            remoteResults = emptyList()
        } else {
            remoteResults = resources.filter { item ->
                (scope == "all" || item.scope == scope) &&
                    (listOf(item.label) + item.keywords).any { it.contains(trimmed, ignoreCase = true) }
            }
        }
    }

    GalleryExample("Remote (Debounced Search, Min Query & Retry)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExampleNote("Type 2+ chars (e.g. 'road', 'help', or 'error' to test retry recovery).")
            KiraSpotlightTrigger(text = "Search remote resources", showShortcut = false, onClick = { open = true })
        }
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            scopes = scopes,
            selectedScope = scope,
            onScopeChange = { scope = it },
            query = query,
            onQueryChange = { query = it },
            items = remoteResults,
            disableFilter = true,
            loading = loading,
            errorMessage = error,
            onRetry = {
                query = "roadmap"
                error = null
            },
            emptyMessage = if (query.trim().length < 2) "Type at least 2 characters to search remote resources." else "No remote results found.",
        )
    }
}

@Composable
private fun RecentsSpotlightDemo(resources: List<KiraSpotlightItem<String>>) {
    var open by remember { mutableStateOf(false) }

    GalleryExample("Recents (rememberRecent, maxRecent = 3)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExampleNote("Select items and reopen Spotlight to see them pinned under Recent with a Clear action.")
            KiraSpotlightTrigger(text = "Search with recents", showShortcut = false, onClick = { open = true })
        }
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            items = resources,
            rememberRecent = true,
            maxRecent = 3,
        )
    }
}

@Composable
private fun PreviewSpotlightDemo(resources: List<KiraSpotlightItem<String>>) {
    val colors = KiraTheme.colors
    var open by remember { mutableStateOf(false) }

    GalleryExample("Preview (Live Highlighted Item Details)") {
        KiraSpotlightTrigger(text = "Search with a preview", showShortcut = false, onClick = { open = true })
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            items = resources,
            renderPreview = { item ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText(item.label, style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium))
                    item.description?.let { ExampleNote(it) }
                    ExampleNote("Resource ID: ${item.id}. Press Enter or tap to open.")
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompositionSpotlightDemo() {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    var composedOpen by remember { mutableStateOf(false) }
    var inlineQuery by remember { mutableStateOf("") }
    var selection by remember { mutableStateOf("Nothing selected.") }

    GalleryExample("Composition (Inline Search, Recent & Suggestions)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraSpotlightTrigger(text = "Open composed search", showShortcut = false, onClick = { composedOpen = true })
            KiraSpotlight<Unit>(
                open = composedOpen,
                onOpenChange = { composedOpen = it },
                groups = listOf(
                    KiraSpotlightGroup(
                        id = "actions",
                        label = "Actions",
                        items = listOf(KiraSpotlightItem("create", "Create a project")),
                    ),
                ),
                onSelect = { selection = "Created a project." },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(radius.shapeLg)
                    .border(1.dp, colors.border, radius.shapeLg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KiraInput(
                    value = inlineQuery,
                    onValueChange = { inlineQuery = it },
                    placeholder = "Search resources…",
                    clearable = true,
                )
                ExampleNote("Typed: ${inlineQuery.ifEmpty { "Nothing yet." }}")
                BasicText("Recent searches", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
                KiraButton(
                    text = "Product roadmap",
                    variant = KiraButtonVariant.Ghost,
                    size = KiraButtonSize.Sm,
                    onClick = { selection = "Opened Product roadmap" },
                )
                BasicText("Suggestions", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Create a project", "Import work").forEach { suggestion ->
                        KiraButton(
                            text = suggestion,
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { selection = suggestion },
                        )
                    }
                }
            }
            ExampleNote(selection)
        }
    }
}

@Composable
private fun KiraSpotlightDemo(
    resources: List<KiraSpotlightItem<String>>,
    scopes: List<KiraSpotlightScope>,
) {
    var open by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("Choose a workspace resource.") }

    GalleryExample("Kira (All-In-One Scopes + Recents + Preview + ⌘K)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KiraSpotlightTrigger(text = "Search workspace", showShortcut = true, onClick = { open = true })
            ExampleNote(destination)
        }
        KiraSpotlight(
            open = open,
            onOpenChange = { open = it },
            items = resources,
            scopes = scopes,
            rememberRecent = true,
            renderPreview = { item -> ExampleNote(item.description ?: item.label) },
            onSelect = { item -> destination = "Opened ${item.label}" },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatesSpotlightDemo() {
    var loadingOpen by remember { mutableStateOf(false) }
    var emptyOpen by remember { mutableStateOf(false) }
    var stressOpen by remember { mutableStateOf(false) }

    GalleryExample("States (Loading, Empty Catalog & Stress/Multilingual)") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraSpotlightTrigger(text = "Loading search", showShortcut = false, onClick = { loadingOpen = true })
            KiraSpotlightTrigger(text = "Empty catalog", showShortcut = false, onClick = { emptyOpen = true })
            KiraSpotlightTrigger(text = "Stress & disabled", showShortcut = false, onClick = { stressOpen = true })
        }

        KiraSpotlight<Unit>(open = loadingOpen, onOpenChange = { loadingOpen = it }, loading = true)
        KiraSpotlight<Unit>(open = emptyOpen, onOpenChange = { emptyOpen = it }, items = emptyList())
        KiraSpotlight<Unit>(
            open = stressOpen,
            onOpenChange = { stressOpen = it },
            items = listOf(
                KiraSpotlightItem(
                    id = "restricted",
                    label = "Financial report",
                    disabled = true,
                    disabledReason = "Ask Finance for access.",
                ),
                KiraSpotlightItem(
                    id = "long",
                    label = "Release readiness and international customer onboarding — 日本語 العربية ไทย",
                    description = "A_very_long_unbroken_resource_identifier_012345678901234567890123456789",
                ),
            ),
        )
    }
}

@Composable
private fun FolderGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(color, Offset(size.width * 0.1f, size.height * 0.25f), Size(size.width * 0.8f, size.height * 0.6f), CornerRadius(2.dp.toPx()), style = stroke)
        drawLine(color, Offset(size.width * 0.1f, size.height * 0.25f), Offset(size.width * 0.42f, size.height * 0.25f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun FileGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(color, Offset(size.width * 0.2f, size.height * 0.1f), Size(size.width * 0.6f, size.height * 0.8f), CornerRadius(2.dp.toPx()), style = stroke)
        drawLine(color, Offset(size.width * 0.34f, size.height * 0.45f), Offset(size.width * 0.66f, size.height * 0.45f), strokeWidth = 1.3.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.34f, size.height * 0.65f), Offset(size.width * 0.66f, size.height * 0.65f), strokeWidth = 1.3.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun UserGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = size.minDimension * 0.2f, center = Offset(size.width * 0.5f, size.height * 0.32f), style = stroke)
        drawArc(color, 180f, 180f, false, Offset(size.width * 0.18f, size.height * 0.58f), Size(size.width * 0.64f, size.height * 0.48f), style = stroke)
    }
}
