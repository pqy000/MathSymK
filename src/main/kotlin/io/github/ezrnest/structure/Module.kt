package io.github.ezrnest.structure

/**
 * A module over a ring is a generalization of the notion of vector space over a field.
 * It consists of an Abelian group `(V, +)` and a ring `(R, +, *)` with a scalar (left) multiplication `*: R x V -> V`,
 * satisfying the following properties:
 * 1. For all `r1, r2 in R` and `v in V`, `(r1 + r2) * v = r1 * v + r2 * v` (distributive over the ring addition)
 * 2. For all `r in R` and `v1, v2 in V`, `r * (v1 + v2) = r * v1 + r * v2` (distributive over the module addition)
 * 3. For all `r1, r2 in R` and `v in V`, `(r1 * r2) * v = r1 * (r2 * v)` (associative)
 * 4. For all `v in V`, `1 * v = v`, `0 * v = 0` (identity)
 *
 *
 *
 * See [Module](https://en.wikipedia.org/wiki/Module_(mathematics)) for more introduction.
 *
 * Created at 2018/9/20 19:22
 * @author  liyicheng
 * @param R the ring of scalars
 * @param V the module
 */
interface Module<R, V> : AddGroup<V> {
    /*
    Rewritten at 2024/8/25 19:12
     */

    /**
     * The ring of scalars of this module.
     */
    val scalars: Ring<R>


    /**
     * Returns the result of the scalar multiplication of `k * v`.
     */
    fun scalarMul(k: R, v: V): V


//    fun rAdd(r1: R, r2: R): R {
//        return scalars.add(r1, r2)
//    }
//
//    fun rSubtract(r1: R, r2: R): R {
//        return scalars.subtract(r1, r2)
//    }
//
//    fun rNegate(r: R): R {
//        return scalars.negate(r)
//    }
//
//    fun rMultiply(r1: R, r2: R): R {
//        return scalars.multiply(r1, r2)
//    }
//    val rZero: R
//        get() = scalars.zero

//    not possible to define this function because of JVM overloading rules
//    operator fun R.times(v: V): V {
//        return scalarMul(this, v)
//    }

    fun sumWeighted(weights : List<R>, elements :List<V>): V {
        require(weights.size == elements.size)
        return weights.indices.fold(zero) { acc, i -> add(acc, scalarMul(weights[i], elements[i]) ) }
    }
}


interface RightModule<R, V> : AddGroup<V>{
    val scalars: Ring<R>

    /**
     * Returns the result of the scalar (right) multiplication of `v * k`.
     */
    fun scalarMulR(v: V, k: R): V

    fun sumWeightedR(weights : List<R>, elements :List<V>): V {
        require(weights.size == elements.size)
        return weights.indices.fold(zero) { acc, i -> add(acc, scalarMulR(elements[i], weights[i]) ) }
    }
}

interface RingModule<R, V> : Module<R, V>, Ring<V> {
    /*
    Rewritten at 2024/8/25 19:12
     */

}

interface UnitRingModule<R, V> : RingModule<R, V>, UnitRing<V> {
    /*
    Rewritten at 2024/8/25 19:12
     */
    fun fromScalar(r: R): V {
        return scalarMul(r, one)
    }
}

/**
 * A linear space is a module over a field.
 *
 * @author  liyicheng Created at 2018/11/29 16:57
 */
interface LinearSpace<K , V> : Module<K, V> {
    /*
    Rewritten at 2024/8/25 19:12
    Created by liyicheng at 2020-03-07 10:42
     */

    override val scalars: Field<K>


    /**
     * Returns the result of the scalar division of `x / k = (k^-1) * x`.
     */
    fun scalarDiv(x: V, k: K): V {
        return scalarMul(scalars.reciprocal(k), x)
    }

    // not possible to define this function because of JVM overloading rules
//    operator fun V.div(k: K): V {
//        return scalarDiv(this, k)
//    }

    /**
     * Determines whether the two vectors are linear dependent.
     */
    fun isLinearDependent(u: V, v: V): Boolean {
        throw UnsupportedOperationException()
    }


    /**
     * Determines whether the given vectors are linear dependent.
     */
    fun isLinearDependent(vs: List<V>): Boolean {
        throw UnsupportedOperationException()
    }
}

/**
 * An `Algebra` is a linear space where the vectors also form a ring.
 *
 *
 * Created at 2018/11/29 18:46
 * @author  liyicheng
 */
interface Algebra<K,V> : LinearSpace<K,V>, RingModule<K,V> {
    /*
    Rewritten at 2024/8/25
     */

}

/**
 * Describes an algebra which is also a unit ring.
 *
 *
 * @author  liyicheng, 2024/9/11
 */
interface UnitAlgebra<K,V> : Algebra<K,V>, UnitRingModule<K,V> {
    /*
    Created on 2024/9/11
     */

}

interface DivisionAlgebra<K,V> : UnitAlgebra<K,V>, DivisionRing<V> {
    /*
    Created on 2024/9/28
     */
}