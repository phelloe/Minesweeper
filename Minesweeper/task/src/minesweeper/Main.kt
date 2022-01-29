package minesweeper

import kotlin.random.Random

const val height = 9
const val width = 9

fun main() {
    print("How many mines do you want on the field? ")
    val minefield = Minefield(height, width, readLine()!!.toInt())
    minefield.print()

    while (!minefield.gameOver()) {
        print("Set/unset mines marks or claim a cell as free: ")
        val (x, y, z) = readLine()!!.split(" ")
        when (z) {
            "mine" -> if (minefield.toggleMark(x.toInt(), y.toInt())) minefield.print()
            "free" -> if (minefield.explore(x.toInt(), y.toInt())) minefield.print()
            else -> println("Unknown command. Expecting 'mine' or 'free")
        }
    }
    if (minefield.boom) println("You stepped on a mine and failed!")
    else println("Congratulations! You found all the mines!")
}

abstract class Field(var isHidden: Boolean, var isMarked: Boolean = false)

class Mine : Field(true) {
    override fun toString() = when {
        isMarked -> "*"
        isHidden -> "."
        else -> "X"
    }
}

class Empty : Field(true) {
    var hint = 0
    override fun toString() = when {
        isMarked -> "*"
        isHidden -> "."
        hint == 0 -> "/"
        else -> hint.toString()
    }
}

class Minefield(private val n: Int, private val m: Int, private val numberOfMines: Int) {
    private var firstmove = true
    var boom = false
    private val random = Random.Default
    private var minefield = (
            List(numberOfMines) { Mine() }
                    + List(n * m - numberOfMines) { Empty() })
        .shuffled(random)
        .chunked(m)

    init {
        for ((i, row) in minefield.withIndex()) {
            row.mapIndexed { j, elem ->
                when (elem) {
                    is Empty -> elem.hint = checkField(i, j)
                    else -> {}
                }
            }
        }
    }

    private fun reveal(i: Int, j: Int, unmark: Boolean = false) {
        if (i in 1..width && j in 1..height) {
            when (val field = minefield[j - 1][i - 1]) {
                is Empty -> {
                    if (field.isHidden) {
                        field.isHidden = false
                        if (unmark) {
                            field.isMarked = false
                        }

                        if (field.hint == 0) {
                            reveal(i + 1, j + 1, true)
                            reveal(i + 1, j, true)
                            reveal(i + 1, j - 1, true)
                            reveal(i, j + 1, true)
                            reveal(i, j - 1, true)
                            reveal(i - 1, j + 1, true)
                            reveal(i - 1, j, true)
                            reveal(i - 1, j - 1, true)
                        }
                    }
                }
            }
        }
    }

    fun explore(i: Int, j: Int): Boolean {
        if (i in 1..width && j in 1..height) {
            when (val field = minefield[j - 1][i - 1]) {
                is Mine -> {
                    if (firstmove) {
                        minefield = (
                                List(numberOfMines) { Mine() }
                                        + List(n * m - numberOfMines) { Empty() })
                            .shuffled(random)
                            .chunked(m)
                        for ((x, row) in minefield.withIndex()) {
                            row.mapIndexed { y, elem ->
                                when (elem) {
                                    is Empty -> elem.hint = checkField(x, y)
                                    else -> {}
                                }
                            }
                        }
                    }
                    boom = true
                    minefield.forEach { row -> row.forEach { it.isHidden = false } }
                    return true
                }
                is Empty -> {
                    firstmove = false
                    return if (field.hint > 0) {
                        if (field.isHidden) {
                            field.isHidden = false
                            true
                        } else {
                            println("There is a number here!")
                            false
                        }
                    } else {
                        //field.isHidden = false
                        reveal(i, j)
                        true
                    }
                }
            }
            return true
        } else {
            println("Out of bounce!")
            return false
        }

    }

    fun toggleMark(i: Int, j: Int) =
        if (i in 1..width && j in 1..height) {
            when (val field = minefield[j - 1][i - 1]) {
                is Empty -> {
                    if (field.isHidden) {
                        field.isMarked = !field.isMarked
                        true
                    } else {
                        println("There is a number here!")
                        false
                    }
                }
                else -> {
                    field.isMarked = !field.isMarked
                    true
                }
            }
        } else {
            println("Out of bounce!")
            false
        }

    fun gameOver() =
        boom || minefield.all { row ->
            row.all { field ->
                when (field) {
                    is Mine -> field.isMarked
                    else -> !field.isMarked
                }
            }
        } || minefield.all { row ->
            row.all { field ->
                when (field) {
                    is Empty -> !field.isHidden
                    else -> true
                }
            }
        }

    fun print() {
        print(" |")
        for (i in 0 until width) print(i + 1)
        println("|")
        print("-|")
        for (i in 0 until width) print("-")
        println("|")
        minefield.forEachIndexed { index, it ->
            println((index + 1).toString() + "|" + it.joinToString("") + "|")
        }
        print("-|")
        for (i in 0 until width) print("-")
        println("|")
    }

    private fun checkField(x: Int, y: Int) = listOf(
        getFromCoordinate(x - 1, y - 1),
        getFromCoordinate(x, y - 1),
        getFromCoordinate(x + 1, y - 1),
        getFromCoordinate(x - 1, y),
        getFromCoordinate(x + 1, y),
        getFromCoordinate(x - 1, y + 1),
        getFromCoordinate(x, y + 1),
        getFromCoordinate(x + 1, y + 1)
    ).count { it is Mine }

    private fun getFromCoordinate(x: Int, y: Int) =
        if (x in 0 until height && y in 0 until width) minefield[x][y] else Empty()

}
