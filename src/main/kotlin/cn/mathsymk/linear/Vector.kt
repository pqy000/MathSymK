package cn.mathsymk.linear

import cn.mathsymk.IMathObject
import cn.mathsymk.MathObject
import cn.mathsymk.model.struct.GenVector
import cn.mathsymk.model.struct.VectorModel
import cn.mathsymk.model.struct.indices
import cn.mathsymk.structure.*
import java.util.function.Function


/**
 * Describes a vector
 *
 * @author Ezrnest
 */
interface Vector<T : Any> : GenVector<T>, MathObject<T, EqualPredicate<T>>, VectorModel<T, Vector<T>> {
    /*
    Created by Ezrnest at 2024/09/04 15:33
     */

    override fun applyAll(f: (T) -> T): Vector<T> {
        return VectorImpl.apply1(this, model, f)
    }

    override fun valueEquals(obj: IMathObject<T>): Boolean {
        if (obj !is Vector) return false
        if (size != obj.size) return false
        return indices.all { model.isEqual(this[it], obj[it]) }
    }

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Vector<N> {
        return VectorImpl.apply1(this, newCalculator, mapper::apply)
    }


    override val isZero: Boolean
        get() {
            val model = model as AddGroup<T>
            return indices.all { model.isZero(this[it]) }
        }

    override fun plus(y: Vector<T>): Vector<T> {
        return VectorImpl.add(this, y, model as AddSemigroup<T>)
    }

    override fun unaryMinus(): Vector<T> {
        return VectorImpl.negate(this, model as AddGroup<T>)
    }

    override fun times(k: T): Vector<T> {
        return VectorImpl.multiply(this, k, model as MulSemigroup<T>)
    }

    override fun div(k: T): Vector<T> {
        return VectorImpl.divide(this, k, model as MulGroup<T>)
    }

    override fun minus(y: Vector<T>): Vector<T> {
        return VectorImpl.subtract(this, y, model as AddGroup<T>)
    }

    override fun times(n: Long): Vector<T> {
        return VectorImpl.multiplyLong(this, n, model as AddGroup<T>)
    }


    /**
     * Returns the inner (dot) product of this vector and the given vector: `⟨this, v⟩`.
     */
    infix fun inner(v: Vector<T>): T {
        return VectorImpl.inner(this, v, model as Ring<T>)
    }

    /**
     * Returns the Hadamard product of this vector and the given vector: `this ⊙ v`.
     */
    infix fun odot(v: Vector<T>): Vector<T> {
        return VectorImpl.odot(this, v, model as MulSemigroup<T>)
    }

    /**
     * Returns the Euclidean norm of this vector, which is the square root of the sum of squares of all elements.
     */
    fun norm(): T {
        return VectorImpl.norm(this, model as Reals<T>)
    }

    /**
     * Returns the sum of squares of all elements, which is the square of the norm.
     */
    fun normSq(): T {
        return VectorImpl.normSq(this, model as Ring<T>)
    }

//    fun normP()

    fun unitize(): Vector<T> {
        return VectorImpl.unitize(this, model as Reals<T>)
    }


    companion object {
        fun <T : Any> of(data: List<T>, model: EqualPredicate<T>): Vector<T> {
            val arr = Array<Any>(data.size) { k -> data[k] }
            return AVector(arr, model)
        }

        fun <T : Any> of(model: EqualPredicate<T>, vararg data: T): Vector<T> {
            val arr = Array<Any>(data.size) { k -> data[k] }
            return AVector(arr, model)
        }

        inline operator fun <T : Any> invoke(size: Int, model: EqualPredicate<T>, supplier: (Int) -> T): Vector<T> {
            val data = Array<Any>(size) { k -> supplier(k) }
            return AVector(data, model)
        }

    }
}


interface MutableVector<T : Any> : Vector<T> {
    fun set(i: Int, value: T)

    fun copy(): MutableVector<T> {
        return VectorImpl.copyOf(this)
    }
}


data class AVector<T : Any>(
    val data: Array<Any>,
    override val model: EqualPredicate<T>
) : MutableVector<T> {
    init {
        require(data.isNotEmpty())
    }

    override val size: Int
        get() = data.size

    override fun get(i: Int): T {
        @Suppress("UNCHECKED_CAST")
        return data[i] as T
    }

    override fun set(i: Int, value: T) {
        data[i] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun toList(): List<T> {
        return data.map { it as T }
    }

    @Suppress("UNCHECKED_CAST")
    override fun elementSequence(): Sequence<T> {
        return data.asSequence().map { it as T }
    }


    override fun copy(): AVector<T> {
        return AVector(data.copyOf(), model)
    }

    override fun toString(): String {
        return data.joinToString(prefix = "[", postfix = "]")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AVector<*>

        if (!data.contentEquals(other.data)) return false
        if (model != other.model) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + model.hashCode()
        return result
    }

}

object VectorImpl {

    private inline fun <T : Any> apply2(
        x: GenVector<T>, y: GenVector<T>,
        model: EqualPredicate<T>, f: (T, T) -> T
    ): AVector<T> {
        require(x.isSameSize(y))
        val data = Array<Any>(x.size) { k ->
            f(x[k], y[k])
        }
        return AVector(data, model)
    }

    internal inline fun <T : Any> apply1(x: GenVector<T>, model: EqualPredicate<T>, f: (T) -> T): AVector<T> {
        val newData = Array<Any>(x.size) { k -> f(x[k]) }
        return AVector(newData, model)
    }

    internal inline fun <T : Any, N : Any> apply1(x: GenVector<T>, model: EqualPredicate<N>, f: (T) -> N): AVector<N> {
        val newData = Array<Any>(x.size) { k -> f(x[k]) }
        return AVector(newData, model)
    }

    fun <T : Any> constant(size: Int, value: T, model: EqualPredicate<T>): AVector<T> {
        return AVector(Array<Any>(size) { value }, model)
    }

    fun <T : Any> zero(size: Int, model: AddMonoid<T>): AVector<T> {
        return constant(size, model.zero, model)
    }

    fun <T : Any> copyOf(x: Vector<T>): AVector<T> {
        val data = Array<Any>(x.size) { k -> x[k] }
        return AVector(data, x.model)
    }

    fun <T : Any> add(x: GenVector<T>, y: GenVector<T>, model: AddSemigroup<T>): AVector<T> {
        return apply2(x, y, model, model::add)
    }

    fun <T : Any> subtract(x: GenVector<T>, y: GenVector<T>, model: AddGroup<T>): AVector<T> {
        return apply2(x, y, model, model::subtract)
    }

    fun <T : Any> negate(x: GenVector<T>, model: AddGroup<T>): AVector<T> {
        return apply1(x, model, model::negate)
    }

    fun <T : Any> multiply(x: GenVector<T>, k: T, model: MulSemigroup<T>): AVector<T> {
        return apply1(x, model) { model.multiply(k, it) }
    }

    fun <T : Any> multiplyLong(x: GenVector<T>, k: Long, model: AddGroup<T>): AVector<T> {
        return apply1(x, model) { model.multiplyLong(it, k) }
    }

    fun <T : Any> divide(x: GenVector<T>, k: T, model: MulGroup<T>): AVector<T> {
        return apply1(x, model) { model.divide(it, k) }
    }

    fun <T : Any> sum(vs: List<GenVector<T>>, size: Int, cal: AddMonoid<T>): AVector<T> {
        require(vs.all { it.size == size }) { "Size mismatch! " }
        return vs.fold(zero(size, cal)) { acc, v -> add(acc, v, cal) }
    }

    fun <T : Any> inner(x: GenVector<T>, y: GenVector<T>, model: Ring<T>): T {
        require(x.isSameSize(y))
        return x.indices.fold(model.zero) { acc, i -> model.add(acc, model.multiply(x[i], y[i])) }
    }

    fun <T : Any> odot(x: GenVector<T>, y: GenVector<T>, model: MulSemigroup<T>): AVector<T> {
        require(x.isSameSize(y))
        return apply2(x, y, model, model::multiply)
    }


    fun <T : Any> normSq(x: GenVector<T>, model: Ring<T>): T {
        return inner(x, x, model)
    }

    fun <T : Any> norm(x: GenVector<T>, model: Reals<T>): T {
        return model.sqrt(normSq(x, model))
    }

    fun <T : Any> unitize(x: GenVector<T>, model: Reals<T>): AVector<T> {
        val n = norm(x, model)
        return apply1(x, model) { model.divide(it, n) }
    }


}