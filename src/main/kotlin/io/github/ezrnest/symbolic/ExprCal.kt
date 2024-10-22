package io.github.ezrnest.symbolic

import io.github.ezrnest.structure.Reals
import io.github.ezrnest.symbolic.sim.RulesExponentialReduce
import io.github.ezrnest.symbolic.sim.RulesTrigonometricReduce
import io.github.ezrnest.symbolic.sim.addAll
import kotlin.math.max


interface ExprCal {

    val options: Map<TypedKey<*>, Any> get() = emptyMap()

    val context: ExprContext

    object Options {
        /**
         * Forces all the computations to be done in the real domain, throwing an ArithmeticException for undefined operations like `sqrt(-1)`.
         */
        val forceReal: TypedKey<Boolean> = TypedKey("forceReal")
    }


    fun isCommutative(name: String): Boolean

    fun simplify(root: Node, depth: Int = Int.MAX_VALUE): Node {
        return simplifyNode(root, context, depth)
    }

    fun simplifyNode(node: Node, context: ExprContext, depth: Int = 0): Node

}


val TestExprContext = BasicExprCal()

open class BasicExprCal : ExprCal, NodeBuilderScope {

    override val options: MutableMap<TypedKey<*>, Any> = mutableMapOf()

    val dispatcher: TreeDispatcher<SimRule> = TreeDispatcher()

    var verbose: Verbosity = Verbosity.NONE

    override val context: ExprContext = BasicExprContext()

    enum class Verbosity {
        NONE, WHEN_APPLIED, ALL
    }


    private val indent = "|  "
    private var simLevel = -1


    val rules: List<SimRule> = listOf(
//        RegularizeNodeN(Node.Names.ADD),
//        RegularizeNodeN(Node.Names.MUL),
        Flatten(Node.Names.ADD),
        Flatten(Node.Names.MUL),
        RuleSort(NodeSig.ADD),
        RuleSort(NodeSig.MUL),
        MergeAdditionRational,
//        ComputeProductRational,
        MergeProduct,
        ComputePow,
        FlattenPow
    )

    init {
        for (r in rules) {
            dispatcher.register(r.matcher, r)
        }
    }

    fun addReduceRule(rule: SimRule) {
        val rule = rule.init(this) ?: rule
        dispatcher.register(rule.matcher, rule)
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


    override fun simplifyNode(node: Node, context: ExprContext, depth: Int): Node {

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
                    is Node1 -> simplifyRecur1(res, context, depth - 1)
                    is Node2 -> simplifyRecur2(res, context, depth - 1)
                    is Node3 -> simplifyRecur3(res, context, depth - 1)
                    is NodeN -> simplifyRecurN(res, context, depth - 1)
                    else -> res
                }
            }
            val appliedRule = dispatcher.dispatchUntil(res) { rule ->
                log(Verbosity.ALL) { "|> ${rule.description}" }
                val p = rule.simplify(res, context, this) ?: return@dispatchUntil false

                if (verbose == Verbosity.WHEN_APPLIED) {
                    log(Verbosity.WHEN_APPLIED) { "|>${rule.description}" }
                }
                depth = p.level
                res = p.item
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


    private fun simplifyRecur1(node: Node1, context: ExprContext, depth: Int): Node {
        val n1 = simplifyNode(node.child, context, depth)
        if (n1 === node.child) return node
        return node.newWithChildren(n1)
    }

    private fun simplifyRecur2(node: Node2, context: ExprContext, depth: Int): Node {
        val n1 = simplifyNode(node.first, context, depth)
        val n2 = simplifyNode(node.second, context, depth)
        if (n1 === node.first && n2 === node.second) return node
        return node.newWithChildren(n1, n2)
    }

    private fun simplifyRecur3(node: Node3, context: ExprContext, depth: Int): Node {
        val n1 = simplifyNode(node.first, context, depth)
        val n2 = simplifyNode(node.second, context, depth)
        val n3 = simplifyNode(node.third, context, depth)
        if (n1 === node.first && n2 === node.second && n3 === node.third) return node
        return node.newWithChildren(n1, n2, n3)
    }

    private fun simplifyRecurN(node: NodeN, context: ExprContext, depth: Int): Node {
        var changed = false
        val children = node.children.map { n ->
            simplifyNode(n, context, depth).also { if (n !== it) changed = true }
        }
        if (!changed) return node
        return node.newWithChildren(children)
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
        return simplify(Node.Neg(x), 0)
    }

    override fun add(x: Node, y: Node): Node {
        return simplify(Node.Add(x, y), 0)
    }

    override fun sum(elements: List<Node>): Node {
        return simplify(Node.Add(elements), 0)
    }

    override fun multiply(x: Node, y: Node): Node {
        return simplify(Node.Mul(x, y), 0)
    }

    override fun reciprocal(x: Node): Node {
        return simplify(Node.Inv(x), 0)
    }

    override fun divide(x: Node, y: Node): Node {
        return simplify(Node.Div(x, y), 1)
    }

    override fun compare(o1: Node, o2: Node): Int {
        TODO("Not yet implemented")
    }

    override fun sqrt(x: Node): Node {
        return simplify(Node.Pow(x, Node.HALF), 0)
    }

    override fun exp(x: Node): Node {
        return simplify(Node.Exp(x), 0)
    }

    override fun ln(x: Node): Node {
        return simplify(Node.Ln(x), 0)
    }

    override fun sin(x: Node): Node {
        return simplify(Node.Sin(x), 0)
    }

    override fun cos(x: Node): Node {
        return simplify(Node.Cos(x), 0)
    }

    override fun arcsin(x: Node): Node {
        return simplify(Node.ArcSin(x), 0)
    }

    override fun tan(x: Node): Node {
        return simplify(Node.Tan(x), 0)
    }

    override fun Node.div(y: Node): Node {
        return divide(this, y)
    }

    override fun Node.times(y: Node): Node {
        return multiply(this, y)
    }

    override fun product(elements: List<Node>): Node {
        return simplify(Node.Mul(elements), 0)
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
        return simplify(Node.Pow(base, pow), 0)
    }

    override fun nroot(x: Node, n: Int): Node {
        return simplify(Node.Pow(x, rational(1, n)), 0)
    }

    override fun log(base: Node, x: Node): Node {
        return simplify(Node.Log(base, x), 0)
    }

    override fun cot(x: Node): Node {
        return simplify(Node.Cot(x), 0)
    }

    override fun arccos(x: Node): Node {
        return simplify(Node.ArcCos(x), 0)
    }

    override fun arctan(x: Node): Node {
        return simplify(Node.ArcTan(x), 0)
    }

    override fun arctan2(y: Node, x: Node): Node {
        TODO()
    }
}