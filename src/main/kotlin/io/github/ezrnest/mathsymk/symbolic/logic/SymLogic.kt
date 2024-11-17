package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeSig
import io.github.ezrnest.mathsymk.symbolic.NodeSig.NType

object SymLogic {

    object Names {
        val AND = "⋀"
        val OR = "⋁"
        val NOT = "¬"
        val IMPLIES = "→"
        val TRUE = "true"
        val FALSE = "false"
    }

    object Signatures {
        val AND = NodeSig(Names.AND, NType.NodeN)
        val OR = NodeSig(Names.OR, NType.NodeN)
        val NOT = NodeSig(Names.NOT, NType.Node1)
        val IMPLIES = NodeSig(Names.IMPLIES, NType.Node2)

    }

    val TRUE = Node.Symbol(Names.TRUE)
    val FALSE = Node.Symbol(Names.FALSE)

    fun And(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return TRUE
        if (nodes.size == 1) return nodes[0]
        return Node.NodeN(Names.AND, nodes)
    }

    fun And(vararg nodes: Node): Node {
        return And(nodes.asList())
    }

    fun Or(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return FALSE
        if (nodes.size == 1) return nodes[0]
        return Node.NodeN(Names.OR, nodes)
    }

    fun Or(vararg nodes: Node): Node {
        return Or(nodes.asList())
    }

    fun Not(node: Node): Node {
        return Node.Node1(Names.NOT, node)
    }

    fun Implies(p: Node, q: Node): Node {
        return Node.Node2(Names.IMPLIES, p, q)
    }

    fun Iff(p: Node, q: Node): Node {
        return And(Implies(p, q), Implies(q, p))
    }

    fun Xor(p: Node, q: Node): Node {
        return Or(And(p, Not(q)), And(Not(p), q))
    }

    fun Nand(p: Node, q: Node): Node {
        return Not(And(p, q))
    }

    fun Nor(p: Node, q: Node): Node {
        return Not(Or(p, q))
    }

    fun Xnor(p: Node, q: Node): Node {
        return Not(Xor(p, q))
    }

}