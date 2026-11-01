package com.trippilot.trippilotai.ui.screens.dev

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.ui.components.BackTopBar
import com.trippilot.trippilotai.ui.components.InfoBanner
import com.trippilot.trippilotai.ui.screens.checkout.PaymentStateActions
import com.trippilot.trippilotai.ui.screens.checkout.PaymentStateContent
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.viewmodel.PaymentUiState

private val sampleStates = listOf(
    "Processing" to PaymentUiState.Processing(PaymentMethod.UPI),
    "Success" to PaymentUiState.Success("MOCK-482913", PaymentMethod.UPI),
    "Failed" to PaymentUiState.Failed(PaymentMethod.CARD, "The simulated Credit / Debit card payment was declined."),
    "Cancelled" to PaymentUiState.Cancelled(PaymentMethod.WALLET),
)

/**
 * Development-only gallery for the four simulated payment states, rendered with sample data.
 * Reachable from Home's profile menu and the checkout dev panel in debug builds.
 */
@Composable
fun PaymentStatesPreviewScreen(onBack: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            Column {
                BackTopBar("Dev: payment states", onBack = onBack)
                PrimaryScrollableTabRow(selectedTabIndex = selected, containerColor = MaterialTheme.colorScheme.background, edgePadding = 16.dp) {
                    sampleStates.forEachIndexed { index, (label, _) ->
                        Tab(selected = selected == index, onClick = { selected = index }, text = { Text(label) })
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            InfoBanner("Preview only — buttons are inactive and no trips are changed.", Modifier.padding(16.dp))
            PaymentStateContent(sampleStates[selected].second, PreviewData.plan, PaymentStateActions())
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PaymentStatesPreviewScreenPreview() {
    TripPilotAITheme { PaymentStatesPreviewScreen(onBack = {}) }
}
