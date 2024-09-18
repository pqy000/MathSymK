package cn.mathsymk.samples

import io.github.ezrnest.linear.*
import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.util.IterUtils


object TensorSamples {

    fun tensorDot() {
        val ℤ = NumberModels.integers()
        val a = Tensor.of(intArrayOf(3, 4, 5), ℤ, 0 until 60)
        val b = Tensor.of(intArrayOf(4, 3, 2), ℤ, 0 until 24)
        /*
        The following three ways give the same result:
         */
        val res0 = Tensor.zeros(ℤ, 5, 2)
        for ((i, j, k, n) in IterUtils.prodIdxN(intArrayOf(5, 2, 3, 4))) {
            res0[i, j] += a[k, n, i] * b[n, k, j] // direct computation
        }
        println(res0.joinToString())
        val res1 = a.permute(2, 1, 0).matmul(b, 2) // matmul at the last 2 axes
        println(res1.shapeString) // (5, 2)
        println(res1.joinToString())

        val res2 = Tensor.einsum("kni,nkj->ij", a, b) // einsum
        println(res2.shapeString) // (5, 2)
        println(res2.joinToString())

    }
}

fun main() {
    TensorSamples.tensorDot()
}