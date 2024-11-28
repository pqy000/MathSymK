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


    override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
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


interface INodeScopeReferring : NodeScope {
    fun ref(name: String): Node {
        return Node.Symbol("_$name")
    }

}

interface NodeScopeMatcher : INodeScopeReferring, NodeScopeWithPredefined {
    override val x: Node get() = ref("x")
    override val y: Node get() = ref("y")
    override val z: Node get() = ref("z")
    override val w: Node get() = ref("w")
    override val a: Node get() = ref("a")
    override val b: Node get() = ref("b")
    override val c: Node get() = ref("c")

    val String.ref get() = ref(this)

    fun Node.named(name: String): Node {
        return Node.Node2(F2_Named, this, ref(name))
    }

    fun Node.where(clause: Node): Node {
        return Node.Node2(F2_Where, this, clause)
    }

    fun Node.where(clauseBuilder: () -> Node): Node {
        return Node.Node2(F2_Where, this, clauseBuilder())
    }

    companion object {

        private data class NodeScopeMatcherImpl(override val context: ExprContext) : NodeScopeMatcher

        operator fun invoke(context: ExprContext): NodeScopeMatcher = NodeScopeMatcherImpl(context)

        val F2_Named = "_Named"
        val F2_Where = "_Where"

        private fun buildSymbol(node: NSymbol): NodeMatcher {
            val name = node.ch
            if (name.startsWith("_")) {
                return MatcherRef(name)
            }
            return FixedNodeMatcher(node)
        }

        private fun buildNamed(node: Node, cal: ExprCal): NodeMatcher {
            node as Node2
            val child = buildMatcher0(node.first, cal)
            val name = (node.second as NSymbol).ch
            return MatcherNamed(child, name)
        }

        private fun buildWhere(node: Node, cal: ExprCal): NodeMatcher {
            node as Node2
            val child = buildMatcher0(node.first, cal)
            val clauseRef = node.second
            return MatcherWithPostConditionNode(child, clauseRef)
        }

        private fun buildMatcher0(node: Node, cal: ExprCal): NodeMatcher {
            when (node.name) {
                F2_Named -> {
                    return buildNamed(node, cal)
                }

                F2_Where -> {
                    return buildWhere(node, cal)
                }
            }
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
                            Node.NodeN(sig.name, listOf(sub, ref(remName)))
                        }
                    }
                }
                return MatcherReplaceRule(matcher, replacement, description, maxDepth)
            }
            return MatcherReplaceRule(matcher, rep, description, maxDepth)
        }


        fun substituteIn(nodeRef: Node, rootCtx: ExprContext, matching: MatchResult): Node {
            val cal = matching.cal
            return cal.substitute(nodeRef, rootCtx) { node, ctx ->
                val name = node.ch
                if (name.startsWith("_")) {
                    val ref = matching.getRef(name)
                        ?: throw IllegalArgumentException("No reference found for [$name]")
                    return@substitute ref
                }
                return@substitute node
            }
        }

        fun testConditionRef(condRef: Node, ctx: ExprContext, matching: MatchResult): Boolean {
            val reifiedCond = substituteIn(condRef, ctx, matching)
            val cal = matching.cal
            return cal.isSatisfied(ctx, reifiedCond)
        }
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

    override val context: ExprContext


    override fun ref(name: String): Node {
        return matchResult.refMap[name] ?: throw IllegalArgumentException("No reference found for $name")
    }

    override fun hasRef(name: String): Boolean {
        return matchResult.refMap.containsKey(name)
    }


    companion object {
        private class NodeScopeMatchedImpl(override val context: ExprContext, override val matchResult: MatchResult) :
            NodeScopeMatched

        operator fun invoke(ctx: ExprContext, matchResult: MatchResult): NodeScopeMatched =
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

    override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        var matchCtx = MatchResult(cal)
        matchCtx = matcher.matches(node, ctx, matchCtx) ?: return null
        val replacementNode = NodeScopeMatched(ctx, matchCtx).replacement() ?: return null
        return WithInt(afterDepth, replacementNode)
    }
}

class MatchNodeReplaceRule(
    private val nodeInit: NodeScopeMatcher.() -> Node,
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
        val node = cal.reduce(NodeScopeMatcher(cal.context).nodeInit())
        val matcher = NodeScopeMatcher.buildMatcher(node, cal)
        return if (allowPartialMatch) {
            NodeScopeMatcher.warpPartialMatcherReplace(matcher, replacement, description, afterDepth)
        } else {
            MatcherReplaceRule(matcher, replacement, description, afterDepth)
        }
    }

    override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        throw IllegalStateException("Matcher is not initialized")
    }
}