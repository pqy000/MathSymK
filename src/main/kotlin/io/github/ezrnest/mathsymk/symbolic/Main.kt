package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.symbolic.alg.*
import io.github.ezrnest.mathsymk.symbolic.logic.SymLogic


val TestExprCal = BasicExprCal()

fun main() {
    val cal = TestExprCal

    cal.registerSymbol(QualifierSymbolDef(SymLogic.Symbols.FOR_ALL))
    cal.registerSymbol(QualifierSymbolDef(SymAlg.Symbols.SUM))
    cal.registerReduceRule(RulesSummationReduce)
    cal.registerReduceRule(RulesPrimaryFunctions)


//    cal.verbose = BasicExprCal.Verbosity.WHEN_APPLIED
//        ESymbol.displayHash = true
    with(cal) {
        alg {
            assume(x leq 0.e)

//            val r = sum(1.e, Infinity) { x ->
//                pow(x, -2)
//            }
            val r = sum(
                x, 1.e, infinity,
                pow(x, -2)
            )

            println(reduce(r))
        }
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
