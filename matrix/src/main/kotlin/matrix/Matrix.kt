package matrix

import customThreadPool.CustomThreadPool
import java.util.concurrent.CountDownLatch
import kotlin.random.Random


class Matrix(val rows: Int, val cols: Int, empty: Boolean = false) {
    var longest: Int = 0
    var nThreads = 6
    var matrix: Array<DoubleArray> = Array(rows) {
        DoubleArray(cols) {
            if (!empty) {
                val i = Random.nextDouble(1000.0, 10000.0)
                if (longest < i.toInt().toString().length) {
                    longest = i.toInt().toString().length
                }
                i
            } else {
                longest = 1
                0.0
            }
        }
    }

    operator fun get(rowIndex: Int, colIndex: Int): Double {
        if (rowIndex < 0 || colIndex < 0 || rowIndex >= rows || colIndex >= cols) {
            throw IllegalArgumentException("Matrix.get: Index out of bound")
        } else {
            return matrix[rowIndex][colIndex]
        }
    }

    operator fun set(rowIndex: Int, colIndex: Int, value: Number) {
        if (rowIndex < 0 || colIndex < 0 || rowIndex >= rows || colIndex >= cols) {
            throw IllegalArgumentException("Matrix.set: Index out of bound")
        } else {
            matrix[rowIndex][colIndex] = value.toDouble()
        }
    }

    constructor(doubleArrayOfInt: Array<DoubleArray>) : this(doubleArrayOfInt.size, doubleArrayOfInt[0].size) {
        for (i in doubleArrayOfInt.indices) {
            for (j in doubleArrayOfInt[i].indices) {
                this[i, j] = doubleArrayOfInt[i][j]
            }
        }
    }

    operator fun times(other: Matrix): Matrix {
        return if (cols != other.rows) {
            throw IllegalArgumentException("Matrix.times: Illegal Matrix multiplication.")
        } else {
            val new = Matrix(Array(rows) {
                DoubleArray(other.cols) { it2 ->
                    var sum = 0.0
                    for (i in 0 until other.rows) {
                        sum += this[it, i] * other[i, it2]
                    }
                    sum
                }
            })
            new.longest = this.longest + other.longest
            new
        }
    }

    operator fun times(number: Number): Matrix {
        val result = Matrix(rows, cols, true)
        result.longest = longest
        for (i in this.matrix.indices) {
            for (j in this.matrix[i].indices) {
                result[i, j] = this[i, j] * number.toDouble()
            }
        }
        return result
    }

    infix fun multiplyBy(number: Number): Matrix {
        val cdl = CountDownLatch(rows)
        val result = Matrix(rows, cols, true)
        result.longest = longest
        val executor = CustomThreadPool(nThreads)
        for (i in this.matrix.indices) {
            val I = i
            executor.execute {
                for (j in this.matrix[0].indices) {
                    result[I, j] = this[I, j] * number.toDouble()
                }
                cdl.countDown()
            }
        }
        cdl.await()
        executor.shutdown()
        return result
    }

    infix fun multiplyBy(other: Matrix): Matrix {
        if (cols != other.rows) {
            throw IllegalArgumentException("Matrix.times: Illegal Matrix multiplication.")
        } else {
            val cdl = CountDownLatch(this.rows * other.cols)
            val result = Matrix(rows, other.cols, true)
            result.longest = this.longest + other.longest
            val executor = CustomThreadPool(nThreads)
            for (i in this.matrix.indices) {
                val I = i
                for (j in other.matrix[i].indices) {
                    val J = j
                    executor.execute {
                        for (k in other.matrix.indices) {
                            result[I, J] += this[I, k] * other[k, J]
                        }
                        cdl.countDown()
                    }
                }
            }
            cdl.await()
            executor.shutdown()
            return result
        }
    }

    override fun toString(): String {
        var string = ""
        for (i in 0 until rows) {
            string += "["
            for (j in 0 until cols) {
                string += String.format("%${this.longest + 6}.2f", this[i, j])
            }
            string += "  ]\n"
        }
        return string
    }
}
