package samples

import io.github.ezrnest.model.NumberModels
import io.github.ezrnest.model.Polynomial

//object PolynomialExamples{
//
//}

fun polynomial(){
//    val Z = NumberModels.intAsIntegers() // Integers, Z
    val Z97 = NumberModels.intModP(97) // Integers mod 97, Z/97Z, a field
    val polyZ = Polynomial.over(Z97) // Polynomials over Z/97Z
    with(polyZ) {
        val f = x+1
        val g = x-1
        val p1 = f * g // x^2 - 1
        val p2 = f pow 2 // x^2 + 2x + 1
        println("p1 = $p1")
        println("p2 = $p2")
        val h = gcd(p1,p2).toMonic()
        println("gcd(p1,p2) = $h") // x + 1
    }
}



fun main() {
    polynomial()
}