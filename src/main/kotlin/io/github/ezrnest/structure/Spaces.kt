package io.github.ezrnest.structure

/**
 * A normed space is a vector space equipped with a norm function satisfying:
 * 1. Non-negativity: `norm(v) >= 0` and `norm(v) = 0` if and only if `v = 0`;
 * 2. Homogeneity: `norm(k * v) = |k| * norm(v)` for any scalar `k`;
 * 3. Triangle inequality: `norm(u + v) <= norm(u) + norm(v)`.
 *
 */
interface NormedSpace<T, V> : LinearSpace<T, V> {
    /**
     * Returns the norm of the vector.
     */
    fun norm(v: V): T
}

interface InnerProductSpace<T, V> : LinearSpace<T, V> {
    /**
     * Returns the inner product of the two vectors.
     */
    fun inner(u: V, v: V): T
}

/**
 * A metric space is composed of a set **M** and a function d: **M** × **M** -> **R**,
 * where the function d satisfies:
 * 1. Non-negative:
 *      `d(x,y) >= 0` and `d(x,y) = 0` if and only if `x = y`
 * 2. Symmetry:
 *      `d(x,y) = d(y,x)`
 * 3. Triangle inequality:
 *     `d(x,y) + d(y,z) >= d(x,z)`
 *
 *
 * See : [Metric space](https://en.wikipedia.org/wiki/Metric_space)
 *
 *
 * Created at 2018/11/29 16:17
 * @author  liyicheng
 */
interface MetricSpace<T, V> {
    /**
     * Returns the distance between the two vectors.
     */
    fun distance(u: V, v: V): T
}

///**
// * Describes a finite dimensional linear basis of type [V] over a field [K].
// */
//interface FiniteLinearBasis<K, V> {
//    /**
//     * The elements of this basis.
//     */
//    val elements: List<V>
//
//    /**
//     * The rank of this basis, which is equal to the number of elements.
//     */
//    val rank: Int
//        get() = elements.size
//
//    /**
//     * Returns the coefficients of the given vector under this basis.
//     */
//    fun reduce(v : V) : List<K>
//
//    /**
//     * Returns the vector represented by the given coefficients.
//     */
//    fun produce(coefficients: List<K>): V
//
//    /**
//     * Determines whether the given vector is in the span of this basis.
//     */
//    fun contains(v: V): Boolean
//}

/**
 * Describes a linear space of finite dimension on a field [K] with vectors of type [V].
 *
 * An instance of linear space is associated with a fixed [basis], which is a list of vectors.
 */
interface FiniteDimLinearSpace<K, V> : LinearSpace<K, V> {
//Created by lyc at 2020-03-07 17:50
//Rewritten at 2024/9/21
    /**
     * Gets the dimension of this linear space.
     */
    val dim: Int
        get() = basis.size

    /**
     * Gets the basis of this linear space.
     */
    val basis: List<V>

    /**
     * Gets the coefficients of the given vector under the [basis] of this linear space.
     */
    fun coefficients(v: V): List<K>

    /**
     * Returns the vector represented by the given coefficients under the [basis].
     */
    fun produce(coefficients: List<K>): V {
        require(coefficients.size == dim) { "The number of coefficients must be equal to the dimension of the space." }
        return sumWeighted(coefficients, basis)
    }

}

/**
 * Describes a finite dimensional affine space on a field [K] with vectors of type [V].
 *
 * An affine space is a vector space with an origin.
 */
interface FiniteDimAffineSpace<K, V> {
    val origin: V

    val space: FiniteDimLinearSpace<K, V>

    val dim: Int
        get() = space.dim
}