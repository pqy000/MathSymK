package io.github.ezrnest.symbolic

import io.github.ezrnest.symbolic.sim.rule


fun main() {

    val cal = TestExprContext
    cal.verbose = true
//    cal.options[ExprContext.Options.forceReal] = true
    rule {
        name = "sin^2(x) + cos^2(x) = 1"
        match {
            pow(sin(x), 2.e) + pow(cos(x), 2.e)
        } to {
            1.e
        }
    }.also { cal.addRule(it) }
    rule {
        name = "sin(pi/2) = 1"
//        match {
//            sin(symbol(Node.PI) / 2.e)
//        } to {
//            1.e
//        }
    }

    val expr = buildNode {
//      1.e * 2.e + x * pow(x, 2.e) * 3.e - x * x * (2.e * x)
        pow(sin(x + y), 2.e) + c + pow(cos(x + y), 2.e)
    }
    println(expr.plainToString())
    println(expr.treeToString())

    val res = cal.simplifyFull(expr)
    println()
    println(res.plainToString())
    println(res.treeToString())

}
