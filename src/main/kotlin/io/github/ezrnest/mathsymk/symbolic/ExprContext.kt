package io.github.ezrnest.mathsymk.symbolic
// created at 2024/10/01


/**
 * Describes the assumptions of the expression.
 */
interface ExprContext {
    val conditions : List<Any> // TODO
        get() = emptyList()
}

object EmptyExprContext : ExprContext

class BasicExprContext : ExprContext {
//    override val conditions: List<Any> = emptyList()
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

