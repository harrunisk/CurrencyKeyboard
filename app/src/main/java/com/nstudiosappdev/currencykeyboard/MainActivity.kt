package com.nstudiosappdev.currencykeyboard

import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText = findViewById<View>(R.id.editText) as EditText
        val keyboard = findViewById<View>(R.id.currencyKeyboard) as CurrencyKeyboard

        editText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        editText.setTextIsSelectable(true)

        val ic = editText.onCreateInputConnection(EditorInfo())
        keyboard.setInputConnection(ic)

        editText.addTextChangedListener(MoneyTextWatcher(editText))

    }

    class MoneyTextWatcher(editText: EditText?) : TextWatcher {
        private val editTextWeakReference: WeakReference<EditText> =
            WeakReference<EditText>(editText)

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable) {
            val editText: EditText = editTextWeakReference.get() ?: return
            if (editable.isEmpty()) return
            editText.removeTextChangedListener(this)

            val cleanString = editable.replace("[$*,-]".toRegex(), "")
            val parsed = BigDecimal(cleanString)
            val formatted: String = NumberFormat.getCurrencyInstance().format(parsed)

            val wordToSpan: Spannable = SpannableString(formatted)
            var isSelectionEnabled = true
            when {
                parsed.toDouble() == 0.0 -> {
                    wordToSpan.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        0,
                        wordToSpan.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                editable.toString().contains('-') -> {
                    isSelectionEnabled = false
                    wordToSpan.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        wordToSpan.length - 2,
                        wordToSpan.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                editable.toString().contains('*') -> {
                    wordToSpan.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        wordToSpan.length - 3,
                        wordToSpan.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                formatted.substring(formatted.length - 2, formatted.length) == "00" -> {
                    if (cleanString.length > 3  && cleanString.substring(cleanString.length -2, cleanString.length) == ".0"){
                        wordToSpan.setSpan(
                            ForegroundColorSpan(Color.GRAY),
                            wordToSpan.length - 2,
                            wordToSpan.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        wordToSpan.setSpan(
                            ForegroundColorSpan(Color.GRAY),
                            wordToSpan.length - 3,
                            wordToSpan.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                formatted.last() == '0' -> {
                    wordToSpan.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        wordToSpan.length - 1,
                        wordToSpan.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            editText.setText(wordToSpan)
            if (isSelectionEnabled)
                editText.setSelection(formatted.length - 3) else
                editText.setSelection(formatted.length - 2)
            editText.addTextChangedListener(this)
        }

    }


}