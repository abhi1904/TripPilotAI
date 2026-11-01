package com.trippilot.trippilotai.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal val utc: TimeZone = TimeZone.getTimeZone("UTC")

fun formatInr(amount: Int): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 0
    }.format(amount)

/** Compact form for slider labels, e.g. ₹45K or ₹1.2L. */
fun formatInrCompact(amount: Int): String = when {
    amount >= 100_000 -> "₹" + String.format(Locale.US, "%.1fL", amount / 100_000f).replace(".0L", "L")
    amount >= 1_000 -> "₹${amount / 1_000}K"
    else -> "₹$amount"
}

fun formatDate(utcMillis: Long, pattern: String = "d MMM yyyy"): String =
    SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = utc }.format(Date(utcMillis))

fun formatDateRange(startMillis: Long, endMillis: Long): String =
    "${formatDate(startMillis, "d MMM")} – ${formatDate(endMillis, "d MMM yyyy")}"
