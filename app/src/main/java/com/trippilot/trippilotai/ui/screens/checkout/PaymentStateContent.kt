package com.trippilot.trippilotai.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.ui.components.BannerTone
import com.trippilot.trippilotai.ui.components.InfoBanner
import com.trippilot.trippilotai.ui.components.PrimaryActionButton
import com.trippilot.trippilotai.ui.theme.Danger
import com.trippilot.trippilotai.ui.theme.DangerContainer
import com.trippilot.trippilotai.ui.theme.Success
import com.trippilot.trippilotai.ui.theme.SuccessContainer
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.theme.Warning
import com.trippilot.trippilotai.ui.theme.WarningContainer
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.util.formatDateRange
import com.trippilot.trippilotai.ui.util.formatInr
import com.trippilot.trippilotai.ui.viewmodel.PaymentUiState

/** Callbacks for the actions shown in the non-idle payment states. */
data class PaymentStateActions(
    val onCancel: () -> Unit = {},
    val onViewTrip: () -> Unit = {},
    val onDone: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onChangeMethod: () -> Unit = {},
    val onPayNow: () -> Unit = {},
)

/** Full-screen UI for Processing, Success, Failed and Cancelled simulated payments. */
@Composable
fun PaymentStateContent(
    state: PaymentUiState,
    plan: TripPlan,
    actions: PaymentStateActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        when (state) {
            PaymentUiState.Idle -> Unit
            is PaymentUiState.Processing -> {
                CircularProgressIndicator(Modifier.size(72.dp), strokeWidth = 6.dp)
                StateText("Processing simulated payment…", "Confirming ${formatInr(plan.budget.total)} via ${state.method.label}. Please don't close the app.")
                LinearProgressIndicator(Modifier.fillMaxWidth(0.7f))
                SimulationNote()
                OutlinedButton(onClick = actions.onCancel, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
                    Text("Cancel payment")
                }
            }
            is PaymentUiState.Success -> {
                StatusIcon(Icons.Default.CheckCircle, Success, SuccessContainer)
                StateText("Booking confirmed (simulated)", "Your trip is saved and marked as paid in this demo. No real money was transferred.")
                OrderDetails(plan, state.method, orderId = state.orderId)
                SimulationNote()
                PrimaryActionButton("View trip details", actions.onViewTrip)
                OutlinedButton(onClick = actions.onDone, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
                    Text("Back to home")
                }
            }
            is PaymentUiState.Failed -> {
                StatusIcon(Icons.Default.ErrorOutline, Danger, DangerContainer)
                StateText("Payment failed", "${state.message} Nothing was charged — this is a simulation.")
                OrderDetails(plan, state.method, orderId = null)
                PrimaryActionButton("Retry Payment", actions.onRetry)
                OutlinedButton(onClick = actions.onChangeMethod, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
                    Text("Change Payment Method")
                }
            }
            is PaymentUiState.Cancelled -> {
                StatusIcon(Icons.Default.DoNotDisturbOn, Warning, WarningContainer)
                StateText("Payment cancelled", "You cancelled the simulated payment. Your trip is still saved — you can pay any time.")
                OrderDetails(plan, state.method, orderId = null)
                PrimaryActionButton("Pay Now", actions.onPayNow)
                OutlinedButton(onClick = actions.onChangeMethod, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
                    Text("Change Payment Method")
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(icon: ImageVector, tint: Color, container: Color) {
    Box(Modifier.size(96.dp).background(container, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(56.dp))
    }
}

@Composable
private fun StateText(title: String, message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun OrderDetails(plan: TripPlan, method: PaymentMethod, orderId: String?) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (orderId != null) DetailRow("Mock order ID", orderId)
            DetailRow("Trip", plan.destination.name)
            DetailRow("Dates", formatDateRange(plan.request.startDateMillis, plan.request.endDateMillis))
            DetailRow("Simulated method", method.label)
            DetailRow("Estimated amount", formatInr(plan.budget.total))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun SimulationNote() {
    InfoBanner("Local simulation only — no payment gateway, no money transferred.", tone = BannerTone.Warning)
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun ProcessingPreview() {
    TripPilotAITheme { PaymentStateContent(PaymentUiState.Processing(PaymentMethod.UPI), PreviewData.plan, PaymentStateActions()) }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun SuccessPreview() {
    TripPilotAITheme { PaymentStateContent(PaymentUiState.Success("MOCK-482913", PaymentMethod.UPI), PreviewData.plan, PaymentStateActions()) }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun FailedPreview() {
    TripPilotAITheme {
        PaymentStateContent(PaymentUiState.Failed(PaymentMethod.CARD, "The simulated card payment was declined."), PreviewData.plan, PaymentStateActions())
    }
}

@Preview(showBackground = true, heightDp = 760)
@Composable
private fun CancelledPreview() {
    TripPilotAITheme { PaymentStateContent(PaymentUiState.Cancelled(PaymentMethod.WALLET), PreviewData.plan, PaymentStateActions()) }
}
