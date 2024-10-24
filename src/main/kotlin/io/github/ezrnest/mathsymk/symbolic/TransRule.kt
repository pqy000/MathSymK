package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.util.WithInt

interface TransRule {
    val description: String

    /**
     * The key for marking the node as tried by the rule but not applicable.
     * This can be used to avoid trying the same rule again.
     */
    val metaKeyApplied: TypedKey<Boolean>

    val matcher: NodeMatcherT<Node>
        get() = AnyMatcher

    fun transform(node : Node, ctx: ExprContext, cal: ExprCal) : List<WithInt<Node>>

    fun init(context: ExprCal): TransRule? {
        return this
    }
}