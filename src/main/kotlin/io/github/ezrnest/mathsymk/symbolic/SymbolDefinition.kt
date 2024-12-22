package io.github.ezrnest.mathsymk.symbolic

import java.util.Collections


interface SymbolDefinition {
    val symbol: ESymbol

    fun enterContext(root: Node, rootCtx: EContext, cal: ExprCal): List<EContext>

    fun qualifiedVariables(root: Node): List<ESymbol>


    fun <T : Any> getProp(key: TypedKey<T>): T?


    fun <T : Any> getProp(key: TypedKey<T>, default: T): T {
        return getProp(key) ?: default
    }

    fun <T : Any> setProp(key: TypedKey<T>, value: T)


    object Properties{
        val COMMUTATIVE = TypedKey<Boolean>("commutative")
    }
}

abstract class AbsSymbolDef(final override val symbol: ESymbol) : SymbolDefinition {

    val properties: MutableMap<TypedKey<*>, Any> = mutableMapOf()


    override fun <T : Any> getProp(key: TypedKey<T>): T? {
        return properties.getTyped(key)
    }

    override fun <T : Any> getProp(key: TypedKey<T>, default: T): T {
        return properties.getTyped(key, default)
    }

    override fun <T : Any> setProp(key: TypedKey<T>, value: T) {
        properties[key] = value
    }
}

class FunctionSymbolDef(
    symbol: ESymbol,
//    val argCount: Int,
) : AbsSymbolDef(symbol) {

    override fun enterContext(root: Node, rootCtx: EContext, cal: ExprCal): List<EContext> {
        require(root is NodeChilded)
        return Collections.nCopies(root.childCount, rootCtx)
    }

    override fun qualifiedVariables(root: Node): List<ESymbol> {
        return emptyList()
    }
}

class QualifierSymbolDef(
    symbol: ESymbol,
) : AbsSymbolDef(symbol) {

    override fun qualifiedVariables(root: Node): List<ESymbol> {
        require(root is NodeChilded)
        val variablesNode = root.children[0]
        require(variablesNode is NodeN)
        return variablesNode.children.map { (it as NSymbol).symbol }
    }


    override fun enterContext(root: Node, rootCtx: EContext, cal: ExprCal): List<EContext> {
        require(root is NodeChilded)
        val ctxIntro = EContextImpl()
        val variablesNode = root.children[0]
        require(variablesNode is NodeN)
        for (varNode in variablesNode.children) {
            require(varNode is NSymbol)
            ctxIntro.qualifiedSymbols.add(varNode.symbol)
        }
        val conditionNode = root.children[1]
        ctxIntro.addCondition(conditionNode)

        val newCtx = rootCtx.with(ctxIntro)

        // return n copies of the context
        return Collections.nCopies(root.childCount, newCtx)
    }
}