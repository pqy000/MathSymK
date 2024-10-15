package io.github.ezrnest.symbolic

import io.github.ezrnest.structure.PartialOrder
import io.github.ezrnest.symbolic.TreeDispatcher.DispatchNode
import java.util.PriorityQueue
import java.util.SortedMap

//created at 2024/10/10

interface MatchContext {
    val exprContext: ExprContext

    val refMap: Map<String, Node>
}

interface MutableMatchContext : MatchContext {
    override val refMap: MutableMap<String, Node>

    fun copy(): MutableMatchContext

    companion object {

        operator fun invoke(context: ExprContext): MutableMatchContext {
            return MutableMatchContextImpl(context)
        }
    }
}

internal data class MutableMatchContextImpl(
    override val exprContext: ExprContext = TestExprContext,
    override val refMap: MutableMap<String, Node> = mutableMapOf()
) : MutableMatchContext {

    override fun copy(): MutableMatchContext {
        return MutableMatchContextImpl(exprContext, refMap.toMutableMap())
    }
}


sealed interface NodeMatcher<out T : Node> {

    /**
     * Tries to match the given node with the pattern represented by this matcher, modifying the match context if the match is successful.
     */
    fun matches(node: Node, matchContext: MutableMatchContext): T?


    val refNames: Set<String>
}


sealed interface NodeMatcherForSpec<T : Node> : NodeMatcher<T> {
    /**
     * The signature of the node that is matched by this matcher.
     */
    val nodeSig: NodeSig
}

interface NodeMatcherChildedOrdered<T : Node> : NodeMatcherForSpec<T> {
    val children: List<NodeMatcher<Node>>
}

interface TransparentNodeMatcher<T : Node> : NodeMatcher<T> {
    val matcher: NodeMatcher<T>

}

interface LeafMatcher<T : Node> : NodeMatcher<T> {

}

abstract class AbstractNodeMatcherForSpec<T : Node>(final override val nodeSig: NodeSig) : NodeMatcherForSpec<T> {


}

class NodeMatcher1<C : Node>(val child: NodeMatcher<C>, nodeName: NodeSig) :
    AbstractNodeMatcherForSpec<Node1T<C>>(nodeName), NodeMatcherChildedOrdered<Node1T<C>> {

    override val children: List<NodeMatcher<Node>>
        get() = listOf(child)

    override val refNames: Set<String>
        get() = child.refNames

    override fun matches(node: Node, matchContext: MutableMatchContext): Node1T<C>? {
        if (node !is Node1) return null
        if (!nodeSig.matches(node)) return null
        child.matches(node.child, matchContext) ?: return null
        @Suppress("UNCHECKED_CAST")
        return node as Node1T<C>
    }

    override fun toString(): String {
        return "${nodeSig.name}($child)"
    }
}

class NodeMatcher2Ordered<C1 : Node, C2 : Node>(
    val child1: NodeMatcher<C1>, val child2: NodeMatcher<C2>, nodeName: NodeSig
) :
    AbstractNodeMatcherForSpec<Node2T<C1, C2>>(nodeName), NodeMatcherChildedOrdered<Node2T<C1, C2>> {
    override val children: List<NodeMatcher<Node>>
        get() = listOf(child1, child2)

    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        child1.refNames + child2.refNames
    }

    override fun matches(node: Node, matchContext: MutableMatchContext): Node2T<C1, C2>? {
        if (node !is Node2) return null
        if (!nodeSig.matches(node)) return null
        child1.matches(node.first, matchContext) ?: return null
        child2.matches(node.second, matchContext) ?: return null
        @Suppress("UNCHECKED_CAST")
        return node as Node2T<C1, C2>
    }

    override fun toString(): String {
        return "${nodeSig.name}($child1, $child2)"
    }
}

class NodeMatcher3Ordered<C1 : Node, C2 : Node, C3 : Node>(
    val child1: NodeMatcher<C1>, val child2: NodeMatcher<C2>, val child3: NodeMatcher<C3>, nodeName: NodeSig
) : AbstractNodeMatcherForSpec<Node3T<C1, C2, C3>>(nodeName),
    NodeMatcherChildedOrdered<Node3T<C1, C2, C3>> {
    override val children: List<NodeMatcher<Node>>
        get() = listOf(child1, child2, child3)
    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        child1.refNames + child2.refNames + child3.refNames
    }

    override fun matches(node: Node, matchContext: MutableMatchContext): Node3T<C1, C2, C3>? {
        if (node !is Node3) return null
        if (!nodeSig.matches(node)) return null
        child1.matches(node.first, matchContext) ?: return null
        child2.matches(node.second, matchContext) ?: return null
        child3.matches(node.third, matchContext) ?: return null
        @Suppress("UNCHECKED_CAST")
        return node as Node3T<C1, C2, C3>
    }

    override fun toString(): String {
        return "${nodeSig.name}($child1, $child2, $child3)"
    }
}

class NodeMatcherNOrdered(override val children: List<NodeMatcher<Node>>, nodeName: NodeSig) :
    AbstractNodeMatcherForSpec<NodeN>(nodeName), NodeMatcherChildedOrdered<NodeN> {
    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        children.flatMap { it.refNames }.toSet()
    }

    override fun matches(node: Node, matchContext: MutableMatchContext): NodeN? {
        if (node !is NodeN) return null
        if (!nodeSig.matches(node)) return null
        if (node.children.size != children.size) return null
        for (i in children.indices) {
            if (children[i].matches(node.children[i], matchContext) == null) return null
        }
        return node
    }

    override fun toString(): String {
        return "${nodeSig.name}(${children.joinToString(", ")})"
    }
}

class NodeMatcherNPartialOrder(children: List<NodeMatcher<Node>>, nodeName: NodeSig) :
    AbstractNodeMatcherForSpec<NodeN>(nodeName) {

    private val children : List<NodeMatcher<Node>> = TODO()

    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        children.flatMap { it.refNames }.toSet()
    }

    private val link : IntArray = TODO()


    override fun matches(node: Node, matchContext: MutableMatchContext): NodeN? {
        TODO("Not yet implemented")
    }

    object Preference : Comparator<NodeMatcher<*>>{
        override fun compare(o1: NodeMatcher<*>, o2: NodeMatcher<*>): Int {
            TODO()
        }
    }

    companion object{


    }
}


object AnyMatcher : LeafMatcher<Node> {
    override fun matches(node: Node, matchContext: MutableMatchContext): Node? {
        return node
    }

    override val refNames: Set<String> get() = emptySet()

    override fun toString(): String {
        return "Any"
    }
}

object AnyRationalMatcher : LeafMatcher<NRational>, NodeMatcherForSpec<NRational> {
    override fun matches(node: Node, matchContext: MutableMatchContext): NRational? {
        return node as? NRational
    }

    override val refNames: Set<String> get() = emptySet()

    override val nodeSig: NodeSig get() = NodeSig.RATIONAL

    override fun toString(): String {
        return "AnyRational"
    }
}

object AnySymbolMatcher : LeafMatcher<NSymbol>, NodeMatcherForSpec<NSymbol> {
    override fun matches(node: Node, matchContext: MutableMatchContext): NSymbol? {
        return node as? NSymbol
    }

    override val refNames: Set<String> get() = emptySet()

    override val nodeSig: NodeSig get() = NodeSig.SYMBOL

    override fun toString(): String {
        return "AnySymbol"
    }
}


class RefMatcher(val name: String) : LeafMatcher<Node> {
    override fun matches(node: Node, matchContext: MutableMatchContext): Node? {
        val ref = matchContext.refMap[name]
        if (ref == null) {
            matchContext.refMap[name] = node
            return node
        }
        return if (ref == node) node else null
    }

    override val refNames: Set<String> get() = setOf(name)

    override fun toString(): String {
        return "Ref($name)"
    }
}

class NamedMatcher<T : Node>(override val matcher: NodeMatcher<T>, val name: String) : TransparentNodeMatcher<T> {
    override val refNames: Set<String>
        get() = matcher.refNames + name

    override fun matches(node: Node, matchContext: MutableMatchContext): T? {
        val ref = matchContext.refMap[name]
        if (ref != null && ref != node) return null
        val res = matcher.matches(node, matchContext) ?: return null
        if (ref == null) {
            matchContext.refMap[name] = node
        }
        return res
    }

    override fun toString(): String {
        return "Named(name=$name, $matcher)"
    }
}

class FixedSymbolMatcher(val symbol: NSymbol) : LeafMatcher<NSymbol>, NodeMatcherForSpec<NSymbol> {
    override fun matches(node: Node, matchContext: MutableMatchContext): NSymbol? {
        return if (node is NSymbol && node == symbol) symbol else null
    }

    override val refNames: Set<String> get() = emptySet()

    override val nodeSig: NodeSig get() = NodeSig.SYMBOL

    override fun toString(): String {
        return "$symbol"
    }
}

class LeafMatcherForSpec<T : Node> private constructor(signature: NodeSig) :
    AbstractNodeMatcherForSpec<T>(signature), LeafMatcher<T> {

    override val refNames: Set<String>
        get() = emptySet()

    override fun matches(node: Node, matchContext: MutableMatchContext): T? {
        @Suppress("UNCHECKED_CAST")
        return if (nodeSig.matches(node)) node as T else null
    }

    companion object {
        fun forLeaf(name: String): LeafMatcherForSpec<LeafNode> {
            return LeafMatcherForSpec(NodeSig(name, NodeSig.NType.Leaf))
        }

        fun forNode1(name: String): LeafMatcherForSpec<Node1> {
            return LeafMatcherForSpec(NodeSig(name, NodeSig.NType.Node1))
        }

        fun forNode2(name: String): LeafMatcherForSpec<Node2> {
            return LeafMatcherForSpec(NodeSig(name, NodeSig.NType.Node2))
        }

        fun forNode3(name: String): LeafMatcherForSpec<Node3> {
            return LeafMatcherForSpec(NodeSig(name, NodeSig.NType.Node3))
        }

        fun forNodeN(name: String): LeafMatcherForSpec<NodeN> {
            return LeafMatcherForSpec(NodeSig(name, NodeSig.NType.NodeN))
        }

        operator fun invoke(name: String, type: NodeSig.NType): LeafMatcherForSpec<Node> {
            return LeafMatcherForSpec(NodeSig(name, type))
        }

        operator fun invoke(signature: NodeSig): LeafMatcherForSpec<Node> {
            return LeafMatcherForSpec(signature)
        }
    }
}

object MatcherPartialOrder : PartialOrder<NodeMatcher<*>> {


    fun compareChildren(o1: NodeMatcher<*>, o2: NodeMatcher<*>): PartialOrder.Result {
        if (o1 is NodeMatcher1<*> && o2 is NodeMatcher1<*>) {
            return compare(o1.child, o2.child)
        }


        if (o1 is NodeMatcherChildedOrdered && o2 is NodeMatcherChildedOrdered) {
            val c = o1.children.size - o2.children.size
            if (c != 0) return PartialOrder.Result.ofInt(c)
            for (i in o1.children.indices) {
                val c = compare(o1.children[i], o2.children[i])
                if (c != PartialOrder.Result.EQUAL) return c
            }
            return PartialOrder.Result.EQUAL
        }
        TODO()
    }


    override fun compare(
        o1: NodeMatcher<*>, o2: NodeMatcher<*>
    ): PartialOrder.Result {
        if (o1 is NodeMatcherForSpec<*> && o2 is NodeMatcherForSpec<*>) {
            val c = o1.nodeSig.compareTo(o2.nodeSig)
            if (c != 0) return PartialOrder.Result.ofInt(c)
        }
        return compareChildren(o1, o2)
    }
}

object MatcherBuilderScope {

    fun <T : Node, S : Node> pow(base: NodeMatcher<T>, exp: NodeMatcher<S>): NodeMatcher<Node2T<T, S>> {
        return NodeMatcher2Ordered(base, exp, NodeSig.POW)
    }

    fun <T : Node> sin(x: NodeMatcher<T>): NodeMatcher<Node1T<T>> {
        return NodeMatcher1(x, NodeSig.F1_SIN)
    }


    val x: RefMatcher get() = RefMatcher("x")
    val y: RefMatcher get() = RefMatcher("y")
    val z: RefMatcher get() = RefMatcher("z")

    fun symbol(name: String): NodeMatcher<NSymbol> {
        return symbol(NSymbol(name))
    }

    fun symbol(s: NSymbol): NodeMatcher<NSymbol> {
        return FixedSymbolMatcher(s)
    }

    val π: NodeMatcher<NSymbol> get() = symbol(Node.PI)

    val integer: NodeMatcher<NRational> get() = AnyRationalMatcher

    val rational: NodeMatcher<NRational> get() = AnyRationalMatcher
}


class TreeDispatcher<T>() {
    private val dispatchRoot: DispatchNode<T> = DispatchNode()

//    private sealed interface DispatchNode<T> {
//        var signed: MutableMap<NodeSig, DispatchResult<T>>?
//    }

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


    private data class WithLevel<D>(val level: Int, val item: D) : Comparable<WithLevel<D>> {
        override fun compareTo(other: WithLevel<D>): Int {
            return -level.compareTo(other.level) //
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
        dispatchStack: PriorityQueue<WithLevel<DispatchNode<T>>>, level: Int
    ): T? {
        if (res == null) return null
        res.result?.forEach {
            if (f(it)) return it
        }
        res.next?.forEach { (key, value) ->
            dispatchStack.add(WithLevel(level + key, value))
        }
        return null
    }

    /**
     * Dispatches the given node to the registered matchers,
     * and applies the given function to the matched data until the function returns `true`.
     */
    fun dispatchUntil(root: Node, f: (T) -> Boolean): T? {
        val stack = mutableListOf<Iterator<Node>>()
        val dispatchStack = PriorityQueue<WithLevel<DispatchNode<T>>>()
        dispatchStack.add(WithLevel(0, dispatchRoot))
        var node = root // the current node that is successfully matched
        var level = 0
        val tempMap = LinkedHashMap<NodeSig, DispatchResult<T>>(4)
        while (dispatchStack.isNotEmpty()) {
            // now we are at the same level: level==nextLevel
//            println("Dealing with level=$level")
            tempMap.clear()
            while (dispatchStack.isNotEmpty() && dispatchStack.peek().level == level) {
                val (_, p) = dispatchStack.poll()
                applyDispatchResult(p.wildcard, f, dispatchStack, level)?.let { return it }
                applyDispatchResult(p.fixed?.get(node.signature), f, dispatchStack, level)?.let { return it }
                p.variable?.let { va ->
                    applyDispatchResult(va[node.signature], f, dispatchStack, level)?.let { return it }
                    tempMap.putAll(va)
                }
            }
            // retain the variable nodes
            if (tempMap.isNotEmpty()) {
                val varNode = DispatchNode(variable = tempMap)
                dispatchStack.add(WithLevel(level, varNode))
            }

            FindDispatch@
            while (dispatchStack.isNotEmpty()) {
                // let go to the next level
                val nextLevel = dispatchStack.peek().level
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
                    while (dispatchStack.isNotEmpty() && dispatchStack.peek().level > level) {
                        dispatchStack.poll()
                    }
                    continue@FindDispatch
                }
                break@FindDispatch
            }
        }
        return null
    }


    private fun buildTo(matcher: NodeMatcher<*>, node: DispatchNode<T>): WithLevel<DispatchResult<T>> {
        if (matcher is TransparentNodeMatcher) {
            return buildTo(matcher.matcher, node)
        }
        val nodeRes: DispatchResult<T> = if (matcher is NodeMatcherForSpec) {
            val map = node.fixed ?: mutableMapOf<NodeSig, DispatchResult<T>>().also { node.fixed = it }
            map.getOrPut(matcher.nodeSig) { DispatchResult() }
        } else {
            node.wildcard ?: DispatchResult<T>().also { node.wildcard = it }
        }
        if (matcher !is NodeMatcherChildedOrdered) {
            return WithLevel(0, nodeRes) // do not go down
        }

        var prevRes = nodeRes
        var prevDepth = 0

        for (m in matcher.children) {
            val prevNexts = prevRes.initNext()
            val relLevel = (1 - prevDepth)
            val curNode = prevNexts.getOrPut(relLevel) { DispatchNode() }
            val w = buildTo(m, curNode)
            prevRes = w.item
            prevDepth = w.level + 1
        }
        return WithLevel(prevDepth, prevRes)
    }

    fun register(matcher: NodeMatcher<*>, data: T) {
        val (_, d) = buildTo(matcher, dispatchRoot)
        val res = d.result ?: mutableListOf<T>().also { d.result = it }
        res.add(data)
    }

//    fun dispatchLeveled(root : Node, level)

    fun dispatchToList(root: Node): List<T> {
        val res = mutableListOf<T>()
        dispatch(root) {
            res.add(it)
        }
        return res
    }
}

fun TreeDispatcher<NodeMatcher<*>>.register(matcher: NodeMatcher<*>) {
    register(matcher, matcher)
}

fun <T : Node> NodeMatcher<T>.named(name: String): NodeMatcher<T> {
    return NamedMatcher(this, name)
}

fun <T : Node> NodeMatcher<T>.named(ref: RefMatcher): NodeMatcher<T> {
    return NamedMatcher(this, ref.name)
}


fun main() {
    val dispatcher = TreeDispatcher<NodeMatcher<*>>()
    with(MatcherBuilderScope) {
        dispatcher.register(integer)
        dispatcher.register(AnyMatcher)
        val m1 = pow(pow(π, y), rational)
        dispatcher.register(m1)
        val m2 = pow(sin(x), rational)
        dispatcher.register(m2)
        val m3 = pow(pow(rational, rational), rational)
        dispatcher.register(m3)
        val m4 = pow(rational, rational)
        dispatcher.register(m4)
        val m5 = pow(sin(sin(sin(x))), x)
        dispatcher.register(m5)
    }
    val expr1 = with(NodeBuilderScope) {
//        pow(pow(Node.PI, y), 1.e)
        pow(sin(sin(sin(sin(1.e)))), sin(2.e))
    }
    println("Dispatched To")
    dispatcher.dispatch(expr1) {
        val matches = it.matches(expr1, MutableMatchContextImpl()) != null
        println("Dispatched: $it; matched: $matches")
    }
//    val m1 = with(MatcherBuilderScope) {
//        pow(pow(π, y), rational).named("m1")
//    }
//    val context = MutableMatchContext()
//    println(m1.matches(expr1, context))
//    println(context.refMap)

//    println(NodeSignature.POW.matches(expr1))
}