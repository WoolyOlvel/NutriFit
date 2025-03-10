package com.ascrib.nutrifit.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.ascrib.nutrifit.R

class PinEntryEditText : AppCompatEditText {

    private var mSpace = 24f //24 dp by default, space between the lines
    private var mNumChars = 4f
    private var mLineSpacing = 8f //8dp by default, height of the text from our lines
    private var mClickListener: View.OnClickListener? = null
    private var mLineStroke = 1f //1dp by default
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLinesPaint: Paint? = null
    private var textPaint: TextPaint? = null
    private var textEmptyPaint: TextPaint? = null
    private val mStates = arrayOf(intArrayOf(android.R.attr.state_selected), // selected
        intArrayOf(android.R.attr.state_focused), // focused
        intArrayOf(-android.R.attr.state_focused))// unfocused
    private val mColors = intArrayOf(Color.GREEN, Color.BLACK, Color.GRAY)
    private val mColorStates = ColorStateList(mStates, mColors)
    private var textWidths = FloatArray(4)

    /**
     * CompanionObject
     */
    companion object {
        private const val MAX_LENGTH = "maxLength"
        private const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
        /**
         * Returns if the pinEntryEditText is in password mode
         * @param inputType the inputType for this pinEntryEditText
         * @return whether this pinEntryEditText is in password mode
         */
        private fun isPasswordInputType(inputType: Int): Boolean {
            val variation = inputType and (EditorInfo.TYPE_MASK_CLASS or EditorInfo.TYPE_MASK_VARIATION)
            return (variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                    || variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
                    || variation == EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)
        }
    }

    /**
     * Constructor
     * @param context Context
     */
    constructor(context: Context) : super(context)

    /**
     * Constructor
     * @param context Context
     * @param attrs Attribute Set for view
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    /**
     * Constructor
     * @param context Context
     * @param attrs Attribute Set for view
     * @param defStyleAttr Int style from attr
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    /**
     * Sets the textPaint, colors and vars needed to paint the bottom lines
     * @param context the context
     * @param attrs the attributes
     */
    private fun init(context: Context, attrs: AttributeSet) {
        val multi = context.resources.displayMetrics.density
        mLineStroke *= multi
        mLineStrokeSelected *= multi
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint?.density = multi
        textPaint?.style = Paint.Style.FILL
        textPaint?.textSize = textSize
        textPaint?.color = ContextCompat.getColor(context, R.color.colorPrimary)
        textEmptyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textEmptyPaint?.style = Paint.Style.STROKE
        textEmptyPaint?.textSize = textSize
        textEmptyPaint?.strokeWidth = 2F
        textEmptyPaint?.color = ContextCompat.getColor(context, R.color.colorPrimary)
        mLinesPaint = Paint(paint)
        mLinesPaint?.strokeWidth = mLineStroke
        if (!isInEditMode) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(R.color.colorPrimary, outValue, true)
            val colorActivated = outValue.data
            mColors[0] = colorActivated

            context.theme.resolveAttribute(R.color.colorSecondary, outValue, true)
            val colorDark = outValue.data
            mColors[1] = colorDark

            context.theme.resolveAttribute(R.color.orange, outValue, true)
            val colorHighlight = outValue.data
            mColors[2] = colorHighlight
        }
        setBackgroundResource(0)
        mSpace *= multi //convert to pixels for our density
        mLineSpacing *= multi //convert to pixels for our density
        val mMaxLength = attrs.getAttributeIntValue(XML_NAMESPACE_ANDROID, MAX_LENGTH, 4)
        textWidths = FloatArray(mMaxLength)
        mNumChars = mMaxLength.toFloat()

        // When tapped, move cursor to end of text.
        super.setOnClickListener { v ->
            setSelection(text!!.length)
            mClickListener?.let {
                mClickListener?.onClick(v)
            }
        }
    }

    /**
     * Setting click listener in view
     * @param l OnClickListener
     */
    override fun setOnClickListener(l: View.OnClickListener?) {
        mClickListener = l
    }

    /**
     * Draw canvas
     * @param canvas Canvas of view
     */
    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas);
        val availableWidth = width - paddingRight - paddingLeft
        val mCharSize: Float
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumChars * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }

        var startX = paddingLeft
        val bottom = height - paddingBottom

        //Text Width
        val text = text
        val textLength = text!!.length
        paint.getTextWidths(getText(), 0, textLength, textWidths)

        var i = 0
        val passwordInputType = isPasswordInputType(inputType)
        while (i < mNumChars) {
            updateColorForLines(i == textLength)
            val middle = startX + mCharSize / 2
            if (passwordInputType)
                canvas.drawCircle(middle, (bottom - mLineSpacing) / 2, mCharSize / 8, textEmptyPaint!!)
            else
                canvas.drawLine(startX.toFloat(), bottom.toFloat(), startX + mCharSize, bottom.toFloat(), mLinesPaint!!)
            if (getText()!!.length > i) {
                if (passwordInputType)
                    canvas.drawCircle(middle, (bottom - mLineSpacing) / 2, mCharSize / 8, textPaint!!)
                else
                    canvas.drawText(text, i, i + 1, middle - textWidths[i] / 2, bottom - mLineSpacing, textPaint!!)
            }

            startX += if (mSpace < 0) {
                (mCharSize * 2).toInt()
            } else {
                (mCharSize + mSpace).toInt()
            }
            i++
        }
    }

    /**
     * Gets the color associated with the state
     * @param states the states
     * @return the color associated with the state
     */
    private fun getColorForState(vararg states: Int): Int {
        return mColorStates.getColorForState(states, Color.BLUE)
    }

    /**
     * Updates mLinesPaint based on next highlight
     * @param next whether the current char is the next character to be input
     */
    private fun updateColorForLines(next: Boolean) {
        if (isFocused) {
            mLinesPaint?.strokeWidth = mLineStrokeSelected
            mLinesPaint?.color = ContextCompat.getColor(context, R.color.colorPrimary)
            if (next) {
                mLinesPaint?.color = ContextCompat.getColor(context, R.color.colorPrimary)
            }
        } else {
            mLinesPaint?.strokeWidth = mLineStroke
            mLinesPaint?.color = ContextCompat.getColor(context, R.color.colorPrimary)
        }
    }

}