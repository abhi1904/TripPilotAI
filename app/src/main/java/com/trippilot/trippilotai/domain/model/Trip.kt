package com.trippilot.trippilotai.domain.model

enum class PaymentMethod(val label: String, val description: String) {
    UPI("UPI", "Pay with any UPI app"),
    CARD("Credit / Debit card", "Visa, Mastercard, RuPay"),
    NET_BANKING("Net banking", "All major banks"),
    WALLET("Wallet", "Popular mobile wallets"),
}

/** Payment status of a saved trip. "Paid" is always a local simulation in Phase 1. */
enum class PaymentStatus(val label: String) {
    NOT_PAID("Saved · Unpaid"),
    PAID("Paid (simulated)"),
    FAILED("Payment failed"),
    CANCELLED("Payment cancelled"),
}

data class Trip(
    val plan: TripPlan,
    val savedAtMillis: Long,
    val paymentStatus: PaymentStatus,
    val orderId: String? = null,
    val paymentMethod: PaymentMethod? = null,
) {
    val id: String get() = plan.id

    fun isCompleted(todayMillis: Long): Boolean = plan.request.endDateMillis < todayMillis
}

data class ChatMessage(val id: Long, val fromUser: Boolean, val text: String)
