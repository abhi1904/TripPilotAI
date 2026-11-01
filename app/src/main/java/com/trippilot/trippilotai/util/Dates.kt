package com.trippilot.trippilotai.util

import java.util.Calendar
import java.util.TimeZone

/** Today's date at UTC midnight, matching the convention used by Material date pickers. */
fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
