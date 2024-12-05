package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.ESymbol
import io.github.ezrnest.mathsymk.symbolic.NSymbol
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.Node2T

object SymSets {



    val INTEGER = NSymbol(Symbols.INTEGER)
    val NATURAL = NSymbol(Symbols.NATURAL)
    val RATIONAL = NSymbol(Symbols.RATIONAL)
    val REALS = NSymbol(Symbols.REALS)
    val COMPLEX = NSymbol(Symbols.COMPLEX)



    object Symbols{


        val INTEGER = ESymbol("ℤ")
        val NATURAL = ESymbol("ℕ")
        val RATIONAL = ESymbol("ℚ")
        val REALS = ESymbol("ℝ")
        val COMPLEX = ESymbol("ℂ")






    }



    fun belongs(x: Node, y: Node): Node {
        return Node2T(Symbols.BELONGS, x, y)
    }
}