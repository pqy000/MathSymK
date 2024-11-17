package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeSig
import io.github.ezrnest.mathsymk.symbolic.NodeSig.NType

object SymSets {

    val EMPTY_SET = Node.Symbol(Names.EMPTY_SET)

    val INTEGERS = Node.Symbol(Names.INTEGERS)

    val NATURALS = Node.Symbol(Names.NATURALS)

    val RATIONALS = Node.Symbol(Names.RATIONALS)

    val REALS = Node.Symbol(Names.REALS)


    object Names {
        val BELONGS = "∈"

//        val CONTAINS = "∋"

        val SUBSET = "⊆"

        val INTERSECT = "∩"

        val UNION = "∪"

        val EMPTY_SET = "∅"

        val INTEGERS = "ℤ"

        val NATURALS = "ℕ"

        val RATIONALS = "ℚ"

        val REALS = "ℝ"

        val COMPLEX = "ℂ"
    }

    object Signatures {
        val BELONGS = NodeSig(Names.BELONGS, NType.Node2)

        val INTERSECT = NodeSig(Names.INTERSECT, NType.NodeN)

        val UNION = NodeSig(Names.UNION, NType.NodeN)

        val SUBSET = NodeSig(Names.SUBSET, NType.Node2)
    }
}