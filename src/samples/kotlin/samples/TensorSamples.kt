package cn.mathsymk.samples

import io.github.ezrnest.linear.*
import io.github.ezrnest.linear.get
import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.util.IterUtils


object TensorSamples {

    fun tensorDot() {
        val ℤ = NumberModels.integers()
        val a = Tensor.of(intArrayOf(3, 4, 5), ℤ, 0 until 60)
        val b = Tensor.of(intArrayOf(4, 3, 2), ℤ, 0 until 24)
        // The following three ways give the same result:
        val res0 = Tensor.zeros(ℤ, 5, 2)
        for ((i, j, k, n) in IterUtils.prodIdxN(intArrayOf(5, 2, 3, 4))) {
            res0[i, j] += a[k, n, i] * b[n, k, j] // direct computation
        }
        println(res0)
        val res1 = a.permute(2, 1, 0).matmul(b, 2) // matmul at the last 2 axes
        println(res1) // should be equal to res0

        val res2 = Tensor.einsum("kni,nkj->ij", a, b) // einsum
        println(res2) // also should be equal to res0

    }

    fun slice() {
        val ℤ = NumberModels.integers()
        val a = Tensor.of(intArrayOf(2, 3, 2, 5), ℤ, 0 until 60)
        println("a is a 4D tensor with shape (2, 3, 2, 5)")
        print("a[0, 1, 2] = ") // a scalar
        println(a[0, 1, 1, 2]) // a scalar
        println("a[0, 1..2, null, 1..2 step 2] = ") // a vector
        a[0,1,1,2]
        println(a[-1, 1..2, null, 0..<5 step 2]) // overloaded version of a.slice
//        val s = a.slice(0, 1..2, null, 0..<5 step 2)
        println()
        println("a[1, ..., 1, NEW_AXIS, 2] = ") // a vector
        println(a[1, Tensor.DOTS, 1, Tensor.NEW_AXIS, 2])
    }
}

fun main() {
    TensorSamples.slice()
}