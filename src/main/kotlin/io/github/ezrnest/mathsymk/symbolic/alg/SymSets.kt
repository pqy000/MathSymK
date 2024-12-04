package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.ESymbol
import io.github.ezrnest.mathsymk.symbolic.NSymbol
import io.github.ezrnest.mathsymk.symbolic.Node

object SymSets {

    val EMPTY_SET = NSymbol(Symbols.EMPTY_SET)

    val UNIVERSE = NSymbol(Symbols.UNIVERSE)

    val INTEGER = NSymbol(Symbols.INTEGER)
    val NATURAL = NSymbol(Symbols.NATURAL)
    val RATIONAL = NSymbol(Symbols.RATIONAL)
    val REALS = NSymbol(Symbols.REALS)
    val COMPLEX = NSymbol(Symbols.COMPLEX)



    object Symbols{
        val EMPTY_SET = ESymbol("∅")
        val UNIVERSE = ESymbol("Universe")

        val INTEGER = ESymbol("ℤ")
        val NATURAL = ESymbol("ℕ")
        val RATIONAL = ESymbol("ℚ")
        val REALS = ESymbol("ℝ")
        val COMPLEX = ESymbol("ℂ")



        val BELONGS = ESymbol("∈")
        val CONTAINS = ESymbol("∋")
        val SUBSET = ESymbol("⊆")
        val INTERSECT = ESymbol("∩")
        val UNION = ESymbol("∪")



    }



    fun belongs(x: Node, y: Node): Node {
        return Node.Node2(Symbols.BELONGS, x, y)
    }
}