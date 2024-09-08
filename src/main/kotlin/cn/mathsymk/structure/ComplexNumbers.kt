package cn.mathsymk.structure

import cn.mathsymk.model.Fraction

//interface ComplexOverUnitRing<R, C> : UnitRing<C>, Module<R, C> {
//    override val scalars: UnitRing<R>
//
//    override val zero: C get() = ofReal(scalars.zero)
//
//    override val one: C get() = ofReal(scalars.one)
//
//
//
//    override fun scalarMul(k: R, v: C): C {
//        return of(scalars.multiply(k, re(v)), scalars.multiply(k, im(v)))
//    }
//
//    override fun isEqual(x: C, y: C): Boolean {
//        return scalars.isEqual(re(x), re(y)) && scalars.isEqual(im(x), im(y))
//    }
//
//    override fun negate(x: C): C {
//        return of(scalars.negate(re(x)), scalars.negate(im(x)))
//    }
//
//    override fun contains(x: C): Boolean {
//        return scalars.contains(re(x)) && scalars.contains(im(x))
//    }
//
//
//    override fun add(x: C, y: C): C {
//        return of(scalars.add(re(x), re(y)), scalars.add(im(x), im(y)))
//    }
//
//    override fun multiply(x: C, y: C): C {
//        val a1 = re(x)
//        val b1 = im(x)
//        val a2 = re(y)
//        val b2 = im(y)
//        return scalars.eval { of(a1 * a2 - b1 * b2, a1 * b2 + b1 * a2) }
//    }
//}

/**
 * Describes the complex numbers ℂ of type [C], associated with the real numbers [R].
 */
interface ComplexNumbers<R, C> : Field<C>, Algebra<R, C>,
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
    val i: C
        get() = of(scalars.zero, scalars.one)

    /**
     * Returns `a + bi`, the complex number with the given real and imaginary parts.
     */
    fun of(a: R, b: R): C

    /**
     * Returns the real part of the complex number `z`.
     */
    fun re(z: C): R

    /**
     * Returns the imaginary part of the complex number `z`.
     */
    fun im(z: C): R

    /**
     * Returns `a + 0i`, the complex number with the given real part and zero imaginary part:
     *
     */
    fun ofReal(a: R): C

    /**
     * Returns `0 + bi`, the complex number with the given imaginary part and zero real part:
     */
    fun ofImag(b: R): C

    /**
     * Returns the conjugate of the complex number `z`.
     */
    fun conj(z: C): C

    /**
     * Multiplies the complex number `z` by the real number `k`.
     */
    override fun scalarMul(k: R, v: C): C

    /**
     * Divides the complex number `z` by the real number `k`.
     */
    override fun scalarDiv(x: C, k: R): C

    /**
     * Returns the modulus squared of the complex number `z`.
     *
     * `|z|^2 = re(z)^2 + im(z)^2`
     */
    fun modSq(z: C): R

    /**
     * Returns the absolute value of the complex number `z`.
     *
     * `abs(z) = sqrt(re(z)^2 + im(z)^2)`
     */
    fun abs(z: C): R


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
        return exp(ix) - exp(-ix) / ofImag(reals.of(2))
    }

    override fun cos(x: C): C {
        // use the formula cos(x) = (e^(ix) + e^(-ix)) / 2
        val ix = multiply(i, x)
        return exp(ix) + exp(-ix) / ofReal(reals.of(2))
    }

    override fun tan(x: C): C {
        throw UnsupportedOperationException("Not yet implemented")
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