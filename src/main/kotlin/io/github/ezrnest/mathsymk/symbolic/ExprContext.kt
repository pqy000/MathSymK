package io.github.ezrnest.mathsymk.symbolic
// created at 2024/10/01


/**
 * The context
 *
 * Describes the assumptions of the expression.
 */
interface ExprContext {
    val conditions : List<Node> // TODO
        get() = emptyList()


    /**
     * Gets a symbol node.
     */
    fun symbol(name: String): Node


    fun constant(name: String): Node {
        throw IllegalArgumentException("Unknown constant: $name")
    }

    val qualifiedSymbols : Set<NSymbol>

    fun isFree(s : NSymbol) : Boolean{
        return s !in qualifiedSymbols
    }

    fun isQualified(s : NSymbol) : Boolean{
        return s in qualifiedSymbols
    }

    fun getFree() : NSymbol{
        TODO()
    }

    fun getFreeSymbols() : Set<NSymbol>{
        TODO()
    }

}

object EmptyExprContext : ExprContext {

    override fun symbol(name: String): Node {
        return NSymbol(name)
    }

    override val qualifiedSymbols = emptySet<NSymbol>()

}


