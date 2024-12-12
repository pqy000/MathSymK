package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.symbolic.alg.ExprCalReal

object Symbolic {

    fun reals(): ExprCalReal {
        return ExprCalReal()
    }
}

fun main() {
    with(Symbolic.reals()) {
//        verbose = BasicExprCal.Verbosity.ALL
//        val r = sum(1.e, Infinity, "n") { n ->
//            pow(n, -2)
//        }
//        println(reduce(r))

//        println(sin(π / 2))
//        println(format(1.e / 3.e))
//        sin(π / 2).also {
//            println(format(reduce(it)))
//        }
    }
}