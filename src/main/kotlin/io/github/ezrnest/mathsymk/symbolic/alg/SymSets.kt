package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeSig
import io.github.ezrnest.mathsymk.symbolic.NodeSig.NType

object SymSets {

    val EMPTY_SET = Node.Symbol(Names.EMPTY_SET)

    val INTEGERS = Node.Symbol(Names.INTEGER)

    val NATURALS = Node.Symbol(Names.NATURAL)

    val RATIONALS = Node.Symbol(Names.RATIONAL)

    val REALS = Node.Symbol(Names.REALS)

    val COMPLEXES = Node.Symbol(Names.COMPLEX)

    val UNIVERSE = Node.Symbol(Names.UNIVERSE)


    object Names {
        val BELONGS = "∈"

//        val CONTAINS = "∋"

        val SUBSET = "⊆"

        val INTERSECT = "∩"

        val UNION = "∪"

        val EMPTY_SET = "∅"

        val INTEGER = "ℤ"

        val NATURAL = "ℕ"

        val RATIONAL = "ℚ"

        val REALS = "ℝ"

        val COMPLEX = "ℂ"

        val UNIVERSE = "Universe"
    }

    object Signatures {
        val BELONGS = NodeSig(Names.BELONGS, NType.Node2)

        val INTERSECT = NodeSig(Names.INTERSECT, NType.NodeN)

        val UNION = NodeSig(Names.UNION, NType.NodeN)

        val SUBSET = NodeSig(Names.SUBSET, NType.Node2)
    }


    fun belongs(x: Node, y: Node): Node {
        return Node.Node2(Names.BELONGS, x, y)
    }
}