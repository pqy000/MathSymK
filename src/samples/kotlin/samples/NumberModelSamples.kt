package cn.mathsymk.samples

import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.model.RFraction

fun variousFractions(){
    val Z = NumberModels.bigIntegers()
    val Q = RFraction.over(Z)
    with(Q) {
        val a = frac(1.toBigInteger(), 2.toBigInteger())
        val b = frac(1.toBigInteger(), 3.toBigInteger())
        println(a + b) // 5/6
        println(a * b) // 1/6
    }
}