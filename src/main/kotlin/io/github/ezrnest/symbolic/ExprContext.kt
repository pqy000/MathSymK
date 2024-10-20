package io.github.ezrnest.symbolic
// created at 2024/10/01
import kotlin.math.max

interface ExprContext : NodeBuilderScope {

    val options: Map<TypedKey<*>, Any> get() = emptyMap()

    fun isCommutative(name: String): Boolean

    fun simplify(node: Node, depth: Int = Int.MAX_VALUE): Node


    fun fullSimplify(node: Node): Node {
        return simplify(node, Int.MAX_VALUE)
    }


    object Options {
        /**
         * Forces all the computations to be done in the real domain, throwing an ArithmeticException for undefined operations like `sqrt(-1)`.
         */
        val forceReal: TypedKey<Boolean> = TypedKey("forceReal")
    }
}

/*
    fun sortTree(node: Node): Node {
        if (node is LeafNode) return node

        if (true == node[EMeta.sorted]) return node

        val res = when (node) {
            is Node1 -> node.newWithChildren(sortTree(node.child))
            is NodeChilded -> {
                val children = node.children.map { sortTree(it) }
                val newChildren = if (isCommutative(node.name)) {
                    children.sortedWith(NodeOrder)
                } else {
                    children
                }
                node.newWithChildren(newChildren)
            }

            else -> node
        }
        res[EMeta.sorted] = true

        return res
    }
 */


//object BasicAlgebraContext : ExprContext{
//
//
//}

val TestExprCal = BasicExprCal()

open class BasicExprCal : ExprContext {

    final override val options: MutableMap<TypedKey<*>, Any> = mutableMapOf()

    protected val dispatcher: TreeDispatcher<SimRule> = TreeDispatcher()

    var verbose: Verbosity = Verbosity.NONE

    enum class Verbosity {
        NONE, WHEN_APPLIED, ALL
    }


    protected var indent = "|  "
    protected var simLevel = -1


//    val rules: List<SimRule>

    protected val _reduceRules: MutableList<SimRule> = mutableListOf()

    val reduceRules: List<SimRule> get() = _reduceRules

    init {
        listOf(
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
        ).forEach { addRule(it) }
    }

    fun addRule(rule: SimRule) {
        val r = rule.init(this) ?: return
        _reduceRules.add(r)
        dispatcher.register(r.matcher, r)
    }

    override fun isCommutative(name: String): Boolean {
        return when (name) {
            Node.Names.ADD -> true
            Node.Names.MUL -> true
            else -> false
        }
    }

    protected inline fun log(level: Verbosity, supplier: () -> String) {
        if (verbose >= level) {
            println(indent.repeat(max(simLevel, 0)) + supplier())
        }
    }

    protected fun showNode(node: Node): String {
        val meta = node.meta
        if (meta.isEmpty()) return node.plainToString()
        return "${node.plainToString()}, ${node.meta}"
    }


    override fun simplify(node: Node, depth: Int): Node {
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
                    is Node1 -> simplifyRecur1(res, depth - 1)
                    is Node2 -> simplifyRecur2(res, depth - 1)
                    is Node3 -> simplifyRecur3(res, depth - 1)
                    is NodeN -> simplifyRecurN(res, depth - 1)
                    else -> res
                }
            }
            val appliedRule = dispatcher.dispatchUntil(res) { rule ->
                log(Verbosity.ALL) { "|> ${rule.description}" }
                val p = rule.simplify(res, this) ?: return@dispatchUntil false

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


    private fun simplifyRecur1(node: Node1, depth: Int): Node {
        val n1 = simplify(node.child, depth)
        if (n1 === node.child) return node
        return node.newWithChildren(n1)
    }

    private fun simplifyRecur2(node: Node2, depth: Int): Node {
        val n1 = simplify(node.first, depth)
        val n2 = simplify(node.second, depth)
        if (n1 === node.first && n2 === node.second) return node
        return node.newWithChildren(n1, n2)
    }

    private fun simplifyRecur3(node: Node3, depth: Int): Node {
        val n1 = simplify(node.first, depth)
        val n2 = simplify(node.second, depth)
        val n3 = simplify(node.third, depth)
        if (n1 === node.first && n2 === node.second && n3 === node.third) return node
        return node.newWithChildren(n1, n2, n3)
    }

    private fun simplifyRecurN(node: NodeN, depth: Int): Node {
        var changed = false
        val children = node.children.map { n ->
            simplify(n, depth).also { if (n !== it) changed = true }
        }
        if (!changed) return node
        return node.newWithChildren(children)
    }

}
