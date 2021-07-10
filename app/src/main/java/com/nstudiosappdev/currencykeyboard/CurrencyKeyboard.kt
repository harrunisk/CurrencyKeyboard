package com.nstudiosappdev.currencykeyboard

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.nstudiosappdev.currencykeyboard.databinding.LayoutCurrencyKeyboardBinding
import com.nstudiosappdev.currencykeyboard.ext.*
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
        MutableStateFlow(Pair(INITIAL_POSITION, initialValueArrayList))
    private val valueFlow: Flow<Pair<Int, ArrayList<Char>>> get() = _valueFlow

    private val scope = Dispatchers.Main
    private var commitTextJob: Job? = null
    private var setTextJob: Job? = null

    private var maxCharacterCount = 15

    var binding: LayoutCurrencyKeyboardBinding = LayoutCurrencyKeyboardBinding.inflate(
        LayoutInflater.from(context), this, true
    ).apply {
        currencyKeyboard = this@CurrencyKeyboard
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CurrencyKeyboard, 0, 0)
        typedArray.getInt(R.styleable.CurrencyKeyboard_maxCharacterOnIntegerSection, 0).apply {
            setMaxCharacterCount(this)
        }
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
                if (getCursorPosition() == 0) return
                var cursorPosition = getCursorPosition()
                val currentText = getCurrentText()
                when {
                    cursorPosition.isCursorOnDecimalValues(currentText.size) -> {
                        cursorPosition--
                    }
                    cursorPosition.isCursorOnValues(currentText.size) -> {
                        currentText[cursorPosition - 1] = INITIAL_POSITION_CHAR
                        cursorPosition--
                    }
                    cursorPosition.isCursorLeftOnStartPosition() -> {
                        currentText[cursorPosition - 1] = INITIAL_POSITION_CHAR
                        cursorPosition--
                    }
                    else -> {
                        cursorPosition--
                        currentText.removeAt(cursorPosition)
                    }
                }
                emitText(cursorPosition, currentText)
            }
            R.id.buttonDot -> {
                var cursorPosition = getCursorPosition()
                val currentText = getCurrentText()
                if (cursorPosition.isCursorOnStart()) {
                    cursorPosition += 2
                } else if (cursorPosition.isCursorOnRightFirstValue(currentText.size)) {
                    cursorPosition++
                }
                emitText(cursorPosition, currentText)
            }
            else -> {
                if (getCurrentText().size < getMaxCharacterCount() || getCursorPosition() > (getCurrentText().size - 3)) {
                    val text = (view as MaterialButton).text.first()
                    val cursorPosition = getCursorPosition()
                    val currentText = getCurrentText()
                    var newCursorPosition = cursorPosition

                    if (cursorPosition.isCursorOnStart() || cursorPosition.isCursorOnDecimalValues(
                            currentText.size
                        )
                    ) {
                        currentText[cursorPosition] = text
                        if (view.text == INITIAL_POSITION.toString() && isEmptyState(
                                cursorPosition,
                                getCurrentText().joinToString(BLANK)
                            )
                        ) newCursorPosition++
                        newCursorPosition++
                    } else if (cursorPosition.isCursorOnLastDecimalValue(currentText.size)) {
                        currentText[cursorPosition] = text
                        newCursorPosition++
                    } else if (cursorPosition.isTextFull(currentText.size)) {
                        // no op
                    } else {
                        if (currentText[cursorPosition - 1] == INITIAL_POSITION_CHAR) {
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

    private fun setMaxCharacterCount(maxCharacterCount: Int) {
        this.maxCharacterCount = maxCharacterCount
    }

    private fun getMaxCharacterCount(): Int = this.maxCharacterCount

    private fun isEmptyState(position: Int, text: String): Boolean {
        return position == INITIAL_POSITION && text == initialValueArrayList.joinToString(BLANK)
    }

    private fun emitText(cursorPosition: Int, text: ArrayList<Char>) {
        commitTextJob?.cancel()
        commitTextJob = CoroutineScope(scope).launch {
            _valueFlow.emit(Pair(cursorPosition, text))
        }
    }

    companion object {
        private const val INITIAL_POSITION = 0
        private const val INITIAL_POSITION_CHAR = '0'
        private val initialValueArrayList = arrayListOf('0', '.', '0', '0')

        private const val BLANK = ""
    }
}