package io.github.ezrnest.mathsymk.symbolic
// created at 2024/10/01


/**
 * Describes the assumptions of the expression.
 */
interface ExprContext {
    val conditions : List<Any> // TODO
        get() = emptyList()
}

object EmptyExprContext : ExprContext

class BasicExprContext : ExprContext {
//    override val conditions: List<Any> = emptyList()
}


