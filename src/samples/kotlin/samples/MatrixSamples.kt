package samples

import io.github.ezrnest.linear.Matrix
import io.github.ezrnest.linear.MatrixImpl
import io.github.ezrnest.linear.MatrixUtils.charPoly
import io.github.ezrnest.model.Multinomial
import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.model.Polynomial
import io.github.ezrnest.numberTheory.NTFunctions
import io.github.ezrnest.structure.eval
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.MathUtils
import kotlin.random.Random

fun computingDeterminants() {
    val ℤ = NumberModels.intAsIntegers()
    val mult = Multinomial.over(ℤ)
    val A = Matrix(3, mult) { i, j ->
        mult.monomial("a${i + 1}${j + 1}")
    }
    val det = A.det()
    println("Matrix A:")
    println(A)
    println("Determinant of A:")
    println(det.toString())
}


fun matrixCharacteristicPolynomials() {
    val ℤ = NumberModels.intAsIntegers()
    val n = 4
    val A = Matrix(n, ℤ) { i, j -> i + 2 * j }
    println("Matrix A:")
    println(A)
    val p = A.charPoly() // the characteristic polynomial of A, p(λ) = det(λI - A)
    println("Characteristic polynomial of A:")
    println(p.format(ch = "λ"))
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
    val p2 = Polynomial.fromList(ℤ, coef)
    println("Another way to compute the characteristic polynomial:")
    println(p2.format(ch = "λ"))

    val matOverZ = Matrix.over(n, ℤ)
    println("Substitute A into the polynomial, is it zero?")
    println(p.substitute(A, matOverZ).isZero) // p(A) = 0, a matrix of zeros
}

fun matrixCharacteristicPolynomialsComplexExample() {
    // this example show the flexibility of the library
    // now we work with multinomials over integers
    val ℤ = NumberModels.intAsIntegers()
    val multiOverZ = Multinomial.over(ℤ)
    val n = 4
    val A = Matrix(n, multiOverZ) { i, j -> multiOverZ.eval { i * a + 2 * j * b } }
    println("Matrix A:")
    println(A)
    val p = A.charPoly() // the characteristic polynomial of A, p(λ) = det(λI - A)
    println("Characteristic polynomial of A:")
    println(p.format(ch = "λ", bracket = true))
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
    val p2 = Polynomial.fromList(multiOverZ, coef)
    println("Another way to compute the characteristic polynomial:")
    println(p2.format(ch = "λ", bracket = true))

    val matOverZ = Matrix.over(n, multiOverZ)
    println("Substitute A into the polynomial, is it zero?")
    println(p.substitute(A, matOverZ).isZero) // p(A) = 0, a matrix of zeros

}

fun computeInvariantFactors(){
    val Z = NumberModels.intAsIntegers()
    val n = 5
    val rng = Random(11)
    val A = Matrix(n, Z) { i, j ->
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


fun main() {
    computeInvariantFactors()
}