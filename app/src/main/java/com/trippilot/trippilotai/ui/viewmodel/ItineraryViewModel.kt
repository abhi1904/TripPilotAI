package com.trippilot.trippilotai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.trippilot.trippilotai.data.AppContainer
import com.trippilot.trippilotai.data.TripDraftStore
import com.trippilot.trippilotai.domain.model.ChatMessage
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.TravelAssistant
import com.trippilot.trippilotai.domain.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItineraryUiState(
    val plan: TripPlan? = null,
    val selectedDay: Int = 1,
    val isSaved: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isAssistantTyping: Boolean = false,
)

private data class ChatState(
    val planId: String? = null,
    val selectedDay: Int = 1,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isTyping: Boolean = false,
)

class ItineraryViewModel(
    private val draftStore: TripDraftStore,
    private val tripRepository: TripRepository,
    private val assistant: TravelAssistant,
) : ViewModel() {

    private val chat = MutableStateFlow(ChatState())
    private var nextMessageId = 0L

    init {
        // Reset day selection and conversation whenever a different plan is opened.
        viewModelScope.launch {
            draftStore.current.collect { plan ->
                if (plan?.id != chat.value.planId) {
                    chat.value = ChatState(planId = plan?.id, messages = plan?.let { listOf(greeting(it)) }.orEmpty())
                }
            }
        }
    }

    val uiState: StateFlow<ItineraryUiState> =
        combine(draftStore.current, tripRepository.trips, chat) { plan, trips, chatState ->
            ItineraryUiState(
                plan = plan,
                selectedDay = chatState.selectedDay,
                isSaved = plan != null && trips.any { it.id == plan.id },
                messages = chatState.messages,
                input = chatState.input,
                isAssistantTyping = chatState.isTyping,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItineraryUiState(plan = draftStore.current.value))

    fun selectDay(day: Int) = chat.update { it.copy(selectedDay = day) }

    fun onInputChange(value: String) = chat.update { it.copy(input = value) }

    fun saveTrip() {
        draftStore.current.value?.let(tripRepository::saveTrip)
    }

    fun send(text: String = chat.value.input) {
        val plan = draftStore.current.value ?: return
        val message = text.trim()
        if (message.isEmpty() || chat.value.isTyping) return
        chat.update { it.copy(messages = it.messages + ChatMessage(nextMessageId++, true, message), input = "", isTyping = true) }
        viewModelScope.launch {
            val reply = assistant.reply(message, plan, chat.value.selectedDay)
            chat.update {
                if (it.planId != plan.id) it // a different plan was opened meanwhile
                else it.copy(messages = it.messages + ChatMessage(nextMessageId++, false, reply), isTyping = false)
            }
        }
    }

    private fun greeting(plan: TripPlan) = ChatMessage(
        nextMessageId++,
        fromUser = false,
        text = "Hi! I'm your trip assistant for ${plan.destination.name}. Ask me to adjust your plan — " +
            "for example a slower day, cheaper options or food spots. (Mock replies in this preview.)",
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ItineraryViewModel(AppContainer.draftStore, AppContainer.tripRepository, AppContainer.travelAssistant)
            }
        }
    }
}
