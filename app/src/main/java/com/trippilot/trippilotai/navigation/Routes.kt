package com.trippilot.trippilotai.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val PLAN = "plan?destination={destination}"
    const val ITINERARY = "itinerary?tab={tab}"
    const val BUDGET = "budget"
    const val CHECKOUT = "checkout"
    const val MY_TRIPS = "trips"
    const val TRIP_DETAIL = "trips/{tripId}"
    const val DEV_PAYMENT_STATES = "dev/payment-states"

    const val ARG_DESTINATION = "destination"
    const val ARG_TAB = "tab"
    const val ARG_TRIP_ID = "tripId"

    fun plan(destination: String? = null) =
        if (destination == null) "plan" else "plan?destination=${Uri.encode(destination)}"

    /** tab = 0 for the itinerary, 1 for the assistant chat. */
    fun itinerary(tab: Int = 0) = "itinerary?tab=$tab"

    fun tripDetail(tripId: String) = "trips/${Uri.encode(tripId)}"
}

enum class TopLevelDestination(
    val route: String,
    val navigateRoute: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(Routes.HOME, Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    PLAN(Routes.PLAN, Routes.plan(), "Plan", Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
    ITINERARY(Routes.ITINERARY, Routes.itinerary(), "Itinerary", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    TRIPS(Routes.MY_TRIPS, Routes.MY_TRIPS, "My Trips", Icons.Filled.Luggage, Icons.Outlined.Luggage),
}
