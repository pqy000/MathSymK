package io.github.ezrnest.symbolic
// created at 2024/10/05
typealias NodeOrder = Comparator<Node>


object DefaultNodeOrder : NodeOrder {

    fun nodeTypeOrdinal(node: Node): Int {
        return when (node) {
            is NRational -> 0
            is NSymbol -> 10
            is Node1 -> 100
            is Node2 -> 110
            is Node3 -> 120
            is NodeN -> 1000
            else -> Int.MAX_VALUE
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
        o1.name.compareTo(o2.name).let {
            if (it != 0) return it
        }
        (nodeTypeOrdinal(o1) - nodeTypeOrdinal(o2)).let {
            if (it != 0) return it
        }

        if (o1 is NRational && o2 is NRational) {
            return compareRational(o1, o2)
        }
        if (o1 is NSymbol && o2 is NSymbol) {
            return o1.ch.compareTo(o2.ch)
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
