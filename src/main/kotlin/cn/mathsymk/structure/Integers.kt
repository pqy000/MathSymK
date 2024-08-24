package cn.mathsymk.structure

import cn.mathsymk.util.exceptions.ExceptionUtil
import cn.mathsymk.model.struct.EuclidRingNumberModel
import java.math.BigInteger


/**
 * A calculator for unique factorization domain (UFD).
 * It supports computing the greatest common divisor of two elements.
 *
 * **Implementation Notes:** Subclasses should always implement the method [UnitRing.isUnit].
 *
 *
 */
interface UniqueFactorizationDomain<T : Any> : UnitRing<T> {

    /**
     * Determines whether the given element is a unit, namely invertible with respect to multiplication.
     */
    override fun isUnit(x: T): Boolean

    /**
     * Returns the greatest common divisor of [a] and [b].
     */
    fun gcd(a: T, b: T): T

    //    /**
    //     * Returns the greatest common divisor of two numbers and a pair of number (u,v) such that
    //     * <pre>ua+vb=gcd(a,b)</pre>
    //     * The returned greatest common divisor is the same as {@link NTCalculator#gcd(Object, Object)}.
    //     * Note that the pair of <code>u</code> and <code>v</code> returned is not unique and different implementation
    //     * may return differently when a,b is the same.<P></P>
    //     * The default implementation is based on the Euclid's algorithm.
    //     *
    //     * @return a tuple of <code>{gcd(a,b), u, v}</code>.
    //     */
    //    @NotNull
    //    default Triple<T, T, T> gcdUV(@NotNull T a, T b) {
    //        if (isZero(a)) {
    //            return new Triple<>(b, getZero(), getOne());
    //        }
    //        if (isZero(b)) {
    //            return new Triple<>(a, getOne(), getZero());
    //        }
    //        return gcdUV0(a, b);
    //    }

    /**
     * Determines whether the two numbers `a` and `b`
     * are co-prime.
     */
    fun isCoprime(a: T, b: T): Boolean {
        return isUnit(gcd(a, b))
    }

    /**
     * Returns the result of exact division `a/b`, throws an `ArithmeticException` if it is not exact division.
     */
    override fun exactDivide(a: T, b: T): T

    /**
     * Determines whether `x` exactly divides `y`.
     */
    fun isExactDivide(a: T, b: T): Boolean
}

/*
 * Created by liyicheng at 2020-03-09 19:32
 */
/**
 * Describes a calculator for a Euclidean domain. The fundamental operation of this type of calculator is
 * [divideAndRemainder].
 *
 *
 * For example, integers and polynomials on a field are both `EuclideanDomain`,
 *
 * See [EuclideanDomain](https://mathworld.wolfram.com/EuclideanDomain.html) for more information.
 */
interface EuclideanDomain<T : Any> : UniqueFactorizationDomain<T> {

    /**
     * Returns a pair of `(q, r)` such that
     *
     *     a = qb + r
     *
     * where `q` is the quotient and `r` the remainder.
     */
    fun divideAndRemainder(a: T, b: T): Pair<T, T>

    /**
     * Returns the quotient part of [divideAndRemainder].
     *
     * @param a the dividend
     * @param b the divisor
     * @return the quotient `q` of `a = qb + r`.
     * @see divideAndRemainder
     */
    fun divideToInteger(a: T, b: T): T = divideAndRemainder(a, b).first

    /**
     * Returns the remainder part of [divideAndRemainder].
     *
     * @param a the dividend
     * @param b the divisor
     * @return the remainder `r` of `a = qb + r`.
     * @see divideAndRemainder
     */
    fun remainder(a: T, b: T): T = divideAndRemainder(a, b).second


    /**
     * Returns `a mod b`, which is generally the same as [remainder].
     *
     * Note that the result may differ with respect to a unit in the ring.
     */
    fun mod(a: T, b: T): T = remainder(a, b)

    override fun exactDivide(a: T, b: T): T {
        val (q, r) = divideAndRemainder(a, b)
        if (!isZero(r)) {
            ExceptionUtil.notExactDivision(a, b)
        }
        return q
    }

    override fun isExactDivide(a: T, b: T): Boolean {
        return isZero(remainder(a, b))
    }

    /**
     * Returns the greatest common divisor of [a] and [b].
     *
     * The default implementation uses Euclidean algorithm.
     */
    override fun gcd(a: T, b: T): T {
        var x = a
        var y = b
        var t: T
        while (!isZero(y)) {
            t = y
            y = remainder(x, y)
            x = t
        }
        return a
    }

    /**
     * Returns the greatest common divisor of two numbers and a pair of number `(u,v)` such that
     *
     *     ua + vb = gcd(a, b)
     *
     * The returned greatest common divisor is the same as [gcd].
     * Note that the pair of `u` and `v` returned is not unique and different implementations
     * may return differently when `a, b` is the same.
     *
     * The default implementation is based on the extended Euclid's algorithm.
     *
     * @return a tuple of `(gcd(a,b), u, v)`.
     */
    fun gcdUV(a: T, b: T): Triple<T, T, T> {
        //trivial cases
        if (isZero(a)) {
            return Triple(b, zero, one)
        }
        if (isZero(b)) {
            return Triple(a, one, zero)
        }
        /*
        Euclid's Extended Algorithms:
        Refer to Henri Cohen 'A course in computational algebraic number theory' Algorithm 1.3.6

        Explanation of the algorithm:
        we want to maintain the following equation while computing the gcd using the Euclid's algorithm
        let d0=a, d1=b, d2, d3 ... be the sequence of remainders in Euclid's algorithm,
        then we have
            a*1 + b*0 = d0
            a*0 + b*1 = d1
        let
            u0 = 1, v0 = 0
            u1 = 0, v1 = 1
        then we want to build a sequence of u_i, v_i such that
            a*u_i + b*v_i = d_i,
        when we find the d_n = gcd(a,b), the corresponding u_n and v_n is what we want.
        We have:
            d_i = q_i * d_{i+1} + d_{i+2}        (by Euclid's algorithm
        so
            a*u_i + b*v_i = q_i * (a*u_{i+1} + b*v_{i+1}) + (a*u_{i+2} + b*v_{i+2})
            u_i - q_i * u_{i+1} = u_{i+2}
            v_i - q_i * v_{i+1} = v_{i+2}
        but it is only necessary for us to record u_i, since v_i can be calculated from the equation
            a*u_i + b*v_i = d_i
         */
        var d0 = a
        var d1 = b
        var u0: T = one
        var u1: T = zero
        while (!isZero(d1)) {
            val (q, d2) = divideAndRemainder(d0, d1)
            d0 = d1
            d1 = d2
            val u2 = subtract(u0, multiply(q, u1))
            u0 = u1
            u1 = u2
        }
        val v: T = divideToInteger(d0 - a * u0, b) // discard the possible remainder caused by numeric imprecision
        return Triple(d0, u0, v)
    }

    /**
     * Returns the greatest common divisor of two numbers and a pair of number `(u,v)` such that
     *
     *     ua + vb = gcd(a, b)
     *
     * and that `u,v` is minimal, that is, `v` is equal to the modular of `a1 = a/d`.
     *
     * The default implementation is based on the extended Euclid's algorithm.
     *
     * @return a tuple of `(gcd(a,b), u, v)`.
     * @see gcdUV
     */
    fun gcdUVMin(a: T, b: T): Triple<T, T, T> {
        val result = gcdUV(a, b)
        if (isZero(a)) {
            return result
        }
        val (d, u, v) = result

        val a1 = divideToInteger(a, d)
        val b1 = divideToInteger(b, d)
        val (k, v1) = divideAndRemainder(v, a1)

        val u1 = u + b1 * k
        return Triple(d, u1, v1)
    }

    /**
     * Returns the modular inverse of `a` with respect to `p`, that is, find the element `b` such
     * that `ab = 1 (mod p)`.
     *
     * The default implementation is based on [gcdUV].
     *
     * @return the modular inverse of `a`
     *
     */
    fun modInverse(a: T, p: T): T {
        val (g, u, _) = gcdUV(a, p)
        if (!isUnit(g)) {
            throw ArithmeticException("[$a] is not invertible with respect to modular [$p]")
        }
        return u
    }


    /**
     * Returns `(a^n) mod m`, where `n` is a non-negative integer.
     *
     *
     * This method is mathematically equivalent to `mod(pow(x, n), m)`.
     *
     *
     * For example, `powerAndMod(2,2,3) = 1`, and `powerAndMod(3,9,7) = 6`.
     *
     * @param a a number
     * @param n the power, a non-negative number.
     * @param m the modular.
     */
    fun powMod(a: T, n: Long, m: T): T {
        require(n >= 0) { "n must be non-negative!" }
        var x = a
        if (isEqual(m, one)) {
            return zero
        }
        if (isEqual(x, one)) {
            return one
        }
        var ans = one
        var p = n
        x = mod(x, m)
        while (p > 0) {
            if (p and 1 == 1L) {
                ans = mod(multiply(x, ans), m)
            }
            x = mod(multiply(x, x), m)
            p = p shr 1
        }
        return ans
    }


    /**
     * Returns the result `x` such that
     *
     *     x = remainders[i] (mod mods[i])
     *
     * @param mods a list of the modular, they must be co-prime
     *
     */
    fun chineseRemainder(mods: List<T>, remainders: List<T>): T {
//        val prod: T = mods.reduce(this::multiply)
//        var x: T = zero
//        for (i in mods.indices) {
//            val m: T = mods[i]
//            val r: T = remainders[i]
//            val t = exactDivide(prod,m)
//            val inv = this.modInverse(t, m)
//            x += r * t * inv
//            x = mod(x,prod)
//        }
//        return x
        //Created by lyc at 2021-04-20 20:31
        var m: T = mods[0]
        var x: T = remainders[0]
        for (i in 1 until mods.size) {
            val (_, u, v) = gcdUV(m, mods[i])
            x = u * m * remainders[i] + v * mods[i] * x
            m *= mods[i]
            x = mod(x, m)
        }
        return x
    }

    companion object {


        fun <T : EuclidRingNumberModel<T>> gcdUV(a: T, b: T, zero: T, one: T): Triple<T, T, T> {
            //trivial cases
            if (a.isZero) {
                return Triple(b, zero, one)
            }
            if (b.isZero) {
                return Triple(a, one, zero)
            }
            // see EuclideanDomain.gcdUV for explanation
            var d0 = a
            var d1 = b
            var u0: T = one
            var u1: T = zero
            while (!d1.isZero) {
                val (q, d2) = d0.divideAndRemainder(d1)
                d0 = d1
                d1 = d2
                val u2 = u0 - q * u1
                u0 = u1
                u1 = u2
            }
            val v: T = (d0 - a * u0).divideToInteger(b) // discard the possible remainder caused by numeric imprecision
            return Triple(d0, u0, v)
        }



        /**
         * Creates a quotient field calculator with a prime element [prime].
         */
        fun <T : Any> quotientFieldCalculator(cal: EuclideanDomain<T>, prime: T): Field<T> {
            return QuotientFieldCal(cal, prime)
        }


        class QuotientFieldCal<T : Any>(val cal: EuclideanDomain<T>, val p: T) : Field<T> {

            override fun contains(x: T): Boolean {
                TODO()
            }

            override fun reciprocal(x: T): T {
                return cal.modInverse(x, p)
            }

            override fun isEqual(x: T, y: T): Boolean {
                return cal.eval {
                    isZero(mod(x - y, p))
                }
            }

            override fun multiply(x: T, y: T): T {
                return cal.eval {
                    mod(multiply(x, y), p)
                }
            }

            override val characteristic: Long
                get() = throw UnsupportedOperationException()
            override val zero: T
                get() = cal.zero

            override fun add(x: T, y: T): T {
                return cal.eval {
                    mod(add(x, y), p)
                }
            }

            override fun negate(x: T): T {
                return cal.negate(x)
            }

            override val one: T
                get() = cal.one

            override fun multiplyLong(x: T, n: Long): T {
                return cal.eval { mod(multiplyLong(x, n), p) }
            }


            override fun of(n: Long): T {
                return cal.eval { mod(of(n), p) }
            }

            override fun subtract(x: T, y: T): T {
                return cal.eval { mod(subtract(x, y), p) }
            }

            override val isCommutative: Boolean
                get() = cal.isCommutative
        }

    }
}


/**
 * IntCalculator represents a common supertype for all calculator that deal with integers(int, long, BigInteger...),
 * which provides more methods related to integers.
 *
 *
 *
 * @author liyicheng 2017-09-09 20:33
 */
interface Integers<T : Any> : EuclideanDomain<T>, OrderedRing<T> {
    /**
     * Returns the integer `1` of type T.
     */
    override val one: T


    // methods that is often used as a number theory calculator.
    /**
     * Returns `x+1`.
     *
     *
     * This method is added for convenience. The default implement is
     *
     * <pre>
     * return add(x, getOne());
    </pre> *
     *
     * @param x a number
     * @return `x+1`
     */
    fun increase(x: T): T {
        return add(x, one)
    }

    /**
     * Returns `x-1`.
     *
     *
     * This method is added for convenience. The default implement is
     *
     * <pre>
     * return subtract(x, getOne());
    </pre> *
     *
     * @param x a number
     * @return `x-1`
     */
    fun decrease(x: T): T {
        return subtract(x, one)
    }

    //////////////////////////////////////////////////////////////////
    // Separate line for methods
    //////////////////////////////////////////////////////////////////
    //    /**
    //     * Determines whether the number is an integer. An integer must always be a
    //     * quotient. The constant values {@link #getOne()} and {@link #getZero()}
    //     * must be an integer.
    //     * <p>
    //     * For example, {@code 1} is an integer while {@code 1.1} is not.
    //     *
    //     * @param x a number
    //     * @return {@code true} if the number is an integer, otherwise
    //     * {@code false}.
    //     */
    //    boolean isInteger(T x);
    /**
     * Converts a value of type T to long, throws [UnsupportedOperationException] if
     * this cannot be done.
     */
    fun asLong(x: T): Long {
        return asBigInteger(x).longValueExact()
    }

    /**
     * Converts a value of type T to BigInteger, throws [UnsupportedOperationException] if
     * this cannot be done.
     */
    fun asBigInteger(x: T): BigInteger

    //    /**
    //     * Determines whether the number is a quotient, which can be represented by
    //     * a quotient {@code p/q} where {@code p} and {@code q} has no common
    //     * factor.
    //     * <p>
    //     * For example, {@code 1}, {@code 2/3} are quotients, while {@code sqr(2)}
    //     * is not.
    //     *
    //     * @param x a number
    //     * @return {@code true} if the number is a quotient, otherwise
    //     * {@code false}.
    //     */
    //    boolean isQuotient(T x);
    /**
     * Returns `a mod b`, a *non-negative* number as the result. It
     * is required that `b` is positive, otherwise an ArithmeticException
     * will be thrown.
     *
     *
     * For example, `mod(2,1)=0`, `mod(7,3)=1` and
     * `mod(-7,3) = 2`.
     *
     * @param a a number
     * @param b the modulus
     * @return `a mod b`
     */
    override fun mod(a: T, b: T): T

    /**
     * Returns the remainder:`a % b`. If `a>0`, then the result will
     * be positive, and if `a<0`, then the result should be negative. It
     * is required that `b` is positive, otherwise an ArithmeticException
     * will be thrown.
     *
     *
     * For example, `remainder(1,2)=0`, `remainder(7,3)=1` and
     * `remainder(-7,3) = -1`.
     *
     * @param a the dividend
     * @param b the divisor
     * @return `a % b`
     */
    override fun remainder(a: T, b: T): T {
        if (isZero(b)) {
            throw java.lang.ArithmeticException()
        }
        val signum: Int = compare(a, zero)
        if (signum == 0) {
            return zero
        }
        return if (signum < 0) {
            // return a negative number
            subtract(mod(negate(a), abs(b)), b)
        } else {
            mod(a, abs(b))
        }
    }

    /**
     * Returns the result of `a \ b`. Returns the biggest integer `n`
     * in absolute value that `|nb|<=|a|`, whether the result is positive
     * is determined by `a`.
     *
     *
     * For example, `divideToInteger(3,2) == 1` and
     * `divideToInteger(-5,2) == -2`
     *
     * @param a the dividend
     * @param b the divisor
     * @return `a \ b`
     */
    override fun divideToInteger(a: T, b: T): T

    /**
     * Returns a pair of two numbers containing `(this / val)` followed by
     * `(this % val)`.
     *
     * @param a the dividend
     * @param b the divisor
     * @return a pair of two numbers: the quotient `(a / b)` is the first
     * element, and the remainder `(a % b)` is the second element.
     */
    override fun divideAndRemainder(a: T, b: T): Pair<T, T> {
        val quotient = divideToInteger(a, b)
        val reminder = remainder(a, b)
        return Pair(quotient, reminder)
    }

    /**
     * Determines whether `mod(a,b)==0`.
     *
     *
     * This method is added for convenience. The default implement is
     *
     * <pre>
     * return isEqual(mod(a, b), getZero());
    </pre> *
     *
     * @param a a number
     * @param b another number
     * @return `mod(a,b)==0`
     */
    override fun isExactDivide(a: T, b: T): Boolean {
        return isEqual(mod(a, b), zero)
    }

    /**
     * Determines whether the number is an odd number. A number is an odd number
     * if it is an integer and `mod(x,2)==1`.
     *
     *
     * This method is added for convenience. The default implement is
     *
     * <pre>
     * if (!isInteger(x)) {
     * return false;
     * }
     * T two = increase(getOne());
     * return isEqual(getOne(), mod(x, two));
    </pre> *
     *
     * @param x a number
     * @return `true` if it is an odd number, otherwise `false`.
     */
    fun isOdd(x: T): Boolean {
//        if (!isInteger(x)) {
//            return false;
//        }
        return !isEven(x)
    }

    /**
     * Determines whether the number is an even number. A number is an even
     * number if it is an integer and `mod(x,2)==0`.
     *
     *
     * This method is added for convenience. The default implement is
     *
     * <pre>
     * if (!isInteger(x)) {
     * return false;
     * }
     * T two = increase(getOne());
     * return isEqual(getZero(), mod(x, two));
    </pre> *
     *
     * @param x a number
     * @return `true` if it is an even number, otherwise `false`.
     */
    fun isEven(x: T): Boolean {
//        if (!isInteger(x)) {
//            return false;
//        }
        val two = increase(one)
        return isZero(mod(x, two))
    }

    /**
     * Returns gcd(|a|,|b|), the maximal positive common factor of the two
     * numbers. Returns `0` if `a==0&&b==0`, and returns another
     * non-zero number if either of them is `0`. Whether the two number is
     * negative is ignored. This method is implemented with Euclid's algorithm by default.
     *
     *
     * For example, `gcd(3,5)=1`, `gcd(12,30)=6`.
     *
     * @param a a number
     * @param b another number
     * @return `gcd(|a|,|b|)`
     */
    override fun gcd(a: T, b: T): T {
        var x = a
        var y = b
        x = abs(x)
        y = abs(y)
        var t: T
        while (!isZero(y)) {
            t = y
            y = mod(x, y)
            x = t
        }
        return x
    }


    /**
     * Returns lcm(|a|,|b|), the two numbers' positive least common multiple.
     * If either of the two numbers is 0, then 0 will be return.
     *
     *
     * For example, `lcm(3,5)=15`, `lcm(12,30)=60`.
     *
     * @param a a number
     * @param b another number
     * @return lcm(|a|,|b|).
     */
    fun lcm(a: T, b: T): T {
        var x = a
        var y = b
        if (isZero(x) || isZero(y)) {
            return zero
        }
        x = abs(x)
        y = abs(y)
        val gcd = gcd(x, y)
        return multiply(exactDivide(x, gcd), y)
    }

    /**
     * Returns the max number k that `|b|%|a|^k==0` while
     * `|b|%|a|^(k+1)!=0`.
     *
     * @param a a number except `0,1,-1`.
     * @param b another number
     * @return deg(a, b)
     */
    fun deg(a: T, b: T): T {
        var x = a
        var y = b
        x = abs(x)
        y = abs(y)
        require(!(isZero(x) || isEqual(x, one))) { "a==0 or |a|==1" }
        var k: T = zero
        var dar = divideAndRemainder(y, x)
        while (isZero(dar.second)) {
            // b%a==0
            k = increase(k)
            y = dar.first
            // b = b/a;
            dar = divideAndRemainder(y, x)
        }
        // while(b % a ==0){
        // k++;
        // b = b/a;
        // }
        return k
    }

    /**
     * Returns `(a^n) mod m`.
     *
     *
     * For example, `powMod(2,2,3) = 1`, and
     * `powMod(3,9,7) = 6`.
     *
     * @param a a number.
     * @param n a non-negative number.
     * @param m the modular.
     */
    fun powMod(a: T, n: T, m: T): T {
        var x = a
        var p = n
        require(!isNegative(p)) { "n < 0" }

        val one = one
        if (isEqual(m, one)) {
            return zero
        }
        if (isEqual(x, one)) {
            return one
        }
        var ans = one
        val two: T = add(one, one)
        x = mod(x, m)
        while (!isZero(p)) {
            if (isOdd(p)) {
                ans = mod(multiply(x, ans), m)
            }
            x = mod(multiply(x, x), m)
            p = divideToInteger(p, two)
        }
        return ans
    }

}


