package samples

import io.github.ezrnest.model.Multinomial
import io.github.ezrnest.model.NumberModels


fun buildMultinomial(){
    val Z = NumberModels.intAsIntegers() // Integers, Z
    val mult = Multinomial.over(Z)
    with(mult) {
        val m = x - 2 * y - y * z + 4 * x.pow(2)
        println(m)
    }
}

fun main() {
    buildMultinomial()
}