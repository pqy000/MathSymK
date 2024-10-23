package cn.mathsymk.samples

import io.github.ezrnest.mathsymk.linear.Tensor
import io.github.ezrnest.mathsymk.linear.get
import io.github.ezrnest.mathsymk.model.Fraction
import io.github.ezrnest.mathsymk.model.Multinomial
import io.github.ezrnest.mathsymk.model.Models
import io.github.ezrnest.mathsymk.model.Models.fractions
import io.github.ezrnest.mathsymk.model.Models.ints
import io.github.ezrnest.mathsymk.util.IterUtils
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
        with(Tensor.over(Models.ints())) {
            val a = Tensor.ofFlat(intArrayOf(3, 4, 5), 0 until 60)
            val b = Tensor.ofFlat(intArrayOf(4, 3, 2), 0 until 24)
            // The following three ways give the same result:
            val res0 = zeros(5, 2).also { res0 ->
                for ((i, j, k, n) in IterUtils.prodIdxNoCopy(intArrayOf(5, 2, 3, 4))) {
                    res0[i, j] += a[k, n, i] * b[n, k, j] // direct computation
                }
            }
            val res1 = a.permute(2, 1, 0).matmul(b, 2) // matmul at the last 2 axes
            val res2 = einsum("kni,nkj->ij", a, b) // einsum

            println(isEqual(res0, res1) && isEqual(res0, res2)) // should be true
        }
    }

    fun slice() {
        val a = Tensor.ofFlat(intArrayOf(2, 3, 2, 5), 0 until 60)
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
        val ℤ = Models.ints()
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

    fun tensorExample3(){
        with(Tensor.over(ints())) {
            val a = zeros(2,3,5)
            println(a.sum())
        }
    }

    fun tensorTimeCost() {
        // can be improved in the future version with specialized implementation for primitive types including Int and Double
        val ℤ = Models.ints()
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
    TensorSamples.tensorExample3()
}