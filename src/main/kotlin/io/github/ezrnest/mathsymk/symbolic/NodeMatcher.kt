package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.structure.PartialOrder
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg
import java.util.PriorityQueue
import java.util.SortedMap
import io.github.ezrnest.mathsymk.util.WithInt

//created at 2024/10/10

interface MatchResult {
    val cal: ExprCal

    val refMap: Map<String, Node>


    fun addRef(name: String, node: Node): MatchResult {
        val newMap = refMap.toMutableMap()
        newMap[name] = node
        return MatchResultImpl(cal, newMap)
    }

    companion object {

        operator fun invoke(cal: ExprCal): MatchResult {
            return MatchResultImpl(cal)
        }

        internal data class MatchResultImpl(
            override val cal: ExprCal,
            override val refMap: Map<String, Node> = mapOf()
        ) : MatchResult
    }
}


typealias NodeMatcher = NodeMatcherT<Node>

sealed interface NodeMatcherT<out T : Node> {

    /**
     * Tries to match the given node with the pattern represented by this matcher.
     *
     * Returns the resulting context if the node matches the pattern, or `null` otherwise.
     */
    fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult?


    /**
     * Determines whether this matcher requires a specifically determined node to match.
     */
    fun requireSpecific(ctx: ExprContext, matching: MatchResult): Boolean {
        return false
    }

    /**
     * Gets the specifically determined node that *may* be matched by this matcher or
     * `null` if it can not be determined.
     *
     * That is, if `spec = matcher.getSpecific(context)` is not `null`, then
     * `node != spec` will imply `matcher.matches(node, context) == null`.
     */
    fun getSpecific(ctx: ExprContext, matching: MatchResult): Node? {
        return null
    }


    val refNames: Set<String>
}


interface NodeMatcherFixSig<T : Node> : NodeMatcherT<T> {
    /**
     * The signature of the node that is matched by this matcher.
     */
    val nodeSig: NodeSig
}

interface NMatcherChilded<T : Node> : NodeMatcherFixSig<T> {
    val children: List<NodeMatcherT<Node>>

}

interface NMatcherChildedOrdered<T : Node> : NMatcherChilded<T> {
    override val children: List<NodeMatcherT<Node>>
}

interface TransparentNodeMatcher<T : Node> : NodeMatcherT<T> {
    val matcher: NodeMatcherT<T>
}

interface LeafMatcher<T : Node> : NodeMatcherT<T> {

}

abstract class AbsNMatcherFixSig<T : Node>(final override val nodeSig: NodeSig) : NodeMatcherFixSig<T> {


}

class NodeMatcher1<C : Node>(val child: NodeMatcherT<C>, nodeName: NodeSig) :
    AbsNMatcherFixSig<Node1T<C>>(nodeName), NMatcherChildedOrdered<Node1T<C>> {

    override val children: List<NodeMatcherT<Node>>
        get() = listOf(child)

    override val refNames: Set<String>
        get() = child.refNames

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        if (node !is Node1) return null
        if (!nodeSig.matches(node)) return null
        val subCtx = matching.cal.enterContext(node, ctx)[0]
        return child.matches(node.child, subCtx, matching)
    }

    override fun toString(): String {
        return "${nodeSig.name}($child)"
    }

    override fun requireSpecific(ctx: ExprContext, matching: MatchResult): Boolean {
        return child.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: ExprContext, matching: MatchResult): Node1T<Node>? {
        val c = child.getSpecific(ctx, matching) ?: return null
        return Node.Node1(nodeSig.name, c)
    }
}

class NodeMatcher2Ordered<C1 : Node, C2 : Node>(
    val child1: NodeMatcherT<C1>, val child2: NodeMatcherT<C2>, nodeName: NodeSig
) :
    AbsNMatcherFixSig<Node2T<C1, C2>>(nodeName), NMatcherChildedOrdered<Node2T<C1, C2>> {
    override val children: List<NodeMatcherT<Node>>
        get() = listOf(child1, child2)

    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        child1.refNames + child2.refNames
    }

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        if (node !is Node2) return null
        if (!nodeSig.matches(node)) return null
        val (node1, node2) = node
        val (subCtx1, subCtx2) = matching.cal.enterContext(node, ctx)
        var newM = matching
        newM = child1.matches(node1, subCtx1, newM) ?: return null
        newM = child2.matches(node2, subCtx2, newM) ?: return null
        return newM
    }

    override fun toString(): String {
        return "${nodeSig.name}($child1, $child2)"
    }

    override fun requireSpecific(ctx: ExprContext, matching: MatchResult): Boolean {
        return child1.requireSpecific(ctx, matching) && child2.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: ExprContext, matching: MatchResult): Node2? {
        val c1 = child1.getSpecific(ctx, matching) ?: return null
        val c2 = child2.getSpecific(ctx, matching) ?: return null
        return Node.Node2(nodeSig.name, c1, c2)
    }
}

class NodeMatcher3Ordered<C1 : Node, C2 : Node, C3 : Node>(
    val child1: NodeMatcherT<C1>, val child2: NodeMatcherT<C2>, val child3: NodeMatcherT<C3>, nodeName: NodeSig
) : AbsNMatcherFixSig<Node3T<C1, C2, C3>>(nodeName),
    NMatcherChildedOrdered<Node3T<C1, C2, C3>> {
    override val children: List<NodeMatcherT<Node>>
        get() = listOf(child1, child2, child3)
    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        child1.refNames + child2.refNames + child3.refNames
    }

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        if (node !is Node3) return null
        if (!nodeSig.matches(node)) return null
        val (node1, node2, node3) = node
        val (subCtx1, subCtx2, subCtx3) = matching.cal.enterContext(node, ctx)
        var newM = matching
        newM = child1.matches(node1, subCtx1, newM) ?: return null
        newM = child2.matches(node2, subCtx2, newM) ?: return null
        newM = child3.matches(node3, subCtx3, newM) ?: return null
        return newM
    }

    override fun toString(): String {
        return "${nodeSig.name}($child1, $child2, $child3)"
    }

    override fun requireSpecific(ctx: ExprContext, matching: MatchResult): Boolean {
        return child1.requireSpecific(ctx, matching)
                && child2.requireSpecific(ctx, matching)
                && child3.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: ExprContext, matching: MatchResult): Node3? {
        val c1 = child1.getSpecific(ctx, matching) ?: return null
        val c2 = child2.getSpecific(ctx, matching) ?: return null
        val c3 = child3.getSpecific(ctx, matching) ?: return null
        return Node.Node3(nodeSig.name, c1, c2, c3)
    }
}

class NMatcherNOrdered(override val children: List<NodeMatcherT<Node>>, nodeName: NodeSig) :
    AbsNMatcherFixSig<NodeN>(nodeName), NMatcherChildedOrdered<NodeN> {
    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        children.flatMap { it.refNames }.toSet()
    }

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        if (node !is NodeN) return null
        if (!nodeSig.matches(node)) return null
        if (node.children.size != children.size) return null
        val subCtxs = matching.cal.enterContext(node, ctx)
        var newM = matching
        for (i in children.indices) {
            newM = children[i].matches(node.children[i], subCtxs[i], newM) ?: return null
        }
        return newM
    }

    override fun toString(): String {
        return "${nodeSig.name}(${children.joinToString(", ")})"
    }
}

object NothingMatcher : LeafMatcher<Node>, NodeMatcherFixSig<Node> {

    override val nodeSig: NodeSig = Node.UNDEFINED.signature

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        return null
    }

    override val refNames: Set<String> get() = emptySet()

    override fun toString(): String {
        return "Nothing"
    }

    override fun requireSpecific(ctx: ExprContext, matching: MatchResult): Boolean {
        return true
    }

    override fun getSpecific(ctx: ExprContext, matching: MatchResult): Node? {
        return Node.UNDEFINED
    }
}

object AnyMatcher : LeafMatcher<Node> {
    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        return matching
    }

    override val refNames: Set<String> get() = emptySet()

    override fun toString(): String {
        return "Any"
    }
}

object AnyRationalMatcher : LeafMatcher<NRational>, NodeMatcherFixSig<NRational> {
    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        if (node is NRational) return matching
        return null
    }

    override val refNames: Set<String> get() = emptySet()

    override val nodeSig: NodeSig get() = SymAlg.Signatures.RATIONAL

    override fun toString(): String {
        return "AnyRational"
    }
}

object AnySymbolMatcher : LeafMatcher<NSymbol>, NodeMatcherFixSig<NSymbol> {
    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        if (node is NSymbol) return matching
        return null
    }

    override val refNames: Set<String> get() = emptySet()

    override val nodeSig: NodeSig get() = NodeSig.SYMBOL

    override fun toString(): String {
        return "AnySymbol"
    }
}


class MatcherRef(val name: String) : LeafMatcher<Node> {
    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        val ref = matching.refMap[name]
        if (ref == null) {
            return matching.addRef(name, node)
        }
        return if (ref == node) matching else null
    }

    override val refNames: Set<String> get() = setOf(name)

    override fun toString(): String {
        return "Ref($name)"
    }
}

class MatcherNamed<T : Node>(override val matcher: NodeMatcherT<T>, val name: String) : TransparentNodeMatcher<T> {
    override val refNames: Set<String>
        get() = matcher.refNames + name

    override fun toString(): String {
        return "Named(name=$name, $matcher)"
    }

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        val ref = matching.refMap[name]
        if (ref != null && ref != node) return null
        val res = matcher.matches(node,ctx , matching) ?: return null
        return if (ref == null) {
            res.addRef(name, node)
        } else {
            res
        }
    }

    override fun requireSpecific(ctx: ExprContext, matching: MatchResult): Boolean {
        if (name in matching.refMap) {
            return true // requires the specific node to match
        }
        return matcher.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: ExprContext, matching: MatchResult): Node? {
        if (name in matching.refMap) {
            return matching.refMap[name]
        }
        return matcher.getSpecific(ctx, matching)
    }
}


class MatcherWithPrecondition<T : Node>(
    override val matcher: NodeMatcherT<T>, val pre: (Node, ExprContext, MatchResult) -> Boolean
) : TransparentNodeMatcher<T> {
    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        return if (pre(node,ctx, matching)) matcher.matches(node,ctx , matching) else null
    }

    override val refNames: Set<String>
        get() = matcher.refNames
}

class MatcherWithPostcondition<T : Node>(
    override val matcher: NodeMatcherT<T>, val post: (Node, ExprContext, MatchResult) -> Boolean
) : TransparentNodeMatcher<T> {
    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        return matcher.matches(node,ctx , matching)?.takeIf { post(node,ctx, it) }
    }

    override val refNames: Set<String>
        get() = matcher.refNames
}

class MatcherWithPostConditionNode<T : Node>(
    override val matcher: NodeMatcherT<T>, val condition: Node,
) : TransparentNodeMatcher<T> {
    override val refNames: Set<String>
        get() = matcher.refNames

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        val newM = matcher.matches(node,ctx , matching) ?: return null
        val satisfied = NodeScopeMatcher.testConditionRef(condition, ctx, newM)
        if(satisfied) return newM
        return null
    }
}

class FixedNodeMatcher<T : Node>(val target: T) : LeafMatcher<T>, NodeMatcherFixSig<T> {
    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        return if (node == target) matching else null
    }

    override val refNames: Set<String> get() = emptySet()

    override val nodeSig: NodeSig = target.signature

    override fun toString(): String {
        return target.plainToString()
    }

    override fun requireSpecific(ctx: ExprContext, matching: MatchResult): Boolean {
        return true
    }

    override fun getSpecific(ctx: ExprContext, matching: MatchResult): T {
        return target
    }
}

class LeafMatcherFixSig<T : Node> private constructor(signature: NodeSig) :
    AbsNMatcherFixSig<T>(signature), LeafMatcher<T> {

    override val refNames: Set<String>
        get() = emptySet()

    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        return if (nodeSig.matches(node)) matching else null
    }

    companion object {
        fun forLeaf(name: String): LeafMatcherFixSig<LeafNode> {
            return LeafMatcherFixSig(NodeSig(name, NodeSig.NType.Leaf))
        }

        fun forNode1(name: String): LeafMatcherFixSig<Node1> {
            return LeafMatcherFixSig(NodeSig(name, NodeSig.NType.Node1))
        }

        fun forNode2(name: String): LeafMatcherFixSig<Node2> {
            return LeafMatcherFixSig(NodeSig(name, NodeSig.NType.Node2))
        }

        fun forNode3(name: String): LeafMatcherFixSig<Node3> {
            return LeafMatcherFixSig(NodeSig(name, NodeSig.NType.Node3))
        }

        fun forNodeN(name: String): LeafMatcherFixSig<NodeN> {
            return LeafMatcherFixSig(NodeSig(name, NodeSig.NType.NodeN))
        }

        operator fun invoke(name: String, type: NodeSig.NType): LeafMatcherFixSig<Node> {
            return LeafMatcherFixSig(NodeSig(name, type))
        }

        operator fun invoke(signature: NodeSig): LeafMatcherFixSig<Node> {
            return LeafMatcherFixSig(signature)
        }
    }
}


class NodeMatcherNPO(
    children: List<NodeMatcherT<Node>>, nodeName: NodeSig,
    var remMatcher: NodeMatcherT<Node> = NothingMatcher
) : AbsNMatcherFixSig<NodeN>(nodeName), NMatcherChilded<NodeN> {

    override fun toString(): String {
        return "${nodeSig.name}(${childrenChains.flatten().joinToString(", ")})"
    }


    val childrenChains: List<List<NodeMatcherT<*>>> =
        PartialOrder.chainDecomp(children, MatcherPartialOrder).sortedWith(ChainPreference)

    val totalChildren: Int = children.size

    override val children: List<NodeMatcherT<Node>>
        get() = childrenChains.flatten()

    private val requireFullMatch: Boolean
        get() = childrenChains.size == 1 && remMatcher === NothingMatcher

    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        children.flatMap { it.refNames }.toSet()
    }

    private fun match0(
        chainIndex: Int, pos: Int, ctx: MatchResult,
        children: List<Node>, subCtxs : List<ExprContext>,
        matched: BooleanArray, cur: Int
    ): MatchResult? {
        val curChain = childrenChains[chainIndex]
        val curChainRem = curChain.size - pos
        val matcher = curChain[pos]

        for (i in cur..(children.size - curChainRem)) {
            if (matched[i]) continue
            val child = children[i]
            val subCtx = subCtxs[i]
            if (matcher.matches(child,subCtx , ctx) != null) {
                matched[i] = true
                if (pos == curChain.size - 1) {
                    if (chainIndex == childrenChains.size - 1) {
                        return ctx
                    }
                    val sub = match0(chainIndex + 1, 0, ctx, children,subCtxs, matched, 0)
                    if (sub != null) return sub
                } else {
                    val sub = match0(chainIndex, pos + 1, ctx, children,subCtxs, matched, i + 1)
                    if (sub != null) return sub
                }
                matched[i] = false
            }
        }
        return null
    }

    private fun fullMatchOrdered(node: NodeN, matchers: List<NodeMatcher>,ctx : ExprContext, matchResult: MatchResult): MatchResult? {
        val children = node.children
        if (children.size != matchers.size) return null
        val subCtxs = matchResult.cal.enterContext(node, ctx)
        var newM = matchResult
        for (i in children.indices) {
            val child = children[i]
            val subCtx = subCtxs[i]
            val matcher = matchers[i]
            newM = matcher.matches(child,subCtx , newM) ?: return null
        }
        return newM
    }


    override fun matches(node: Node, ctx: ExprContext, matching: MatchResult): MatchResult? {
        if (node !is NodeN) return null
        if (node.name != nodeSig.name) return null
        if (requireFullMatch) {
            return fullMatchOrdered(node, childrenChains[0],ctx, matching)
        }
        val nodeChildren = node.children
        val subCtxs = matching.cal.enterContext(node, ctx)
        val nodeCount = nodeChildren.size
        if (nodeCount < totalChildren) return null
        if (nodeCount > totalChildren && remMatcher === NothingMatcher) return null
        val matched = BooleanArray(nodeChildren.size)
        var newM = match0(0, 0, matching, nodeChildren,subCtxs, matched, 0) ?: return null
        if (nodeCount == totalChildren) {
            return newM
        }
        val remChildren = ArrayList<Node>(nodeCount - totalChildren)
        nodeChildren.filterIndexedTo(remChildren) { index, _ -> !matched[index] }
        val remNode = Node.NodeN(nodeSig.name, remChildren)
        return remMatcher.matches(remNode,ctx,newM)
    }

    companion object {
        private fun NodeMatcherT<*>.unwrap(): NodeMatcherT<Node> {
            var m = this
            while (m is TransparentNodeMatcher<*>) {
                m = m.matcher
            }
            return m
        }
    }

    object ChainPreference : Comparator<List<NodeMatcherT<*>>> {

        fun compareChildren(o1: NodeMatcherFixSig<*>, o2: NodeMatcherFixSig<*>): Int {
            if (o1 is NMatcherChildedOrdered && o2 is NMatcherChildedOrdered) {
                val c = o1.children.size - o2.children.size
                if (c != 0) return c
                for (i in o1.children.indices) {
                    val c = compareNode(o1.children[i], o2.children[i])
                    if (c != 0) return c
                }
            }
            return 0
        }

        fun compareNode(o1: NodeMatcherT<*>, o2: NodeMatcherT<*>): Int {
            val a = o1.unwrap()
            val b = o2.unwrap()
            if (a is NodeMatcherFixSig<*> && b is NodeMatcherFixSig<*>) {
                val c = a.nodeSig.compareTo(b.nodeSig)
                if (c != 0) return c
                return compareChildren(a, b)
            }
            if (a is NodeMatcherFixSig) return -1
            if (b is NodeMatcherFixSig) return 1

            return 0
        }

        override fun compare(o1: List<NodeMatcherT<*>>, o2: List<NodeMatcherT<*>>): Int {
            if (o1.size != o2.size) return o2.size - o1.size
            for (i in o1.indices) {
                val c = compareNode(o1[i], o2[i])
                if (c != 0) return c
            }
            return 0
        }


    }

    /**
     * Defines a partial order on NodeMatcher such that `x < y` guarantees `x.match < y.match` in node's order.
     */
    object MatcherPartialOrder : PartialOrder<NodeMatcherT<*>> {

        fun compareChildren(o1: NodeMatcherFixSig<*>, o2: NodeMatcherFixSig<*>): PartialOrder.Result {
            if (o1 is NMatcherChildedOrdered && o2 is NMatcherChildedOrdered) {
                val c = o1.children.size - o2.children.size
                if (c != 0) return PartialOrder.Result.ofInt(c)
                for (i in o1.children.indices) {
                    val c = compare(o1.children[i], o2.children[i])
                    if (c != PartialOrder.Result.EQUAL) return c
                }
                return PartialOrder.Result.EQUAL
            }
            return PartialOrder.Result.INCOMPARABLE
        }


        override fun compare(o1: NodeMatcherT<*>, o2: NodeMatcherT<*>): PartialOrder.Result {
            val a = o1.unwrap()
            val b = o2.unwrap()
            if (a is NodeMatcherFixSig<*> && b is NodeMatcherFixSig<*>) {
                val c = a.nodeSig.compareTo(b.nodeSig)
                if (c != 0) return PartialOrder.Result.ofInt(c)
                return compareChildren(a, b)
            }
            return PartialOrder.Result.INCOMPARABLE
        }
    }


}


interface MatcherBuilderScope {
    val x: MatcherRef get() = MatcherRef("x")
    val y: MatcherRef get() = MatcherRef("y")
    val z: MatcherRef get() = MatcherRef("z")


    val String.ref: MatcherRef get() = MatcherRef(this)
    val String.s: NodeMatcherT<NSymbol> get() = symbol(NSymbol(this))

    fun ref(name: String): MatcherRef {
        return MatcherRef(name)
    }

    fun symbol(name: String): NodeMatcherT<NSymbol> {
        return symbol(NSymbol(name))
    }

    fun symbol(s: NSymbol): NodeMatcherT<NSymbol> {
        return FixedNodeMatcher(s)
    }

    fun <T : Node> node(n: T): FixedNodeMatcher<T> {
        return FixedNodeMatcher(n)
    }
}

interface MatcherScopeAlg : MatcherBuilderScope {

    fun <T : Node, S : Node> pow(base: NodeMatcherT<T>, exp: NodeMatcherT<S>): NodeMatcherT<Node2T<T, S>> {
        return NodeMatcher2Ordered(base, exp, SymAlg.Signatures.POW)
    }

    fun <T : Node> exp(x: NodeMatcherT<T>): NodeMatcherT<Node2T<NSymbol, T>> {
        return NodeMatcher2Ordered(NATURAL_E, x, SymAlg.Signatures.POW)
    }

    fun <T : Node> sin(x: NodeMatcherT<T>): NodeMatcherT<Node1T<T>> {
        return NodeMatcher1(x, SymAlg.Signatures.F1_SIN)
    }

    fun <T : Node> cos(x: NodeMatcherT<T>): NodeMatcherT<Node1T<T>> {
        return NodeMatcher1(x, SymAlg.Signatures.F1_COS)
    }

    fun <T : Node> tan(x: NodeMatcherT<T>): NodeMatcherT<Node1T<T>> {
        return NodeMatcher1(x, SymAlg.Signatures.F1_TAN)
    }


    val any: NodeMatcherT<Node> get() = AnyMatcher

    val NATURAL_E get() = symbol(SymAlg.NATURAL_E)
    val π: NodeMatcherT<NSymbol> get() = symbol(SymAlg.PI)


    val integer: NodeMatcherT<NRational> get() = AnyRationalMatcher

    val rational: NodeMatcherT<NRational> get() = AnyRationalMatcher


    val Int.e: NodeMatcherT<NRational> get() = node(SymAlg.Int(this))


    private fun flatten(children: List<NodeMatcherT<Node>>, sig: NodeSig): NodeMatcher {
        // flatten the children
        val newChildren = ArrayList<NodeMatcherT<Node>>(children.size)
        for (c in children) {
            if (c is NMatcherNOrdered && c.nodeSig == sig) {
                newChildren.addAll(c.children)
            } else if (c is NodeMatcherNPO && c.nodeSig == sig) {
                newChildren.addAll(c.childrenChains[0])
            } else {
                newChildren.add(c)
            }
        }
        return NodeMatcherNPO(newChildren, sig)
    }

    operator fun NodeMatcherT<Node>.times(other: NodeMatcher): NodeMatcher {
        return flatten(listOf(this, other), SymAlg.Signatures.MUL)
    }

    operator fun NodeMatcherT<Node>.plus(other: NodeMatcher): NodeMatcher {
        return flatten(listOf(this, other), SymAlg.Signatures.ADD)
    }


    fun <T : Node> NodeMatcherT<T>.named(name: String): NodeMatcherT<T> {
        return MatcherNamed(this, name)
    }

    fun <T : Node> NodeMatcherT<T>.named(ref: MatcherRef): NodeMatcherT<T> {
        return MatcherNamed(this, ref.name)
    }

    fun <T : Node> NodeMatcherT<T>.also(postCond: NodeScopeMatched.() -> Boolean): NodeMatcherT<T> {
        return MatcherWithPostcondition(this) { node, ctx,matching ->
            NodeScopeMatched(ctx,matching).postCond()
        }
    }


    companion object : MatcherScopeAlg
}

fun <T : Node> buildMatcher(action: MatcherScopeAlg.() -> NodeMatcherT<T>): NodeMatcherT<T> {
    TODO()
//    return action(MatcherBuilderScope)
}


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


fun main() {
}
/*
    val dispatcher = TreeDispatcher<Int>()

    with(MatcherBuilderScope) {

        dispatcher.register(integer, 1)
        dispatcher.register(AnyMatcher, 2)
//        val m1 =
        dispatcher.register(pow(pow(π, y), rational), 3)
//        val m2 = pow(sin(x), rational)
//        dispatcher.register(m2)
//        val m3 = pow(pow(rational, rational), rational)
//        dispatcher.register(m3)
//        val m4 = pow(rational, rational)
//        dispatcher.register(m4)
//        val m5 = pow(sin(sin(sin(x))), x)
//        dispatcher.register(m5)

        val m1 = pow(sin(x), 2.e)
        val m2 = pow(cos(x), 2.e)
        val mat = NodeMatcherNPO(listOf(m1, m2), NodeSig.ADD, ref("rem"))
        mat.childrenChains.forEach { println(it) }
//        dispatcher.register(mat)
        dispatcher.register(mat, 4)
        dispatcher.register(m1, 5)
        dispatcher.register(m2, 6)
        dispatcher.printDispatchTree()
    }
//    TestExprContext.dispatcher.printDispatchTree()
//    val expr1 = with(NodeBuilderScope) {
//        pow(sin(x), 2.e) + pow(cos(x), 2.e) + x + sin(x)
//    }.let {
//        TestExprContext.simplifyFull(it)
////        it
//    }
//    println(expr1.plainToString())
//    println("Dispatched To")
//    dispatcher.dispatch(expr1) {
//        val ctx = MutableMatchContextImpl()
//        val matches = it.matches(expr1, ctx) != null
//        println("$it")
//        if (matches) {
//            val mapStr = ctx.refMap.entries.joinToString("; ") { "${it.key} = ${it.value.plainToString()}" }
//            println("Matched: {$mapStr}")
//        } else {
//            println("Not Matched")
//        }
//    }
 */