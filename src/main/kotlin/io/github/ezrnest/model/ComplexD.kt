package io.github.ezrnest.model

import io.github.ezrnest.model.struct.FieldModel
import io.github.ezrnest.structure.ComplexNumbers
import io.github.ezrnest.structure.Reals
import io.github.ezrnest.util.ModelPatterns
import java.util.*
import kotlin.math.*


/**
 * Describe the complex numbers in `double`.
 *
 * @author lyc
 */
@JvmRecord
data class ComplexD(val re: Double, val im: Double) : FieldModel<ComplexD>,Formattable {
    // re-written at 2024/8/29 16:57

    override fun toString(): String {
        return if (im < 0) {
            "$re${im}i"
        } else {
            "$re+${im}i"
        }
    }

    override fun formatTo(formatter: Formatter, flags: Int, width: Int, precision: Int) {
        formatter.format("%.${precision}f%+.${precision}fi", re, im)
    }


    override val isZero: Boolean
        get() = re == 0.0 && im == 0.0

    /**
     * Returns the argument of this complex number, which falls in the range of `[-π,π]`.
     *
     * Note: There is no strict guarantee of the choice of `-π` or `π` when the argument is `±π`.
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
     * Returns the square of the modulus of this complex number.
     */
    val modSq: Double
        get() = re * re + im * im

    /**
     * Returns the modulus of this complex number as a complex number.
     *
     * @return `|this|` as ComplexD
     */
    val modComplex: ComplexD
        get() = ComplexD(mod, 0.0)

    /**
     * Returns `this + y`
     *
     * @param y another complex
     * @return `this + y`
     */
    override fun plus(y: ComplexD): ComplexD {
        return ComplexD(re + y.re, im + y.im)
    }

    operator fun plus(d: Double): ComplexD {
        return ComplexD(re + d, im)
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
        val r2 = y.re * y.re + y.im * y.im
        val a = (re * y.re + im * y.im)/r2
        val b = (im * y.re - re * y.im)/r2
        return ComplexD(a, b)
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
        val mod2 = modSq
        return ComplexD(re / mod2, -im / mod2)
    }


    /**
     * Returns the conjugate complex number of `this`.
     *
     */
    val conj: ComplexD
        get() = ComplexD(re, -im)

    override fun pow(n: Long): ComplexD {
        if (n == 0L) return ONE
        if(n < 0) return inv().pow(-n)
        return ModelPatterns.binaryProduce(n, this,ComplexD::times)
    }


    /**
     * Returns one of the square roots of this complex number.
     */
    fun sqrt(): ComplexD {
        return root(2)
    }

    /**
     * Returns one of the `n`-th roots of this complex number.
     *
     * @param n the root number, positive integer
     */
    fun root(n: Long): ComplexD {
        require(n > 0) { "n=$n should be positive" }
        val arg = arg / n
        val m = exp(ln(mod) / n)
        return modArg(m, arg)
    }

    fun pow(f: Fraction): ComplexD {
        return pow(f.toDouble())
    }


    fun pow(p : Double) : ComplexD{
        val arg = arg * p
        val m = mod.pow(p)
        return modArg(m, arg)
    }

    /**
     * Returns `x^y = exp(y * ln(x))`, where `ln(x)` is chosen to be the principal value of the natural logarithm.
     *
     * @param y a complex number
     */
    fun pow(y: ComplexD): ComplexD {
        return exp(y * ln(this))
    }




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
//        /**
//         * An useful value in complex.
//         */
//        private const val TWO_PI = kotlin.math.PI * 2

        val ZERO: ComplexD = ComplexD(0.0, 0.0)

        val ONE: ComplexD = ComplexD(1.0, 0.0)

        /**
         * The imaginary unit.
         */
        val I: ComplexD = ComplexD(0.0, 1.0)

        val PI: ComplexD = ComplexD(Math.PI, 0.0)
        val E: ComplexD = ComplexD(Math.E, 0.0)


        fun real(a: Double): ComplexD {
            return ComplexD(a, 0.0)
        }

        fun imag(b: Double): ComplexD {
            return ComplexD(0.0, b)
        }

        private fun iz(z: ComplexD): ComplexD {
            return ComplexD(-z.im, z.re)
        }


        /**
         * Returns `z = r * e^(i*theta) = r * (cos(theta) + i*sin(theta))`.
         */
        fun modArg(r: Double, theta: Double): ComplexD {
            val a = cos(theta) * r
            val b = sin(theta) * r
            return ComplexD(a, b)
        }

        /**
         * Returns the complex value of `e^z = e^(a+bi) = e^a * e^(bi) = e^a * (cos(b) + i*sin(b))`.
         *
         * The resulting complex number will have the modulus of `e^a` and the argument of `b (mod 2π)`.
         *
         * @param z a complex number
         */
        fun exp(z: ComplexD): ComplexD {
            val m = exp(z.re)
            return modArg(m, z.im)
        }

        /**
         * Returns the result of `exp(it) = cos(t) + i*sin(t)`.
         */
        fun expIt(t: Double): ComplexD {
            return ComplexD(cos(t), sin(t))
        }

        /**
         * Returns `base^pow = exp(pow * ln(base))`, where `ln(base)` is chosen to be the principal value of the natural logarithm.
         *
         */
        fun exp(base: ComplexD, pow: ComplexD): ComplexD {
            return base.pow(pow)
        }


        fun exp(base : Double, pow: ComplexD): ComplexD {
            return exp(pow * ln(base))
        }

        /**
         * Returns the complex value of `sinh(z)`:
         *
         *    sinh(z) = [e^z - e^(-z)]/2
         */
        fun sinh(z: ComplexD): ComplexD {
            return (exp(z) - exp(-z)) / 2.0
        }

        /**
         * Returns the complex value of `cosh(z)`:
         *
         *    cosh(z) = [e^z + e^(-z)]/2
         */
        fun cosh(z: ComplexD): ComplexD {
            return (exp(z) + exp(-z)) / 2.0
        }

        fun tanh(z: ComplexD): ComplexD {
            val ez = exp(z)
            val e_z = exp(-z)
            return (ez - e_z) / (ez + e_z)
        }

        /**
         * Returns `sin(z)`:
         *
         *     sin(z) = [exp(iz) - exp(-iz)] / (2i)
         *
         * @param z in
         */
        fun sin(z: ComplexD): ComplexD {
            // use the formula sin(z) = (e^(iz) - e^(-iz)) / (2i)
            val iz = iz(z)
            val t = exp(iz) - exp(-iz)
            return ComplexD(t.im/ 2.0, -t.re/ 2.0)
        }

        /**
         * Returns `cos(z)`:
         *
         *    cos(z) = [exp(iz) + exp(-iz)] / 2
         */
        fun cos(z: ComplexD): ComplexD {
            val iz = ComplexD(-z.im, z.re)
            return (exp(iz) + exp(-iz)) / 2.0
        }


        /**
         * Returns `tan(z)`:
         *
         *    tan(z) = sin(z)/cos(z) = [exp(iz) - exp(-iz)]/[exp(iz) + exp(-iz)] / i
         *
         * @param z a complex
         * @return tan(z)
         */
        fun tan(z: ComplexD): ComplexD {
            val iz = ComplexD(-z.im, z.re)
            val ez = exp(iz)
            val e_z = exp(-iz)
            // = i [exp(-iz) - exp(iz)]/[exp(iz) + exp(-iz)]
            return iz((e_z - ez) / (ez + e_z))
        }

        /**
         * Returns `cot(z)`:
         *
         *    cot(z) = cos(z)/sin(z) = i[exp(iz) + exp(-iz)]/[exp(iz) - exp(-iz)]
         */
        fun cot(z: ComplexD): ComplexD {
            val iz = ComplexD(-z.im, z.re)
            val ez = exp(iz)
            val e_z = exp(-iz)
            return iz((ez + e_z) / (ez - e_z))
        }

        /**
         * Returns the primary value of `ln(z)`:
         *
         *     ln(z) = ln(|z|) + arg(z) i
         *
         */
        fun ln(z: ComplexD): ComplexD {
            val x = ln(z.mod)
            val arg = z.arg
            return ComplexD(x, arg)
        }

        fun log(base: ComplexD, x: ComplexD): ComplexD {
            return ln(x) / ln(base)
        }



        //TODO: implement complex functions with multiple branches

//        /**
//         * Returns the complex value of `Ln(z)`,which can be calculated as
//         * <pre>
//         * result = ln(|z|) + (arg(z)+2k*Pi)i
//        </pre> *
//         * and the primary value is
//         * <pre> ln(|z|) + arg(z)i</pre>
//         * The number of results is infinite, and
//         * the iterator of the ComplexResult will iterate from
//         *
//         * @param z a complex number except 0.
//         * @return the results.
//         */
//        fun logarithm(z: ComplexD): ComplexResult {
////            val main = ln(z)
////            return LogResult(main.re, main.im)
//        }






    }
}


class ComplexDModel(dev: Double) : ComplexNumbers<Double, ComplexD> {
    override val reals: Reals<Double> = NumberModels.doubles(dev)

    override fun contains(x: ComplexD): Boolean {
        return true
    }

    override fun isEqual(x: ComplexD, y: ComplexD): Boolean {
        return reals.isEqual(x.re, y.re) && reals.isEqual(x.im, y.im)
    }

    override fun of(a: Double, b: Double): ComplexD {
        return ComplexD(a, b)
    }

    override fun re(z: ComplexD): Double {
        return z.re
    }

    override fun im(z: ComplexD): Double {
        return z.im
    }

    override fun ofReal(a: Double): ComplexD {
        return ComplexD(a, 0.0)
    }

    override fun ofImag(b: Double): ComplexD {
        return ComplexD(0.0, b)
    }

    override fun conj(z: ComplexD): ComplexD {
        return z.conj
    }

    override fun scalarMul(k: Double, v: ComplexD): ComplexD {
        return v * k
    }

    override fun scalarDiv(x: ComplexD, k: Double): ComplexD {
        return x / k
    }

    override fun modSq(z: ComplexD): Double {
        return z.modSq
    }

    override fun abs(z: ComplexD): Double {
        return z.mod
    }

    override val one: ComplexD
        get() = ComplexD.ONE

    override val zero: ComplexD
        get() = ComplexD.ZERO

    override fun negate(x: ComplexD): ComplexD {
        return -x
    }


    override fun add(x: ComplexD, y: ComplexD): ComplexD {
        return x + y
    }


    override fun multiply(x: ComplexD, y: ComplexD): ComplexD {
        return x * y
    }

    override fun reciprocal(x: ComplexD): ComplexD {
        return x.inv()
    }

    override fun sqrt(x: ComplexD): ComplexD {
        return x.sqrt()
    }

    override fun nroot(x: ComplexD, n: Long): ComplexD {
        return x.root(n)
    }

    override fun exp(base: ComplexD, pow: ComplexD): ComplexD {
        return base.pow(pow)
    }

    override fun exp(x: ComplexD): ComplexD {
        return ComplexD.exp(x)
    }

    override fun log(base: ComplexD, x: ComplexD): ComplexD {
        return ComplexD.log(base, x)
    }

    override fun ln(x: ComplexD): ComplexD {
        return ComplexD.ln(x)
    }

    override fun sin(x: ComplexD): ComplexD {
        return ComplexD.sin(x)
    }

    override fun cos(x: ComplexD): ComplexD {
        return ComplexD.cos(x)
    }

    override fun tan(x: ComplexD): ComplexD {
        return ComplexD.tan(x)
    }

    override fun cot(x: ComplexD): ComplexD {
        return ComplexD.cot(x)
    }

    override fun arctan2(y: ComplexD, x: ComplexD): ComplexD {
        return arctan(y / x)
    }
}