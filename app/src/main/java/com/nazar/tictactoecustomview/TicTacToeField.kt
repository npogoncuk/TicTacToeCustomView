package com.nazar.tictactoecustomview

enum class Cell {
    PLAYER_X, PLAYER_O, EMPTY
}

typealias OnFiledChangedListener = (TicTacToeField) -> Unit

class TicTacToeField(
    val rows: Int,
    val columns: Int,
) {
    init {
        require(rows > 0 && columns > 0) { "Rows and columns must be positive" }
    }

    private val cells = Array(rows) { Array(columns) { Cell.EMPTY } }

    val listeners = mutableListOf<OnFiledChangedListener>()

    operator fun get(i: Int, j: Int): Cell {
        requireValidIndices(i, j)
        return cells[i][j]
    }

    operator fun set(i: Int, j: Int, cell: Cell) {
        requireValidIndices(i, j)
        cells[i][j] = cell
        listeners.forEach { it(this) }
    }

    operator fun set(i: Int, j: Int, getCell: () -> Cell) {
        this[i, j] = getCell()
    }


}

private fun TicTacToeField.requireValidIndices(i: Int, j: Int) {
    require(i in 0 until rows && j in 0 until columns) { "Invalid indices" }
}