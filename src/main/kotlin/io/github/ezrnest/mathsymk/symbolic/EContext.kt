package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.symbolic.alg.SymSets

// created at 2024/10/01


/**
 * The context
 *
 * Describes the assumptions of the expression.
 */
interface EContext {
    val conditions: Set<Node> // TODO
        get() = emptySet()


    /**
     * Gets a symbol node.
     */
    fun symbol(name: String): Node


    fun constant(name: String): Node {
        throw IllegalArgumentException("Unknown constant: $name")
    }

    val qualifiedSymbols: Set<NSymbol>

    fun isFree(s: NSymbol): Boolean {
        return s !in qualifiedSymbols
    }

    fun isQualified(s: NSymbol): Boolean {
        return s in qualifiedSymbols
    }

    fun getFree(): NSymbol {
        TODO()
    }

    fun freeSymbols(): Sequence<NSymbol> {
        TODO()
    }

    fun addQualified(symbol: NSymbol, cal : ExprCal) : EContext {
        val newQualified = mutableSetOf<NSymbol>()
        newQualified.addAll(qualifiedSymbols)
        newQualified.add(symbol)
        return EContextImpl(qualifiedSymbols = newQualified, conditions = conditions)
    }

    fun addRestrictedQualified(symbol: NSymbol, condition: Node, cal: ExprCal): EContext {
        val newQualified = mutableSetOf<NSymbol>()
        newQualified.addAll(qualifiedSymbols)
        newQualified.add(symbol)
        val newConditions = sortedSetOf(NodeOrder)
//        for(cond in conditions){
//            TODO
//        }
        newConditions.addAll(conditions)
        newConditions.add(condition)
        return EContextImpl(qualifiedSymbols = newQualified, conditions = newConditions)
    }

    fun removeQualified(symbols: List<NSymbol>): EContext {
        TODO()
    }

    fun addConditions(conditions: List<Node>): EContext {
        val newConditions = sortedSetOf(NodeOrder)
        newConditions.addAll(this.conditions)
        newConditions.addAll(conditions)
        return EContextImpl(qualifiedSymbols = qualifiedSymbols, conditions = newConditions)
    }
}

object EmptyEContext : EContext {
    override fun symbol(name: String): Node {
        return NSymbol(name)
    }

    override val qualifiedSymbols = emptySet<NSymbol>()
}

data class EContextImpl(
    override val qualifiedSymbols: Set<NSymbol> = emptySet(),
    override val conditions: Set<Node> = emptySet()
) : EContext {

    override fun symbol(name: String): Node {
        return NSymbol(name)
    }

    override fun isFree(s: NSymbol): Boolean {
        return s !in qualifiedSymbols
    }

    override fun isQualified(s: NSymbol): Boolean {
        return s in qualifiedSymbols
    }

    override fun getFree(): NSymbol {
        TODO()
    }

    override fun freeSymbols(): Sequence<NSymbol> {
        TODO()
    }
}
