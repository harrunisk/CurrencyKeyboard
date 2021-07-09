package com.nstudiosappdev.currencykeyboard

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan

fun Spannable.setSpan(spanLength: Int) {
    this.setSpan(
        ForegroundColorSpan(Color.GRAY),
        spanLength,
        this.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}