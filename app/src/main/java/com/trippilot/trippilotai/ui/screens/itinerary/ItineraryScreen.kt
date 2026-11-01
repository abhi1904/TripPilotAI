package com.trippilot.trippilotai.ui.screens.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trippilot.trippilotai.domain.model.Activity
import com.trippilot.trippilotai.domain.model.ChatMessage
import com.trippilot.trippilotai.domain.model.ItineraryDay
import com.trippilot.trippilotai.ui.components.BackTopBar
import com.trippilot.trippilotai.ui.components.EmptyState
import com.trippilot.trippilotai.ui.components.InfoBanner
import com.trippilot.trippilotai.ui.components.PrimaryActionButton
import com.trippilot.trippilotai.ui.components.TripSummaryHeader
import com.trippilot.trippilotai.ui.components.icon
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.util.formatDate
import com.trippilot.trippilotai.ui.util.formatInr
import com.trippilot.trippilotai.ui.viewmodel.ItineraryUiState
import com.trippilot.trippilotai.ui.viewmodel.ItineraryViewModel
import kotlinx.coroutines.launch

private val suggestions = listOf("Make this day more relaxed", "Find cheaper options", "Add local food spots", "What if it rains?")

@Composable
fun ItineraryScreen(
    initialTab: Int,
    onPlanTrip: () -> Unit,
    onViewBudget: () -> Unit,
    viewModel: ItineraryViewModel = viewModel(factory = ItineraryViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ItineraryContent(
        state = state,
        initialTab = initialTab,
        onPlanTrip = onPlanTrip,
        onViewBudget = onViewBudget,
        onSelectDay = viewModel::selectDay,
        onSave = viewModel::saveTrip,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::send,
    )
}

@Composable
fun ItineraryContent(
    state: ItineraryUiState,
    initialTab: Int,
    onPlanTrip: () -> Unit,
    onViewBudget: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onSave: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
) {
    var tab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val plan = state.plan

    Scaffold(
        topBar = {
            Column {
                BackTopBar("AI Itinerary", onBack = null)
                if (plan != null) {
                    PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Itinerary") })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Ask assistant") })
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (plan != null && tab == 0) {
                Surface(shadowElevation = 8.dp) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                onSave()
                                scope.launch { snackbarHostState.showSnackbar("Trip saved to My Trips — no payment needed") }
                            },
                            enabled = !state.isSaved,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Icon(if (state.isSaved) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.isSaved) "Saved" else "Save trip")
                        }
                        PrimaryActionButton("View budget", onViewBudget, Modifier.weight(1f), icon = Icons.Default.Payments)
                    }
                }
            }
        },
    ) { padding ->
        when {
            plan == null -> EmptyState(
                icon = Icons.Default.AutoAwesome,
                title = "No itinerary yet",
                message = "Plan a trip to see a day-by-day itinerary and chat with your trip assistant.",
                actionLabel = "Plan a Trip",
                onAction = onPlanTrip,
                modifier = Modifier.padding(padding),
            )
            tab == 0 -> ItineraryTab(state, onSelectDay, Modifier.padding(padding))
            else -> ChatTab(state, onInputChange, onSend, Modifier.padding(padding))
        }
    }
}

@Composable
private fun ItineraryTab(state: ItineraryUiState, onSelectDay: (Int) -> Unit, modifier: Modifier = Modifier) {
    val plan = state.plan ?: return
    val day = plan.days.getOrElse(state.selectedDay - 1) { plan.days.first() }
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { TripSummaryHeader(plan) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryStat("Est. total", formatInr(plan.budget.total), Modifier.weight(1f))
                SummaryStat("Interests", plan.request.interests.joinToString { it.label }, Modifier.weight(1.4f))
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(plan.days, key = { it.dayNumber }) { d ->
                    FilterChip(
                        selected = d.dayNumber == day.dayNumber,
                        onClick = { onSelectDay(d.dayNumber) },
                        label = {
                            Column(Modifier.padding(vertical = 6.dp)) {
                                Text("Day ${d.dayNumber}", style = MaterialTheme.typography.labelLarge)
                                Text(formatDate(d.dateMillis, "EEE d MMM"), style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }
        }
        item { DayCard(day) }
        item { InfoBanner("Mock itinerary for UI review. Times and costs are illustrative estimates.") }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 2)
        }
    }
}

@Composable
fun DayCard(day: ItineraryDay, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Day ${day.dayNumber} · ${formatDate(day.dateMillis, "EEEE, d MMM")}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(day.title, style = MaterialTheme.typography.titleLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Est. daily cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatInr(day.estimatedCost), style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            day.activities.forEachIndexed { index, activity ->
                ActivityRow(activity, isLast = index == day.activities.lastIndex)
            }
        }
    }
}

@Composable
private fun ActivityRow(activity: Activity, isLast: Boolean) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            activity.time,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(52.dp).padding(top = 8.dp),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(36.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(activity.category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            if (!isLast) {
                Box(Modifier.width(2.dp).height(44.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(top = 4.dp, bottom = if (isLast) 0.dp else 12.dp)) {
            Row {
                Text(activity.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (activity.costPerPerson > 0) {
                    Text("${formatInr(activity.costPerPerson)}/pp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(activity.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChatTab(
    state: ItineraryUiState,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val itemCount = state.messages.size + if (state.isAssistantTyping) 1 else 0
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }
    Column(modifier.fillMaxSize().imePadding()) {
        InfoBanner(
            "Mock assistant — replies are canned and don't change your itinerary yet. Context: Day ${state.selectedDay}.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.messages, key = { it.id }) { ChatBubble(it) }
            if (state.isAssistantTyping) {
                item { ChatBubble(ChatMessage(-1, fromUser = false, text = "Thinking…")) }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(onClick = { onSend(suggestion) }, label = { Text(suggestion) }, enabled = !state.isAssistantTyping)
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask to change your plan…") },
                shape = RoundedCornerShape(28.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend(state.input) }),
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = { onSend(state.input) },
                enabled = state.input.isNotBlank() && !state.isAssistantTyping,
                modifier = Modifier.size(52.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val fromUser = message.fromUser
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start) {
        if (!fromUser) {
            Box(
                Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            color = if (fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (fromUser) 20.dp else 4.dp, bottomEnd = if (fromUser) 4.dp else 20.dp,
            ),
            shadowElevation = if (fromUser) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Text(message.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ItineraryPreview() {
    TripPilotAITheme {
        ItineraryContent(ItineraryUiState(plan = PreviewData.plan, selectedDay = 2), 0, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatPreview() {
    TripPilotAITheme {
        ItineraryContent(
            ItineraryUiState(
                plan = PreviewData.plan,
                messages = listOf(
                    ChatMessage(1, false, "Hi! I'm your trip assistant for Goa."),
                    ChatMessage(2, true, "Make day 2 more relaxed"),
                    ChatMessage(3, false, "Sure — I'd keep only the morning plan on Day 2."),
                ),
                isAssistantTyping = true,
            ),
            1, {}, {}, {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItineraryEmptyPreview() {
    TripPilotAITheme { ItineraryContent(ItineraryUiState(), 0, {}, {}, {}, {}, {}, {}) }
}
