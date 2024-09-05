package cn.mathsymk.structure



/**
 * Describes the real numbers, **R**.
 *
 * @author Ezrnest
 */
interface Reals<T> : OrderedField<T>, Field<T>,ProvideElementaryFunctions<T> {
    // Re-created at 2024/8/11 18:54 by Ezrnest

    /**
     * The characteristic of the real numbers is `0`.
     */
    override val characteristic: Long
        get() = 0



    /**
     * Returns `a^b`, the exponentiation operation.
     *
     * This method is implemented in default by:
     *
     *    a^b = exp(ln(a) * b)
     *
     *
     */
    override fun exp(base: T, pow: T): T {
        return exp(ln(base) * pow)
    }

    /**
     * Returns the `n`-th root of `x`, where `n` is a non-negative positive integer.
     *
     * This method is implemented in default by:
     *
     *    nroot(x,n) = exp(x, 1/n)
     */
    override fun nroot(x: T, n: Long): T {
        return exp(x, divideLong(x,n))
    }

    /**
     * Returns `log_{base}(x)`, the logarithm function with base `base`.
     *
     * This method is implemented in default by:
     *
     *    log_{base}(x) = ln(x) / ln(base)
     */
    override fun log(base: T, x: T): T {
        return ln(x) / ln(base)
    }



    /**
     * Returns `cos(x)`, the cosine function.
     *
     * This method is implemented in default by:
     *
     *    cos(x) = sqrt(1 - x^2)
     *
     *
     * @return `cos(x)`
     */
    override fun cos(x: T): T {
        return sqrt(one - x*x)
    }

    /**
     * Returns `tan(x)`, the tangent function.
     *
     * This method is implemented in default by:
     *
     *    tan(x) = sin(x) / cos(x)
     *
     * @return `tan(x)`
     */
    override fun tan(x: T): T {
        return sin(x) / cos(x)
    }


    /**
     * Returns `cot(x)`, the cotangent function.
     *
     * This method is implemented in default by:
     *
     *    cot(x) = cos(x) / sin(x)
     *
     * @return `cot(x)`
     */
    override fun cot(x: T): T {
        return cos(x) / sin(x)
    }

    /**
     * Returns `arccos(x)`, the inverse cosine function.
     *
     * This method is implemented in default by:
     *
     *    arccos(x) = pi / 2 - arcsin(x)
     *
     * @param x a number
     * @return `arccos(x)`
     */
    override fun arccos(x: T): T {
        return subtract(divideLong(constantValue("pi"), 2L), arcsin(x))
    }

    /**
     * Returns `arctan(x)`, the inverse tangent function.
     *
     * This method is implemented in default by:
     *
     *     arctan(x) = arcsin(x / sqrt(1 + x^2))
     *
     * @param x a number
     * @return `arctan(x)`
     */
    override fun arctan(x: T): T {
        return arcsin(divide(x, sqrt(one + x*x)))
    }

    override fun arctan2(y: T, x: T): T {
        return arctan(divide(y, x))
    }
}
