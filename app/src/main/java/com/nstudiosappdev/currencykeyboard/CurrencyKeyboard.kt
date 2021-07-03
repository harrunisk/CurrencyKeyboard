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
            editText.setTextIsSelectable(false)

            val ic = editText.onCreateInputConnection(EditorInfo())
            ic?.let { setInputConnection(it) }
            editText.addTextChangedListener(MoneyTextWatcher(editText))
        }

    override fun onClick(view: View?) {
        if (inputConnection == null) return

        when (view?.id) {
            R.id.buttonDelete -> {
                if (decimalEnabled) {
                    when (decimalItemCount) {
                        DecimalItemCount.TWO_ITEM.itemCount -> {
                            inputConnection!!.deleteSurroundingText(1, 0)
                            decimalItemCount--
                            moveCursorToNewPosition(3)
                        }
                        DecimalItemCount.ONE_ITEM.itemCount -> {
                            inputConnection!!.deleteSurroundingText(1, 0)
                            decimalItemCount--
                            moveCursorToNewPosition(2)
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
                        inputConnection!!.commitText(text, 1)
                        decimalItemCount++
                        if (decimalItemCount > 1) {
                            moveCursorToNewPosition(4)
                        } else {
                            moveCursorToNewPosition(3)
                        }
                    } else {
                        moveCursorToNewPosition(4)
                    }
                } else {
                    inputConnection!!.commitText(text, 2)
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
        private const val SIGN_ENABLE_DECIMAL = "-"
        private const val SIGN_DISABLE_DECIMAL = "*"

        private const val BLANK = ""
    }
}