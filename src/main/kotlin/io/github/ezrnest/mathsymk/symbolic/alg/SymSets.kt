package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.*


interface SymSets : SymBasic {

    fun belongs(x: Node, y: Node): Node {
        return Node2T(SymBasic.Symbols.Belongs, x, y)
    }

    fun intRange(start: Node, end: Node): Node {
        return Node2T(Symbols.IntRange, start, end)
    }


    companion object Instance : SymSets

    object Constants {
        val Integer = NSymbol(Symbols.INTEGER)
        val NATURAL = NSymbol(Symbols.NATURAL)
        val RATIONAL = NSymbol(Symbols.RATIONAL)
        val REALS = NSymbol(Symbols.REALS)
        val COMPLEX = NSymbol(Symbols.COMPLEX)
    }

    object Symbols {
        val INTEGER = ESymbol("ℤ")
        val NATURAL = ESymbol("ℕ")
        val RATIONAL = ESymbol("ℚ")
        val REALS = ESymbol("ℝ")
        val COMPLEX = ESymbol("ℂ")


        val IntRange = ESymbol("Range")
    }

    object Definitions{


    }
}

/*
object SymSets {










    fun belongs(x: Node, y: Node): Node {
        return Node2T(SymBasic.Symbols.BELONGS, x, y)
    }

    fun intRange(start: Node, end: Node): Node {
        return Node2T(Symbols.IntRange, start, end)
    }
}


 */
