package cn.mathsymk.samples

import io.github.ezrnest.linear.*
import io.github.ezrnest.linear.get
import io.github.ezrnest.model.Fraction
import io.github.ezrnest.model.Multinomial
import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.model.NumberModels.fractions
import io.github.ezrnest.util.IterUtils
import kotlin.system.measureTimeMillis


object TensorSamples {

    fun tensorExample1() {
        with(Tensor.over(fractions())) {
            val a = zeros(2, 3)
            a += ones(3)
            a /= Fraction(3)
            a += ones(2)
            println(a.sumAll())
        }
    }

    fun tensorDot() {
        val ℤ = NumberModels.integers()
        with(Tensor.over(ℤ)) {
            val a = Tensor.of(intArrayOf(3, 4, 5), 0 until 60)
            val b = Tensor.of(intArrayOf(4, 3, 2), 0 until 24)
            // The following three ways give the same result:
            val res0 = Tensor.zeros(ℤ, 5, 2)
            for ((i, j, k, n) in IterUtils.prodIdxNoCopy(intArrayOf(5, 2, 3, 4))) {
                res0[i, j] += a[k, n, i] * b[n, k, j] // direct computation
            }
            println(res0)
            val res1 = a.permute(2, 1, 0).matmul(b, 2) // matmul at the last 2 axes
            println(res1) // should be equal to res0

            val res2 = einsum("kni,nkj->ij", a, b) // einsum
            println(res2) // also should be equal to res0
        }
    }

    fun slice() {
        val ℤ = NumberModels.integers()
        val a = Tensor.of(intArrayOf(2, 3, 2, 5), ℤ, 0 until 60)
        println("a is a 4D tensor with shape (2, 3, 2, 5)")
        print("a[0, 1, 1, 2] = ") // a scalar
        println(a[0, 1, 1, 2]) // a scalar
        println("a[0, 1..2, null, 1..2 step 2] = ") // a vector
        println(a[-1, 1..2, null, 0..<5 step 2]) // overloaded version of a.slice
//        val s = a.slice(0, 1..2, null, 0..<5 step 2)
        println()
        println("a[1, ..., 1, NEW_AXIS, 2] = ") // a vector
        println(a[1, Tensor.DOTS, 1, Tensor.NEW_AXIS, 2])
    }


    fun tensorMap() {
        val a = Tensor(2, 3) { (i, j) -> i + j }
        val b = a.map { it > 0 }
        println(a)
        println(b)

        val c = a.map(Fraction::of)
        with(Tensor.over(fractions())) {
            c /= Fraction(2)
            println(c)
        }
    }

    fun tensorExample2() {
        val ℤ = NumberModels.integers()
        val multiZ = Multinomial.over(ℤ)
        with(multiZ) {
            with(Tensor.over(multiZ)) {
                val a = Tensor(2, 3) { (i, j) -> i * x + j * y }
                println(a)
                println(a.sum(0))
                println(a.sumAll())
                val b = Tensor.like(a) { it.sum() }.map { it * z }
                println(b)
                println(a + b)
            }

        }
    }

    fun tensorTimeCost() {
        // can be improved in the future version with specialized implementation for primitive types including Int and Double
        val ℤ = NumberModels.integers()
        with(Tensor.over(ℤ)) {
            measureTimeMillis {
                val a = zeros(1000, 1000, 100)
                a += 0
            }.also { println("Time cost: $it ms") }

            measureTimeMillis {
                val a = zeros(1000, 1000, 100)
                // can be improved in the future
                a += a
            }.also { println("Time cost: $it ms") }
        }
    }
}

fun main() {
    TensorSamples.tensorExample2()
}