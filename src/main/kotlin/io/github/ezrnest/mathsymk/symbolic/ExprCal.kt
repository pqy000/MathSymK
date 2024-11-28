package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.model.*
import io.github.ezrnest.mathsymk.structure.Reals
import io.github.ezrnest.mathsymk.symbolic.alg.IAlgebraScope
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg
import io.github.ezrnest.mathsymk.symbolic.alg.RulesExponentialReduce
import io.github.ezrnest.mathsymk.symbolic.alg.RulesTrigonometricReduce
import io.github.ezrnest.mathsymk.symbolic.logic.SymLogic
import io.github.ezrnest.mathsymk.util.WithInt
import java.util.*
import kotlin.math.max
import kotlin.math.min


interface ExprCal {

    val options: Map<TypedKey<*>, Any> get() = emptyMap()

    val context: ExprContext

    val rationals: BigFracAsQuot get() = BigFracAsQuot
    val multinomials: MultiOverField<BigFrac> get() = MultiOverField(rationals, Multinomial.DEFAULT_ORDER)
    val polynomials: PolyOverField<BigFrac> get() = PolyOverField(rationals)


    object Options {
        /**
         * Forces all the computations to be done in the real domain, throwing an ArithmeticException for undefined operations like `sqrt(-1)`.
         */
        val forceReal: TypedKey<Boolean> = TypedKey("forceReal")

        val simMaxStep: TypedKey<Int> = TypedKey("simMaxStep")

        val simTruncationFactor: TypedKey<Double> = TypedKey("simTruncationFactor")
    }


    fun isCommutative(name: String): Boolean

    fun reduce(root: Node, depth: Int = Int.MAX_VALUE): Node {
        return reduceNode(root, context, depth)
    }

    fun reduceNode(node: Node, context: ExprContext, depth: Int = 0): Node


    fun isSatisfied(ctx: ExprContext, condition: Node): Boolean {
//        return false
        return reduceNode(condition, ctx, Int.MAX_VALUE) == SymLogic.TRUE
    }

    fun simplify(node: Node): List<Node>

    fun format(node: Node): String {
        return node.plainToString()
    }

    fun substitute(node: Node, src: NSymbol, dest: Node, rootCtx : ExprContext= this.context): Node {
        return substitute(node,rootCtx) { it,ctx ->
            if (it == src) dest else null
        }
    }

    fun substitute(root: Node, rootCtx : ExprContext = this.context, mapping: (NSymbol,ExprContext) -> Node?): Node {
        return recurMapCtx(root, rootCtx, Int.MAX_VALUE) { n, ctx ->
            if (n is NSymbol && !ctx.isQualified(n)) mapping(n,ctx) else null
        }
    }

    fun enterContext(root: Node, context: ExprContext): List<ExprContext>


    fun recurMapCtx(
        root: Node, context: ExprContext = this.context,
        depth: Int = Int.MAX_VALUE, mapping: (Node, ExprContext) -> Node?,
    ): Node

    companion object {

//        internal fun
    }
}

//fun ExprCal.enterContext1(root : Node1, context: ExprContext): ExprContext {
//    return enterContext(root, context)[0]
//}
//
//fun ExprCal.enterContext2(root : Node2, context: ExprContext): Pair<ExprContext, ExprContext> {
//    val ctx = enterContext(root, context)
//    return ctx[0] to ctx[1]
//}
//
//fun ExprCal.enterContext3(root : Node3, context: ExprContext): Triple<ExprContext, ExprContext, ExprContext> {
//    val ctx = enterContext(root, context)
//    return Triple(ctx[0], ctx[1], ctx[2])
//}
//



typealias NodeWithComplexity = WithInt<Node>

class SimProcess(
    var depth: Int = 0, var steps: Int = 0, var context: ExprContext,
    val complexity: NodeComplexity,
) {
    val order = compareBy<NodeWithComplexity> { it.v }.thenBy(NodeOrder) { it.item }
    var stepLimit: Int = 1000
    var maxDepth: Int = Int.MAX_VALUE
    var discardRatio: Double = 100.0
    var simMaxWidth: Int = 10


    fun create(node: Node): NodeWithComplexity {
        return NodeWithComplexity(complexity.complexity(node, context), node)
    }
}


open class BasicExprCal : ExprCal, NodeScope {

    override val options: MutableMap<TypedKey<*>, Any> = mutableMapOf()

    val reduceRules: TreeDispatcher<SimRule> = TreeDispatcher()

    val transRules: TreeDispatcher<TransRule> = TreeDispatcher()

    var verbose: Verbosity = Verbosity.NONE
    var showMeta = false

    enum class Verbosity {
        NONE, WHEN_APPLIED, ALL
    }

    override val context: ExprContext = EmptyExprContext // TODO

    var complexity: NodeComplexity = BasicComplexity


    private val indent = "|  "
    private var simLevel = -1


    init {
        listOf(
            Flatten(SymAlg.Names.ADD),
            Flatten(SymAlg.Names.MUL),
            RuleSort(SymAlg.Signatures.ADD),
            RuleSort(SymAlg.Signatures.MUL),
            MergeAdditionRational,
            MergeProduct,
            ComputePow,
            FlattenPow
        ).forEach {
            addReduceRule(it)
        }
    }

    fun addReduceRule(rule: SimRule) {
        val rule = rule.init(this) ?: return
        reduceRules.register(rule.matcher, rule)
    }

    fun addReduceRuleAll(rules: RuleList) {
        rules.list.forEach { addReduceRule(it) }
    }

    fun addRule(rule: TransRule) {
        val rule = rule.init(this) ?: return
        transRules.register(rule.matcher, rule)
    }


    override fun isCommutative(name: String): Boolean {
        return when (name) {
            SymAlg.Names.ADD -> true
            SymAlg.Names.MUL -> true
            else -> false
        }
    }

    private inline fun log(level: Verbosity, supplier: () -> String) {
        if (verbose >= level) {
            println(indent.repeat(max(simLevel, 0)) + supplier())
        }
    }

    private fun showNode(node: Node): String {
        val meta = node.meta
        if (meta.isEmpty() || !showMeta) return node.plainToString()
        return "${node.plainToString()}, ${node.meta}"
    }


    override fun reduceNode(node: Node, context: ExprContext, depth: Int): Node {
        node[NodeMetas.reduceTo]?.let {
            return it
        }
        var depth = depth
        var res = node
        simLevel++
        log(Verbosity.WHEN_APPLIED) { "Sim:  ${showNode(node)}" }
        while (true) {
            if (res[NodeMetas.simplified] == true) break
            res = if (depth <= 0) {
                res
            } else {
                when (res) {
                    is LeafNode -> res
                    is Node1 -> reduceRecur1(res, context, depth - 1)
                    is Node2 -> reduceRecur2(res, context, depth - 1)
                    is Node3 -> reduceRecur3(res, context, depth - 1)
                    is NodeN -> reduceRecurN(res, context, depth - 1)
                    else -> res
                }
            }
            var appliedRule: SimRule? = null
            for (rule in reduceRules.dispatchSeq(res)) {
                log(Verbosity.ALL) { "|> ${rule.description}" }
                val p = rule.simplify(res, context, this) ?: continue
                if (verbose == Verbosity.WHEN_APPLIED) {
                    log(Verbosity.WHEN_APPLIED) { "|>${rule.description}" }
                }
                appliedRule = rule
                depth = p.v
                res = p.item
                log(Verbosity.WHEN_APPLIED) { "|->  ${showNode(res)}" }
                break
            }
            if (appliedRule == null) {
                log(Verbosity.ALL) { "|> Nothing happened ..." }
                log(Verbosity.ALL) { "|->  ${showNode(res)}" }
                break
            }
        }

        simLevel--
        res[NodeMetas.reduceTo] = res
        return res
    }

    override fun enterContext(root: Node, context: ExprContext): List<ExprContext> {

        TODO()
    }

    override fun recurMapCtx(
        root: Node, context: ExprContext, depth: Int, mapping: (Node, ExprContext) -> Node?
    ): Node {
        if (depth <= 0) return mapping(root, context) ?: root
        val depth1 = depth - 1
        val res = when (root) {
            is LeafNode -> root
            is Node1 -> {
                val ctx = enterContext(root, context)
                val n1 = recurMapCtx(root.child, ctx[0], depth1, mapping)
                if (n1 === root.child)
                    root else root.newWithChildren(n1)
            }

            is Node2 -> {
                val (c1, c2) = root
                val (ctx1, ctx2) = enterContext(root, context)
                val n1 = recurMapCtx(c1, ctx1, depth1, mapping)
                val n2 = recurMapCtx(c2, ctx2, depth1, mapping)
                if (n1 === c1 && n2 === c2) root
                else root.newWithChildren(n1, n2)
            }

            is Node3 -> {
                val (c1, c2, c3) = root
                val (ctx1, ctx2, ctx3) = enterContext(root, context)
                val n1 = recurMapCtx(c1, ctx1, depth1, mapping)
                val n2 = recurMapCtx(c2, ctx2, depth1, mapping)
                val n3 = recurMapCtx(c3, ctx3, depth1, mapping)
                if (n1 === c1 && n2 === c2 && n3 === c3) root
                else root.newWithChildren(n1, n2, n3)
            }

            is NodeN -> {
                var changed = false
                val children = root.children
                val childContext = enterContext(root, context)
                val newChildren = children.indices.map { i ->
                    val child = children[i]
                    recurMapCtx(child, childContext[i], depth1, mapping).also { if (it !== child) changed = true }
                }
                if (!changed) root else root.newWithChildren(newChildren)
            }
        }
        return mapping(res, context) ?: res
    }

    private fun reduceRecur1(node: Node1, context: ExprContext, depth: Int): Node {
        val ctx = enterContext(node, context)
        val n1 = reduceNode(node.child, ctx[0], depth)
        if (n1 === node.child) return node
        return node.newWithChildren(n1)
    }

    private fun reduceRecur2(node: Node2, context: ExprContext, depth: Int): Node {
        val (c1, c2) = node
        val (ctx1, ctx2) = enterContext(node, context)
        val n1 = reduceNode(c1, ctx1, depth)
        val n2 = reduceNode(c2, ctx2, depth)
        if (n1 === c1 && n2 === c2) return node
        return node.newWithChildren(n1, n2)
    }

    private fun reduceRecur3(node: Node3, context: ExprContext, depth: Int): Node {
        val (c1, c2, c3) = node
        val (ctx1, ctx2, ctx3) = enterContext(node, context)
        val n1 = reduceNode(c1, ctx1, depth)
        val n2 = reduceNode(c2, ctx2, depth)
        val n3 = reduceNode(c3, ctx3, depth)
        if (n1 === c1 && n2 === c2 && n3 === c3) return node
        return node.newWithChildren(n1, n2, n3)
    }

    private fun reduceRecurN(node: NodeN, context: ExprContext, depth: Int): Node {
        var changed = false
        val children = node.children
        val childContext = enterContext(node, context)
        val newChildren = children.indices.map { i ->
            val child = children[i]
            reduceNode(child, childContext[i], depth).also { if (it !== child) changed = true }
        }
        if (!changed) return node
        return node.newWithChildren(newChildren)
    }


    private fun transRecur1(node: Node1, sim: SimProcess): List<Node> {
        return transNode(node.child, sim).map { node.newWithChildren(it.item) }
    }

    private fun transRecur2(node: Node2, sim: SimProcess): List<Node> {
        val s1 = transNode(node.first, sim)
        val s2 = transNode(node.second, sim)
        val size = s1.size + s2.size
        if (size == 0) return emptyList()
        val res = ArrayList<Node>(size)
        for (f1 in s2) {
            res.add(node.newWithChildren(f1.item, node.second))
        }
        for (f2 in s2) {
            res.add(node.newWithChildren(node.first, f2.item))
        }
        return res
    }

    private fun transRecur3(node: Node3, sim: SimProcess): List<Node> {
        val s1 = transNode(node.first, sim)
        val s2 = transNode(node.second, sim)
        val s3 = transNode(node.third, sim)
        val size = s1.size + s2.size + s3.size
        if (size == 0) return emptyList()
        val res = ArrayList<Node>(size)
        for (f1 in s1) {
            res.add(node.newWithChildren(f1.item, node.second, node.third))
        }
        for (f2 in s2) {
            res.add(node.newWithChildren(node.first, f2.item, node.third))
        }
        for (f3 in s3) {
            res.add(node.newWithChildren(node.first, node.second, f3.item))
        }
        return res
    }

    private fun transRecurN(node: NodeChilded, sim: SimProcess): List<Node> {
        val subs = node.children.map { n -> transNode(n, sim) }
        val size = subs.sumOf { it.size }
        if (size == 0) return emptyList()
        val res = ArrayList<Node>(size)
        for (i in subs.indices) {
            for (sub in subs[i]) {
                val childrenList = node.children.toMutableList()
                childrenList[i] = sub.item
                res.add(node.newWithChildren(childrenList))
            }
        }
        return res
    }


    /**
     * Returns a list of transformations of the given node, not including the node itself.
     * The list can be empty if no transformation is possible.
     * The results are sorted by complexity.
     */
    private fun transNode(node: Node, sim: SimProcess): List<NodeWithComplexity> {
        if (node[NodeMetas.simplified] == true) return emptyList()
        node[NodeMetas.transforms]?.let {
            return it
        }

        simLevel++
        val results = sortedSetOf(sim.order)
        val pending = PriorityQueue(sim.order)
        val origin = sim.create(node)
        pending += origin
        results += origin
        if (sim.steps >= sim.stepLimit) return emptyList()
        while (sim.steps < sim.stepLimit && pending.isNotEmpty()) {
            val node = pending.remove().item
            val subResult: List<Node>
            if (sim.depth <= 0) {
                subResult = emptyList()
            } else {
                sim.depth--
                subResult = when (node) {
                    is LeafNode -> emptyList()
                    is Node1 -> transRecur1(node, sim)
                    is Node2 -> transRecur2(node, sim)
                    is Node3 -> transRecur3(node, sim)
                    is NodeChilded -> transRecurN(node, sim)
                    else -> emptyList()
                }
                sim.depth++
            }
            for (n in subResult) {
                val reduced = reduceNode(n, sim.context, sim.depth)
                val ns = sim.create(reduced)
                if (results.add(ns)) {
                    pending += ns
                }
            }

            for (rule in transRules.dispatchSeq(node)) {
                sim.steps++
                if (sim.steps >= sim.stepLimit) break
                val transList = rule.transform(node, sim.context, this)
                if (transList.isEmpty()) continue
                log(Verbosity.WHEN_APPLIED) { "|>${rule.description}" }
                for ((dep, trans) in transList) {
                    val res = reduceNode(trans, sim.context, min(sim.depth, dep))
                    val ns = sim.create(res)
                    if (results.add(ns)) {
                        pending += ns
                    }
                }
            }
        }
        results.remove(origin)
        val finalResult = results.take(sim.simMaxWidth)
        node[NodeMetas.transforms] = finalResult
        simLevel--
        return finalResult

    }


    override fun simplify(node: Node): List<Node> {
        val sim = SimProcess(context = context, complexity = complexity, depth = Int.MAX_VALUE)
        val reduced = reduceNode(node, context, Integer.MAX_VALUE)
        val res = transNode(reduced, sim)
        val results = res.toMutableList()
        results.add(sim.create(reduced))
        results.sortBy { it.v }
        return results.map { it.item }
    }
}


class ExprCalReal : BasicExprCal(), Reals<Node>, IAlgebraScope {

//    private fun addAllReduce(rules: RuleList) {
//        rules.list.forEach { addReduceRule(it) }
//    }


    init {
        options[ExprCal.Options.forceReal] = true

        addReduceRuleAll(RulesTrigonometricReduce())
        addReduceRuleAll(RulesExponentialReduce())

    }

    override fun constantValue(name: String): Node {
        return when (name) {
            "pi", SymAlg.Names.Symbol_PI -> SymAlg.PI
            "e", SymAlg.Names.Symbol_E -> SymAlg.NATURAL_E
            else -> throw IllegalArgumentException("Unknown constant: $name")
        }
    }

    override val one: Node
        get() = SymAlg.ONE

    override val zero: Node
        get() = SymAlg.ZERO


    override fun contains(x: Node): Boolean = true // TODO

    override fun isEqual(x: Node, y: Node): Boolean {
        TODO("Not yet implemented")
    }

    override fun negate(x: Node): Node {
        return reduce(super.negate(x), 0)
    }

    override fun add(x: Node, y: Node): Node {
        return reduce(super<IAlgebraScope>.sum(x, y), 0)
    }

    override fun sum(elements: List<Node>): Node {
        return reduce(super<IAlgebraScope>.sum(elements), 0)
    }

    override fun multiply(x: Node, y: Node): Node {
        return reduce(super.multiply(x, y), 0)
    }

    override fun product(elements: List<Node>): Node {
        return super<IAlgebraScope>.product(elements).also { reduce(it, 0) }
    }

    override fun reciprocal(x: Node): Node {
        return super<IAlgebraScope>.inv(x).also { reduce(it, 0) }
    }

    override fun divide(x: Node, y: Node): Node {
        return super<IAlgebraScope>.divide(x, y).also { reduce(it, 1) }
    }

    override fun Node.div(y: Node): Node {
        return divide(this, y)
    }

    override fun Node.times(y: Node): Node {
        return multiply(this, y)
    }

    override fun Node.minus(y: Node): Node {
        return subtract(this, y)
    }

    override fun Node.unaryMinus(): Node {
        return negate(this)
    }

    override fun Node.plus(y: Node): Node {
        return add(this, y)
    }


    override fun sqrt(x: Node): Node {
        return super<IAlgebraScope>.sqrt(x).also { reduce(it, 0) }
    }

    override fun exp(x: Node): Node {
        return super<IAlgebraScope>.exp(x).also { reduce(it, 0) }
    }

    override fun exp(base: Node, pow: Node): Node {
        return super<IAlgebraScope>.pow(base, pow).also { reduce(it, 0) }
    }

    override fun pow(base: Node, exp: Node): Node {
        return super<IAlgebraScope>.pow(base, exp).also { reduce(it, 0) }
    }

    override fun nroot(x: Node, n: Int): Node {
        return pow(x, rational(1, n))
    }

    override fun ln(x: Node): Node {
        return super<IAlgebraScope>.ln(x).also { reduce(it, 0) }
    }

    override fun log(base: Node, x: Node): Node {
        return super<IAlgebraScope>.log(base, x).also { reduce(it, 0) }
    }

    override fun sin(x: Node): Node {
        return super<IAlgebraScope>.sin(x).also { reduce(it, 0) }
    }

    override fun cos(x: Node): Node {
        return super<IAlgebraScope>.cos(x).also { reduce(it, 0) }
    }

    override fun tan(x: Node): Node {
        return super<IAlgebraScope>.tan(x).also { reduce(it, 0) }
    }

    //    override fun cot(x: Node): Node {
//        return super<NodeScopeAlg>.cot(x).also { reduce(it, 0) }
//    }

    override fun arcsin(x: Node): Node {
        return super<IAlgebraScope>.arcsin(x).also { reduce(it, 0) }
    }

    override fun arccos(x: Node): Node {
        return super<IAlgebraScope>.arccos(x).also { reduce(it, 0) }
    }

    override fun arctan(x: Node): Node {
        return super<IAlgebraScope>.arctan(x).also { reduce(it, 0) }
    }

    override fun arctan2(y: Node, x: Node): Node {
        TODO()
    }

    override fun compare(o1: Node, o2: Node): Int {
        TODO("Not yet implemented")
    }
}
