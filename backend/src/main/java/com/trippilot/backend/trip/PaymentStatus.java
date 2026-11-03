package com.trippilot.backend.trip;

/** Mirrors the Android PaymentStatus enum. Payments are simulated; PAID never means real money moved. */
public enum PaymentStatus {
    NOT_PAID,
    PAID,
    FAILED,
    CANCELLED
}
