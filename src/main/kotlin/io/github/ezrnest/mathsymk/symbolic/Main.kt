package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.symbolic.alg.*
import io.github.ezrnest.mathsymk.symbolic.logic.SymLogic


val TestExprCal = BasicExprCal()

fun main() {
    val cal = TestExprCal
    cal.verbose = BasicExprCal.Verbosity.ALL
    cal.registerContextInfo(QualifierNodeProperties(SymLogic.Symbols.FOR_ALL))
    cal.registerContextInfo(QualifierNodeProperties(SymAlg.Symbols.SUM))
    with(cal) {
//        ESymbol.displayHash = true
        alg {
            var expr = x

//            expr = forAll(x, condition = x gtr y, x geq y)
//            expr = forAll { x ->
//                x geq y
//            }
            val N = SymAlg.Infinity
            val a = sum(x, 1.e, N, sin(x))
            println(a.plainToString())
            val b = sum(1.e, N, "n") { x ->
                sin(x) / pow(x, 2.e)
            }
            println(b.plainToString())
            println(directEquals(a, b))
            println(expr.plainToString())
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
