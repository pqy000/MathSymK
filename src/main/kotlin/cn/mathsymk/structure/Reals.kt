package cn.mathsymk.structure

interface CompleteField<T : Any> : Field<T> {

}


/**
 * Describes the real numbers, **R**.
 *
 * @author Ezrnest
 */
interface Reals<T : Any> : Quotients<T>, CompleteField<T> {
    // Re-created at 2024/8/11 18:54 by Ezrnest

    /**
     * Return the value zero in this kind of number type.
     *
     * @return 0
     */
    override val zero: T

    /**
     * Return the value one in this kind of number type. The returned number should
     * be equal to `this.divide(t,t)`.
     *
     * @return 1
     */
    override val one: T

    /**
     * Returns the class object of the number type operated by this MathCalculator.
     *
     * @return the class
     */
    override val numberClass: Class<T>


    /**
     * Compare the two numbers and determines whether these two numbers are the
     * identity.
     *
     * ** For any calculator, this method should be implemented.**
     *
     * @param x a number
     * @param y another number
     * @return `true` if `para1 == para2`,otherwise `false`
     */
    override fun isEqual(x: T, y: T): Boolean

    /**
     * Compare the two numbers, return -1 if `para1 < para2 `, 0 if
     * `para1==para2` , or 1 if `para1 > para2`.This method is
     * recommended to be literally the identity to the method `compareTo()` if the
     * object `T` is comparable.
     *
     * @param o1 a number
     * @param o2 another number
     * @return -1 if `para1 < para2 `, 0 if `para1==para2` , or 1 if
     * `para1 > para2`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     */
    override fun compare(o1: T, o2: T): Int

    /**
     * Add two parameters, this method is required to be commutative, so is it
     * required that `add(t1,t2)=add(t2,t1)`.
     *
     * @param x a number
     * @param y another number
     * @return `para1 + para2`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun add(x: T, y: T): T


    /**
     * Returns the negation of this number.
     *
     * @param x a number
     * @return `-para`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun negate(x: T): T

    /**
     * Returns the absolute value of this number.
     *
     * @param x a number
     * @return `|x|`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun abs(x: T): T

    /**
     * Returns the result of `para1-para2`,this method should return the identity
     * result with `add(para1,this.negate(para2))`.
     *
     * @param x a number
     * @param y another number
     * @return `para1-para2`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun subtract(x: T, y: T): T {
        return super<Quotients>.subtract(x, y)
    }


    /**
     * Returns the result of `x * y`.
     *
     * @param x a number
     * @param y another number
     * @return `x * y`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun multiply(x: T, y: T): T

    /**
     * Multiply the parameters,this method is equal to:
     *
     *    T re = getOne();
     *    for (T t : ps) {
     *      re = multiply(re, t);
     *    }
     *    return re;
     *
     * @param ps a list of numbers
     * @return the result
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun product(ps: List<T>): T {
        var re = one
        for (t in ps) {
            re = multiply(re, t)
        }
        return re
    }

    /**
     * Returns the result of `x / y`.
     *
     * @param x a number as dividend
     * @param y another number as divisor
     * @return `x / y`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun divide(x: T, y: T): T

    /**
     * Return the value of `1/p`. This method should be equal to
     * `this.divide(this.getOne,p)`.
     *
     * @param x a number
     * @return `1/p`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun reciprocal(x: T): T

    /**
     * Return the result of `x*n`, this method is provided because this is
     * equals to add for `n` times. This method expects a better
     * performance.
     *
     * @param x a number
     * @param n another number of long
     * @return `x*n`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun multiplyLong(x: T, n: Long): T {
        return super<Quotients>.multiplyLong(x, n)
    }

    /**
     * Return the result of `x / n` , throws exception if necessary.
     *
     * @param x a number as dividend
     * @param n another number of long as divisor
     * @return `x / n`
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun divideLong(x: T, n: Long): T

    /**
     * Return the square root of `x`. This method should return the positive
     * square of `x`.
     *
     * @param x a number
     * @return `x ^ 0.5`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun squareRoot(x: T): T

    /**
     * Return the n-th root of `x`. This method should return a positive
     * number if `n` is even.
     *
     * @param x a number
     * @return `x ^ (1/n)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun nroot(x: T, n: Long): T

    /**
     * Return `x ^ n`.This method should be equal to calling
     * multiply for many times if `n > 0` , or
     * divide if `n < 0 `, or return `getOne()` if
     * `n == 0`.Notice that this calculator may not throw an
     * ArithmeticException if `n == 0 && n <= 0`, whether to throw exception
     * is determined by the implementation.
     *
     * @param x   a number
     * @param n the exponent
     * @return `p ^ exp`.
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    override fun power(x: T, n: Long): T {
        return super<Quotients>.power(x, n)
    }

    /**
     * Gets a constant value from the calculator, the constant value is got by its
     * name as a String. The name of the constant value should be
     * specified wherever the value is needed.
     *
     * Some common constants are list below:
     *
     *  * <tt>Pi, [STR_PI]</tt> :the ratio of the circumference of a circle to its
     * diameter.  See: [Math.PI]
     *  * <tt>e, [STR_E]</tt> :the base of the natural logarithms. See: [Math.E]
     *
     *
     * @param name the name of the constant value, case insensitive
     * @return a number that represents the constant value.
     * @throws UnsupportedOperationException if this operation can not be done. (optional)
     */
    fun constantValue(name: String): T

    /**
     * Returns the result of `a^b`. <br></br>
     * This method provides a default implement by computing:
     * `exp(multiply(ln(a), b))`.
     *
     * @param a a number
     * @param b the exponent
     * @return `a^b`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun exp(a: T, b: T): T {
        return exp(multiply(ln(a), b))
    }

    /**
     * Returns the result of `e^x`, where `e` is the base of the natural
     * logarithm.
     *
     * @param x the exponent
     * @return `e^x`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun exp(x: T): T

    /**
     * Returns result of
     *
     *
     * `log(a,b) = ln(b) / ln(a)`
     *
     * This method provides a default implement by computing:
     * `divide(ln(b),ln(a))`.
     *
     * @param base a number
     * @param x another number
     * @return `log(a,b) = ln(b) / ln(a)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun log(base: T, x: T): T {
        return divide(ln(x), ln(base))
    }

    /**
     * Returns result of
     *
     * `ln(x)`
     *
     * namely the natural logarithm (base e).
     *
     * @param x a number
     * @return `ln(x)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun ln(x: T): T

    /**
     * Returns the result of `sin(x)`
     *
     * @param x a number
     * @return `sin(x)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun sin(x: T): T

    /**
     * Returns the result of `cos(x)`. <br></br>
     * This method provides a default implement by computing:
     * `squareRoot(subtract(getOne(), multiply(x, x)))`. If a better implement
     * is available, subclasses should always override this method.
     *
     * @param x a number
     * @return `cos(x)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun cos(x: T): T {
        return squareRoot(subtract(one, multiply(x, x)))
    }

    /**
     * Returns the result of `tan(x)`. <br></br>
     * This method provides a default implement by computing:
     * `divide(sin(x),cos(x))`. If a better implement is available,
     * subclasses should always override this method.
     *
     * @param x a number
     * @return `tan(x)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun tan(x: T): T {
        return divide(sin(x), cos(x))
    }

    /**
     * Returns the result of `arcsin(x)`.
     *
     * @param x a number
     * @return `arcsin(x)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun arcsin(x: T): T

    /**
     * Returns the result of `arccos(x)`. <br></br>
     * This method provides a default implement by computing:
     * `subtract(divideLong(constantValue(STR_PI), 2l), arcsin(x))`. If a
     * better implement is available, subclasses should always override this method.
     *
     * @param x a number
     * @return `arccos(x)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun arccos(x: T): T {
        return subtract(divideLong(constantValue(STR_PI), 2L), arcsin(x))
    }

    /**
     * Returns the result of `arctan(x)`. <br></br>
     * This method provides a default implement by computing:
     * `arcsin(divide(x,squareRoot(add(getOne(), multiply(x, x)))))`. If a
     * better implement is available, subclasses should always override this method.
     *
     * @param x a number
     * @return `arctan(x)`
     * @throws UnsupportedOperationException if this operation can not be done.(optional)
     * @throws ArithmeticException             if this operation causes an exceptional arithmetic condition.
     */
    fun arctan(x: T): T {
        return arcsin(divide(x, squareRoot(add(one, multiply(x, x)))))
    }

    /**
     * Returns a value that represents the given integer.
     */
    override fun of(n: Long): T {
        return one * n
    }

//    /**
//     * Returns a value that represents the given fraction.
//     */
//    override fun of(x: Fraction): T {
//        if (x.isZero) {
//            return zero
//        }
//        var re: T = of(x.numerator)
//        if (x.denominator != 1L) {
//            re = divideLong(re, x.denominator)
//        }
//        return re
//    }


    companion object {

        /**
         * The string representation of pi.
         */
        const val STR_PI = "pi"

        /**
         * The string representation of e.
         */
        const val STR_E = "e"

        /**
         * The string representation of i, the square root of -1.
         * This constant value may not be available.
         */
        const val STR_I = "i"

    }

}
