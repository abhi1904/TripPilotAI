package com.trippilot.trippilotai.data.mock

import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.SyncState
import com.trippilot.trippilotai.domain.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Saved trips held in memory for the current app session only. */
class InMemoryTripRepository(
    private val generator: MockItineraryGenerator,
    seedSamples: Boolean = true,
) : TripRepository {

    private val _trips = MutableStateFlow(if (seedSamples) sampleTrips(generator) else emptyList())
    override val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    override val syncState: StateFlow<SyncState> = MutableStateFlow(SyncState.Idle)

    override fun refresh() = Unit

    override fun saveTrip(plan: TripPlan) {
        _trips.update { trips -> trips.withSaved(plan, System.currentTimeMillis()) }
    }

    override fun recordPayment(plan: TripPlan, status: PaymentStatus, method: PaymentMethod, orderId: String?) {
        _trips.update { trips -> trips.withPayment(plan, status, method, orderId, System.currentTimeMillis()) }
    }

    override fun clearAll() {
        _trips.value = emptyList()
    }

    override fun restoreSamples() {
        _trips.value = sampleTrips(generator)
    }
}

/** Adds [plan] as an unpaid trip unless it's already saved. Shared by the in-memory and remote repositories. */
internal fun List<Trip>.withSaved(plan: TripPlan, nowMillis: Long): List<Trip> =
    if (any { it.id == plan.id }) this
    else listOf(Trip(plan, nowMillis, PaymentStatus.NOT_PAID)) + this

/** Records a payment on [plan]'s trip (saving it first if needed) and moves it to the front. */
internal fun List<Trip>.withPayment(
    plan: TripPlan,
    status: PaymentStatus,
    method: PaymentMethod,
    orderId: String?,
    nowMillis: Long,
): List<Trip> {
    val existing = firstOrNull { it.id == plan.id }
    val updated = (existing ?: Trip(plan, nowMillis, status))
        .copy(paymentStatus = status, paymentMethod = method, orderId = orderId ?: existing?.orderId)
    return listOf(updated) + filterNot { it.id == plan.id }
}
