package io.github.ezrnest.structure


/**
 *
 */
interface ProvideElementaryFunctions<T> {

    /**
     * Gets a constant by the [name]
     *
     * Some common constants are list below:
     * * `pi` : the constant `π`, the ratio of the circumference of a circle to its diameter.
     * * `e` : the constant `e`, the base of the natural logarithm.
     *
     *
     *
     * @param name the name of the constant value
     * @return a number that represents the constant value.
     */
    fun constantValue(name: String): T

    /**
     * Returns the positive square root of `x`.
     */
    fun sqrt(x: T): T

    /**
     * Returns the `n`-th root of `x`, where `n` is a non-negative positive integer.
     */
    fun nroot(x: T, n: Long): T

     /**
     * Returns `base^pow`, the exponential function.
     *
     * @param base the base
     * @param pow the exponent
     */
    fun exp(base: T, pow: T): T

    /**
     * Returns `e^x`, the exponential function.
     *
     * @param x the exponent
     * @return `e^x`
     */
    fun exp(x: T): T

    /**
     * Returns `log_{base}(x)`, the logarithm function with base `a`.
     *  
     * 
     * @param base the base of the logarithm
     * @param x the number to be calculated
     */
    fun log(base: T, x: T): T

    /**
     * Returns `ln(x)`, the natural logarithm (base `e`).
     *
     */
    fun ln(x: T): T

    /**
     * Returns `sin(x)`, the sine function.
     *
     * @param x a number
     * @return `sin(x)`
     */
    fun sin(x: T): T

    /**
     * Returns `cos(x)`, the cosine function.
     *
     * @param x a number
     * @return `cos(x)`
     */
    fun cos(x: T): T

    /**
     * Returns `tan(x)`, the tangent function.
     *
     * @param x a number
     */
    fun tan(x: T): T

    /**
     * Returns `cot(x)`, the cotangent function.
     */
    fun cot(x: T): T

    /**
     * Returns `arcsin(x)`, the inverse sine function.
     */
    fun arcsin(x: T): T

    /**
     * Returns `arccos(x)`, the inverse cosine function.
     */
    fun arccos(x: T): T

    /**
     * Returns `arctan(x)`, the inverse tangent function.
     */
    fun arctan(x: T): T

    /**
     * Returns `arctan2(y, x) = arctan(y/x)`, the inverse tangent function with two arguments.
     */
    fun arctan2(y: T, x: T): T

    companion object{

        val PI = "pi"
        val E = "e"
    }
}

