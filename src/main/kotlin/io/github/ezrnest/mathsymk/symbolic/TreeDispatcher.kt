package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.util.WithInt
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class TreeDispatcher<T>() {
    private val dispatchRoot: DispatchNode<T> = DispatchNode()

    private val _elements: MutableList<T> = mutableListOf()

    val elements: List<T> get() = _elements

    private data class DispatchNode<T>(
        var wildcard: DispatchResult<T>? = null,
        var fixed: MutableMap<NodeSig, DispatchResult<T>>? = null,
        var variable: MutableMap<NodeSig, DispatchResult<T>>? = null,
    )

    private data class DispatchResult<T>(
        var result: MutableList<T>? = null,
        var next: SortedMap<Int, DispatchNode<T>>? = null
    ) {

        fun initNext(): SortedMap<Int, DispatchNode<T>> {
            return next ?: sortedMapOf<Int, DispatchNode<T>>().also { next = it }
        }
    }


    fun dispatch(root: Node, f: (T) -> Unit) {
        dispatchUntil(root) {
            f(it)
            false
        }
    }

    private fun applyDispatchResult(
        res: DispatchResult<T>?, f: (T) -> Boolean,
        tempList: MutableList<WithInt<DispatchNode<T>>>, level: Int
    ): T? {
        if (res == null) return null
        res.result?.forEach {
            if (f(it)) return it
        }
        res.next?.forEach { (key, value) ->
            tempList.add(WithInt(level + key, value))
        }
        return null
    }


    private suspend fun SequenceScope<T>.applyDispatchSeq(
        res: DispatchResult<T>?, tempList: ArrayList<WithInt<DispatchNode<T>>>, level: Int
    ) {
        if (res == null) return
        res.result?.forEach {
            yield(it)
        }
        res.next?.forEach { (key, value) ->
            tempList.add(WithInt(level + key, value))
        }
    }

    private object IndexedValueComparator : Comparator<WithInt<*>> {
        override fun compare(o1: WithInt<*>, o2: WithInt<*>): Int {
            return -(o1.v - o2.v)
        }
    }

    /**
     * Dispatches the given node to the registered matchers,
     * and applies the given function to the matched data until the function returns `true`.
     */
    fun dispatchUntil(root: Node, f: (T) -> Boolean): T? {
        return dispatchSeq(root).firstOrNull(f)
    }


    /**
     * Dispatches the given node to the registered matchers,
     * and applies the given function to the matched data until the function returns `true`.
     */
    fun dispatchSeq(root: Node): Sequence<T> = sequence {
        val stack = mutableListOf<Iterator<Node>>()
        val dispatchStack = PriorityQueue<WithInt<DispatchNode<T>>>(IndexedValueComparator)
        dispatchStack.add(WithInt(0, dispatchRoot))
        var node = root // the current node that is successfully matched
        var level = 0
        val tempMap = LinkedHashMap<NodeSig, DispatchResult<T>>(4)
        val tempList = ArrayList<WithInt<DispatchNode<T>>>(4)
        while (dispatchStack.isNotEmpty()) {
            // now we are at the same level: level==nextLevel
//            println("Dealing with level=$level")
//            println("Matching node: ${node.signature}")
            while (dispatchStack.isNotEmpty() && dispatchStack.peek().v == level) {
                val (_, p) = dispatchStack.poll()
                val res = p.wildcard
                applyDispatchSeq(res, tempList, level)
                applyDispatchSeq(p.fixed?.get(node.signature), tempList, level)
                p.variable?.let { va ->
                    applyDispatchSeq(va[node.signature], tempList, level)
                    tempMap.putAll(va)
                }
            }
            // retain the variable nodes
            if (tempMap.isNotEmpty()) {
                val varNode = DispatchNode(variable = tempMap.toMutableMap())
                dispatchStack.add(WithInt(level, varNode))
                tempMap.clear()
            }
            if (tempList.isNotEmpty()) {
                dispatchStack.addAll(tempList)
                tempList.clear()
            }

            FindDispatch@
            while (dispatchStack.isNotEmpty()) {
                // let go to the next level
                val nextLevel = dispatchStack.peek().v
                if (level >= nextLevel) {
                    while (level > nextLevel) {
                        stack.removeLast()
                        level--
                    }
                    // we are at the same level, go to the sibling
                    val iter = stack.last()
                    if (iter.hasNext()) {
                        node = iter.next()
                    } else {
                        // no more sibling, go up
                        stack.removeLast()
                        level--
                    }
                } else {
                    while (level < nextLevel) {
                        // go down the tree
                        if (node !is NodeChilded) break // this dispatch requires a deeper level but the current node is not a childed node
                        val iter = node.children.iterator()
                        node = iter.next()
                        stack.add(iter)
                        level++
                    }

                }
                if (level < nextLevel) {
                    while (dispatchStack.isNotEmpty() && dispatchStack.peek().v > level) {
                        dispatchStack.poll()
                    }
                    continue@FindDispatch
                }
                break@FindDispatch
            }
        }
    }


//    private fun buildChild(nodeRes: DispatchResult<T>, child : NodeMatcher<Node>) : IndexedValue<DispatchResult<T>>{
//        if(child)
//    }

    private fun buildChildrenTo(
        nodeRes: DispatchResult<T>, children: List<NodeMatcherT<Node>>, variable: Boolean = false
    ): WithInt<DispatchResult<T>> {
//        if(children.size == 1) return buildChild(nodeRes, children[0])
        var prevRes = nodeRes
        var prevDepth = 0
        for (m in children) {
            val prevNexts = prevRes.initNext()
            val relLevel = (1 - prevDepth)
            val curNode = prevNexts.getOrPut(relLevel) { DispatchNode() }
            val w = buildTo(m, curNode, variable)
            prevRes = w.item
            prevDepth = w.v + 1
        }
        return WithInt(prevDepth, prevRes)
    }

    private fun buildTo(
        matcher: NodeMatcherT<*>, node: DispatchNode<T>, variable: Boolean = false
    ): WithInt<DispatchResult<T>> {
        if (matcher is TransparentNodeMatcher) {
            return buildTo(matcher.matcher, node, variable)
        }
        val nodeRes: DispatchResult<T> = if (matcher is NodeMatcherFixSig) {
            val map = if (variable) {
                node.variable ?: mutableMapOf<NodeSig, DispatchResult<T>>().also { node.variable = it }
            } else {
                node.fixed ?: mutableMapOf<NodeSig, DispatchResult<T>>().also { node.fixed = it }
            }
            map.getOrPut(matcher.nodeSig) { DispatchResult() }
        } else {
            node.wildcard ?: DispatchResult<T>().also { node.wildcard = it }
        }
        if (matcher is NMatcherChildedOrdered) {
            return buildChildrenTo(nodeRes, matcher.children)
        }
        if (matcher is NodeMatcherNPO) {
            return buildChildrenTo(nodeRes, matcher.childrenChains[0], true)
        }

        return WithInt(0, nodeRes) // do not go down


    }

    fun register(matcher: NodeMatcherT<*>, data: T) {
        val (_, d) = buildTo(matcher, dispatchRoot)
        val res = d.result ?: mutableListOf<T>().also { d.result = it }
        res.add(data)
        _elements.add(data)
    }

//    fun dispatchLeveled(root : Node, level)

    fun dispatchToList(root: Node): List<T> {
        val res = mutableListOf<T>()
        dispatch(root) {
            res.add(it)
        }
        return res
    }


    private fun printNext(next: SortedMap<Int, DispatchNode<T>>?, level: Int) {
        if (next == null) return
        val indent = "  ".repeat(level)
        next.forEach { (key, value) ->
//            val newLevel = level + key
//            println("  ".repeat(newLevel))
            println("$indent> $key")
            printTree0(value, level + 1)
        }
    }

    private fun printTree0(node: DispatchNode<T>, level: Int) {
        val indent = "  ".repeat(level)
        node.wildcard?.let {
            println(indent + "Wildcard: ${it.result}")
            printNext(it.next, level)
        }
        node.fixed?.forEach { (key, value) ->
            println(indent + "Fixed: $key -> ${value.result}")
            printNext(value.next, level)
        }
        node.variable?.forEach { (key, value) ->
            println(indent + "Variable: $key -> ${value.result}")
            printNext(value.next, level)
        }
    }

    fun printDispatchTree() {
        printTree0(dispatchRoot, 0)
    }
}
