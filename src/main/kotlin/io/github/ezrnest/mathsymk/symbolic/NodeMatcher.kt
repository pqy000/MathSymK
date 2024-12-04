package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.structure.PartialOrder
import io.github.ezrnest.mathsymk.symbolic.alg.AlgebraScope
import io.github.ezrnest.mathsymk.symbolic.alg.ComputePow
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg

//created at 2024/10/10

interface MatchResult {
    val cal: ExprCal

    val refMap: Map<String, Node>


    fun addRef(name: String, node: Node): MatchResult {
        val newMap = refMap.toMutableMap()
        newMap[name] = node
        return MatchResultImpl(cal, newMap)
    }

    fun getRef(name: String): Node? {
        return refMap[name]
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
    fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult?


    /**
     * Determines whether this matcher requires a specifically determined node to match.
     */
    fun requireSpecific(ctx: EContext, matching: MatchResult): Boolean {
        return false
    }

    /**
     * Gets the specifically determined node that *may* be matched by this matcher or
     * `null` if it can not be determined.
     *
     * That is, if `spec = matcher.getSpecific(context)` is not `null`, then
     * `node != spec` will imply `matcher.matches(node, context) == null`.
     */
    fun getSpecific(ctx: EContext, matching: MatchResult): Node? {
        return null
    }


    val refNames: Set<String>
}


interface NodeBranchMatcher<T : Node> : NodeMatcherT<T> {
    val symbol : ESymbol
}

interface NMatcherChilded<T : Node> : NodeBranchMatcher<T> {
    val children: List<NodeMatcherT<Node>>

}

interface NMatcherChildedOrdered<T : Node> : NMatcherChilded<T> {
    override val children: List<NodeMatcherT<Node>>
}

interface TransparentNodeMatcher<T : Node> : NodeMatcherT<T> {
    val matcher: NodeMatcherT<T>
}

interface LeafMatcher<T : Node> : NodeMatcherT<T>

abstract class AbsNMatcherFixSig<T : Node>(final override val symbol: ESymbol) : NodeBranchMatcher<T>

class NodeMatcher1<C : Node>(val child: NodeMatcherT<C>, symbol: ESymbol) :
    AbsNMatcherFixSig<Node1T<C>>(symbol), NMatcherChildedOrdered<Node1T<C>> {

    override val children: List<NodeMatcherT<Node>>
        get() = listOf(child)

    override val refNames: Set<String>
        get() = child.refNames

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if (node !is Node1) return null
        if (symbol != node.symbol) return null
        val subCtx = matching.cal.enterContext(node, ctx)[0]
        return child.matches(node.child, subCtx, matching)
    }

    override fun toString(): String {
        return "${symbol}($child)"
    }

    override fun requireSpecific(ctx: EContext, matching: MatchResult): Boolean {
        return child.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: EContext, matching: MatchResult): Node1T<Node>? {
        val c = child.getSpecific(ctx, matching) ?: return null
        return Node.Node1(symbol, c)
    }
}

class NodeMatcher2Ordered<C1 : Node, C2 : Node>(
    val child1: NodeMatcherT<C1>, val child2: NodeMatcherT<C2>, symbol: ESymbol
) :
    AbsNMatcherFixSig<Node2T<C1, C2>>(symbol), NMatcherChildedOrdered<Node2T<C1, C2>> {
    override val children: List<NodeMatcherT<Node>>
        get() = listOf(child1, child2)

    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        child1.refNames + child2.refNames
    }

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if (node !is Node2) return null
        if (symbol != node.symbol) return null
        val (node1, node2) = node
        val (subCtx1, subCtx2) = matching.cal.enterContext(node, ctx)
        var newM = matching
        newM = child1.matches(node1, subCtx1, newM) ?: return null
        newM = child2.matches(node2, subCtx2, newM) ?: return null
        return newM
    }

    override fun toString(): String {
        return "${symbol}($child1, $child2)"
    }

    override fun requireSpecific(ctx: EContext, matching: MatchResult): Boolean {
        return child1.requireSpecific(ctx, matching) && child2.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: EContext, matching: MatchResult): Node2? {
        val c1 = child1.getSpecific(ctx, matching) ?: return null
        val c2 = child2.getSpecific(ctx, matching) ?: return null
        return Node.Node2(symbol, c1, c2)
    }
}

class NodeMatcher3Ordered<C1 : Node, C2 : Node, C3 : Node>(
    val child1: NodeMatcherT<C1>, val child2: NodeMatcherT<C2>, val child3: NodeMatcherT<C3>, symbol: ESymbol
) : AbsNMatcherFixSig<Node3T<C1, C2, C3>>(symbol),
    NMatcherChildedOrdered<Node3T<C1, C2, C3>> {
    override val children: List<NodeMatcherT<Node>>
        get() = listOf(child1, child2, child3)
    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        child1.refNames + child2.refNames + child3.refNames
    }

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if (node !is Node3) return null
        if (symbol != node.symbol) return null
        val (node1, node2, node3) = node
        val (subCtx1, subCtx2, subCtx3) = matching.cal.enterContext(node, ctx)
        var newM = matching
        newM = child1.matches(node1, subCtx1, newM) ?: return null
        newM = child2.matches(node2, subCtx2, newM) ?: return null
        newM = child3.matches(node3, subCtx3, newM) ?: return null
        return newM
    }

    override fun toString(): String {
        return "${symbol}($child1, $child2, $child3)"
    }

    override fun requireSpecific(ctx: EContext, matching: MatchResult): Boolean {
        return child1.requireSpecific(ctx, matching)
                && child2.requireSpecific(ctx, matching)
                && child3.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: EContext, matching: MatchResult): Node3? {
        val c1 = child1.getSpecific(ctx, matching) ?: return null
        val c2 = child2.getSpecific(ctx, matching) ?: return null
        val c3 = child3.getSpecific(ctx, matching) ?: return null
        return Node.Node3(symbol, c1, c2, c3)
    }
}

class NMatcherNOrdered(override val children: List<NodeMatcherT<Node>>, symbol: ESymbol) :
    AbsNMatcherFixSig<NodeN>(symbol), NMatcherChildedOrdered<NodeN> {
    override val refNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        children.flatMap { it.refNames }.toSet()
    }

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if (node !is NodeN) return null
        if (symbol != node.symbol) return null
        if (node.children.size != children.size) return null
        val subCtxs = matching.cal.enterContext(node, ctx)
        var newM = matching
        for (i in children.indices) {
            newM = children[i].matches(node.children[i], subCtxs[i], newM) ?: return null
        }
        return newM
    }

    override fun toString(): String {
        return "${symbol}(${children.joinToString(", ")})"
    }
}

object NothingMatcher : LeafMatcher<Node> {

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        return null
    }

    override val refNames: Set<String> get() = emptySet()

    override fun toString(): String {
        return "Nothing"
    }

    override fun requireSpecific(ctx: EContext, matching: MatchResult): Boolean {
        return true
    }

    override fun getSpecific(ctx: EContext, matching: MatchResult): Node? {
        return Node.UNDEFINED
    }
}

object AnyMatcher : LeafMatcher<Node> {
    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        return matching
    }

    override val refNames: Set<String> get() = emptySet()

    override fun toString(): String {
        return "Any"
    }
}

object AnyRationalMatcher : LeafMatcher<NRational> {
    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if (node is NRational) return matching
        return null
    }

    override val refNames: Set<String> get() = emptySet()


    override fun toString(): String {
        return "AnyRational"
    }
}

object AnySymbolMatcher : LeafMatcher<NSymbol> {
    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if (node is NSymbol) return matching
        return null
    }

    override val refNames: Set<String> get() = emptySet()

    override fun toString(): String {
        return "AnySymbol"
    }
}


class MatcherRef(val name: String) : LeafMatcher<Node> {
    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
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

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        val ref = matching.refMap[name]
        if (ref != null && ref != node) return null
        val res = matcher.matches(node, ctx, matching) ?: return null
        return if (ref == null) {
            res.addRef(name, node)
        } else {
            res
        }
    }

    override fun requireSpecific(ctx: EContext, matching: MatchResult): Boolean {
        if (name in matching.refMap) {
            return true // requires the specific node to match
        }
        return matcher.requireSpecific(ctx, matching)
    }

    override fun getSpecific(ctx: EContext, matching: MatchResult): Node? {
        if (name in matching.refMap) {
            return matching.refMap[name]
        }
        return matcher.getSpecific(ctx, matching)
    }
}


class MatcherWithPrecondition<T : Node>(
    override val matcher: NodeMatcherT<T>, val pre: (Node, EContext, MatchResult) -> Boolean
) : TransparentNodeMatcher<T> {
    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        return if (pre(node, ctx, matching)) matcher.matches(node, ctx, matching) else null
    }

    override val refNames: Set<String>
        get() = matcher.refNames
}

class MatcherWithPostcondition<T : Node>(
    override val matcher: NodeMatcherT<T>, val post: (Node, EContext, MatchResult) -> Boolean
) : TransparentNodeMatcher<T> {
    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        return matcher.matches(node, ctx, matching)?.takeIf { post(node, ctx, it) }
    }

    override val refNames: Set<String>
        get() = matcher.refNames
}

class MatcherWithPostConditionNode<T : Node>(
    override val matcher: NodeMatcherT<T>, val condition: Node,
) : TransparentNodeMatcher<T> {
    override val refNames: Set<String>
        get() = matcher.refNames

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        val newM = matcher.matches(node, ctx, matching) ?: return null
        val satisfied = NodeScopeMatcher.testConditionRef(condition, ctx, newM)
        if (satisfied) return newM
        return null
    }
}

class FixedNodeMatcher<T : Node>(val target: T) : LeafMatcher<T> {
    // TODO
    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        return if (node == target) matching else null
    }

    override val refNames: Set<String> get() = emptySet()


    override fun toString(): String {
        return target.plainToString()
    }

    override fun requireSpecific(ctx: EContext, matching: MatchResult): Boolean {
        return true
    }

    override fun getSpecific(ctx: EContext, matching: MatchResult): T {
        return target
    }
}

class LeafMatcherFixSig(symbol: ESymbol) :
    AbsNMatcherFixSig<NodeChilded>(symbol), LeafMatcher<NodeChilded> {

    override val refNames: Set<String>
        get() = emptySet()

    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if(node !is NodeChilded) return null
        if (node.symbol != symbol) return null
        return matching
    }
}


class NodeMatcherNPO(
    children: List<NodeMatcherT<Node>>, symbol: ESymbol,
    var remMatcher: NodeMatcherT<Node> = NothingMatcher
) : AbsNMatcherFixSig<NodeN>(symbol), NMatcherChilded<NodeN> {

    override fun toString(): String {
        return "${symbol}(${childrenChains.flatten().joinToString(", ")})"
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
        children: List<Node>, subCtxs: List<EContext>,
        matched: BooleanArray, cur: Int
    ): MatchResult? {
        val curChain = childrenChains[chainIndex]
        val curChainRem = curChain.size - pos
        val matcher = curChain[pos]

        for (i in cur..(children.size - curChainRem)) {
            if (matched[i]) continue
            val child = children[i]
            val subCtx = subCtxs[i]
            if (matcher.matches(child, subCtx, ctx) != null) {
                matched[i] = true
                if (pos == curChain.size - 1) {
                    if (chainIndex == childrenChains.size - 1) {
                        return ctx
                    }
                    val sub = match0(chainIndex + 1, 0, ctx, children, subCtxs, matched, 0)
                    if (sub != null) return sub
                } else {
                    val sub = match0(chainIndex, pos + 1, ctx, children, subCtxs, matched, i + 1)
                    if (sub != null) return sub
                }
                matched[i] = false
            }
        }
        return null
    }

    private fun fullMatchOrdered(
        node: NodeN, matchers: List<NodeMatcher>, ctx: EContext, matchResult: MatchResult
    ): MatchResult? {
        val children = node.children
        if (children.size != matchers.size) return null
        val subCtxs = matchResult.cal.enterContext(node, ctx)
        var newM = matchResult
        for (i in children.indices) {
            val child = children[i]
            val subCtx = subCtxs[i]
            val matcher = matchers[i]
            newM = matcher.matches(child, subCtx, newM) ?: return null
        }
        return newM
    }


    override fun matches(node: Node, ctx: EContext, matching: MatchResult): MatchResult? {
        if (node !is NodeN) return null
        if(symbol != node.symbol) return null
        if (requireFullMatch) {
            return fullMatchOrdered(node, childrenChains[0], ctx, matching)
        }
        val nodeChildren = node.children
        val subCtxs = matching.cal.enterContext(node, ctx)
        val nodeCount = nodeChildren.size
        if (nodeCount < totalChildren) return null
        if (nodeCount > totalChildren && remMatcher === NothingMatcher) return null
        val matched = BooleanArray(nodeChildren.size)
        var newM = match0(0, 0, matching, nodeChildren, subCtxs, matched, 0) ?: return null
        if (nodeCount == totalChildren) {
            return newM
        }
        val remChildren = ArrayList<Node>(nodeCount - totalChildren)
        nodeChildren.filterIndexedTo(remChildren) { index, _ -> !matched[index] }
        val remNode = Node.NodeN(symbol, remChildren)
        return remMatcher.matches(remNode, ctx, newM)
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

        fun compareChildren(o1: NodeBranchMatcher<*>, o2: NodeBranchMatcher<*>): Int {
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
            if (a is NodeBranchMatcher<*> && b is NodeBranchMatcher<*>) {
                val c = a.symbol.compareTo(b.symbol)
                if (c != 0) return c
                return compareChildren(a, b)
            }
            if (a is NodeBranchMatcher) return -1
            if (b is NodeBranchMatcher) return 1

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

        fun compareChildren(o1: NodeBranchMatcher<*>, o2: NodeBranchMatcher<*>): PartialOrder.Result {
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
            if (a is NodeBranchMatcher<*> && b is NodeBranchMatcher<*>) {
                val c = a.symbol.compareTo(b.symbol)
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
//    val String.s: NodeMatcherT<NSymbol> get() = symbol(NSymbol(this))

    fun ref(name: String): MatcherRef {
        return MatcherRef(name)
    }

    fun symbol(name: ESymbol): NodeMatcherT<NSymbol> {
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


    private fun flatten(children: List<NodeMatcherT<Node>>, sig: ESymbol): NodeMatcher {
        // flatten the children
        val newChildren = ArrayList<NodeMatcherT<Node>>(children.size)
        for (c in children) {
            if (c is NMatcherNOrdered && c.symbol == sig) {
                newChildren.addAll(c.children)
            } else if (c is NodeMatcherNPO && c.symbol == sig) {
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
        return MatcherWithPostcondition(this) { node, ctx, matching ->
            NodeScopeMatched(ctx, matching).postCond()
        }
    }


    companion object : MatcherScopeAlg
}

fun <T : Node> buildMatcher(action: MatcherScopeAlg.() -> NodeMatcherT<T>): NodeMatcherT<T> {
//    TODO()
    return action(MatcherScopeAlg)
}

fun buildMatcherExpr(cal: ExprCal, action: NodeScopeMatcher.() -> Node): NodeMatcher {
    val node = NodeScopeMatcher(cal.context).action()
    return NodeScopeMatcher.buildMatcher(node, cal)
}


fun main() {
    val cal = TestExprCal
//    val matcher = buildMatcherExpr(cal) {
//        alg {
//            x and all(x) {
//                x gtr ZERO
//            }
//        }
//    }
//    val node = buildAlg {
//        x and all(y) {
//            x gtr y
//        }
//    }
//    println(matcher.matches(node, cal.context, MatchResult(cal)))
    val dispatcher = TreeDispatcher<Int>()
    with(MatcherScopeAlg) {
//        dispatcher.register(pow(rational, rational), 1)
        dispatcher.register(ComputePow.matcher,1)
    }
    dispatcher.printDispatchTree()
    with(AlgebraScope(EmptyEContext)) {
        dispatcher.dispatch(pow(2.e, 3.e)) {
            println(it)
        }
    }

}
/*


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