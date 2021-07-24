package com.nstudiosappdev.view

import android.text.Spannable
import android.text.style.ForegroundColorSpan

fun areSpannableStartAndEndPointsSame(
    expectedSpannable: Spannable?,
    resultSpannable: Spannable?
): Boolean {
    val expForegroundColorSpan = expectedSpannable?.getSpans(
        0, expectedSpannable.length,
        ForegroundColorSpan::class.java
    )?.firstOrNull()

    val resultForegroundColorSpan = resultSpannable?.getSpans(
        0, resultSpannable.length,
        ForegroundColorSpan::class.java
    )?.firstOrNull()

    val expectedSpanStart = expectedSpannable?.getSpanStart(expForegroundColorSpan)
    val expectedSpanEnd = expectedSpannable?.getSpanEnd(expForegroundColorSpan)

    val resultSpanStart = resultSpannable?.getSpanStart(resultForegroundColorSpan)
    val resultSpanEnd = resultSpannable?.getSpanEnd(resultForegroundColorSpan)

    return expectedSpanStart == resultSpanStart && expectedSpanEnd == resultSpanEnd
}

fun areSpannableColorsSame(
    expectedSpannable: Spannable?,
    resultSpannable: Spannable?
): Boolean {
    val expForegroundColor = expectedSpannable?.getSpans(
        0, expectedSpannable.length,
        ForegroundColorSpan::class.java
    )?.firstOrNull()?.foregroundColor

    val resultForegroundColor = resultSpannable?.getSpans(
        0, resultSpannable.length,
        ForegroundColorSpan::class.java
    )?.firstOrNull()?.foregroundColor

    return expForegroundColor == resultForegroundColor
}