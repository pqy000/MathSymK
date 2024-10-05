package io.github.ezrnest.symbolic


fun main() {
    val m = mapOf(1 to null)
    println(m.containsKey(1))
    val cal = TestExprContext
    with(NodeBuilderScope) {
//        val n0 = (1.e + 2.e) * (x + y) * z + a
        val n0 = 1.e * 2.e * 3.e
        println(n0.plainToString())
        println(n0.treeToString())
//
//
        val res = cal.simplifyFull(n0)
        println(res.treeToString())
        println(res.plainToString())

    }

}