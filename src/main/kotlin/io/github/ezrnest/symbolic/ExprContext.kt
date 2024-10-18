package io.github.ezrnest.symbolic
// created at 2024/10/01
import io.github.ezrnest.model.BigFractionAsQuotients
import kotlin.math.max


//interface ExprCal {
//    fun simplifyNode(node: Node): Node
//
//    val context: ExprContext
//}

interface ExprContext {
    val rational: BigFractionAsQuotients

    val nodeOrder: NodeComparator

    val options: Map<TypedKey<*>, Any> get() = emptyMap()

    fun isCommutative(name: String): Boolean


    fun sortTree(node: Node): Node {
        if (node is LeafNode) return node

        if (true == node[EMeta.sorted]) return node

        val res = when (node) {
            is Node1 -> node.newWithChildren(sortTree(node.child))
//            is Node2 -> sortNode2(node)
//            is Node3 -> sortNode3(node)
            is NodeChilded -> {
                val children = node.children.map { sortTree(it) }
                val newChildren = if (isCommutative(node.name)) {
                    children.sortedWith(nodeOrder)
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

    fun simplifyFull(root: Node): Node {
        return simplifyNode(root, Int.MAX_VALUE)
    }

    fun simplifyNode(node: Node, depth: Int = 0): Node

    object Options {
        /**
         * Forces all the computations to be done in the real domain, throwing an ArithmeticException for undefined operations like `sqrt(-1)`.
         */
        val forceReal: TypedKey<Boolean> = TypedKey("forceReal")
    }
}

object TestExprContext : ExprContext {

    override val rational: BigFractionAsQuotients
        get() = BigFractionAsQuotients

    override val nodeOrder: NodeComparator
        get() = NodeOrder

    override val options: MutableMap<TypedKey<*>, Any> = mutableMapOf()

    val dispatcher: TreeDispatcher<SimRule> = TreeDispatcher()

    var verbose = false
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

    private fun println(s: String) {
        if (verbose) kotlin.io.println(indent.repeat(max(simLevel, 0)) + s)
    }


    override fun simplifyNode(node: Node, depth: Int): Node {
        var depth = depth
        var res = node
        simLevel++
        if (verbose) println("Simplifying: ${node.plainToString()}, ${node.meta}")
        while (true) {
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
                if (verbose) println("|> ${rule.description}")
                val p = rule.simplify(res, this) ?: return@dispatchUntil false
                depth = p.level
                res = p.item
                if (verbose) println("|To: ${res.plainToString()}, ${res.meta}")
                true
            }
            if (appliedRule == null) {
                if (verbose) println("|> Nothing happened ...")
                break
            }
        }
        if(verbose) println("|-> ${res.plainToString()}")
        simLevel--
        return res
    }


    private fun simplifyRecur1(node: Node1, depth: Int): Node {
        val n1 = simplifyNode(node.child, depth)
        if (n1 === node.child) return node
        return node.newWithChildren(n1)
    }

    private fun simplifyRecur2(node: Node2, depth: Int): Node {
        val n1 = simplifyNode(node.first, depth)
        val n2 = simplifyNode(node.second, depth)
        if (n1 === node.first && n2 === node.second) return node
        return node.newWithChildren(n1, n2)
    }

    private fun simplifyRecur3(node: Node3, depth: Int): Node {
        val n1 = simplifyNode(node.first, depth)
        val n2 = simplifyNode(node.second, depth)
        val n3 = simplifyNode(node.third, depth)
        if (n1 === node.first && n2 === node.second && n3 === node.third) return node
        return node.newWithChildren(n1, n2, n3)
    }

    private fun simplifyRecurN(node: NodeN, depth: Int): Node {
        var changed = false
        val children = node.children.map { n ->
            simplifyNode(n, depth).also { if (n !== it) changed = true }
        }
        if (!changed) return node
        return node.newWithChildren(children)
    }

}
