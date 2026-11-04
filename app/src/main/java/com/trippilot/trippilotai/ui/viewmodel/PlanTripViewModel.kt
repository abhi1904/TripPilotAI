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
import com.trippilot.trippilotai.data.mock.MockItineraryGenerator
import com.trippilot.trippilotai.domain.model.DestinationSuggestion
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.TripRequest
import com.trippilot.trippilotai.domain.repository.DestinationSearch
import com.trippilot.trippilotai.domain.repository.ItineraryGenerator
import com.trippilot.trippilotai.navigation.Routes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlanField { DESTINATION, DATES, INTERESTS }

data class PlanTripUiState(
    val destination: String = "",
    val suggestions: List<DestinationSuggestion> = emptyList(),
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val travellers: Int = 2,
    val budgetMin: Int = 30_000,
    val budgetMax: Int = 90_000,
    val interests: Set<Interest> = setOf(Interest.NATURE, Interest.FOOD),
    val errors: Map<PlanField, String> = emptyMap(),
    val isGenerating: Boolean = false,
    /** Set once a plan is ready; the screen navigates and then calls [PlanTripViewModel.onNavigated]. */
    val generatedPlanReady: Boolean = false,
)

class PlanTripViewModel(
    savedStateHandle: SavedStateHandle,
    private val generator: ItineraryGenerator,
    private val draftStore: TripDraftStore,
    private val destinationSearch: DestinationSearch,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlanTripUiState(destination = savedStateHandle.get<String>(Routes.ARG_DESTINATION).orEmpty()),
    )
    val uiState: StateFlow<PlanTripUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /** The user typed: keeps current suggestions until new ones arrive, so the list doesn't flicker. */
    fun onDestinationTyped(value: String) {
        searchJob?.cancel()
        _uiState.update { state ->
            state.copy(
                destination = value,
                suggestions = if (value.isBlank()) emptyList() else state.suggestions,
                errors = state.errors - PlanField.DESTINATION,
            )
        }
        if (value.isBlank()) return
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            val suggestions = destinationSearch.search(value)
            _uiState.update { it.copy(suggestions = suggestions) }
        }
    }

    /** The user picked a destination outright, so there is nothing left to suggest. */
    fun onDestinationPicked(value: String) {
        searchJob?.cancel()
        _uiState.update {
            it.copy(destination = value, suggestions = emptyList(), errors = it.errors - PlanField.DESTINATION)
        }
    }

    fun onDatesChange(start: Long?, end: Long?) = _uiState.update {
        it.copy(startDateMillis = start, endDateMillis = end, errors = it.errors - PlanField.DATES)
    }

    fun onTravellersChange(value: Int) = _uiState.update { it.copy(travellers = value.coerceIn(MIN_TRAVELLERS, MAX_TRAVELLERS)) }

    fun onBudgetChange(min: Int, max: Int) = _uiState.update { it.copy(budgetMin = min, budgetMax = max) }

    fun onInterestToggle(interest: Interest) = _uiState.update {
        val interests = if (interest in it.interests) it.interests - interest else it.interests + interest
        it.copy(interests = interests, errors = it.errors - PlanField.INTERESTS)
    }

    fun generate() {
        val state = _uiState.value
        val errors = validate(state)
        _uiState.update { it.copy(errors = errors) }
        if (errors.isNotEmpty() || state.isGenerating) return

        val request = TripRequest(
            destinationName = state.destination.trim(),
            startDateMillis = state.startDateMillis!!,
            endDateMillis = state.endDateMillis!!,
            travellers = state.travellers,
            budgetMin = state.budgetMin,
            budgetMax = state.budgetMax,
            interests = state.interests,
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val plan = generator.generate(request)
            draftStore.setCurrent(plan)
            _uiState.update { it.copy(isGenerating = false, generatedPlanReady = true) }
        }
    }

    fun onNavigated() = _uiState.update { it.copy(generatedPlanReady = false) }

    companion object {
        private const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val MIN_TRAVELLERS = 1
        const val MAX_TRAVELLERS = 12
        const val BUDGET_FLOOR = 10_000
        const val BUDGET_CEILING = 500_000

        fun validate(state: PlanTripUiState): Map<PlanField, String> = buildMap {
            if (state.destination.isBlank()) put(PlanField.DESTINATION, "Choose or enter a destination")
            val start = state.startDateMillis
            val end = state.endDateMillis
            when {
                start == null || end == null -> put(PlanField.DATES, "Select your start and end dates")
                end < start -> put(PlanField.DATES, "End date must be after the start date")
                (end - start) / TripRequest.DAY_MILLIS + 1 > MockItineraryGenerator.MAX_DAYS ->
                    put(PlanField.DATES, "Trips can be up to ${MockItineraryGenerator.MAX_DAYS} days")
            }
            if (state.interests.isEmpty()) put(PlanField.INTERESTS, "Pick at least one interest")
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlanTripViewModel(createSavedStateHandle(), AppContainer.itineraryGenerator, AppContainer.draftStore, AppContainer.destinationSearch)
            }
        }
    }
}
