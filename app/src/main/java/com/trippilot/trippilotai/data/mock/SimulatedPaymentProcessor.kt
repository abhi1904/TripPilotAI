package com.trippilot.trippilotai.data.mock

import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.repository.PaymentProcessor
import com.trippilot.trippilotai.domain.repository.PaymentResult
import com.trippilot.trippilotai.domain.repository.SimulatedOutcome
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Local payment simulation. No gateway, no network and no money is moved. */
class SimulatedPaymentProcessor(private val delayMillis: Long = 2_500) : PaymentProcessor {

    override suspend fun pay(amount: Int, method: PaymentMethod, outcome: SimulatedOutcome): PaymentResult {
        delay(delayMillis)
        return when (outcome) {
            SimulatedOutcome.SUCCESS -> PaymentResult.Success(orderId = "MOCK-${Random.nextInt(100_000, 999_999)}")
            SimulatedOutcome.FAILURE -> PaymentResult.Failed("The simulated ${method.label} payment was declined.")
        }
    }
}
