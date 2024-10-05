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

    fun isCommutative(name: String): Boolean = false


    fun sortTree(node: Node): Node {
        if (node is LeafNode) return node

        if (true == node[EMeta.sorted]) return node

        val res = when (node) {
            is Node1 -> node.newWithChildren(sortTree(node.child))
            is Node2 -> sortNode2(node)
            is Node3 -> sortNode3(node)
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

    fun simplifyFull(node: Node): Node {
        return simplifyNode(node,Int.MAX_VALUE)
    }

    fun simplifyNode(node: Node, depth: Int = 0): Node

    fun NodeN(name: String, children: List<Node>): Node {
        require(children.isNotEmpty())
        val children = if (isCommutative(name)) children.sortedWith(nodeOrder) else children
        return NodeNImpl(name, children)
    }

    fun Add(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return Node.ZERO
        return NodeN(Node.Names.ADD, nodes)
    }

    fun Mul(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return Node.ONE
        return NodeN(Node.Names.MUL, nodes)
    }


    fun Node1(name: String, child: Node): Node {
        return Node1Impl(name, child)
    }

    fun Node2(name: String, first: Node, second: Node): Node {
        return if (isCommutative(name)) {
            val a = nodeOrder.compare(first, second)
            if (a <= 0) {
                Node2Impl(name, first, second)
            } else {
                Node2Impl(name, second, first)
            }
        } else {
            Node2Impl(name, first, second)
        }
    }

    fun Node3(name: String, first: Node, second: Node, third: Node): Node {
        if (!isCommutative(name)) return Node3Impl(name, first, second, third)
        val a = nodeOrder.compare(first, second)
        val b = nodeOrder.compare(second, third)
        val c = nodeOrder.compare(first, third)
        return when {
            a <= 0 && b <= 0 -> Node3Impl(name, first, second, third)
            a <= 0 && c <= 0 -> Node3Impl(name, first, third, second)
            b <= 0 && c <= 0 -> Node3Impl(name, second, third, first)
            a <= 0 -> Node3Impl(name, first, second, third)
            b <= 0 -> Node3Impl(name, second, third, first)
            c <= 0 -> Node3Impl(name, first, third, second)
            else -> Node3Impl(name, third, second, first)
        }
    }

    fun Neg(child: Node): Node {
        return Mul(listOf(Node.NEG_ONE, child))
    }

    fun Inv(child: Node): Node {
        return Pow(child, Node.NEG_ONE)
    }

    fun Pow(base: Node, exp: Node): Node {
        return Node2(Node.Names.POW, base, exp)
    }


    companion object {
        private fun ExprContext.sortNode2(node: Node2): Node2 {
            if (!isCommutative(node.name)) return node
            val first = sortTree(node.first)
            val second = sortTree(node.second)
            return if (nodeOrder.compare(first, second) <= 0) {
                node.newWithChildren(first, second)
            } else {
                node.newWithChildren(second, first)
            }
        }

        private fun ExprContext.sortNode3(node: Node3): Node3 {
            if (!isCommutative(node.name)) return node
            val first = sortTree(node.first)
            val second = sortTree(node.second)
            val third = sortTree(node.third)
            val a = nodeOrder.compare(first, second)
            val b = nodeOrder.compare(second, third)
            val c = nodeOrder.compare(first, third)
            return when {
                a <= 0 && b <= 0 -> node.newWithChildren(first, second, third)
                a <= 0 && c <= 0 -> node.newWithChildren(first, third, second)
                b <= 0 && c <= 0 -> node.newWithChildren(second, third, first)
                a <= 0 -> node.newWithChildren(first, second, third)
                b <= 0 -> node.newWithChildren(second, third, first)
                c <= 0 -> node.newWithChildren(first, third, second)
                else -> node.newWithChildren(third, second, first)
            }
        }
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
        MergeAdditionRational(),
        MergeProduct(),
    )


    private fun simplifyOne(node: Node): Node {
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
            }
            if (!applied) break
        }
        return res
    }

    override fun simplifyNode(node: Node, depth: Int): Node {
        return node.recurMap(depth) { n ->
            simplifyOne(n)
        }
    }
}
