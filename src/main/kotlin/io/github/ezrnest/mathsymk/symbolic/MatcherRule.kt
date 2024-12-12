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
    fun simplifyMatched(node: T, matching: Matching): WithInt<Node>?


    override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        if (node[metaKeyApplied] == true) return null
        val matching = matcher.matches(node, cal) ?: return null

        @Suppress("UNCHECKED_CAST")
        val nodeT = node as T
        val res = simplifyMatched(nodeT, matching)
        if (res != null) return res
        node[metaKeyApplied] = true // tried but not applicable
        return null
    }
}


//interface INodeScopeReferred : NodeScope {
//    /**
//     * Gets a reference to a node with the given name.
//     */
//    fun ref(name: ESymbol): Node
//
//    /**
//     * Checks if a reference with the given name exists.
//     */
//    fun hasRef(name: ESymbol): Boolean
//}
//
//interface NodeScopeReferred : NodeScopeWithPredefined, INodeScopeReferred {
//    override val x: Node get() = ref("x")
//    override val y: Node get() = ref("y")
//    override val z: Node get() = ref("z")
//    override val w: Node get() = ref("w")
//
//    override val a: Node get() = ref("a")
//    override val b: Node get() = ref("b")
//    override val c: Node get() = ref("c")
//
//
//    val String.ref
//        get() = ref(this)
//}
//
//
//interface NodeScopeMatched : NodeScope, NodeScopeReferred {
//
//    val matching: Matching
//
//    override val context: EContext
//
//
//    override fun ref(name: ESymbol): Node {
//        return matching.refMap[name] ?: throw IllegalArgumentException("No reference found for $name")
//    }
//
//    override fun hasRef(name: ESymbol): Boolean {
//        return matching.refMap.containsKey(name)
//    }
//
//
//    companion object {
//        private class NodeScopeMatchedImpl(context: EContext, override val matching: Matching) :
//            AbstractNodeScope(context), NodeScopeMatched
//
//        operator fun invoke(ctx: EContext, matching: Matching): NodeScopeMatched =
//            NodeScopeMatchedImpl(ctx, matching)
//    }
//}


typealias RepBuilder = (EContext, ExprCal, Matching) -> Node?


class MatcherNodeReplaceRule(
    override val description: String,
    override val matcher: NodeMatcherT<*>,
    val repNode: Node,
    val afterDepth: Int = Int.MAX_VALUE
) : SimRule {

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey(description)

    override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        val matching = matcher.matches(node, cal) ?: return null
        val result = NodeScopeMatcher.substituteRef(repNode, matching)
        return WithInt(afterDepth, result)
    }
}


class MatcherReplaceRule(
    override val matcher: NodeMatcherT<*>,
    val replacement: RepBuilder,
    override val description: String,
    val afterDepth: Int = Int.MAX_VALUE
) : SimRule {

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey(description)

    override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        val matchCtx = matcher.matches(node, cal) ?: return null
        val replacementNode = replacement(ctx, cal, matchCtx) ?: return null
        return WithInt(afterDepth, replacementNode)
    }

    companion object {

        fun warpPartialMatcherReplace(
            matcher: NodeMatcher, rep: RepBuilder, description: String,
            maxDepth: Int = Int.MAX_VALUE
        ): MatcherReplaceRule {
            if (matcher is NodeMatcherNPO && matcher.remMatcher is NothingMatcher) {
                val sym = matcher.symbol
                val remName = ESymbol("rem${matcher.symbol.name}")
                val rem = MatcherRef(remName)
                matcher.remMatcher = rem
                val replacement: RepBuilder = { ctx, cal, matching ->
                    rep(ctx, cal, matching)?.let { sub ->
                        val remNode = matching.getRef(remName)
                        if (remNode == null) {
                            sub
                        } else {
                            NodeN(sym, listOf(sub, remNode))
                        }
                    }
                }
                return MatcherReplaceRule(matcher, replacement, description, maxDepth)
            }
            return MatcherReplaceRule(matcher, rep, description, maxDepth)
        }


    }
}

//class BuilderMatchNodeReplaceRule(
//    private val nodeInit: NodeScopeMatcher.() -> Node,
//    private val replacement: RepBuilder,
//    val description: String,
//    private val afterDepth: Int = Int.MAX_VALUE,
//    private val allowPartialMatch: Boolean = true
//) : SimRuleProvider {
//
//
//    override fun init(cal: ExprCal): List<SimRule> {
////        val nodeMatch = context.simplify(nodeInit(context))
////        val nodeRep = context.simplify()
//        val matcherScope = NodeScopeMatcher(cal.context)
//        val node = cal.reduce(matcherScope.nodeInit())
//        val matcher = NodeScopeMatcher.buildMatcher(matcherScope, node, cal)
//        return if (allowPartialMatch) {
//            MatcherReplaceRule.warpPartialMatcherReplace(matcher, replacement, description, afterDepth)
//        } else {
//            MatcherReplaceRule(matcher, replacement, description, afterDepth)
//        }
//    }
//
//}