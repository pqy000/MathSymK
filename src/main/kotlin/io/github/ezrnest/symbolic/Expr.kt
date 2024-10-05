package io.github.ezrnest.symbolic


fun main() {
    val m = mapOf(1 to null)
    println(m.containsKey(1))
    val cal = TestExprCal
    with(NodeBuilderScope) {
//        val n0 = (1.e + 2.e) * (x + y) * z + a
        val n0 = x + y + z + a + b + 1.e + 2.e + x + (-2).e * x
//        val n0 = x + (-1).e * x
//        val n0 = 1.e + 2.e + (-1).e
        println(n0.plainToString())
        println(n0.treeToString())


        println(cal.simplifyNode(n0).treeToString())
    }

}