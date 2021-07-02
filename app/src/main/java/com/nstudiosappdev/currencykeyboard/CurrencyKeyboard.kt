package com.nstudiosappdev.currencykeyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.nstudiosappdev.currencykeyboard.databinding.LayoutCurrencyKeyboardBinding


class CurrencyKeyboard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private var inputConnection: InputConnection? = null
    private var decimalEnabled: Boolean = false
    private var decimalCommitCount: Int = 0

    private val binding: LayoutCurrencyKeyboardBinding =
        LayoutCurrencyKeyboardBinding.inflate(
            LayoutInflater.from(context), this, true
        ).apply {
            currencyKeyboard = this@CurrencyKeyboard
        }


    override fun onClick(view: View?) {

        if (inputConnection == null) return

        when (view?.id) {
            R.id.buttonDelete -> {
                val selectedText = inputConnection!!.getSelectedText(0)
                if (selectedText.isNullOrEmpty()) {
                    if (decimalEnabled) {
                        when (decimalCommitCount) {
                            2 -> {
                                inputConnection!!.deleteSurroundingText(1, 0)
                                decimalCommitCount--
                                inputConnection!!.commitText("", 3)
                            }
                            1 -> {
                                inputConnection!!.deleteSurroundingText(1, 0)
                                decimalCommitCount--
                                inputConnection!!.commitText("", 2)
                            }
                            else -> {
                                decimalEnabled = false
                                inputConnection!!.commitText("", -1)
                            }
                        }
                    } else {
                        inputConnection!!.deleteSurroundingText(1, 0)
                    }
                } else {
                    inputConnection!!.commitText("", 1)
                }
            }
            R.id.buttonDot -> {
                inputConnection!!.commitText("", 2)
                decimalEnabled = true
            }
            else -> {
                val text = (view as MaterialButton).text
                if (decimalEnabled) {
                    if (decimalCommitCount < 2) {
                        inputConnection!!.commitText(text, 1)
                        decimalCommitCount++
                        if (decimalCommitCount > 1) {
                            inputConnection!!.commitText("", 4)
                        } else {
                            inputConnection!!.commitText("", 3)
                        }
                    } else {
                        inputConnection!!.commitText("", 4)
                    }
                } else inputConnection!!.commitText(text, 1)

                val currentText = inputConnection!!.getExtractedText(ExtractedTextRequest(), 0).text

            }
        }
    }

    fun setInputConnection(inputConnection: InputConnection) {
        this.inputConnection = inputConnection
    }
}