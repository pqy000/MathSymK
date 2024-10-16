package io.github.ezrnest.symbolic

import io.github.ezrnest.structure.PartialOrder


//fun main() {
//
//    val cal = TestExprContext
////    cal.options[ExprContext.Options.forceReal] = true
//    with(NodeBuilderScope) {
////        val n0 = (1.e + 2.e) * (x + y) * z + a
////        val np = pow(2.e,3.e)
////        val expr = sin(np) + cos(np) + np
//        val expr = 1.e * 2.e + x * pow(x, 2.e) * 3.e - x * x * (2.e * x)
//        println(expr.plainToString())
//        println(expr.treeToString())
//
//        val res = cal.simplifyFull(expr)
//        println(res.treeToString())
//        println(res.plainToString())
//
//    }
//
//}

// Usage example
data class Element(val value: Int)

object ElementPartialOrder : PartialOrder<Element> {
    override fun compare(o1: Element, o2: Element): PartialOrder.Result {
        return PartialOrder.Result.ofInt(o1.value.compareTo(o2.value))
    }
}
object StringPartialOrder : PartialOrder<String> {
    override fun compare(o1: String, o2: String): PartialOrder.Result {
        if(o1 == o2) return PartialOrder.Result.EQUAL
        if(o1.startsWith(o2)) return PartialOrder.Result.GREATER
        if(o2.startsWith(o1)) return PartialOrder.Result.LESS
        return PartialOrder.Result.INCOMPARABLE
    }
}

fun main() {
    val elements = listOf("a", "ab", "abc", "b", "bc", "c")
    val chains = PartialOrder.chainDecomp(elements, StringPartialOrder)

    chains.forEachIndexed { index, chain ->
        println("Chain $index: $chain")
    }
}