package io.github.ezrnest.mathsymk.symbolic

object Symbolic {

    fun reals(): ExprCalReal {
        return ExprCalReal()
    }
}

fun main() {
    with(Symbolic.reals()) {
        println(format(π - π))
        sin(π / 2).also {
            println(format(it))
        }
    }
}