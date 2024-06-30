package com.nazar.tictactoecustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

typealias OnCellActionListener = (row: Int, column: Int, field: TicTacToeField) -> Unit

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
            updateViewSizes()

            requestLayout()
            invalidate()
        }

    var cellActionListener: OnCellActionListener? = null

    private val fieldRect = RectF()
    private var cellSize = 0f
    private val cellPadding: Float
        get() = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                7f,
                resources.displayMetrics
            )

    private val cellRect = object : RectF() {
        operator fun get(row: Int, column: Int): RectF {
            val left = fieldRect.left + column * cellSize + cellPadding
            val top = fieldRect.top + row * cellSize + cellPadding
            val right = left + cellSize - cellPadding * 2
            val bottom = top + cellSize - cellPadding * 2

            set(left, top, right, bottom)
            return this
        }
    }

    private lateinit var player1Paint: Paint
    private lateinit var player2Paint: Paint
    private lateinit var gridPaint: Paint

    init {
        attrs
            ?.let { initializeAttributes(it, defStyleAttr, defStyleRes) }
            ?: initializeWithDefaults()

        initPaints()
        if (isInEditMode) {
            ticTacToeField = TicTacToeField(8, 6)
            ticTacToeField?.apply {
                this[2, 2] = Cell.PLAYER_X
                this[3, 3] = Cell.PLAYER_O
            }
        }
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

    private fun initPaints() {
        fun Paint.setUpPaint(color: Int, strokeWidth: Float = 3f /* in dp*/) = this.apply {
            this.flags = Paint.ANTI_ALIAS_FLAG
            this.color = color
            this.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                strokeWidth,
                resources.displayMetrics
            )
            this.style = Paint.Style.STROKE
        }

        player1Paint = Paint().setUpPaint(player1Color)
        player2Paint = Paint().setUpPaint(player2Color)
        gridPaint = Paint().setUpPaint(gridColor, strokeWidth = 1.5f)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ticTacToeField?.listeners?.add(onFieldChangeListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ticTacToeField?.listeners?.remove(onFieldChangeListener)
    }

    private val horizontalPaddings: Int
        get() = paddingLeft + paddingRight

    private val verticalPaddings: Int
        get() = paddingTop + paddingBottom

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + horizontalPaddings
        val minHeight = suggestedMinimumHeight + verticalPaddings

        val desiredCellSizeInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DESIRED_CELL_SIZE.toFloat(),
            resources.displayMetrics
        ).toInt()

        val rows = ticTacToeField?.rows ?: 0
        val columns = ticTacToeField?.columns ?: 0
        val desiredWidth = max(minWidth, columns * desiredCellSizeInPixels + horizontalPaddings)
        val desiredHeight = max(minHeight, rows * desiredCellSizeInPixels + verticalPaddings)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }

    private fun updateViewSizes() {
        val field = ticTacToeField ?: return

        val safeWidth = width - horizontalPaddings
        val safeHeight = height - verticalPaddings

        val cellWidth = safeWidth / field.columns.toFloat()
        val cellHeight = safeHeight / field.rows.toFloat()
        cellSize = min(cellWidth, cellHeight)

        val fieldWidth = field.columns * cellSize
        val fieldHeight = field.rows * cellSize

        with(fieldRect) {
            left = paddingLeft + (safeWidth - fieldWidth) / 2
            top = paddingTop + (safeHeight - fieldHeight) / 2
            right = left + fieldWidth
            bottom = top + fieldHeight
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val field = ticTacToeField ?: return
        if (cellSize == 0f || fieldRect.isEmpty) return

        canvas.drawGrid(field)
        canvas.drawCells(field)
    }

    private fun Canvas.drawGrid(field: TicTacToeField) {
        val rows = field.rows
        val columns = field.columns

        val xStart = fieldRect.left
        val eEnd = fieldRect.right
        for (i in 0 .. rows) {
            val y = fieldRect.top + i * cellSize
            drawLine(xStart, y, eEnd, y, gridPaint)
        }

        val yStart = fieldRect.top
        val yEnd = fieldRect.bottom
        for (j in 0 .. columns) {
            val x = fieldRect.left + j * cellSize
            drawLine(x, yStart, x, yEnd, gridPaint)
        }
    }

    private fun Canvas.drawCells(field: TicTacToeField) {
        fun drawPlayer1(row: Int, column: Int) {
            val cellRect = cellRect[row, column]
            drawLine(cellRect.left, cellRect.top, cellRect.right, cellRect.bottom, player1Paint)
            drawLine(cellRect.right, cellRect.top, cellRect.left, cellRect.bottom, player1Paint)
        }

        fun drawPlayer2(row: Int, column: Int) {
            val cellRect = cellRect[row, column]
            drawCircle(cellRect.centerX(), cellRect.centerY(), cellRect.width() / 2, player2Paint)
        }

        for (row in 0 until field.rows) {
            for (column in 0 until field.columns) {
                when (field[row, column]) {
                    Cell.PLAYER_X -> drawPlayer1(row, column)
                    Cell.PLAYER_O -> drawPlayer2(row, column)
                    Cell.EMPTY -> Unit
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val field = ticTacToeField ?: return false

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {

                val x = event.x
                val y = event.y

                val row = ((y - fieldRect.top) / cellSize).toInt()
                val column = ((x - fieldRect.left) / cellSize).toInt()

                if (row in 0 until field.rows && column in 0 until field.columns) {
                    cellActionListener?.invoke(row, column, field)
                    return true
                }

                return false
            }
        }
        return false
    }

    private val onFieldChangeListener : OnFiledChangedListener = {
        invalidate()
    }

    companion object {
        private const val DEFAULT_PLAYER1_COLOR = Color.GREEN
        private const val DEFAULT_PLAYER2_COLOR = Color.RED
        private const val DEFAULT_GRID_COLOR = Color.GRAY

        private val DESIRED_CELL_SIZE = 50 // in dp
    }
}