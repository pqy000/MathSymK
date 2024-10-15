package io.github.ezrnest.structure

import io.github.ezrnest.discrete.MutableGraph
import io.github.ezrnest.discrete.MutableGraph.Companion.invoke

@FunctionalInterface
fun interface PartialOrder<T> : EqualPredicate<T> {
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

    override fun isEqual(o1: T, o2: T): Boolean {
        return compare(o1, o2) == Result.EQUAL
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


        fun <T> findMinimalElements(elements: List<T>, partialOrder: PartialOrder<T>): List<T> {
            val minimal = mutableListOf<T>()
            for (element in elements) {
                if (elements.none { partialOrder.less(it, element) }) {
                    minimal.add(element)
                }
            }
            return minimal
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
    }
}