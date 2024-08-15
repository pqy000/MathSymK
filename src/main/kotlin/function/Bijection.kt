package function

import cn.mathsymk.function.MathFunction
import cn.mathsymk.model.struct.Invertible

interface Bijection<P, R> : MathFunction<P, R>, Invertible<Bijection<R,P>> {
    override fun apply(x: P): R

    fun invert(y: R): P

    override fun inverse(): Bijection<R, P> {
        val f = this
        return object : Bijection<R, P> {
            override fun apply(x: R): P = f.invert(x)
            override fun invert(y: P): R = f.apply(y)
            override fun inverse(): Bijection<P, R> = f
        }
    }
}

interface BijectiveOperator<T> : Bijection<T, T>, MathFunction<T, T> {
    override fun apply(x: T): T
    override fun invert(y: T): T
    override fun inverse(): BijectiveOperator<T>
}
