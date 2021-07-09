package com.nstudiosappdev.currencykeyboard

import android.content.Context
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
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

    private lateinit var inputConnection: InputConnection

    private val scope = Dispatchers.Main
    private var commitTextJob: Job? = null

    var binding: LayoutCurrencyKeyboardBinding

    init {
        binding =
            LayoutCurrencyKeyboardBinding.inflate(
                LayoutInflater.from(context), this, true
            ).apply {
                currencyKeyboard = this@CurrencyKeyboard
                editText.setRawInputType(InputType.TYPE_CLASS_TEXT)

                val ic = editText.onCreateInputConnection(EditorInfo())
                ic?.let { setInputConnection(it) }

            }

        CoroutineScope(scope).launch {
            valueFlow.collect {
                val text = it.second.joinToString(separator = "")
                val numberFormatCurrencyInstance =
                    NumberFormat.getCurrencyInstance(Locale("en", "AE"))
                val currencyWithSpace = "${numberFormatCurrencyInstance.currency} "
                var cleanString = text.replace("[${currencyWithSpace},-]".toRegex(), "")
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
                                getCurrentText().joinToString(separator = "")
                            )
                        ) newCursorPosition++
                        newCursorPosition++
                    } else if (cursorPosition == currentText.size - 1) {
                        currentText[cursorPosition] = text
                        newCursorPosition++
                    } else if (cursorPosition == currentText.size) {
                        // no op
                    } else {
                        currentText.add(cursorPosition, text)
                        newCursorPosition++
                    }
                    emitText(newCursorPosition, currentText)
                }
            }
        }
    }

    private fun getCursorPosition(): Int {
        return _valueFlow.value.first
    }

    private fun getCurrentText(): ArrayList<Char> {
        return _valueFlow.value.second
    }

    private fun isEmptyState(position: Int, text: String): Boolean {
        return position == VALUE_INITIAL_POSITION && text == VALUE_INITIAL.joinToString(separator = "")
    }

    private fun emitText(cursorPosition: Int, text: ArrayList<Char>) {
        commitTextJob?.cancel()
        commitTextJob = CoroutineScope(scope).launch {
            _valueFlow.emit(Pair(cursorPosition, text))
        }
    }

    private fun setInputConnection(inputConnection: InputConnection) {
        this.inputConnection = inputConnection
    }

    companion object {
        private const val INITIAL_VALUE = 0
        private const val VALUE_INITIAL_POSITION = 0
        private val VALUE_INITIAL = arrayListOf('0', '.', '0', '0')

    }
}