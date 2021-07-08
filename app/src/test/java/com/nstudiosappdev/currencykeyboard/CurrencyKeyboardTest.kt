package com.nstudiosappdev.currencykeyboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nstudiosappdev.currencykeyboard.databinding.LayoutCurrencyKeyboardBinding
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
@LooperMode(LooperMode.Mode.PAUSED)
class CurrencyKeyboardTest {

    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    private lateinit var currencyKeyboardBinding: LayoutCurrencyKeyboardBinding

    @Before
    fun setUp() {
        val activity: MainActivity? = Robolectric.setupActivity(MainActivity::class.java)
        currencyKeyboardBinding =
            activity?.findViewById<CurrencyKeyboard>(R.id.currencyKeyboard)?.binding!!
    }

    @Test
    fun showOnlyHint() {
        val expectedTest = ""
        Assert.assertEquals(currencyKeyboardBinding.editText.text.toString(), expectedTest)
        Assert.assertEquals(currencyKeyboardBinding.editText.hint.toString(), "AED 0.00")
    }

    @Test
    fun `Should set text to "AED 1dot00" when click in 1 in on initial state`() {
        val expectedText = "AED 1.00"
        currencyKeyboardBinding.button1.performClick()
        Assert.assertEquals(currencyKeyboardBinding.editText.text.toString(), expectedText)
    }

    @Test
    fun `Should set text to "AED 15,420dot50" when click in order 1 5 4 2 0 dot 50`() {
        val expectedText = "AED 15,420.50"
        currencyKeyboardBinding.button1.performClick()
        currencyKeyboardBinding.button5.performClick()
        currencyKeyboardBinding.button4.performClick()
        currencyKeyboardBinding.button2.performClick()
        currencyKeyboardBinding.button0.performClick()
        currencyKeyboardBinding.buttonDot.performClick()
        currencyKeyboardBinding.button5.performClick()
        currencyKeyboardBinding.button0.performClick()
        Assert.assertEquals(currencyKeyboardBinding.editText.text.toString(), expectedText)
    }


}