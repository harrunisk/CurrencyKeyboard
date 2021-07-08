package com.nstudiosappdev.currencykeyboard

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.nstudiosappdev.currencykeyboard.databinding.LayoutCurrencyKeyboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class CurrencyKeyboard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val _valueFlow: MutableStateFlow<Pair<Int, String>> =
        MutableStateFlow(Pair(VALUE_INITIAL_POSITION, VALUE_INITIAL))
    val valueFlow: Flow<Pair<Int, String>> get() = _valueFlow

    private val _decimalValueFlow: MutableStateFlow<Pair<Int, String>> =
        MutableStateFlow(Pair(DECIMAL_VALUE_INITIAL_POSITION, DECIMAL_VALUE_INITIAL))
    val decimalValueFlow: Flow<Pair<Int, String>> get() = _valueFlow

    private val _decimalEnableFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val decimalEnableFlow: Flow<Boolean> get() = _decimalEnableFlow

    private val _buttonClickFlow: MutableSharedFlow<String> = MutableSharedFlow(1)
    val buttonClickFlow: Flow<String> get() = _buttonClickFlow

    private val _removeTextFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val removeTextFlow: Flow<Boolean> get() = _removeTextFlow

    private lateinit var inputConnection: InputConnection
    private var decimalEnabled: Boolean = false
    private var decimalItemCount: Int = 0

    init {

    }

    suspend fun collectValues() {
        combine(
            valueFlow,
            decimalValueFlow,
            decimalEnableFlow,
            buttonClickFlow,
            removeTextFlow
        ) { value, decimalValue, decimalEnabled, buttonClick, removeText ->

            when (removeText) {
                true -> {
                    if (decimalEnabled) {
                        val cursorPosition = decimalValue.first
                        if (cursorPosition == 0) {
                            _decimalEnableFlow.emit(false)
                        } else {
                            val newValue = decimalValue.second.toCharArray()
                            newValue[cursorPosition] = '0'
                            _removeTextFlow.value = false
                            _decimalValueFlow.emit(Pair(cursorPosition - 1, newValue.toString()))
                        }
                    } else {
                        val cursorPosition = value.first
                        val newValue = value.second.substring(0, value.second.length - 1)
                        _removeTextFlow.value = false
                        _valueFlow.emit(Pair(cursorPosition - 1, newValue))
                    }
                }
                false -> {
                    when (decimalEnabled) {
                        true -> {

                        }
                        false -> {

                        }
                    }
                }
            }
        }
    }

    private val binding: LayoutCurrencyKeyboardBinding =
        LayoutCurrencyKeyboardBinding.inflate(
            LayoutInflater.from(context), this, true
        ).apply {
            currencyKeyboard = this@CurrencyKeyboard
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT)

            val ic = editText.onCreateInputConnection(EditorInfo())
            ic?.let { setInputConnection(it) }
            editText.addTextChangedListener(MoneyTextWatcher(editText))

            val numberFormatCurrencyInstance = NumberFormat.getCurrencyInstance(Locale("en", "AE"))
            val currencyWithSpace = "${numberFormatCurrencyInstance.currency} "

            editText.hint =
                numberFormatCurrencyInstance.format(INITIAL_VALUE).replace(
                    "${numberFormatCurrencyInstance.currency}", currencyWithSpace
                )

        }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonDelete -> {
                val text = (view as MaterialButton).text
                CoroutineScope(Dispatchers.Main).launch {
                    _removeTextFlow.emit(true)
                }
                if (decimalEnabled) {
                    when (decimalItemCount) {
                        DecimalItemCount.TWO_ITEM.itemCount -> {
                            inputConnection.deleteSurroundingText(1, 0)
                            decimalItemCount--
                            inputConnection.commitText(SIGN_REMOVE_DECIMAL_ON_SECOND_PLACE, 3)
                        }
                        DecimalItemCount.ONE_ITEM.itemCount -> {
                            inputConnection.deleteSurroundingText(1, 0)
                            decimalItemCount--
                            inputConnection.commitText(SIGN_REMOVE_DECIMAL_ON_FIRST_PLACE, 2)
                        }
                        else -> {
                            decimalEnabled = false
                            inputConnection.commitText(SIGN_DISABLE_DECIMAL, -1)
                        }
                    }
                } else {
                    inputConnection.deleteSurroundingText(1, 0)
                }
            }
            R.id.buttonDot -> {
                if (!decimalEnabled) {
                    inputConnection.commitText(SIGN_ENABLE_DECIMAL, 2)
                    decimalEnabled = true
                }
            }
            else -> {
                val text = (view as MaterialButton).text
                if (decimalEnabled) {
                    if (decimalItemCount < DecimalItemCount.TWO_ITEM.itemCount) {
                        decimalItemCount++
                        when (decimalItemCount) {
                            DecimalItemCount.TWO_ITEM.itemCount -> {
                                inputConnection.commitText(
                                    "${text}${SIGN_DECIMAL_SECOND_PLACE_FILLED}",
                                    1
                                )
                            }
                            DecimalItemCount.ONE_ITEM.itemCount -> {
                                inputConnection.commitText(
                                    "${text}${SIGN_DECIMAL_FIRST_PLACE_FILLED}",
                                    1
                                )
                            }
                        }
                    }
                } else {
                    inputConnection.commitText(text, 0)
                }
            }
        }
    }

    private fun setInputConnection(inputConnection: InputConnection) {
        this.inputConnection = inputConnection
    }

    enum class DecimalItemCount(val itemCount: Int) {
        TWO_ITEM(2),
        ONE_ITEM(1)
    }

    companion object {
        const val SIGN_ENABLE_DECIMAL = "-"
        const val SIGN_DISABLE_DECIMAL = "*"

        const val SIGN_DECIMAL_SECOND_PLACE_FILLED = "("
        const val SIGN_DECIMAL_FIRST_PLACE_FILLED = ")"

        const val SIGN_REMOVE_DECIMAL_ON_SECOND_PLACE = "?"
        const val SIGN_REMOVE_DECIMAL_ON_FIRST_PLACE = "+"

        private const val INITIAL_VALUE = 0

        private const val VALUE_INITIAL_POSITION = 0
        private const val VALUE_INITIAL = "0"

        private const val DECIMAL_VALUE_INITIAL_POSITION = 0
        private const val DECIMAL_VALUE_INITIAL = ".00"

    }
}