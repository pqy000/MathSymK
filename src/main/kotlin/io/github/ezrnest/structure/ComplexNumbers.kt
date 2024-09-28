package io.github.ezrnest.structure

import io.github.ezrnest.model.Fraction


interface ComplexOverURing<T, C> : UnitRing<C>, Module<T, C> {

    override val scalars: UnitRing<T>


    override fun isEqual(x: C, y: C): Boolean {
        return scalars.isEqual(re(x), re(y)) && scalars.isEqual(im(x), im(y))
    }

    /**
     * Returns the complex number with the given real and imaginary parts.
     */
    fun complexOf(re: T, im: T): C

    override val zero: C
        get() = ofReal(scalars.zero)

    override fun isZero(x: C): Boolean {
        return scalars.isZero(re(x)) && scalars.isZero(im(x))
    }

    override val one: C
        get() = ofReal(scalars.one)

    /**
     * Returns the imaginary unit `i`.
     */
    val i: C
        get() = complexOf(scalars.zero, scalars.one)

    /**
     * Gets a complex number with the imaginary part being the given scalar.
     */
    val T.i : C
        get() = ofImag(this)

    /**
     * Returns `a + 0i`, the complex number with the given real part and zero imaginary part:
     *
     */
    fun ofReal(a: T): C {
        return complexOf(a, scalars.zero)
    }

    /**
     * Returns `0 + bi`, the complex number with the given imaginary part and zero real part:
     */
    fun ofImag(b: T): C {
        return complexOf(scalars.zero, b)
    }

    /**
     * Returns the real part of the complex number `z`.
     */
    fun re(z: C): T

    /**
     * Returns the imaginary part of the complex number `z`.
     */
    fun im(z: C): T

    /**
     * Returns the conjugate of the complex number `z`.
     */
    fun conj(z: C): C = complexOf(re(z), scalars.negate(im(z)))

    /**
     * Returns the modulus squared of the complex number `z`.
     *
     * `|z|^2 = re(z)^2 + im(z)^2`
     */
    fun modSq(z: C): T {
        val a = re(z)
        val b = im(z)
        return scalars.eval { a * a + b * b }
    }


    override fun scalarMul(k: T, v: C): C = complexOf(scalars.multiply(k, re(v)), scalars.multiply(k, im(v)))

    override fun add(x: C, y: C): C {
        return complexOf(scalars.add(re(x), re(y)), scalars.add(im(x), im(y)))
    }


    override fun negate(x: C): C {
        return complexOf(scalars.negate(re(x)), scalars.negate(im(x)))
    }

    override fun subtract(x: C, y: C): C {
        return complexOf(scalars.subtract(re(x), re(y)), scalars.subtract(im(x), im(y)))
    }

    override fun multiply(x: C, y: C): C {
        val a = re(x)
        val b = im(x)
        val c = re(y)
        val d = im(y)
        return scalars.eval { complexOf(a * c - b * d, a * d + b * c) }
    }


    override fun exactDiv(a: C, b: C): C {
        val x1 = re(a)
        val y1 = im(a)
        val x2 = re(b)
        val y2 = im(b)
        val r2 = scalars.eval { x2 * x2 + y2 * y2 }
        val real = scalars.eval { exactDiv(x1 * x2 + y1 * y2, r2) }
        val imag = scalars.eval { exactDiv(y1 * x2 - x1 * y2, r2) }
        return complexOf(real, imag)
    }

    override fun multiplyN(x: C, n: Long): C {
        return complexOf(scalars.multiplyN(re(x), n), scalars.multiplyN(im(x), n))
    }

    override fun isUnit(x: C): Boolean {
        return scalars.isUnit(modSq(x))
    }

    override fun ofN(n: Long): C {
        return ofReal(scalars.ofN(n))
    }

}


interface ComplexOverField<T, C> : Field<C>, DivisionAlgebra<T, C>, ComplexOverURing<T, C> {
    override val characteristic: Long?
        get() = scalars.characteristic

    override fun reciprocal(x: C): C {
        val a = re(x)
        val b = im(x)
        val modSq = scalars.eval { a * a + b * b }
        return scalars.eval { complexOf(a / modSq, -b / modSq) }
    }

    override fun exactDiv(a: C, b: C): C {
        return divide(a, b)
    }

    override fun isUnit(x: C): Boolean {
        return !isZero(x)
    }

    override fun divide(x: C, y: C): C {
        val a = re(x)
        val b = im(x)
        val c = re(y)
        val d = im(y)
        val r2 = scalars.eval { c * c + d * d }
        val real = scalars.eval { (a * c + b * d) / r2 }
        val imag = scalars.eval { (b * c - a * d) / r2 }
        return complexOf(real, imag)
    }

}

/**
 * Describes the complex numbers ℂ of type [C], associated with the real numbers [R].
 */
interface ComplexNumbers<R, C> : Field<C>, UnitAlgebra<R, C>, ComplexOverField<R, C>,
    ProvideElementaryFunctions<C> {


    override val characteristic: Long
        get() = 0

    override val scalars: Reals<R>
        get() = reals

    /**
     * Gets the real numbers in this complex number system.
     *
     * @see scalars
     */
    val reals: Reals<R>

    /**
     * Returns the imaginary unit `i`.
     */
    override val i: C
        get() = complexOf(scalars.zero, scalars.one)

    /**
     * Returns the complex number with the given real and imaginary parts.
     */
    override fun complexOf(re: R, im: R): C


    /**
     * Multiplies the complex number `z` by the real number `k`.
     */
    override fun scalarMul(k: R, v: C): C

    /**
     * Divides the complex number `z` by the real number `k`.
     */
    override fun scalarDiv(x: C, k: R): C


    /**
     * Returns the absolute value of the complex number `z`.
     *
     * `abs(z) = sqrt(re(z)^2 + im(z)^2)`
     */
    fun abs(z: C): R {
        return reals.sqrt(modSq(z))
    }


    override fun constantValue(name: String): C {
        when (name) {
            "i" -> return i
        }
        return ofReal(reals.constantValue(name))
    }

    /**
     * Returns the
     */
    override fun sqrt(x: C): C {
        return nroot(x, 2)
    }

    override fun nroot(x: C, n: Long): C {
        return exp(x, ofReal(reals.of(Fraction(1, n))))
    }

    override fun exp(base: C, pow: C): C {
        return exp(pow * ln(base))
    }

    override fun exp(x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun log(base: C, x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun ln(x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun sin(x: C): C {
        // use the formula sin(x) = (e^(ix) - e^(-ix)) / (2i)
        val ix = multiply(i, x)
        return exp(ix) - exp(-ix) / ofImag(reals.ofN(2))
    }

    override fun cos(x: C): C {
        // use the formula cos(x) = (e^(ix) + e^(-ix)) / 2
        val ix = multiply(i, x)
        return exp(ix) + exp(-ix) / ofReal(reals.ofN(2))
    }

    override fun tan(x: C): C {
//        val ix = multiply(i, x)
        return sin(x) / cos(x)
    }

    override fun cot(x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun arcsin(x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun arccos(x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun arctan(x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun arctan2(y: C, x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
    }

    //TODO special functions
}