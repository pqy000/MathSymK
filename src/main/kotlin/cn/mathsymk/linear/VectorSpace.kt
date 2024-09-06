package cn.mathsymk.linear

import cn.mathsymk.structure.FiniteDimLinearSpace
import cn.mathsymk.structure.FiniteLinearBasis

interface VectorBasis<K> : FiniteLinearBasis<K, Vector<K>> {
    val vectorLength: Int
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
    override fun contains(x: Vector<K>): Boolean

    /**
     * Gets the coefficients of the vector in the basis of this vector space.
     * Throws [IllegalArgumentException] if the vector is not in this vector space.
     * @see contains
     */
    override fun coefficients(v: Vector<K>): List<K>

    override val basis: VectorBasis<K>
}
