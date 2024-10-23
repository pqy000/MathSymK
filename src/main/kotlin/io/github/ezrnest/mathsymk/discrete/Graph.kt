package io.github.ezrnest.mathsymk.discrete

import io.github.ezrnest.mathsymk.discrete.Graph.Edge
import io.github.ezrnest.mathsymk.discrete.Graph.Vertex
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


interface Graph<T> {
    interface Vertex<V> {
        val data: V
    }

    class Edge<V>(val source: Vertex<V>, val target: Vertex<V>)

    /**
     * Returns all vertices in the graph.
     */
    val vertices: Set<Vertex<T>>

    /**
     * Returns the number of vertices in the graph.
     */
    val size: Int
        get() = vertices.size

    /**
     * Returns all edges in the graph as a set of pairs (source, target).
     */
    val edges: Set<Edge<T>>

    /**
     * Returns the outgoing neighbors of the given vertex.
     * @param vertex The vertex for which to get outgoing neighbors.
     * @return A set of vertices that are directly reachable from the given vertex.
     */
    fun neighbors(vertex: Vertex<T>): Set<Vertex<T>>

    /**
     * Returns the incoming neighbors of the given vertex.
     * @param vertex The vertex for which to get incoming neighbors.
     * @return A set of vertices that have edges pointing to the given vertex.
     */
    fun incomingNeighbors(vertex: Vertex<T>): Set<Vertex<T>>

    /**
     * Returns the in-degree of the given vertex.
     * @param vertex The vertex for which to get the in-degree.
     * @return The number of incoming edges to the vertex.
     */
    fun inDegree(vertex: Vertex<T>): Int {
        return incomingNeighbors(vertex).size
    }

    /**
     * Returns the out-degree of the given vertex.
     * @param vertex The vertex for which to get the out-degree.
     * @return The number of outgoing edges from the vertex.
     */
    fun outDegree(vertex: Vertex<T>): Int {
        return neighbors(vertex).size
    }

    /**
     * Returns all vertices with no incoming edges.
     * @return A set of source vertices.
     */
    fun sources(): Set<Vertex<T>> {
        return vertices.filterTo(hashSetOf()) { inDegree(it) == 0 }
    }

    /**
     * Returns all vertices with no outgoing edges.
     * @return A set of sink vertices.
     */
    fun sinks(): Set<Vertex<T>> {
        return vertices.filterTo(hashSetOf()) { outDegree(it) == 0 }
    }

    fun traverseWith(
        start: Vertex<T>, frontier: InOutCollection<Vertex<T>>, visited: MutableSet<Vertex<T>>,
        f: (Vertex<T>) -> Boolean
    ): Boolean {
        frontier.add(start)
        visited.add(start)
        while (frontier.isNotEmpty()) {
            val current = frontier.remove()
            if (f(current)) return true
            for (neighbor in neighbors(current)) {
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    frontier.add(neighbor)
                }
            }
        }
        return false
    }


    fun dfsFromUntil(
        v: Vertex<T>, visited: MutableSet<Vertex<T>> = mutableSetOf(), f: (Vertex<T>) -> Boolean
    ): Boolean {
        return traverseWith(v, InOutCollection.stack(), visited, f)
    }


    fun dfsUntil(f: (Vertex<T>) -> Boolean): Boolean {
        val visited = HashSet<Vertex<T>>(this.size)
        val stack = InOutCollection.stack<Vertex<T>>()
        for (v in this.vertices) {
            if (v in visited) {
                continue
            }
            if (traverseWith(v, stack, visited, f)) return true
        }
        return false
    }

    fun dfs(): List<Vertex<T>> {
        val result = ArrayList<Vertex<T>>(this.vertices.size)
        dfsUntil { result.add(it); false }
        return result
    }

    fun dfs(f: (Vertex<T>) -> Unit) {
        dfsUntil { f(it); false }
    }

    fun dfsFrom(v: Vertex<T>, visited: MutableSet<Vertex<T>> = mutableSetOf()): List<Vertex<T>> {
        val result = ArrayList<Vertex<T>>(this.vertices.size)
        dfsFromUntil(v, visited) { result.add(it); false }
        return result
    }

    fun bfsFromUntil(
        v: Vertex<T>, visited: MutableSet<Vertex<T>> = mutableSetOf(), f: (Vertex<T>) -> Boolean
    ): Boolean {
        return traverseWith(v, InOutCollection.queue(), visited, f)
    }

    fun bfsUntil(f: (Vertex<T>) -> Boolean): Boolean {
        val visited = HashSet<Vertex<T>>(this.size)
        val queue = InOutCollection.queue<Vertex<T>>()
        for (v in this.vertices) {
            if (v in visited) {
                continue
            }
            if (traverseWith(v, queue, visited, f)) return true
        }
        return false
    }

    fun bfs(): List<Vertex<T>> {
        val result = ArrayList<Vertex<T>>(this.vertices.size)
        bfsUntil { result.add(it); false }
        return result
    }

    fun astar(start: Vertex<T>, priority: (Vertex<T>) -> Int, f: (Vertex<T>) -> Boolean): Boolean {
        val visited = HashSet<Vertex<T>>(this.size)
        val queue = InOutCollection.priority<Vertex<T>>(comparator = compareBy { priority(it) })
        for (v in this.vertices) {
            if (v in visited) {
                continue
            }
            if (traverseWith(v, queue, visited, f)) return true
        }
        return false
    }


    /**
     * Checks if there is a path from vertex 'from' to vertex 'to'.
     * @param from The starting vertex.
     * @param to The target vertex.
     * @return True if a path exists, false otherwise.
     */
    fun hasPath(from: Vertex<T>, to: Vertex<T>): Boolean {
        if (!this.vertices.contains(from) || !this.vertices.contains(to)) {
            return false
        }
        return dfsFromUntil(from) { it == to }
    }


    /**
     * Returns a topological ordering of the vertices in the graph if it is a DAG, null otherwise.
     *
     * @return A list representing the topological order of the vertices or null if the graph is not a DAG.
     */
    fun topoSortWith(frontier: InOutCollection<Vertex<T>>): List<Vertex<T>>? {
        val inDegreeMap = HashMap<Vertex<T>, Int>(this.size)
        val topologicalOrder = ArrayList<Vertex<T>>(this.size)

        // Calculate in-degrees for all vertices
        for (vertex in vertices) {
            inDegreeMap[vertex] = inDegree(vertex)
        }

        for (vertex in vertices) {
            if (inDegreeMap[vertex] == 0) {
                frontier.add(vertex)
            }
        }

        // Process the graph
        while (frontier.isNotEmpty()) {
            val current = frontier.remove()
            topologicalOrder.add(current)

            // For each neighbor, reduce its in-degree
            for (neighbor in neighbors(current)) {
                inDegreeMap[neighbor] = inDegreeMap[neighbor]!! - 1
                // If in-degree becomes 0, add it to the queue
                if (inDegreeMap[neighbor] == 0) {
                    frontier.add(neighbor)
                }
            }
        }

        // If topologicalOrder does not contain all vertices, a cycle exists
        if (topologicalOrder.size != vertices.size) {
            return null
        }

        return topologicalOrder
    }

    fun topoSort(): List<Vertex<T>>? {
        return topoSortWith(InOutCollection.stack())
    }


    companion object{



    }

}


interface InOutCollection<T> {
    val size: Int
    fun isNotEmpty(): Boolean = size > 0

    fun add(e: T)

    fun remove(): T

    companion object {
        fun <T> stack(initialCapacity: Int = 10): InOutCollection<T> = object : InOutCollection<T> {

            val list = ArrayList<T>(initialCapacity)

            override val size: Int
                get() = list.size

            override fun add(e: T) {
                list.add(e)
            }

            override fun remove(): T {
                return list.removeLast()
            }
        }

        fun <T : Any> queue(initialCapacity: Int = 10): InOutCollection<T> = object : InOutCollection<T> {

            val list = ArrayDeque<T>(initialCapacity)

            override val size: Int
                get() = list.size

            override fun add(e: T) {
                list.addLast(e)
            }

            override fun remove(): T {
                return list.removeFirst()
            }
        }

        fun <T : Any> priority(initialCapacity: Int = 10, comparator: Comparator<T>): InOutCollection<T> =
            object : InOutCollection<T> {
                val list = PriorityQueue<T>(initialCapacity, comparator)
                override val size: Int
                    get() = list.size

                override fun add(e: T) {
                    list.add(e)
                }

                override fun remove(): T {
                    return list.remove()
                }
            }
    }
}


/**
 * Represents a mutable Directed Acyclic Graph (DAG).
 * @param T The type of elements in the graph.
 */
interface MutableGraph<T> : Graph<T> {
    /**
     * Adds a new vertex to the DAG with the given data.
     */
    fun addVertex(data: T): Vertex<T>

    /**
     * Removes a vertex from the DAG, along with all edges connected to it.
     *
     * @return True if the vertex was removed, false if it did not exist.
     */
    fun removeVertex(v: Vertex<T>): Boolean

    fun addEdge(source: Vertex<T>, target: Vertex<T>): Boolean

    fun removeEdge(source: Vertex<T>, target: Vertex<T>): Boolean

    companion object{
        operator fun <T> invoke(): MutableGraph<T> = SimpleGraph()
    }
}


class SimpleGraph<T> : MutableGraph<T> {
    class SimpleVertex<T>(override val data: T, val neighbours: MutableSet<Vertex<T>>) : Vertex<T>

    @Suppress("NOTHING_TO_INLINE")
    @OptIn(ExperimentalContracts::class)
    private inline fun checkV(v: Vertex<T>) {
        contract {
            returns() implies (v is SimpleVertex<T>)
        }
        require(v in vertexSet) { "Vertex not in graph" }
    }

    private val vertexSet = mutableSetOf<SimpleVertex<T>>()

    override val vertices: Set<SimpleVertex<T>>
        get() = vertexSet

    override val edges: Set<Edge<T>>
        get() = vertexSet.flatMap { v -> v.neighbours.map { Edge(v, it) } }.toSet()

    override fun neighbors(vertex: Vertex<T>): Set<Vertex<T>> {
        checkV(vertex)
        return vertex.neighbours
    }

    override fun incomingNeighbors(vertex: Vertex<T>): Set<Vertex<T>> {
        checkV(vertex)
        return vertexSet.filterTo(hashSetOf()) { it.neighbours.contains(vertex) }
    }

    override fun addEdge(source: Vertex<T>, target: Vertex<T>): Boolean {
        checkV(source)
        checkV(target)
        val neighbors = source.neighbours
        return neighbors.add(target)
    }

    override fun removeEdge(source: Vertex<T>, target: Vertex<T>): Boolean {
        checkV(source)
        checkV(target)
        val neighbors = source.neighbours
        return neighbors.remove(target)
    }

    override fun addVertex(data: T): Vertex<T> {
        val v = SimpleVertex(data, mutableSetOf())
        vertexSet.add(v)
        return v
    }

    override fun removeVertex(v: Vertex<T>): Boolean {
        checkV(v)
        val removed = vertexSet.remove(v)
        if (!removed) return false
        for (vertex in vertexSet) {
            vertex.neighbours.remove(v)
        }
        return true
    }
}