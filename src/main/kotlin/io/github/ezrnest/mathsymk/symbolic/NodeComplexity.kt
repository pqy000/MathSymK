package io.github.ezrnest.mathsymk.symbolic


interface NodeComplexity {

    fun complexity(node: Node, ctx: ExprContext): Int
}


object BasicComplexity : NodeComplexity {

    override fun complexity(node: Node, ctx: ExprContext): Int {
        return when (node) {
            is NRational -> {
                val (nume, deno) = node.value
                nume.bitCount() + deno.bitLength() - 1
            }
            is NSymbol -> 5
            is LeafNode -> 5
            is NodeChilded -> 1 + node.children.sumOf { complexity(it, ctx) }
            else -> 10
        }
    }
}