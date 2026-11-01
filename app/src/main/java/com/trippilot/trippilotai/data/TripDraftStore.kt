package com.trippilot.trippilotai.data

import com.trippilot.trippilotai.domain.model.TripPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds the plan currently being viewed across Itinerary → Budget → Checkout. In-memory only. */
class TripDraftStore {
    private val _current = MutableStateFlow<TripPlan?>(null)
    val current: StateFlow<TripPlan?> = _current.asStateFlow()

    fun setCurrent(plan: TripPlan) {
        _current.value = plan
    }
}
