package io.github.ezrnest.linear

import io.github.ezrnest.AbstractMathObject
import io.github.ezrnest.ModeledMathObject
import io.github.ezrnest.ValueEquatable
import io.github.ezrnest.structure.*
import java.util.function.Function

/**
 *
 */
interface VectorBasis<K> : FiniteLinearBasis<K, Vector<K>> {
    val vectorLength: Int

    companion object {
        fun <K> standardBasis(vectorLength: Int, scalars: Field<K>): VectorBasis<K> {
            return StandardVectorBasis(vectorLength, scalars)
        }

        fun <K> zero(vectorLength: Int, scalars: Field<K>): VectorBasis<K> {
            return ZeroVectorBasis(scalars, vectorLength)
        }
    }
}

interface VectorSpace<K> : FiniteDimLinearSpace<K, Vector<K>> {
    /*
    Rewritten at 2024/8/25
     */

    /**
     * Returns the length of the vector in this vector space.
     *
     * Note: This is the length of the vector, not the dimension of the vector space.
     */
    val vectorLength: Int

    /**
     * Determines whether the given vector is in this vector space.
     *
     */
    override fun contains(x: Vector<K>): Boolean {
        return x.size == vectorLength && basis.contains(x)
    }

    /**
     * Gets the coefficients of the vector in the basis of this vector space.
     * Throws [IllegalArgumentException] if the vector is not in this vector space.
     * @see contains
     */
    override fun coefficients(v: Vector<K>): List<K> {
        return basis.reduce(v)
    }

    override val basis: VectorBasis<K>
}


open class CanonicalVectorSpace<K>(override val vectorLength: Int, override val scalars: Field<K>) :
    VectorSpace<K>, InnerProductSpace<K, Vector<K>> {

    /**
     * Creates a new vector with the given [data].
     */
    fun vec(vararg data: K): Vector<K> {
        require(data.size == vectorLength)
        return Vector.of(data.asList(), scalars)
    }


    override fun contains(x: Vector<K>): Boolean {
        return x.model == scalars && x.size == vectorLength && x.elementSequence().all { scalars.contains(it) }
    }

    override fun isEqual(x: Vector<K>, y: Vector<K>): Boolean {
        return VectorImpl.isEqual(x, y, scalars)
    }

    override val zero: Vector<K>
        get() = VectorImpl.zero(vectorLength, scalars)

    override fun negate(x: Vector<K>): Vector<K> {
        return VectorImpl.negate(x, scalars)
    }

    override fun scalarMul(k: K, v: Vector<K>): Vector<K> {
        return VectorImpl.multiply(v, k, scalars)
    }

    override fun add(x: Vector<K>, y: Vector<K>): Vector<K> {
        return VectorImpl.add(x, y, scalars)
    }

    override fun subtract(x: Vector<K>, y: Vector<K>): Vector<K> {
        return VectorImpl.subtract(x, y, scalars)
    }

    override fun sum(elements: List<Vector<K>>): Vector<K> {
        return VectorImpl.sum(elements, vectorLength, scalars)
    }

    override fun inner(u: Vector<K>, v: Vector<K>): K {
        return VectorImpl.inner(u, v, scalars)
    }


    override val basis: VectorBasis<K>
        get() = StandardVectorBasis(vectorLength, scalars)


    override fun coefficients(v: Vector<K>): List<K> {
        return v.toList()
    }
}

/**
 * A standard basis for a vector space, which is the set of unit vectors.
 */
class StandardVectorBasis<K>(
    override val vectorLength: Int,
    private val scalars: Field<K>
) : VectorBasis<K> {
    override val elements: List<Vector<K>>
        get() = (0 until vectorLength).map { i -> Vector.unitVector(vectorLength, i, scalars) }

    override fun reduce(v: Vector<K>): List<K> {

        return v.toList()
    }

    override fun produce(coefficients: List<K>): Vector<K> {
        return Vector.of(coefficients, scalars)
    }

    override fun contains(v: Vector<K>): Boolean {
        return v.model == scalars && v.size == vectorLength
    }
}

internal class DVectorBasis<T>(
    mc: Field<T>,
    override val vectorLength: Int, override val elements: List<Vector<T>>
) :
    AbstractMathObject<T, Field<T>>(mc),
    VectorBasis<T> {

    override fun valueEquals(obj: ValueEquatable<T>): Boolean {
        return obj is DVectorBasis && obj.vectorLength == vectorLength &&
                elements.zip(obj.elements).all { (a, b: Vector<T>) -> a.valueEquals(b) }
    }

    override fun <S> mapTo(newModel: EqualPredicate<S>, mapping: Function<T, S>): DVectorBasis<S> {
        return DVectorBasis(newModel as Field<S>, vectorLength, elements.map { it.mapTo(newModel, mapping) })
    }

    override fun reduce(v: Vector<T>): List<T> {
        TODO("Not yet implemented")
    }

    override fun produce(coefficients: List<T>): Vector<T> {
        TODO("Not yet implemented")
    }

    override fun contains(v: Vector<T>): Boolean {
        TODO("Not yet implemented")
    }
}


class ZeroVectorBasis<T> internal constructor(model: Field<T>, dimension: Int) :
    AbstractMathObject<T, Field<T>>(model),
    VectorBasis<T> {
    override val rank: Int
        get() = 0
    override val vectorLength: Int = dimension
    override val elements: List<Vector<T>>
        get() = emptyList()

    override fun reduce(v: Vector<T>): List<T> {
        throw ArithmeticException("$v cannot be reduced in the zero basis.")
    }

    override fun produce(coefficients: List<T>): Vector<T> {
        return Vector.zero(vectorLength, model)
    }

    override fun contains(v: Vector<T>): Boolean {
        return false
    }

    override fun <S> mapTo(newModel: EqualPredicate<S>, mapping: Function<T, S>): ZeroVectorBasis<S> {
        return ZeroVectorBasis(newModel as Field<S>, vectorLength)
    }

    override fun valueEquals(obj: ValueEquatable<T>): Boolean {
        return obj is ZeroVectorBasis<*> && obj.vectorLength == vectorLength
    }
}