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
 * - Rational numbers `Q`. [cn.mathsymk.structure.Quotients]
 * - Real numbers `R`. [cn.mathsymk.structure.Reals]
 * - Complex numbers `C`.
 * - Fraction fields of integral domains.
 *
 * ## Hierarchy of algebraic structures
 * In the hierarchy of algebraic structures, a field is a division ring whose multiplication is commutative.
 */
interface Field<T : Any> : DivisionRing<T> {
    /**
     * Returns the characteristic of this field.
     *
     * This method is optional.
     */
    val characteristic: Long?

    /**
     * The multiplication in a field is commutative.
     */
    override val isCommutative: Boolean
        get() = true


}