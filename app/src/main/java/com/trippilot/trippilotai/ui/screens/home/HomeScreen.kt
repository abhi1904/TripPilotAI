package com.trippilot.trippilotai.ui.screens.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trippilot.trippilotai.BuildConfig
import com.trippilot.trippilotai.data.mock.MockDestinations
import com.trippilot.trippilotai.domain.model.Destination
import com.trippilot.trippilotai.domain.model.Scene
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.ui.components.DestinationCard
import com.trippilot.trippilotai.ui.components.SceneryArt
import com.trippilot.trippilotai.ui.components.SectionHeader
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.PreviewData
import com.trippilot.trippilotai.ui.util.formatDateRange
import com.trippilot.trippilotai.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onPlanTrip: (destination: String?) -> Unit,
    onOpenItinerary: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenAssistant: () -> Unit,
    onOpenDevPaymentStates: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val currentPlan by viewModel.currentPlan.collectAsStateWithLifecycle()
    HomeContent(
        destinations = MockDestinations.all,
        currentPlan = currentPlan,
        onPlanTrip = onPlanTrip,
        onOpenItinerary = onOpenItinerary,
        onOpenBudget = onOpenBudget,
        onOpenAssistant = onOpenAssistant,
        onOpenDevPaymentStates = onOpenDevPaymentStates,
    )
}

@Composable
fun HomeContent(
    destinations: List<Destination>,
    currentPlan: TripPlan?,
    onPlanTrip: (destination: String?) -> Unit,
    onOpenItinerary: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenAssistant: () -> Unit,
    onOpenDevPaymentStates: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun comingSoon(feature: String) {
        scope.launch { snackbarHostState.showSnackbar("$feature are not part of this UI preview") }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopBar(
                onNotifications = { comingSoon("Notifications") },
                onProfile = { comingSoon("Profiles and accounts") },
                onOpenDevPaymentStates = onOpenDevPaymentStates,
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item { HeroSection(onPlanTrip = { onPlanTrip(null) }, modifier = Modifier.padding(horizontal = 16.dp)) }

            if (currentPlan != null) {
                item { ContinuePlanningCard(currentPlan, onOpenItinerary, Modifier.padding(horizontal = 16.dp)) }
            }

            item {
                SectionHeader("Explore destinations", Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(destinations, key = { it.id }) { destination ->
                        DestinationCard(destination, onClick = { onPlanTrip(destination.name) })
                    }
                }
                Text(
                    "*Indicative mock prices for preview only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                )
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader("Plan smarter")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureShortcut(Icons.Default.AutoAwesome, "AI itinerary", "Day-by-day plans", { onPlanTrip(null) }, Modifier.weight(1f))
                        FeatureShortcut(Icons.Default.Payments, "Budget", "Cost estimates", onOpenBudget, Modifier.weight(1f))
                        FeatureShortcut(Icons.Default.Forum, "Assistant", "Tweak your trip", onOpenAssistant, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(onNotifications: () -> Unit, onProfile: () -> Unit, onOpenDevPaymentStates: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("TripPilot AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Your AI travel companion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onNotifications) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
        }
        Box {
            IconButton(onClick = { if (BuildConfig.DEBUG) menuOpen = true else onProfile() }) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Profile") }, onClick = { menuOpen = false; onProfile() })
                DropdownMenuItem(
                    text = { Text("Dev: preview payment states") },
                    onClick = { menuOpen = false; onOpenDevPaymentStates() },
                )
            }
        }
    }
}

@Composable
private fun HeroSection(onPlanTrip: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(300.dp).clip(MaterialTheme.shapes.extraLarge)) {
        SceneryArt(Scene.BEACH, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.05f), Color(0xFF003736).copy(alpha = 0.85f))),
            ),
        )
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text("Where to next?", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(4.dp))
            Text(
                "Plan your perfect trip\nin minutes",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Personalised itineraries, budgets and help — all in one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPlanTrip,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(52.dp),
            ) {
                Text("Plan a Trip", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ContinuePlanningCard(plan: TripPlan, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SceneryArt(plan.destination.scene, Modifier.size(56.dp).clip(MaterialTheme.shapes.small))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Continue planning", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(plan.destination.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    formatDateRange(plan.request.startDateMillis, plan.request.endDateMillis),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun FeatureShortcut(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomePreview() {
    TripPilotAITheme {
        HomeContent(MockDestinations.all, PreviewData.plan, {}, {}, {}, {}, {})
    }
}
