package com.trippilot.trippilotai.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.trippilot.trippilotai.data.AppContainer
import com.trippilot.trippilotai.data.TripDraftStore
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.SyncState
import com.trippilot.trippilotai.domain.repository.TripRepository
import com.trippilot.trippilotai.navigation.Routes
import com.trippilot.trippilotai.util.todayUtcMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class TripFilter(val label: String) { ALL("All"), UPCOMING("Upcoming"), COMPLETED("Completed") }

data class MyTripsUiState(
    val filter: TripFilter = TripFilter.ALL,
    val trips: List<Trip> = emptyList(),
    val hasAnyTrips: Boolean = false,
    val todayMillis: Long = todayUtcMillis(),
    val isSyncing: Boolean = false,
    val syncError: String? = null,
)

class MyTripsViewModel(private val tripRepository: TripRepository) : ViewModel() {

    private val filter = MutableStateFlow(TripFilter.ALL)

    init {
        // Pick up changes made elsewhere (another device, or the backend restarted) each time My Trips opens.
        tripRepository.refresh()
    }

    val uiState: StateFlow<MyTripsUiState> =
        combine(tripRepository.trips, filter, tripRepository.syncState) { trips, filter, sync ->
            val today = todayUtcMillis()
            MyTripsUiState(
                filter = filter,
                trips = when (filter) {
                    TripFilter.ALL -> trips
                    TripFilter.UPCOMING -> trips.filterNot { it.isCompleted(today) }
                    TripFilter.COMPLETED -> trips.filter { it.isCompleted(today) }
                }.sortedBy { it.plan.request.startDateMillis },
                hasAnyTrips = trips.isNotEmpty(),
                todayMillis = today,
                isSyncing = sync is SyncState.Loading,
                syncError = (sync as? SyncState.Error)?.message,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyTripsUiState())

    fun setFilter(value: TripFilter) {
        filter.value = value
    }

    fun retry() = tripRepository.refresh()

    // Development helpers for previewing the empty state.
    fun clearAll() = tripRepository.clearAll()
    fun restoreSamples() = tripRepository.restoreSamples()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { MyTripsViewModel(AppContainer.tripRepository) }
        }
    }
}

class TripDetailViewModel(
    savedStateHandle: SavedStateHandle,
    tripRepository: TripRepository,
    private val draftStore: TripDraftStore,
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle[Routes.ARG_TRIP_ID])

    val trip: StateFlow<Trip?> = tripRepository.trips
        .map { trips -> trips.firstOrNull { it.id == tripId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), tripRepository.trips.value.firstOrNull { it.id == tripId })

    /** Makes this trip the current plan so Itinerary/Budget/Checkout show it. */
    fun open(plan: TripPlan) = draftStore.setCurrent(plan)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TripDetailViewModel(createSavedStateHandle(), AppContainer.tripRepository, AppContainer.draftStore)
            }
        }
    }
}

class HomeViewModel(draftStore: TripDraftStore) : ViewModel() {
    val currentPlan: StateFlow<TripPlan?> = draftStore.current

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(AppContainer.draftStore) }
        }
    }
}
