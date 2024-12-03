package io.github.ezrnest.mathsymk.symbolic

// created at 2024/10/01


sealed interface SymbolDeclaration{
    object Free : SymbolDeclaration
    class Qualified : SymbolDeclaration
}

/**
 * The context
 *
 * Describes the assumptions of the expression.
 */
interface EContext {

    val definedSymbols: Map<NSymbol,SymbolDeclaration>


    val conditions: Set<Node> // TODO

    /**
     * Gets a symbol node.
     */
    fun symbol(name: String): Node{
        return NSymbol(name)
    }


    fun constant(name: String): Node {
        throw IllegalArgumentException("Unknown constant: $name")
    }



    fun isFree(s: NSymbol): Boolean {
        return s !in definedSymbols
    }

    fun isDefined(s: NSymbol): Boolean {
        return s in definedSymbols
    }

//    fun getFree(): NSymbol {
//        TODO()
//    }
//
//    fun freeSymbols(): Sequence<NSymbol> {
//        TODO()
//    }

    fun with(ctx : EContext) : EContext{
        val newDefinedSymbols = definedSymbols.toMutableMap()
        newDefinedSymbols.putAll(ctx.definedSymbols)
        val newConditions = conditions.toMutableSet()
        newConditions.addAll(ctx.conditions)
        return EContextImpl(newDefinedSymbols, newConditions)
    }

//    fun addQualified(symbol: NSymbol, cal: ExprCal): EContext {
//        TODO()
////        val newQualified = TODO()
////        newQualified.addAll(definedSymbols)
////        newQualified.add(symbol)
////        return EContextImpl(definedSymbols = newQualified, conditions = conditions)
//    }
//
//    fun addRestrictedQualified(symbol: NSymbol, condition: Node, cal: ExprCal): EContext {
//        val newQualified = mutableSetOf<NSymbol>()
//        newQualified.addAll(definedSymbols)
//        newQualified.add(symbol)
//        val newConditions = sortedSetOf(NodeOrder)
//        newConditions.addAll(conditions)
//        newConditions.add(condition)
//        return EContextImpl(definedSymbols = newQualified, conditions = newConditions)
//    }
//
//    fun removeQualified(symbols: List<NSymbol>): EContext {
//        TODO()
//    }
//
//    fun addConditions(conditions: List<Node>): EContext {
//        val newConditions = sortedSetOf(NodeOrder)
//        newConditions.addAll(this.conditions)
//        newConditions.addAll(conditions)
//        return EContextImpl(definedSymbols = definedSymbols, conditions = newConditions)
//    }
}

interface MutableEContext : EContext {
    override val definedSymbols: MutableMap<NSymbol,SymbolDeclaration>
    override val conditions: MutableSet<Node>


    fun addQualifiedSymbol(symbol: NSymbol) : SymbolDeclaration {
        val declaration = SymbolDeclaration.Qualified()
        definedSymbols[symbol] = declaration
        return declaration
    }

    fun addDeclaredSymbol(symbol: NSymbol, decl : SymbolDeclaration) {
        definedSymbols[symbol] = decl
    }

    fun addCondition(condition: Node) {
        conditions.add(condition)
    }
}

object EmptyEContext : EContext {
    override fun symbol(name: String): Node {
        return NSymbol(name)
    }

    override val definedSymbols = emptyMap<NSymbol,SymbolDeclaration>()
    override val conditions: Set<Node> = emptySet()
}

data class EContextImpl(
    override val definedSymbols: MutableMap<NSymbol,SymbolDeclaration> = mutableMapOf(),
    override val conditions: MutableSet<Node> = sortedSetOf(NodeOrder),
) : MutableEContext {



}
