package com.trippilot.trippilotai.data.mock

import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripRequest
import com.trippilot.trippilotai.util.todayUtcMillis

/** Demo trips (one paid upcoming, one unpaid upcoming, one completed) used by the dev "restore samples" action. */
fun sampleTrips(generator: MockItineraryGenerator): List<Trip> {
    val today = todayUtcMillis()
    val day = TripRequest.DAY_MILLIS
    fun plan(id: String, name: String, startOffset: Int, days: Int, travellers: Int, interests: Set<Interest>) =
        generator.build(
            TripRequest(name, today + startOffset * day, today + (startOffset + days - 1) * day, travellers, 40_000, 150_000, interests),
            id = id,
        )
    return listOf(
        Trip(
            plan("sample-goa", "Goa", 21, 4, 2, setOf(Interest.RELAXATION, Interest.FOOD)),
            today, PaymentStatus.PAID, orderId = "MOCK-482913", paymentMethod = PaymentMethod.UPI,
        ),
        Trip(
            plan("sample-manali", "Manali", 60, 5, 3, setOf(Interest.ADVENTURE, Interest.NATURE)),
            today, PaymentStatus.NOT_PAID,
        ),
        Trip(
            plan("sample-jaipur", "Jaipur", -45, 3, 2, setOf(Interest.CULTURE, Interest.SHOPPING)),
            today - 60 * day, PaymentStatus.PAID, orderId = "MOCK-130577", paymentMethod = PaymentMethod.CARD,
        ),
    )
}
