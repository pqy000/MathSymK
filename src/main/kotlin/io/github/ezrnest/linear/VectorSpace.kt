package io.github.ezrnest.linear

import io.github.ezrnest.structure.*


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

    fun basisAsMatrix(): Matrix<K> {
        return Matrix.fromColumns(basis, scalars)
    }

    /**
     * Determines whether the given vector is in this vector space.
     *
     */
    override fun contains(x: Vector<K>): Boolean {
        checkVector(x)
        return MatrixImpl.solveLinear(basisAsMatrix(), x, scalars) != null
    }

    /**
     * Gets the coefficients of the vector in the basis of this vector space.
     *
     * Throws [IllegalArgumentException] if the vector is not in this vector space.
     *
     * @see contains
     */
    override fun coefficients(v: Vector<K>): List<K> {
        checkVector(v)
        val mat = Matrix.fromColumns(basis)
        val result =
            MatrixImpl.solveLinear(mat, v, scalars) ?: throw IllegalArgumentException("The vector is not in the space.")
        return result.solution.toList()
    }

    /**
     * Gets the basis of the vector space as a list of vectors.
     */
    override val basis: List<Vector<K>>

    override val scalars: Field<K>

    override val zero: Vector<K>
        get() = VectorImpl.zero(vectorLength, scalars)

    override fun add(x: Vector<K>, y: Vector<K>): Vector<K> {
        checkVector(x)
        checkVector(y)
        return VectorImpl.add(x, y, scalars)
    }

    override fun negate(x: Vector<K>): Vector<K> {
        checkVector(x)
        return VectorImpl.negate(x, scalars)
    }

    override fun scalarMul(k: K, v: Vector<K>): Vector<K> {
        checkVector(v)
        return VectorImpl.multiply(v, k, scalars)
    }

    override fun subtract(x: Vector<K>, y: Vector<K>): Vector<K> {
        checkVector(x)
        checkVector(y)
        return VectorImpl.subtract(x, y, scalars)
    }

    override fun produce(coefficients: List<K>): Vector<K> {
        return VectorImpl.sumWeighted(coefficients, basis, vectorLength, scalars)
    }

    override fun isEqual(x: Vector<K>, y: Vector<K>): Boolean {
        checkVector(x)
        checkVector(y)
        return VectorImpl.isEqual(x, y, scalars)
    }

    companion object {
        fun <K> zero(vectorLength: Int, scalars: Field<K>): VectorSpace<K> {
            return ZeroVectorSpace(vectorLength, scalars)
        }

        fun <K> standard(vectorLength: Int, scalars: Field<K>): StandardVectorSpace<K> {
            return StandardVectorSpace(vectorLength, scalars)
        }

        private fun <K> VectorSpace<K>.checkVector(v: Vector<K>) {
            require(v.size == vectorLength) { "Vector size mismatch: expected $vectorLength, got ${v.size}" }
        }

        /**
         * Creates a vector space from the given non-empty basis.
         *
         * It is the caller's responsibility to ensure that the basis is linearly independent.
         *
         */
        fun <K> fromBasis(basis: List<Vector<K>>): VectorSpace<K> {
            val v1 = basis.first()
            val scalars = v1.model as Field<K>
            val vectorLength = v1.size
            return DVectorSpace(scalars, vectorLength, basis)
        }

        fun <K> fromBasis(basis: List<Vector<K>>, vectorLength: Int, model : Field<K>): VectorSpace<K> {
            if(basis.isEmpty()) return zero(vectorLength, model)
            return DVectorSpace(model, vectorLength, basis)
        }

        /**
         * Creates a vector space spanned by the given non-empty list of vectors.
         *
         */
        fun <K> span(basis: List<Vector<K>>): VectorSpace<K> {
            val v = basis.first()
            val model = v.model as Field<K>
            val vectorLength = v.size
            return MatrixImpl.spanOf(basis, vectorLength, model)
        }
    }
}


/**
 * Describes the canonical `d`-dimensional vector space over the field [K] with the standard basis of unit vectors.
 *
 * The [dim] and [vectorLength] are equal to `d`.
 */
open class StandardVectorSpace<K>(override val dim: Int, override val scalars: Field<K>) :
    VectorSpace<K>, InnerProductSpace<K, Vector<K>> {
    override val vectorLength: Int
        get() = dim

    override val zero: Vector<K>
        get() = VectorImpl.zero(vectorLength, scalars)

    /**
     * Creates a new vector with the given [data].
     */
    fun vec(vararg data: K): Vector<K> {
        return produce(data.asList())
    }

    override fun produce(coefficients: List<K>): Vector<K> {
        require(
            coefficients.size == vectorLength
        ) { "The number of coefficients must be equal to the dimension of the space." }
        return Vector.of(coefficients, scalars)
    }


    override fun contains(x: Vector<K>): Boolean {
        return x.model == scalars && x.size == vectorLength && x.elementSequence().all { scalars.contains(it) }
    }

    override fun isEqual(x: Vector<K>, y: Vector<K>): Boolean {
        return VectorImpl.isEqual(x, y, scalars)
    }


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


    override val basis: List<Vector<K>>
        get() = Vector.unitVectors(vectorLength, scalars)


    override fun coefficients(v: Vector<K>): List<K> {
        return v.toList()
    }
}


/**
 * Describes the zero vector space over the field [K] embedded in the vector space of [vectorLength].
 */
class ZeroVectorSpace<K>(override val vectorLength: Int, override val scalars: Field<K>) :
    VectorSpace<K> {
    override fun contains(x: Vector<K>): Boolean {
        return false
    }

    override val basis: List<Vector<K>>
        get() = emptyList()
}

class DVectorSpace<K> internal constructor(
    override val scalars: Field<K>, override val vectorLength: Int, override val basis: List<Vector<K>>
) : VectorSpace<K>


open class VectorAffineSpace<K>(override val origin: Vector<K>, override val space: VectorSpace<K>) :
    FiniteDimAffineSpace<K, Vector<K>>


class LinearEquationSolution<T>(solution: Vector<T>, nullSpace: VectorSpace<T>) :
    VectorAffineSpace<T>(solution, nullSpace) {
    /**
     * Gets a special solution of the linear equation.
     */
    val solution: Vector<T>
        get() = origin

    val nullSpace: VectorSpace<T>
        get() = space


    val isUnique: Boolean
        get() = nullSpace.dim == 0
}