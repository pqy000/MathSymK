package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.ESymbol
import io.github.ezrnest.mathsymk.symbolic.NSymbol
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.alg.SymSets

object SymLogic {

    object Symbols {
        val TRUE = ESymbol("true")
        val FALSE = ESymbol("false")

        val AND = ESymbol("⋀")
        val OR = ESymbol("⋁")
        val NOT = ESymbol("¬")
        val IMPLIES = ESymbol("→")


        val FOR_ALL = ESymbol("∀")
        val EXISTS = ESymbol("∃")
    }

//    object Signatures {
//        val AND = NodeSig(Symbols.AND, NType.NodeN)
//        val OR = NodeSig(Symbols.OR, NType.NodeN)
//        val NOT = NodeSig(Symbols.NOT, NType.Node1)
//        val IMPLIES = NodeSig(Symbols.IMPLIES, NType.Node2)
//
//        /**
//         * `∀(x, P(x))` or `∀x P(x)`
//         */
//        val FOR_ALL = NodeSig(Symbols.FOR_ALL, NType.Node2)
//        val EXISTS = NodeSig(Symbols.EXISTS, NType.Node2)
//    }

    val TRUE = NSymbol(Symbols.TRUE)
    val FALSE = NSymbol(Symbols.FALSE)

    fun And(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return TRUE
        if (nodes.size == 1) return nodes[0]
        return Node.NodeN(Symbols.AND, nodes)
    }

    fun And(vararg nodes: Node): Node {
        return And(nodes.asList())
    }

    fun Or(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return FALSE
        if (nodes.size == 1) return nodes[0]
        return Node.NodeN(Symbols.OR, nodes)
    }

    fun Or(vararg nodes: Node): Node {
        return Or(nodes.asList())
    }

    fun Not(node: Node): Node {
        return Node.Node1(Symbols.NOT, node)
    }

    fun Implies(p: Node, q: Node): Node {
        return Node.Node2(Symbols.IMPLIES, p, q)
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


    fun ForAll(x: NSymbol, set: Node, p: Node): Node {
        //TODO
        return Node.Node2(Symbols.FOR_ALL, SymSets.belongs(x, set), p)
    }

    fun Exists(x: NSymbol, set: Node, p: Node): Node {
        //TODO
        return Node.Node2(Symbols.EXISTS, SymSets.belongs(x, set), p)
    }

}