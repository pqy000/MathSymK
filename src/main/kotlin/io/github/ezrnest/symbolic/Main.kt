package io.github.ezrnest.symbolic


fun main() {
    val cal = TestExprContext
//    cal.options[ExprContext.Options.forceReal] = true
    with(NodeBuilderScope) {
//        val n0 = (1.e + 2.e) * (x + y) * z + a
//        val np = pow(2.e,3.e)
//        val expr = sin(np) + cos(np) + np
        val expr = 1.e * 2.e + x * pow(x, 2.e) * 3.e - x * x * (2.e * x)
        println(expr.plainToString())
        println(expr.treeToString())

        val res = cal.simplifyFull(expr)
        println(res.treeToString())
        println(res.plainToString())

    }

}