package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.symbolic.alg.SymSets

// created at 2024/10/01


/**
 * The context
 *
 * Describes the assumptions of the expression.
 */
interface ExprContext {
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

    fun addQualified(symbol: NSymbol, cal : ExprCal) : ExprContext {
        val newQualified = mutableSetOf<NSymbol>()
        newQualified.addAll(qualifiedSymbols)
        newQualified.add(symbol)
        return ExprContextImpl(qualifiedSymbols = newQualified, conditions = conditions)
    }

    fun addRestrictedQualified(symbol: NSymbol, condition: Node, cal: ExprCal): ExprContext {
        val newQualified = mutableSetOf<NSymbol>()
        newQualified.addAll(qualifiedSymbols)
        newQualified.add(symbol)
        val newConditions = sortedSetOf(NodeOrder)
//        for(cond in conditions){
//            TODO
//        }
        newConditions.addAll(conditions)
        newConditions.add(condition)
        return ExprContextImpl(qualifiedSymbols = newQualified, conditions = newConditions)
    }

    fun removeQualified(symbols: List<NSymbol>): ExprContext {
        TODO()
    }

    fun addConditions(conditions: List<Node>): ExprContext {
        val newConditions = sortedSetOf(NodeOrder)
        newConditions.addAll(this.conditions)
        newConditions.addAll(conditions)
        return ExprContextImpl(qualifiedSymbols = qualifiedSymbols, conditions = newConditions)
    }
}

object EmptyExprContext : ExprContext {

    override fun symbol(name: String): Node {
        return NSymbol(name)
    }

    override val qualifiedSymbols = emptySet<NSymbol>()
}

data class ExprContextImpl(
    override val qualifiedSymbols: Set<NSymbol> = emptySet(),
    override val conditions: Set<Node> = emptySet()
) : ExprContext {

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
        return qualifiedSymbols.first()
    }

    override fun freeSymbols(): Sequence<NSymbol> {
        return qualifiedSymbols.asSequence()
    }
}


interface NodeContextInfo {
    val nodeSignature: NodeSig
    fun enterContext(root: Node, rootCtx: ExprContext, cal: ExprCal): List<ExprContext>
}

class QualiferNodeContextInfo(
    override val nodeSignature: NodeSig,
    val qualifierIdx: Int = 0,
) : NodeContextInfo {
    override fun enterContext(root: Node, rootCtx: ExprContext, cal: ExprCal): List<ExprContext> {
        root as NodeChilded
        val restrictedVarNode = root.children[qualifierIdx]
        require(restrictedVarNode is Node2 && restrictedVarNode.name == SymSets.Names.BELONGS)
        val variable = restrictedVarNode.children[0] as NSymbol
        val ctxVariable = rootCtx.addQualified(variable, cal)
        val ctxSubExpr =  rootCtx.addRestrictedQualified(variable, restrictedVarNode, cal)
        val result = ArrayList<ExprContext>(root.childCount)
        repeat(root.childCount) {
            result.add(ctxSubExpr)
        }
        result[qualifierIdx] = ctxVariable
        return result
    }
}