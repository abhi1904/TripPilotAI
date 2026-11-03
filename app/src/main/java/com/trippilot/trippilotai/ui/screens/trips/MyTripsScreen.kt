package com.trippilot.trippilotai.ui.screens.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trippilot.trippilotai.BuildConfig
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.ui.components.BackTopBar
import com.trippilot.trippilotai.ui.components.EmptyState
import com.trippilot.trippilotai.ui.components.PaymentStatusPill
import com.trippilot.trippilotai.ui.components.PrimaryActionButton
import com.trippilot.trippilotai.ui.components.SceneryArt
import com.trippilot.trippilotai.ui.components.StatusPill
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.util.formatDateRange
import com.trippilot.trippilotai.ui.util.formatInr
import com.trippilot.trippilotai.ui.viewmodel.MyTripsUiState
import com.trippilot.trippilotai.ui.viewmodel.MyTripsViewModel
import com.trippilot.trippilotai.ui.viewmodel.TripFilter

@Composable
fun MyTripsScreen(
    onTripClick: (tripId: String) -> Unit,
    onPlanTrip: () -> Unit,
    viewModel: MyTripsViewModel = viewModel(factory = MyTripsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyTripsContent(state, viewModel::setFilter, onTripClick, onPlanTrip, viewModel::clearAll, viewModel::restoreSamples, viewModel::retry)
}

@Composable
fun MyTripsContent(
    state: MyTripsUiState,
    onFilterChange: (TripFilter) -> Unit,
    onTripClick: (String) -> Unit,
    onPlanTrip: () -> Unit,
    onDevClear: () -> Unit,
    onDevRestore: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            BackTopBar("My Trips", onBack = null) {
                if (BuildConfig.DEBUG) DevMenu(onDevClear, onDevRestore)
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.isSyncing) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            state.syncError?.let { message ->
                SyncErrorCard(message, onRetry, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
            }
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TripFilter.entries.forEach { filter ->
                    FilterChip(selected = state.filter == filter, onClick = { onFilterChange(filter) }, label = { Text(filter.label) })
                }
            }
            if (state.trips.isEmpty() && state.isSyncing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading your trips…", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (state.trips.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Luggage,
                    title = if (state.hasAnyTrips) "No ${state.filter.label.lowercase()} trips" else "No trips yet",
                    message = if (state.hasAnyTrips) "Try another filter, or plan something new."
                    else "Saved itineraries appear here — with or without payment.",
                    actionLabel = "Plan Another Trip",
                    onAction = onPlanTrip,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(state.trips, key = { it.id }) { trip ->
                        TripCard(trip, isCompleted = trip.isCompleted(state.todayMillis), onClick = { onTripClick(trip.id) })
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        PrimaryActionButton("Plan Another Trip", onPlanTrip, icon = Icons.Default.Add)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncErrorCard(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun DevMenu(onClear: () -> Unit, onRestore: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Developer options") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Dev: clear all trips") }, onClick = { open = false; onClear() })
            DropdownMenuItem(text = { Text("Dev: restore sample trips") }, onClick = { open = false; onRestore() })
        }
    }
}

@Composable
fun TripCard(trip: Trip, isCompleted: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val plan = trip.plan
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SceneryArt(plan.destination.scene, Modifier.size(84.dp).clip(MaterialTheme.shapes.medium))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plan.destination.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TripTimingPill(isCompleted)
                }
                Text(
                    "${formatDateRange(plan.request.startDateMillis, plan.request.endDateMillis)} · ${plan.request.travellers} travellers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Est. ${formatInr(plan.budget.total)}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    PaymentStatusPill(trip.paymentStatus)
                }
            }
        }
    }
}

@Composable
fun TripTimingPill(isCompleted: Boolean) {
    StatusPill(
        text = if (isCompleted) "Completed" else "Upcoming",
        container = if (isCompleted) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer,
        content = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Preview(showBackground = true)
@Composable
private fun MyTripsPreview() {
    TripPilotAITheme {
        MyTripsContent(MyTripsUiState(trips = listOf(PreviewData.trip), hasAnyTrips = true, todayMillis = 0), {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MyTripsEmptyPreview() {
    TripPilotAITheme { MyTripsContent(MyTripsUiState(), {}, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun MyTripsSyncErrorPreview() {
    TripPilotAITheme {
        MyTripsContent(
            MyTripsUiState(syncError = "Couldn't load your trips. Can't reach the server — is the backend running?"),
            {}, {}, {}, {}, {}, {},
        )
    }
}
