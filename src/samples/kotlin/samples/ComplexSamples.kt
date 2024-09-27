package samples

import io.github.ezrnest.model.Complex
import io.github.ezrnest.model.Complex128
import io.github.ezrnest.model.Complex128.Companion.exp
import io.github.ezrnest.model.NumberModels


fun basicDoubleComplex(){
    val z1 = Complex128(1.0, 2.0)
    val z2 = Complex128(3.0, 4.0)
    println(listOf(z1 + z2, z1 - z2, z1 * z2, z1 / z2)) // (4.0, 6.0), (-2.0, -2.0), (-5.0, 10.0), (0.44, 0.08)
    println(listOf(z2.mod,z2.arg)) // 5.0, 0.93
    val π = Math.PI
    val i = Complex128.I
    println(String.format("%.2s",exp(i * π))) // -1.0

}

fun variousDoubleModels(){
    val Z = NumberModels.integers()
    val GaussianInt = Complex.over(Z)
    with(GaussianInt){
        val z1 = 1 + 2.i
        val z2 = 3 + 4.i
        println(listOf(z1 + z2, z1 - z2, z1 * z2)) // (4, 6), (-2, -2), (-5, 10)
    }

    val Q = NumberModels.fractions()
    val complex2 = Complex.over(Q) // Complex numbers with rational components
    with(Q){
        with(complex2) {
            val z1 = frac(1, 2) + frac(1, 3).i
            val z2 = frac(1, 4) + frac(1, 5).i
            println(listOf(z1 + z2, z1 - z2, z1 * z2))
            // (3/4, 8/15), (1/4, 2/15), (7/120, 11/60)
        }
    }
}

fun main() {
    basicDoubleComplex()
}