package com.nstudiosappdev.currencykeyboard

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

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
        private val editTextWeakReference: WeakReference<EditText> = WeakReference<EditText>(editText)
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable) {
            val editText: EditText = editTextWeakReference.get() ?: return
            val s = editable.toString()
            if (s.isEmpty()) return
            editText.removeTextChangedListener(this)
            val cleanString = s.replace("[$,]".toRegex(), "")
            val parsed: BigDecimal = BigDecimal(cleanString)
                .multiply(BigDecimal(1))
            val formatted: String = NumberFormat.getCurrencyInstance().format(parsed)
            editText.setText(formatted)
            editText.setSelection(formatted.length -3)
            editText.addTextChangedListener(this)
        }

    }




}