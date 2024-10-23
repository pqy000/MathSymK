package samples

import io.github.ezrnest.mathsymk.linear.Matrix
import io.github.ezrnest.mathsymk.linear.MatrixImpl
import io.github.ezrnest.mathsymk.linear.T
import io.github.ezrnest.mathsymk.linear.Vector
import io.github.ezrnest.mathsymk.model.Multinomial
import io.github.ezrnest.mathsymk.model.Models
import io.github.ezrnest.mathsymk.model.Polynomial.Companion.substitute
import io.github.ezrnest.mathsymk.numTh.NTFunctions
import io.github.ezrnest.mathsymk.structure.eval
import io.github.ezrnest.mathsymk.util.IterUtils
import io.github.ezrnest.mathsymk.util.MathUtils
import kotlin.math.cos
import kotlin.random.Random

fun computingDeterminantSymbolic() {
    val ℤ = Models.ints()
    val mult = Multinomial.over(ℤ)
    with(Matrix.over(mult)) {
        val A = Matrix(3) { i, j ->
            mult.monomial("a${i + 1}${j + 1}")
        }
        val det = A.det()
        println("Matrix A:")
        println(A)
        println("Determinant of A:")
        println(det.toString())
    }
}

fun matrixMultiplication() {
    val ℤ = Models.ints()
    with(Matrix.over(ℤ)) {
        val n = 3
        val A = Matrix(n) { i, j -> (5 * cos(i + 2.0 * j)).toInt() } // generate a non-singular matrix
        println("Matrix A:")
        println(A)
        val B = A * A.T // Matrix multiplication and transpose
        println("det(A) = ${A.det()}; det(A*A.T) = ${B.det()}") // determinant
        val u = Vector(n) { i -> i + 1 }
        println(A * u) // matrix-vector multiplication
        println(u.T * B * u) // quadratic form
    }
}


fun matCharPoly(){
    val ℤ = Models.ints()
    val n = 4
    with(Matrix.over(ℤ,n)){
        val A = Matrix(n) { i, j -> i + 2 * j }
        val p = A.charPoly() // the characteristic polynomial of A, p(λ) = det(λI - A)
        println(p.toString(ch = "λ"))
        println("Substitute A into the polynomial, is it zero?")
        println(isZero(p.substitute(A, this@with))) // p(A) = 0, a matrix of zeros
    }
}

fun matrixCharacteristicPolynomials() {
    val ℤ = Models.ints()
    val n = 4
    with(Matrix.over(ℤ, n)) {
        val A = Matrix(n) { i, j -> i + 2 * j }
        println("Matrix A:")
        println(A)
        val p = A.charPoly() // the characteristic polynomial of A, p(λ) = det(λI - A)
        println("Characteristic polynomial of A:")
        println(p.toString(ch = "λ"))
        println("trace(A) = ${A.trace()}") // the trace of A, also the coefficient of λ^(n-1) in p(λ) with a minus sign
        println("det(A) = ${A.det()}") // the determinant of A, also the constant term of p(λ) with (-1)^n

        // another way to compute the characteristic polynomial
        // sum of all principal minors of A
        val coef = (0..n).map { k ->
            if (k == n) return@map ℤ.one
            var res = ℤ.zero
            for (rows in IterUtils.comb(A.row, n - k, false)) {
                // select n-k rows from A without repetition
                val major = A.slice(rows, rows).det() // take the principal minor of A
                res += major
            }
            res * MathUtils.powOfMinusOne(k)
        }
        val p2 = io.github.ezrnest.mathsymk.model.Polynomial.fromList(ℤ, coef)
        println("Another way to compute the characteristic polynomial:")
        println(p2.toString(ch = "λ"))

        println("Substitute A into the polynomial, is it zero?")
        println(isZero(p.substitute(A, this@with))) // p(A) = 0, a matrix of zeros
    }
}

fun matrixCharacteristicPolynomialsComplexExample() {
    // this example show the flexibility of the library
    // now we work with multinomials over integers
    val ℤ = Models.ints()
    val multiOverZ = Multinomial.over(ℤ)
    val n = 4
    val mat = Matrix.over(multiOverZ, n)
    with(mat) {
        val A = Matrix(n) { i, j -> multiOverZ.eval { i * a + 2 * j * b } }
        println("Matrix A:")
        println(A)
        val p = A.charPoly() // the characteristic polynomial of A, p(λ) = det(λI - A)
        println("Characteristic polynomial of A:")
        println(p.toString(ch = "λ"))
        println("trace(A) = ${A.trace()}") // the trace of A, also the coefficient of λ^(n-1) in p(λ) with a minus sign
        println("det(A) = ${A.det()}") // the determinant of A, also the constant term of p(λ) with (-1)^n

        // another way to compute the characteristic polynomial
        // sum of all principal minors of A
        val coef = (0..n).map { k ->
            if (k == n) return@map multiOverZ.one
            var res = multiOverZ.zero
            for (rows in IterUtils.comb(A.row, n - k, false)) {
                // select n-k rows from A without repetition
                val major = A.slice(rows, rows).det() // take the principal minor of A
                res += major
            }
            res * MathUtils.powOfMinusOne(k)
        }
        val p2 = io.github.ezrnest.mathsymk.model.Polynomial.fromList(multiOverZ, coef)
        println("Another way to compute the characteristic polynomial:")
        println(p2.toString(ch = "λ"))

        println("Substitute A into the polynomial, is it zero?")
        println(p.substitute(A, mat)) // p(A) = 0, a matrix of zeros
    }

}

fun computeInvariantFactors() {
    val Z = Models.ints()
    val n = 5
    with(Matrix.over(Z)) {
        val rng = Random(11)
        val A = Matrix(n) { i, j ->
            rng.nextInt(10) //
        }
        println("The matrix A:")
        println(A)
        val invFactors = MatrixImpl.invariantFactors(A, Z)
        println("Computing the invariant factors")
        println(invFactors)
        val accProd = invFactors.scan(1) { acc, factor -> acc * factor }.drop(1)
        println("The accumulated product of the invariant factors:")
        println(accProd)
        val detFactors = mutableListOf<Int>()
        for (k in 1..n) {
            val rows = IterUtils.comb(A.row, k, false)
            val cols = IterUtils.comb(A.column, k, false)
            val minors = IterUtils.prod2(rows, cols).map { A.slice(it.first, it.second).det() }.toList().toIntArray()
            val gcd = NTFunctions.gcd(*minors)
            if (gcd == 0) {
                break
            }
            detFactors.add(gcd)
        }
        println("The determinant factors: (should be the same as the acc. prod. of invariant factors)")
        println(detFactors)
    }
}


fun main() {
    matrixMultiplication()
}