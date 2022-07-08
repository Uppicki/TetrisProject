package com.example.tetrisproject.models

import android.graphics.Point
import com.example.tetrisproject.constants.CellConstants
import com.example.tetrisproject.constants.FieldConstants
import com.example.tetrisproject.helper.array2dOfBytes
import com.example.tetrisproject.storage.AppPreferences

class AppModel {
    var score = 0
    private var preferences: AppPreferences? = null

    var currentBlock: Block? = null
    var currentState: String = Statues.AWAITING_START.name

    private var field: Array<ByteArray> = array2dOfBytes(
        FieldConstants.ROW_COUNT.value,
        FieldConstants.COLUMN_COUNT.value
    )


    enum class Statues {
        AWAITING_START,
        ACTIVE,
        INACTIVE,
        OVER;
    }

    enum class Motions {
        LEFT,
        RIGHT,
        DOWN,
        ROTATE;
    }


    fun setPreferences(preferences: AppPreferences?) {
        this.preferences = preferences
    }

    fun getCellStatus(row: Int, column: Int): Byte? {
        return field[row][column]
    }

    fun setCellStatus(row: Int, column: Int, status: Byte?) {
        if (status != null)
            field[row][column] = status
    }

    fun isGameOver(): Boolean{
        return currentState == Statues.OVER.name
    }

    fun isGameActive(): Boolean{
        return currentState == Statues.ACTIVE.name
    }

    fun isGameAwaitingStart(): Boolean{
        return currentState == Statues.AWAITING_START.name
    }

    private fun boostScare() {
        score += 10
        if (score > preferences?.getHighScore() as Int)
            preferences?.saveHighScore(score)
    }

    private fun generateNextBlock() {
        currentBlock = Block.createBlock()
    }

    private fun validTranslation(position: Point, shape: Array<ByteArray>): Boolean {
        return if (position.y < 0 || position.x < 0) {
            false
        } else if (position.y + shape.size > FieldConstants.ROW_COUNT.value) {
            false
        } else if (position.x +shape[0].size > FieldConstants.COLUMN_COUNT.value){
            false
        } else {
            for (i in shape.indices)
                for (j in 0 until shape[i].size) {
                    val y = position.y+i
                    val x = position.x+j
                    if (CellConstants.EMPTY.value != shape[i][j] &&
                            CellConstants.EMPTY.value != field[y][x])
                                return false
                }
            true
        }
    }

    private fun moveValid(position: Point, frameNumber: Int?): Boolean {
        val shape: Array<ByteArray>? = currentBlock?.getShape(frameNumber as Int)
        return validTranslation(position, shape as Array<ByteArray>)
    }

    fun generateField(action: String) {
        if (isGameActive()) {

            var frameNumber: Int? = currentBlock?.frameNumber
            val coordinate: Point? = Point()
            coordinate?.x = currentBlock?.position?.x
            coordinate?.y = currentBlock?.position?.y

            when (action) {
                Motions.LEFT.name -> {
                    coordinate?.x = currentBlock?.position?.x?.minus(1)
                }
                Motions.RIGHT.name -> {
                    coordinate?.x = currentBlock?.position?.x?.plus(1)
                }
                Motions.DOWN.name -> {
                    coordinate?.y = currentBlock?.position?.y?.plus(1)
                }
                Motions.ROTATE.name -> {
                    frameNumber = frameNumber?.plus(1)

                    if (frameNumber != null)
                        if (frameNumber >= currentBlock?.frameCount as Int)
                            frameNumber = 0
                }
            }

            if (!moveValid(coordinate as Point, frameNumber)) {
                translateBlock(currentBlock?.position as Point,
                                currentBlock?. frameNumber as Int)
                if (Motions.DOWN.name == action) {
                    boostScare()
                    persistCellDAta()
                    assessField()
                    generateNextBlock()
                    if(!blockAdditionPossible()) {
                        currentState = Statues.OVER.name
                        currentBlock = null
                        resetField(false)
                    }
                }
            } else {
                if (frameNumber != null) {
                    translateBlock(coordinate, frameNumber)
                    currentBlock?.setState(frameNumber, coordinate)
                }
            }
        }
    }

    private fun resetField(ephemeralCellsOnly: Boolean = true) {
        for (i in 0 until FieldConstants.ROW_COUNT.value) {
            (0 until FieldConstants.COLUMN_COUNT.value)
                .filter {
                    !ephemeralCellsOnly || field[i][it] == CellConstants.EPHEMERAL.value
                }
                .forEach{
                    field[i][it] = CellConstants.EMPTY.value
                }
        }
    }

    private fun persistCellDAta() {
        for (i in field.indices)
            for (j in field[i].indices) {
                var status = getCellStatus(i, j)
                if (status == CellConstants.EPHEMERAL.value) {
                    status = currentBlock?.staticValue
                    setCellStatus(i, j, status)
                }
            }
    }

    private fun assessField() {
        for (i in field.indices){
            var emptyCells = 0
            for (j in field[i].indices) {
                val status = getCellStatus(i, j)
                val isEmpty = CellConstants.EMPTY.value == status
                if (isEmpty) emptyCells++
                if (emptyCells == 0) shiftRows(i)
            }
        }
    }

    private fun translateBlock(position: Point, frameNumber: Int) {
        synchronized(field) {
            val shape: Array<ByteArray>? = currentBlock?.getShape(frameNumber)
            if (shape != null) {
                for (i in shape.indices)
                    for (j in shape[i].indices) {
                        val y = position.y + i
                        val x = position.x + j
                        if (CellConstants.EMPTY.value != shape[i][j]) {
                            field[y][x] =shape[i][j]
                        }
                    }
            }
        }
    }

    private fun blockAdditionPossible(): Boolean {
        if (!moveValid(currentBlock?.position as Point,
            currentBlock?.frameNumber)) {
            return false
        }
        return true
    }

    private fun shiftRows(nToRow: Int) {
        if (nToRow > 0)
            for (j in nToRow - 1 downTo 0)
                for (m in field[j].indices)
                    setCellStatus(j+1, m, getCellStatus(j, m))

        for (j in field[0].indices)
            setCellStatus(0, j, CellConstants.EMPTY.value)
    }

    fun startGame() {
        if (!isGameActive()) {
            currentState = Statues.ACTIVE.name
            generateNextBlock()
        }
    }

    fun restartGame() {
        resetModel()
        startGame()
    }

    fun endGame() {
        score = 0
        currentState = AppModel.Statues.OVER.name
    }

    private fun resetModel() {
        resetField(false)
        currentState = Statues.AWAITING_START.name
        score = 0
    }
}