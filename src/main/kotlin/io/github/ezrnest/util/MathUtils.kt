package io.github.ezrnest.util

import io.github.ezrnest.util.exceptions.ExceptionUtil
import java.math.BigInteger
import java.util.*
import kotlin.math.*


/**
 * Provides some useful math functions which are not included in JDK.
 *
 *
 * @author lyc
 */
object MathUtils {
    /**
     * Returns the quotient of `a/b` if it is divisible, throws an Exception otherwise.
     */
    @JvmStatic
    fun divideExact(a: Long, b: Long): Long {
        if (a % b != 0L) {
            ExceptionUtil.notExactDivision(a, b)
        }
        return a / b
    }

    /**
     * Returns the quotient of `a/b` if it is divisible, throws an Exception otherwise.
     */
    @JvmStatic
    fun divideExact(a: Int, b: Int): Int {
        if (a % b != 0) {
            ExceptionUtil.notExactDivision(a, b)
        }
        return a / b
    }

    /**
     * Calculate the square root of `n`. If n cannot be expressed as
     * a square of an integer,then `-1` will be returned. Throws an
     * exception if `n<0`
     *
     * @param n a number, positive or 0.
     * @return the positive exact square root of n,or `-1`
     */
    fun squareRootExact(n: Long): Long {
        var n = n
        if (n < 0) {
            ExceptionUtil.sqrtForNegative()
        }
        if (n == 0L) {
            return 0
        }
        if (n == 1L) {
            return 1
        }
        var re: Long = 1
        //fast even number test
        while ((n and 1L) == 0L) {
            n = n shr 1
            if ((n and 1L) != 0L) {
                return -1
            }

            n = n shr 1
            re = re shl 1
        }
        var t: Long = 3
        var t2: Long = 9
        while (t2 <= n) {
            if (n % t == 0L) {
                if (n % t2 != 0L) {
                    return -1
                }
                re *= t
                n /= t2
                continue
            }
            t += 2
            t2 = t * t
        }
        if (n != 1L) {
            return -1
        }
        return re
    }

    /**
     * Determines whether `n` is perfect square, that is, there exists `m`
     * such that `n = m^2`.
     */
    fun isPerfectSquare(n: Long): Boolean {
        if (n < 0) {
            return false
        }
        val sqrt = sqrtInt(n)
        return sqrt * sqrt == n
    }


    /**
     * Return the value of `n^p`.
     *
     * @param n a number
     * @param p a non-negative integer
     * @return `n^p`.
     * @throws ArithmeticException if `p < 0` or `p==0 && n==0`.
     */
    fun pow(n: Long, p: Int): Long {
        require(p >= 0) { "power=$p should be non-negative!" }
        if (p == 0) {
            if (n == 0L) {
                ExceptionUtil.zeroExponent()
            }
            return 1L
        }
        var n1 = n
        var p1 = p
        var re = 1L
        while (p1 > 0) {
            if ((p1 and 1) != 0) {
                re *= n1
            }
            n1 *= n1
            p1 = p1 shr 1
        }
        return re
    }

    /**
     * Return the value of `n^p`.
     *
     * @param n a number
     * @param p a non-negative integer
     * @return `n^p`.
     * @throws ArithmeticException if `p < 0` or `p==0 && n==0`.
     */
    fun pow(n: Int, p: Int): Long {
        require(p >= 0) { "power=$p should be non-negative!" }
        if (p == 0) {
            if (n == 0) {
                ExceptionUtil.zeroExponent()
            }
            return 1L
        }
        var n1 = n
        var p1 = p
        var re = 1L
        while (p1 > 0) {
            if ((p1 and 1) != 0) {
                re *= n1
            }
            n1 *= n1
            p1 = p1 shr 1
        }
        return re
    }

    /**
     * Return the value of n^p.
     *
     * @param n a number
     * @param p > -1
     * @return n ^ p
     * @throws ArithmeticException if `p < 0` or `p==0&&n==0` or the result overflows a long
     */
    fun powExact(n: Long, p: Int): Long {
        require(p >= 0) { "power=$p should be non-negative!" }
        if (p == 0) {
            if (n == 0L) throw ArithmeticException("0^0")
            return 1L
        }
        var n = n
        var p = p
        var re = 1L
        while (p > 0) {
            if ((p and 1) != 0) {
                re = Math.multiplyExact(re, n)
            }
            n = Math.multiplyExact(n, n)
            p = p shr 1
        }
        return re
    }




//    /**
//     * Turn the vector = (x,y) anticlockwise for `rad`.
//     */
//    fun turnRad(x: Double, y: Double, rad: Double): DoubleArray {
//        // x' = x cos - y sin
//        // y' = x sin + y cos
//
//        val sin = sin(rad)
//        val cos = cos(rad)
//        val xy = DoubleArray(2)
//        xy[0] = x * cos - y * sin
//        xy[1] = x * sin + y * cos
//        return xy
//    }

    /**
     * find the number `n` such that
     * `n=2^k, 2^(k-1) < num < n`
     *
     * @param num num>0
     */
    fun findMin2T(num: Int): Int {
        var n = 1
        while (n < num) {
            n = n shl 1
        }
        return n
    }


    /**
     * Return the average of a1 and a2 exactly as an integer,
     * this method is equal to (a1+a2)/2 without overflow and
     * underflow.
     *
     * @return (a1 + a2)/2
     */
    fun averageExactly(a1: Int, a2: Int): Int {
        return ((a1.toLong() + a2.toLong()) / 2).toInt()
    }

    /**
     * Returns the positive n-th root of `x`,or `-1` if it cannot be represent as
     * long.For example `rootN(1024,5) = 4`
     *
     * @param x a number
     * @param n indicate the times of root
     * @return n-th root of `x`
     * @throws IllegalArgumentException if `n<=0` or `x<0`
     */
    fun rootN(x: Long, n: Int): Long {
        //TODO better implementation
        require(n > 0) { "n<=0" }
        if (n == 1) {
            return x
        }
        if (x == 1L || x == 0L) {
            return x
        }
        var root = 2L
        //try from 2.
        var t: Long
        while (true) {
            t = pow(root, n)
            if (t == x) {
                break
            }
            if (t <= 0 || t > x) {
                root = -1
                break
            }
            root++
        }
        return root
    }


//
//    /**
//     * Produces a random long number x (x>=0 && x < bound) according to the random.
//     *
//     * @param rd    a random
//     * @param bound exclusive
//     */
//    fun randomLong(rd: Random, bound: Long): Long {
//        if (bound <= Int.MAX_VALUE) {
//            return rd.nextInt(bound.toInt()).toLong()
//        }
//        var mask = Long.MAX_VALUE
//        while (mask >= bound) {
//            mask = mask shr 1
//        }
//        mask = mask shl 1
//        mask--
//        while (true) {
//            val r = rd.nextLong() and mask
//            if (r < bound) {
//                return r
//            }
//        }
//    }

    /**
     * Returns the max number of k that `a^k <= b && a^(k+1) > b`,this
     * method requires that `|a| > 1 && b != 0`.
     *
     * This method is equal to [log(a,b)] in math.
     *
     * @param a a number , `|a| > 1`
     * @param b a number , `b != 0`
     * @return the result , non-negative
     */
    @JvmStatic
    fun maxPower(a: Long, b: Long): Int {
        var a = abs(a)
        var b = abs(b)
        require(!(a == 0L || a == 1L || b == 0L))
        var re = 0
        var p: Long = 1
        while (p <= b) {
            p *= a
            re++
            //            print(p + ",re=" + re);
        }
        return --re
    }

    /**
     * Reduce the number to an array representing each digit of the radix. The
     * `number` should be the sum of
     * <pre>result[i ] * radix^i</pre>
     *
     * @param number a positive integer
     * @param radix  an integer bigger than one
     * @return an array containing corresponding digits.
     */
    fun radix(number: Int, radix: Int): IntArray {
        var n = number
        require(n >= 0) { "number < 0" }
        checkValidRadix(radix)
        if (n < radix) {
            return intArrayOf(n)
        }
        val maxPow = maxPower(radix.toLong(), n.toLong())
        //        print(maxPow);
        val res = IntArray(maxPow + 1)
        var i = 0
        while (n > 0) {
            val t = n / radix
            res[i++] = n - radix * t
            n = t
        }
        return res
    }

    private fun checkValidRadix(radix: Int) {
        require(radix > 1) { "radix <= 1" }
    }

    /**
     * Returns a number that is equal to the sum of
     * <pre>digits[i] * radix^i</pre>
     *
     * @param digits an array of digits.
     * @param radix  an integer bigger than one
     */
    fun fromRadix(digits: IntArray, radix: Int): Int {
        checkValidRadix(radix)
        if (digits.size == 0) {
            return 0
        }
        var result = digits[digits.size - 1]
        for (i in digits.size - 2 downTo -1 + 1) {
            result *= radix
            result += digits[i]
        }
        return result
    }

    private fun checkValidRadix(radix: Long) {
        require(radix > 1) { "radix <= 1" }
    }

    /**
     * Reduce the number to an array representing each digit of the radix. The
     * `number` should be the sum of
     * <pre>result[i ] * radix^i</pre>
     *
     * @param number a positive integer
     * @param radix  an integer bigger than one
     * @return an array containing corresponding digits.
     */
    fun radix(number: Long, radix: Long): LongArray {
        var number = number
        require(number >= 0) { "number < 0" }
        checkValidRadix(radix)
        if (number < radix) {
            return longArrayOf(number)
        }
        val maxPow = maxPower(radix, number)
        //        print(maxPow);
        val res = LongArray(maxPow + 1)
        var i = 0
        while (number > 0) {
            val t = number / radix
            res[i++] = number - radix * t
            number = t
        }
        return res
    }

    /**
     * Returns a number that is equal to the sum of
     * <pre>digits[i] * radix^i</pre>
     *
     * @param digits an array of digits.
     * @param radix  an integer bigger than one
     */
    fun fromRadix(digits: LongArray, radix: Long): Long {
        checkValidRadix(radix)
        if (digits.size == 0) {
            return 0
        }
        var result = digits[digits.size - 1]
        for (i in digits.size - 2 downTo -1 + 1) {
            result *= radix
            result += digits[i]
        }
        return result
    }


    /**
     * Returns the integer value of the square root of `n`, that is, an integer `m`
     * such that `m<sup>2</sup> <= n < `(m+1)<sup>2</sup>``.
     *
     * @param n a positive number.
     * @return `[sqrt(n)]`
     */
    fun sqrtInt(n: Long): Long {
        if (n < 0) {
            throw ArithmeticException("n<0")
        }
        if (n >= 9223372030926249001L) {
            //to prevent overflow
            return 3037000499L
        }
        return sqrt(n.toDouble()).toLong()

        //        int p = 4;
//        //find the lower bound and the upper bound.
//        long high = 64L;
//        while (high < n) {
//            p += 2;
//            high *= 4;
//        }
//        p >>>= 1;
//        long low = 1L << p;
//        high = low << 1;
//        return ModelPatterns.binarySearchL(low, high, (long x) -> {
//            long sqr = x * x;
////			print(x);
//            if (sqr == n) {
//                return 0;
//            }
//            if (sqr > n) {
//                return 1;
//            }
//            if (sqr + 2 * x + 1 > n) {
//                return 0;
//            }
//            return -1;
//        });
    }


    /**
     * Returns `x*y<=0`
     */
    fun isOppositeSign(x: Double, y: Double): Boolean {
        return (x >= 0.0 && y <= 0.0) || (x <= 0.0 && y >= 0.0)
    }

    /**
     * Returns `(x-a)(y-a)<=0`
     */
    fun isOppositeSide(x: Double, y: Double, a: Double): Boolean {
        return (a in y..x) || (a in x..y)
    }


    /**
     * Determines whether x and y have the same sign.
     */
    fun isSameSign(x: Int, y: Int): Boolean {
        return if (x > 0) {
            y > 0
        } else if (x == 0) {
            y == 0
        } else { //x<0
            y < 0
        }
    }


    /**
     * Returns the sign number of `x` as an int.
     */
    fun signum(x: Double): Int {
        return x.compareTo(0.0)
    }

    fun indicator(b: Boolean): Int {
        return if (b) {
            1
        } else {
            0
        }
    }


    /**
     * Returns the so-called 'Tschebyscheff distance':
     * `max(abs(x1-x2), abs(y1-y2))`
     */
    fun tschebyscheffDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return max(abs(x1 - x2), abs(y1 - y2))
    }

    /**
     * Returns the distance of (x1,y2) and (x2,y2) defined in space Lp, which is equal to
     * <pre>(abs(x1-x2)^p+abs(y1-y2)^p)^(1/p)</pre>
     * If `p==Double.POSITIVE_INFINITY`, then returns the tschebyscheff distance.
     */
    fun distanceP(x1: Double, y1: Double, x2: Double, y2: Double, p: Double): Double {
        if (p == Double.POSITIVE_INFINITY) {
            return tschebyscheffDistance(x1, y1, x2, y2)
        }
        require(!(p <= 0)) { "p<=0" }
        val dx = abs(x1 - x2)
        val dy = abs(y1 - y2)
        return (dx.pow(p) + dy.pow(p)).pow(1 / p)
    }

    /**
     * Returns the biggest number n that meets the requirements that:
     * `n = k*p` where `k` is an integer,
     * `n <= x`.
     *
     * @param x a number
     * @param p a positive number
     */
    fun maxBelow(x: Double, p: Double): Double {
        var p = p
        p = abs(p)
        if (x < 0) {
            val t = x % p
            if (t == 0.0) {
                return x
            }
            return x - t - p
        } else {
            return x - x % p
        }
    }

    /**
     * Returns the biggest integer k that meets the requirements that:
     * `n = k*p` and
     * `n <= x`.
     *
     * @param x a positive number
     * @param p a positive number
     */
    fun maxBelowK(x: Double, p: Double): Long {
        if (p > x) {
            return 0
        }
        var n: Long = 1
        var t = p
        while (t < x) {
            t *= 2.0
            n *= 2
        }
        val d = n / 2
        while (n > d) {
            if (t <= x) {
                return n
            }
            t -= p
            n--
        }
        return d
    }


    /**
     * Returns `(-1)<sup>pow</sup>`.
     */
    fun powOfMinusOne(pow: Int): Int {
        return if (pow % 2 == 0) {
            1
        } else {
            -1
        }
    }

    /**
     * Returns `x + (y - x) * k`.
     *
     * @param k the interpolate factor.
     */
    fun interpolate(x: Double, y: Double, k: Double): Double {
        return x + (y - x) * k
    }

    fun product(array: DoubleArray): Double {
        var r = 1.0
        for (l in array) {
            r *= l
        }
        return r
    }

    fun product(array: LongArray): Long {
        return array.fold(1L, Long::times)
    }

    fun product(array: IntArray): Int {
        return array.fold(1, Int::times)
    }

    fun sum(array: DoubleArray, start: Int, end: Int): Double {
        var r = 0.0
        for (i in start until end) {
            r += array[i]
        }
        return r
    }

    fun sum(array: LongArray, start: Int, end: Int): Long {
        var r: Long = 0
        for (i in start until end) {
            r += array[i]
        }
        return r
    }

    fun sum(array: IntArray, start: Int, end: Int): Int {
        var r = 0
        for (i in start until end) {
            r += array[i]
        }
        return r
    }

    fun inner(x: DoubleArray, y: DoubleArray): Double {
        require(x.size == y.size) { "The length must be the same! Given: " + x.size + ", " + y.size }
        var result = 0.0
        for (i in x.indices) {
            result += x[i] * y[i]
        }
        return result
    }

    fun inner(x: LongArray, y: LongArray): Long {
        require(x.size == y.size) { "The length must be the same! Given: " + x.size + ", " + y.size }
        var result: Long = 0
        for (i in x.indices) {
            result += x[i] * y[i]
        }
        return result
    }

    fun inner(x: IntArray, y: IntArray): Int {
        require(x.size == y.size) { "The length must be the same! Given: " + x.size + ", " + y.size }
        var result = 0
        for (i in x.indices) {
            result += x[i] * y[i]
        }
        return result
    }

}


infix fun Int.pow(p: Int): Int {
    return MathUtils.pow(this, p).toInt()
}

infix fun Long.pow(p: Int): Long {
    return MathUtils.pow(this, p)
}

infix fun Long.powExact(p: Int): Long {
    return MathUtils.powExact(this, p)
}