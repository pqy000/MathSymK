package io.github.ezrnest.symbolic


fun main() {
    val cal = TestExprContext
//    cal.options[ExprContext.Options.forceReal] = true
    with(NodeBuilderScope) {
//        val n0 = (1.e + 2.e) * (x + y) * z + a
//        val n0 = 1.e * 2.e * x * x * x * pow(x,2.e + y) / 2.e
        val n0 = pow(x * y * z, 3.e)
        println(n0.plainToString())
        println(n0.treeToString())
//
//
        val res = cal.simplifyFull(n0)
        println(res.treeToString())
        println(res.plainToString())

    }

}