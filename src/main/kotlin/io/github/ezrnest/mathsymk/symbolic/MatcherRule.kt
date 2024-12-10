package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.util.WithInt


interface SimRuleMatched<T : Node> : SimRule {


    /**
     * A matcher describing the nodes that the rule can be applied to.
     */
    override val matcher: NodeMatcherT<T>

    /**
     * Simplify the matched node.
     */
    fun simplifyMatched(node: T, matchResult: MatchResult): WithInt<Node>?


    override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        if (node[metaKeyApplied] == true) return null
        var matchResult = MatchResult(cal)
        matchResult = matcher.matches(node, ctx, matchResult) ?: return null
        @Suppress("UNCHECKED_CAST")
        val nodeT = node as T
        val res = simplifyMatched(nodeT, matchResult)
        if (res != null) return res
        node[metaKeyApplied] = true // tried but not applicable
        return null
    }
}



interface INodeScopeReferred : NodeScope {
    /**
     * Gets a reference to a node with the given name.
     */
    fun ref(name: String): Node

    /**
     * Checks if a reference with the given name exists.
     */
    fun hasRef(name: String): Boolean
}

interface NodeScopeReferred : NodeScopeWithPredefined, INodeScopeReferred {
    override val x: Node get() = ref("x")
    override val y: Node get() = ref("y")
    override val z: Node get() = ref("z")
    override val w: Node get() = ref("w")

    override val a: Node get() = ref("a")
    override val b: Node get() = ref("b")
    override val c: Node get() = ref("c")


    val String.ref
        get() = ref(this)
}


interface NodeScopeMatched : NodeScope, NodeScopeReferred {

    val matchResult: MatchResult

    override val context: EContext


    override fun ref(name: String): Node {
        return matchResult.refMap[name] ?: throw IllegalArgumentException("No reference found for $name")
    }

    override fun hasRef(name: String): Boolean {
        return matchResult.refMap.containsKey(name)
    }


    companion object {
        private class NodeScopeMatchedImpl(context: EContext, override val matchResult: MatchResult) :
            AbstractNodeScope(context), NodeScopeMatched

        operator fun invoke(ctx: EContext, matchResult: MatchResult): NodeScopeMatched =
            NodeScopeMatchedImpl(ctx, matchResult)
    }
}


typealias RepBuilder = NodeScopeMatched.() -> Node?

class MatcherReplaceRule(
    override val matcher: NodeMatcherT<*>,
    val replacement: RepBuilder,
    override val description: String,
    val afterDepth: Int = Int.MAX_VALUE
) : SimRule {

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey(description)

    override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        var matchCtx = MatchResult(cal)
        matchCtx = matcher.matches(node, ctx, matchCtx) ?: return null
        val replacementNode = NodeScopeMatched(ctx, matchCtx).replacement() ?: return null
        return WithInt(afterDepth, replacementNode)
    }
}

class BuilderMatchNodeReplaceRule(
    private val nodeInit: NodeScopeMatcher.() -> Node,
    private val replacement: RepBuilder,
    val description: String,
    private val afterDepth: Int = Int.MAX_VALUE,
    private val allowPartialMatch: Boolean = true
) : BuilderSimRule {


    override fun init(cal: ExprCal): SimRule? {
//        val nodeMatch = context.simplify(nodeInit(context))
//        val nodeRep = context.simplify()
        val matcherScope = NodeScopeMatcher(cal.context)
        val node = cal.reduce(matcherScope.nodeInit())
        val matcher = NodeScopeMatcher.buildMatcher(matcherScope, node, cal)
        return if (allowPartialMatch) {
            NodeScopeMatcher.warpPartialMatcherReplace(matcher, replacement, description, afterDepth)
        } else {
            MatcherReplaceRule(matcher, replacement, description, afterDepth)
        }
    }

}