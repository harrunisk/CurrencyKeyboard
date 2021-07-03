package com.nstudiosappdev.currencykeyboard

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import com.nstudiosappdev.currencykeyboard.CurrencyKeyboard.Companion.SIGN_DECIMAL_FIRST_PLACE_FILLED
import com.nstudiosappdev.currencykeyboard.CurrencyKeyboard.Companion.SIGN_DECIMAL_SECOND_PLACE_FILLED
import com.nstudiosappdev.currencykeyboard.CurrencyKeyboard.Companion.SIGN_DISABLE_DECIMAL
import com.nstudiosappdev.currencykeyboard.CurrencyKeyboard.Companion.SIGN_ENABLE_DECIMAL
import com.nstudiosappdev.currencykeyboard.CurrencyKeyboard.Companion.SIGN_REMOVE_DECIMAL_ON_FIRST_PLACE
import com.nstudiosappdev.currencykeyboard.CurrencyKeyboard.Companion.SIGN_REMOVE_DECIMAL_ON_SECOND_PLACE
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.text.NumberFormat

class MoneyTextWatcher(editText: EditText?) : TextWatcher {
    private val editTextWeakReference: WeakReference<EditText> =
        WeakReference<EditText>(editText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(editable: Editable) {
        val editText: EditText = editTextWeakReference.get() ?: return
        if (editable.isEmpty()) return
        editText.removeTextChangedListener(this)

        var cleanString = editable.replace("[()+?$*,-]".toRegex(), "")
        if (cleanString.isNullOrEmpty()) cleanString = "0"
        val parsed = BigDecimal(cleanString)
        val formatted: String = NumberFormat.getCurrencyInstance().format(parsed)

        val wordToSpan: Spannable = SpannableString(formatted)
        var cursorPosition = formatted.length - 3
        when {
            editable.contains(SIGN_REMOVE_DECIMAL_ON_SECOND_PLACE) -> {
                setSpan(wordToSpan, wordToSpan.length - 1)
                cursorPosition = formatted.length - 1
            }
            editable.contains(SIGN_REMOVE_DECIMAL_ON_FIRST_PLACE) -> {
                setSpan(wordToSpan, wordToSpan.length - 2)
                cursorPosition = formatted.length - 2
            }
            editable.contains(SIGN_DISABLE_DECIMAL) -> {
                setSpan(wordToSpan, wordToSpan.length - 3)
                cursorPosition = formatted.length - 3
            }
            editable.contains(SIGN_ENABLE_DECIMAL) -> {
                setSpan(wordToSpan, wordToSpan.length - 2)
                cursorPosition = formatted.length - 2
            }
            editable.contains(SIGN_DECIMAL_SECOND_PLACE_FILLED) -> {
                setSpan(wordToSpan, wordToSpan.length)
                cursorPosition = formatted.length
            }
            editable.contains(SIGN_DECIMAL_FIRST_PLACE_FILLED) -> {
                setSpan(wordToSpan, wordToSpan.length - 1)
                cursorPosition = formatted.length - 1
            }
            parsed.toDouble() == 0.0 -> {
                setSpan(wordToSpan, 0)
            }
            else -> {
                setSpan(wordToSpan, wordToSpan.length - 3)
            }
        }

        editText.setText(wordToSpan)
        editText.setSelection(cursorPosition)
        editText.addTextChangedListener(this)
    }

    private fun setSpan(spannable: Spannable, spanLength: Int) {
        spannable.setSpan(
            ForegroundColorSpan(Color.GRAY),
            spanLength,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

}