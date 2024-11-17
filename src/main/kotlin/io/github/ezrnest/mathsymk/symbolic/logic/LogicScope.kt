package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.ExprContext
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeScope

interface NodeScopeLogic : NodeScope{

    val Boolean.n: Node
        get() = if(this) SymLogic.TRUE else SymLogic.FALSE

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

    override fun constant(name: String): Node {
        return when(name){
            "true" -> SymLogic.TRUE
            "false" -> SymLogic.FALSE
            else -> super.constant(name)
        }
    }

    companion object{

        internal class LogicScopeImpl(override val context: ExprContext) : NodeScopeLogic{

        }

        operator fun invoke(context: ExprContext): NodeScopeLogic = LogicScopeImpl(context)
    }
}