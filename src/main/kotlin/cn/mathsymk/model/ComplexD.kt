package cn.mathsymk.model

import cn.mathsymk.model.struct.FieldModel
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.pow



/**
 * Describes a complex number with double precision.
 *
 * @author lyc
 */
@JvmRecord
data class ComplexD(val re: Double, val im: Double) : FieldModel<ComplexD> {
    // re-written at 2024/8/29 16:57

    override fun toString(): String {
        return if (im < 0) {
            "$re${im}i"
        } else {
            "$re+${im}i"
        }
    }


    /**
     * Returns the argument of this complex number, which falls in the range of `(-pi,pi]`.
     */
    val arg: Double
        get() = atan2(im, re)

    /**
     * Returns the modulus of this complex number.
     *
     * @return `|this|`
     */
    val mod: Double
        get() = hypot(re, im)

    /**
     * Returns the modulus of this complex number as a complex number.
     *
     * @return `|this|` as ComplexD
     */
    val modComplex: ComplexD
        get() = ComplexD(mod, 0.0)
//    fun modAsC(): ComplexD {
//        return real(mod())
//    }

    /**
     * Returns `this + y`
     *
     * @param y another complex
     * @return `this + y`
     */
    override fun plus(y: ComplexD): ComplexD {
        return ComplexD(re + y.re, im + y.im)
    }

    /**
     * Returns `-this`
     *
     * @return `-this`
     */
    override fun unaryMinus(): ComplexD {
        return ComplexD(-re, -im)
    }

    /**
     * Returns `this - y`
     *
     * @param y another complex
     * @return `this - y`
     */
    override fun minus(y: ComplexD): ComplexD {
        return ComplexD(re - y.re, im - y.im)
    }

    /**
     * Returns `this * y`
     *
     * @param y another complex
     * @return `this * y`
     */
    override fun times(y: ComplexD): ComplexD {
        return ComplexD(re * y.re - im * y.im, re * y.im + im * y.re)
    }

    operator fun times(d: Double): ComplexD {
        return ComplexD(re * d, im * d)
    }


    /**
     * Returns `this / y`
     *
     * @param y another complex
     * @return `this / y`
     * @throws ArithmeticException if z = 0
     */
    override fun div(y: ComplexD): ComplexD {
        val d = y.re * y.re + y.im * y.im
        var an = re * y.re + im * y.im
        var bn = im * y.re - re * y.im
        an /= d
        bn /= d
        return ComplexD(an, bn)
    }

    operator fun div(d: Double): ComplexD {
        return ComplexD(re / d, im / d)
    }


    /**
     * Returns `1/this`
     *
     * @return `1/this`
     */
    override fun inv(): ComplexD {
        val mod2 = re * re + im * im
        return ComplexD(re / mod2, -im / mod2)
    }


    /**
     * Returns the conjugate complex number of `this`.
     *
     */
    val conj: ComplexD
        get() = ComplexD(re, -im)


    /**
     * Returns `this^p`,this method will calculate by using angle form.
     * If `p==0`,ONE will be returned.
     *
     *
     *
     * @return `this^p`
     * @see .pow
     */
    fun powArg(p: Long): ComplexD {
        if (p == 0L) {
            return ONE
        }
        // (r,theta)^p = (r^p,p*theta)
        var arg = arg
        var m = mod
        m = m.pow(p.toDouble())
        arg *= p.toDouble()
        return modArg(m, arg)
    }


    /**
     * Returns `x<sup>y</sup> = e<sup>ln(x)*y</sup>`, where `x` is the complex number of
     * `this`.
     *
     * @param y a complex number
     */
    fun pow(y: ComplexD): ComplexD {
        return exp(y * ln(this))
    }


    //    /**
    //     * Returns <code>log<sub>x</sub>(y) = ln(y)/ln(x), where <code>x</code> is the complex number of <code>this</code>.
    //     * @param y a complex number
    //     */
    //    public ComplexResult log(ComplexI y){
    //
    //    }
    /**
     * Returns n-th roots of the complex.
     *
     * @param n must fit `n>0`
     */
    fun root(n: Long): ComplexResult {
        require(n > 0) { "n<=0" }
        TODO()
//        val arg = arg
//        var m = mod
//
//        m = kotlin.math.exp(kotlin.math.ln(m) / n)
//        return RootResult(n, m, arg)
    }

    /**
     * Returns <pre>
     * this<sup>f</sup>
    </pre> *
     *
     * @param f a Fraction
     */
    fun pow(f: Fraction): ComplexResult {
        TODO()
//        if (f.signum == 0) {
////			if(this.a == 0 && this.b == 0){
////				throw new IllegalArgumentException("0^0");
////			}
//            return RootResult(1, 1.0, arg)
//        }
//        val p: Long
//        val q: Long
//        if (f.signum == -1) {
//            p = f.deno
//            q = f.numeratorAbs
//        } else {
//            p = f.nume
//            q = f.deno
//        }
//        return pow(p).root(q)
    }

    override val isZero: Boolean
        get() = re == 0.0 && im == 0.0






//    /**
//     * Returns the point representing this Complex number,the calculator will be
//     * the default Double-calculator.
//     *
//     * @return a point
//     */
//    fun toPoint(mc: Reals<Double>?): Point<Double> {
//        return Point(mc, a, b)
//    }

//    /**
//     * Returns the vector representing this Complex number,the calculator will be
//     * the default Double-calculator.
//     *
//     * @return a vector
//     */
//    fun toVector(mc: RealCalculator<Double>?): PVector<Double> {
//        return PVector.valueOf(a, b, mc)
//    }






    companion object {
        /**
         * An useful value in complex.
         */
        private const val TWO_PI = 2 * Math.PI

        const val ANGLE_UPPER_BOUND: Double = Math.PI
        const val ANGLE_DOWNER_BOUND: Double = -Math.PI

        val ZERO: ComplexD = ComplexD(0.0, 0.0)
        val ONE: ComplexD = ComplexD(1.0, 0.0)
        val I: ComplexD = ComplexD(0.0, 1.0)
        val PI: ComplexD = ComplexD(Math.PI, 0.0)
        val E: ComplexD = ComplexD(Math.E, 0.0)


        fun real(a: Double): ComplexD {
            return ComplexD(a, 0.0)
        }

        fun imag(b: Double): ComplexD {
            return ComplexD(0.0, b)
        }


        /**
         * Returns the Complex z that `arg(z) = arg && |z| = mod`.The `arg` of this complex will be adjusted so
         * that
         * it will be in [-pi,pi] and of `mod` is negative,then it will be turned to positive and corresponding `arg` will
         * be modified.
         */
        fun modArg(mod: Double, arg: Double): ComplexD {
            val a = kotlin.math.cos(arg) * mod
            val b = kotlin.math.sin(arg) * mod
            return ComplexD(a, b)
        }

        /**
         * Returns the complex value of `e^z`.
         *
         * @param z a complex number
         * @return `e^z`
         */
        fun exp(z: ComplexD): ComplexD {
            val m = kotlin.math.exp(z.re)
            return modArg(m, z.im)
        }

        /**
         * Returns the result of `e<sup>it</sup>`.
         */
        fun expIt(t: ComplexD): ComplexD {
            val a = -t.im
            val b = t.re
            val m = kotlin.math.exp(a)
            return modArg(m, b)
        }

        /**
         * Returns the complex value of `Ln(z)`,which can be calculated as
         * <pre>
         * result = ln(|z|) + (arg(z)+2k*Pi)i
        </pre> *
         * and the primary value is
         * <pre> ln(|z|) + arg(z)i</pre>
         * The number of results is infinite, and
         * the iterator of the ComplexResult will iterate from
         *
         * @param z a complex number except 0.
         * @return the results.
         */
        fun logarithm(z: ComplexD): ComplexResult {
            TODO()
//            val main = ln(z)
//            return LogResult(main.re, main.im)
        }

        /**
         * Returns the primary value of `ln(z)`
         * <pre>
         * result = ln(|z|) + arg(z)i
        </pre> *
         */
        fun ln(z: ComplexD): ComplexD {
            val mod = z.mod
            if (mod == 0.0) {
                throw ArithmeticException("ln(0)")
            }
            val x = kotlin.math.ln(mod)
            val arg = z.arg
            return ComplexD(x, arg)
        }

        /**
         * Returns sin(z),which is defined as
         * <pre>
         * (e<sup>iz</sup> - e<sup>-iz</sup>)/2
        </pre> *
         *
         * @param z a complex
         * @return sin(z)
         */
        fun sin(z: ComplexD): ComplexD {
            val iz = ComplexD(-z.im, z.re)
            val eiz = exp(iz)
            val t = eiz.re * eiz.re + eiz.im * eiz.im
            val tt = t * 2.0
            val a = eiz.im * (t + 1) / tt
            val b = eiz.re * (t - 1) / tt
            return ComplexD(a, b)
        }

        /**
         * Returns cos(z),which is defined as
         * <pre>
         * (e<sup>iz</sup> + e<sup>-iz</sup>)/2
        </pre> *
         *
         * @param z a complex
         * @return cos(z)
         */
        fun cos(z: ComplexD): ComplexD {
            val iz = ComplexD(-z.im, z.re)
            val eiz = exp(iz)
            val t = eiz.re * eiz.re + eiz.im * eiz.im
            val tt = t * 2.0
            val a = eiz.im * (t - 1) / tt
            val b = eiz.re * (t + 1) / tt
            return ComplexD(a, b)
        }

        /**
         * Returns tan(z),which is defined as
         * <pre>
         * (e<sup>iz</sup> - e<sup>-iz</sup>)/(e<sup>iz</sup> + e<sup>-iz</sup>)
        </pre> *
         *
         * @param z a complex
         * @return tan(z)
         */
        fun tan(z: ComplexD): ComplexD {
            val iz = ComplexD(-z.im, z.re)
            val t = exp(iz)
            //a^2-b^2
            val a0 = t.re * t.re - t.im * t.im
            val b0 = 2 * t.re * t.im
            val re = ComplexD(a0 - 1, b0).div(ComplexD(a0 + 1, b0))
            return ComplexD(-re.im, re.re)
        }
    }
}



/**
 * This class describes the complex result set of multiple result functions in complex
 * calculation such as root() or so on.
 *
 *
 * In the implement of this class,usually,the results will only be calculated when
 * they are required,and they are not saved,so if the result is required for multiple times,
 * extra temptation is recommended.
 *
 * @author lyc
 */
interface ComplexResult : Iterable<ComplexD> {
    //TODO
    /**
     * Returns the main value of this result.
     *
     * @return a complex number
     */
    val mainValue: ComplexD

//    /**
//     * Returns the number of complexes in this result set,if the
//     * number of results is infinite,this method should return `-1`
//     *
//     * @return the number of results,or `-1`
//     */
//    fun number(): Long {
//        return size
//    }
//
//    open val isInfinite: Boolean
//        /**
//         * Returns `true` if the number of result.
//         */
//        get() = size == -1L


}

//private class RootResult(size: Long, private val m: Double, private val arg: Double) : ComplexResult(size) {
//    override fun iterator(): Iterator<ComplexD> {
//        return object : Iterator<ComplexD> {
//            private var index: Long = 0
//
//            override fun next(): ComplexD {
//                return modArg(m, ((index++) * TWO_PI + arg) / size)
//            }
//
//            override fun hasNext(): Boolean {
//                return index < size
//            }
//        }
//    }
//
//    override fun mainValue(): ComplexD {
//        return modArg(m, arg / size)
//    }
//
//    override val isInfinite: Boolean
//        get() = false
//
//    override fun contains(z: ComplexD): Boolean {
//        if (z.mod == m) {
//            //we use two-divide method
//            val arg = z.arg
//            var downer: Long = 0
//            var upper = size - 1
//            while (downer <= upper) {
//                val t = (downer + upper) / 2
//                val arg0 = (arg + t * TWO_PI) / size
//                if (arg0 == arg) {
//                    return true
//                } else if (arg0 < arg) {
//                    downer = t + 1
//                } else {
//                    upper = t - 1
//                }
//            }
//        }
//        return false
//    }
//}
//



//    private class LogResult(private val x: Double, private val arg: Double) : ComplexResult(-1) {
//        override fun iterator(): Iterator<ComplexD> {
//            return object : Iterator<ComplexD> {
//                var index: Long = 0
//
//                override fun hasNext(): Boolean {
//                    return true
//                }
//
//                override fun next(): ComplexD {
//                    return ComplexD(x, arg + TWO_PI * index++)
//                }
//            }
//        }
//
//        override fun mainValue(): ComplexD {
//            return ComplexD(x, arg)
//        }
//
//        override val isInfinite: Boolean
//            get() = true
//
//        override fun contains(z: ComplexD): Boolean {
//            if (z.re == x) {
//                var b = z.im
//                if (b < 0) {
//                    b = -b
//                }
//                while (b > 0) {
//                    b -= TWO_PI
//                }
//                return b == 0.0
//            }
//            return false
//        }
//    }