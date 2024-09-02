package cn.mathsymk.model

import cn.mathsymk.structure.Algebra
import cn.mathsymk.structure.DivisionRing
import cn.mathsymk.structure.Field
import cn.mathsymk.structure.eval


/**
 * Describes a quaternion.
 */
@JvmRecord
data class Quaternion<T>(val a: T, val b: T, val c: T, val d: T) {

    override fun toString(): String {
        return "($a) + ($b)i + ($c)j + ($d)k"
    }

    fun <N> mapTo(mapper: (T) -> N): Quaternion<N> {
        return Quaternion(mapper(a), mapper(b), mapper(c), mapper(d))
    }

    companion object {
        fun <T : Any> from(model: Field<T>): QuaternionNumbers<T> {
            return QuaternionNumbers(model)
        }
    }
}

class QuaternionNumbers<T : Any>(val model: Field<T>) : DivisionRing<Quaternion<T>>, Algebra<T, Quaternion<T>> {
    override val isCommutative: Boolean
        get() = false

    override val zero: Quaternion<T> = Quaternion(model.zero, model.zero, model.zero, model.zero)
    override val one: Quaternion<T> = Quaternion(model.one, model.zero, model.zero, model.zero)

    override fun contains(x: Quaternion<T>): Boolean {
        return model.contains(x.a) && model.contains(x.b) && model.contains(x.c) && model.contains(x.d)
    }

    override fun isEqual(x: Quaternion<T>, y: Quaternion<T>): Boolean {
        model.eval {
            return isEqual(x.a, y.a) && isEqual(x.b, y.b) && isEqual(x.c, y.c) && isEqual(x.d, y.d)
        }
    }

    override fun isZero(x: Quaternion<T>): Boolean {
        return model.eval {
            isZero(x.a) && isZero(x.b) && isZero(x.c) && isZero(x.d)
        }
    }

    fun quat(a: T, b: T, c: T, d: T): Quaternion<T> {
        return Quaternion(a, b, c, d)
    }

    fun real(a: T): Quaternion<T> {
        return Quaternion(a, model.zero, model.zero, model.zero)
    }

    fun baseI(b: T): Quaternion<T> {
        return Quaternion(model.zero, b, model.zero, model.zero)
    }

    fun baseJ(c: T): Quaternion<T> {
        return Quaternion(model.zero, model.zero, c, model.zero)
    }

    fun baseK(d: T): Quaternion<T> {
        return Quaternion(model.zero, model.zero, model.zero, d)
    }


    inline val T.q: Quaternion<T>
        get() = real(this)

    inline val T.i: Quaternion<T>
        get() = baseI(this)

    inline val T.j: Quaternion<T>
        get() = baseJ(this)

    inline val T.k: Quaternion<T>
        get() = baseK(this)


    val Quaternion<T>.conj: Quaternion<T>
        get() = model.eval { Quaternion(a, -b, -c, -d) }

    val Quaternion<T>.tensor: T
        get() = model.eval { a * a + b * b + c * c + d * d }


    override fun add(x: Quaternion<T>, y: Quaternion<T>): Quaternion<T> {
        model.eval {
            return Quaternion(x.a + y.a, x.b + y.b, x.c + y.c, x.d + y.d)
        }
    }

    override fun negate(x: Quaternion<T>): Quaternion<T> {
        model.eval {
            return Quaternion(-x.a, -x.b, -x.c, -x.d)
        }
    }

    override fun subtract(x: Quaternion<T>, y: Quaternion<T>): Quaternion<T> {
        model.eval {
            return Quaternion(x.a - y.a, x.b - y.b, x.c - y.c, x.d - y.d)
        }
    }


    override fun multiply(x: Quaternion<T>, y: Quaternion<T>): Quaternion<T> {
        model.eval {
            val na = x.a * y.a - x.b * y.b - x.c * y.c - x.d * y.d
            val nb = x.b * y.a + x.a * y.b + x.c * y.d - x.d * y.c
            val nc = x.c * y.a + x.a * y.c - x.b * y.d + x.d * y.b
            val nd = x.d * y.a + x.a * y.d + x.b * y.c - x.c * y.b
            return Quaternion(na, nb, nc, nd)
        }
    }


    override val scalars: Field<T>
        get() = model

    override fun scalarMul(k: T, v: Quaternion<T>): Quaternion<T> {
        model.eval {
            return Quaternion(k * v.a, k * v.b, k * v.c, k * v.d)
        }
    }


    operator fun T.times(q: Quaternion<T>): Quaternion<T> {
        return scalarMul(this, q)
    }

    operator fun Quaternion<T>.times(k: T): Quaternion<T> {
        return scalarMul(k, this)
    }

    override fun scalarDiv(x: Quaternion<T>, k: T): Quaternion<T> {
        model.eval {
            return Quaternion(x.a / k, x.b / k, x.c / k, x.d / k)
        }
    }

    operator fun Quaternion<T>.div(k: T): Quaternion<T> {
        return scalarDiv(this, k)
    }

    override fun reciprocal(x: Quaternion<T>): Quaternion<T> {
        return x.conj / x.tensor
    }

    operator fun T.div(q: Quaternion<T>): Quaternion<T> {
        return reciprocal(q) * this
    }

    override fun multiplyLong(x: Quaternion<T>, n: Long): Quaternion<T> {
        model.eval {
            return Quaternion(multiplyLong(x.a, n), multiplyLong(x.b, n), multiplyLong(x.c, n), multiplyLong(x.d, n))
        }
    }


}

