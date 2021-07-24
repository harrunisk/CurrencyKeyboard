package com.nstudiosappdev.view

import android.text.SpannableString
import androidx.appcompat.widget.AppCompatEditText
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.text.toSpannable
import com.google.android.material.button.MaterialButton
import com.mediastudios.currencykeyboard.R
import com.nstudiosappdev.view.ext.setSpanDecimalDisabled
import com.nstudiosappdev.view.ext.setSpanDecimalEnabled
import com.nstudiosappdev.view.ext.setSpanDecimalOneItemEntered
import com.nstudiosappdev.view.helper.CurrencyKeyboardHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
@LooperMode(LooperMode.Mode.PAUSED)
class CurrencyKeyboardTest {

    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var currencyKeyboard: CurrencyKeyboard

    private lateinit var activity: CurrencyKeyboardTestActivity

    @Before
    fun setUp() {
        activity = Robolectric.setupActivity(
            CurrencyKeyboardTestActivity::class.java
        )
        currencyKeyboard = activity.findViewById(R.id.currencyKeyboardTest)
    }

    @After
    fun reset() {
        currencyKeyboard.resetKeyboard()
    }

    @Test
    fun `Text should be CURRENCY 0dot00 on initial state`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedTest = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 0.00"
            Assert.assertEquals(activity.findViewById<AppCompatEditText>(R.id.editText).text.toString(), expectedTest)
        }

    @Test
    fun `Should set text to CURRENCY 0dot07 decimal enabled when click in order 0 0 7`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedText = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 0.07"
            val expectedSpannable = SpannableString(expectedText).toSpannable()

            activity.findViewById<MaterialButton>(R.id.button0).performClick()
            activity.findViewById<MaterialButton>(R.id.button0).performClick()
            activity.findViewById<MaterialButton>(R.id.button7).performClick()

            val resultText = activity.findViewById<AppCompatEditText>(R.id.editText).text.toString()
            val resultSpannable = activity.findViewById<AppCompatEditText>(R.id.editText).text?.toSpannable()

            assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
            assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
            Assert.assertEquals(resultText, expectedText)
        }

    @Test
    fun `Should set text to CURRENCY 1dot00 when click in 1 on initial state`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedText = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 1.00"
            val expectedSpannable = SpannableString(expectedText).toSpannable()
            expectedSpannable.setSpanDecimalDisabled()

            activity.findViewById<MaterialButton>(R.id.button1).performClick()

            val resultText = activity.findViewById<AppCompatEditText>(R.id.editText).text.toString()
            val resultSpannable = activity.findViewById<AppCompatEditText>(R.id.editText).text?.toSpannable()

            assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
            assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
            Assert.assertEquals(expectedText, resultText)
        }

    @Test
    fun `Should set text to CURRENCY 15,420dot50 when click in order 1 5 4 2 0 dot 50`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedText = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 15,420.50"
            val expectedSpannable = SpannableString(expectedText).toSpannable()

            activity.findViewById<MaterialButton>(R.id.button1).performClick()
            activity.findViewById<MaterialButton>(R.id.button5).performClick()
            activity.findViewById<MaterialButton>(R.id.button4).performClick()
            activity.findViewById<MaterialButton>(R.id.button2).performClick()
            activity.findViewById<MaterialButton>(R.id.button0).performClick()
            activity.findViewById<MaterialButton>(R.id.buttonDot).performClick()
            activity.findViewById<MaterialButton>(R.id.button5).performClick()
            activity.findViewById<MaterialButton>(R.id.button0).performClick()

            val resultText = activity.findViewById<AppCompatEditText>(R.id.editText).text.toString()
            val resultSpannable = activity.findViewById<AppCompatEditText>(R.id.editText).text?.toSpannable()

            assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
            assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
            Assert.assertEquals(resultText, expectedText)
        }


    @Test
    fun `Should set text to CURRENCY 0dot00 decimal enabled when click in dot`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedText = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 0.00"
            val expectedSpannable = SpannableString(expectedText).toSpannable()
            expectedSpannable.setSpanDecimalEnabled()

            activity.findViewById<MaterialButton>(R.id.buttonDot).performClick()

            val resultText = activity.findViewById<AppCompatEditText>(R.id.editText).text.toString()
            val resultSpannable = activity.findViewById<AppCompatEditText>(R.id.editText).text?.toSpannable()

            assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
            assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
            Assert.assertEquals(resultText, expectedText)
        }


    @Test
    fun `Should set text to CURRENCY 1dot38 decimal enabled when click in order , 2 5 delete delete delete 1 , 3 8 9 9`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedText = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 1.38"
            val expectedSpannable = SpannableString(expectedText).toSpannable()

            activity.findViewById<MaterialButton>(R.id.buttonDot).performClick()
            activity.findViewById<MaterialButton>(R.id.button2).performClick()
            activity.findViewById<MaterialButton>(R.id.button5).performClick()
            activity.findViewById<MaterialButton>(R.id.buttonDelete).performClick()
            activity.findViewById<MaterialButton>(R.id.buttonDelete).performClick()
            activity.findViewById<MaterialButton>(R.id.buttonDelete).performClick()
            activity.findViewById<MaterialButton>(R.id.button1).performClick()
            activity.findViewById<MaterialButton>(R.id.buttonDot).performClick()
            activity.findViewById<MaterialButton>(R.id.button3).performClick()
            activity.findViewById<MaterialButton>(R.id.button8).performClick()
            activity.findViewById<MaterialButton>(R.id.button9).performClick()
            activity.findViewById<MaterialButton>(R.id.button9).performClick()

            val resultText = activity.findViewById<AppCompatEditText>(R.id.editText).text.toString()
            val resultSpannable = activity.findViewById<AppCompatEditText>(R.id.editText).text?.toSpannable()

            assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
            assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
            Assert.assertEquals(resultText, expectedText)
        }


    @Test
    fun `Should set text to CURRENCY 0dot07 decimal enabled when click in order dot 0 7`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedText = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 0.07"
            val expectedSpannable = SpannableString(expectedText).toSpannable()

            activity.findViewById<MaterialButton>(R.id.buttonDot).performClick()
            activity.findViewById<MaterialButton>(R.id.button0).performClick()
            activity.findViewById<MaterialButton>(R.id.button7).performClick()

            val resultText = activity.findViewById<AppCompatEditText>(R.id.editText).text.toString()
            val resultSpannable = activity.findViewById<AppCompatEditText>(R.id.editText).text?.toSpannable()

            assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
            assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
            Assert.assertEquals(resultText, expectedText)
        }

    @Test
    fun `Should set text to CURRENCY 4dot70 one decimal item enabled when click in order 4 dot 7`() =
        mainCoroutineRule.testDispatcher.runBlockingTest {
            val expectedText = "${CurrencyKeyboardHelper.getCurrencySymbol(currencyKeyboard.getLocale())} 4.70"
            val expectedSpannable = SpannableString(expectedText).toSpannable()
            expectedSpannable.setSpanDecimalOneItemEntered()

            activity.findViewById<MaterialButton>(R.id.button4).performClick()
            activity.findViewById<MaterialButton>(R.id.buttonDot).performClick()
            activity.findViewById<MaterialButton>(R.id.button7).performClick()


            val resultText = activity.findViewById<AppCompatEditText>(R.id.editText).text.toString()
            val resultSpannable = activity.findViewById<AppCompatEditText>(R.id.editText).text?.toSpannable()

            assert(areSpannableColorsSame(expectedSpannable, resultSpannable))
            assert(areSpannableStartAndEndPointsSame(expectedSpannable, resultSpannable))
            Assert.assertEquals(resultText, expectedText)
        }

}