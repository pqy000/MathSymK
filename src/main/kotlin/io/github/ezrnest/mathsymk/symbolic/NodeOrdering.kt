package io.github.ezrnest.mathsymk.symbolic



object NodeOrder : Comparator<Node> {

    fun nodeTypeOrdinal(node: Node): Int {
        return when (node) {
            is NOther -> -1
            is NRational -> 0
            is NSymbol -> 10
            is LeafNode -> 99
            is NodeChilded -> 100
//            is Node1 -> 100
//            is Node2 -> 110
//            is Node3 -> 120
//            is NodeN -> 1000
//            is NodeChilded -> 9999
        }
    }

    private fun compareRational(o1: NRational, o2: NRational): Int {
        val (n1, d1) = o1.value
        val (n2, d2) = o2.value
        n1.compareTo(n2).let {
            if (it != 0) return it
        }
        return d1.compareTo(d2)
    }


    override fun compare(o1: Node, o2: Node): Int {
        (nodeTypeOrdinal(o1) - nodeTypeOrdinal(o2)).let {
            if (it != 0) return it
        }

        if(o1 is NodeChilded && o2 is NodeChilded){
            o1.symbol.compareTo(o2.symbol).let {
                if (it != 0) return it
            }
        }

        if (o1 is NRational && o2 is NRational) {
            return compareRational(o1, o2)
        }
        if (o1 is NSymbol && o2 is NSymbol) {
            return o1.symbol.compareTo(o2.symbol)
        }

        if (o1 is Node1 && o2 is Node1) {
            return compare(o1.child, o2.child)
        }

        if (o1 is Node2 && o2 is Node2) {
            compare(o1.first, o2.first).let {
                if (it != 0) return it
            }
            return compare(o1.second, o2.second)
        }

        if (o1 is Node3 && o2 is Node3) {
            compare(o1.first, o2.first).let {
                if (it != 0) return it
            }
            compare(o1.second, o2.second).let {
                if (it != 0) return it
            }
            return compare(o1.third, o2.third)
        }

        if (o1 is NodeN && o2 is NodeN) {
            val c1 = o1.children
            val c2 = o2.children
            c1.size.compareTo(c2.size).let {
                if (it != 0) return it
            }

            val n = c1.size
            for (i in 0 until n) {
                compare(c1[i], c2[i]).let {
                    if (it != 0) return it
                }
            }
            return 0
        }

        throw IllegalArgumentException("Cannot compare $o1 and $o2")
    }
}


interface NodeComplexity {
    fun complexity(node: Node, ctx: EContext): Int
}


object BasicComplexity : NodeComplexity {

    override fun complexity(node: Node, ctx: EContext): Int {
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