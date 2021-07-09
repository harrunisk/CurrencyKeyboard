package com.nstudiosappdev.currencykeyboard.ext

fun Int.isCursorOnDecimalValues(textSize: Int): Boolean {
    return textSize - 2 == this
}

fun Int.isCursorOnValues(textSize: Int): Boolean {
    return this > textSize - 2
}

fun Int.isCursorLeftOnStartPosition(): Boolean {
    return this - 1 == 0
}

fun Int.isCursorOnStart(): Boolean {
    return this == 0
}

fun Int.isTextFull(textSize: Int): Boolean {
    return this == textSize
}

fun Int.isCursorOnRightFirstValue(textSize: Int): Boolean {
    return this < textSize - 2
}

fun Int.isCursorOnLastDecimalValue(textSize: Int): Boolean {
    return this == textSize -1
}