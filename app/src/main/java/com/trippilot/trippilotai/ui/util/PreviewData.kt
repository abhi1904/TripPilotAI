package com.trippilot.trippilotai.ui.util

import com.trippilot.trippilotai.data.mock.MockItineraryGenerator
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.model.TripRequest

/** Sample data for @Preview composables only. */
object PreviewData {
    private const val START = 1_798_761_600_000L // 1 Jan 2027 UTC

    val plan: TripPlan = MockItineraryGenerator().build(
        TripRequest(
            destinationName = "Goa",
            startDateMillis = START,
            endDateMillis = START + 3 * TripRequest.DAY_MILLIS,
            travellers = 2,
            budgetMin = 40_000,
            budgetMax = 90_000,
            interests = setOf(Interest.RELAXATION, Interest.FOOD, Interest.CULTURE),
        ),
        id = "preview",
    )

    val trip = Trip(plan, START, PaymentStatus.PAID, orderId = "MOCK-482913")
}
