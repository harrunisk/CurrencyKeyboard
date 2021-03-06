package com.nstudiosappdev.view

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.view.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.mediastudios.currencykeyboard.R
import com.nstudiosappdev.view.ext.*
import com.nstudiosappdev.view.helper.CurrencyKeyboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class CurrencyKeyboard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    /**
     * Pair of cursor position and the all text
     */
    private val _valueFlow: MutableStateFlow<Pair<Int, ArrayList<Char>>> =
        MutableStateFlow(
            Pair(
                CurrencyKeyboardHelper.getInitialCursorPositionInt(),
                CurrencyKeyboardHelper.getInitialTextArray()
            )
        )
    private val valueFlow: Flow<Pair<Int, ArrayList<Char>>> get() = _valueFlow

    /**
     * Coroutine Scope and required jobs
     */
    private val coroutineContext = Dispatchers.Main
    private var commitTextJob: Job? = null
    private var setTextJob: Job? = null

    /**
     * Character count on the integer section
     */
    private var maxCharacterOnIntegerSection: Int? = null

    /**
     * Locale info
     */
    private var locale: Locale? = null

    /**
     * Custom View
     */
    private var view: View = LayoutInflater.from(context).inflate(
        R.layout.layout_currency_keyboard,
        this,
        true
    )

    init {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CurrencyKeyboard)

        typedArray.getInt(
            R.styleable.CurrencyKeyboard_maxCharacterOnIntegerSection,
            DEFAULT_MAX_CHARACTER_ON_INTEGER_SECTION
        ).apply {
            setMaxCharacterOnIntegerSection(this)
        }

        typedArray.getDimension(
            R.styleable.CurrencyKeyboard_currencyTextSize,
            DEFAULT_CURRENCY_TEXT_SIZE
        ).apply {
            view.findViewById<AppCompatEditText>(R.id.editText).textSize = this
        }

        val localeLang =
            typedArray.getString(R.styleable.CurrencyKeyboard_localeLanguage) ?: DEFAULT_LOCALE_LANG
        val localeCountry = typedArray.getString(R.styleable.CurrencyKeyboard_localeCountry)
            ?: DEFAULT_LOCALE_COUNTRY
        setLocale(Locale(localeLang, localeCountry))

        setClickListeners(view)

        setTextJob?.cancel()
        setTextJob = CoroutineScope(coroutineContext).launch {
            valueFlow.collect {
                val text = it.second.joinToString(BLANK)
                formatAndUpdateText(text, view.findViewById(R.id.editText))
            }
        }
    }

    /**
     * Click functions and text logics
     *
     * @param view
     */
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
                    cursorPosition.isCursorOnValues(currentText.size) || cursorPosition.isCursorLeftOnStartPosition() -> {
                        currentText[cursorPosition - 1] =
                            CurrencyKeyboardHelper.getInitialCursorPositionChar()
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
                } else if (cursorPosition.isCursorOnRightOfFirstValue(currentText.size)) {
                    cursorPosition++
                }
                emitText(cursorPosition, currentText)
            }
            else -> {
                if (getCurrentText().size < getMaxCharacterOnIntegerSection() || getCursorPosition() > (getCurrentText().size - 3)) {
                    val text = (view as MaterialButton).text.first()
                    val cursorPosition = getCursorPosition()
                    val currentText = getCurrentText()
                    var newCursorPosition = cursorPosition
                    when {
                        cursorPosition.isCursorOnStart() || cursorPosition.isCursorOnDecimalValues(
                            currentText.size
                        ) -> {
                            currentText[cursorPosition] = text
                            if (view.text == CurrencyKeyboardHelper.getInitialCursorPositionStr() && isEmptyState(
                                    cursorPosition, getCurrentText().joinToString(
                                        BLANK
                                    )
                                )
                            )
                                newCursorPosition++
                            newCursorPosition++
                        }
                        cursorPosition.isCursorOnLastDecimalValue(currentText.size) -> {
                            currentText[cursorPosition] = text
                            newCursorPosition++
                        }
                        cursorPosition.isTextFull(currentText.size) -> {
                            // no-op
                        }
                        else -> {
                            if (currentText[cursorPosition - 1] == CurrencyKeyboardHelper.getInitialCursorPositionChar()) {
                                currentText[cursorPosition - 1] = text
                                formatAndUpdateText(
                                    currentText.joinToString(BLANK),
                                    this.view.findViewById(R.id.editText)
                                )
                            } else {
                                newCursorPosition++
                                currentText.add(cursorPosition, text)
                            }
                        }
                    }
                    emitText(newCursorPosition, currentText)
                }
            }
        }
    }

    /**
     * Cancel jobs on custom view detach
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setTextJob?.cancel()
        commitTextJob?.cancel()
    }

    /**
     * Text format and update
     * @param text
     */
    private fun formatAndUpdateText(text: String, editText: AppCompatEditText) {

        val currencyWithSpace = "${CurrencyKeyboardHelper.getCurrencySymbol(getLocale())} "
        var cleanString = text.replace("[${currencyWithSpace},-]".toRegex(), BLANK)
        if (cleanString.isEmpty()) cleanString = "0"
        val parsed = BigDecimal(cleanString)

        val formatted: String =
            CurrencyKeyboardHelper.getCurrencyInstance(getLocale()).format(parsed).replace(
                CurrencyKeyboardHelper.getCurrencySymbol(getLocale()), currencyWithSpace
            )
        val wordToSpan: Spannable = SpannableString(formatted)
        val specialCharacterCount = formatted.count { it == ',' }
        var spanPoint =
            currencyWithSpace.length + getCursorPosition() + specialCharacterCount
        if (isEmptyState(getCursorPosition(), cleanString)) spanPoint = 0

        wordToSpan.setSpan(spanPoint)
        editText.setText(wordToSpan)
    }

    /**
     * Get cursor position returns int
     */
    private fun getCursorPosition(): Int {
        return _valueFlow.value.first
    }

    /**
     * Get current text returns char ArrayList
     */
    private fun getCurrentText(): ArrayList<Char> {
        return _valueFlow.value.second
    }

    /**
     * Set locale
     * @param locale
     */
    private fun setLocale(locale: Locale) {
        this.locale = locale
    }

    /**
     * Get locale
     */
    fun getLocale() = this.locale!!

    /**
     * Set locale
     * @param maxCharacterCount
     */
    private fun setMaxCharacterOnIntegerSection(maxCharacterCount: Int) {
        this.maxCharacterOnIntegerSection = maxCharacterCount
    }

    /**
     * Get maxCharacterCount
     */
    private fun getMaxCharacterOnIntegerSection(): Int = this.maxCharacterOnIntegerSection!!

    /**
     * Checks is text on empty state
     * @param position
     * @param text
     */
    private fun isEmptyState(position: Int, text: String): Boolean {
        return position == CurrencyKeyboardHelper.getInitialCursorPositionInt() && text == CurrencyKeyboardHelper.getInitialTextArray()
            .joinToString(
                BLANK
            )
    }

    /**
     * Emits text after logic
     * @param cursorPosition
     * @param text
     */
    private fun emitText(cursorPosition: Int, text: ArrayList<Char>) {
        commitTextJob?.cancel()
        commitTextJob = CoroutineScope(coroutineContext).launch {
            _valueFlow.emit(Pair(cursorPosition, text))
        }
    }

    /**
     * Set click listeners
     * @param view
     */
    private fun setClickListeners(view: View?) {
        view?.findViewById<MaterialButton>(R.id.button0)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button1)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button2)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button3)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button4)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button5)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button6)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button7)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button8)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.button9)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.buttonDot)?.setOnClickListener(this)
        view?.findViewById<MaterialButton>(R.id.buttonDelete)?.setOnClickListener(this)
    }

    /**
     * Reset the keyboard
     */
    fun resetKeyboard() {
        emitText(
            CurrencyKeyboardHelper.getInitialCursorPositionInt(),
            CurrencyKeyboardHelper.getInitialTextArray()
        )
    }

    companion object {
        private const val BLANK = ""

        private const val DEFAULT_LOCALE_LANG = "en"
        private const val DEFAULT_LOCALE_COUNTRY = "AE"
        private const val DEFAULT_MAX_CHARACTER_ON_INTEGER_SECTION = 15
        private const val DEFAULT_CURRENCY_TEXT_SIZE = 36F
    }
}