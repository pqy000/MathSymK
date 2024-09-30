package io.github.ezrnest.model

import io.github.ezrnest.ValueEquatable
import io.github.ezrnest.structure.*
import io.github.ezrnest.util.DataStructureUtil
import java.util.*
import java.util.function.Function
import java.util.regex.Pattern
import kotlin.Comparator


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
@JvmRecord
data class TermChs(val data: Array<ChPow>, val totalPow: Int = data.sumOf { it.pow }) {
    val size: Int
        get() = data.size

    // proxy methods
    operator fun get(index: Int): ChPow {
        return data[index]
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TermChs

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    operator fun contains(ch: String): Boolean {
        return data.binarySearch(ChPow(ch, 0), compareBy { it.ch }) >= 0
    }

    fun contains(other: TermChs, chComp: Comparator<String>): Boolean {
        var j = 0
        for (i in other.data.indices) {
            // find the corresponding character in other
            while (j < data.size && chComp.compare(data[j].ch, other.data[i].ch) < 0) {
                j++
            }
            if (j == data.size || chComp.compare(data[j].ch, other.data[i].ch) != 0) {
                return false
            }
            if (data[j].pow < other.data[i].pow) {
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
    fun exactDivide(other: TermChs): TermChs {
        val newChs = ArrayList<ChPow>(other.size)
        var j = 0
        val data = this.data
        val otherData = other.data
        for (i in otherData.indices) {
            // find the corresponding character in other
            while (j < size && data[j].ch < other[i].ch) {
                newChs.add(data[j]) // this character is not in the other term
                j++
            }
            if (j == data.size || data[j].ch != otherData[i].ch || data[j].pow < otherData[i].pow) {
                throw IllegalArgumentException("${data[i]} in $this is not in the other term ${other}.")
            }
            val r = data[j].pow - otherData[i].pow
            if (r != 0) {
                newChs.add(ChPow(data[j].ch, r))
            }
            j++
        }
        while (j < data.size) {
            newChs.add(data[j])
            j++
        }
        return TermChs(newChs.toTypedArray())
    }

    fun normalized(chComp: Comparator<String>): TermChs {
        val newChs = data.filter { it.pow != 0 }.toTypedArray()
        Arrays.sort(newChs) { o1, o2 -> chComp.compare(o1.ch, o2.ch) }
        return TermChs(newChs)
    }

    fun reorderedBy(chComp: Comparator<String>): TermChs {
        val newChs = data.copyOf()
        Arrays.sort(newChs) { o1, o2 -> chComp.compare(o1.ch, o2.ch) }
        return TermChs(newChs)
    }

    override fun toString(): String {
        return data.joinToString("") { it.toString() }
    }

    val isConstant: Boolean
        get() = data.isEmpty()

    companion object {
        val EMPTY = TermChs(emptyArray())

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
            t1: TermChs, t2: TermChs,
            chComparator: Comparator<String> = Comparator.naturalOrder()
        ): Int {
            if (t1.totalPow != t2.totalPow) {
                return t1.totalPow - t2.totalPow
            }
            return compareLex(t1, t2, chComparator)
        }

        fun getLexComparator(chComp: Comparator<String> = Comparator.naturalOrder()): MonomialOrder {
            return object : MonomialOrder(chComp) {
                override fun compare(o1: TermChs, o2: TermChs): Int {
                    return compareLex(o1, o2, chComp)
                }
            }
        }

        fun getGradedLexComparator(chComp: Comparator<String> = Comparator.naturalOrder()): MonomialOrder {
            return object : MonomialOrder(chComp) {
                override fun compare(o1: TermChs, o2: TermChs): Int {
                    return compareGradedLex(o1, o2, chComp)
                }
            }
        }

        fun getGradedRevLexComparator(chComp: Comparator<String> = Comparator.naturalOrder()): MonomialOrder {
            return getGradedLexComparator(chComp.reversed())
        }


        fun multiplyChars(chs1: TermChs, chs2: TermChs, chComp: Comparator<String>): TermChs {
            val newData = DataStructureUtil.mergeSorted2Arr(chs1.data, chs2.data,
                compFunc = { x, y -> chComp.compare(x.ch, y.ch) }, // compare by character
                merger2 = { x, y -> if (x.pow + y.pow == 0) null else ChPow(x.ch, x.pow + y.pow) })
            return TermChs(newData)
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
            val data = map.map { (k, v) -> ChPow(k, v) }.toTypedArray()
            return TermChs(data)
        }

        fun single(ch: String, pow: Int = 1): TermChs {
            require(pow >= 0) { "The power must be non-negative." }
            if (pow == 0) {
                return EMPTY
            }
            return TermChs(arrayOf(ChPow(ch, pow)))
        }
    }
}


abstract class MonomialOrder(val chOrder: Comparator<String>) : Comparator<TermChs>


typealias MTerm<T> = Term<T, TermChs>

/**
 *
 *
 *
 * ## Term (monomial) order
 *
 */
class Multinomial<T>
internal constructor(
    model: Ring<T>,
    /**
     * The list of terms in the ascending order given by [termComparator].
     * Terms with higher order come later in the list.
     */
    terms: List<MTerm<T>>,
    /**
     * The comparator used to order the terms and the characters.
     */
    termComparator: MonomialOrder
) : AbstractMultinomial<T, TermChs, MonomialOrder, Multinomial<T>>(model, terms, termComparator) {


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
     * @see termOrder
     */
    val leadTerm: MTerm<T>
        get() = terms.last()

    /**
     * Returns the leading term's characters, equivalent to `leadTerm.chs`.
     *
     * @see leadTerm
     */
    val leadChs: TermChs
        get() = leadTerm.key


    /*
    Term order
     */

    /**
     * Returns a new multinomial by reordering the terms using the given comparator.
     */
    fun reorderedBy(comp: MonomialOrder): Multinomial<T> {
        if (termOrder == comp) {
            return this
        }
        val chComp = comp.chOrder
        val newTerms = terms.map { Term(it.c, it.key.reorderedBy(chComp)) }
            .sortedWith(Comparator.comparing(Term<T, TermChs>::key, comp))
        return Multinomial(model, newTerms, comp)
    }

    /**
     * Creates a new multinomial from the given terms.
     * It is required that the terms are ordered by the term comparator.
     */
    override fun fromTerms(terms: List<Term<T, TermChs>>): Multinomial<T> {
        return Multinomial(model, terms, termOrder)
    }


    fun leadTermCompare(y: Multinomial<T>): Int {
        return termOrder.compare(leadTerm.key, y.leadTerm.key)
    }


    /*
    Math object
     */

    override fun toString(): String {
        val keyToString = TermChs::toString
        val valueToString = Any?::toString
        return stringOf(this.terms, isOneFromModel(model), isNegativeAndAbsFromModel(model), valueToString, keyToString)
    }


    override fun valueEquals(obj: ValueEquatable<T>): Boolean {
        if (obj !is Multinomial<T>) {
            return false
        }
        if (model != obj.model) {
            return false
        }
        val otherTerms = if (termOrder == obj.termOrder) obj.terms else obj.terms.sortedWith(comparatorTerm)
        for ((a, b) in terms.zip(otherTerms)) {
            if (a.key != b.key || !model.isEqual(a.c, b.c)) {
                return false
            }
        }
        return true
    }

    override fun <S> mapTo(newModel: EqualPredicate<S>, mapping: Function<T, S>): Multinomial<S> {
        require(newModel is Ring)
        val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
            val c = mapping.apply(t.c)
            if (newModel.isZero(c)) null else Term(c, t.key)
        }
        return Multinomial(newModel, newTerms, termOrder)
    }

    /*
    Term-wise operations
     */

    //    override fun keyMultiply(k1: TermChs, k2: TermChs): TermChs {
//        return TermChs.multiplyChars(k1, k2, termOrder.chOrder)
//    }
    override fun termMultiply(t1: Term<T, TermChs>, t2: Term<T, TermChs>): Term<T, TermChs>? {
        val c = model.multiply(t1.c, t2.c)
        if (model.isZero(c)) return null
        val chs = TermChs.multiplyChars(t1.key, t2.key, termOrder.chOrder)
        return Term(c, chs)
    }

    private fun times(t: MTerm<T>): Multinomial<T> {
        return mapTermsPossiblyZero { termMultiply(it, t) }
    }

    /*
    Multinomial as a ring
     */

    fun isUnit(): Boolean {
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        if (terms.size != 1) return false
        val t = terms[0]
        return t.key.isConstant && model.isUnit(t.c)
    }

    /*
    GCD
     */

    private fun MTerm<T>.contains(t: MTerm<T>): Boolean {
        return key.contains(t.key, termOrder.chOrder)
    }


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
            if (!p.leadTerm.contains(y.leadTerm)) {
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
                val leadTermP = p.leadTerm
                val leadTermF = f.leadTerm
                if (!leadTermP.contains(leadTermF)) {
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


    companion object {

        private fun <T> termExactDiv(model: UnitRing<T>, t1: MTerm<T>, t2: MTerm<T>): MTerm<T> {
            val c = model.exactDiv(t1.c, t2.c)
            val chs = t1.key.exactDivide(t2.key)
            return MTerm(c, chs)
        }

        /**
         * The default monomial order.
         */
        val DEFAULT_MONOMIAL_ORDER: MonomialOrder = TermChs.getLexComparator()

        /**
         * Returns a zero multinomial.
         */
        fun <T> zero(model: Ring<T>, comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER): Multinomial<T> {
            return Multinomial(model, emptyList(), comp)
        }

        /**
         * Creates a multinomial from a list of terms, possibly unordered and containing zero terms.
         */
        fun <T> fromTerms(
            terms: List<MTerm<T>>, model: Ring<T>, comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): Multinomial<T> {
            val filteredTerms = terms.mapNotNull {
                if (model.isZero(it.c)) null else MTerm(it.c, it.key.normalized(comp.chOrder))
            }
            val comparator = Comparator.comparing(MTerm<T>::key, comp)
            val mergedTerms = mergeTerms(model, comparator, filteredTerms)
            return Multinomial(model, mergedTerms, comp)
        }

        /**
         * Creates a multinomial from a list of terms.
         */
        fun <T> of(
            mc: Ring<T>,
            vararg terms: Pair<T, String>,
            comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): Multinomial<T> {
            return fromTerms(terms.map { MTerm(it.first, TermChs.parseChar(it.second)) }, mc, comp)
        }

        /**
         * Creates a constant multinomial.
         */
        fun <T> constant(c: T, model: Ring<T>, comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER): Multinomial<T> {
            return if (model.isZero(c)) {
                zero(model, comp)
            } else {
                Multinomial(model, listOf(MTerm(c, TermChs.EMPTY)), comp) // use a singleton array
            }
        }

        /**
         * Creates a multinomial `1`.
         */
        fun <T> one(model: UnitRing<T>, comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER): Multinomial<T> {
            return constant(model.one, model, comp)
        }

        /**
         * Creates a monomial from a term.
         */
        fun <T> monomial(
            t: MTerm<T>,
            model: Ring<T>,
            comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): Multinomial<T> {
            if (model.isZero(t.c)) {
                return zero(model)
            }
            val newT = MTerm(t.c, t.key.normalized(comp.chOrder))
            return Multinomial(model, listOf(newT), comp)
        }

        /**
         * Creates a monomial from a coefficient and a character.
         */
        fun <T> monomial(
            c: T, ch: String, pow: Int, model: Ring<T>,
            comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): Multinomial<T> {
            if (model.isZero(c)) {
                return zero(model, comp)
            }
            val term = MTerm(c, TermChs.single(ch, pow))
            return Multinomial(model, listOf(term), comp)
        }

        fun <T> monomialParse(
            c: T,
            chars: String,
            model: Ring<T>,
            comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): Multinomial<T> {
            val term = MTerm(c, TermChs.parseChar(chars))
            return Multinomial(model, listOf(term), comp)
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
//        inline fun <T, R> with(
//            model: UnitRing<T>, comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER,
//            action: MultinomialBuilderScope<T>.() -> R
//        ): R {
//            return action(MultinomialBuilderScope(model, comp))
//        }

        fun <T> parse(str: String, model: UnitRing<T>, comp: MonomialOrder = DEFAULT_MONOMIAL_ORDER) {
            TODO("Not yet implemented$str $model $comp")
        }

        fun getMonomialOrderLex(chComparator: Comparator<String>): MonomialOrder {
            return TermChs.getLexComparator(chComparator)
        }

        fun getMonomialOrderLexGraded(chComparator: Comparator<String>): MonomialOrder {
            return TermChs.getGradedLexComparator(chComparator)
        }

        /**
         * Sums a list of multinomials.
         */
        fun <T> sum(model: Ring<T>, vararg terms: Multinomial<T>): Multinomial<T> {
            return sum(model, terms.asList())
        }

        /**
         * Sums a list of multinomials.
         */
        fun <T> sum(model: Ring<T>, ms: List<Multinomial<T>>): Multinomial<T> {
            when (ms.size) {
                0 -> return zero(model)
                1 -> return ms[0]
                2 -> ms[0] + ms[1]
            }
            val m0 = ms[0]
            return m0.addTermsAll(ms.map { it.terms })
        }


        fun <T> over(
            model: Ring<T>,
            monomialOrder: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): MultinomialOverRing<T> {
            return MultinomialOverRing(model, monomialOrder)
        }

        fun <T> over(
            model: UnitRing<T>,
            monomialOrder: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): MultinomialOverUnitRing<T> {
            return MultinomialOverUnitRing(model, monomialOrder)
        }

        fun <T> over(
            model: Field<T>,
            monomialOrder: MonomialOrder = DEFAULT_MONOMIAL_ORDER
        ): MultinomialOverField<T> {
            return MultinomialOverField(model, monomialOrder)
        }


    }
}

open class MultinomialOverRing<T>(protected val _model: Ring<T>, val monomialOrder: MonomialOrder) :
    Ring<Multinomial<T>>, InclusionTo<T, Multinomial<T>> {

    open val model: Ring<T>
        get() = _model

    final override val zero: Multinomial<T> = Multinomial.zero(_model, monomialOrder)


    fun constant(c: T): Multinomial<T> {
        return Multinomial.constant(c, model, monomialOrder)
    }

    override fun include(t: T): Multinomial<T> {
        return constant(t)
    }

//    val T.const: Multinomial<T>
//        get() = Multinomial.constant(this, model, monomialOrder)


    override fun add(x: Multinomial<T>, y: Multinomial<T>): Multinomial<T> {
        return x + y
    }

    override fun negate(x: Multinomial<T>): Multinomial<T> {
        return -x
    }

    override fun multiply(x: Multinomial<T>, y: Multinomial<T>): Multinomial<T> {
        return x * y
    }


    override fun isEqual(x: Multinomial<T>, y: Multinomial<T>): Boolean {
        return x.valueEquals(y)
    }

    override fun contains(x: Multinomial<T>): Boolean {
        return x.terms.all { model.contains(it.c) }
    }

    override fun subtract(x: Multinomial<T>, y: Multinomial<T>): Multinomial<T> {
        return x - y
    }

    override fun multiplyN(x: Multinomial<T>, n: Long): Multinomial<T> {
        return x.times(n)
    }

    override fun isZero(x: Multinomial<T>): Boolean {
        return x.isZero
    }

    override fun sum(elements: List<Multinomial<T>>): Multinomial<T> {
        return Multinomial.sum(model, *elements.toTypedArray())
    }
}

open class MultinomialOverUnitRing<T>(_model: UnitRing<T>, order: MonomialOrder) :
    MultinomialOverRing<T>(_model, order),
    UnitRing<Multinomial<T>> {

    override val model: UnitRing<T>
        get() = _model as UnitRing<T>

    override val one: Multinomial<T> = Multinomial.constant(_model.one, _model)

    val x get() = ch("x")
    val y get() = ch("y")
    val z get() = ch("z")
    val w get() = ch("w")
    val a get() = ch("a")
    val b get() = ch("b")
    val c get() = ch("c")

    fun of(vararg terms: Pair<T, String>): Multinomial<T> {
        return Multinomial.of(model, *terms, comp = monomialOrder)
    }

    fun ch(name: String): Multinomial<T> {
        return Multinomial.monomialParse(model.one, name, model, monomialOrder)
    }

    fun monomial(name: String, pow: Int = 1): Multinomial<T> {
        return Multinomial.monomial(model.one, name, pow, model, monomialOrder)
    }

    val String.m get() = Multinomial.monomialParse(model.one, this, model, monomialOrder)


    operator fun T.times(chs: String): Multinomial<T> {
        val term = MTerm(this, TermChs.parseChar(chs))
        return Multinomial(model, listOf(term), monomialOrder)
    }

    val T.x get() = this * "x"
    val T.y get() = this * "y"
    val T.z get() = this * "z"
    val T.a get() = this * "a"
    val T.b get() = this * "b"
    val T.c get() = this * "c"
    val T.xy get() = this * "xy"
    val T.xz get() = this * "xz"
    val T.yz get() = this * "yz"
    val T.xyz get() = this * "xyz"

    operator fun T.times(m: Multinomial<T>): Multinomial<T> {
        return m.times(this)
    }

    override fun exactDiv(a: Multinomial<T>, b: Multinomial<T>): Multinomial<T> {
        val (q, r) = a.divideAndRemainder(b)
        if (!r.isZero) {
            throw ArithmeticException("The division is not exact.")
        }
        return q
    }


}

open class MultinomialOverField<T>(model: Field<T>, order: MonomialOrder) : MultinomialOverUnitRing<T>(model, order),
    IntegralDomain<Multinomial<T>>
