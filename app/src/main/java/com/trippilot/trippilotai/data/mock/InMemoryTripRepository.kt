package com.trippilot.trippilotai.data.mock

import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.model.TripRequest
import com.trippilot.trippilotai.domain.repository.TripRepository
import com.trippilot.trippilotai.util.todayUtcMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Saved trips held in memory for the current app session only. */
class InMemoryTripRepository(
    private val generator: MockItineraryGenerator,
    seedSamples: Boolean = true,
) : TripRepository {

    private val _trips = MutableStateFlow(if (seedSamples) sampleTrips() else emptyList())
    override val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    override fun saveTrip(plan: TripPlan) {
        _trips.update { trips ->
            if (trips.any { it.id == plan.id }) trips
            else listOf(Trip(plan, System.currentTimeMillis(), PaymentStatus.NOT_PAID)) + trips
        }
    }

    override fun recordPayment(plan: TripPlan, status: PaymentStatus, method: PaymentMethod, orderId: String?) {
        _trips.update { trips ->
            val existing = trips.firstOrNull { it.id == plan.id }
            val updated = (existing ?: Trip(plan, System.currentTimeMillis(), status))
                .copy(paymentStatus = status, paymentMethod = method, orderId = orderId ?: existing?.orderId)
            listOf(updated) + trips.filterNot { it.id == plan.id }
        }
    }

    override fun clearAll() {
        _trips.value = emptyList()
    }

    override fun restoreSamples() {
        _trips.value = sampleTrips()
    }

    private fun sampleTrips(): List<Trip> {
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
}
