package io.github.ezrnest.symbolic

import io.github.ezrnest.symbolic.sim.RuleSinSpecial
import io.github.ezrnest.symbolic.sim.rule


fun main() {

    val cal = TestExprContext
    cal.verbose = BasicExprContext.Verbosity.WHEN_APPLIED
//    cal.options[ExprContext.Options.forceReal] = true
    rule {
        name = "Trig[ sin^2(x) + cos^2(x) = 1 ]"
        match {
            pow(sin(x), 2.e) + pow(cos(x), 2.e)
        } to {
            1.e
        }
    }.also { cal.addRule(it) }

    cal.addRule(RuleSinSpecial())
    val expr = buildNode {
//      1.e * 2.e + x * pow(x, 2.e) * 3.e - x * x * (2.e * x)
//        pow((-1).e,2.e )
//        val sub = pow(sin(x + y), 2.e) + pow(cos(x + y), 2.e)
//        pow(sin(pi + pi * sub / 3.e), 2.e)
        sin(pi / 12.e)
//        pow(sin(x + y), 2.e) + c + pow(cos(x + y), 2.e)
    }
    println(expr.plainToString())
    println(expr.treeToString())

    val res = cal.simplifyFull(expr)
    println()
    println(res.plainToString())
    println(res.treeToString())

}
