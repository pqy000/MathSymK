package cn.mathsymk.model

import cn.mathsymk.AbstractMathObject
import cn.mathsymk.IMathObject
import cn.mathsymk.model.struct.AlgebraModel
import cn.mathsymk.model.struct.EuclidDomainModel
import cn.mathsymk.structure.EqualPredicate
import cn.mathsymk.structure.Field
import cn.mathsymk.structure.Ring
import cn.mathsymk.structure.UnitRing
import cn.mathsymk.util.ArraySup
import cn.mathsymk.util.DataStructureUtil
import java.util.*
import java.util.function.Function
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/*
 * Created at 2018/12/12 18:49
 * @author  liyicheng
 *
 * Re-written from 2024/08/19
 */



@JvmRecord
data class ChPow(val ch: String, val pow: Int) : Comparable<ChPow> {
    override fun compareTo(other: ChPow): Int {
        val c = ch.compareTo(other.ch)
        return if (c != 0) c else pow - other.pow
    }

    override fun toString(): String {
        if (pow == 1) {
            return ch
        }
        return "$ch^$pow"
    }
}

typealias TermChs = Array<ChPow>

@JvmRecord
data class MTerm<T>(
    /**
     * A sorted array of characters and their powers.
     */
    val chs: TermChs,
    val c: T,
    val totalPow: Int = chs.sumOf { it.pow }
) : Comparable<MTerm<T>> {

    override fun compareTo(other: MTerm<T>): Int {
        val d = totalPow - other.totalPow
        if (d != 0) return d

        return ArraySup.compareLexi(chs, other.chs)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MTerm<*>

        if (!chs.contentEquals(other.chs)) return false
        if (c != other.c) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chs.contentHashCode()
        result = 31 * result + (c?.hashCode() ?: 0)
        return result
    }


    fun getCharPow(ch: String): Int {
        return chs.binarySearch(ChPow(ch, 0)).let {
            if (it < 0) 0 else chs[it].pow
        }
    }

    fun chsEquals(other: MTerm<T>): Boolean {
        return chs.contentEquals(other.chs)
    }


    fun chsContains(other : MTerm<T>) : Boolean {
        var j = 0
        for (i in other.chs.indices) {
            // find the corresponding character in other
            while (j < chs.size && chs[j].ch < other.chs[i].ch) {
                j++
            }
            if (j == chs.size || chs[j].ch != other.chs[i].ch) {
                return false
            }
            if (chs[j].pow < other.chs[i].pow) {
                return false
            }
            j++
        }
        return true
    }

    fun chsMultiply(other: MTerm<T>): TermChs {
        return multiplyChars(chs, other.chs)
    }

    fun chsExactDivide(other: MTerm<T>): TermChs {
        // this / other
        val newChs = ArrayList<ChPow>(other.chs.size)
        var j = 0
        for (i in other.chs.indices) {
            // find the corresponding character in other
            while (j < chs.size && chs[j].ch < other.chs[i].ch) {
                newChs.add(other.chs[j]) // character not in this term
                j++
            }
            if (j == chs.size || chs[j].ch != other.chs[i].ch || chs[j].pow < other.chs[i].pow) {
                throw IllegalArgumentException("${chs[i]} in $this is not in the other term ${other}.")
            }
            val r = chs[j].pow - other.chs[i].pow
            if (r != 0) {
                newChs.add(ChPow(chs[j].ch, r))
            }
            j++
        }
        while(j < chs.size) {
            newChs.add(chs[j])
            j++
        }
        return newChs.toTypedArray()
    }

    /**
     * Normalizes the term, which means removing the characters with power 0.
     */
    fun normalize(): MTerm<T> {
        if (chs.all { it.pow != 0 }) {
            return this
        }
        return MTerm(chs.filter { it.pow != 0 }.toTypedArray(), c)
    }

    val isConstant: Boolean
        get() = chs.isEmpty()

    override fun toString(): String {
        return if (chs.isEmpty()) {
            c.toString()
        } else {
            val sb = StringBuilder()
            sb.append(c)
            for (ch in chs) {
                sb.append(ch)
            }
            sb.toString()
        }
    }

    companion object {
        fun multiplyChars(chs1: TermChs, chs2: TermChs): TermChs {
            return DataStructureUtil.mergeSorted2(chs1, chs2,
                comparing = { x, y -> x.compareTo(y) },
                merger2 = { x, y -> if (x.pow + y.pow == 0) null else ChPow(x.ch, x.pow + y.pow) })
        }

        private val CHAR_PATTERN =
            Pattern.compile("((?<ch1>[a-zA-Z]_?\\d*)|(\\{(?<ch2>[^}]+)}))(\\^(?<pow>[+-]?\\d+))?")


        /**
         * Parses the character part and combines it with the coefficient to build a term. The format of
         * character is:
         *
         *     character  ::= ch1 | ("{" ch2 "}")
         *     ch1        ::= [a-zA-Z]\w*
         *     ch2        ::= [^{]+
         *     power      ::= (+|-)? \d+
         *
         * `ch1` or `ch2` will be the characters stored in the resulting term.
         *
         */
        fun parseChar(chs: String): TermChs {
            val map = TreeMap<String, Int>()
            val matcher = CHAR_PATTERN.matcher(chs)
            while (matcher.lookingAt()) {
                val ch = matcher.group("ch1") ?: matcher.group("ch2")
                val pow = matcher.group("pow")?.toInt() ?: 1
                map.merge(ch, pow, Int::plus)
                matcher.region(matcher.end(), chs.length)
            }
            if (!matcher.hitEnd()) {
                throw IllegalArgumentException("Illegal format: $chs")
            }
            return map.map { (k, v) -> ChPow(k, v) }.toTypedArray()
        }

        fun <T> parse(c: T, chs: String): MTerm<T> {
            return MTerm(parseChar(chs), c)
        }
    }
}


class Multinomial<T : Any>
internal constructor(
    model: Ring<T>,
    val terms: List<MTerm<T>>
) : AbstractMathObject<T, Ring<T>>(model),
    EuclidDomainModel<Multinomial<T>>,
    AlgebraModel<T, Multinomial<T>> {


    /*
    Basic operations
     */

    override val isZero: Boolean
        get() = terms.isEmpty()

    /**
     * Returns the leading term of the multinomial.
     *
     * @throws NoSuchElementException if the multinomial is zero.
     */
    val leadTerm: MTerm<T>
        get() = terms[0]

    val leadPow: TermChs
        get() = leadTerm.chs


    private inline fun mapTermsNonZeroT(transform: (MTerm<T>) -> MTerm<T>): Multinomial<T> {
        val newTerms = terms.map(transform)
        return Multinomial(model, newTerms)
    }

    private inline fun mapTermsNonZero(transform: (T) -> T): Multinomial<T> {
        return mapTermsNonZeroT { MTerm(it.chs, transform(it.c)) }
    }

    /*
    Math object
     */
    override fun toString(): String {
        if (terms.isEmpty()) return "0"
        return terms.joinToString(" + ") { it.toString() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Multinomial<*>) return false

        if (model != other.model) return false
        if (terms != other.terms) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + terms.hashCode()
        return result
    }

    override fun valueEquals(obj: IMathObject<T>): Boolean {
        if (obj !is Multinomial<T>) {
            return false
        }
        if (model != obj.model) {
            return false
        }
        for ((a, b) in terms.zip(obj.terms)) {
            if (!a.chsEquals(b) || !model.isEqual(a.c, b.c)) {
                return false
            }
        }
        return true
    }

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Multinomial<N> {
        require(newCalculator is Ring)
        return mapTermsPossiblyZero(terms, newCalculator) { mapper.apply(it) }
    }

    /*
    Term-wise operations
     */
    override fun times(k: T): Multinomial<T> {
        return mapTermsPossiblyZero(terms, model) { model.multiply(k, it) }
    }

    fun times(t: MTerm<T>): Multinomial<T> {
        return mapTermsPossiblyZeroT(terms, model) {
            termMultiply(it, t)
        }
    }

    override fun div(k: T): Multinomial<T> {
        require(model is UnitRing)
        return mapTermsNonZero { model.exactDivide(it, k) }
    }

//    fun exactDivide(t: MTerm<T>): Multinomial<T> {
//        require(model is UnitRing)
//        return mapTermsNonZeroT { termDivide(it, t) }
//    }

    protected fun termMultiply(t1: MTerm<T>, t2: MTerm<T>): MTerm<T> {
        val c = model.multiply(t1.c, t2.c)
        val chs = MTerm.multiplyChars(t1.chs, t2.chs)
        return MTerm(chs, c)
    }

//    protected fun termExactDiv(t1 : MTerm<T>, t2 : MTerm<T>) : MTerm<T> {
//        val c = model.exactDivide(t1.c, t2.c)
//        val chs = MTerm.multiplyChars(t1.chs, t2.chs)
//        return MTerm(chs, c)
//    }

    /*
    Multinomial as a ring
     */

    override fun plus(y: Multinomial<T>): Multinomial<T> {
        return addTerms2(model, terms, y.terms)
    }

    override fun unaryMinus(): Multinomial<T> {
        return mapTermsNonZero { model.negate(it) }
    }

    override fun times(y: Multinomial<T>): Multinomial<T> {
        return multiplyTerms(model, terms, y.terms)
    }


    override fun isUnit(): Boolean {
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        if (terms.size != 1) return false
        val t = terms[0]
        return t.isConstant && model.isUnit(t.c)
    }

    /*
    GCD
     */


    override fun divideAndRemainder(y: Multinomial<T>): Pair<Multinomial<T>, Multinomial<T>> {
        if (y.isZero) throw ArithmeticException("Division by zero")
        require(model is UnitRing<T>) { "The model must support `exactDivide`" }
        if (isZero) {
            return this to this
        }
        var remainder = this
        val quotientTerms = mutableListOf<MTerm<T>>()

        val leadTermY = y.leadTerm
        while (!remainder.isZero) {
            val remTerms = remainder.terms
            for (j in remTerms.lastIndex downTo 0) {
                val term = remTerms[j]
                if (term.totalPow < leadTermY.totalPow) {
                    break
                }
                if (!term.chsContains(leadTermY)) {
                    continue
                }
                val q = model.exactDivide(term.c, leadTermY.c)
                val newChs = term.chsExactDivide(leadTermY) // term / leadTermY
                val quotientTerm = MTerm(newChs, q)
                quotientTerms.add(quotientTerm)

                val subtrahend = y.times(quotientTerm)
                remainder -= subtrahend
                continue
            }
            break // no term can be divided
        }
        return Multinomial(model, quotientTerms.sorted()) to remainder
    }

    override fun gcdUV(y: Multinomial<T>): Triple<Multinomial<T>, Multinomial<T>, Multinomial<T>> {
        require(model is Field<T>) { "The model is not a field." }
        return EuclidDomainModel.gcdUVForModel(this, y, zero(model), one(model))
    }


    companion object {
        private val EMPTY_ARRAY = emptyArray<ChPow>()

        private inline fun <T : Any, R : Any> mapTermsPossiblyZeroT(
            terms: List<MTerm<T>>,
            model: Ring<R>,
            transform: (MTerm<T>) -> MTerm<R>
        ): Multinomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val t2 = transform(t)
                if (model.isZero(t2.c)) null else t2
            }
            return Multinomial(model, newTerms)
        }

        private inline fun <T : Any, R : Any> mapTermsPossiblyZero(
            terms: List<MTerm<T>>,
            model: Ring<R>,
            transform: (T) -> R
        ): Multinomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val v = transform(t.c)
                if (model.isZero(v)) null else MTerm(t.chs, v)
            }
            return Multinomial(model, newTerms)
        }

        private fun <T : Any> add2Term(model: Ring<T>, a: MTerm<T>, b: MTerm<T>): MTerm<T>? {
            val r = model.add(a.c, b.c)
            return if (model.isZero(r)) null else MTerm(a.chs, r)
        }

        private fun <T : Any> addMultiTerm(model: Ring<T>, list: List<MTerm<T>>, tempList: ArrayList<T>): MTerm<T>? {
            tempList.clear()
            list.mapTo(tempList) { it.c }
            val sum = model.sum(tempList)
            return if (model.isZero(sum)) null else MTerm(list[0].chs, sum)
        }

        private fun <T : Any> mergeTerms(
            model: Ring<T>,
            rawTerms: List<MTerm<T>>,
            maxMergeSizeEst: Int = 3,
            estimatedSize: Int = rawTerms.size
        ): List<MTerm<T>> {
            val tempList = ArrayList<T>(maxMergeSizeEst)
            return DataStructureUtil.mergeRawList(
                rawTerms,
                comparing = { x, y -> x.compareTo(y) },
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) },
                estimatedSize = estimatedSize
            )
        }

        private fun <T : Any> addTerms2(model: Ring<T>, a: List<MTerm<T>>, b: List<MTerm<T>>): Multinomial<T> {
            val result = DataStructureUtil.mergeSorted2(
                a, b,
                comparing = { x, y -> x.compareTo(y) },
                merger2 = { x, y -> add2Term(model, x, y) },
            )
            return Multinomial(model, result)
        }

        private fun <T : Any> addTermsAll(model: Ring<T>, termsList: List<List<MTerm<T>>>): Multinomial<T> {
            val tempList = ArrayList<T>(termsList.size)
            val resultTerms = DataStructureUtil.mergeSortedK(
                termsList,
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) }
            )
            return Multinomial(model, resultTerms)
        }

        private fun <T : Any> multiplyTerms(model: Ring<T>, a: List<MTerm<T>>, b: List<MTerm<T>>): Multinomial<T> {
            if (a.isEmpty() || b.isEmpty()) {
                return Multinomial(model, emptyList())
            }
            val result = ArrayList<MTerm<T>>(a.size * b.size)
            for (ai in a) {
                for (bj in b) {
                    val v = model.multiply(ai.c, bj.c)
                    if (!model.isZero(v)) {
                        val newChs = MTerm.multiplyChars(ai.chs, bj.chs)
                        result.add(MTerm(newChs, v))
                    }
                }
            }
            val resTerms = mergeTerms(model, result, estimatedSize = a.size + b.size)
            return Multinomial(model, resTerms)
        }


        /**
         * Returns a zero multinomial.
         */
        fun <T : Any> zero(model: Ring<T>): Multinomial<T> {
            return Multinomial(model, emptyList())
        }

        /**
         * Creates a multinomial from a list of terms.
         */
        fun <T : Any> fromTerms(model: Ring<T>, terms: List<MTerm<T>>): Multinomial<T> {
            val filteredTerms = terms.map { it.normalize() }.filter { !model.isZero(it.c) }
            return Multinomial(model, mergeTerms(model, filteredTerms))
        }

        /**
         * Creates a multinomial from a list of terms.
         */
        fun <T : Any> of(mc: Ring<T>, vararg terms: Pair<T, String>): Multinomial<T> {
            return fromTerms(mc, terms.map { MTerm.parse(it.first, it.second) })
        }

        /**
         * Creates a constant multinomial.
         */
        fun <T : Any> constant(model: Ring<T>, c: T): Multinomial<T> {
            return if (model.isZero(c)) {
                zero(model)
            } else {
                Multinomial(model, listOf(MTerm(EMPTY_ARRAY, c))) // use a singleton array
            }
        }

        /**
         * Creates a multinomial `1`.
         */
        fun <T : Any> one(model: UnitRing<T>): Multinomial<T> {
            return constant(model, model.one)
        }

        /**
         * Creates a monomial from a term.
         */
        fun <T : Any> monomial(model: Ring<T>, t: MTerm<T>): Multinomial<T> {
            if (model.isZero(t.c)) {
                return zero(model)
            }
            return Multinomial(model, listOf(t.normalize()))
        }

        /**
         * Creates a monomial from a coefficient and a character.
         */
        fun <T : Any> monomial(model: Ring<T>, c: T, ch: String, pow: Int = 1): Multinomial<T> {
            if (model.isZero(c)) {
                return zero(model)
            }
            if (pow == 0) {
                return constant(model, c)
            }
            return monomial(model, MTerm(arrayOf(ChPow(ch, pow)), c))
        }

        /**
         * Builds a multinomial using the given builder action.
         *
         * In the builder action, some variables are predefined, including `x`, `y`, `z`, `w`, `a`, `b`.
         * For example, one can build a multinomial like this:
         * ```
         * val model = NumberModels.IntAsIntegers
         * val m = Multinomial.of(model) {
         *    3 * x + 2 * y + 1
         * }
         * ```
         *
         *
         */
        fun <T : Any> of(
            model: UnitRing<T>,
            builderAction: MultinomialBuilderScope<T>.() -> Multinomial<T>
        ): Multinomial<T> {
            return MultinomialBuilderScope(model).builderAction()
        }

        /**
         * Sums a list of multinomials.
         */
        fun <T : Any> sum(model: Ring<T>, vararg terms: Multinomial<T>): Multinomial<T> {
            return sum(model, terms.asList())
        }

        /**
         * Sums a list of multinomials.
         */
        fun <T : Any> sum(model: Ring<T>, terms: List<Multinomial<T>>): Multinomial<T> {
            when (terms.size) {
                0 -> return zero(model)
                1 -> return terms[0]
                2 -> return addTerms2(model, terms[0].terms, terms[1].terms)
            }
            return addTermsAll(model, terms.map { it.terms })
        }
    }
}

open class MultinomialOnRing<T : Any>(model: Ring<T>) : Ring<Multinomial<T>> {

    @Suppress("CanBePrimaryConstructorProperty")
    open val model: Ring<T> = model

    final override val zero: Multinomial<T> = Multinomial.zero(model)

    override fun add(x: Multinomial<T>, y: Multinomial<T>): Multinomial<T> {
        return x + y
    }

    override fun negate(x: Multinomial<T>): Multinomial<T> {
        return -x
    }

    override fun multiply(x: Multinomial<T>, y: Multinomial<T>): Multinomial<T> {
        return x * y
    }

    override val numberClass: Class<*>
        get() = Multinomial::class.java

    override fun isEqual(x: Multinomial<T>, y: Multinomial<T>): Boolean {
        return x.valueEquals(y)
    }

    override fun contains(x: Multinomial<T>): Boolean {
        return x.terms.all { model.contains(it.c) }
    }

    override fun subtract(x: Multinomial<T>, y: Multinomial<T>): Multinomial<T> {
        return x - y
    }

    override fun multiplyLong(x: Multinomial<T>, n: Long): Multinomial<T> {
        return x.times(n)
    }

    override fun isZero(x: Multinomial<T>): Boolean {
        return x.isZero
    }

    override fun sum(elements: List<Multinomial<T>>): Multinomial<T> {
        return Multinomial.sum(model, *elements.toTypedArray())
    }
}

open class MultinomialOnUnitRing<T : Any>(model: UnitRing<T>) : MultinomialOnRing<T>(model), UnitRing<Multinomial<T>> {
    override val one: Multinomial<T> = Multinomial.constant(model, model.one)
    override val numberClass: Class<*>
        get() = Multinomial::class.java
}

open class MultinomialOnField<T : Any>(model: Field<T>) : MultinomialOnUnitRing<T>(model) {

}


class MultinomialBuilderScope<T : Any>(val model: UnitRing<T>) {

    val x = Multinomial.monomial(model, model.one, "x")
    val y = Multinomial.monomial(model, model.one, "y")
    val z = Multinomial.monomial(model, model.one, "z")
    val w = Multinomial.monomial(model, model.one, "w")
    val a = Multinomial.monomial(model, model.one, "a")
    val b = Multinomial.monomial(model, model.one, "b")

    val one = Multinomial.constant(model, model.one)
    val zero = Multinomial.zero(model)


    operator fun T.times(chs: String): Multinomial<T> {
        val term = MTerm.parse(this, chs)
        return Multinomial(model, listOf(term))
    }

    operator fun T.times(m: Multinomial<T>): Multinomial<T> {
        return m.times(this)
    }

}