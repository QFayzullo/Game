package uz.gita.numbergame2048

import android.content.SharedPreferences

class AppRepository {

    var matrix = emptyMatrix()
        private set

    var score = 0
        private set

    private var previousMatrix: Array<Array<Int>>? = null
    private var previousScore = 0

    init {
        addNewElement()
        addNewElement()
    }

    fun addNewElement() {
        val list = ArrayList<Pair<Int, Int>>()
        for (i in 0 until 4)
            for (j in 0 until 4)
                if (matrix[i][j] == 0) list.add(Pair(i, j))
        if (list.isEmpty()) return
        val random = list.random()
        matrix[random.first][random.second] = if (Math.random() < 0.9) 2 else 4
    }

    fun resetGame() {
        matrix = emptyMatrix()
        score = 0
        previousMatrix = null
        previousScore = 0
        addNewElement()
        addNewElement()
    }

    fun saveToPrefs(prefs: SharedPreferences) {
        val editor = prefs.edit()
        for (i in 0 until 4)
            for (j in 0 until 4)
                editor.putInt("cell_${i}_${j}", matrix[i][j])
        editor.putInt("score", score)
        editor.apply()
    }

    fun loadFromPrefs(prefs: SharedPreferences) {
        for (i in 0 until 4)
            for (j in 0 until 4)
                matrix[i][j] = prefs.getInt("cell_${i}_${j}", 0)
        score = prefs.getInt("score", 0)
    }

    fun hasSavedGame(prefs: SharedPreferences): Boolean {
        for (i in 0 until 4)
            for (j in 0 until 4)
                if (prefs.getInt("cell_${i}_${j}", 0) != 0) return true
        return false
    }

    fun clearSavedGame(prefs: SharedPreferences) {
        val editor = prefs.edit()
        for (i in 0 until 4)
            for (j in 0 until 4)
                editor.putInt("cell_${i}_${j}", 0)
        editor.putInt("score", 0)
        editor.apply()
    }
    fun canUndo(): Boolean = previousMatrix != null

    fun undo() {
        previousMatrix?.let {
            matrix = it
            score = previousScore
            previousMatrix = null
        }
    }

    fun isGameOver(): Boolean {
        for (i in 0 until 4)
            for (j in 0 until 4)
                if (matrix[i][j] == 0) return false
        for (i in 0 until 4)
            for (j in 0 until 4) {
                if (j < 3 && matrix[i][j] == matrix[i][j + 1]) return false
                if (i < 3 && matrix[i][j] == matrix[i + 1][j]) return false
            }
        return true
    }

    fun isWin(): Boolean {
        for (i in 0 until 4)
            for (j in 0 until 4)
                if (matrix[i][j] == 2048) return true
        return false
    }
    fun moveToLeft(): Boolean {
        val old = copyMatrix()
        saveState()
        val temp = emptyMatrix()
        for (i in 0 until 4) {
            val merged = merge(getRow(i))
            for (k in merged.indices) temp[i][k] = merged[k]
        }
        matrix = temp
        val changed = !matricesEqual(old, matrix)
        if (!changed) previousMatrix = null
        return changed
    }

    fun moveToRight(): Boolean {
        val old = copyMatrix()
        saveState()
        val temp = emptyMatrix()
        for (i in 0 until 4) {
            val row = getRow(i)
            row.reverse()
            val merged = merge(row)
            val reversed = merged.reversed()
            val offset = 4 - reversed.size
            for (k in reversed.indices) temp[i][k + offset] = reversed[k]
        }
        matrix = temp
        val changed = !matricesEqual(old, matrix)
        if (!changed) previousMatrix = null
        return changed
    }

    fun moveToUp(): Boolean {
        val old = copyMatrix()
        saveState()
        val temp = emptyMatrix()
        for (j in 0 until 4) {
            val merged = merge(getCol(j))
            for (k in merged.indices) temp[k][j] = merged[k]
        }
        matrix = temp
        val changed = !matricesEqual(old, matrix)
        if (!changed) previousMatrix = null
        return changed
    }

    fun moveToDown(): Boolean {
        val old = copyMatrix()
        saveState()
        val temp = emptyMatrix()
        for (j in 0 until 4) {
            val col = getCol(j)
            col.reverse()
            val merged = merge(col)
            val reversed = merged.reversed()
            val offset = 4 - reversed.size
            for (k in reversed.indices) temp[k + offset][j] = reversed[k]
        }
        matrix = temp
        val changed = !matricesEqual(old, matrix)
        if (!changed) previousMatrix = null
        return changed
    }
    private fun merge(input: MutableList<Int>): List<Int> {
        val ls = ArrayList<Int>()
        var isAdded = false
        for (value in input) {
            if (value == 0) continue
            if (ls.isEmpty()) {
                ls.add(value)
            } else {
                if (ls.last() == value && !isAdded) {
                    val merged = ls.last() * 2
                    ls[ls.lastIndex] = merged
                    score += merged
                    isAdded = true
                } else {
                    ls.add(value)
                    isAdded = false
                }
            }
        }
        return ls
    }

    private fun saveState() {
        previousMatrix = copyMatrix()
        previousScore = score
    }

    private fun getRow(i: Int): MutableList<Int> {
        val list = ArrayList<Int>()
        for (j in 0 until 4) list.add(matrix[i][j])
        return list
    }

    private fun getCol(j: Int): MutableList<Int> {
        val list = ArrayList<Int>()
        for (i in 0 until 4) list.add(matrix[i][j])
        return list
    }

    private fun emptyMatrix(): Array<Array<Int>> = arrayOf(
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0),
        arrayOf(0, 0, 0, 0)
    )

    private fun copyMatrix(): Array<Array<Int>> {
        val copy = emptyMatrix()
        for (i in 0 until 4)
            for (j in 0 until 4)
                copy[i][j] = matrix[i][j]
        return copy
    }

    private fun matricesEqual(a: Array<Array<Int>>, b: Array<Array<Int>>): Boolean {
        for (i in 0 until 4)
            for (j in 0 until 4)
                if (a[i][j] != b[i][j]) return false
        return true
    }
}