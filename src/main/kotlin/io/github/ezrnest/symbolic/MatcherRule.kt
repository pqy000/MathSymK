package io.github.ezrnest.symbolic


object NodeBuilderForMatch : NodeBuilderScope {

    override fun symbol(name: String): Node {
        return ref(name)
    }

    fun ref(name: String): Node {
        return Node.Symbol("_$name")
    }

    override val x: Node get() = ref("x")
    override val y: Node get() = ref("y")
    override val z: Node get() = ref("z")

    private fun buildSymbol(node: NSymbol): NodeMatcher {
        val ref = node.ch
        if (ref.startsWith("_")) {
            return MatcherRef(ref.substring(1))
        }
        return FixedNodeMatcher(node)
    }

    private fun buildMatcher0(node: Node, cal: ExprCal ): NodeMatcher {
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

    fun buildMatcher(build: NodeBuilderScope.() -> Node, cal: ExprCal): NodeMatcher {
        val node = cal.reduce(build(this))
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
}


interface ReplacementScope : NodeBuilderScope {

    val matchContext: MatchContext

    fun ref(name: String): Node {
        return matchContext.refMap[name] ?: throw IllegalArgumentException("No reference found for $name")
    }

    fun hasRef(name: String): Boolean {
        return matchContext.refMap.containsKey(name)
    }

    override fun symbol(name: String): Node {
        return ref(name)
    }

    override val x: Node get() = ref("x")
    override val y: Node get() = ref("y")
    override val z: Node get() = ref("z")
    override val a: Node get() = ref("a")
    override val b: Node get() = ref("b")
    override val c: Node get() = ref("c")

    val String.ref get() = ref(this)

    companion object {
        private class ReplacementScopeImpl(override val matchContext: MatchContext) : ReplacementScope

        fun create(matchContext: MatchContext): ReplacementScope = ReplacementScopeImpl(matchContext)
    }
}


typealias RepBuilder = ReplacementScope.() -> Node?

class MatcherReplaceRule(
    override val matcher: NodeMatcherT<*>,
    val replacement: RepBuilder,
    override val description: String,
    val afterDepth: Int = Int.MAX_VALUE
) : SimRule {

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey(description)

    override fun simplify(node: Node, context: ExprContext, cal : ExprCal): IndexedValue<Node>? {
        val matchCtx = MutableMatchContext(context)
        matcher.matches(node, matchCtx) ?: return null
        val replacementNode = ReplacementScope.create(matchCtx).replacement() ?: return null
        return IndexedValue(afterDepth, replacementNode)
    }
}

class MatchNodeReplaceRule(
    private val nodeInit: NodeBuilderScope.() -> Node,
    private val replacement: RepBuilder,
    override val description: String,
    private val afterDepth: Int = Int.MAX_VALUE,
    private val allowPartialMatch: Boolean = true
) : SimRule {
    override val metaKeyApplied: TypedKey<Boolean>
        get() = throw IllegalStateException("Matcher is not initialized")

    override val matcher: NodeMatcherT<Node>
        get() = throw IllegalStateException("Matcher is not initialized")


    override fun init(context: ExprCal): SimRule? {
//        val nodeMatch = context.simplify(nodeInit(context))
//        val nodeRep = context.simplify()
        val matcher = NodeBuilderForMatch.buildMatcher(nodeInit, context)
        return if (allowPartialMatch) {
            NodeBuilderForMatch.warpPartialMatcherReplace(matcher, replacement, description, afterDepth)
        } else {
            MatcherReplaceRule(matcher, replacement, description, afterDepth)
        }
    }

    override fun simplify(node: Node, context: ExprContext, cal : ExprCal): IndexedValue<Node>? {
        throw IllegalStateException("Matcher is not initialized")
    }
}