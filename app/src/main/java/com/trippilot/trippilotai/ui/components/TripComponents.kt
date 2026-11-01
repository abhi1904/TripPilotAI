package com.trippilot.trippilotai.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trippilot.trippilotai.data.mock.MockDestinations
import com.trippilot.trippilotai.domain.model.ActivityCategory
import com.trippilot.trippilotai.domain.model.BudgetCategory
import com.trippilot.trippilotai.domain.model.Destination
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.ui.theme.Sand80
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme
import com.trippilot.trippilotai.ui.util.formatDateRange
import com.trippilot.trippilotai.ui.util.formatInr

val ActivityCategory.icon: ImageVector
    get() = when (this) {
        ActivityCategory.TRAVEL -> Icons.Default.Flight
        ActivityCategory.STAY -> Icons.Default.Hotel
        ActivityCategory.NATURE -> Icons.Default.Landscape
        ActivityCategory.CULTURE -> Icons.Default.Museum
        ActivityCategory.FOOD -> Icons.Default.Restaurant
        ActivityCategory.ADVENTURE -> Icons.Default.Hiking
        ActivityCategory.RELAXATION -> Icons.Default.Spa
        ActivityCategory.SHOPPING -> Icons.Default.ShoppingBag
    }

val Interest.icon: ImageVector
    get() = when (this) {
        Interest.NATURE -> Icons.Default.Landscape
        Interest.CULTURE -> Icons.Default.Museum
        Interest.FOOD -> Icons.Default.Restaurant
        Interest.ADVENTURE -> Icons.Default.Hiking
        Interest.RELAXATION -> Icons.Default.Spa
        Interest.SHOPPING -> Icons.Default.ShoppingBag
    }

val BudgetCategory.icon: ImageVector
    get() = when (this) {
        BudgetCategory.ACCOMMODATION -> Icons.Default.Hotel
        BudgetCategory.TRANSPORT -> Icons.Default.Flight
        BudgetCategory.ACTIVITIES -> Icons.Default.LocalActivity
        BudgetCategory.FOOD -> Icons.Default.Restaurant
        BudgetCategory.OTHER -> Icons.Default.MoreHoriz
    }

val PaymentMethod.icon: ImageVector
    get() = when (this) {
        PaymentMethod.UPI -> Icons.Default.QrCode2
        PaymentMethod.CARD -> Icons.Default.CreditCard
        PaymentMethod.NET_BANKING -> Icons.Default.AccountBalance
        PaymentMethod.WALLET -> Icons.Default.AccountBalanceWallet
    }

private val scrim = Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))

@Composable
fun DestinationCard(destination: Destination, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.width(200.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(130.dp)) {
            SceneryArt(destination.scene, Modifier.fillMaxSize())
            Row(
                Modifier.align(Alignment.TopEnd).padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Sand80, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(3.dp))
                Text("${destination.rating}", style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
        Column(Modifier.padding(14.dp)) {
            Text(destination.name, style = MaterialTheme.typography.titleMedium)
            Text(
                destination.region,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                destination.tagline,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "From ${formatInr(destination.fromPricePerPerson)} / person*",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** Scenic banner with destination name, dates and traveller count. */
@Composable
fun TripSummaryHeader(plan: TripPlan, modifier: Modifier = Modifier, height: Int = 160) {
    Box(modifier.fillMaxWidth().height(height.dp).clip(MaterialTheme.shapes.large)) {
        SceneryArt(plan.destination.scene, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(scrim))
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(
                "${plan.destination.name}, ${plan.destination.region}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SummaryChip(Icons.Default.CalendarMonth, formatDateRange(plan.request.startDateMillis, plan.request.endDateMillis))
                SummaryChip(Icons.Default.Group, "${plan.request.travellers}")
                SummaryChip(Icons.AutoMirrored.Filled.DirectionsWalk, "${plan.days.size} days")
            }
        }
    }
}

@Composable
private fun SummaryChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Preview
@Composable
private fun DestinationCardPreview() {
    TripPilotAITheme { DestinationCard(MockDestinations.all.first(), onClick = {}) }
}
