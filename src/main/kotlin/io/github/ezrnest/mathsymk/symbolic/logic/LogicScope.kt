package io.github.ezrnest.mathsymk.symbolic.logic

import io.github.ezrnest.mathsymk.symbolic.*

interface ILogicScope : NodeScope {

    val Boolean.n: Node
        get() = if (this) SymLogic.TRUE else SymLogic.FALSE

    override fun constant(name: String): Node? {
        return when (name) {
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


    fun all(varName : String = "#1", clause : (NSymbol)->Node) : Node{
        val varNode = NSymbol(ESymbol(varName))
        TODO()
    }

//    fun all(varExpr: Node, expr: Node): Node {
//        //TODO
//        return Node.Qualified2(SymLogic.Symbols.FOR_ALL, varExpr, expr)
//    }
//
//
//    fun exists(variable: Node, expr: Node): Node {
//        //TODO
//        return Node.Qualified2(SymLogic.Symbols.EXISTS, variable, expr)
//    }

    companion object {

        internal class LogicScopeImpl(context: EContext) : AbstractNodeScope(context), ILogicScope {

        }

        operator fun invoke(context: EContext): ILogicScope = LogicScopeImpl(context)
    }
}



inline fun NodeScope.logic(builder: ILogicScope.() -> Node): Node {
    return ILogicScope(this.context).builder()
}