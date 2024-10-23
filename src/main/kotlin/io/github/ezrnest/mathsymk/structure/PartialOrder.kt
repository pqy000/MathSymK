package io.github.ezrnest.mathsymk.structure

import io.github.ezrnest.mathsymk.discrete.MutableGraph
import io.github.ezrnest.mathsymk.discrete.MutableGraph.Companion.invoke

@FunctionalInterface
fun interface PartialOrder<in T> : EqualPredicate<T> {
    fun compare(o1: T, o2: T): Result


    enum class Result {
        LESS, EQUAL, GREATER, INCOMPARABLE;

        operator fun not(): Result {
            return when (this) {
                LESS -> GREATER
                GREATER -> LESS
                else -> this
            }
        }

        companion object {
            fun ofInt(i: Int): Result {
                return when {
                    i < 0 -> LESS
                    i > 0 -> GREATER
                    else -> EQUAL
                }
            }
        }
    }


    fun greater(o1: T, o2: T): Boolean {
        return compare(o1, o2) == Result.GREATER
    }

    fun less(o1: T, o2: T): Boolean {
        return compare(o1, o2) == Result.LESS
    }

    override fun isEqual(x: T, y: T): Boolean {
        return compare(x, y) == Result.EQUAL
    }

    fun greaterEqual(o1: T, o2: T): Boolean {
        val r = compare(o1, o2)
        return r == Result.GREATER || r == Result.EQUAL
    }

    fun lessEqual(o1: T, o2: T): Boolean {
        val r = compare(o1, o2)
        return r == Result.LESS || r == Result.EQUAL
    }


    fun reverse(): PartialOrder<T> {
        return PartialOrder { o1, o2 -> compare(o2, o1) }
    }


    companion object {
        fun <T> buildDAG(elements: List<T>, partialOrder: PartialOrder<T>): MutableGraph<T> {
            val graph = MutableGraph<T>()

            // Add all elements as vertices
            val vertices = elements.map { graph.addVertex(it) }

            // Add edges based on the partial order
            for (i in elements.indices) {
                for (j in elements.indices) {
                    if (i == j) continue // Skip self-comparison
                    when (partialOrder.compare(elements[i], elements[j])) {
                        Result.LESS -> {
                            graph.addEdge(vertices[i], vertices[j])
                        }

                        Result.GREATER -> {
                            graph.addEdge(vertices[j], vertices[i])
                        }

                        Result.EQUAL, Result.INCOMPARABLE -> {
                            // Do not add any edge for EQUAL or INCOMPARABLE elements
                        }
                    }
                }
            }
            return graph
        }

        /**
         * Decompose a list of elements into chains based on the partial order.
         */
        fun <T> chainDecomp(elements: List<T>, order: PartialOrder<T>): List<List<T>> {
            val topoOrdered = buildDAG(elements, order).topoSort()!!.map { it.data }
            val chains = mutableListOf<MutableList<T>>()

            for (element in topoOrdered) {
                // Try to find a chain to append the element
                var placed = false
                for (chain in chains) {
                    val last = chain.last()
                    when (order.compare(last, element)) {
                        Result.LESS, Result.EQUAL -> {
                            chain.add(element)
                            placed = true
                            break
                        }

                        else -> { /* Do nothing */
                        }
                    }
                }
                // If not placed in any existing chain, start a new chain
                if (!placed) {
                    chains.add(mutableListOf(element))
                }
            }
            return chains
        }



        /**
         * Returns a partial order defined by the inclusion relation: `x <= y` if `x` is a subset of `y`.
         */
        fun <T, S : Set<T>> inclusionOrder(): PartialOrder<S> {
            return PartialOrder { o1, o2 ->
                if (o1.size < o2.size) {
                    if (o2.containsAll(o1)) Result.LESS else Result.INCOMPARABLE
                } else if (o1.size > o2.size) {
                    if (o1.containsAll(o2)) Result.GREATER else Result.INCOMPARABLE
                } else {
                    if (o1 == o2) Result.EQUAL else Result.INCOMPARABLE
                }
            }
        }

        /**
         * Returns a partial order defined by the prefix relation: `x <= y` if `x` is contained in `y`.
         */
        fun stringInclusionOrder(): PartialOrder<CharSequence> {
            return PartialOrder { o1, o2 ->
                // first compare the lengths to speed up the process
                if (o1.length < o2.length) {
                    if (o2.contains(o1)) Result.LESS else Result.INCOMPARABLE
                } else if (o1.length > o2.length) {
                    if (o1.contains(o2)) Result.GREATER else Result.INCOMPARABLE
                } else {
                    if (o1 == o2) Result.EQUAL else Result.INCOMPARABLE
                }
            }
        }

        /**
         * Returns a partial order defined by the prefix relation: `x <= y` if `x` is a prefix of `y`.
         */
        fun stringPrefixOrder(): PartialOrder<CharSequence> {
            return PartialOrder { o1, o2 ->
                // first compare the lengths to speed up the process
                if (o1.length < o2.length) {
                    if (o2.startsWith(o1)) Result.LESS else Result.INCOMPARABLE
                } else if (o1.length > o2.length) {
                    if (o1.startsWith(o2)) Result.GREATER else Result.INCOMPARABLE
                } else {
                    if (o1 == o2) Result.EQUAL else Result.INCOMPARABLE
                }
            }
        }

        fun <T> totalOrder(comparator: Comparator<T>): PartialOrder<T> {
            return PartialOrder { o1, o2 -> Result.ofInt(comparator.compare(o1, o2)) }
        }

        fun <T> totalOrder(): PartialOrder<T> where T : Comparable<T> {
            return PartialOrder { o1, o2 -> Result.ofInt(o1.compareTo(o2)) }
        }

        /**
         * Returns a partial order for pairs of ordered elements:
         * ```
         * (x1, y1) <= (x2, y2)   if   x1 <= x2 && y1 <= y2
         * ```
         */
        fun <T> latticeOrder2(comp : Comparator<T>) : PartialOrder<Pair<T, T>> {
            return PartialOrder { o1, o2 ->
                val c1 = comp.compare(o1.first, o2.first)
                val c2 = comp.compare(o1.second, o2.second)
                when {
                    c1 < 0 && c2 < 0 -> Result.LESS
                    c1 > 0 && c2 > 0 -> Result.GREATER
                    c1 == 0 && c2 == 0 -> Result.EQUAL
                    else -> Result.INCOMPARABLE
                }
            }
        }

        /**
         * Returns a partial order for lists of ordered elements:
         * ```
         * [x1, x2, ..., xn] <= [y1, y2, ..., ym]   if   x1 <= y1, x2 <= y2, ..., xk <= yk, for some k = min(n, m)
         * ```
         */
        fun <T> latticeOrderN(comp : Comparator<T>): PartialOrder<List<T>> {
            return PartialOrder { o1, o2 ->
                val n = minOf(o1.size, o2.size)
                for(i in 0 until n){
                    val c = comp.compare(o1[i], o2[i])
                    if(c < 0){
                        return@PartialOrder Result.LESS
                    }else if(c > 0){
                        return@PartialOrder Result.GREATER
                    }
                }
                if(o1.size < o2.size){
                    Result.LESS
                }else if(o1.size > o2.size){
                    Result.GREATER
                }else{
                    Result.EQUAL
                }
            }
        }
    }
}

/**
 * Returns the minimal elements, namely `x` such that there is no `y` such that `y < x`.
 */
fun <T> Iterable<T>.minimalBy(order: PartialOrder<T>): List<T> {
    return filter { element -> none { order.less(it, element) } }
}

/**
 * Returns the maximal elements, namely `x` such that there is no `y` such that `y > x`.
 */
fun <T> Iterable<T>.maximalBy(order: PartialOrder<T>): List<T> {
    return filter { element -> none { order.greater(it, element) } }
}