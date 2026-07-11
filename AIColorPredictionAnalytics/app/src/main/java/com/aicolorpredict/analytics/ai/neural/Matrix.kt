package com.aicolorpredict.analytics.ai.neural

import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Minimal pure-Kotlin matrix math used by the neural models. We deliberately
 * do not pull in a native BLAS — the networks are tiny (<= 64 hidden units)
 * and the overhead of cache misses is dominated by the rest of the pipeline.
 *
 * All operations are allocation-explicit (no implicit temporaries in hot loops)
 * so the GC pressure stays predictable.
 */
class Matrix(val rows: Int, val cols: Int) {
    val data: DoubleArray = DoubleArray(rows * cols)

    operator fun get(r: Int, c: Int): Double = data[r * cols + c]
    operator fun set(r: Int, c: Int, v: Double) { data[r * cols + c] = v }

    fun copy(): Matrix {
        val m = Matrix(rows, cols)
        System.arraycopy(data, 0, m.data, 0, data.size)
        return m
    }

    /** In-place element-wise tanh. */
    fun tanhInPlace() {
        for (i in data.indices) {
            val x = data[i]
            data[i] = if (x > 20.0) 1.0 else if (x < -20.0) -1.0 else kotlin.math.tanh(x)
        }
    }

    /** In-place sigmoid. */
    fun sigmoidInPlace() {
        for (i in data.indices) {
            val x = data[i]
            data[i] = if (x > 30.0) 1.0 else if (x < -30.0) 0.0 else 1.0 / (1.0 + exp(-x))
        }
    }

    /** Returns A × B (this × other). */
    fun matmul(other: Matrix): Matrix {
        require(cols == other.rows) { "matmul dim mismatch: $rows×$cols · ${other.rows}×${other.cols}" }
        val out = Matrix(rows, other.cols)
        for (r in 0 until rows) {
            for (c in 0 until other.cols) {
                var sum = 0.0
                for (k in 0 until cols) sum += data[r * cols + k] * other.data[k * other.cols + c]
                out.data[r * other.cols + c] = sum
            }
        }
        return out
    }

    /** Returns A × B^T (this × other-transposed). */
    fun matmulTransposed(other: Matrix): Matrix {
        require(cols == other.cols) { "matmulT dim mismatch: $rows×$cols · ${other.rows}×${other.cols}" }
        val out = Matrix(rows, other.rows)
        for (r in 0 until rows) {
            for (c in 0 until other.rows) {
                var sum = 0.0
                for (k in 0 until cols) sum += data[r * cols + k] * other.data[c * cols + k]
                out.data[r * other.rows + c] = sum
            }
        }
        return out
    }

    fun addBroadcastRow(bias: DoubleArray): Matrix {
        require(bias.size == cols) { "bias size ${bias.size} != cols $cols" }
        val out = Matrix(rows, cols)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out.data[r * cols + c] = data[r * cols + c] + bias[c]
            }
        }
        return out
    }

    fun softmaxRowWise(): Matrix {
        val out = Matrix(rows, cols)
        for (r in 0 until rows) {
            var max = Double.NEGATIVE_INFINITY
            for (c in 0 until cols) if (data[r * cols + c] > max) max = data[r * cols + c]
            var sum = 0.0
            for (c in 0 until cols) {
                val e = exp(data[r * cols + c] - max)
                out.data[r * cols + c] = e
                sum += e
            }
            if (sum > 0) for (c in 0 until cols) out.data[r * cols + c] /= sum
        }
        return out
    }

    companion object {
        /** Xavier/Glorot init for tanh/sigmoid layers. */
        fun xavier(rows: Int, cols: Int, rng: Random): Matrix {
            val m = Matrix(rows, cols)
            val std = sqrt(2.0 / (rows + cols))
            for (i in m.data.indices) m.data[i] = rng.nextGaussian() * std
            return m
        }

        /** He init for ReLU layers. */
        fun he(rows: Int, cols: Int, rng: Random): Matrix {
            val m = Matrix(rows, cols)
            val std = sqrt(2.0 / cols)
            for (i in m.data.indices) m.data[i] = rng.nextGaussian() * std
            return m
        }

        fun zeros(rows: Int, cols: Int): Matrix = Matrix(rows, cols)
    }
}

private fun Random.nextGaussian(): Double {
    // Box-Muller
    var u = 0.0; var v = 0.0
    while (u == 0.0) u = nextDouble()
    while (v == 0.0) v = nextDouble()
    return sqrt(-2.0 * kotlin.math.ln(u)) * cos(2 * Math.PI * v)
}

private fun cos(x: Double): Double = kotlin.math.cos(x)
