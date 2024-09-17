package io.github.ezrnest.model

import io.github.ezrnest.structure.Algebra
import io.github.ezrnest.structure.DivisionRing
import io.github.ezrnest.structure.Field
import io.github.ezrnest.structure.eval


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

        /**
         * Creates the quaternion numbers from the given field
         */
        fun <T> from(field: Field<T>): QuaternionNumbers<T> {
            return QuaternionNumbers(field)
        }
    }
}

class QuaternionNumbers<T>(val field: Field<T>) : DivisionRing<Quaternion<T>>, Algebra<T, Quaternion<T>> {
    /**
     * The quaternion numbers are not commutative.
     */
    override val isCommutative: Boolean
        get() = false

    override val zero: Quaternion<T> = Quaternion(field.zero, field.zero, field.zero, field.zero)
    override val one: Quaternion<T> = Quaternion(field.one, field.zero, field.zero, field.zero)

    override fun contains(x: Quaternion<T>): Boolean {
        return field.contains(x.a) && field.contains(x.b) && field.contains(x.c) && field.contains(x.d)
    }

    override fun isEqual(x: Quaternion<T>, y: Quaternion<T>): Boolean {
        field.eval {
            return isEqual(x.a, y.a) && isEqual(x.b, y.b) && isEqual(x.c, y.c) && isEqual(x.d, y.d)
        }
    }

    override fun isZero(x: Quaternion<T>): Boolean {
        return field.eval {
            isZero(x.a) && isZero(x.b) && isZero(x.c) && isZero(x.d)
        }
    }

    /**
     * Creates a quaternion from the given components.
     */
    fun quat(a: T, b: T, c: T, d: T): Quaternion<T> {
        return Quaternion(a, b, c, d)
    }


    /**
     * Creates a real quaternion.
     */
    fun real(a: T): Quaternion<T> {
        return Quaternion(a, field.zero, field.zero, field.zero)
    }

    fun baseI(b: T): Quaternion<T> {
        return Quaternion(field.zero, b, field.zero, field.zero)
    }

    fun baseJ(c: T): Quaternion<T> {
        return Quaternion(field.zero, field.zero, c, field.zero)
    }

    fun baseK(d: T): Quaternion<T> {
        return Quaternion(field.zero, field.zero, field.zero, d)
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
        get() = field.eval { Quaternion(a, -b, -c, -d) }

    val Quaternion<T>.tensor: T
        get() = field.eval { a * a + b * b + c * c + d * d }


    override fun add(x: Quaternion<T>, y: Quaternion<T>): Quaternion<T> {
        field.eval {
            return Quaternion(x.a + y.a, x.b + y.b, x.c + y.c, x.d + y.d)
        }
    }

    override fun negate(x: Quaternion<T>): Quaternion<T> {
        field.eval {
            return Quaternion(-x.a, -x.b, -x.c, -x.d)
        }
    }

    override fun subtract(x: Quaternion<T>, y: Quaternion<T>): Quaternion<T> {
        field.eval {
            return Quaternion(x.a - y.a, x.b - y.b, x.c - y.c, x.d - y.d)
        }
    }


    override fun multiply(x: Quaternion<T>, y: Quaternion<T>): Quaternion<T> {
        field.eval {
            val na = x.a * y.a - x.b * y.b - x.c * y.c - x.d * y.d
            val nb = x.b * y.a + x.a * y.b + x.c * y.d - x.d * y.c
            val nc = x.c * y.a + x.a * y.c - x.b * y.d + x.d * y.b
            val nd = x.d * y.a + x.a * y.d + x.b * y.c - x.c * y.b
            return Quaternion(na, nb, nc, nd)
        }
    }


    override val scalars: Field<T>
        get() = this.field

    override fun scalarMul(k: T, v: Quaternion<T>): Quaternion<T> {
        return field.eval { Quaternion(k * v.a, k * v.b, k * v.c, k * v.d) }
    }


    operator fun T.times(q: Quaternion<T>): Quaternion<T> {
        return scalarMul(this, q)
    }

    operator fun Quaternion<T>.times(k: T): Quaternion<T> {
        return scalarMul(k, this)
    }

    override fun scalarDiv(x: Quaternion<T>, k: T): Quaternion<T> {
        field.eval {
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
        field.eval {
            return Quaternion(multiplyLong(x.a, n), multiplyLong(x.b, n), multiplyLong(x.c, n), multiplyLong(x.d, n))
        }
    }


}

