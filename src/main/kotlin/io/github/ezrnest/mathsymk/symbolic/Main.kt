package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.symbolic.alg.*
import io.github.ezrnest.mathsymk.symbolic.logic.SymLogic


val TestExprCal = BasicExprCal()

fun main() {
    val Q = BigFracAsQuot

    val cal = TestExprCal
    cal.verbose = BasicExprCal.Verbosity.NONE
//    cal.options[ExprContext.Options.forceReal] = true
//    rule {
//        name = "Trig[ sin^2(x) + cos^2(x) = 1 ]"
//        match {
//            pow(sin(x), 2.e) + pow(cos(x), 2.e)
//        } to {
//            1.e
//        }
//    }.also { cal.addReduceRule(it) }
    cal.registerContextInfo(QualifierNodeContextInfo(SymLogic.Signatures.FOR_ALL))

//    cal.registerReduceRule(RuleSinSpecial())
//    cal.registerReduceRule(RuleCosSpecial())
//    cal.registerReduceRule(RuleTanSpecial())
//    cal.registerReduceRuleAll(RulesTrigonometricReduce())
//    cal.addAllRules(RulesTrigonometricTransform())
//    cal.addRule(RuleExpandMul)
    with(cal) {
        with(AlgebraScope(cal.context)) {
            println(format(1.e / 3.e))
        }
    }

    with(AlgebraScope(cal.context)){
//        val expr = all(x){
//            sin(x)
//        }
//        println(cal.substitute(expr, x, 1.e).plainToString())
    }

//    val expr = buildAlg {
//      1.e * 2.e + x * pow(x, 2.e) * 3.e - x * x * (2.e * x)
//        pow((-1).e,2.e )
//        val sub = pow(sin(x + y), 2.e) + pow(cos(x + y), 2.e)
//        pow(sin(pi + pi * sub / 3.e), 2.e)
//        tan(pi / 2.e)
//        sin(x+y) - sin(x-y)
//        (x+y) * (x-y)
//        (x+1.e)*(x+2.e) * (x+3.e) - 1.e
//    }

//    val ss = sortedSetOf(compareBy<SimProcess.NodeStatus> { it.complexity }.thenBy(NodeOrder) { it.node })
//    ss.add(SimProcess.NodeStatus(expr, BasicComplexity.complexity(expr, cal.context)))
//
//    buildNode { sin(x + y) - cos(x + y) }.also { ss.add(SimProcess.NodeStatus(it,BasicComplexity.complexity(it, cal.context))) }
//    buildNode { sin(x + y) }.also { ss.add(SimProcess.NodeStatus(it,BasicComplexity.complexity(it, cal.context))) }
//    println(ss.joinToString())

//    println(expr.plainToString())
//    println(expr.treeToString())
//    println()

//    println()
//    res.forEachIndexed { i, it ->
//        print("Result $i: ")
//        println(it.plainToString())
////        println(it.treeToString())
//    }

//    println(res.plainToString())
//    println(res.treeToString())

}
