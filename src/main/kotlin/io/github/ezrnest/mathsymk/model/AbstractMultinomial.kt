package io.github.ezrnest.mathsymk.model

import io.github.ezrnest.mathsymk.AbstractMathObject
import io.github.ezrnest.mathsymk.model.struct.AlgebraModel
import io.github.ezrnest.mathsymk.structure.EqualPredicate
import io.github.ezrnest.mathsymk.structure.OrderedRing
import io.github.ezrnest.mathsymk.structure.Ring
import io.github.ezrnest.mathsymk.structure.UnitRing
import io.github.ezrnest.mathsymk.util.DataStructureUtil

@JvmRecord
data class Term<T, K>(val c: T, val key: K) {
    override fun toString(): String {
        return "$c*$key"
    }
}

abstract class AbstractMultinomial<T, K, C : Comparator<K>, R : AbstractMultinomial<T, K, C, R>>(
    model: Ring<T>,
    /**
     * The terms of this multinomial in a sorted order given by [termOrder].
     */
    val terms: List<Term<T, K>>,
    /**
     * The comparator for the keys of the terms.
     */
    val termOrder: C
) : AbstractMathObject<T, Ring<T>>(model), AlgebraModel<T, R> {
    /**
     * A comparator which compares the keys of the terms.
     */
    protected val comparatorTerm: Comparator<Term<T, K>> = Comparator.comparing(Term<T, K>::key, termOrder)

    /**
     * Creates a new multinomial with the given terms.
     */
    protected abstract fun fromTerms(terms: List<Term<T, K>>): R

    protected fun checkTermOrder(y: R) {
        require(termOrder == y.termOrder) {
            "The term orders are different for: $this and $y"
        }
    }

    protected fun checkTermOrder(ys: Iterable<R>) {
        ys.forEach { checkTermOrder(it) }
    }

    /*
    Math object
     */

    override fun toString(): String {
        if (terms.isEmpty()) return "0"
        return terms.joinToString(" + ")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractMultinomial<*, *, *, *>) return false

        if (model != other.model) return false
        if (termOrder != other.termOrder) return false
        if (terms != other.terms) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + terms.hashCode()
        return result
    }


    /*
    Term-wise operations
     */


//    protected inline fun mapTermsNonZeroT(transform: (Term<T, K>) -> Term<T, K>): R {
//        val newTerms = terms.map(transform)
//        return fromTerms(newTerms)
//    }

    protected inline fun mapTermsNonZero(transform: (Term<T, K>) -> Term<T, K>): R {
        val newTerms = terms.map(transform)
        return fromTerms(newTerms)
    }

    protected inline fun mapTermsPossiblyZero(transform: (Term<T, K>) -> Term<T, K>?): R {
        val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
            transform(t)?.takeUnless { model.isZero(it.c) }
        }
        return fromTerms(newTerms)
    }


    protected fun add2Term(t1: Term<T, K>, t2: Term<T, K>): Term<T, K>? {
        val r = model.add(t1.c, t2.c)
        return if (model.isZero(r)) null else Term(r, t1.key)
    }

    protected fun addMultiTerms(terms: List<Term<T, K>>, tempList: ArrayList<T>): Term<T, K>? {
        tempList.clear()
        terms.mapTo(tempList) { it.c }
        val sum = model.sumOf(tempList)
        return if (model.isZero(sum)) null else Term(sum, terms[0].key)
    }

    protected fun addTermList2(a: List<Term<T, K>>, b: List<Term<T, K>>): R {
        val newTerms = DataStructureUtil.mergeSorted2(
            a, b, comparator = comparatorTerm, merger2 = this::add2Term
        )
        return fromTerms(newTerms)
    }

    protected fun addTermsAll(termsList: List<List<Term<T, K>>>): R {
        val tempList = ArrayList<T>(termsList.size)
        val resultTerms = DataStructureUtil.mergeSortedK(
            termsList,
            comparator = comparatorTerm,
            merger2 = this::add2Term,
            mergerMulti = { terms -> addMultiTerms(terms, tempList) }
        )
        return fromTerms(resultTerms)
    }


    protected abstract fun termMultiply(t1: Term<T, K>, t2: Term<T, K>): Term<T, K>?

    override fun plus(y: R): R {
        return addTermList2(terms, y.terms)
    }

    override fun unaryMinus(): R {
        return mapTermsNonZero { Term(model.negate(it.c), it.key) }
    }

    override val isZero: Boolean
        get() = terms.isEmpty()

    override fun scalarMul(k: T): R {
        if (model.isZero(k)) return fromTerms(emptyList())
        return mapTermsPossiblyZero { Term(model.multiply(k, it.c), it.key) }
    }

    protected fun multiplyTerms(a: List<Term<T, K>>, b: List<Term<T, K>>): R {
        val result = ArrayList<Term<T, K>>(a.size * b.size)
        for (ai in a) {
            for (bj in b) {
                val t = termMultiply(ai, bj) ?: continue
                result.add(t)
            }
        }
        val merged = mergeTerms(model, comparatorTerm, result)
        return fromTerms(merged)
    }

    override fun times(y: R): R {
        if (isZero || y.isZero) return fromTerms(emptyList())
        return multiplyTerms(terms, y.terms)
    }

    override fun scalarDiv(k: T): R {
        val model = model
        require(model is UnitRing) { "The model must support division." }
        return mapTermsNonZero { Term(model.exactDiv(it.c, k), it.key) }
    }

    companion object {
        protected fun <T, K> add2Term(model: Ring<T>, t1: Term<T, K>, t2: Term<T, K>): Term<T, K>? {
            val r = model.add(t1.c, t2.c)
            return if (model.isZero(r)) null else Term(r, t1.key)
        }

        protected fun <T, K> addMultiTerms(
            model: Ring<T>, terms: List<Term<T, K>>, tempList: ArrayList<T>
        ): Term<T, K>? {
            tempList.clear()
            terms.mapTo(tempList) { it.c }
            val sum = model.sumOf(tempList)
            return if (model.isZero(sum)) null else Term(sum, terms[0].key)
        }


        @JvmStatic
        protected fun <T, K> mergeTerms(
            model: Ring<T>, comparatorTerm: Comparator<Term<T, K>>,
            rawTerms: List<Term<T, K>>
        ): List<Term<T, K>> {
            val tempList = ArrayList<T>(4)
            return DataStructureUtil.mergeRawList(rawTerms, comparatorTerm,
                { t1, t2 -> add2Term(model, t1, t2) },
                { terms -> addMultiTerms(model, terms, tempList) })
        }


        fun <T, K> stringOf(
            terms: List<Term<T, K>>, isOne: (T) -> Boolean,
            isNegativeAndAbs: (T) -> Pair<Boolean, T>,
            valueToString: (T) -> String, keyToString: (K) -> String
        ): String {
            if (terms.isEmpty()) return "0"
            val sb = StringBuilder()
            for (element in terms) {
                val (c, k) = element
                val (isNeg, v) = isNegativeAndAbs(c)
                if(sb.isEmpty()) {
                    if (isNeg) sb.append("-")
                }else{
                    if (isNeg) {
                        sb.append(" - ")
                    } else {
                        sb.append(" + ")
                    }
                }

                val valueString = valueToString(v)
                val keyString = keyToString(k)

                if (keyString.isEmpty()) {
                    sb.append(valueString)
                } else {
                    if (isOne(v)) {
                        sb.append(keyString)
                    } else {
                        sb.append(v).append("*").append(keyString)
                    }
                }
            }
            return sb.toString()
        }

        internal fun <T> isOneFromModel(model: EqualPredicate<T>): (T) -> Boolean {
            if (model is UnitRing) {
                return model::isOne
            }
            return { false }
        }

        internal fun <T> isNegativeAndAbsFromModel(model: Ring<T>): (T) -> Pair<Boolean, T> {
            return if (model is OrderedRing) { t: T -> model.isNegative(t) to model.abs(t) } else { t: T -> false to t }
        }
    }

}