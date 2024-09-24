package io.github.ezrnest.model

import io.github.ezrnest.linear.Matrix
import io.github.ezrnest.linear.MutableMatrix
import io.github.ezrnest.model.Polynomial.Companion.compute
import io.github.ezrnest.model.Polynomial.Companion.computeGeneral
import io.github.ezrnest.structure.*
import io.github.ezrnest.util.DataStructureUtil
import java.util.Comparator

/**
 * Describes a term of a polynomial with a power and a value.
 */
@JvmRecord
data class PTerm<T>(val pow: Int, val value: T) : Comparable<PTerm<T>> {
    override fun toString(): String {
        return "($pow, $value)"
    }

    override fun compareTo(other: PTerm<T>): Int {
        return pow - other.pow
    }
}

/**
 * Represents a polynomial with coefficients of type [T].
 */
@JvmRecord
@ConsistentCopyVisibility
data class Polynomial<T> internal constructor(
    /**
     * A list of terms of this polynomial.
     */
    val terms: List<PTerm<T>>
) {

    /**
     * The degree of this polynomial, which is the highest power of `x` in this polynomial.
     * If this polynomial is zero, then the degree is `-1`.
     */
    val degree: Int
        get() = terms.lastOrNull()?.pow ?: -1

    /*
    Basic coefficient operations
     */

    /**
     * Returns the coefficient of the term with the specified [index].
     */
    fun getOrNull(index: Int): T? {
        //TODO better design
        return terms.binarySearchBy(index) { it.pow }.let {
            if (it >= 0) {
                terms[it].value
            } else {
                null
            }
        }
    }


//    /**
//     * Returns a list of coefficients of this polynomial.
//     * The coefficient of the term with index `i` is at the position `i` in the list.
//     * The size of the list is `degree + 1`.
//     * If the polynomial is zero, then the list is empty.
//     */
//    fun coefficientList(): List<T> {
//        val result = MutableList(degree + 1) { model.zero }
//        for ((index, value) in terms) {
//            result[index] = value
//        }
//        return result
//    }

    /**
     * Returns the leading term of this polynomial.
     *
     * The leading term is the non-zero term with the highest power of `x`.
     * If the polynomial is zero, then the leading term does not exist, and a `NoSuchElementException` will be thrown.
     *
     * @throws NoSuchElementException if this polynomial is zero.
     */
    val leadTerm: PTerm<T>
        get() = terms.last()

    /**
     * Returns the leading coefficient of this polynomial.
     *
     * @throws NoSuchElementException if this polynomial is zero.
     * @see leadTerm
     */
    val leadCoef: T
        get() = leadTerm.value


//    val constantTerm: PTerm<T>
//        get() = terms.firstOrNull() ?: PTerm(0, model.zero)


    val isZero: Boolean
        get() = terms.isEmpty()


    /**
     * Determines whether this polynomial is a constant polynomial, including the zero polynomial.
     */
    val isConstant: Boolean
        get() = degree <= 0


    /*
    MathObject
     */

    override fun toString(): String {
        return terms.asReversed().joinToString(separator = " + ") { "${it.value}x^${it.pow}" }
    }

    /*
    Coefficient-wise transformations
     */

    internal inline fun mapTermsNonZeroT(transform: (PTerm<T>) -> PTerm<T>): Polynomial<T> {
        return Polynomial(terms.map { transform(it) })
    }

    internal inline fun mapTermsNonZero(crossinline transform: (T) -> T): Polynomial<T> {
        return mapTermsNonZeroT { PTerm(it.pow, transform(it.value)) }
    }


//    /**
//     * Returns the greatest common divisor of this polynomial and another polynomial.
//     *
//     * It is required that the [model] is either a [Field] or a [UniqueFactorizationDomain].
//     */
//    override fun gcd(y: Polynomial<T>): Polynomial<T> {
//        if (model is Field) {
//            return super.gcd(y) // Euclidean Method
//        }
//        if (model is UniqueFactorizationDomain) {
//            return subResultantGCD(this, y)
//        }
//        throw UnsupportedOperationException("The model is not a field or a UFD.")
//    }
//
//    override fun gcdUV(y: Polynomial<T>): Triple<Polynomial<T>, Polynomial<T>, Polynomial<T>> {
//        require(model is Field<T>) { "The model is not a field." }
//        return EuclidDomainModel.gcdUVForModel(this, y, zero(model), one(model))
//    }
//
//
//    /**
//     * Returns the result `(q, r)` of dividing `this` by `y` and the remainder.
//     * It is guaranteed that `this = q * y + r` and `r.degree < y.degree`.
//     *
//     * It is required that either of the following conditions is satisfied:
//     * - The [model] is a [Field].
//     * - The [model] is a [UniqueFactorizationDomain] and every division is exact.
//     */
//    override fun divideAndRemainder(y: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
//        return divideAndRemainder(this, y, model as UnitRing)
//    }


    /*
    Extra algebraic operations for polynomials
     */


    companion object {


        fun <T> linear(model: AddMonoid<T>, a: T, b: T): Polynomial<T> {
            return Polynomial(listOf(PTerm(1, a), PTerm(0, b)))
        }

        fun <T> fromList(model: AddMonoid<T>, cs: List<T>): Polynomial<T> {
            val terms = ArrayList<PTerm<T>>(cs.size)
            for ((index, value) in cs.withIndex()) {
                if (!model.isZero(value)) {
                    terms.add(PTerm(index, value))
                }
            }
            return Polynomial(terms)
        }

        fun <T> of(model: AddMonoid<T>, vararg cs: T): Polynomial<T> {
            return fromList(model, cs.asList())
        }


        internal inline fun <T, M> computeGeneral(
            p: Polynomial<T>, x: M,
            zero: () -> M, inclusion: (T) -> M,
            add: (M, M) -> M, multiply: (M, M) -> M, pow: (M, Long) -> M,
        ): M {
            if (p.terms.isEmpty()) {
                return zero()
            }
            val terms = p.terms
            var term = terms.last()
            var power = term.pow
            var result = inclusion(term.value)
            var pos = terms.lastIndex
            while (--pos >= 0) {
                term = terms[pos]
                val pDiff = power - term.pow
                result = if (pDiff == 1) {
                    multiply(result, x)
                } else {
                    multiply(result, pow(x, pDiff.toLong()))
                }
                result = add(result, inclusion(term.value))
                power = term.pow
            }
            if (power > 0) {
                result = multiply(result, pow(x, power.toLong()))
            }
            return result
        }

        fun <T, M> compute(p: Polynomial<T>, x: M, model: UnitRingModule<T, M>): M {
            return computeGeneral(p, x, model::zero, model::fromScalar, model::add, model::multiply, model::power)
        }


        /*
        Number models
         */

        /**
         * Returns a `Ring<Polynomial<T>>` over a `Ring<T>`.
         *
         * @see [Ring]
         */
        fun <T> over(model: Ring<T>): PolyOverRing<T> {
            return PolyOverRing(model)
        }

        /**
         * Returns a `UnitRing<Polynomial<T>>` over a `Ring<T>`.
         *
         * @see [UnitRing]
         */
        fun <T> over(model: UnitRing<T>): PolyOverUnitRing<T> {
            return PolyOverUnitRing(model)
        }

        /**
         */
        fun <T> over(model: UniqueFactorizationDomain<T>): PolyOverUFD<T> {
            return PolyOverUFD(model)
        }

        /**
         * Returns a `EuclideanDomain<Polynomial<T>>` over a `Field<T>`.
         *
         * @see EuclideanDomain
         * @see Field
         */
        fun <T> over(model: Field<T>): PolyOverField<T> {
            return PolyOverField(model)
        }


        /*
        Methods for number theory
         */


    }
}

interface PolyOps<T> : EqualPredicate<Polynomial<T>> {

    val model: AddMonoid<T>

    /**
     * Returns the coefficient of the term with the specified [index].
     */
    operator fun Polynomial<T>.get(index: Int): T {
        return terms.binarySearchBy(index) { it.pow }.let {
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
     * The size of the list is `degree + 1`.
     * If the polynomial is zero, then the list is empty.
     */
    fun Polynomial<T>.coefficientList(): List<T> {
        val result = MutableList(degree + 1) { model.zero }
        for ((index, value) in terms) {
            result[index] = value
        }
        return result
    }

    /**
     * Gets the coefficient of the constant term of this polynomial.
     */
    val Polynomial<T>.constantCoef: T
        get() = terms.firstOrNull()?.value ?: model.zero

    val zero: Polynomial<T>

    /**
     * Returns a constant polynomial.
     */
    fun constant(c: T): Polynomial<T> {
        if (model.isZero(c)) return zero
        return Polynomial(listOf(PTerm(0, c)))
    }


    /**
     * Returns a polynomial `ax + b`.
     */
    fun linear(a: T, b: T): Polynomial<T> {
        return fromList(listOf(b, a))
    }

    fun quadratic(a: T, b: T, c: T): Polynomial<T> {
        return fromList(listOf(c, b, a))
    }

    fun xpow(p: Int, a: T): Polynomial<T> {
        if (model.isZero(a)) return zero
        return Polynomial(listOf(PTerm(p, a)))
    }

    infix fun T.xpow(p: Int): Polynomial<T> {
        return xpow(p, this)
    }


    val T.x: Polynomial<T> get() = xpow(1, this)

    val T.x2: Polynomial<T>
        get() = xpow(2, this)

    val T.x3: Polynomial<T>
        get() = xpow(3, this)

    val T.x4: Polynomial<T>
        get() = xpow(4, this)


    /**
     * Creates a polynomial from a list of coefficients.
     * The index of the coefficient corresponds to the power of `x`:
     *
     *     cs[i] * x^i
     *
     * For example, the list `[1, 2, 3]` corresponds to the polynomial `1 + 2x + 3x^2`.
     *
     * @return a polynomial with the specified coefficients.
     */
    fun fromList(cs: List<T>): Polynomial<T> {
        return Polynomial.fromList(model, cs)
    }


    /**
     * Creates a polynomial from the coefficients.
     * The first coefficient corresponds to the constant term, the second coefficient corresponds to the coefficient of `x`, and so on.
     *
     *
     * @see fromList
     */
    fun poly(vararg coef: T): Polynomial<T> {
        return fromList(coef.asList())
    }


    override fun isEqual(x: Polynomial<T>, y: Polynomial<T>): Boolean {
        if (x.terms.size != y.terms.size) return false
        for (i in x.terms.indices) {
            val tx = x.terms[i]
            val ty = y.terms[i]
            if (tx.pow != ty.pow) return false
            if (!model.isEqual(tx.value, ty.value)) return false
        }
        return true
    }

    fun Polynomial<T>.shiftRight(shift: Int): Polynomial<T> {
        if (isZero || shift == 0) return this
        if (shift > 0) {
            return Polynomial(terms.map { PTerm(it.pow + shift, it.value) })
        }
        val remainingTerms = terms.mapNotNull { term ->
            val newPow = term.pow + shift
            if (newPow < 0) {
                null
            } else {
                PTerm(newPow, term.value)
            }
        }
        return Polynomial(remainingTerms)
    }

    /**
     * Returns the sylvester matrix of this and g.
     * It is required that `this` and `g` must not be zero at the same time.
     *
     * Let `this` be `a_n x^n + a_{n-1} x^{n-1} + ... + a_1 x + a_0` and `g` be `b_m x^m + b_{m-1} x^{m-1} + ... + b_1 x + b_0`.
     * Then, the sylvester matrix of `this` and `g` is a square matrix whose size is `n + m`:
     *
     *     [ a_n a_{n-1} ... a_1 a_0 0       ... 0   ]
     *     [ 0   a_n     ... a_2 a_1 a_0     ... 0   ]
     *     ...
     *     [ 0   0       ...     a_m a_{m-1} ... a_0 ] (the m-th row)
     *     [ b_m b_{m-1} ... b_1 b_0 0       ... 0   ]
     *     [ 0   b_m     ... b_2 b_1 b_0     ... 0   ]
     *     ...
     *     [ 0   0       ... 0   b_n b_{n-1} ... b_0 ] (the (m+n)-th row)
     *
     *
     * @return a square matrix whose size is `this.degree + g.degree`.
     */
    fun syvlesterMatrix(f: Polynomial<T>, g: Polynomial<T>): Matrix<T> {
        require(f.degree >= 0 || g.degree >= 0) { "Both polynomials are zero." }
        val n = f.degree
        val m = g.degree
        val M = MutableMatrix.zero(n + m, model)
        for (t in f.terms) {
            for (i in 0 until m) {
                M[i, n - t.pow + i] = t.value
            }
        }
        for (t in g.terms) {
            for (i in 0 until n) {
                M[m + i, m - t.pow + i] = t.value
            }
        }
        return M
    }
}


open class PolyOverRing<T>(protected val modelRing: Ring<T>) :
    PolyOps<T>,
    Ring<Polynomial<T>>,
    Module<T, Polynomial<T>>,
    InclusionTo<T, Polynomial<T>> {

    override val model: Ring<T>
        get() = modelRing

    final override val zero: Polynomial<T> = Polynomial(emptyList())

    override val scalars: Ring<T>
        get() = model


    val T.p: Polynomial<T>
        get() = constant(this)

    override fun include(t: T): Polynomial<T> {
        return constant(t)
    }


    fun Polynomial<T>.format(ch: String, bracket: Boolean = false): String {
        if (isZero) return "0"
        val isOne = AbstractMultinomial.isOneFromModel(model)
        val isNegativeAndAbs = AbstractMultinomial.isNegativeAndAbsFromModel(model)
        val transform = if (bracket) { t: T -> "($t)" } else { t: T -> t.toString() }
        return stringOf(this, ch, isOne, isNegativeAndAbs, transform)
    }

    /**
     * Apply this polynomial to a value `x` and returns the result.
     */
    fun Polynomial<T>.apply(x: T): T {
        return computeGeneral(this, x, model::zero, { it }, model::add, model::multiply, model::power)
    }

    fun <M> Polynomial<T>.substitute(x: M, model: UnitRingModule<T, M>): M {
        return compute(this, x, model)
    }


    /*
    Polynomial as a ring
     */


    override fun isZero(x: Polynomial<T>): Boolean {
        return x.isZero
    }

    override fun scalarMul(k: T, v: Polynomial<T>): Polynomial<T> {
        if (model.isZero(k)) {
            return zero
        }
        return mapTermsPossiblyZero(v.terms, model) { model.multiply(it, k) }
    }

    /**
     * Adds two terms with the same power.
     */
    private fun add2Term(a: PTerm<T>, b: PTerm<T>): PTerm<T>? {
        val r = model.add(a.value, b.value)
        return if (model.isZero(r)) null else PTerm(a.pow, r)
    }

    /**
     * Adds multiple terms with the same power.
     */
    private fun addMultiTerm(list: List<PTerm<T>>, tempList: ArrayList<T>): PTerm<T>? {
        tempList.clear()
        list.mapTo(tempList) { it.value }
        val sum = model.sum(tempList)
        return if (model.isZero(sum)) null else PTerm(list[0].pow, sum)
    }


    /**
     * Merges an unordered list of terms into a polynomial.
     */
    protected fun mergeTerms(
        rawTerms: List<PTerm<T>>,
        maxMergeSizeEst: Int = 3,
        estimatedSize: Int = rawTerms.size
    ): List<PTerm<T>> {
        val tempList = ArrayList<T>(maxMergeSizeEst)
        return DataStructureUtil.mergeRawList(
            rawTerms,
            comparator = Comparator.naturalOrder(),
            merger2 = ::add2Term,
            mergerMulti = { list -> addMultiTerm(list, tempList) },
            estimatedSize = estimatedSize
        )
    }


    protected fun addTermsAll(termsList: List<List<PTerm<T>>>): Polynomial<T> {
        val tempList = ArrayList<T>(termsList.size)
        val resultTerms = DataStructureUtil.mergeSortedK(
            termsList,
            comparator = Comparator.naturalOrder(),
            merger2 = ::add2Term,
            mergerMulti = { list -> addMultiTerm(list, tempList) }
        )
        return Polynomial(resultTerms)
    }


    final override fun add(x: Polynomial<T>, y: Polynomial<T>): Polynomial<T> {
        val result = DataStructureUtil.mergeSorted2(
            x.terms, y.terms,
            comparator = Comparator.naturalOrder(),
            merger2 = ::add2Term,
        )
        return Polynomial(result)
    }

    final override fun sum(elements: List<Polynomial<T>>): Polynomial<T> {
        return when (elements.size) {
            0 -> zero
            1 -> elements[0]
            2 -> add(elements[0], elements[1])
            else -> addTermsAll(elements.map { it.terms })
        }
    }

    final override fun negate(x: Polynomial<T>): Polynomial<T> {
        val model = model
        return x.mapTermsNonZero { model.negate(it) }
    }

    final override fun multiply(x: Polynomial<T>, y: Polynomial<T>): Polynomial<T> {
        if (x.isZero || y.isZero) return zero
        val a = x.terms
        val b = y.terms
        val result = ArrayList<PTerm<T>>(a.size * b.size)
        val model = model
        for (ai in a) {
            for (bj in b) {
                val index = ai.pow + bj.pow
                val value = model.multiply(ai.value, bj.value)
                if (!model.isZero(value)) {
                    result.add(PTerm(index, value))
                }
            }
        }
        val resTerms = mergeTerms(result, estimatedSize = a.size + b.size)
        return Polynomial(resTerms)
    }


    override fun contains(x: Polynomial<T>): Boolean {
        val model = model
        return x.terms.all { model.contains(it.value) }
    }

    override fun multiplyLong(x: Polynomial<T>, n: Long): Polynomial<T> {
        return x.times(n)
    }


    operator fun T.plus(x: Polynomial<T>): Polynomial<T> {
        return add(constant(this), x)
    }

    operator fun Polynomial<T>.plus(t: T): Polynomial<T> {
        return add(this, constant(t))
    }

    operator fun T.minus(x: Polynomial<T>): Polynomial<T> {
        return subtract(constant(this), x)
    }

    operator fun Polynomial<T>.minus(t: T): Polynomial<T> {
        return subtract(this, constant(t))
    }

    operator fun T.times(x: Polynomial<T>): Polynomial<T> {
        return scalarMul(this, x)
    }

    operator fun Polynomial<T>.times(t: T): Polynomial<T> {
        return scalarMul(t, this)
    }


    /**
     * Returns the formal derivative of this polynomial.
     *
     * The formal derivative of a polynomial
     *
     *      a_n x^n + a_{n-1} x^{n-1} + ... + a_1 x + a_0
     *
     * is
     *
     *     n a_n x^{n-1} + (n-1) a_{n-1} x^{n-2} + ... + a_1.
     *
     *
     */
    fun Polynomial<T>.derivative(): Polynomial<T> {
        if (isConstant) return zero

        val nonConstantTerms = if (terms[0].pow == 0) terms.subList(1, terms.size) else terms
        return mapTermsPossiblyZeroT(nonConstantTerms, model) { t ->
            PTerm(t.pow - 1, model.multiplyLong(t.value, t.pow.toLong()))
        }
    }

    internal companion object {


        private fun <T> stringOf(
            p: Polynomial<T>, ch: String,
            isOne: (T) -> Boolean, isNegativeAndAbs: (T) -> Pair<Boolean, T>,
            transform: (T) -> String,
            highOrderFirst: Boolean = true
        ): String {
            if (p.isZero) return "0"
            val terms = if (highOrderFirst) p.terms.asReversed() else p.terms
            val sb = StringBuilder()
            for (element in terms) {
                val (isNeg, v) = isNegativeAndAbs(element.value)
                if (sb.isEmpty()) {
                    if (isNeg) sb.append("-")
                } else {
                    if (isNeg) {
                        sb.append(" - ")
                    } else {
                        sb.append(" + ")
                    }
                }

                val valueString = transform(v)
                val pow = element.pow
                if (pow == 0) {
                    sb.append(valueString)
                } else {
                    if (isOne(v)) {
                        sb.append(ch)
                    } else {
                        sb.append(valueString).append("*").append(ch)
                    }
                    if (pow != 1) {
                        sb.append("^").append(pow)
                    }
                }
            }
            return sb.toString()
        }

        inline fun <T, R> mapTermsPossiblyZeroT(
            terms: List<PTerm<T>>,
            model: Ring<R>,
            transform: (PTerm<T>) -> PTerm<R>
        ): Polynomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val t2 = transform(t)
                if (model.isZero(t2.value)) null else t2
            }
            return Polynomial(newTerms)
        }

        inline fun <T, R> mapTermsPossiblyZero(
            terms: List<PTerm<T>>,
            model: Ring<R>,
            transform: (T) -> R
        ): Polynomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val v = transform(t.value)
                if (model.isZero(v)) null else PTerm(t.pow, v)
            }
            return Polynomial(newTerms)
        }
    }
}

open class PolyOverUnitRing<T>(_model: UnitRing<T>) : PolyOverRing<T>(_model), UnitRing<Polynomial<T>> {
    override val model: UnitRing<T> = _model

    override val one: Polynomial<T>
        get() = constant(model.one)

    val x: Polynomial<T>
        get() = xpow(1, model.one)

    val x2: Polynomial<T>
        get() = xpow(2, model.one)

    val x3: Polynomial<T>
        get() = xpow(3, model.one)

    val x4: Polynomial<T>
        get() = xpow(4, model.one)


    override fun exactDiv(a: Polynomial<T>, b: Polynomial<T>): Polynomial<T> {
        val (q, r) = divideAndRemainder0(a, b)
        if (!r.isZero) {
            throw ArithmeticException("Not exact division.")
        }
        return q
    }

    fun Polynomial<T>.scalarExactDiv(k: T): Polynomial<T> {
        val model = model
        return mapTermsPossiblyZero(terms, model) { model.exactDiv(it, k) }
    }

    /**
     * Divides this polynomial by a number to get a new polynomial whose leading coefficient is one.
     */
    fun Polynomial<T>.toMonic(): Polynomial<T> {
        if (isZero) {
            return this
        }
        val c = leadCoef
        if (model.isOne(c)) return this
        return scalarExactDiv(c)
    }

    override fun isUnit(x: Polynomial<T>): Boolean {
        return x.degree == 0 && model.isUnit(x.constantCoef)
    }

    /**
     * Performs the pseudo division of two polynomials only over a ring.
     * This algorithm finds `Q` and `R` such that
     * ```
     *     d^(A.degree - B.degree + 1) A = BQ + R     and     R.degree < B.degree.
     * ```
     * It is required that `B` is not zero and
     * `A.degree >= B.degree`.
     *
     * @param T the calculator for [T] should at least be a ring calculator.
     */
    @Suppress("LocalVariableName")
    fun pseudoDivision(A: Polynomial<T>, B: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
        require(!B.isZero)
        /*
        See Algorithm 3.1.2, page 112 of
        'A Course in Computational Algebraic Number Theory', Henri Cohen
        Created by lyc at 2020-03-01 14:25
         */
        val m = A.degree
        val n = B.degree
        require(m >= n)
        val model = model
        val d = B.leadCoef
        var R = A
        var Q = zero
        var e = m - n + 1
        while (!R.isZero && R.degree >= B.degree) {
            val S = xpow(R.degree - B.degree, R.leadCoef)
            Q = Q * d + S
            R = R * d - S * B
            e -= 1
        }
        val q = model.power(d, e.toLong())
        Q *= q
        R *= q
        return Pair(Q, R)
    }

    /**
     * Performs the pseudo division of two polynomials only over a ring.
     * This algorithm finds `Q` and `R` such that
     * ```
     *     d^(A.degree - B.degree + 1) A = BQ + R     and     R.degree < B.degree,
     * ```
     * where `d` is the leading coefficient of `B`.
     *
     * It is required that `B` is not zero and
     * `A.degree >= B.degree`.
     *
     * @param T the calculator for [T] should at least be a ring calculator.
     */
    @Suppress("LocalVariableName")
    fun pseudoDivRem(A: Polynomial<T>, B: Polynomial<T>): Polynomial<T> {
        require(!B.isZero)
        /*
        See Algorithm 3.1.2, page 112 of
        'A Course in Computational Algebraic Number Theory', Henri Cohen
        Created by lyc at 2020-03-01 14:25
         */
        val model = model
        val m = A.degree
        val n = B.degree
        require(m >= n)
        val d = B.leadCoef
        var R = A
        var e = m - n + 1
        while (!R.isZero && R.degree >= B.degree) {
            val S = R.leadCoef xpow (R.degree - B.degree)
            R = R * d - S * B
            e -= 1
        }
        val q = model.power(d, e.toLong())
        R *= q
        return R
    }

    protected fun divideAndRemainder0(x: Polynomial<T>, y: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
        if (y.isZero) throw ArithmeticException("Division by zero")
        if (x.isZero) return x to x
        val model = model
        var remainder = x
        val quotientTerms = mutableListOf<PTerm<T>>()
        val leadTermY = y.leadTerm
        while (!remainder.isZero && remainder.degree >= y.degree) {
            val leadTermR = remainder.leadTerm
            val q = model.exactDiv(leadTermR.value, leadTermY.value)
            val leadPowQuotient = leadTermR.pow - leadTermY.pow
            val quotientTerm = PTerm(leadPowQuotient, q)
            quotientTerms.add(quotientTerm)
            val subtrahend = y.mapTermsNonZeroT { PTerm(it.pow + leadPowQuotient, model.eval { -it.value * q }) }
            remainder = add(remainder, subtrahend)
        }
        val quotient = Polynomial(quotientTerms.reversed())
        return quotient to remainder
    }
}

open class PolyOverUFD<T>(override val model: UniqueFactorizationDomain<T>) :
    PolyOverUnitRing<T>(model),
    UniqueFactorizationDomain<Polynomial<T>> {


    /**
     * Computes the GCD of two polynomials over a UFD.
     *
     * It is required that the underlying model is [UniqueFactorizationDomain].
     *
     * @see [subResultantGCD]
     */
    fun primitiveGCD(f: Polynomial<T>, g: Polynomial<T>): Polynomial<T> {
        if (f.isZero) return g
        if (g.isZero) return f

        /*
        See Algorithm 3.2.10, page 117 of
        'A Course in Computational Algebraic Number Theory', Henri Cohen
        Created by lyc at 2020-03-01 16:02
         */
        val mc = model
        var (A, B) = if (f.degree > g.degree) f to g else g to f

        val a = A.cont()
        val b = B.cont()
        A = A.scalarExactDiv(a)
        B = B.scalarExactDiv(b)
        while (true) {
            val R = pseudoDivRem(A, B)
            if (R.isZero) {
                break
            }
            if (R.isConstant) {
                B = one
                break
            }
            A = B
            B = R.toPrimitive()
        }
        val d = mc.gcd(a, b)
        return B * d
    }

    /**
     * Computes the GCD of two polynomials over a UFD using sub-resultant method.
     *
     * @see [primitiveGCD]
     */
    @Suppress("LocalVariableName")
    fun subResultantGCD(f: Polynomial<T>, g: Polynomial<T>): Polynomial<T> {
        /*
        See Algorithm 3.3.1, page 118 of
        'A Course in Computational Algebraic Number Theory', Henri Cohen
        Created by lyc at 2020-03-01 16:02
         */
        var (A, B) = if (f.degree > g.degree) f to g else g to f // A.degree >= B.degree
        if (B.isZero) return A

        val model = this.model
        val a = A.cont()
        val b = B.cont()
        val d = model.gcd(a, b)
        var g1 = model.one
        var h1 = model.one
        while (true) {
            val t = (A.degree - B.degree).toLong()
            val R = pseudoDivRem(A, B)
            if (R.isZero) break
            if (R.isConstant) {
                B = one
                break
            }
            A = B
            B = model.eval { R.scalarExactDiv(g1 * (h1 pow t)) }
            g1 = A.leadCoef
            h1 = model.eval { h1 * ((g1 / h1) pow t) }
        }
        return B.toPrimitive() * d
    }


    override fun gcd(a: Polynomial<T>, b: Polynomial<T>): Polynomial<T> {
        return subResultantGCD(a, b)
    }


    override fun isExactDiv(a: Polynomial<T>, b: Polynomial<T>): Boolean {
        return divideAndRemainder0(a, b).second.isZero
    }

    /**
     * Returns the `gcd` of all coefficients of this polynomial, which is referred to as the content of this polynomial.
     *
     * It is required that the [model] is a [EuclideanDomain].
     *
     * @return the greatest common divisor of all coefficients of this polynomial.
     */
    fun Polynomial<T>.cont(): T {
        if (isZero) return model.zero

        return terms.fold(model.zero) { acc, term ->
            model.gcd(acc, term.value)
        }
    }

    /**
     * Returns the primitive part of this polynomial, which is the polynomial obtained by dividing this polynomial by its content.
     *
     * The primitive part is a polynomial whose content is one.
     *
     * @return the primitive part of this polynomial.
     * @see cont
     */
    fun Polynomial<T>.toPrimitive(): Polynomial<T> {
        if (isZero) return this

        return scalarExactDiv(cont())
    }


}


open class PolyOverField<T>(override val model: Field<T>) : PolyOverUFD<T>(model),
    EuclideanDomain<Polynomial<T>>, UnitAlgebra<T, Polynomial<T>> {

    override val scalars: Field<T>
        get() = model

    override fun scalarDiv(x: Polynomial<T>, k: T): Polynomial<T> {
        return x.mapTermsNonZero { model.divide(it, k) }
    }

    operator fun Polynomial<T>.div(k: T): Polynomial<T> = scalarDiv(this, k)

    override fun gcd(a: Polynomial<T>, b: Polynomial<T>): Polynomial<T> {
        return super<PolyOverUFD>.gcd(a, b)
    }

    override fun exactDiv(a: Polynomial<T>, b: Polynomial<T>): Polynomial<T> {
        return super<PolyOverUFD>.exactDiv(a, b)
    }

    override fun divideAndRem(a: Polynomial<T>, b: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
        return divideAndRemainder0(a, b)
    }

    override fun isExactDiv(a: Polynomial<T>, b: Polynomial<T>): Boolean {
        return super<PolyOverUFD>.isExactDiv(a, b)
    }

    /**
     * Determines whether this polynomial and another polynomial have a common root.
     */
    fun hasCommonRoot(x: Polynomial<T>, y: Polynomial<T>): Boolean {
        return !gcd(x, y).isConstant
    }


    /*
    Calculus operations
     */


    /**
     * Returns the formal indefinite integral of this polynomial, with the constant of integration being zero.
     *
     * The indefinite integral of a polynomial
     *
     *     a_n x^n + a_{n-1} x^{n-1} + ... + a_1 x + a_0
     *
     * is
     *
     *     a_n/(n+1) x^{n+1} + a_{n-1}/n x^{n} + ... + a_1/2 x^2 + a_0 x.
     *
     *
     * It is required that the [model] is a [Field].
     */
    fun Polynomial<T>.integral(): Polynomial<T> {
        if (isZero) return zero
        return mapTermsPossiblyZeroT(terms, model) { t ->
            PTerm(t.pow + 1, model.divideLong(t.value, (t.pow + 1).toLong()))
        }
    }

    /**
     * Returns the resultant `f` and `g` over a field.
     *
     * The resultant of two polynomials `f` and `g` is a scalar (an element of the field) such that
     * the resultant of `f` and `g` is zero if and only if `f` and `g` have a common root.
     *
     *
     *
     */
    fun resultant(f: Polynomial<T>, g: Polynomial<T>): T {
        /*
        Refer to Ex 3.6.11 of 'A Course in Computational Algebraic Number Theory', Henri Cohen
         */
        // Check if either polynomial is zero
        if (f.isZero || g.isZero) {
            return model.zero
        }

        var h = f
        var s = g
        var res = model.one
        val model = model

        while (s.degree > 0) {
            // Compute the remainder r of h divided by s
            val (_, r) = divideAndRem(h, s)

            val degH = h.degree
            val degS = s.degree
            val degR = if (r.isZero) -1 else r.degree

            val pow = degH - degR
            val lcS = s.leadCoef

            // Compute (-1)^(deg(h) * deg(s))
            val degProduct = degH * degS
            val sign = if (degProduct % 2 == 0) model.one else model.negate(model.one)

            // Compute LC(s)^(deg(h) - deg(r))
            val lcSPow = model.power(lcS, pow.toLong())

            // Update the resultant
            res = model.multiply(model.multiply(sign, lcSPow), res)

            // Update h and s for the next iteration
            h = s
            s = r
        }

        // After the loop, check if h or s is zero
        if (h.isZero || s.isZero) {
            res = model.zero
        } else if (h.degree > 0) {
            // Multiply res by LC(s)^(deg(h))
            val lcS = s.leadCoef
            val degH = h.degree
            val lcSPow = model.power(lcS, degH.toLong())
            res = model.multiply(lcSPow, res)
        }

        return res
    }
}


//fun main() {
////    val Z = NumberModels.intModP(97)
////    val p = Polynomial.of(Z, -1, 2, 3, -6)
////    val q = Polynomial.of(Z, 1, 2, 3)
////    println(p.sylvesterMatrix(q).det())
////    println(Polynomial.resultant(p, q, Z))
//}