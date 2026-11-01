package com.trippilot.trippilotai.ui.screens.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.ui.components.BackTopBar
import com.trippilot.trippilotai.ui.components.EmptyState
import com.trippilot.trippilotai.ui.components.PaymentStatusPill
import com.trippilot.trippilotai.ui.components.PrimaryActionButton
import com.trippilot.trippilotai.ui.components.TripSummaryHeader
import com.trippilot.trippilotai.ui.screens.itinerary.DayCard
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.util.formatInr
import com.trippilot.trippilotai.ui.viewmodel.TripDetailViewModel
import com.trippilot.trippilotai.util.todayUtcMillis

@Composable
fun TripDetailScreen(
    onBack: () -> Unit,
    onOpenItinerary: () -> Unit,
    onCheckout: () -> Unit,
    viewModel: TripDetailViewModel = viewModel(factory = TripDetailViewModel.Factory),
) {
    val trip by viewModel.trip.collectAsStateWithLifecycle()
    TripDetailContent(
        trip = trip,
        onBack = onBack,
        onOpenItinerary = { trip?.let { viewModel.open(it.plan); onOpenItinerary() } },
        onCheckout = { trip?.let { viewModel.open(it.plan); onCheckout() } },
    )
}

@Composable
fun TripDetailContent(trip: Trip?, onBack: () -> Unit, onOpenItinerary: () -> Unit, onCheckout: () -> Unit) {
    Scaffold(topBar = { BackTopBar("Trip details", onBack = onBack) }) { padding ->
        if (trip == null) {
            EmptyState(
                icon = Icons.Default.SearchOff,
                title = "Trip not found",
                message = "This trip may have been removed.",
                actionLabel = "Go back",
                onAction = onBack,
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        val plan = trip.plan
        val canPay = trip.paymentStatus != PaymentStatus.PAID && !trip.isCompleted(todayUtcMillis())
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TripSummaryHeader(plan, height = 180)
            Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        Text("Status", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TripTimingPill(trip.isCompleted(todayUtcMillis()))
                    }
                    DetailLine("Payment") { PaymentStatusPill(trip.paymentStatus) }
                    trip.orderId?.let { DetailLine("Mock order ID") { Text(it, style = MaterialTheme.typography.titleSmall) } }
                    trip.paymentMethod?.let { DetailLine("Simulated method") { Text(it.label, style = MaterialTheme.typography.titleSmall) } }
                    DetailLine("Estimated total") { Text(formatInr(plan.budget.total), style = MaterialTheme.typography.titleSmall) }
                }
            }

            PrimaryActionButton("Open itinerary & assistant", onOpenItinerary, icon = Icons.Default.AutoAwesome)
            if (canPay) {
                OutlinedButton(onClick = onCheckout, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pay now (simulated)")
                }
            }

            Text("Itinerary", style = MaterialTheme.typography.titleLarge)
            plan.days.forEach { DayCard(it) }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        value()
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun TripDetailPreview() {
    TripPilotAITheme { TripDetailContent(PreviewData.trip, {}, {}, {}) }
}
