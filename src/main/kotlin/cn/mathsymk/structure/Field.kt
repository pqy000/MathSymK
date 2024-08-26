package cn.mathsymk.structure


/**
 * A field defines a set with two operations, addition `+` and multiplication `*`.
 * They satisfy the following properties:
 *
 * 1. The set is an Abelian group under addition with zero element `0` and inverse operation `-`.
 *    - `a + (b + c) = (a + b) + c` (Associative)
 *    - `a + b = b + a` (Commutative)
 *    - `a + 0 = a` (Identity)
 *    - `a + (-a) = 0` (Inverse)
 *
 * 2. The set is a commutative monoid under multiplication with identity element `1`. Moreover, every non-zero element has a multiplicative inverse `a^-1`.
 *    - `a * (b * c) = (a * b) * c` (Associative)
 *    - `a * b = b * a` (Commutative)
 *    - `a * 1 = a` (Identity)
 *    - `a * a^-1 = 1` (Inverse), provided `a != 0`
 *
 * 3. Multiplication distributes over addition.
 *    - `a * (b + c) = a * b + a * c`
 *
 * ## Examples
 * - Rational numbers `Q`. [Quotients][cn.mathsymk.structure.Quotients]
 * - Real numbers `R`. [Reals][cn.mathsymk.structure.Reals]
 * - Complex numbers `C`.
 * - Fraction fields of integral domains. [FractionField][cn.mathsymk.model.RingFraction]
 *
 * ## Hierarchy of algebraic structures
 * In the hierarchy of algebraic structures, a field is a division ring whose multiplication is commutative.
 */
interface Field<T : Any> : DivisionRing<T>, CommutativeRing<T> {
    /**
     * Returns the characteristic of this field.
     *
     * This method is optional and may return `null` if the characteristic is not determined.
     */
    val characteristic: Long?


    /**
     * In a field, `x` is a unit if and only if `x` is not zero.
     */
    override fun isUnit(x: T): Boolean {
        return !isZero(x)
    }

    /**
     * In a field, the division is always exact.
     *
     * @see divide
     */
    override fun exactDivide(a: T, b: T): T {
        return divide(a, b)
    }


    /**
     * The multiplication in a field is commutative.
     */
    override val isCommutative: Boolean
        get() = true

}