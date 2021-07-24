package com.nstudiosappdev.view.helper

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

object CurrencyKeyboardHelper {

    private var currencyInstance: NumberFormat? = null

    fun getInitialTextArray(): ArrayList<Char> {
        return arrayListOf('0', '.', '0', '0')
    }

    fun getInitialCursorPositionInt(): Int {
        return 0
    }

    fun getInitialCursorPositionStr(): String {
        return "0"
    }

    fun getInitialCursorPositionChar(): Char {
        return '0'
    }

    fun getCurrencySymbol(locale: Locale): String {
        if (currencyInstance == null)
            currencyInstance = NumberFormat.getCurrencyInstance(locale)

        return (currencyInstance as DecimalFormat).decimalFormatSymbols.currencySymbol
    }

    fun getCurrencyInstance(locale: Locale): NumberFormat {
        return if (currencyInstance == null) {
            currencyInstance = NumberFormat.getCurrencyInstance(locale)
            currencyInstance as NumberFormat
        } else currencyInstance as NumberFormat
    }

}