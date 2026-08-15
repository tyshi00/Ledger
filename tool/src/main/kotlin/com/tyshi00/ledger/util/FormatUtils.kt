package com.tyshi00.ledger.util

import android.util.Log
import java.util.Calendar

private const val TAG = "LedgerFormat"

fun formatCents(cents: Long, symbol: String = "$"): String {
    val negative = cents < 0
    val abs = if (negative) -cents else cents
    val dollars = abs / 100
    val remainder = abs % 100
    val formatted = "%s%,d.%02d".format(symbol, dollars, remainder)
    return if (negative) "-$formatted" else formatted
}

fun parseCentsFromInput(input: String): Long? {
    // Strip currency symbols, commas, spaces
    val cleaned = input
        .replace(",", "")
        .replace(" ", "")
        .replace("$", "").replace("\u20AC", "").replace("\u00A3", "")
        .replace("\u00A5", "").replace("\u20B9", "").replace("\u20A9", "")
        .replace("\u20B1", "").replace("\u20A6", "")
        .trim()

    Log.d(TAG, "parseCentsFromInput: raw='$input' cleaned='$cleaned'")

    if (cleaned.isEmpty()) return null

    // Handle decimal input
    val parts = cleaned.split(".")
    val result = when (parts.size) {
        1 -> {
            // Whole number — treat as dollars
            val dollars = parts[0].toLongOrNull()
            if (dollars != null) dollars * 100 else null
        }
        2 -> {
            // Has decimal — dollars.cents
            val dollars = if (parts[0].isEmpty()) 0L else parts[0].toLongOrNull() ?: return null
            val centStr = parts[1].take(2).padEnd(2, '0')
            val cents = centStr.toLongOrNull() ?: return null
            dollars * 100 + cents
        }
        else -> null
    }

    Log.d(TAG, "parseCentsFromInput: result=$result cents")
    return result
}

val MONTH_NAMES = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

fun monthLabel(year: Int, month: Int): String = "${MONTH_NAMES[month - 1]} $year"

fun currentYearMonth(): Pair<Int, Int> {
    val cal = Calendar.getInstance()
    return Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
}

fun previousMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 1) Pair(year - 1, 12) else Pair(year, month - 1)

fun nextMonth(year: Int, month: Int): Pair<Int, Int> =
    if (month == 12) Pair(year + 1, 1) else Pair(year, month + 1)
