package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.ESymbol
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeN
import io.github.ezrnest.mathsymk.symbolic.SymBasic

object SymLogic {


    object Symbols {
        val AND = ESymbol("⋀")
        val OR = ESymbol("⋁")
        val NOT = ESymbol("¬")
        val IMPLIES = ESymbol("→")


        val FOR_ALL = ESymbol("∀")
        val EXISTS = ESymbol("∃")
    }


    fun And(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return SymBasic.TRUE
        if (nodes.size == 1) return nodes[0]
        return NodeN(Symbols.AND, nodes)
    }

    fun And(vararg nodes: Node): Node {
        return And(nodes.asList())
    }

    fun Or(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return SymBasic.FALSE
        if (nodes.size == 1) return nodes[0]
        return NodeN(Symbols.OR, nodes)
    }

    fun Or(vararg nodes: Node): Node {
        return Or(nodes.asList())
    }

    fun Not(node: Node): Node {
        return SymBasic.Node1(Symbols.NOT, node)
    }

    fun Implies(p: Node, q: Node): Node {
        return SymBasic.Node2(Symbols.IMPLIES, p, q)
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