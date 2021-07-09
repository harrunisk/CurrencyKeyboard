package com.nstudiosappdev.currencykeyboard

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.nstudiosappdev.currencykeyboard.databinding.LayoutCurrencyKeyboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class CurrencyKeyboard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val _valueFlow: MutableStateFlow<Pair<Int, ArrayList<Char>>> =
        MutableStateFlow(Pair(VALUE_INITIAL_POSITION, VALUE_INITIAL))
    private val valueFlow: Flow<Pair<Int, ArrayList<Char>>> get() = _valueFlow

    private val scope = Dispatchers.Main
    private var commitTextJob: Job? = null
    private var setTextJob: Job? = null

    var binding: LayoutCurrencyKeyboardBinding = LayoutCurrencyKeyboardBinding.inflate(
        LayoutInflater.from(context), this, true
    ).apply {
        currencyKeyboard = this@CurrencyKeyboard
    }

    init {
        setTextJob?.cancel()
        setTextJob = CoroutineScope(scope).launch {
            valueFlow.collect {
                val text = it.second.joinToString(BLANK)
                formatAndUpdateText(text)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonDelete -> {
                if (getCursorPosition() != 0) {
                    val cursorPosition = getCursorPosition()
                    val currentText = getCurrentText()
                    var newCursorPosition = cursorPosition
                    when {
                        cursorPosition == currentText.size - 2 -> {
                            newCursorPosition--
                        }
                        cursorPosition > currentText.size - 2 -> {
                            currentText[cursorPosition - 1] = '0'
                            newCursorPosition--
                        }
                        cursorPosition - 1 == 0 -> {
                            currentText[cursorPosition - 1] = '0'
                            newCursorPosition--
                        }
                        else -> {
                            newCursorPosition--
                            currentText.removeAt(newCursorPosition)
                        }
                    }
                    emitText(newCursorPosition, currentText)
                }
            }
            R.id.buttonDot -> {
                var newCursorPosition = getCursorPosition()
                val currentText = getCurrentText()
                if (newCursorPosition == VALUE_INITIAL_POSITION) {
                    newCursorPosition += 2
                } else if (newCursorPosition < currentText.size - 2) {
                    newCursorPosition++
                }
                emitText(newCursorPosition, currentText)
            }
            else -> {
                if (getCurrentText().size < 10 || getCursorPosition() > (getCurrentText().size - 3)) {
                    val text = (view as MaterialButton).text.first()
                    val cursorPosition = getCursorPosition()
                    val currentText = getCurrentText()
                    var newCursorPosition = cursorPosition
                    if (cursorPosition == 0 || cursorPosition == currentText.size - 2) {
                        currentText[cursorPosition] = text
                        if (view.text == INITIAL_VALUE.toString() && isEmptyState(
                                cursorPosition,
                                getCurrentText().joinToString(BLANK)
                            )
                        ) newCursorPosition++
                        newCursorPosition++
                    } else if (cursorPosition == currentText.size - 1) {
                        currentText[cursorPosition] = text
                        newCursorPosition++
                    } else if (cursorPosition == currentText.size) {
                        // no op
                    } else {
                        if (currentText[cursorPosition - 1] == '0') {
                            currentText[cursorPosition - 1] = text
                            formatAndUpdateText(currentText.joinToString(BLANK))
                        } else {
                            newCursorPosition++
                            currentText.add(cursorPosition, text)
                        }
                    }
                    emitText(newCursorPosition, currentText)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setTextJob?.cancel()
        commitTextJob?.cancel()
    }

    private fun formatAndUpdateText(text: String) {
        val numberFormatCurrencyInstance =
            NumberFormat.getCurrencyInstance(Locale("en", "AE"))
        val currencyWithSpace = "${numberFormatCurrencyInstance.currency} "
        var cleanString = text.replace("[${currencyWithSpace},-]".toRegex(), BLANK)
        if (cleanString.isEmpty()) cleanString = "0"
        val parsed = BigDecimal(cleanString)
        val formatted: String = numberFormatCurrencyInstance.format(parsed).replace(
            "${numberFormatCurrencyInstance.currency}", currencyWithSpace
        )
        val wordToSpan: Spannable = SpannableString(formatted)
        val specialCharacterCount = formatted.count { it == ',' }
        var spanPoint =
            currencyWithSpace.length + getCursorPosition() + specialCharacterCount
        if (isEmptyState(getCursorPosition(), cleanString)) spanPoint = 0

        wordToSpan.setSpan(spanPoint)
        binding.editText.setText(wordToSpan)
    }

    private fun getCursorPosition(): Int {
        return _valueFlow.value.first
    }

    private fun getCurrentText(): ArrayList<Char> {
        return _valueFlow.value.second
    }

    private fun isEmptyState(position: Int, text: String): Boolean {
        return position == VALUE_INITIAL_POSITION && text == VALUE_INITIAL.joinToString(BLANK)
    }

    private fun emitText(cursorPosition: Int, text: ArrayList<Char>) {
        commitTextJob?.cancel()
        commitTextJob = CoroutineScope(scope).launch {
            _valueFlow.emit(Pair(cursorPosition, text))
        }
    }

    companion object {
        private const val INITIAL_VALUE = 0
        private const val VALUE_INITIAL_POSITION = 0
        private val VALUE_INITIAL = arrayListOf('0', '.', '0', '0')

        private const val BLANK = ""
    }
}