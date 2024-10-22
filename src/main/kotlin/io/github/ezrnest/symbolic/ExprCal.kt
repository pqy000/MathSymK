package io.github.ezrnest.symbolic

import kotlin.math.max


interface ExprCal {

    val options: Map<TypedKey<*>, Any> get() = emptyMap()

    val context: ExprContext

    object Options {
        /**
         * Forces all the computations to be done in the real domain, throwing an ArithmeticException for undefined operations like `sqrt(-1)`.
         */
        val forceReal: TypedKey<Boolean> = TypedKey("forceReal")
    }


    fun isCommutative(name: String): Boolean

    fun simplifyFull(root: Node): Node {
        return simplifyNode(root, context, Int.MAX_VALUE)
    }

    fun simplifyNode(node: Node, context: ExprContext, depth: Int = 0): Node
}


val TestExprContext = BasicExprCal()

class BasicExprCal : ExprCal {

    override val options: MutableMap<TypedKey<*>, Any> = mutableMapOf()

    val dispatcher: TreeDispatcher<SimRule> = TreeDispatcher()

    var verbose: Verbosity = Verbosity.NONE

    override val context: ExprContext = BasicExprContext()

    enum class Verbosity {
        NONE, WHEN_APPLIED, ALL
    }


    private val indent = "|  "
    private var simLevel = -1


    val rules: List<SimRule> = listOf(
//        RegularizeNodeN(Node.Names.ADD),
//        RegularizeNodeN(Node.Names.MUL),
        Flatten(Node.Names.ADD),
        Flatten(Node.Names.MUL),
        RuleSort(NodeSig.ADD),
        RuleSort(NodeSig.MUL),
        MergeAdditionRational,
//        ComputeProductRational,
        MergeProduct,
        ComputePow,
        FlattenPow
    )

    init {
        for (r in rules) {
            dispatcher.register(r.matcher, r)
        }
    }

    fun addRule(rule: SimRule) {
        dispatcher.register(rule.matcher, rule)
    }

    override fun isCommutative(name: String): Boolean {
        return when (name) {
            Node.Names.ADD -> true
            Node.Names.MUL -> true
            else -> false
        }
    }

    private inline fun log(level: Verbosity, supplier: () -> String) {
        if (verbose >= level) {
            println(indent.repeat(max(simLevel, 0)) + supplier())
        }
    }

    private fun showNode(node: Node): String {
        val meta = node.meta
        if (meta.isEmpty()) return node.plainToString()
        return "${node.plainToString()}, ${node.meta}"
    }


    override fun simplifyNode(node: Node, context: ExprContext, depth: Int): Node {

        var depth = depth
        var res = node
        simLevel++
        log(Verbosity.WHEN_APPLIED) { "Sim:  ${showNode(node)}" }
        while (true) {
            if (res[NodeMetas.simplified] == true) break
            res = if (depth <= 0) {
                res
            } else {
                when (res) {
                    is LeafNode -> res
                    is Node1 -> simplifyRecur1(res, context, depth - 1)
                    is Node2 -> simplifyRecur2(res, context, depth - 1)
                    is Node3 -> simplifyRecur3(res, context, depth - 1)
                    is NodeN -> simplifyRecurN(res, context, depth - 1)
                    else -> res
                }
            }
            val appliedRule = dispatcher.dispatchUntil(res) { rule ->
                log(Verbosity.ALL) { "|> ${rule.description}" }
                val p = rule.simplify(res, context, this) ?: return@dispatchUntil false

                if (verbose == Verbosity.WHEN_APPLIED) {
                    log(Verbosity.WHEN_APPLIED) { "|>${rule.description}" }
                }
                depth = p.level
                res = p.item
                log(Verbosity.WHEN_APPLIED) { "|->  ${showNode(res)}" }
                true
            }
            if (appliedRule == null) {
                log(Verbosity.ALL) { "|> Nothing happened ..." }
                log(Verbosity.ALL) { "|->  ${showNode(res)}" }
                break
            }
        }

        simLevel--
        return res
    }


    private fun simplifyRecur1(node: Node1, context: ExprContext, depth: Int): Node {
        val n1 = simplifyNode(node.child, context, depth)
        if (n1 === node.child) return node
        return node.newWithChildren(n1)
    }

    private fun simplifyRecur2(node: Node2, context: ExprContext, depth: Int): Node {
        val n1 = simplifyNode(node.first, context, depth)
        val n2 = simplifyNode(node.second, context, depth)
        if (n1 === node.first && n2 === node.second) return node
        return node.newWithChildren(n1, n2)
    }

    private fun simplifyRecur3(node: Node3, context: ExprContext, depth: Int): Node {
        val n1 = simplifyNode(node.first, context, depth)
        val n2 = simplifyNode(node.second, context, depth)
        val n3 = simplifyNode(node.third, context, depth)
        if (n1 === node.first && n2 === node.second && n3 === node.third) return node
        return node.newWithChildren(n1, n2, n3)
    }

    private fun simplifyRecurN(node: NodeN, context: ExprContext, depth: Int): Node {
        var changed = false
        val children = node.children.map { n ->
            simplifyNode(n, context, depth).also { if (n !== it) changed = true }
        }
        if (!changed) return node
        return node.newWithChildren(children)
    }

}
