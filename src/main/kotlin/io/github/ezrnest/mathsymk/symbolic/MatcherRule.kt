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
    fun simplifyMatched(node: T, matchContext: MatchContext): WithInt<Node>?


    override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        if (node[metaKeyApplied] == true) return null
        var matchContext = MatchContext(ctx)
        matchContext = matcher.matches(node, matchContext) ?: return null
        @Suppress("UNCHECKED_CAST")
        val nodeT = node as T
        val res = simplifyMatched(nodeT, matchContext)
        if (res != null) return res
        node[metaKeyApplied] = true // tried but not applicable
        return null
    }
}


interface NodeScopeRef : NodeScope {
    fun ref(name: String): Node {
        return Node.Symbol("_$name")
    }
}

interface NodeScopePredefinedRef : NodeScopeRef, NodeScopeWithPredefined {
    override val x: Node get() = ref("x")
    override val y: Node get() = ref("y")
    override val z: Node get() = ref("z")
    override val w: Node get() = ref("w")
    override val a: Node get() = ref("a")
    override val b: Node get() = ref("b")
    override val c: Node get() = ref("c")
}

class ForMatchScope(override val context: ExprContext) : NodeScopePredefinedRef, NodeScopeWithPredefined {

    companion object {
        private fun buildSymbol(node: NSymbol): NodeMatcher {
            val ref = node.ch
            if (ref.startsWith("_")) {
                return MatcherRef(ref.substring(1))
            }
            return FixedNodeMatcher(node)
        }

        private fun buildMatcher0(node: Node, cal: ExprCal): NodeMatcher {
            when (node) {
                is NSymbol -> return buildSymbol(node)
                is LeafNode -> return FixedNodeMatcher(node)
                is Node1 -> {
                    val child = buildMatcher0(node.child, cal)
                    return NodeMatcher1(child, node.signature)
                }

                is Node2 -> {
                    val left = buildMatcher0(node.first, cal)
                    val right = buildMatcher0(node.second, cal)
                    return NodeMatcher2Ordered(left, right, node.signature)
                }

                is Node3 -> {
                    val first = buildMatcher0(node.first, cal)
                    val second = buildMatcher0(node.second, cal)
                    val third = buildMatcher0(node.third, cal)
                    return NodeMatcher3Ordered(first, second, third, node.signature)
                }

                is NodeChilded -> {
                    val children = node.children.map { buildMatcher0(it, cal) }
                    if (cal.isCommutative(node.name)) {
                        return NodeMatcherNPO(children, node.signature)
                    }
                    return NMatcherNOrdered(children, node.signature)
                }

                else -> throw IllegalArgumentException("Unknown node type: ${node::class.simpleName}")
            }
        }

        fun buildMatcher(node: Node, cal: ExprCal): NodeMatcher {
            return buildMatcher0(node, cal)
        }

        fun warpPartialMatcherReplace(
            matcher: NodeMatcher, rep: RepBuilder, description: String,
            maxDepth: Int = Int.MAX_VALUE
        ): MatcherReplaceRule {
            if (matcher is NodeMatcherNPO && matcher.remMatcher is NothingMatcher) {
                val sig = matcher.nodeSig
                val remName = "_rem${matcher.nodeSig.name}"
                val rem = MatcherRef(remName)
                matcher.remMatcher = rem
                val replacement: RepBuilder = {
                    rep()?.let { sub ->
                        if (!hasRef(remName)) {
                            sub
                        } else {
                            Node.NodeN(sig.name, listOf(sub, getRef(remName)))
                        }
                    }
                }
                return MatcherReplaceRule(matcher, replacement, description, maxDepth)
            }
            return MatcherReplaceRule(matcher, rep, description, maxDepth)
        }
    }
}


interface AfterMatchScope : NodeScope, NodeScopeWithPredefined {

    val matchContext: MatchContext

    override val context: ExprContext
        get() = matchContext.exprContext


    fun getRef(name: String): Node {
        return matchContext.refMap[name] ?: throw IllegalArgumentException("No reference found for $name")
    }

    fun hasRef(name: String): Boolean {
        return matchContext.refMap.containsKey(name)
    }

    override val x: Node get() = getRef("x")
    override val y: Node get() = getRef("y")
    override val z: Node get() = getRef("z")
    override val w: Node get() = getRef("w")

    override val a: Node get() = getRef("a")
    override val b: Node get() = getRef("b")
    override val c: Node get() = getRef("c")

    val String.ref get() = getRef(this)

    companion object {
        private class AfterMatchScopeImpl(override val matchContext: MatchContext) : AfterMatchScope

        operator fun invoke(matchContext: MatchContext): AfterMatchScope = AfterMatchScopeImpl(matchContext)
    }
}


typealias RepBuilder = AfterMatchScope.() -> Node?

class MatcherReplaceRule(
    override val matcher: NodeMatcherT<*>,
    val replacement: RepBuilder,
    override val description: String,
    val afterDepth: Int = Int.MAX_VALUE
) : SimRule {

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey(description)

    override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        var matchCtx = MatchContext(ctx)
        matchCtx = matcher.matches(node, matchCtx) ?: return null
        val replacementNode = AfterMatchScope(matchCtx).replacement() ?: return null
        return WithInt(afterDepth, replacementNode)
    }
}

class MatchNodeReplaceRule(
    private val nodeInit: ForMatchScope.() -> Node,
    private val replacement: RepBuilder,
    override val description: String,
    private val afterDepth: Int = Int.MAX_VALUE,
    private val allowPartialMatch: Boolean = true
) : SimRule {
    override val metaKeyApplied: TypedKey<Boolean>
        get() = throw IllegalStateException("Matcher is not initialized")

    override val matcher: NodeMatcherT<Node>
        get() = throw IllegalStateException("Matcher is not initialized")


    override fun init(cal: ExprCal): SimRule? {
//        val nodeMatch = context.simplify(nodeInit(context))
//        val nodeRep = context.simplify()
        val node = cal.reduce(ForMatchScope(cal.context).nodeInit())
        val matcher = ForMatchScope.buildMatcher(node, cal)
        return if (allowPartialMatch) {
            ForMatchScope.warpPartialMatcherReplace(matcher, replacement, description, afterDepth)
        } else {
            MatcherReplaceRule(matcher, replacement, description, afterDepth)
        }
    }

    override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        throw IllegalStateException("Matcher is not initialized")
    }
}