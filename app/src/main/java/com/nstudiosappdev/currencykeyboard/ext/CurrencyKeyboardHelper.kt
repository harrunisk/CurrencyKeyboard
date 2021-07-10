package com.nstudiosappdev.currencykeyboard.ext

object CurrencyKeyboardHelper {

    fun getInitialTextArray(): ArrayList<Char>{
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

}