package io.github.ezrnest.mathsymk.model

import io.github.ezrnest.mathsymk.model.struct.FieldModel
import io.github.ezrnest.mathsymk.structure.ComplexNumbers
import io.github.ezrnest.mathsymk.structure.Reals
import io.github.ezrnest.mathsymk.util.ModelPatterns
import java.util.*
import kotlin.math.*


/**
 * Describe the complex numbers in `double`.
 *
 * @author lyc
 */
@JvmRecord
data class Complex128(val re: Double, val im: Double) : FieldModel<Complex128>, Formattable {
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
    val modComplex: Complex128
        get() = Complex128(mod, 0.0)

    /**
     * Returns `this + y`
     *
     * @param y another complex
     * @return `this + y`
     */
    override fun plus(y: Complex128): Complex128 {
        return Complex128(re + y.re, im + y.im)
    }

    operator fun plus(d: Double): Complex128 {
        return Complex128(re + d, im)
    }

    /**
     * Returns `-this`
     *
     * @return `-this`
     */
    override fun unaryMinus(): Complex128 {
        return Complex128(-re, -im)
    }

    /**
     * Returns `this - y`
     *
     * @param y another complex
     * @return `this - y`
     */
    override fun minus(y: Complex128): Complex128 {
        return Complex128(re - y.re, im - y.im)
    }

    /**
     * Returns `this * y`
     *
     * @param y another complex
     * @return `this * y`
     */
    override fun times(y: Complex128): Complex128 {
        return Complex128(re * y.re - im * y.im, re * y.im + im * y.re)
    }

    operator fun times(d: Double): Complex128 {
        return Complex128(re * d, im * d)
    }


    /**
     * Returns `this / y`
     *
     * @param y another complex
     * @return `this / y`
     * @throws ArithmeticException if z = 0
     */
    override fun div(y: Complex128): Complex128 {
        val r2 = y.re * y.re + y.im * y.im
        val a = (re * y.re + im * y.im) / r2
        val b = (im * y.re - re * y.im) / r2
        return Complex128(a, b)
    }

    operator fun div(d: Double): Complex128 {
        return Complex128(re / d, im / d)
    }


    /**
     * Returns `1/this`
     *
     * @return `1/this`
     */
    override fun inv(): Complex128 {
        val mod2 = modSq
        return Complex128(re / mod2, -im / mod2)
    }


    /**
     * Returns the conjugate complex number of `this`.
     *
     */
    val conj: Complex128
        get() = Complex128(re, -im)

    override fun pow(n: Long): Complex128 {
        if (n == 0L) return ONE
        if (n < 0) return inv().pow(-n)
        return ModelPatterns.binaryProduce(n, this, Complex128::times)
    }


    /**
     * Returns one of the square roots of this complex number.
     */
    fun sqrt(): Complex128 {
        return root(2)
    }

    /**
     * Returns one of the `n`-th roots of this complex number.
     *
     * @param n the root number, positive integer
     */
    fun root(n: Int): Complex128 {
        require(n > 0) { "n=$n should be positive" }
        val arg = arg / n
        val m = exp(ln(mod) / n)
        return modArg(m, arg)
    }

    fun pow(f: Fraction): Complex128 {
        return pow(f.toDouble())
    }


    fun pow(p: Double): Complex128 {
        val arg = arg * p
        val m = mod.pow(p)
        return modArg(m, arg)
    }

    /**
     * Returns `x^y = exp(y * ln(x))`, where `ln(x)` is chosen to be the principal value of the natural logarithm.
     *
     * @param y a complex number
     */
    fun pow(y: Complex128): Complex128 {
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

        val ZERO: Complex128 = Complex128(0.0, 0.0)

        val ONE: Complex128 = Complex128(1.0, 0.0)

        /**
         * The imaginary unit.
         */
        val I: Complex128 = Complex128(0.0, 1.0)

        val PI: Complex128 = Complex128(Math.PI, 0.0)
        val E: Complex128 = Complex128(Math.E, 0.0)


        fun real(a: Double): Complex128 {
            return Complex128(a, 0.0)
        }

        fun imag(b: Double): Complex128 {
            return Complex128(0.0, b)
        }

        private fun iz(z: Complex128): Complex128 {
            return Complex128(-z.im, z.re)
        }


        /**
         * Returns `z = r * e^(i*theta) = r * (cos(theta) + i*sin(theta))`.
         */
        fun modArg(r: Double, theta: Double): Complex128 {
            val a = cos(theta) * r
            val b = sin(theta) * r
            return Complex128(a, b)
        }

        /**
         * Returns the complex value of `e^z = e^(a+bi) = e^a * e^(bi) = e^a * (cos(b) + i*sin(b))`.
         *
         * The resulting complex number will have the modulus of `e^a` and the argument of `b (mod 2π)`.
         *
         * @param z a complex number
         */
        fun exp(z: Complex128): Complex128 {
            val m = exp(z.re)
            return modArg(m, z.im)
        }

        /**
         * Returns the result of `exp(it) = cos(t) + i*sin(t)`.
         */
        fun expIt(t: Double): Complex128 {
            return Complex128(cos(t), sin(t))
        }

        /**
         * Returns `base^pow = exp(pow * ln(base))`, where `ln(base)` is chosen to be the principal value of the natural logarithm.
         *
         */
        fun exp(base: Complex128, pow: Complex128): Complex128 {
            return base.pow(pow)
        }


        fun exp(base: Double, pow: Complex128): Complex128 {
            return exp(pow * ln(base))
        }

        /**
         * Returns the complex value of `sinh(z)`:
         *
         *    sinh(z) = [e^z - e^(-z)]/2
         */
        fun sinh(z: Complex128): Complex128 {
            return (exp(z) - exp(-z)) / 2.0
        }

        /**
         * Returns the complex value of `cosh(z)`:
         *
         *    cosh(z) = [e^z + e^(-z)]/2
         */
        fun cosh(z: Complex128): Complex128 {
            return (exp(z) + exp(-z)) / 2.0
        }

        fun tanh(z: Complex128): Complex128 {
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
        fun sin(z: Complex128): Complex128 {
            // use the formula sin(z) = (e^(iz) - e^(-iz)) / (2i)
            val iz = iz(z)
            val t = exp(iz) - exp(-iz)
            return Complex128(t.im / 2.0, -t.re / 2.0)
        }

        /**
         * Returns `cos(z)`:
         *
         *    cos(z) = [exp(iz) + exp(-iz)] / 2
         */
        fun cos(z: Complex128): Complex128 {
            val iz = Complex128(-z.im, z.re)
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
        fun tan(z: Complex128): Complex128 {
            val iz = Complex128(-z.im, z.re)
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
        fun cot(z: Complex128): Complex128 {
            val iz = Complex128(-z.im, z.re)
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
        fun ln(z: Complex128): Complex128 {
            val x = ln(z.mod)
            val arg = z.arg
            return Complex128(x, arg)
        }

        fun log(base: Complex128, x: Complex128): Complex128 {
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


class ComplexDModel(dev: Double) : ComplexNumbers<Double, Complex128> {
    override val reals: Reals<Double> = Models.doubles(dev)

    override fun contains(x: Complex128): Boolean {
        return true
    }

    override fun isEqual(x: Complex128, y: Complex128): Boolean {
        return reals.isEqual(x.re, y.re) && reals.isEqual(x.im, y.im)
    }

    override fun complexOf(re: Double, im: Double): Complex128 {
        return Complex128(re, im)
    }

    override fun re(z: Complex128): Double {
        return z.re
    }

    override fun im(z: Complex128): Double {
        return z.im
    }

    override fun ofReal(a: Double): Complex128 {
        return Complex128(a, 0.0)
    }

    override fun ofImag(b: Double): Complex128 {
        return Complex128(0.0, b)
    }

    override fun conj(z: Complex128): Complex128 {
        return z.conj
    }

    override fun scalarMul(k: Double, v: Complex128): Complex128 {
        return v * k
    }

    override fun scalarDiv(x: Complex128, k: Double): Complex128 {
        return x / k
    }

    override fun modSq(z: Complex128): Double {
        return z.modSq
    }

    override fun abs(z: Complex128): Double {
        return z.mod
    }

    override val one: Complex128
        get() = Complex128.ONE

    override val zero: Complex128
        get() = Complex128.ZERO

    override fun negate(x: Complex128): Complex128 {
        return -x
    }


    override fun add(x: Complex128, y: Complex128): Complex128 {
        return x + y
    }


    override fun multiply(x: Complex128, y: Complex128): Complex128 {
        return x * y
    }

    override fun reciprocal(x: Complex128): Complex128 {
        return x.inv()
    }

    override fun sqrt(x: Complex128): Complex128 {
        return x.sqrt()
    }

    override fun nroot(x: Complex128, n: Int): Complex128 {
        return x.root(n)
    }

    override fun exp(base: Complex128, pow: Complex128): Complex128 {
        return base.pow(pow)
    }

    override fun exp(x: Complex128): Complex128 {
        return Complex128.exp(x)
    }

    override fun log(base: Complex128, x: Complex128): Complex128 {
        return Complex128.log(base, x)
    }

    override fun ln(x: Complex128): Complex128 {
        return Complex128.ln(x)
    }

    override fun sin(x: Complex128): Complex128 {
        return Complex128.sin(x)
    }

    override fun cos(x: Complex128): Complex128 {
        return Complex128.cos(x)
    }

    override fun tan(x: Complex128): Complex128 {
        return Complex128.tan(x)
    }

    override fun cot(x: Complex128): Complex128 {
        return Complex128.cot(x)
    }

    override fun arctan2(y: Complex128, x: Complex128): Complex128 {
        return arctan(y / x)
    }
}