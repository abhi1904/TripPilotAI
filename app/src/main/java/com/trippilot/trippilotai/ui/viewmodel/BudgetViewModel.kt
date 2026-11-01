package com.trippilot.trippilotai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.trippilot.trippilotai.data.AppContainer
import com.trippilot.trippilotai.data.TripDraftStore
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BudgetUiState(val plan: TripPlan? = null, val isSaved: Boolean = false) {
    val isOverBudget: Boolean get() = plan != null && plan.budget.total > plan.request.budgetMax
    val isUnderBudget: Boolean get() = plan != null && plan.budget.total < plan.request.budgetMin
}

class BudgetViewModel(
    private val draftStore: TripDraftStore,
    private val tripRepository: TripRepository,
) : ViewModel() {

    val uiState: StateFlow<BudgetUiState> =
        combine(draftStore.current, tripRepository.trips) { plan, trips ->
            BudgetUiState(plan, isSaved = plan != null && trips.any { it.id == plan.id })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState(draftStore.current.value))

    fun saveTrip() {
        draftStore.current.value?.let(tripRepository::saveTrip)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { BudgetViewModel(AppContainer.draftStore, AppContainer.tripRepository) }
        }
    }
}
