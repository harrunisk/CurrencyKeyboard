package com.nstudiosappdev.currencykeyboard.ext

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan

fun Spannable.setSpan(spanLength: Int) {
    setSpan(
        ForegroundColorSpan(Color.GRAY),
        spanLength,
        length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun Spannable.setSpanDecimalEnabled() {
    setSpan(
        ForegroundColorSpan(Color.GRAY),
        length - 2,
        length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun Spannable.setSpanDecimalOneItemEntered() {
    setSpan(
        ForegroundColorSpan(Color.GRAY),
        length - 1,
        length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun Spannable.setSpanDecimalDisabled() {
    setSpan(
        ForegroundColorSpan(Color.GRAY),
        length - 3,
        length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}