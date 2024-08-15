package cn.mathsymk.function

import cn.mathsymk.model.struct.Composable


interface MathFunction<in P, out R> {
    fun apply(x: P): R
}


operator fun <P, R> MathFunction<P, R>.invoke(x: P): R = apply(x)

interface MathOperator<T> : MathFunction<T, T> {
    override fun apply(x: T): T
}

interface BiMathFunction<in P1, in P2, out R> {


    fun apply(x: P1, y: P2): R
}

operator fun <P1, P2, R> BiMathFunction<P1, P2, R>.invoke(x: P1, y: P2): R = apply(x, y)


interface BiMathOperator<T> : BiMathFunction<T, T, T> {

    override fun apply(x: T, y: T): T
}