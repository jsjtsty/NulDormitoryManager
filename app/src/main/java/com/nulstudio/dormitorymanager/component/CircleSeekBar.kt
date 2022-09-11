package com.nulstudio.dormitorymanager.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.nulstudio.dormitorymanager.R
import kotlin.math.*

class CircleSeekBar : View {
    private val paint: Paint = Paint()
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var radius: Int = 0
    private var innerRadius: Int = 0

    private var ringLineWidth: Float = 0.0f
    private var ringPositiveColor: Int = 0
    private var ringPositiveBackgroundColor: Int = 0
    private var ringLineLengthPercentage: Float = 0.0f
    private var maxValue: Float = 10.0f
    private var ringLineCount: Int = 30

    private var rounds: Int = 0
    private var roundValue: Float = 0.0f

    private var virtualValue: Float = 0.0f

    private var downOnArc: Boolean = false

    private var progressChangeListener: OnProgressChangeListener? = null

    val value: Float
        get() = roundValue + rounds * maxValue

    constructor(context: Context) : this(context, null) {

    }

    constructor(context: Context, attributeSet: AttributeSet?)
            : this(context, attributeSet, 0) {

    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleSeekBar)

        ringLineWidth = typedArray.getDimension(R.styleable.CircleSeekBar_ringLineWidth, 4.0f)
        maxValue = typedArray.getFloat(R.styleable.CircleSeekBar_maxValue, 10.0f)
        roundValue = typedArray.getFloat(R.styleable.CircleSeekBar_value, 0.0f)
        ringLineCount = typedArray.getInteger(R.styleable.CircleSeekBar_ringLineCount, 120)
        ringPositiveColor = typedArray.getColor(
            R.styleable.CircleSeekBar_ringPositiveColor,
            context.getColor(R.color.circle_seekbar_positive)
        )
        ringPositiveBackgroundColor = typedArray.getColor(
            R.styleable.CircleSeekBar_ringPositiveBackgroundColor,
            context.getColor(R.color.circle_seekbar_positive_background)
        )
        ringLineLengthPercentage = 1.0f - typedArray.getFloat(
            R.styleable.CircleSeekBar_ringLineLengthPercentage, 0.1f
        )

        typedArray.recycle()
    }

    fun setValue(value: Float) {
        this.roundValue = value
        requestLayout()
        invalidate()
    }

    private fun isOnArc(x: Int, y: Int): Boolean {
        val dist = (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)
        val max: Int = (radius * 1.2).toInt()
        val min: Int = (innerRadius * 0.8).toInt()
        return dist >= min * min && dist <= max * max;
    }

    private fun calcArcValue(x: Int, y: Int): Double {
        val rx: Int = x - centerX
        val ry: Int = y - centerY
        val angle: Double = -atan2(ry.toDouble(), rx.toDouble()) / PI
        Log.i("rad", angle.toString())

        return when (angle) {
            in 0.0..0.5 -> (0.5 - angle) / 2
            in 0.5..1.0 -> -(angle - 0.5) / 2 + 1
            in -0.5..0.0 -> -angle / 2 + 0.25
            else -> (-0.5 - angle) / 2 + 0.5
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val min = min(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.getSize(widthMeasureSpec))
        setMeasuredDimension(min, min)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = (width * 0.9f / 2).toInt()
        innerRadius = (radius * ringLineLengthPercentage).toInt()
        centerX = width / 2
        centerY = height / 2
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event == null) return super.onTouchEvent(event)

        val action = event.action
        val x = (event.x).toInt()
        val y = (event.y).toInt()

        return when(action) {
            MotionEvent.ACTION_DOWN -> {
                if(isOnArc(x, y)) {
                    setValue(calcArcValue(x, y).toFloat() * maxValue)
                    downOnArc = true
                    progressChangeListener?.onProgressChange(this, value)
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if(downOnArc) {
                    val curArc = calcArcValue(x, y).toFloat()
                    if(curArc < 0.4f && roundValue > 0.6f * maxValue) {
                        ++rounds
                        setValue(curArc * maxValue)
                    } else if(curArc > 0.6f && roundValue < 0.4f * maxValue) {
                        if(rounds > 0) {
                            --rounds
                            setValue(curArc * maxValue)
                        } else {
                            setValue(0.0f)
                        }
                    } else {
                        setValue(curArc * maxValue)
                    }
                    progressChangeListener?.onProgressChange(this, value)
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                downOnArc = false
                super.onTouchEvent(event)
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.isAntiAlias = true
        paint.strokeWidth = ringLineWidth

        val relValue: Float = when {
            roundValue > 0.0f -> roundValue / maxValue
            else -> 0.0f
        }
        val paintLines: Int = abs(relValue * ringLineCount).toInt()

        when {
            relValue > 0 -> {
                paint.color = ringPositiveColor
                for(i: Int in 0 until ringLineCount) {
                    canvas?.drawLine(
                        (centerX + innerRadius * sin(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        (centerY - innerRadius * cos(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        (centerX + radius * sin(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        (centerY - radius * cos(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        paint
                    )
                    if(i == paintLines) paint.color = ringPositiveBackgroundColor
                }
            }
            else -> {
                paint.color = ringPositiveBackgroundColor
                for(i: Int in 0..ringLineCount) {
                    canvas?.drawLine(
                        (centerX + innerRadius * sin(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        (centerY - innerRadius * cos(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        (centerX + radius * sin(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        (centerY - radius * cos(i.toFloat() / ringLineCount * 2 * PI)).toFloat(),
                        paint
                    )
                }
            }
        }
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener) {
        progressChangeListener = listener
    }

    interface OnProgressChangeListener {
        fun onProgressChange(view: View, value: Float)
    }
}