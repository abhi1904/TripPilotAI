package com.trippilot.trippilotai.data.remote

import com.trippilot.trippilotai.data.mock.withPayment
import com.trippilot.trippilotai.data.mock.withSaved
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.SyncState
import com.trippilot.trippilotai.domain.repository.TripRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

/**
 * Saved trips stored by the backend. Changes show up in [trips] immediately and are sent to the
 * server in the background, one request at a time so they reach it in order.
 * Requests run in [scope] (app-wide) so they finish even if the screen that started them closes.
 */
class RemoteTripRepository(
    private val api: TripPilotApi,
    private val scope: CoroutineScope,
    private val samples: () -> List<Trip>,
    private val clock: () -> Long = System::currentTimeMillis,
) : TripRepository {

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    override val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Loading)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val requests = Mutex()

    init {
        refresh()
    }

    override fun refresh() = sync("Couldn't load your trips") {
        _trips.value = api.getTrips().map(TripDto::toDomain)
    }

    override fun saveTrip(plan: TripPlan) {
        if (_trips.value.any { it.id == plan.id }) return
        _trips.update { it.withSaved(plan, clock()) }
        sync("Couldn't save your trip") { replace(api.saveTrip(SaveTripBody(plan)).toDomain()) }
    }

    override fun recordPayment(plan: TripPlan, status: PaymentStatus, method: PaymentMethod, orderId: String?) {
        _trips.update { it.withPayment(plan, status, method, orderId, clock()) }
        sync("Couldn't save the payment status") {
            replace(api.recordPayment(plan.id, PaymentBody(status, method, orderId, plan)).toDomain())
        }
    }

    override fun clearAll() {
        _trips.value = emptyList()
        sync("Couldn't delete trips") { api.deleteAll() }
    }

    override fun restoreSamples() = sync("Couldn't restore sample trips") {
        api.deleteAll()
        samples().forEach { trip ->
            api.saveTrip(SaveTripBody(trip.plan))
            trip.paymentMethod?.let { method ->
                api.recordPayment(trip.id, PaymentBody(trip.paymentStatus, method, trip.orderId))
            }
        }
        _trips.value = api.getTrips().map(TripDto::toDomain)
    }

    /** Swaps in the server's copy of a trip, keeping list order. */
    private fun replace(trip: Trip) {
        _trips.update { trips ->
            if (trips.any { it.id == trip.id }) trips.map { if (it.id == trip.id) trip else it }
            else listOf(trip) + trips
        }
    }

    private fun sync(failureMessage: String, request: suspend () -> Unit) {
        scope.launch {
            requests.withLock {
                _syncState.value = SyncState.Loading
                _syncState.value = try {
                    request()
                    SyncState.Idle
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    SyncState.Error("$failureMessage. ${e.reason()}")
                }
            }
        }
    }

    private fun Exception.reason(): String = when (this) {
        is HttpException -> "The server returned an error (${code()})."
        is IOException -> "Can't reach the server — is the backend running?"
        else -> "Unexpected response from the server."
    }
}
