package cn.mathsymk.samples

import io.github.ezrnest.model.Fraction
import io.github.ezrnest.model.Models
import io.github.ezrnest.model.RingFrac


object NumberModelSamples {
    fun variousFractions(){
        val Z = Models.bigIntegers()
        val Q = RingFrac.over(Z)
        with(Q) {
            val a = frac(1.toBigInteger(), 2.toBigInteger())
            val b = frac(1.toBigInteger(), 3.toBigInteger())
            println(a + b) // 5/6
            println(a * b) // 1/6
        }
    }

    fun fractionSample2(){
        val a = Fraction(1, 2)
    }


}


fun main() {
    NumberModelSamples.variousFractions()
}