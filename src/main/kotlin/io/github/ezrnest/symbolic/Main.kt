package io.github.ezrnest.symbolic


fun main() {
    val cal = TestExprContext
//    cal.options[ExprContext.Options.forceReal] = true
    with(NodeBuilderScope) {
//        val n0 = (1.e + 2.e) * (x + y) * z + a
//        val n0 = exp(x) * exp(0.e-x)
        val n0 = pow(0.e, pow((3).e, pow((3).e, 20.e)))
        println(n0.plainToString())
        println(n0.treeToString())
//
//
        val res = cal.simplifyFull(n0)
        println(res.treeToString())
        println(res.plainToString())

    }

}