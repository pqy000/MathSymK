package cn.mathsymk.function

import java.util.function.BiFunction


/**
 *
 *
 */
interface BiMathFunction<P1, P2, R> {


    fun apply(x: P1, y: P2): R
}


interface BiMathOperator<T> : BiMathFunction<T,T,T> {

    override fun apply(x: T, y: T): T
}