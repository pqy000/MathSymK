package cn.mathsymk.structure

import cn.mathsymk.model.Fraction


interface ComplexNumbers<R, C> : Field<C>, ProvideElementaryFunctions<C> {

    /**
     * Gets the real numbers in this complex number system.
     */
    val reals: Reals<R>

    /**
     * Returns the imaginary unit `i`.
     */
    val i: C
        get() = of(reals.zero, reals.one)

    /**
     * Returns the real part of the complex number `z`.
     */
    fun re(z: C): R

    /**
     * Returns the imaginary part of the complex number `z`.
     */
    fun im(z: C): R

    /**
     * Returns the complex number with the given real and imaginary parts.
     */
    fun of(a: R, b: R): C

    fun fromReal(a: R): C {
        return of(a, reals.zero)
    }

    fun fromImag(b: R): C {
        return of(reals.zero, b)
    }

    fun mulReal(z: C, a: R): C {
        return of(reals.multiply(re(z), a), reals.multiply(im(z), a))
    }

    fun divReal(z: C, a: R): C {
        return of(reals.divide(re(z), a), reals.divide(im(z), a))
    }

    /**
     * Returns the conjugate of the complex number `z`.
     */
    fun conj(z: C): C {
        return of(re(z), reals.negate(im(z)))
    }

    /**
     * Returns the absolute value of the complex number `z`.
     *
     * `abs(z) = sqrt(re(z)^2 + im(z)^2)`
     */
    fun abs(z: C): R {
        val a = re(z)
        val b = im(z)
        return reals.eval { sqrt(a * a + b * b) }
    }


    override fun constantValue(name: String): C {
        when (name) {
            "i" -> return i
        }
        return fromReal(reals.constantValue(name))
    }

    /**
     * Returns the
     */
    override fun sqrt(x: C): C {
        return nroot(x, 2)
    }

    override fun nroot(x: C, n: Long): C {
        return exp(x, fromReal(reals.of(Fraction(1, n))))
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
        return exp(ix) - exp(-ix) / fromImag(reals.of(2))
    }

    override fun cos(x: C): C {
        // use the formula cos(x) = (e^(ix) + e^(-ix)) / 2
        val ix = multiply(i, x)
        return exp(ix) + exp(-ix) / fromReal(reals.of(2))
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