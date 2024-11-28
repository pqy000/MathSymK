package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.ExprContext
import io.github.ezrnest.mathsymk.symbolic.NSymbol
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeScope

interface ILogicScope : NodeScope{

    val Boolean.n: Node
        get() = if(this) SymLogic.TRUE else SymLogic.FALSE

    override fun constant(name: String): Node? {
        return when(name){
            "true" -> SymLogic.TRUE
            "false" -> SymLogic.FALSE
            else -> super.constant(name)
        }
    }


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


    fun allN(variable : Node, expr: Node): Node = SymLogic.ForAll(variable as NSymbol, expr)

    fun exists(variable : Node, expr: Node): Node = SymLogic.Exists(variable as NSymbol, expr)

    companion object{

        internal class LogicScopeImplScope(override val context: ExprContext) : ILogicScope{

        }

        operator fun invoke(context: ExprContext): ILogicScope = LogicScopeImplScope(context)
    }
}

inline fun ILogicScope.all(variable : Node, expr: ILogicScope.()->Node): Node = SymLogic.ForAll(variable as NSymbol, expr())

inline fun ILogicScope.exists(variable : Node, expr: ILogicScope.()->Node): Node = SymLogic.Exists(variable as NSymbol, expr())


inline fun NodeScope.logic(builder: ILogicScope.() -> Node): Node {
    return ILogicScope(this.context).builder()
}