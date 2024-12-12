package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.*
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedConditioned
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedConditionedRep

interface NScopeExtLogic : NScopeExtBasic {

    val Boolean.n: Node
        get() = if (this) SymBasic.TRUE else SymBasic.FALSE

//    override fun constant(name: String): Node? {
//        return when (name) {
//            "true" -> SymBasic.TRUE
//            "false" -> SymBasic.FALSE
//            else -> super.constant(name)
//        }
//    }


    operator fun Node.not(): Node = SymLogic.Not(this)

    infix fun Node.implies(that: Node): Node = SymLogic.Implies(this, that)

    infix fun Node.iff(that: Node): Node = SymLogic.Iff(this, that)

    infix fun Node.and(that: Node): Node = SymLogic.And(this, that)

    infix fun Node.or(that: Node): Node = SymLogic.Or(this, that)

    infix fun Node.nand(that: Node): Node = SymLogic.Nand(this, that)

    infix fun Node.nor(that: Node): Node = SymLogic.Nor(this, that)

    infix fun Node.xor(that: Node): Node = SymLogic.Xor(this, that)

    infix fun Node.xnor(that: Node): Node = SymLogic.Xnor(this, that)


    fun and(vararg nodes: Node): Node = SymLogic.And(*nodes)

    fun or(vararg nodes: Node): Node = SymLogic.Or(*nodes)


    fun NodeScope.forAll(x: Node, condition: Node = SymBasic.TRUE, clause: Node): Node {
        return qualifiedConditionedRep(SymLogic.Symbols.FOR_ALL, x, condition, clause)
    }

    fun NodeScope.forAll(
        varName: String? = null, condition: (NSymbol) -> Node = { SymBasic.TRUE }, clause: (NSymbol) -> Node
    ): Node {
        return qualifiedConditioned(SymLogic.Symbols.FOR_ALL, varName, condition, clause)
    }

    fun NodeScope.forAll(x: Node, condition: Node = SymBasic.TRUE, clause: () -> Node): Node {
        return forAll(x, condition, clause())
    }


    companion object Instance : NScopeExtLogic
}


inline fun NodeScope.logic(builder: NScopeExtLogic.() -> Node): Node {
    return NScopeExtLogic.Instance.builder()
}