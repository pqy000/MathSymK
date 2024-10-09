package model

import TestUtils.assertEquals
import io.github.ezrnest.model.Models
import io.github.ezrnest.model.Polynomial
import io.github.ezrnest.numTh.NTFunctions
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelsTest {

    @Test
    fun testDoubleAsReals() {
        val real = Models.doubles()
        assert(real.contains(1.0))
        assertEquals(0.0, real.zero)
        assertEquals(2.0, real.add(1.0, 1.0))
        assertEquals(-1.0, real.negate(1.0))
        assertEquals(1.0 - 1.0, real.subtract(1.0, 1.0))
        assertEquals(2.0 - 1.0, real.subtract(2.0, 1.0))
    }

    @Test
    fun testIntAsIntegers() {
        val int = Models.ints()
        assert(int.contains(1))
        assertEquals(0, int.zero)
        assertEquals(2, int.add(1, 1))
        assertEquals(-1, int.negate(1))
        assertEquals(1 - 1, int.subtract(1, 1))
        assertEquals(2 - 1, int.subtract(2, 1))

        // exactDivide
        assertEquals(0, int.mod(3, 1))
        assertEquals(3, int.divToInt(3, 1))
        assertEquals(1, int.exactDiv(2, 2))
        assertEquals(-3, int.exactDiv(-3, 1))
    }

    @Test
    fun testGCD() {
        val int = Models.ints()
        with(int) {
            val (a, b) = 12 to 30
            val (g, u, v) = int.gcdUV(a, b)
            assertEquals(6, g)
            assertEquals(g, u * a + v * b)
            val gcdFull = int.gcdExtendedFull(a, b)
            assertEquals(g, gcdFull.gcd)
            assertEquals(u, gcdFull.u)
            assertEquals(v, gcdFull.v)
            assertEquals(a / g, gcdFull.ad)
            assertEquals(b / g, gcdFull.bd)
        }
    }

    @Test
    fun testGCDPoly() {
        val frac = Models.fractions()
        val polyF = Polynomial.over(frac)
        with(frac) {
            with(polyF) {
                val xP1 = x + 1.asQ
                val a = xP1.pow(3)
                val b = xP1.pow(2) * (x + 2.asQ)
                val (g, u, v) = gcdUV(a, b)
                assertEquals(xP1.pow(2).toMonic(), g.toMonic())
                assertEquals(g, u * a + v * b)
                val gcdFull = gcdExtendedFull(a, b)
                assertEquals(g, gcdFull.gcd)
                assertEquals(u, gcdFull.u)
                assertEquals(v, gcdFull.v)
                assertEquals(a / g, gcdFull.ad)
                assertEquals(b / g, gcdFull.bd)
            }
        }
    }

    @Test
    fun testIntModP() {
        with(Models.intModP(97)) {
            assertEquals(3, add(1, 2))
            assertEquals(96, subtract(1, 2))
            assertEquals(2, multiply(1, 2))
            assertEquals(49, reciprocal(2))
            assertEquals(49, divide(98, 2))
            assertThrows<ArithmeticException> { divide(98, 0) }
        }
    }

    @Test
    fun testBigInteger() {
        with(Models.bigIntegers()) {
            assertEquals(0.toBigInteger(), zero)
            assertEquals(2.toBigInteger(), add(1.toBigInteger(), 1.toBigInteger()))
            assertEquals((-1).toBigInteger(), negate(1.toBigInteger()))
            assertEquals(1.toBigInteger() - 1.toBigInteger(), subtract(1.toBigInteger(), 1.toBigInteger()))
            assertEquals(2.toBigInteger() - 1.toBigInteger(), subtract(2.toBigInteger(), 1.toBigInteger()))
        }
    }

    @Test
    fun testBigFrac() {
        val bigFrac = Models.fractionBig()
        with(bigFrac) {
            val f1 = bfrac(1, 2)
            val f2 = bfrac(1, 3)
            assertEquals(bfrac(5, 6), f1 + f2)
        }
    }

    @Test
    fun testIntModN() {
        with(Models.intModN(97)) {
            assertEquals(3, add(1, 2))
            assertEquals(96, subtract(1, 2))
            assertEquals(2, multiply(1, 2))
        }
        with(Models.intModN(Int.MAX_VALUE)) {
            assertEquals(3, add(1, 2))
            assertEquals(Int.MAX_VALUE - 1, subtract(1, 2))
            assertEquals(2, multiply(1, 2))
            assertEquals(9, add(Int.MAX_VALUE - 1, 10))
        }
        with(Models.intModN(Int.MAX_VALUE / 2 - 1)) {
            val n = Int.MAX_VALUE / 2L - 1
            assertEquals(3, add(1, 2))
            assertEquals(46341L * 46341 % n, multiply(46341, 46341).toLong())
            assertEquals(46340L * 46340 % n, multiply(46340, 46340).toLong())
        }
    }

    @Test
    fun testPowerFactor(){
        val rng = Random(100)
        repeat(100){
            with(Models.fractionBig()) {
                val f = bfrac(rng.nextInt(1, 100), rng.nextInt(1, 100))
                val (p,q) = NTFunctions.gcdReduce(rng.nextInt(1, 10),rng.nextInt(1, 10))
                val (f1, a1) = powerFactor(f, p, q)
                assertEquals(f.pow(p), f1.pow(q) * a1.bfrac)
            }
        }

    }
}