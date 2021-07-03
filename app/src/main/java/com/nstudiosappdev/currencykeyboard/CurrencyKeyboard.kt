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
import java.text.NumberFormat

class CurrencyKeyboard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private var inputConnection: InputConnection? = null
    private var decimalEnabled: Boolean = false
    private var decimalItemCount: Int = 0

    private val binding: LayoutCurrencyKeyboardBinding =
        LayoutCurrencyKeyboardBinding.inflate(
            LayoutInflater.from(context), this, true
        ).apply {
            currencyKeyboard = this@CurrencyKeyboard
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT)

            val ic = editText.onCreateInputConnection(EditorInfo())
            ic?.let { setInputConnection(it) }
            editText.addTextChangedListener(MoneyTextWatcher(editText))
            editText.hint = NumberFormat.getCurrencyInstance().format(INITIAL_VALUE)
        }

    override fun onClick(view: View?) {
        if (inputConnection == null) return

        when (view?.id) {
            R.id.buttonDelete -> {
                if (decimalEnabled) {
                    when (decimalItemCount) {
                        DecimalItemCount.TWO_ITEM.itemCount -> {
                            inputConnection!!.deleteSurroundingText(1, 0)
                            inputConnection!!.commitText(SIGN_REMOVE_DECIMAL_ON_SECOND_PLACE, 3)
                            decimalItemCount--
                        }
                        DecimalItemCount.ONE_ITEM.itemCount -> {
                            inputConnection!!.deleteSurroundingText(1, 0)
                            decimalItemCount--
                            inputConnection!!.commitText(SIGN_REMOVE_DECIMAL_ON_FIRST_PLACE, 2)
                        }
                        else -> {
                            decimalEnabled = false
                            inputConnection!!.commitText(SIGN_DISABLE_DECIMAL, -1)
                        }
                    }
                } else {
                    inputConnection!!.deleteSurroundingText(1, 0)
                }
            }
            R.id.buttonDot -> {
                inputConnection!!.commitText(SIGN_ENABLE_DECIMAL, 2)
                decimalEnabled = true
            }
            else -> {
                val text = (view as MaterialButton).text
                if (decimalEnabled) {
                    if (decimalItemCount < DecimalItemCount.TWO_ITEM.itemCount) {
                        decimalItemCount++
                        if (decimalItemCount == DecimalItemCount.TWO_ITEM.itemCount) {
                            inputConnection!!.commitText( "${text}${SIGN_DECIMAL_SECOND_PLACE_FILLED}", 1)
                        } else if (decimalItemCount == DecimalItemCount.ONE_ITEM.itemCount) {
                            inputConnection!!.commitText( "${text}${SIGN_DECIMAL_FIRST_PLACE_FILLED}", 1)
                        }
                    } else {
                        moveCursorToNewPosition(4)
                    }
                } else {
                    inputConnection!!.commitText(text, 0)
                }
            }
        }
    }

    private fun setInputConnection(inputConnection: InputConnection) {
        this.inputConnection = inputConnection
    }

    private fun moveCursorToNewPosition(position: Int) {
        inputConnection!!.commitText(BLANK, position)
    }

    enum class DecimalItemCount(val itemCount: Int) {
        TWO_ITEM(2),
        ONE_ITEM(1),
        ZERO_ITEM_ENABLED(0)
    }

    companion object {
        const val SIGN_ENABLE_DECIMAL = "-"
        const val SIGN_DISABLE_DECIMAL = "*"

        const val SIGN_DECIMAL_SECOND_PLACE_FILLED = "("
        const val SIGN_DECIMAL_FIRST_PLACE_FILLED = ")"

        const val SIGN_REMOVE_DECIMAL_ON_SECOND_PLACE = "?"
        const val SIGN_REMOVE_DECIMAL_ON_FIRST_PLACE = "+"

        private const val BLANK = ""
        private const val INITIAL_VALUE = 0
    }
}