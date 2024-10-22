package io.github.ezrnest.symbolic


interface NodeComplexity {

    fun complexity(node: Node, ctx: ExprContext): Int
}


object BasicComplexity : NodeComplexity {

    override fun complexity(node: Node, ctx: ExprContext): Int {
        return when (node) {
            is LeafNode -> 1
            is NodeChilded -> 1 + node.children.sumOf { complexity(it, ctx) }
            else -> 1
        }
    }
}