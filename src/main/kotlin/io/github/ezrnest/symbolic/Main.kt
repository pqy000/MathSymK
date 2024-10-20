package io.github.ezrnest.symbolic

import io.github.ezrnest.model.BigFracAsQuot
import io.github.ezrnest.symbolic.sim.RuleCosSpecial
import io.github.ezrnest.symbolic.sim.RuleSinSpecial
import io.github.ezrnest.symbolic.sim.RuleTanSpecial
import io.github.ezrnest.symbolic.sim.rule


fun main() {
    val Q = BigFracAsQuot

    val cal = TestExprCal
    cal.verbose = BasicExprCal.Verbosity.ALL
//    cal.options[ExprContext.Options.forceReal] = true
    rule {
        name = "Trig[ sin^2(x) + cos^2(x) = 1 ]"
        match {
            pow(sin(x), 2.e) + pow(cos(x), 2.e)
        } to {
            1.e
        }
    }.also { cal.addRule(it) }
    rule {
        name = "Trig[ sin(x + y) = sin(x)cos(y) + cos(x)sin(y) ]"
        match {
            sin(x + y)
        } to {
            sin(x) * cos(y) + cos(x) * sin(y)
        }
    }.also { cal.addRule(it) }

    cal.addRule(RuleSinSpecial())
    cal.addRule(RuleCosSpecial())
    cal.addRule(RuleTanSpecial())

    val expr = buildNode {
//      1.e * 2.e + x * pow(x, 2.e) * 3.e - x * x * (2.e * x)
//        pow((-1).e,2.e )
//        val sub = pow(sin(x + y), 2.e) + pow(cos(x + y), 2.e)
//        pow(sin(pi + pi * sub / 3.e), 2.e)
//        -TrigonometricUtils.sinTable[Q.bfrac(1,3)]!!
//        tan(pi / 2.e)
//        sin(pi / 3.e)
        pow(sin(x + y), 2.e) + c + pow(cos(x + y), 2.e)
    }
    println(expr.plainToString())
    println(expr.treeToString())

    val res = cal.simplify(expr)
    println()
    println(res.plainToString())
    println(res.treeToString())

}
