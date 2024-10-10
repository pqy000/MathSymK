package io.github.ezrnest.symbolic
// created at 2024/10/01
import io.github.ezrnest.model.BigFractionAsQuotients


//interface ExprCal {
//    fun simplifyNode(node: Node): Node
//
//    val context: ExprContext
//}

interface ExprContext {
    val rational: BigFractionAsQuotients

    val nodeOrder: NodeOrder

    val options : Map<TypedKey<*>, Any> get() = emptyMap()

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

    fun simplifyPlain(node: Node): Node

    fun simplifyNode(node: Node, depth: Int = 0): Node {
        if (depth <= 0) return simplifyPlain(node)
        val res = when (node) {
            is LeafNode -> node
            is Node1 -> simplifyRecur1(node, depth-1)
            is Node2 -> simplifyRecur2(node, depth-1)
            is Node3 -> simplifyRecur3(node, depth-1)
            is NodeN -> simplifyRecurN(node, depth-1)
            else -> node
        }
        return simplifyPlain(res)
    }


    companion object {

        private fun ExprContext.simplifyRecur1(node: Node1, depth: Int): Node {
            val n1 = simplifyNode(node.child, depth)
            if(n1 === node.child) return node
            return Node.Node1(node.name, n1)
        }

        private fun ExprContext.simplifyRecur2(node: Node2, depth: Int): Node {
            val n1 = simplifyNode(node.first, depth)
            val n2 = simplifyNode(node.second, depth)
            if(n1 === node.first && n2 === node.second) return node
            return Node.Node2(node.name, n1, n2)
        }

        private fun ExprContext.simplifyRecur3(node: Node3, depth: Int): Node {
            val n1 = simplifyNode(node.first, depth)
            val n2 = simplifyNode(node.second, depth)
            val n3 = simplifyNode(node.third, depth)
            if(n1 === node.first && n2 === node.second && n3 === node.third) return node
            return Node.Node3(node.name, n1, n2, n3)
        }

        private fun ExprContext.simplifyRecurN(node: NodeN, depth: Int): Node {
            var changed = false
            val children = node.children.map { n ->
                simplifyNode(n, depth).also { if(n !== it) changed = true }
            }
            if(!changed) return node
            return Node.NodeN(node.name, children)
        }
    }

    object Options{
        /**
         * Forces all the computations to be done in the real domain, throwing an ArithmeticException for undefined operations like `sqrt(-1)`.
         */
        val forceReal : TypedKey<Boolean> = TypedKey("forceReal")
    }
}

object TestExprContext : ExprContext {

    override val rational: BigFractionAsQuotients
        get() = BigFractionAsQuotients

    override val nodeOrder: NodeOrder
        get() = DefaultNodeOrder

    override val options: MutableMap<TypedKey<*>, Any> = mutableMapOf()

    init {

    }

    override fun isCommutative(name: String): Boolean {
        return when (name) {
            Node.Names.ADD -> true
            Node.Names.MUL -> true
            else -> false
        }
    }

    val rules: List<SimRule> = listOf(
        RegularizeNodeN(Node.Names.ADD),
        RegularizeNodeN(Node.Names.MUL),
        Flatten(Node.Names.ADD),
        Flatten(Node.Names.MUL),
        RuleSort,
        MergeAdditionRational,
//        ComputeProductRational,
        MergeProduct,
        ComputePow,
        FlattenPow
    )


    override fun simplifyPlain(node: Node): Node {
        var res = node
        var previousRule: SimRule? = null
        while (true) {
            var applied = false
            for (r in rules) {
                if (r === previousRule) continue // don't apply the same rule twice
                val simplified = r.simplify(res, this) ?: continue
                res = simplified
                applied = true
                previousRule = r
                break
            }
            if (!applied) break
        }
        return res
    }

}
