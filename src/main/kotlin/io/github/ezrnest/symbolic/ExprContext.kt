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

    fun simplifyOne(node: Node): Node

    fun simplifyNode(node: Node, depth: Int = 0): Node {
        if (depth <= 0) return simplifyOne(node)
        val res = when (node) {
            is LeafNode -> node
            is Node1 -> Node.Node1(node.name, simplifyNode(node.child, depth - 1))
            is Node2 -> Node.Node2(node.name, simplifyNode(node.first, depth - 1), simplifyNode(node.second, depth - 1))
            is Node3 -> Node.Node3(
                node.name,
                simplifyNode(node.first, depth - 1),
                simplifyNode(node.second, depth - 1),
                simplifyNode(node.third, depth - 1)
            )
            is NodeN -> Node.NodeN(node.name, node.children.map { simplifyNode(it, depth - 1) })
        }
        return simplifyOne(res)
    }


    companion object {
//        fun ExprContext.sortNode2(node: Node2): Node2 {
//            if (!isCommutative(node.name)) return node
//            val first = sortTree(node.first)
//            val second = sortTree(node.second)
//            return if (nodeOrder.compare(first, second) <= 0) {
//                node.newWithChildren(first, second)
//            } else {
//                node.newWithChildren(second, first)
//            }
//        }
//
//        fun ExprContext.sortNode3(node: Node3): Node3 {
//            if (!isCommutative(node.name)) return node
//            val first = sortTree(node.first)
//            val second = sortTree(node.second)
//            val third = sortTree(node.third)
//            val a = nodeOrder.compare(first, second)
//            val b = nodeOrder.compare(second, third)
//            val c = nodeOrder.compare(first, third)
//            return when {
//                a <= 0 && b <= 0 -> node.newWithChildren(first, second, third)
//                a <= 0 && c <= 0 -> node.newWithChildren(first, third, second)
//                b <= 0 && c <= 0 -> node.newWithChildren(second, third, first)
//                a <= 0 -> node.newWithChildren(first, second, third)
//                b <= 0 -> node.newWithChildren(second, third, first)
//                c <= 0 -> node.newWithChildren(first, third, second)
//                else -> node.newWithChildren(third, second, first)
//            }
//        }
//
    }
}

object TestExprContext : ExprContext {

    override val rational: BigFractionAsQuotients
        get() = BigFractionAsQuotients

    override val nodeOrder: NodeOrder
        get() = DefaultNodeOrder

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
        MergeAdditionRational(),
        ComputeProduct,
        MergeProduct(),
    )


    override fun simplifyOne(node: Node): Node {
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
