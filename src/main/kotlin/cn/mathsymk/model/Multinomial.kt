package cn.mathsymk.model

import cn.mathsymk.AbstractMathObject
import cn.mathsymk.IMathObject
import cn.mathsymk.model.struct.AlgebraModel
import cn.mathsymk.model.struct.EuclidDomainModel
import cn.mathsymk.model.struct.RingModel
import cn.mathsymk.structure.*
import cn.mathsymk.util.DataStructureUtil
import java.util.*
import java.util.function.Function
import java.util.regex.Pattern
import kotlin.Comparator
import kotlin.collections.ArrayList


/*
 * Created at 2018/12/12 18:49
 * @author  liyicheng
 *
 * Re-written from 2024/08/19
 */



@JvmRecord
data class ChPow(val ch: String, val pow: Int) {
    override fun toString(): String {
        if (pow == 1) {
            return ch
        }
        return "$ch^$pow"
    }
}

/**
 * Represents the character part of a term, which is a read-only array of characters and their powers.
 */
typealias TermChs = Array<ChPow>


abstract class TermComparator(val chComparator: Comparator<String>) : Comparator<MTerm<*>>

/**
 *
 */
@JvmRecord
data class MTerm<T>(
    /**
     * An array of characters and their powers.
     * The array is sorted by the character in a specific order.
     */
    val chs: TermChs,
    val c: T,
    val totalPow: Int = chs.sumOf { it.pow }
) {

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


    /**
     * Determines whether this term contains the other term in characters, ignoring the coefficient.
     *
     * For example, `xy^2` contains `xy` but not `x^2y`.
     */
    fun chsContains(other: MTerm<T>): Boolean {
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

    /**
     * Performs the character division of this term by the other term.
     *
     * For example, `x^2 y^3 z^4` divided by `x y^2 z^3` is `xyz`.
     *
     * @return the resulting characters
     */
    fun chsExactDivide(other: MTerm<T>): TermChs {
        // this / other
        val newChs = ArrayList<ChPow>(other.chs.size)
        var j = 0
        for (i in other.chs.indices) {
            // find the corresponding character in other
            while (j < chs.size && chs[j].ch < other.chs[i].ch) {
                newChs.add(chs[j]) // this character is not in the other term
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
        while (j < chs.size) {
            newChs.add(chs[j])
            j++
        }
        return newChs.toTypedArray()
    }

    /**
     * Normalizes the term, removing the characters with power 0.
     */
    fun normalized(chComp: Comparator<String>): MTerm<T> {
        val newChs = chs.filter { it.pow != 0 }.toTypedArray()
        Arrays.sort(newChs) { o1, o2 -> chComp.compare(o1.ch, o2.ch) }
        return MTerm(newChs, c)
    }

    /**
     * Re-order the characters in the term using the given comparator.
     */
    fun reorderedBy(chComp: Comparator<String>):MTerm<T>{
        val newChs = chs.clone()
        Arrays.sort(newChs) { o1, o2 -> chComp.compare(o1.ch, o2.ch) }
        return MTerm(newChs, c)
    }

    val isConstant: Boolean
        get() = chs.isEmpty()

    override fun toString(): String {
        return if (chs.isEmpty()) {
            c.toString()
        } else {
            val sb = StringBuilder()
            sb.append(c)
//            sb.append("*")
            for (ch in chs) {
                sb.append(ch)
            }
            sb.toString()
        }
    }

    companion object {


        private fun compareLex(
            chs1: TermChs, chs2: TermChs,
            chComparator: Comparator<String> = Comparator.naturalOrder()
        ): Int {
            for (i in 0 until minOf(chs1.size, chs2.size)) {
                val cp1 = chs1[i]
                val cp2 = chs2[i]
                var c = chComparator.compare(cp1.ch, cp2.ch)
                if (c != 0) return -c
                c = cp1.pow - cp2.pow
                if (c != 0) return c
            }
            return chs1.size - chs2.size
        }


        fun compareGradedLex(
            t1: MTerm<*>, t2: MTerm<*>,
            chComparator: Comparator<String> = Comparator.naturalOrder()
        ): Int {
            if (t1.totalPow != t2.totalPow) {
                return t1.totalPow - t2.totalPow
            }
            return compareLex(t1.chs, t2.chs, chComparator)
        }

        fun getLexComparator(chComp: Comparator<String> = Comparator.naturalOrder()): TermComparator {
            return object : TermComparator(chComp) {
                override fun compare(o1: MTerm<*>, o2: MTerm<*>): Int {
                    return compareLex(o1.chs, o2.chs, chComp)
                }
            }
        }

        fun getGradedLexComparator(chComp: Comparator<String> = Comparator.naturalOrder()): TermComparator {
            return object : TermComparator(chComp) {
                override fun compare(o1: MTerm<*>, o2: MTerm<*>): Int {
                    return compareGradedLex(o1, o2, chComp)
                }
            }
        }

        fun getGradedRevLexComparator(chComp: Comparator<String> = Comparator.naturalOrder()): TermComparator {
            return getGradedLexComparator(chComp.reversed())
        }


        fun multiplyChars(chs1: TermChs, chs2: TermChs, chComp: Comparator<String>): TermChs {
            return DataStructureUtil.mergeSorted2Arr(chs1, chs2,
                compFunc = { x, y -> chComp.compare(x.ch, y.ch) }, // compare by character
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

/**
 *
 *
 *
 * ## Term (monomial) order
 *
 */
class Multinomial<T : Any>
internal constructor(
    model: Ring<T>,
    /**
     * The list of terms in the ascending order given by [termComparator].
     * Terms with higher order come later in the list.
     */
    val terms: List<MTerm<T>>,
    /**
     * The comparator used to order the terms and the characters.
     */
    val termComparator: TermComparator
) : AbstractMathObject<T, Ring<T>>(model),
    RingModel<Multinomial<T>>,
    AlgebraModel<T, Multinomial<T>> {


    /*
    Basic operations
     */

    override val isZero: Boolean
        get() = terms.isEmpty()

    /**
     * Returns the leading term of the multinomial with respect to the term comparator,
     * which is the term with "the highest order".
     *
     * For example, with the default term order, the leading term of `1 + x + z + x^2 + x^2 z` is `x^2 z`.
     *
     * @throws NoSuchElementException if the multinomial is zero.
     * @see termComparator
     */
    val leadTerm: MTerm<T>
        get() = terms.last()

    /**
     * Returns the leading term's characters, equivalent to `leadTerm.chs`.
     *
     * @see leadTerm
     */
    val leadChs: TermChs
        get() = leadTerm.chs


    private inline fun mapTermsNonZeroT(transform: (MTerm<T>) -> MTerm<T>): Multinomial<T> {
        val newTerms = terms.map(transform)
        return Multinomial(model, newTerms, termComparator)
    }

    private inline fun mapTermsNonZero(transform: (T) -> T): Multinomial<T> {
        return mapTermsNonZeroT { MTerm(it.chs, transform(it.c)) }
    }
    /*
    Term order
     */

    /**
     * Returns a new multinomial by reordering the terms using the given comparator.
     */
    fun reorderedBy(comp: TermComparator): Multinomial<T> {
        if (termComparator == comp) {
            return this
        }
        val newTerms = terms.map { it.reorderedBy(comp.chComparator) }.sortedWith(comp)
        return Multinomial(model, newTerms, comp)
    }

    private fun checkTermOrder(y: Multinomial<T>) {
        require(termComparator == y.termComparator) { "The term comparators are different." }
    }

    private fun checkTermOrder(ys: Iterable<Multinomial<T>>) {
        require(ys.all { it.termComparator == termComparator }) { "The term comparators are different." }
    }

    /**
     * Creates a new multinomial from the given terms.
     * It is required that the terms are ordered by the term comparator.
     */
    private fun fromTerms(ts: List<MTerm<T>>): Multinomial<T> {
        return Multinomial(model, ts, termComparator)
    }

    fun leadTermCompare(y: Multinomial<T>): Int {
        return termComparator.compare(leadTerm, y.leadTerm)
    }

    /*
    Math object
     */
    override fun toString(): String {
        if (terms.isEmpty()) return "0"
        // TODO better string representation
        return terms.joinToString(" + ") { it.toString() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Multinomial<*>) return false

        if (model != other.model) return false
        if (termComparator != other.termComparator) return false
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
        val otherTerms = if (termComparator == obj.termComparator) obj.terms else obj.terms.sortedWith(termComparator)
        for ((a, b) in terms.zip(otherTerms)) {
            if (!a.chsEquals(b) || !model.isEqual(a.c, b.c)) {
                return false
            }
        }
        return true
    }

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Multinomial<N> {
        require(newCalculator is Ring)
        return mapTermsPossiblyZero(terms, newCalculator, termComparator) { mapper.apply(it) }
    }

    /*
    Term-wise operations
     */
    override fun times(k: T): Multinomial<T> {
        return mapTermsPossiblyZero(terms, model, termComparator) { model.multiply(k, it) }
    }

    private fun termMultiply(t1: MTerm<T>, t2: MTerm<T>): MTerm<T> {
        val c = model.multiply(t1.c, t2.c)
        if (model.isZero(c)) return MTerm(EMPTY_ARRAY, c)
        val newChs = MTerm.multiplyChars(t1.chs, t2.chs, termComparator.chComparator)
        return MTerm(newChs, c)
    }

    fun times(t: MTerm<T>): Multinomial<T> {
        return mapTermsPossiblyZeroT(terms, model, termComparator) {
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


    /*
    Multinomial as a ring
     */

    override fun plus(y: Multinomial<T>): Multinomial<T> {
        checkTermOrder(y)
        if(this.isZero) return y
        if(y.isZero) return this
        return addTerms2(model, termComparator, terms, y.terms)
    }

    override fun unaryMinus(): Multinomial<T> {
        if(this.isZero) return this
        return mapTermsNonZero { model.negate(it) }
    }

    override fun times(y: Multinomial<T>): Multinomial<T> {
        checkTermOrder(y)
        if(this.isZero) return this
        if(y.isZero) return y
        return multiplyTerms(model, termComparator, terms, y.terms)
    }


    fun isUnit(): Boolean {
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        if (terms.size != 1) return false
        val t = terms[0]
        return t.isConstant && model.isUnit(t.c)
    }

    /*
    GCD
     */


    /**
     *
     */
    fun divideAndRemainder(y: Multinomial<T>): Pair<Multinomial<T>, Multinomial<T>> {
        require(model is UnitRing<T>) { "The model must support `exactDivide`" }
        checkTermOrder(y)
        if (y.isZero) throw ArithmeticException("The divisor cannot be zero.")
        if (isZero) return this to this
        var p = this
        val qTerms = mutableListOf<MTerm<T>>()
        val rTerms = mutableListOf<MTerm<T>>()
        while (!p.isZero) {
            if (!p.leadTerm.chsContains(y.leadTerm)) {
                rTerms.add(p.leadTerm)
                p = fromTerms(p.terms.subList(0, p.terms.size - 1))
                continue
            }
            val q = termExactDiv(model, p.leadTerm, y.leadTerm)
            qTerms.add(q)
            p -= y.times(q)
        }
        qTerms.reverse()
        rTerms.reverse()
        val q = fromTerms(qTerms)
        val r = fromTerms(rTerms)
        return q to r
    }

    /**
     * Divides this multinomial by a list of multinomials `fs` and returns the quotient and the remainder.
     *
     */
    fun divideAndRemainder(fs: List<Multinomial<T>>): Pair<List<Multinomial<T>>, Multinomial<T>> {
        require(fs.isNotEmpty())
        require(model is UnitRing<T>) { "The model must support `exactDivide`" }
        if (fs.any { it.isZero }) throw ArithmeticException("The divisor cannot be zero.")
        checkTermOrder(fs)
        if (isZero) return Collections.nCopies(fs.size, this) to this
        var p = this
        val qTerms = List(fs.size) { mutableListOf<MTerm<T>>() }
        val rTerms = mutableListOf<MTerm<T>>()
        while (!p.isZero) {
            var found = false
            var i = 0
            while (i < fs.size) {
                val f = fs[i]
                val leadTermF = f.leadTerm
                if (!p.leadTerm.chsContains(leadTermF)) {
                    i++
                    continue
                }
                val q = termExactDiv(model, p.leadTerm, leadTermF)
                qTerms[i].add(q)
                p -= f.times(q)
                found = true
            }
            if (!found) {
                rTerms.add(p.leadTerm)
                p = fromTerms(p.terms.subList(0, p.terms.size - 1))
            }
        }
        val qList = qTerms.map {
            it.reverse()
            fromTerms(it)
        }
        rTerms.reverse()
        val r = fromTerms(rTerms)
        return qList to r
    }

    fun gcdUV(y: Multinomial<T>): Triple<Multinomial<T>, Multinomial<T>, Multinomial<T>> {
        TODO()
//        require(model is Field<T>) { "The model is not a field." }
//        return EuclidDomainModel.gcdUVForModel(this, y, zero(model), one(model))
    }


    companion object {
        private val EMPTY_ARRAY = emptyArray<ChPow>()

        private inline fun <T : Any, R : Any> mapTermsPossiblyZeroT(
            terms: List<MTerm<T>>,
            model: Ring<R>,
            termComparator: TermComparator,
            transform: (MTerm<T>) -> MTerm<R>
        ): Multinomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val t2 = transform(t)
                if (model.isZero(t2.c)) null else t2
            }
            return Multinomial(model, newTerms, termComparator)
        }

        private inline fun <T : Any, R : Any> mapTermsPossiblyZero(
            terms: List<MTerm<T>>,
            model: Ring<R>,
            termComparator: TermComparator,
            transform: (T) -> R
        ): Multinomial<R> {
            return mapTermsPossiblyZeroT(terms, model, termComparator) { MTerm(it.chs, transform(it.c)) }
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
            model: Ring<T>, termComparator: Comparator<MTerm<*>>, rawTerms: List<MTerm<T>>,
            maxMergeSizeEst: Int = 3, estimatedSize: Int = rawTerms.size
        ): List<MTerm<T>> {
            val tempList = ArrayList<T>(maxMergeSizeEst)
            return DataStructureUtil.mergeRawList(
                rawTerms,
                comparator = termComparator,
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) },
                estimatedSize = estimatedSize
            )
        }

        private fun <T : Any> addTerms2(
            model: Ring<T>, termComparator: TermComparator,
            a: List<MTerm<T>>, b: List<MTerm<T>>
        ): Multinomial<T> {
            val result = DataStructureUtil.mergeSorted2(
                a, b,
                comparator = termComparator,
                merger2 = { x, y -> add2Term(model, x, y) },
            )
            return Multinomial(model, result, termComparator)
        }

        private fun <T : Any> addTermsAll(
            model: Ring<T>, termComparator: TermComparator, termsList: List<List<MTerm<T>>>
        ): Multinomial<T> {
            val tempList = ArrayList<T>(termsList.size)
            val resultTerms = DataStructureUtil.mergeSortedK(
                termsList,
                comparator = termComparator,
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) }
            )
            return Multinomial(model, resultTerms, termComparator)
        }

//        private fun <T : Any> termMultiply(
//            model: Ring<T>, termComparator: TermComparator,
//            t1: MTerm<T>, t2: MTerm<T>
//        ): MTerm<T> {
//            val c = model.multiply(t1.c, t2.c)
//            if (model.isZero(c)) {
//                return MTerm(EMPTY_ARRAY, c)
//            }
//            val newChs = MTerm.multiplyChars(t1.chs, t2.chs, termComparator.chComparator)
//            return MTerm(newChs, c)
//        }

        private fun <T : Any> multiplyTerms(
            model: Ring<T>, tc: TermComparator, a: List<MTerm<T>>, b: List<MTerm<T>>
        ): Multinomial<T> {
            val result = ArrayList<MTerm<T>>(a.size * b.size)
            for (ai in a) {
                for (bj in b) {
                    val v = model.multiply(ai.c, bj.c)
                    if (model.isZero(v)) continue
                    val newChs = MTerm.multiplyChars(ai.chs, bj.chs, tc.chComparator)
                    result.add(MTerm(newChs, v))
                }
            }
            val resTerms = mergeTerms(model, tc, result, estimatedSize = a.size + b.size)
            return Multinomial(model, resTerms, tc)
        }

        private fun <T : Any> termExactDiv(model: UnitRing<T>, t1: MTerm<T>, t2: MTerm<T>): MTerm<T> {
            val c = model.exactDivide(t1.c, t2.c)
            val chs = t1.chsExactDivide(t2)
            return MTerm(chs, c)
        }

        /**
         * The default term comparator.
         */
        val DEFAULT_TERM_COMPARATOR: TermComparator = MTerm.getLexComparator()

        /**
         * Returns a zero multinomial.
         */
        fun <T : Any> zero(model: Ring<T>, comp: TermComparator = DEFAULT_TERM_COMPARATOR): Multinomial<T> {
            return Multinomial(model, emptyList(), comp)
        }

        /**
         * Creates a multinomial from a list of terms.
         */
        fun <T : Any> fromTerms(
            terms: List<MTerm<T>>, model: Ring<T>, comp: TermComparator = DEFAULT_TERM_COMPARATOR
        ): Multinomial<T> {
            val filteredTerms = terms.map { it.normalized(comp.chComparator) }.filter { !model.isZero(it.c) }
            return Multinomial(model, mergeTerms(model, comp, filteredTerms), comp)
        }

        /**
         * Creates a multinomial from a list of terms.
         */
        fun <T : Any> of(
            mc: Ring<T>,
            vararg terms: Pair<T, String>,
            comp: TermComparator = DEFAULT_TERM_COMPARATOR
        ): Multinomial<T> {
            return fromTerms(terms.map { MTerm.parse(it.first, it.second) }, mc, comp)
        }

        /**
         * Creates a constant multinomial.
         */
        fun <T : Any> constant(c: T, model: Ring<T>, comp: TermComparator = DEFAULT_TERM_COMPARATOR): Multinomial<T> {
            return if (model.isZero(c)) {
                zero(model, comp)
            } else {
                Multinomial(model, listOf(MTerm(EMPTY_ARRAY, c)), comp) // use a singleton array
            }
        }

        /**
         * Creates a multinomial `1`.
         */
        fun <T : Any> one(model: UnitRing<T>, comp: TermComparator = DEFAULT_TERM_COMPARATOR): Multinomial<T> {
            return constant(model.one, model, comp)
        }

        /**
         * Creates a monomial from a term.
         */
        fun <T : Any> monomial(
            t: MTerm<T>,
            model: Ring<T>,
            comp: TermComparator = DEFAULT_TERM_COMPARATOR
        ): Multinomial<T> {
            if (model.isZero(t.c)) {
                return zero(model)
            }
            return Multinomial(model, listOf(t.normalized(comp.chComparator)), comp)
        }

        /**
         * Creates a monomial from a coefficient and a character.
         */
        fun <T : Any> monomial(
            c: T, ch: String, pow: Int, model: Ring<T>,
            comp: TermComparator = DEFAULT_TERM_COMPARATOR
        ): Multinomial<T> {
            if (model.isZero(c)) {
                return zero(model, comp)
            }
            if (pow == 0) {
                return constant(c, model, comp)
            }
            return monomial(MTerm(arrayOf(ChPow(ch, pow)), c), model, comp)
        }

        /**
         * Creates a context for building multinomials with a specific model and term comparator.
         *
         * In the action, some variables are predefined, including `x`, `y`, `z`, `w`, `a`, `b`.
         * Also, one can use `"x^2y".m` to create a monomial `x^2y`, and `T.m` to create a constant term.
         * For example, one can build a multinomial like this:
         * ```
         * val model = NumberModels.IntAsIntegers
         * val m = Multinomial.with(model) {
         *    3 * x + 2 * y + 1.m + "x^2y".m
         * }
         * ```
         *
         *
         */
        inline fun <T : Any, R> with(
            model: UnitRing<T>, comp: TermComparator = DEFAULT_TERM_COMPARATOR,
            action: MultinomialBuilderScope<T>.() -> R
        ): R {
            return action(MultinomialBuilderScope(model, comp))
        }

        fun <T : Any> parse(str: String, model: UnitRing<T>, comp: TermComparator = DEFAULT_TERM_COMPARATOR) {
            TODO()
        }

        fun getTermComparatorLex(chComparator: Comparator<String>): TermComparator {
            return MTerm.getLexComparator(chComparator)
        }

        fun getTermComparatorLexGraded(chComparator: Comparator<String>): TermComparator {
            return MTerm.getGradedLexComparator(chComparator)
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
                2 -> terms[0] + terms[1]
//                2 -> return addTerms2(model, terms[0].terms, terms[1].terms)
            }
            val tc = terms[0].termComparator
            require(terms.all { it.termComparator == tc }) { "The term comparators are different." }
            return addTermsAll(model, tc, terms.map { it.terms })
        }


        fun <T:Any> asRing(model : Ring<T>) : Ring<Multinomial<T>> = TODO()



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
    override val one: Multinomial<T> = Multinomial.constant(model.one, model)
    override val numberClass: Class<*>
        get() = Multinomial::class.java
}

open class MultinomialOnField<T : Any>(model: Field<T>) : MultinomialOnUnitRing<T>(model), IntegralDomain<Multinomial<T>> {

}


class MultinomialBuilderScope<T : Any>(val model: UnitRing<T>, val tc: TermComparator) {

    val x = ch("x")
    val y = ch("y")
    val z = ch("z")
    val w = ch("w")
    val a = ch("a")
    val b = ch("b")
    val c = ch("c")


    val one = Multinomial.constant(model.one, model, tc)
    val zero = Multinomial.zero(model, tc)

    fun of(vararg terms: Pair<T, String>): Multinomial<T> {
        return Multinomial.of(model, *terms, comp = tc)
    }

    fun ch(name: String): Multinomial<T> {
        return Multinomial.monomial(model.one, name, 1, model, tc)
    }

    val String.m: Multinomial<T>
        get() = ch(this)

    val T.m: Multinomial<T>
        get() = Multinomial.constant(this, model, tc)


    operator fun T.times(chs: String): Multinomial<T> {
        val term = MTerm.parse(this, chs)
        return Multinomial(model, listOf(term), tc)
    }

    operator fun T.times(m: Multinomial<T>): Multinomial<T> {
        return m.times(this)
    }

}