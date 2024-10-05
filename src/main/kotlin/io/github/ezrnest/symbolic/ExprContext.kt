package io.github.ezrnest.symbolic

import io.github.ezrnest.model.BigFractionAsQuotients


interface ExprCal {
    fun simplifyNode(node: Node): Node

    val context: ExprContext
}

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
            Node.NAME_ADD -> true
            Node.NAME_MUL -> true
            else -> false
        }
    }
}

object TestExprCal : ExprCal {
    override val context: ExprContext = TestExprContext
    val rules: List<SimRule> = listOf(
        RegularizeNodeN(Node.NAME_ADD),
        RegularizeNodeN(Node.NAME_MUL),
        Flatten(Node.NAME_ADD),
        Flatten(Node.NAME_MUL),
        MergeAdditionRational()
    )


    private fun simplifyOne(node: Node): Node {
        var res = node
        while (true) {
            val next = rules.fold(res) { acc, rule ->
                val r = rule.simplify(acc, context)
                if (r != null) {
                    context.sortTree(r)
                } else {
                    acc
                }
//                 rule.simplify(acc, context)?.let { context.nodeOrder.sortTree(it) } ?: acc
            }
            if (next === res) return res // no change
            res = next
        }
    }

    override fun simplifyNode(node: Node): Node {
        return node.recurMap { n ->
            simplifyOne(n)
        }
    }
}