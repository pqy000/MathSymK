package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.ESymbol
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeN
import io.github.ezrnest.mathsymk.symbolic.SymBasic


import io.github.ezrnest.mathsymk.symbolic.*
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedConditioned
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedConditionedRep

interface SymLogic : SymBasic {

    object Symbols {
        val AND = ESymbol("⋀")
        val OR = ESymbol("⋁")
        val NOT = ESymbol("¬")
        val IMPLIES = ESymbol("→")


        val FOR_ALL = ESymbol("∀")
        val EXISTS = ESymbol("∃")
    }

    companion object Instance : SymLogic {


    }


    val Boolean.n: Node
        get() = if (this) SymBasic.True else SymBasic.False

    operator fun Node.not(): Node {
        return Node1T(Symbols.NOT, this)
    }

    fun conjunction(vararg nodes: Node): Node {
        return conjunction(nodes.asList())
    }

    fun conjunction(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return SymBasic.True
        if (nodes.size == 1) return nodes[0]
        return NodeN(Symbols.AND, nodes)
    }

    fun disjunction(vararg nodes: Node): Node {
        return disjunction(nodes.asList())
    }

    fun disjunction(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return SymBasic.False
        if (nodes.size == 1) return nodes[0]
        return NodeN(Symbols.OR, nodes)
    }

    infix fun Node.and(that: Node): Node {
        return conjunction(this, that)
    }

    infix fun Node.or(that: Node): Node {
        return disjunction(this, that)
    }

    infix fun Node.implies(that: Node): Node {
        return Node2T(Symbols.IMPLIES, this, that)
    }

    infix fun Node.iff(that: Node): Node {
        return (this implies that) and (that implies this)
    }


    fun NodeScope.forAll(x: Node, condition: Node = SymBasic.True, clause: Node): Node {
        return qualifiedConditionedRep(SymLogic.Symbols.FOR_ALL, x, condition, clause)
    }

    fun NodeScope.forAll(
        varName: String? = null, condition: (NSymbol) -> Node = { SymBasic.True }, clause: (NSymbol) -> Node
    ): Node {
        return qualifiedConditioned(SymLogic.Symbols.FOR_ALL, varName, condition, clause)
    }

    fun NodeScope.forAll(x: Node, condition: Node = SymBasic.True, clause: () -> Node): Node {
        return forAll(x, condition, clause())
    }

}


inline fun logic(builder: SymLogic.() -> Node): Node {
    return SymLogic.Instance.builder()
}