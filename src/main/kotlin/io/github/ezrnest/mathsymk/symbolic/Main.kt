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
            assume(x geq 0.e)
            val r = abs(x)


//            val r = sum(1.e, infinity) { x ->
//                pow(x, -2)
//            }
            println(r.plainToString())
//            val r = sum(x, 1.e, infinity, pow(x, -2))

            println(reduce(r).plainToString())

            println(simplify(r))
        }
    }



}
