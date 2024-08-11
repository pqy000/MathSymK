package cn.mathsymk.model

import cn.mathsymk.structure.AddGroup
import cn.mathsymk.structure.Field
import cn.mathsymk.structure.Ring
import util.ModelPatterns


interface AddMonoidModel<T : AddMonoidModel<T>> {
    operator fun plus(y: T): T


    /**
     * 'multiply' this with the given long, which is the result of summing `this`
     * for `k` times.
     */
    operator fun times(n: Long): T {
        @Suppress("UNCHECKED_CAST")
        val x = this as T
        return ModelPatterns.binaryProduce(n, x) { a, b -> a + b }
    }
}

interface MulMonoidModel<T : MulMonoidModel<T>> {

    operator fun times(y: T): T


    /**
     * Returns the `n`-th power of `this`.
     */
    infix fun pow(n: Long): T {
        @Suppress("UNCHECKED_CAST")
        val x = this as T
        return ModelPatterns.binaryProduce(n, x) { a, b -> a.times(b) }
    }
}


/**
 * Describes a number model which is suitable for a group. The operations are named as addition group.
 */
interface AddGroupModel<T : AddGroupModel<T>> : AddMonoidModel<T> {
    /**
     * Returns `this + y` as the operation defined in the group.
     */
    override fun plus(y: T): T

    /**
     * Returns the negation of `this`.
     */
    operator fun unaryMinus(): T

    /**
     * Returns `this - y`, which should be equal to `add(negate(y))`.
     */
    operator fun minus(y: T): T {
        return plus(-y)
    }

    override fun times(n: Long): T {
        if (n < 0) {
            return unaryMinus().times(n)
        }
        if (n == 0L) {
            return plus(-this)
        }
        return super.times(n)
    }
}

/**
 * Describes a number model which is suitable for a group where the operation is named as multiplication.
 *
 * While being a group, we allow some elements to be not invertible so the model will be compatible for a ring.
 * In this case, an [ArithmeticException] will be thrown when the reciprocal of a non-invertible element is requested.
 */
interface MulGroupModel<T : MulGroupModel<T>> : MulMonoidModel<T> {
    /**
     * Returns `this * y` as the operation defined in the group.
     */
    override fun times(y: T): T

    /**
     * Determines whether this element is invertible.
     */
    val isInvertible: Boolean

    /**
     * Returns the reciprocal of `this`, that is, the element `e` such that `e * this = this * e = 1`
     */
    operator fun inv(): T


    /**
     * Returns `this - y`, which should be equal to `add(negate(y))`.
     */
    operator fun div(y: T): T = times(y.inv())

    override fun pow(n: Long): T {

        if (n < 0) {
            return inv().pow(n)
        }
        @Suppress("UNCHECKED_CAST")
        val x = this as T
        if (n == 0L) {
            return x.div(x)
        }
        return super.pow(n)
    }
}


/**
 * Describes a number model which is suitable for a ring.
 */
interface RingModel<T : RingModel<T>> : AddGroupModel<T>, MulMonoidModel<T> {

    fun isZero(): Boolean
}

//inline operator fun <T : RingNumberModel<T>> RingNumberModel<T>.times(y: T): T = multiply(y)

/**
 * Describes a number model which is suitable for a division ring.
 *
 */
interface DivisionRingModel<T : DivisionRingModel<T>> : RingModel<T>, MulGroupModel<T> {
    override fun inv(): T
}

//inline operator fun <T : DivisionRingNumberModel<T>> DivisionRingNumberModel<T>.div(y: T): T = divide(y)
/**
 * Describes a number model which is suitable for a field.
 * @see cn.mathsymj.math.algebra.abs.structure.Field
 */
interface FieldModel<T : FieldModel<T>> : DivisionRingModel<T>

/**
 * Describes the number model of a (left) module.
 */
interface ModuleModel<R, V : ModuleModel<R, V>> : AddGroupModel<V> {

    /**
     * Performs the scalar multiplication.
     */
    fun multiply(k: R): V


}

/**
 * Describe the number model for a linear space,
 */
interface VectorModel<K, V : VectorModel<K, V>> : ModuleModel<K, V> {
    /**
     * Performs the scalar multiplication.
     */
    override fun multiply(k: K): V


    /**
     * Performs the scalar division.
     */
    fun divide(k: K): V

    operator fun times(k: K): V = multiply(k)

    operator fun div(k: K): V = divide(k)

    /**
     * Determines whether this is linear relevant to [v].
     *
     * This method is optional.
     */
    fun isLinearRelevant(v: V): Boolean {
        throw UnsupportedOperationException()
    }
}

//inline operator fun <K, V : VectorModel<K, V>> VectorModel<K, V>.times(k: K) = multiply(k)
//inline operator fun <K, V : VectorModel<K, V>> VectorModel<K, V>.div(k: K) = divide(k)
operator fun <K, V : ModuleModel<K, V>> K.times(v: ModuleModel<K, V>) = v.multiply(this)


interface AlgebraModel<K, V : AlgebraModel<K, V>> : VectorModel<K, V>, RingModel<V> {
}

//inline operator fun <K, V : AlgebraModel<K, V>> AlgebraModel<K, V>.times(y: V) = multiply(y)

object NumberModels {
    /**
     * Gets a group calculator on the GroupNumberModel.
     */
    fun <T : AddGroupModel<T>> asGroup(zero: T): AddGroup<T> {
        return object : AddGroup<T> {
            override fun contains(x: T): Boolean {
                return true
            }

            override val zero: T = zero

            override fun add(x: T, y: T): T {
                return x + y
            }

            override fun negate(x: T): T {
                return -x
            }

            override fun subtract(x: T, y: T): T {
                return x - y
            }


            override fun isEqual(x: T, y: T): Boolean {
                return x == y
            }
        }
    }

    fun <T : RingModel<T>> asRing(zero: T) = object : Ring<T> {
        override fun contains(x: T): Boolean {
            return true
        }

        override val zero: T = zero

        override fun add(x: T, y: T): T {
            return x + y
        }

        override fun negate(x: T): T {
            return -x
        }

        override fun subtract(x: T, y: T): T {
            return x - y
        }

        override fun multiply(x: T, y: T): T {
            return x * y
        }

        override fun isEqual(x: T, y: T): Boolean {
            return x == y
        }
    }

    fun <T : FieldModel<T>> asField(zero: T, one: T,characteristic : Long = 0) = object : Field<T> {
        override fun contains(x: T): Boolean {
            return true
        }

        override val zero: T = zero

        override val one: T = one

        override val characteristic: Long = 0

        override fun add(x: T, y: T): T {
            return x + y
        }

        override fun negate(x: T): T {
            return -x
        }

        override fun subtract(x: T, y: T): T {
            return x - y
        }

        override fun multiply(x: T, y: T): T {
            return x * y
        }

        override fun isEqual(x: T, y: T): Boolean {
            return x == y
        }

        override fun divide(x: T, y: T): T {
            return x / y
        }

        override fun reciprocal(x: T): T {
            return x.inv()
        }
    }
}