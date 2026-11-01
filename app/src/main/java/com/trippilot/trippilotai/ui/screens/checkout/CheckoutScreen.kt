package com.trippilot.trippilotai.ui.screens.checkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trippilot.trippilotai.BuildConfig
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.SimulatedOutcome
import com.trippilot.trippilotai.ui.components.BackTopBar
import com.trippilot.trippilotai.ui.components.BannerTone
import com.trippilot.trippilotai.ui.components.EmptyState
import com.trippilot.trippilotai.ui.components.InfoBanner
import com.trippilot.trippilotai.ui.components.PrimaryActionButton
import com.trippilot.trippilotai.ui.components.icon
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.util.formatDateRange
import com.trippilot.trippilotai.ui.util.formatInr
import com.trippilot.trippilotai.ui.viewmodel.CheckoutUiState
import com.trippilot.trippilotai.ui.viewmodel.CheckoutViewModel
import com.trippilot.trippilotai.ui.viewmodel.PaymentUiState

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPlanTrip: () -> Unit,
    onViewTrip: (tripId: String) -> Unit,
    onDone: () -> Unit,
    onOpenDevPaymentStates: () -> Unit,
    viewModel: CheckoutViewModel = viewModel(factory = CheckoutViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Back while processing cancels; back from failed/cancelled returns to the form.
    BackHandler(enabled = state.payment is PaymentUiState.Processing) { viewModel.cancelPayment() }
    BackHandler(enabled = state.payment is PaymentUiState.Failed || state.payment is PaymentUiState.Cancelled) {
        viewModel.resetToForm()
    }

    CheckoutContent(
        state = state,
        onBack = onBack,
        onPlanTrip = onPlanTrip,
        onSelectMethod = viewModel::selectMethod,
        onSelectOutcome = viewModel::setSimulatedOutcome,
        onPay = viewModel::pay,
        onOpenDevPaymentStates = onOpenDevPaymentStates,
        paymentActions = PaymentStateActions(
            onCancel = viewModel::cancelPayment,
            onViewTrip = { state.plan?.let { onViewTrip(it.id) } },
            onDone = onDone,
            onRetry = viewModel::pay,
            onChangeMethod = viewModel::resetToForm,
            onPayNow = viewModel::pay,
        ),
    )
}

@Composable
fun CheckoutContent(
    state: CheckoutUiState,
    onBack: () -> Unit,
    onPlanTrip: () -> Unit,
    onSelectMethod: (PaymentMethod) -> Unit,
    onSelectOutcome: (SimulatedOutcome) -> Unit,
    onPay: () -> Unit,
    onOpenDevPaymentStates: () -> Unit,
    paymentActions: PaymentStateActions,
    showDevTools: Boolean = BuildConfig.DEBUG,
) {
    val plan = state.plan
    val isForm = state.payment is PaymentUiState.Idle
    Scaffold(
        topBar = {
            BackTopBar(
                title = if (isForm) "Checkout" else "Payment",
                onBack = when (state.payment) {
                    PaymentUiState.Idle, is PaymentUiState.Success -> onBack
                    is PaymentUiState.Failed, is PaymentUiState.Cancelled -> paymentActions.onChangeMethod
                    is PaymentUiState.Processing -> null
                },
            )
        },
        bottomBar = {
            if (plan != null && isForm) {
                Surface(shadowElevation = 8.dp) {
                    PrimaryActionButton(
                        "Pay ${formatInr(plan.budget.total)} (simulated)",
                        onPay,
                        Modifier.padding(16.dp),
                        icon = Icons.Default.Lock,
                    )
                }
            }
        },
    ) { padding ->
        when {
            plan == null -> EmptyState(
                icon = Icons.Default.ShoppingCartCheckout,
                title = "Nothing to check out",
                message = "Plan a trip and review its budget before checking out.",
                actionLabel = "Plan a Trip",
                onAction = onPlanTrip,
                modifier = Modifier.padding(padding),
            )
            !isForm -> PaymentStateContent(state.payment, plan, paymentActions, Modifier.padding(padding))
            else -> CheckoutForm(state, plan, onSelectMethod, onSelectOutcome, onOpenDevPaymentStates, showDevTools, Modifier.padding(padding))
        }
    }
}

@Composable
private fun CheckoutForm(
    state: CheckoutUiState,
    plan: TripPlan,
    onSelectMethod: (PaymentMethod) -> Unit,
    onSelectOutcome: (SimulatedOutcome) -> Unit,
    onOpenDevPaymentStates: () -> Unit,
    showDevTools: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InfoBanner(
            "Payment is a local simulation. No real payment gateway is used, no money is moved and " +
                "no card, bank, UPI PIN or other payment details are collected.",
            tone = BannerTone.Warning,
        )

        Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Order summary", style = MaterialTheme.typography.titleMedium)
                SummaryRow("${plan.destination.name}, ${plan.destination.region}", "${plan.days.size} days")
                SummaryRow("Dates", formatDateRange(plan.request.startDateMillis, plan.request.endDateMillis))
                SummaryRow("Travellers", "${plan.request.travellers}")
                HorizontalDivider()
                plan.budget.items.forEach { SummaryRow(it.category.label, formatInr(it.amount)) }
                HorizontalDivider()
                Row {
                    Text("Estimated amount", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(formatInr(plan.budget.total), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Text("Payment method (simulated)", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PaymentMethod.entries.forEach { method ->
                PaymentMethodOption(method, selected = method == state.selectedMethod, onClick = { onSelectMethod(method) })
            }
        }

        if (showDevTools) {
            DevOutcomePanel(state.simulatedOutcome, onSelectOutcome, onOpenDevPaymentStates)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PaymentMethodOption(method: PaymentMethod, selected: Boolean, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(method.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(method.label, style = MaterialTheme.typography.titleSmall)
                Text(method.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}

/** Debug-build-only controls for choosing the simulated result and previewing all states. */
@Composable
private fun DevOutcomePanel(
    outcome: SimulatedOutcome,
    onSelect: (SimulatedOutcome) -> Unit,
    onOpenDevPaymentStates: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Developer: simulated outcome", style = MaterialTheme.typography.titleSmall)
            }
            Text(
                "Debug builds only. Tap \"Cancel payment\" while processing to see the cancelled state.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SimulatedOutcome.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == outcome,
                        onClick = { onSelect(option) },
                        shape = SegmentedButtonDefaults.itemShape(index, SimulatedOutcome.entries.size),
                    ) {
                        Text(if (option == SimulatedOutcome.SUCCESS) "Success" else "Failure")
                    }
                }
            }
            TextButton(onClick = onOpenDevPaymentStates) { Text("Preview all payment states") }
        }
    }
}

@Preview(showBackground = true, heightDp = 1300)
@Composable
private fun CheckoutPreview() {
    TripPilotAITheme {
        CheckoutContent(
            CheckoutUiState(plan = PreviewData.plan),
            {}, {}, {}, {}, {}, {}, PaymentStateActions(), showDevTools = true,
        )
    }
}
