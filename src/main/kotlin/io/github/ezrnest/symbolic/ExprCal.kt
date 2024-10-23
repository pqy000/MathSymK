package io.github.ezrnest.symbolic

import io.github.ezrnest.structure.Reals
import io.github.ezrnest.symbolic.sim.RulesExponentialReduce
import io.github.ezrnest.symbolic.sim.RulesTrigonometricReduce
import io.github.ezrnest.symbolic.sim.addAll
import io.github.ezrnest.util.IterUtils
import io.github.ezrnest.util.all2
import java.util.Objects
import java.util.SortedSet
import kotlin.math.max


interface ExprCal {

    val options: Map<TypedKey<*>, Any> get() = emptyMap()

    val context: ExprContext


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


    fun simplify(node: Node): List<Node> {
        TODO()
    }
}

class SimProcess(
    val results: SortedSet<NodeStatus>,
    val maxDepth: Int = Int.MAX_VALUE,
    var steps: Int = 0, val stepLimit: Int = 1000
) {

    class NodeStatus(val node: Node, val complexity: Int, var processed: Boolean = false)
}


open class BasicExprCal : ExprCal, NodeBuilderScope {

    override val options: MutableMap<TypedKey<*>, Any> = mutableMapOf()

    val reduceRules: TreeDispatcher<SimRule> = TreeDispatcher()

    val transRules: TreeDispatcher<SimRule> = TreeDispatcher()

    var verbose: Verbosity = Verbosity.NONE

    enum class Verbosity {
        NONE, WHEN_APPLIED, ALL
    }

    override val context: ExprContext = BasicExprContext()

    var complexity: NodeComplexity = BasicComplexity


    private val indent = "|  "
    private var simLevel = -1


    init {
        listOf(
            Flatten(Node.Names.ADD),
            Flatten(Node.Names.MUL),
            RuleSort(NodeSig.ADD),
            RuleSort(NodeSig.MUL),
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

    fun addRule(rule: SimRule) {
        val rule = rule.init(this) ?: return
        transRules.register(rule.matcher, rule)
    }


    override fun isCommutative(name: String): Boolean {
        return when (name) {
            Node.Names.ADD -> true
            Node.Names.MUL -> true
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
        if (meta.isEmpty()) return node.plainToString()
        return "${node.plainToString()}, ${node.meta}"
    }


    override fun reduceNode(node: Node, context: ExprContext, depth: Int): Node {

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
            val appliedRule = reduceRules.dispatchUntil(res) { rule ->
                log(Verbosity.ALL) { "|> ${rule.description}" }
                val p = rule.simplify(res, context, this) ?: return@dispatchUntil false

                if (verbose == Verbosity.WHEN_APPLIED) {
                    log(Verbosity.WHEN_APPLIED) { "|>${rule.description}" }
                }
                depth = p.index
                res = p.value
                log(Verbosity.WHEN_APPLIED) { "|->  ${showNode(res)}" }
                true
            }
            if (appliedRule == null) {
                log(Verbosity.ALL) { "|> Nothing happened ..." }
                log(Verbosity.ALL) { "|->  ${showNode(res)}" }
                break
            }
        }

        simLevel--
        return res
    }

    private fun enterContext(node: Node, context: ExprContext): ExprContext {
        return context // TODO
    }

    private fun reduceRecur1(node: Node1, context: ExprContext, depth: Int): Node {
        val ctx = enterContext(node, context)
        val n1 = reduceNode(node.child, ctx, depth)
        if (n1 === node.child) return node
        return node.newWithChildren(n1)
    }

    private fun reduceRecur2(node: Node2, context: ExprContext, depth: Int): Node {
        val n1 = reduceNode(node.first, context, depth)
        val n2 = reduceNode(node.second, context, depth)
        if (n1 === node.first && n2 === node.second) return node
        return node.newWithChildren(n1, n2)
    }

    private fun reduceRecur3(node: Node3, context: ExprContext, depth: Int): Node {
        val n1 = reduceNode(node.first, context, depth)
        val n2 = reduceNode(node.second, context, depth)
        val n3 = reduceNode(node.third, context, depth)
        if (n1 === node.first && n2 === node.second && n3 === node.third) return node
        return node.newWithChildren(n1, n2, n3)
    }

    private fun reduceRecurN(node: NodeN, context: ExprContext, depth: Int): Node {
        var changed = false
        val children = node.children.map { n ->
            reduceNode(n, context, depth).also { if (n !== it) changed = true }
        }
        if (!changed) return node
        return node.newWithChildren(children)
    }


    private fun transRecur1(node: Node1, context: ExprContext, depth: Int): Sequence<Node> {
        val ctx = enterContext(node, context)
        return transNode(node.child, ctx, depth).map {
            if (it === node.child) node else node.newWithChildren(it)
        }
    }

    private fun transRecur2(node: Node2, context: ExprContext, depth: Int): Sequence<Node> {
        val ctx = enterContext(node, context)
        val s1 = transNode(node.first, ctx, depth)
        val s2 = transNode(node.second, ctx, depth)
        return s1.flatMap { f1 ->
            s2.map { f2 ->
                if (f1 === node.first && f2 === node.second) node else node.newWithChildren(f1, f2)
            }
        }
    }

    private fun transRecur3(node: Node3, context: ExprContext, depth: Int): Sequence<Node> {
        val ctx = enterContext(node, context)
        val s1 = transNode(node.first, ctx, depth)
        val s2 = transNode(node.second, ctx, depth)
        val s3 = transNode(node.third, ctx, depth)
        return IterUtils.prod(listOf(s1, s2, s3), copy = false).map { (f1, f2, f3) ->
            if (f1 === node.first && f2 === node.second && f3 === node.third) node else node.newWithChildren(f1, f2, f3)
        }
    }

    private fun transRecurN(node: NodeChilded, context: ExprContext, depth: Int): Sequence<Node> {
        val ctx = enterContext(node, context)
        val seqs = node.children.map { n -> transNode(n, ctx, depth) }
        return IterUtils.prod(seqs, copy = false).map { children ->
            if (children.all2(node.children) { x, y -> x === y }) node else node.newWithChildren(children)
        }
    }


    private fun transNode(node: Node, ctx: ExprContext, depth: Int): Sequence<Node> = sequence {
//        val reduced = reduceNode(node, ctx, 0)
//        if (reduced !== node) yield(reduced)
        val seq = if (depth <= 0) {
            sequenceOf(node)
        } else {
            when (node) {
                is LeafNode -> sequenceOf(node)
                is Node1 -> transRecur1(node, ctx, depth-1)
                is Node2 -> transRecur2(node, ctx, depth-1)
                is Node3 -> transRecur3(node, ctx, depth-1)
                is NodeChilded -> transRecurN(node, ctx, depth-1)
                else -> sequenceOf(node)
            }
        }
        for(n in seq){
            yield(n)
            transRules.dispatchUntil(n){ rule ->
                log(Verbosity.ALL) { "|> ${rule.description}" }
                val p = rule.simplify(n, context, this@BasicExprCal) ?: return@dispatchUntil false

                if (verbose == Verbosity.WHEN_APPLIED) {
                    log(Verbosity.WHEN_APPLIED) { "|>${rule.description}" }
                }
                depth = p.index
                res = p.value
                log(Verbosity.WHEN_APPLIED) { "|->  ${showNode(res)}" }
                true

            }
            val appliedRule = reduceRules.dispatchUntil(res) { rule ->
                log(Verbosity.ALL) { "|> ${rule.description}" }
                val p = rule.simplify(res, context, this) ?: return@dispatchUntil false

                if (verbose == Verbosity.WHEN_APPLIED) {
                    log(Verbosity.WHEN_APPLIED) { "|>${rule.description}" }
                }
                depth = p.index
                res = p.value
                log(Verbosity.WHEN_APPLIED) { "|->  ${showNode(res)}" }
                true
            }
            if (appliedRule == null) {
                log(Verbosity.ALL) { "|> Nothing happened ..." }
                log(Verbosity.ALL) { "|->  ${showNode(res)}" }
                break
            }
        }

    }

    private fun simplifyTo(node: Node, sim: SimProcess, ctx: ExprContext, depth: Int) {
//        val reduced = reduceNode(node, ctx, depth)
//        sim.results += SimProcess.NodeStatus(reduced, complexity.complexity(reduced, ctx))
    }
}


class ExprCalReal : BasicExprCal(), Reals<Node> {

    init {
        options[ExprCal.Options.forceReal] = true

        addAll(RulesTrigonometricReduce())
        addAll(RulesExponentialReduce())

    }

    override fun constantValue(name: String): Node {
        return when (name) {
            "pi", Node.Names.Symbol_PI -> Node.PI
            "e", Node.Names.Symbol_E -> Node.NATURAL_E
            else -> throw IllegalArgumentException("Unknown constant: $name")
        }
    }

    override val one: Node
        get() = Node.ONE

    override val zero: Node
        get() = Node.ZERO


    override fun contains(x: Node): Boolean = true // TODO

    override fun isEqual(x: Node, y: Node): Boolean {
        TODO("Not yet implemented")
    }

    override fun negate(x: Node): Node {
        return reduce(Node.Neg(x), 0)
    }

    override fun add(x: Node, y: Node): Node {
        return reduce(Node.Add(x, y), 0)
    }

    override fun sum(elements: List<Node>): Node {
        return reduce(Node.Add(elements), 0)
    }

    override fun multiply(x: Node, y: Node): Node {
        return reduce(Node.Mul(x, y), 0)
    }

    override fun reciprocal(x: Node): Node {
        return reduce(Node.Inv(x), 0)
    }

    override fun divide(x: Node, y: Node): Node {
        return reduce(Node.Div(x, y), 1)
    }

    override fun compare(o1: Node, o2: Node): Int {
        TODO("Not yet implemented")
    }

    override fun sqrt(x: Node): Node {
        return reduce(Node.Pow(x, Node.HALF), 0)
    }

    override fun exp(x: Node): Node {
        return reduce(Node.Exp(x), 0)
    }

    override fun ln(x: Node): Node {
        return reduce(Node.Ln(x), 0)
    }

    override fun sin(x: Node): Node {
        return reduce(Node.Sin(x), 0)
    }

    override fun cos(x: Node): Node {
        return reduce(Node.Cos(x), 0)
    }

    override fun arcsin(x: Node): Node {
        return reduce(Node.ArcSin(x), 0)
    }

    override fun tan(x: Node): Node {
        return reduce(Node.Tan(x), 0)
    }

    override fun Node.div(y: Node): Node {
        return divide(this, y)
    }

    override fun Node.times(y: Node): Node {
        return multiply(this, y)
    }

    override fun product(elements: List<Node>): Node {
        return reduce(Node.Mul(elements), 0)
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

    override fun exp(base: Node, pow: Node): Node {
        return reduce(Node.Pow(base, pow), 0)
    }

    override fun nroot(x: Node, n: Int): Node {
        return reduce(Node.Pow(x, rational(1, n)), 0)
    }

    override fun log(base: Node, x: Node): Node {
        return reduce(Node.Log(base, x), 0)
    }

    override fun cot(x: Node): Node {
        return reduce(Node.Cot(x), 0)
    }

    override fun arccos(x: Node): Node {
        return reduce(Node.ArcCos(x), 0)
    }

    override fun arctan(x: Node): Node {
        return reduce(Node.ArcTan(x), 0)
    }

    override fun arctan2(y: Node, x: Node): Node {
        TODO()
    }
}