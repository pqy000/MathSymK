package model

import cn.mathsymk.model.struct.AlgebraModel
import cn.mathsymk.model.struct.EuclidRingNumberModel
import cn.mathsymk.structure.EuclideanDomain
import cn.mathsymk.structure.Field
import cn.mathsymk.structure.Ring
import cn.mathsymk.structure.UnitRing

class Polynomial<T : Any> internal constructor(
    val model: Ring<T>, val coef: List<T>
) : AlgebraModel<T, Polynomial<T>>, EuclidRingNumberModel<Polynomial<T>> {

    /**
     * The degree of this polynomial, which is the highest power of `x` in this polynomial.
     * If this polynomial is zero, then the degree is `-1`.
     */
    val degree: Int
        get() = coef.size - 1

    init {
    }



    operator fun get(index: Int): T {
        return coef[index]
    }

    override fun isZero(): Boolean {
        return degree == 0 && model.isZero(coef[0])
    }

    override operator fun plus(y: Polynomial<T>): Polynomial<T> {
        return Polynomial(model, addList(model, coef, y.coef))
    }

    override operator fun times(y: Polynomial<T>): Polynomial<T> {
        return Polynomial(model, multiplyList(model, coef, y.coef))
    }

    override fun times(k: T): Polynomial<T> {
        if (model.isZero(k)) {
            return Polynomial(model, emptyList())
        }
        return Polynomial(model, coef.map { model.multiply(it, k) })
    }

    override fun div(k: T): Polynomial<T> {
        if (model.isZero(k)) {
            throw ArithmeticException("Division by zero")
        }
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        return Polynomial(model, coef.map { model.exactDivide(it, k) })
    }

    override fun unaryMinus(): Polynomial<T> {
        return Polynomial(model, coef.map { model.negate(it) })
    }

    override fun isUnit(): Boolean {
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        return degree == 0 && model.isUnit(coef[0])
    }

    override fun gcdUV(y: Polynomial<T>): Triple<Polynomial<T>, Polynomial<T>, Polynomial<T>> {
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        return EuclideanDomain.gcdUV(this, y, zero(model), one(model))
    }

    override fun isCoprime(y: Polynomial<T>): Boolean {
        TODO("Not yet implemented")
    }


    /**
     * Returns the result `(q, r)` of dividing `this` by `y` and the remainder.
     * It is guaranteed that `this = q * y + r` and `r.degree < y.degree`.
     *
     * It is required the [model] is a field.
     */
    override fun divideAndRemainder(y: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
        if (y.isZero()) {
            throw ArithmeticException("Division by zero")
        }
        require(model is Field<T>) { "The model is not a field." }
        TODO()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0..degree) {
            val c = coef[i]
            if (c != model.zero) {
                if (sb.isNotEmpty()) {
                    sb.append(" + ")
                }
                sb.append(c)
                if (i > 0) {
                    sb.append("x")
                    if (i > 1) {
                        sb.append("^$i")
                    }
                }
            }
        }
        return sb.toString()
    }

    companion object {
        private fun <T : Any> trimZero(model: Ring<T>, coef: MutableList<T>): MutableList<T> {
            while (coef.isNotEmpty() && model.isZero(coef.last())) {
                coef.removeLast()
            }
            return coef
        }


        private fun <T : Any> addList(model: Ring<T>, a: List<T>, b: List<T>): List<T> {
            val size = maxOf(a.size, b.size)
            val result = ArrayList<T>(size)
            for (i in 0 until minOf(a.size, b.size)) {
                result.add(model.add(a[i], b[i]))
            }
            if (a.size > size) {
                result.addAll(a.subList(size, a.size))
            } else if (b.size > size) {
                result.addAll(b.subList(size, b.size))
            }
            return trimZero(model, result)
        }

        private fun <T : Any> multiplyList(model: Ring<T>, a: List<T>, b: List<T>): List<T> {
            if (a.isEmpty() || b.isEmpty()) {
                return emptyList()
            }
            val result = MutableList(a.size + b.size - 1) { model.zero }
            for (i in a.indices) {
                for (j in b.indices) {
                    result[i + j] = model.add(result[i + j], model.multiply(a[i], b[j]))
                }
            }
            return trimZero(model, result)
        }

        private fun <T : Any> sumList(model: Ring<T>, values: List<List<T>>): List<T> {
            if (values.isEmpty()) {
                return emptyList()
            }
            val size = values.maxOf { it.size }
            val result = MutableList(size) { model.zero }
            for (list in values) {
                for (i in list.indices) {
                    result[i] = model.add(result[i], list[i])
                }
            }
            return trimZero(model, result)
        }

        fun <T:Any> zero(model: Ring<T>): Polynomial<T>{
            return Polynomial(model, emptyList())
        }

        fun <T:Any> constant(model: Ring<T>, c: T): Polynomial<T>{
            if(model.isZero(c)){
                return zero(model)
            }
            return Polynomial(model, listOf(c))
        }

        fun <T:Any> one(model: UnitRing<T>): Polynomial<T>{
            return constant(model, model.one)
        }

        fun <T:Any> x(model: UnitRing<T>): Polynomial<T>{
            return Polynomial(model, listOf(model.zero, model.one))
        }


        fun <T:Any> fromList(model: Ring<T>, coef: List<T>): Polynomial<T>{
            val newCoef = trimZero(model, coef.toMutableList())
            return Polynomial(model, newCoef)
        }
    }
}