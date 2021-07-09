package com.nstudiosappdev.currencykeyboard

import android.text.SpannableString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.text.toSpannable
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
        val expectedSpannable = SpannableString(expectedText).toSpannable()
        expectedSpannable.setSpan(expectedSpannable.length - 3)

        currencyKeyboardBinding.button1.performClick()

        val resultText = currencyKeyboardBinding.editText.text.toString()
        val resultSpannable = currencyKeyboardBinding.editText.text?.toSpannable()

        assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
        assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
        Assert.assertEquals(expectedText, resultText)
    }

    @Test
    fun `Should set text to "AED 15,420dot50" when click in order 1 5 4 2 0 dot 50`() {
        val expectedText = "AED 15,420.50"
        val expectedSpannable = SpannableString(expectedText).toSpannable()

        currencyKeyboardBinding.button1.performClick()
        currencyKeyboardBinding.button5.performClick()
        currencyKeyboardBinding.button4.performClick()
        currencyKeyboardBinding.button2.performClick()
        currencyKeyboardBinding.button0.performClick()
        currencyKeyboardBinding.buttonDot.performClick()
        currencyKeyboardBinding.button5.performClick()
        currencyKeyboardBinding.button0.performClick()

        val resultText = currencyKeyboardBinding.editText.text.toString()
        val resultSpannable = currencyKeyboardBinding.editText.text?.toSpannable()

        assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
        assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
        Assert.assertEquals(resultText, expectedText)
    }


    @Test
    fun `Should set text to "AED 0dot00" decimal enabled when click in order "dot"`() {
        val expectedText = "AED 0.00"
        val expectedSpannable = SpannableString(expectedText).toSpannable()
        expectedSpannable.setSpan(expectedSpannable.length - 2)

        currencyKeyboardBinding.buttonDot.performClick()

        val resultText = currencyKeyboardBinding.editText.text.toString()
        val resultSpannable = currencyKeyboardBinding.editText.text?.toSpannable()

        assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
        assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
        Assert.assertEquals(resultText, expectedText)
    }


    @Test
    fun `Should set text to "AED 1dot38" decimal enabled when click in order , 2 5 delete delete delete 1 , 3 8 9 9`() {
        val expectedText = "AED 1.38"
        val expectedSpannable = SpannableString(expectedText).toSpannable()

        currencyKeyboardBinding.buttonDot.performClick()
        currencyKeyboardBinding.button2.performClick()
        currencyKeyboardBinding.button5.performClick()
        currencyKeyboardBinding.buttonDelete.performClick()
        currencyKeyboardBinding.buttonDelete.performClick()
        currencyKeyboardBinding.buttonDelete.performClick()
        currencyKeyboardBinding.button1.performClick()
        currencyKeyboardBinding.buttonDot.performClick()
        currencyKeyboardBinding.button3.performClick()
        currencyKeyboardBinding.button8.performClick()
        currencyKeyboardBinding.button9.performClick()
        currencyKeyboardBinding.button9.performClick()


        val resultText = currencyKeyboardBinding.editText.text.toString()
        val resultSpannable = currencyKeyboardBinding.editText.text?.toSpannable()

        assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
        assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
        Assert.assertEquals(resultText, expectedText)
    }

}