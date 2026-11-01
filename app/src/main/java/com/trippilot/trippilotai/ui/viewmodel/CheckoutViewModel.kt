package com.trippilot.trippilotai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.trippilot.trippilotai.data.AppContainer
import com.trippilot.trippilotai.data.TripDraftStore
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.PaymentProcessor
import com.trippilot.trippilotai.domain.repository.PaymentResult
import com.trippilot.trippilotai.domain.repository.SimulatedOutcome
import com.trippilot.trippilotai.domain.repository.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The four simulated payment states plus the idle checkout form. */
sealed interface PaymentUiState {
    data object Idle : PaymentUiState
    data class Processing(val method: PaymentMethod) : PaymentUiState
    data class Success(val orderId: String, val method: PaymentMethod) : PaymentUiState
    data class Failed(val method: PaymentMethod, val message: String) : PaymentUiState
    data class Cancelled(val method: PaymentMethod) : PaymentUiState
}

data class CheckoutUiState(
    val plan: TripPlan? = null,
    val selectedMethod: PaymentMethod = PaymentMethod.UPI,
    val simulatedOutcome: SimulatedOutcome = SimulatedOutcome.SUCCESS,
    val payment: PaymentUiState = PaymentUiState.Idle,
)

class CheckoutViewModel(
    draftStore: TripDraftStore,
    private val paymentProcessor: PaymentProcessor,
    private val tripRepository: TripRepository,
) : ViewModel() {

    // The plan is captured when checkout opens so the order can't change mid-payment.
    private val _uiState = MutableStateFlow(CheckoutUiState(plan = draftStore.current.value))
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var paymentJob: Job? = null

    fun selectMethod(method: PaymentMethod) = _uiState.update { it.copy(selectedMethod = method) }

    fun setSimulatedOutcome(outcome: SimulatedOutcome) = _uiState.update { it.copy(simulatedOutcome = outcome) }

    fun pay() {
        val state = _uiState.value
        val plan = state.plan ?: return
        if (state.payment is PaymentUiState.Processing) return
        val method = state.selectedMethod
        _uiState.update { it.copy(payment = PaymentUiState.Processing(method)) }
        paymentJob = viewModelScope.launch {
            val next = when (val result = paymentProcessor.pay(plan.budget.total, method, state.simulatedOutcome)) {
                is PaymentResult.Success -> {
                    tripRepository.recordPayment(plan, PaymentStatus.PAID, method, result.orderId)
                    PaymentUiState.Success(result.orderId, method)
                }
                is PaymentResult.Failed -> {
                    tripRepository.recordPayment(plan, PaymentStatus.FAILED, method, null)
                    PaymentUiState.Failed(method, result.message)
                }
            }
            _uiState.update { it.copy(payment = next) }
        }
    }

    fun cancelPayment() {
        val processing = _uiState.value.payment as? PaymentUiState.Processing ?: return
        paymentJob?.cancel()
        _uiState.value.plan?.let { tripRepository.recordPayment(it, PaymentStatus.CANCELLED, processing.method, null) }
        _uiState.update { it.copy(payment = PaymentUiState.Cancelled(processing.method)) }
    }

    /** Back to the method selection form (used by "Change payment method"). */
    fun resetToForm() {
        paymentJob?.cancel()
        _uiState.update { it.copy(payment = PaymentUiState.Idle) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CheckoutViewModel(AppContainer.draftStore, AppContainer.paymentProcessor, AppContainer.tripRepository)
            }
        }
    }
}
