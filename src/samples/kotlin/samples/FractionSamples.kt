package samples

import io.github.ezrnest.model.Fraction
import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.model.Polynomial
import io.github.ezrnest.model.RFraction

fun basicFraction() {
    val a = Fraction(1, 2)
    val b = Fraction(1, 3)
    println(
        listOf(a + b, a - b, a * b, a / b) // 5/6, 1/6, 1/6, 3/2
    )
    println(listOf(a.pow(2), a.pow(-1))) // 1/4, 2
}

fun fractionsOverVariousModels() {
    // First example: fractions over integers, just as plain fractions
    val Z = NumberModels.integers()
    with(RFraction.over(Z)) {
        val f1 = frac(3, 4)
        val f2 = frac(1, 2)
        println(f1 + f2) // 5/4
    }

    // Second example: fractions over polynomials
    val polyF = Polynomial.over(Z) // Polynomials over Z
    with(polyF) {
        with(RFraction.over(polyF)) { // Fraction of polynomials over Z
            val f1 = (1 + x) / (1 + 2.x) // an overloaded operator `/` to create a fraction
            val f2 = (2 + 3.x) / (1 + 2.x)
            println(f1 + f2) // (3 + 4x)/(1 + 2x)
            println(f1 / f2) // (1 + x)/(2 + 3x)

            val f3 = x / (1 + 2.x)
            println(f1 + f3) // 1

            val f4 = (1 + x) / (1 + 3.x)
            println(f1 + f4) // (5*x^2 + 7*x + 2)/(6*x^2 + 5*x + 1)
        }
    }
}

fun main() {
    fractionsOverVariousModels()
}