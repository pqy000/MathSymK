package model

import cn.mathsymk.AbstractMathObject
import cn.mathsymk.IMathObject
import cn.mathsymk.MathObject
import cn.mathsymk.function.MathOperator
import cn.mathsymk.model.struct.AlgebraModel
import cn.mathsymk.model.struct.EuclidRingNumberModel
import cn.mathsymk.structure.*
import java.util.function.Function
import java.util.stream.Collectors

typealias PTerm<T> = IndexedValue<T>

class Polynomial<T : Any> internal constructor(
    model: Ring<T>,
    /**
     * A list of terms of this polynomial.
     */
    val terms: List<PTerm<T>>
) : AbstractMathObject<T, Ring<T>>(model),
    AlgebraModel<T, Polynomial<T>>, EuclidRingNumberModel<Polynomial<T>>, MathOperator<T> {

    /**
     * The degree of this polynomial, which is the highest power of `x` in this polynomial.
     * If this polynomial is zero, then the degree is `-1`.
     */
    val degree: Int
        get() = terms.lastOrNull()?.index ?: -1

    init {

    }

    /**
     * Returns the coefficient of the term with the specified [index].
     */
    operator fun get(index: Int): T {
        return terms.binarySearchBy(index) { it.index }.let {
            if (it >= 0) {
                terms[it].value
            } else {
                model.zero
            }
        }
    }

    /**
     * Returns a list of coefficients of this polynomial.
     * The coefficient of the term with index `i` is at the position `i` in the list.
     */
    fun coefficientList(): List<T> {
        val result = MutableList(degree + 1) { model.zero }
        for ((index, value) in terms) {
            result[index] = value
        }
        return result
    }

    override fun valueEquals(obj: IMathObject<T>): Boolean {
        if (obj !is Polynomial<T>) {
            return false
        }
        if (model != obj.model) {
            return false
        }
        for ((t1, t2) in terms.zip(obj.terms)) {
            if (t1.index != t2.index || model.isEqual(t1.value, t2.value)) {
                return false
            }
        }
        return true
    }

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Polynomial<N> {
        val newModel = newCalculator as Ring<N>
        val newTerms = terms.map { PTerm(it.index, mapper.apply(it.value)) } // possibly zero after mapping
        mergeTerms(newModel, newTerms)
        return Polynomial(newModel, newTerms)
    }

    override fun toString(): String {
        if (isZero()) {
            return "0"
        }
        return terms.reversed().joinToString(" + ") { (index, value) ->
            if (index == 0) {
                value.toString()
            } else if (index == 1) {
                "$value*x"
            } else {
                "$value*x^$index"
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polynomial<*>) return false

        if (model != other.model) return false
        for ((t1, t2) in terms.zip(other.terms)) {
            if (t1.index != t2.index || t1.value != t2.value) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        for (term in terms) {
            result = 31 * result + term.hashCode()
        }
        return result
    }

    /*
    Polynomial methods
     */

    override fun apply(x: T): T {
        if (terms.isEmpty()) {
            return model.zero
        }
        var term = terms.last()
        var power = term.index
        var result = term.value
        var pos = terms.lastIndex
        while (--pos >= 0) {
            term = terms[pos]
            val pDiff = power - term.index
            result = if (pDiff == 1) {
                model.eval { result * x }
            } else {
                model.eval { result * x.pow(pDiff.toLong()) }
            }
            result = model.add(result, term.value)
            power = term.index
        }
        if (power > 0) {
            result = model.eval { result * x.pow(power.toLong()) }
        }
        return result
    }

    override fun isZero(): Boolean {
        return terms.isEmpty()
    }

    override operator fun plus(y: Polynomial<T>): Polynomial<T> {
        return Polynomial(model, addTerms(model, terms, y.terms))
    }

    override operator fun times(y: Polynomial<T>): Polynomial<T> {
        return Polynomial(model, multiplyTerms(model, terms, y.terms))
    }

    private inline fun mapTerms(transform: (T) -> T): Polynomial<T> {
        return Polynomial(model, terms.map { PTerm(it.index, transform(it.value)) })
    }

    override fun times(k: T): Polynomial<T> {
        if (model.isZero(k)) {
            return Polynomial(model, emptyList())
        }
        return mapTerms { model.multiply(it, k) }
    }

    override fun div(k: T): Polynomial<T> {
        if (model.isZero(k)) {
            throw ArithmeticException("Division by zero")
        }
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        return mapTerms { model.exactDivide(it, k) }
    }

    override fun unaryMinus(): Polynomial<T> {
        return mapTerms { model.negate(it) }
    }

    override fun isUnit(): Boolean {
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        return degree == 0 && model.isUnit(terms[0].value)
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




    companion object {
        private fun <T : Any> trimZero(model: Ring<T>, coef: MutableList<T>): MutableList<T> {
            while (coef.isNotEmpty() && model.isZero(coef.last())) {
                coef.removeLast()
            }
            return coef
        }

        private fun <T : Any> mergeTerms(model: Ring<T>, rawTerms: List<PTerm<T>>): List<PTerm<T>> {
            val sortedTerms = rawTerms.sortedBy { it.index }
            // merge terms with the same index
            val result = ArrayList<PTerm<T>>(sortedTerms.size)
            var i = 0
            val tempList = ArrayList<T>(3)
            while (i < sortedTerms.size) {
                val term = sortedTerms[i]
                val index = term.index
                // find next terms with the same index
                var next = i + 1
                while (next < sortedTerms.size && sortedTerms[next].index == index) {
                    next++
                }
                if (next == i + 1) {
                    // only one term
                    result.add(term)
                    i = next
                    continue
                }
                val sum = if (next == i + 2) {
                    // two terms
                    model.add(term.value, sortedTerms[i + 1].value)
                } else {
                    // more than two terms
                    tempList.clear()
                    for (j in i until next) {
                        tempList.add(sortedTerms[j].value)
                    }
                    model.sum(tempList)
                }
                if (!model.isZero(sum)) {
                    result.add(PTerm(index, sum))
                }
                i = next
            }
            return result
        }

        private fun <T : Any> addTerms(model: Ring<T>, a: List<PTerm<T>>, b: List<PTerm<T>>): List<PTerm<T>> {
            val size = maxOf(a.size, b.size)
            val result = ArrayList<PTerm<T>>(size)
            var i = 0
            var j = 0
            while (i < a.size && j < b.size) {
                val ai = a[i]
                val bj = b[j]
                if (ai.index == bj.index) {
                    val sum = model.add(ai.value, bj.value)
                    if (!model.isZero(sum)) {
                        result.add(PTerm(ai.index, sum))
                    }
                    i++
                    j++
                } else if (ai.index < bj.index) {
                    result.add(ai)
                    i++
                } else {
                    result.add(bj)
                    j++
                }
            }
            while (i < a.size) {
                result.add(a[i])
                i++
            }
            while (j < b.size) {
                result.add(b[j])
                j++
            }
            return result
        }

        private fun <T : Any> addTermsAll(model: Ring<T>, list: List<List<PTerm<T>>>): List<PTerm<T>> {
            if (list.isEmpty()) {
                return emptyList()
            }
            val size = list.maxOfOrNull { it.size } ?: 0
            val result = ArrayList<PTerm<T>>(size)
            val pos = IntArray(list.size) { 0 }
//            val listSizes = IntArray(list.size) { list[it].size }
            var power = 0
            val valuesToSum = ArrayList<T>(list.size)
            while (true) {
                var nextPower = Int.MAX_VALUE
                for ((i, terms) in list.withIndex()) {
                    if (pos[i] >= terms.size) {
                        continue
                    }
                    val term = terms[pos[i]]
                    if (term.index == power) {
                        valuesToSum.add(term.value)
                        pos[i]++
                    } else {
                        nextPower = minOf(nextPower, term.index)
                    }
                }
                val valuesCount = valuesToSum.size
                if (valuesCount == 0) {
                    break
                }
                if (valuesCount == 1) {
                    result.add(PTerm(power, valuesToSum[0]))
                } else {
                    val sum = model.sum(valuesToSum)
                    if (!model.isZero(sum)) {
                        result.add(PTerm(power, sum))
                    }
                }
                power = nextPower
                valuesToSum.clear()
            }

            return result
        }


        private fun <T : Any> multiplyTerms(model: Ring<T>, a: List<PTerm<T>>, b: List<PTerm<T>>): List<PTerm<T>> {
            if (a.isEmpty() || b.isEmpty()) {
                return emptyList()
            }
            val result = ArrayList<PTerm<T>>(a.size * b.size)
            for (ai in a) {
                for (bj in b) {
                    val index = ai.index + bj.index
                    val value = model.multiply(ai.value, bj.value)
                    if (!model.isZero(value)) {
                        result.add(PTerm(index, value))
                    }
                }
            }
            return mergeTerms(model, result)
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

        fun <T : Any> zero(model: Ring<T>): Polynomial<T> {
            return Polynomial(model, emptyList())
        }

        fun <T : Any> constant(model: Ring<T>, c: T): Polynomial<T> {
            if (model.isZero(c)) {
                return zero(model)
            }
            return Polynomial(model, listOf(PTerm(0, c)))
        }

        fun <T : Any> one(model: UnitRing<T>): Polynomial<T> {
            return constant(model, model.one)
        }

        fun <T : Any> x(model: UnitRing<T>): Polynomial<T> {
            return Polynomial(model, listOf(PTerm(1, model.one)))
        }


        fun <T : Any> fromList(model: Ring<T>, coef: List<T>): Polynomial<T> {
            val terms = coef.mapIndexedNotNull { index, value ->
                if (model.isZero(value)) {
                    null
                } else {
                    PTerm(index, value)
                }
            }
            return Polynomial(model, terms)
        }

        fun <T : Any> of(model: Ring<T>, vararg coef: T): Polynomial<T> {
            return fromList(model, coef.asList())
        }

        fun <T : Any> fromPower(model: Ring<T>, power: Int, value: T): Polynomial<T> {
            if (model.isZero(value)) {
                return zero(model)
            }
            return Polynomial(model, listOf(PTerm(power, value)))
        }


        fun <T : Any> sum(model: Ring<T>, vararg polys: Polynomial<T>): Polynomial<T> {
            return Polynomial(model, addTermsAll(model, polys.map { it.terms }))
        }
    }
}

class PolynomialAsRing<T : Any>(val model: Ring<T>) : Ring<Polynomial<T>> {
    override val zero: Polynomial<T>
        get() = Polynomial.zero(model)

    override fun add(x: Polynomial<T>, y: Polynomial<T>): Polynomial<T> {
        return x + y
    }

    override fun negate(x: Polynomial<T>): Polynomial<T> {
        return -x
    }

    override fun multiply(x: Polynomial<T>, y: Polynomial<T>): Polynomial<T> {
        return x * y
    }

    override val numberClass: Class<*>
        get() = Polynomial::class.java

    override fun isEqual(x: Polynomial<T>, y: Polynomial<T>): Boolean {
        return x.valueEquals(y)
    }



    override fun contains(x: Polynomial<T>): Boolean {
        TODO("Not yet implemented")
    }
}