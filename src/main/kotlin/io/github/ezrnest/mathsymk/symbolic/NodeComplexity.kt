package io.github.ezrnest.mathsymk.symbolic


interface NodeComplexity {

    fun complexity(node: Node, ctx: ExprContext): Int
}


object BasicComplexity : NodeComplexity {

    override fun complexity(node: Node, ctx: ExprContext): Int {
        return when (node) {
            is NRational -> 1
            is NSymbol -> 2
            is LeafNode -> 2
            is NodeChilded -> 3 + node.children.sumOf { complexity(it, ctx) }
            else -> 1
        }
    }
}