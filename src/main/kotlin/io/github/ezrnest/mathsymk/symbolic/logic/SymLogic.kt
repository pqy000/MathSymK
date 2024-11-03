package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.NodeSig
import io.github.ezrnest.mathsymk.symbolic.NodeSig.NType

object SymLogic {

    object Names {

        val AND = "⋀"
        val OR = "⋁"
        val NOT = "¬"
        val IMPLIES = "→"
    }

    object Signatures {
        val AND = NodeSig(Names.AND, NType.NodeN)
        val OR = NodeSig(Names.OR, NType.NodeN)
        val NOT = NodeSig(Names.NOT, NType.Node1)
        val IMPLIES = NodeSig(Names.IMPLIES, NType.Node2)

    }

}