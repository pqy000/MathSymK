package cn.mathsymk.model

import cn.mathsymk.AbstractMathObject
import cn.mathsymk.IMathObject
import cn.mathsymk.function.MathOperator
import cn.mathsymk.model.struct.AlgebraModel
import cn.mathsymk.model.struct.EuclidDomainModel
import cn.mathsymk.model.struct.times
import cn.mathsymk.structure.*
import cn.mathsymk.util.DataStructureUtil
import cn.mathsymk.util.ModelPatterns
import java.util.Comparator
import java.util.function.Function

/**
 * Describes a term of a polynomial with a power and a value.
 */
data class PTerm<T : Any>(val pow: Int, val value: T) : Comparable<PTerm<T>> {
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
class Polynomial<T : Any> internal constructor(
    model: Ring<T>,
    /**
     * A list of terms of this polynomial.
     */
    val terms: List<PTerm<T>>
) : AbstractMathObject<T, Ring<T>>(model),
    AlgebraModel<T, Polynomial<T>>, EuclidDomainModel<Polynomial<T>>, MathOperator<T> {

    /**
     * The degree of this polynomial, which is the highest power of `x` in this polynomial.
     * If this polynomial is zero, then the degree is `-1`.
     */
    val degree: Int
        get() = terms.lastOrNull()?.pow ?: -1

    init {

    }


    /*
    Basic coefficient operations
     */

    /**
     * Returns the coefficient of the term with the specified [index].
     */
    operator fun get(index: Int): T {
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
    fun coefficientList(): List<T> {
        val result = MutableList(degree + 1) { model.zero }
        for ((index, value) in terms) {
            result[index] = value
        }
        return result
    }

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

    val constantCoef: T
        get() = terms.firstOrNull()?.value ?: model.zero


    override val isZero: Boolean
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
        if (isZero) {
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
        if (terms != other.terms) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        for (term in terms) {
            result = 31 * result + term.hashCode()
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
            if (t1.pow != t2.pow || !model.isEqual(t1.value, t2.value)) {
                return false
            }
        }
        return true
    }

    override fun <N : Any> mapTo(newCalculator: EqualPredicate<N>, mapper: Function<T, N>): Polynomial<N> {
        val newModel = newCalculator as Ring<N>
        return mapTermsPossiblyZero(terms, newModel) { mapper.apply(it) }
    }


    /*
    Polynomial methods
     */

    /**
     * Apply this polynomial to a value `x` and returns the result.
     */
    override fun apply(x: T): T {
        if (terms.isEmpty()) {
            return model.zero
        }
        var term = terms.last()
        var power = term.pow
        var result = term.value
        var pos = terms.lastIndex
        while (--pos >= 0) {
            term = terms[pos]
            val pDiff = power - term.pow
            result = if (pDiff == 1) {
                model.eval { result * x }
            } else {
                model.eval { result * x.pow(pDiff.toLong()) }
            }
            result = model.add(result, term.value)
            power = term.pow
        }
        if (power > 0) {
            result = model.eval { result * x.pow(power.toLong()) }
        }
        return result
    }

//    fun apply(x : Module<T>)


    /*
    Coefficient-wise transformations
     */

    private inline fun mapTermsNonZeroT(transform: (PTerm<T>) -> PTerm<T>): Polynomial<T> {
        return Polynomial(model, terms.map { transform(it) })
    }

    private inline fun mapTermsNonZero(crossinline transform: (T) -> T): Polynomial<T> {
        return mapTermsNonZeroT { PTerm(it.pow, transform(it.value)) }
    }

    override fun times(k: T): Polynomial<T> {
        if (model.isZero(k)) {
            return Polynomial(model, emptyList())
        }
        return mapTermsPossiblyZero(terms, model) { model.multiply(it, k) }
    }

    override fun unaryMinus(): Polynomial<T> {
        return mapTermsNonZero { model.negate(it) }
    }

    fun shiftRight(shift: Int): Polynomial<T> {
        if (isZero) {
            return this
        }
        if (shift == 0) {
            return this
        }
        if (shift > 0) {
            return Polynomial(model, terms.map { PTerm(it.pow + shift, it.value) })
        }
        val remainingTerms = terms.mapNotNull { term ->
            val newPow = term.pow + shift
            if (newPow < 0) {
                null
            } else {
                PTerm(newPow, term.value)
            }
        }
        return Polynomial(model, remainingTerms)
    }

//    fun timesSingle(pow : Int, c : T) : Polynomial<T> {
//        if (model.isZero(c)) {
//            return zero(model)
//        }
//        val newTerms = map
//    }

    /*
    Polynomial as a ring
     */


    override operator fun plus(y: Polynomial<T>): Polynomial<T> {
        return addTerms2(model, terms, y.terms)
    }

    operator fun plus(t: T): Polynomial<T> {
        if (model.isZero(t)) return this
        return addTerms2(model, terms, listOf(PTerm(0, t)))
    }

//    override operator fun minus(y: Polynomial<T>): Polynomial<T> {
//        return addTerms2(model, terms, y.terms.map { PTerm(it.pow, model.negate(it.value)) })
//    }

    override operator fun times(y: Polynomial<T>): Polynomial<T> {
        return multiplyTerms(model, terms, y.terms)
    }

    override fun times(n: Long): Polynomial<T> {
        return if (n == 0L) {
            zero(model)
        } else {
            mapTermsPossiblyZero(terms, model) { model.multiplyLong(it, n) }
        }
    }


    /*
    Polynomial on UnitRing
     */


    override fun isUnit(): Boolean {
        require(model is UnitRing<T>) { "The model is not a unit ring." }
        return degree == 0 && model.isUnit(terms[0].value)
    }

    override fun pow(n: Long): Polynomial<T> {
        require(n >= 0) { "The power must be non-negative." }
        if (n == 0L) {
            require(model is UnitRing<T>) { "The model is not a unit ring." }
            return one(model)
        }
        return ModelPatterns.binaryProduce(n, this) { a, b -> a * b }
    }

    override fun div(k: T): Polynomial<T> {
        if (model.isZero(k)) {
            throw ArithmeticException("Division by zero")
        }
        require(model is UnitRing<T>) { "The model is not a unit ring." }

        return mapTermsNonZero { model.exactDivide(it, k) }
        // it is not possible to get a zero term
    }

    /**
     * Divides this polynomial by a number to get a new polynomial whose leading coefficient is one.
     */
    fun toMonic(): Polynomial<T> {
        if (isZero) {
            return this
        }
        val c = terms.last().value
        val mc = model as UnitRing
        if (isZero || mc.isOne(c)) {
            return this
        }
        return div(c)
    }


    /*
    Polynomials on Field
     */

    /**
     * Returns the greatest common divisor of this polynomial and another polynomial.
     *
     * It is required that the [model] is either a [Field] or a [UniqueFactorizationDomain].
     */
    override fun gcd(y: Polynomial<T>): Polynomial<T> {
        if (model is Field) {
            return super.gcd(y) // Euclidean Method
        }
        if (model is UniqueFactorizationDomain) {
            return subResultantGCD(this, y)
        }
        throw UnsupportedOperationException("The model is not a field or a UFD.")
    }

    override fun gcdUV(y: Polynomial<T>): Triple<Polynomial<T>, Polynomial<T>, Polynomial<T>> {
        require(model is Field<T>) { "The model is not a field." }
        return EuclidDomainModel.gcdUVForModel(this, y, zero(model), one(model))
    }


    /**
     * Returns the result `(q, r)` of dividing `this` by `y` and the remainder.
     * It is guaranteed that `this = q * y + r` and `r.degree < y.degree`.
     *
     * It is required that either of the following conditions is satisfied:
     * - The [model] is a [Field].
     * - The [model] is a [UniqueFactorizationDomain] and every division is exact.
     */
    override fun divideAndRemainder(y: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
        if (y.isZero) {
            throw ArithmeticException("Division by zero")
        }
        require(model is UnitRing<T>) { "The model must support `exactDivide`" }
        if (isZero) {
            return this to this
        }
        var remainder = this
        val quotientTerms = mutableListOf<PTerm<T>>()

        val leadTermY = y.leadTerm
        while (!remainder.isZero && remainder.degree >= y.degree) {
            val leadTermR = remainder.leadTerm
            val q = model.exactDivide(leadTermR.value, leadTermY.value)
            val leadPowQuotient = leadTermR.pow - leadTermY.pow
            val quotientTerm = PTerm(leadPowQuotient, q)
            quotientTerms.add(quotientTerm)

            val subtrahend = y.mapTermsNonZeroT { PTerm(it.pow + leadPowQuotient, model.multiply(it.value, q)) }

            remainder -= subtrahend
        }

        val quotient = Polynomial(model, quotientTerms.reversed())
        return quotient to remainder
    }


    /*
    Calculus operations
     */


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
    fun derivative(): Polynomial<T> {
        if (isConstant) {
            return zero(model)
        }
        val nonConstantTerms = if (terms[0].pow == 0) terms.subList(1, terms.size) else terms
        return mapTermsPossiblyZeroT(nonConstantTerms, model) { t ->
            PTerm(t.pow - 1, model.multiplyLong(t.value, t.pow.toLong()))
        }
    }

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
    fun integral(): Polynomial<T> {
        if (isZero) {
            return zero(model)
        }
        require(model is Field<T>) { "The model is not a field." }
        return mapTermsPossiblyZeroT(terms, model) { t ->
            PTerm(t.pow + 1, model.divideLong(t.value, (t.pow + 1).toLong()))
        }
    }

    /*
    Extra algebraic operations for polynomials
     */

    /**
     * Returns the `gcd` of all coefficients of this polynomial, which is referred to as the content of this polynomial.
     *
     * It is required that the [model] is a [EuclideanDomain].
     *
     * @return the greatest common divisor of all coefficients of this polynomial.
     */
    fun cont(): T {
        require(model is EuclideanDomain) { "The model is not a Euclidean domain." }
        if (isZero) {
            return model.zero
        }
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
    fun toPrimitive(): Polynomial<T> {
        if (isZero) {
            return this
        }
        return div(cont())
    }

//    /**
//     * Returns the sylvester matrix of this and g. It is required that `this` and
//     * `g` must not be zero at the same time.
//     * <pre>R(this,g)</pre>
//     *
//     * @param g another polynomial
//     * @return a square matrix whose size is `this.degree + g.degree`.
//     */
//    fun sylvesterMatrix(g: Polynomial<T>): Matrix<T> {
//        return sylvesterDet(this, g)
//    }
//
//    /**
//     * Returns the determinant of the sylvester matrix of this and g. It is required that `this` and
//     * `g` must not be zero at the same time.
//     * <pre>|R(this,g)|</pre>
//     *
//     * @param g another polynomial
//     * @return the determinant of the sylvester matrix
//     */
//    fun sylvesterDet(g: Polynomial<T>): T {
//        return sylvesterMatrix(g).det()
//    }


    /**
     * Determines whether this polynomial and another polynomial have a common root.
     */
    fun hasCommonRoot(y: Polynomial<T>): Boolean {
        val gcd = gcd(y)
        return !gcd.isConstant
    }


    /**
     * Returns the resultant of this polynomial and another polynomial.
     *
     * The resultant of two polynomials `f` and `g` is a scalar (an element of the field) such that
     * the resultant of `f` and `g` is zero if and only if `f` and `g` have a common root.
     *
     * It is required that the [model] is a [Field].
     *
     * @param y another polynomial
     * @return the resultant of this polynomial and `y`.
     */
    fun resultant(y: Polynomial<T>): T {
        // 结式
        require(model is Field<T>) { "The model is not a field." }

        // If either polynomial is zero, the resultant is zero
        if (this.isZero || y.isZero) {
            return model.zero
        }
//
        // Compute the GCD of the two polynomials
        val gcd = gcd(y)

        // If the GCD is not a constant polynomial, the resultant is zero
        if (gcd.degree > 0) {
            return model.zero
        }
        TODO()
//
//        // Otherwise, compute the resultant using the properties of polynomials
//        // (this is simplified for a field)
//        val n = y.degree
//        val m = this.degree
//
//        val leadingCoeffThis = this.leadTerm().value
//        val leadingCoeffY = y.leadTerm().value
//
//        // Compute (-1)^(m*n) * leadingCoeffThis^n * leadingCoeffY^m
//        val sign = if ((m * n) % 2 == 0) model.one else model.negate(model.one)
//        val resultant = model.eval {
//            sign * leadingCoeffThis.pow(m.toLong()) * leadingCoeffY.pow(n.toLong())
//        }
//        return resultant
    }


    companion object {
        private inline fun <T : Any, R : Any> mapTermsPossiblyZeroT(
            terms: List<PTerm<T>>,
            model: Ring<R>,
            transform: (PTerm<T>) -> PTerm<R>
        ): Polynomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val t2 = transform(t)
                if (model.isZero(t2.value)) null else t2
            }
            return Polynomial(model, newTerms)
        }

        private inline fun <T : Any, R : Any> mapTermsPossiblyZero(
            terms: List<PTerm<T>>,
            model: Ring<R>,
            transform: (T) -> R
        ): Polynomial<R> {
            val newTerms = terms.mapNotNullTo(ArrayList(terms.size)) { t ->
                val v = transform(t.value)
                if (model.isZero(v)) null else PTerm(t.pow, v)
            }
            return Polynomial(model, newTerms)
        }


        /**
         * Adds two terms with the same power.
         */
        private fun <T : Any> add2Term(model: Ring<T>, a: PTerm<T>, b: PTerm<T>): PTerm<T>? {
            val r = model.add(a.value, b.value)
            return if (model.isZero(r)) null else PTerm(a.pow, r)
        }

        /**
         * Adds multiple terms with the same power.
         */
        private fun <T : Any> addMultiTerm(model: Ring<T>, list: List<PTerm<T>>, tempList: ArrayList<T>): PTerm<T>? {
            tempList.clear()
            list.mapTo(tempList) { it.value }
            val sum = model.sum(tempList)
            return if (model.isZero(sum)) null else PTerm(list[0].pow, sum)
        }


        /**
         * Merges an unordered list of terms into a polynomial.
         */
        private fun <T : Any> mergeTerms(
            model: Ring<T>,
            rawTerms: List<PTerm<T>>,
            maxMergeSizeEst: Int = 3,
            estimatedSize: Int = rawTerms.size
        ): List<PTerm<T>> {
            val tempList = ArrayList<T>(maxMergeSizeEst)
            return DataStructureUtil.mergeRawList(
                rawTerms,
                comparator = Comparator.naturalOrder(),
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) },
                estimatedSize = estimatedSize
            )
        }

        private fun <T : Any> addTerms2(model: Ring<T>, a: List<PTerm<T>>, b: List<PTerm<T>>): Polynomial<T> {
            val result = DataStructureUtil.mergeSorted2(
                a, b,
                comparator = Comparator.naturalOrder(),
                merger2 = { x, y -> add2Term(model, x, y) },
            )
            return Polynomial(model, result)
        }

        private fun <T : Any> addTermsAll(model: Ring<T>, termsList: List<List<PTerm<T>>>): Polynomial<T> {
            val tempList = ArrayList<T>(termsList.size)
            val resultTerms = DataStructureUtil.mergeSortedK(
                termsList,
                comparator = Comparator.naturalOrder(),
                merger2 = { x, y -> add2Term(model, x, y) },
                mergerMulti = { list -> addMultiTerm(model, list, tempList) }
            )
            return Polynomial(model, resultTerms)
        }


        private fun <T : Any> multiplyTerms(model: Ring<T>, a: List<PTerm<T>>, b: List<PTerm<T>>): Polynomial<T> {
            if (a.isEmpty() || b.isEmpty()) {
                return zero(model)
            }
            val result = ArrayList<PTerm<T>>(a.size * b.size)
            for (ai in a) {
                for (bj in b) {
                    val index = ai.pow + bj.pow
                    val value = model.multiply(ai.value, bj.value)
                    if (!model.isZero(value)) {
                        result.add(PTerm(index, value))
                    }
                }
            }
            val resTerms = mergeTerms(model, result, estimatedSize = a.size + b.size)
            return Polynomial(model, resTerms)
        }


        /**
         * Returns a zero polynomial.
         */
        @JvmStatic
        fun <T : Any> zero(model: Ring<T>): Polynomial<T> {
            return Polynomial(model, emptyList())
        }

        /**
         * Returns a constant polynomial.
         */
        @JvmStatic
        fun <T : Any> constant(model: Ring<T>, c: T): Polynomial<T> {
            if (model.isZero(c)) {
                return zero(model)
            }
            return Polynomial(model, listOf(PTerm(0, c)))
        }

        /**
         * Returns a polynomial with a single term `1`.
         */
        @JvmStatic
        fun <T : Any> one(model: UnitRing<T>): Polynomial<T> {
            return constant(model, model.one)
        }

        /**
         * Returns a polynomial with a single term `x`.
         */
        @JvmStatic
        fun <T : Any> x(model: UnitRing<T>): Polynomial<T> {
            return Polynomial(model, listOf(PTerm(1, model.one)))
        }

        /**
         * Returns a polynomial `ax + b`.
         */
        @JvmStatic
        fun <T : Any> linear(model: Ring<T>, a: T, b: T): Polynomial<T> {
            return Polynomial(model, listOf(PTerm(1, a), PTerm(0, b)))
        }


        /**
         * Creates a polynomial from a list of coefficients.
         * The index of the coefficient corresponds to the power of `x`.
         *
         * For example, the list `[1, 2, 3]` corresponds to the polynomial `1 + 2x + 3x^2`.
         *
         * @return a polynomial with the specified coefficients.
         */
        @JvmStatic
        fun <T : Any> fromList(model: Ring<T>, coefficients: List<T>): Polynomial<T> {
            val terms = coefficients.mapIndexedNotNull { index, value ->
                if (model.isZero(value)) {
                    null
                } else {
                    PTerm(index, value)
                }
            }
            return Polynomial(model, terms)
        }


        /**
         * Creates a polynomial from the coefficients.
         * The first coefficient corresponds to the constant term, the second coefficient corresponds to the coefficient of `x`, and so on.
         *
         *
         * @see fromList
         */
        @JvmStatic
        fun <T : Any> of(model: Ring<T>, vararg coef: T): Polynomial<T> {
            return fromList(model, coef.asList())
        }


        /**
         * Creates a polynomial `a * x^p`.
         */
        fun <T : Any> power(model: Ring<T>, p: Int, a: T): Polynomial<T> {
            require(p >= 0) { "The power must be non-negative." }
            if (model.isZero(a)) {
                return zero(model)
            }
            return Polynomial(model, listOf(PTerm(p, a)))
        }

        /**
         * Returns the sum of a list of polynomials.
         */
        fun <T : Any> sum(model: Ring<T>, list: List<Polynomial<T>>): Polynomial<T> {
            when (list.size) {
                0 -> return zero(model)
                1 -> return list[0]
                2 -> return addTerms2(model, list[0].terms, list[1].terms)
            }
            return addTermsAll(model, list.map { it.terms })
        }

        /**
         * Returns the sum of the given polynomials.
         */
        fun <T : Any> sum(model: Ring<T>, vararg polys: Polynomial<T>): Polynomial<T> {
            return sum(model, polys.asList())
        }


        /*
        Number models
         */

        /**
         * Returns a `Ring<Polynomial<T>>` on a `Ring<T>`.
         *
         * @see [Ring]
         */
        fun <T : Any> asRing(model: Ring<T>): PolyOnRing<T> {
            return PolyOnRing(model)
        }

        /**
         * Returns a `UnitRing<Polynomial<T>>` on a `Ring<T>`.
         *
         * @see [UnitRing]
         */
        fun <T : Any> asRing(model: UnitRing<T>): PolyOnUnitRing<T> {
            return PolyOnUnitRing(model)
        }

        /**
         * Returns a `EuclideanDomain<Polynomial<T>>` on a `Field<T>`.
         *
         * @see EuclideanDomain
         * @see Field
         */
        fun <T : Any> asRing(model: Field<T>): PolyOnField<T> {
            return PolyOnField(model)
        }


        /*
        Methods for number theory
         */

        /**
         * Performs the pseudo division of two polynomials only on a ring.
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
        fun <T : Any> pseudoDivision(A: Polynomial<T>, B: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
            require(!B.isZero)
            /*
            See Algorithm 3.1.2, page 112 of
            'A Course in Computational Algebraic Number Theory', Henri Cohen
            Created by lyc at 2020-03-01 14:25
             */
            val m = A.degree
            val n = B.degree
            require(m >= n)
            val model = A.model
            val d = B.leadCoef
            var R = A
            var Q = zero(model)
            var e = m - n + 1
            while (!R.isZero && R.degree >= B.degree) {
                val S = power(model, R.degree - B.degree, R.leadCoef)
                Q = d * Q + S
                R = d * R - S * B
                e -= 1
            }
            val q = model.power(d, e.toLong())
            Q *= q
            R *= q
            return Pair(Q, R)
        }


        /**
         * Performs the pseudo division of two polynomials only on a ring.
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
        @JvmStatic
        fun <T : Any> pseudoDivisionR(A: Polynomial<T>, B: Polynomial<T>): Polynomial<T> {
            require(!B.isZero)
            /*
            See Algorithm 3.1.2, page 112 of
            'A Course in Computational Algebraic Number Theory', Henri Cohen
            Created by lyc at 2020-03-01 14:25
             */
            val m = A.degree
            val n = B.degree
            require(m >= n)
            val model = A.model
            val d = B.leadCoef
            var R = A
            var e = m - n + 1
            while (!R.isZero && R.degree >= B.degree) {
                val S = power(model, R.degree - B.degree, R.leadCoef)
                R = d * R - S * B
                e -= 1
            }
            val q = model.power(d, e.toLong())
            R *= q
            return R
        }


        /**
         * Computes the GCD of two polynomials on a UFD.
         *
         * It is required that the underlying model is [UniqueFactorizationDomain].
         *
         * @see [subResultantGCD]
         */
        @JvmStatic
        fun <T : Any> primitiveGCD(f: Polynomial<T>, g: Polynomial<T>): Polynomial<T> {
            if (f.isZero) return g
            if (g.isZero) return f
            /*
            See Algorithm 3.2.10, page 117 of
            'A Course in Computational Algebraic Number Theory', Henri Cohen
            Created by lyc at 2020-03-01 16:02
             */
            val mc = f.model

            val rc = mc as UniqueFactorizationDomain<T>
            val a = f.cont()
            val b = g.cont()
            val d = rc.gcd(a, b)
            var A = f.div(a)
            var B = g.div(b)
            while (true) {
                val R = pseudoDivisionR(A, B)
                if (R.isZero) {
                    break
                }
                if (R.isConstant) {
                    B = one(mc)
                    break
                }
                A = B
                B = R.toPrimitive()
            }
            return d * B
        }

        /**
         * Computes the GCD of two polynomials on a UFD using sub-resultant method.
         *
         * @see [primitiveGCD]
         */
        @Suppress("LocalVariableName")
        @JvmStatic
        fun <T : Any> subResultantGCD(f: Polynomial<T>, g: Polynomial<T>): Polynomial<T> {
            /*
            See Algorithm 3.3.1, page 118 of
            'A Course in Computational Algebraic Number Theory', Henri Cohen
            Created by lyc at 2020-03-01 16:02
             */
            var (A, B) = if (f.degree > g.degree) f to g else g to f // A.degree >= B.degree
            if (B.isZero) return A

            val mc = f.model

            val rc = mc as UniqueFactorizationDomain<T>
            val a = A.cont()
            val b = B.cont()
            val d = rc.gcd(a, b)
            var g1 = mc.one
            var h1 = mc.one
            while (true) {
                val t = (A.degree - B.degree).toLong()
                val R = pseudoDivisionR(A, B)
                if (R.isZero) break
                if (R.isConstant) {
                    B = one(mc)
                    break
                }
                A = B
                B = mc.eval { R.div(g1 * (h1 pow t)) }
                g1 = A.leadCoef
                h1 = mc.eval { h1 * ((g1 / h1) pow t) }
            }
            return d * B.toPrimitive()
        }
    }
}

open class PolyOnRing<T : Any>(model: Ring<T>) : Ring<Polynomial<T>>, Module<T, Polynomial<T>> {

    @Suppress("CanBePrimaryConstructorProperty")
    open val model: Ring<T> = model

    final override val zero: Polynomial<T> = Polynomial.zero(model)

    override val scalars: Ring<T>
        get() = model

    override fun scalarMul(k: T, v: Polynomial<T>): Polynomial<T> {
        return v.times(k)
    }

    fun of(vararg coef: T): Polynomial<T> {
        return Polynomial.of(model, *coef)
    }

    fun constant(c: T): Polynomial<T> {
        return Polynomial.constant(model, c)
    }

    fun power(p: Int, a: T): Polynomial<T> {
        return Polynomial.power(model, p, a)
    }


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
        return x.terms.all { model.contains(it.value) }
    }

    override fun subtract(x: Polynomial<T>, y: Polynomial<T>): Polynomial<T> {
        return x - y
    }

    override fun multiplyLong(x: Polynomial<T>, n: Long): Polynomial<T> {
        return x.times(n)
    }

    override fun isZero(x: Polynomial<T>): Boolean {
        return x.isZero
    }

    override fun sum(elements: List<Polynomial<T>>): Polynomial<T> {
        return Polynomial.sum(model, elements)
    }
}

open class PolyOnUnitRing<T : Any>(model: UnitRing<T>) : PolyOnRing<T>(model), UnitRing<Polynomial<T>> {

    override val numberClass: Class<*>
        get() = Polynomial::class.java
    override val one: Polynomial<T> = Polynomial.one(model)

    val x: Polynomial<T>
        get() = Polynomial.x(model as UnitRing<T>)
}

open class PolyOnField<T : Any>(override val model: Field<T>) : PolyOnUnitRing<T>(model),
    EuclideanDomain<Polynomial<T>>, Algebra<T, Polynomial<T>> {
    override val numberClass: Class<*>
        get() = Polynomial::class.java

    override val scalars: Field<T>
        get() = model

    override fun scalarDiv(x: Polynomial<T>, k: T): Polynomial<T> {
        return x.div(k)
    }

    override fun divideAndRemainder(a: Polynomial<T>, b: Polynomial<T>): Pair<Polynomial<T>, Polynomial<T>> {
        return a.divideAndRemainder(b)
    }

    override fun isUnit(x: Polynomial<T>): Boolean {
        return x.isUnit()
    }

    override fun gcdUV(a: Polynomial<T>, b: Polynomial<T>): Triple<Polynomial<T>, Polynomial<T>, Polynomial<T>> {
        return a.gcdUV(b)
    }

    override fun gcd(a: Polynomial<T>, b: Polynomial<T>): Polynomial<T> {
        return a.gcd(b)
    }

//    override fun Polynomial<T>.div(k: T): Polynomial<T> {
//        TODO("Not yet implemented")
//    }
}


