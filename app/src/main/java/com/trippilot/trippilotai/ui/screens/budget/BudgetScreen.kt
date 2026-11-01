package com.trippilot.trippilotai.ui.screens.budget

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trippilot.trippilotai.domain.model.BudgetItem
import com.trippilot.trippilotai.ui.components.BackTopBar
import com.trippilot.trippilotai.ui.components.BannerTone
import com.trippilot.trippilotai.ui.components.EmptyState
import com.trippilot.trippilotai.ui.components.InfoBanner
import com.trippilot.trippilotai.ui.components.PrimaryActionButton
import com.trippilot.trippilotai.ui.components.TripSummaryHeader
import com.trippilot.trippilotai.ui.components.icon
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.util.formatInr
import com.trippilot.trippilotai.ui.viewmodel.BudgetUiState
import com.trippilot.trippilotai.ui.viewmodel.BudgetViewModel

@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    onPlanTrip: () -> Unit,
    onCheckout: () -> Unit,
    viewModel: BudgetViewModel = viewModel(factory = BudgetViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BudgetContent(state, onBack, onPlanTrip, onCheckout, viewModel::saveTrip)
}

@Composable
fun BudgetContent(
    state: BudgetUiState,
    onBack: () -> Unit,
    onPlanTrip: () -> Unit,
    onCheckout: () -> Unit,
    onSave: () -> Unit,
) {
    val plan = state.plan
    Scaffold(
        topBar = {
            BackTopBar("Budget summary", onBack = onBack) {
                if (plan != null) {
                    TextButton(onClick = onSave, enabled = !state.isSaved) {
                        Icon(if (state.isSaved) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (state.isSaved) "Saved" else "Save")
                    }
                }
            }
        },
        bottomBar = {
            if (plan != null) {
                Surface(shadowElevation = 8.dp) {
                    PrimaryActionButton(
                        "Proceed to Checkout",
                        onCheckout,
                        Modifier.padding(16.dp),
                        icon = Icons.Default.ShoppingCartCheckout,
                    )
                }
            }
        },
    ) { padding ->
        if (plan == null) {
            EmptyState(
                icon = Icons.Default.Payments,
                title = "No trip to estimate",
                message = "Plan a trip first and we'll break down the estimated costs here.",
                actionLabel = "Plan a Trip",
                onAction = onPlanTrip,
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TripSummaryHeader(plan, height = 140)

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Estimated total trip cost", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                    Text(formatInr(plan.budget.total), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "≈ ${formatInr(plan.budget.total / plan.request.travellers)} per person · " +
                            "your range ${formatInr(plan.request.budgetMin)} – ${formatInr(plan.request.budgetMax)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
            }

            when {
                state.isOverBudget -> InfoBanner(
                    "This estimate is above your budget range. Ask the assistant for cheaper options.",
                    tone = BannerTone.Warning,
                )
                state.isUnderBudget -> InfoBanner("This estimate is below your budget range — room for upgrades!")
                else -> InfoBanner("This estimate fits within your budget range.")
            }

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Cost breakdown", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    plan.budget.items.forEachIndexed { index, item ->
                        BudgetRow(item, fraction = item.amount.toFloat() / plan.budget.total.coerceAtLeast(1))
                        if (index != plan.budget.items.lastIndex) HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 2.dp)
                    Row {
                        Text("Estimated total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(formatInr(plan.budget.total), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            InfoBanner(
                "All costs are estimates generated from mock data — not live prices or a quote. " +
                    "Actual prices will vary.",
                icon = Icons.Outlined.Info,
                tone = BannerTone.Warning,
            )
        }
    }
}

@Composable
private fun BudgetRow(item: BudgetItem, fraction: Float) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row {
                Text(item.category.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(formatInr(item.amount), style = MaterialTheme.typography.titleSmall)
            }
            Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                drawStopIndicator = {},
                gapSize = 0.dp,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun BudgetPreview() {
    TripPilotAITheme { BudgetContent(BudgetUiState(PreviewData.plan), {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun BudgetEmptyPreview() {
    TripPilotAITheme { BudgetContent(BudgetUiState(), {}, {}, {}, {}) }
}
