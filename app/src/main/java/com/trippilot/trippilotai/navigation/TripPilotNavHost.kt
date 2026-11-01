package com.trippilot.trippilotai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.trippilot.trippilotai.BuildConfig
import com.trippilot.trippilotai.ui.screens.budget.BudgetScreen
import com.trippilot.trippilotai.ui.screens.checkout.CheckoutScreen
import com.trippilot.trippilotai.ui.screens.dev.PaymentStatesPreviewScreen
import com.trippilot.trippilotai.ui.screens.home.HomeScreen
import com.trippilot.trippilotai.ui.screens.itinerary.ItineraryScreen
import com.trippilot.trippilotai.ui.screens.plan.PlanTripScreen
import com.trippilot.trippilotai.ui.screens.trips.MyTripsScreen
import com.trippilot.trippilotai.ui.screens.trips.TripDetailScreen

/** Navigates to a bottom-bar destination, keeping one copy of each tab on the back stack. */
fun NavHostController.navigateToTab(route: String, restoreState: Boolean = true) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        this.restoreState = restoreState
    }
}

@Composable
fun TripPilotNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val toPlan: () -> Unit = { navController.navigateToTab(Routes.plan()) }

    NavHost(navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                // A fresh Plan screen is opened when a destination is pre-selected.
                onPlanTrip = { destination ->
                    navController.navigateToTab(Routes.plan(destination), restoreState = destination == null)
                },
                onOpenItinerary = { navController.navigateToTab(Routes.itinerary()) },
                onOpenBudget = { navController.navigate(Routes.BUDGET) },
                onOpenAssistant = { navController.navigateToTab(Routes.itinerary(tab = 1), restoreState = false) },
                onOpenDevPaymentStates = { navController.navigate(Routes.DEV_PAYMENT_STATES) },
            )
        }
        composable(
            Routes.PLAN,
            arguments = listOf(navArgument(Routes.ARG_DESTINATION) { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) {
            PlanTripScreen(onItineraryReady = { navController.navigateToTab(Routes.itinerary(), restoreState = false) })
        }
        composable(
            Routes.ITINERARY,
            arguments = listOf(navArgument(Routes.ARG_TAB) { type = NavType.IntType; defaultValue = 0 }),
        ) { entry ->
            ItineraryScreen(
                initialTab = entry.arguments?.getInt(Routes.ARG_TAB) ?: 0,
                onPlanTrip = toPlan,
                onViewBudget = { navController.navigate(Routes.BUDGET) },
            )
        }
        composable(Routes.BUDGET) {
            BudgetScreen(
                onBack = { navController.popBackStack() },
                onPlanTrip = toPlan,
                onCheckout = { navController.navigate(Routes.CHECKOUT) },
            )
        }
        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onPlanTrip = toPlan,
                // Drop the finished checkout first so a restored tab never reopens a stale payment result.
                onViewTrip = { id ->
                    navController.popBackStack(Routes.CHECKOUT, inclusive = true)
                    if (navController.currentBackStackEntry?.arguments?.getString(Routes.ARG_TRIP_ID) != id) {
                        navController.navigateToTab(Routes.MY_TRIPS)
                        navController.navigate(Routes.tripDetail(id))
                    }
                },
                onDone = {
                    navController.popBackStack(Routes.CHECKOUT, inclusive = true)
                    navController.navigateToTab(Routes.HOME)
                },
                onOpenDevPaymentStates = { navController.navigate(Routes.DEV_PAYMENT_STATES) },
            )
        }
        composable(Routes.MY_TRIPS) {
            MyTripsScreen(
                onTripClick = { id -> navController.navigate(Routes.tripDetail(id)) },
                onPlanTrip = toPlan,
            )
        }
        composable(
            Routes.TRIP_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_TRIP_ID) { type = NavType.StringType }),
        ) {
            TripDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenItinerary = { navController.navigateToTab(Routes.itinerary(), restoreState = false) },
                onCheckout = { navController.navigate(Routes.CHECKOUT) },
            )
        }
        if (BuildConfig.DEBUG) {
            composable(Routes.DEV_PAYMENT_STATES) {
                PaymentStatesPreviewScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
