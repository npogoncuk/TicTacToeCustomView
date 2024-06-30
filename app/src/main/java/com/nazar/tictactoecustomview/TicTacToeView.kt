package com.nazar.tictactoecustomview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.max
import kotlin.properties.Delegates

class TicTacToeView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        R.style.DefaultTicTacToeFieldStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.ticTacToeFieldStyle
    )

    constructor(context: Context) : this(context, null)

    private var player1Color by Delegates.notNull<Int>()
    private var player2Color by Delegates.notNull<Int>()
    private var gridColor by Delegates.notNull<Int>()

    var ticTacToeField: TicTacToeField? = null
        set(value) {
            field?.listeners?.remove(onFieldChangeListener)
            field = value
            field?.listeners?.add(onFieldChangeListener)

            requestLayout()
            invalidate()
        }

    init {
        attrs
            ?.let { initializeAttributes(it, defStyleAttr, defStyleRes) }
            ?: initializeWithDefaults()


    }

    private fun initializeAttributes(attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TicTacToeView, defStyleAttr, defStyleRes)

        player1Color = typedArray.getColor(R.styleable.TicTacToeView_player1Color, DEFAULT_PLAYER1_COLOR)
        player2Color = typedArray.getColor(R.styleable.TicTacToeView_player2Color, DEFAULT_PLAYER2_COLOR)
        gridColor = typedArray.getColor(R.styleable.TicTacToeView_gridColor, DEFAULT_GRID_COLOR)

        typedArray.recycle()
    }

    private fun initializeWithDefaults() {
        player1Color = DEFAULT_PLAYER1_COLOR
        player2Color = DEFAULT_PLAYER2_COLOR
        gridColor = DEFAULT_GRID_COLOR
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val desiredCellSizeInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DESIRED_CELL_SIZE.toFloat(),
            resources.displayMetrics
        ).toInt()

        val rows = ticTacToeField?.rows ?: 0
        val columns = ticTacToeField?.columns ?: 0
        val desiredWidth = max(minWidth, columns * desiredCellSizeInPixels + paddingLeft + paddingRight)
        val desiredHeight = max(minHeight, rows * desiredCellSizeInPixels + paddingTop + paddingBottom)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ticTacToeField?.listeners?.add(onFieldChangeListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ticTacToeField?.listeners?.remove(onFieldChangeListener)
    }



    private val onFieldChangeListener = object : OnFiledChangedListener {
        override fun invoke(field: TicTacToeField) {

        }
    }

    companion object {
        private const val DEFAULT_PLAYER1_COLOR = Color.GREEN
        private const val DEFAULT_PLAYER2_COLOR = Color.RED
        private const val DEFAULT_GRID_COLOR = Color.GRAY

        private val DESIRED_CELL_SIZE = 50 // in dp
    }
}