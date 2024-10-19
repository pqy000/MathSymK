package io.github.ezrnest.symbolic

import io.github.ezrnest.model.BigFrac
import io.github.ezrnest.model.BigFracAsQuot
import io.github.ezrnest.model.isOdd
import io.github.ezrnest.symbolic.Node.Names
import io.github.ezrnest.util.all2
import java.math.BigInteger
import kotlin.math.absoluteValue

// created at 2024/10/1

interface SimRule {
    val description: String

    /**
     * The key for marking the node as tried by the rule but not applicable.
     * This can be used to avoid trying the same rule again.
     */
    val metaKeyApplied: TypedKey<Boolean>

    fun simplify(node: Node, context: ExprContext): WithLevel<Node>?

    val matcher: NodeMatcherT<Node>
        get() = AnyMatcher
}

@JvmRecord
data class WithLevel<out D>(val level: Int, val item: D) {
}


class RuleSort(val targetSig: NodeSig) : SimRule {

    override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig(targetSig)

    override val description: String = "Sort"

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("sorted")

    private fun sort2(node: Node2): Node2? {
        val (first, second) = node
        if (NodeOrder.compare(first, second) <= 0) {
            node[metaKeyApplied] = true
            return null
        }
        return node.newWithChildren(second, first).also { it[metaKeyApplied] = true }
    }

    private fun sortN(node: NodeChilded, context: ExprContext): NodeChilded? {
        val children = node.children
        val childrenSorted = children.sortedWith(NodeOrder)
        if (children.all2(childrenSorted) { x, y -> x === y }) {
            node[metaKeyApplied] = true
            return null
        }
        return node.newWithChildren(childrenSorted).also { it[metaKeyApplied] = true }
    }


    override fun simplify(node: Node, context: ExprContext): WithLevel<Node>? {
        if (node !is NodeChilded) return null
        if (!context.isCommutative(node.name)) return null
        return when (node) {
            is Node1 -> null
            is Node2 -> sort2(node)?.let { WithLevel(0, it) }
            else -> sortN(node, context)?.let { WithLevel(0, it) }
        }
    }
}


abstract class RuleForSpecificName(val targetName: String) : SimRule {
    protected inline fun <reified T : Node> simplifyNodeTyped(
        node: Node, context: ExprContext, nextSimplification: (T, ExprContext) -> WithLevel<Node>?
    ): WithLevel<Node>? {
        if (node.name != targetName || node !is T || node[metaKeyApplied] == true)
            return null
        val res = nextSimplification(node, context)
        if (res != null) return res
        node[metaKeyApplied] = true // tried but not applicable
        return null
    }
}


//class RegularizeNodeN(targetName: String) : RuleForSpecificName(targetName) {
//    override val description: String = "Regularize $targetName"
//
//    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Reg[$targetName]")
//
//    override fun simplify(node: Node, context: ExprContext): Node? {
//        if (node.name != targetName || node !is NodeChilded || node is NodeN)
//            return null
//        return Node.NodeN(targetName, node.children)
//        // no need for the metaKeyNotApplicable since it is always applicable
//    }
//}


abstract class RuleForSpecificN(targetName: String) : RuleForSpecificName(targetName) {

    final override val matcher: NodeMatcherT<NodeN> = LeafMatcherFixSig.forNodeN(targetName)

    final override fun simplify(node: Node, context: ExprContext): WithLevel<Node>? {
        return simplifyNodeTyped<NodeN>(node, context) { n, c -> simplifyN(n, c) }
    }

    abstract fun simplifyN(root: NodeN, context: ExprContext): WithLevel<Node>?
}


class Flatten(targetName: String) : RuleForSpecificN(targetName) {
    // created at 2024/10/01
    override val description: String = "Flatten $targetName"
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Flatten${targetName}")

    override fun simplifyN(root: NodeN, context: ExprContext): WithLevel<Node>? {
        val children = root.children
        if (children.size == 1) return WithLevel(0, children[0])
        if (children.all { !(it is NodeN && it.name == targetName) }) return null
        val newChildren = children.flatMap {
            if (it is NodeN && it.name == targetName) it.children else listOf(it)
        }
        val res = Node.NodeN(targetName, newChildren)
        return WithLevel(0, res)
    }
}

/**
 * ```
 * a + a -> 2a
 * ```
 */
object MergeAdditionRational : RuleForSpecificN(Names.ADD) {
    // created at 2024/10/05


    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Merge+")


    override val description: String
        get() = "Merge addition rational"

    override fun simplifyN(root: NodeN, context: ExprContext): WithLevel<Node>? {
        val children = root.children
        val Q = BigFracAsQuot
        val collect = sortedMapOf<Node, BigFrac>(NodeOrder)
        var simplified = false
        for (node in children) {
            val (r, n) = SimUtils.extractRational(node, context)
            val t = collect[n]
            if (t == null) {
                collect[n] = r
            } else {
                simplified = true
                val newR = Q.add(t, r)
                if (Q.isZero(newR)) {
                    collect.remove(n)
                } else {
                    collect[n] = newR
                }
            }
        }
        if (!simplified) return null
        if (collect.isEmpty()) return WithLevel(-1, Node.ZERO)
        if (collect.size == 1) {
            val (n, r) = collect.entries.first()
            return WithLevel(0, SimUtils.createWithRational(r, n))
        }

        val newChildren = collect.entries.map { (n, r) -> SimUtils.createWithRational(r, n) }
        return WithLevel(0, Node.Add(newChildren))

    }

}

/**
 * ```
 * x * x -> x^2
 * 1 * x * 2 -> 2x
 * ```
 */
object MergeProduct : RuleForSpecificN(Names.MUL) {
    // created at 2024/10/05

    override val description: String
        get() = "Merge product"

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Merge*")

    private fun buildPower(base: Node, expList: List<Node>, context: ExprContext): Node {
        var exp = if (expList.size == 1) {
            expList[0]
        } else {
            Node.Add(expList)
        }
        exp = context.simplifyNode(exp, 0)
        if (exp == Node.ONE) return base
//        val res = if (base == Node.NATURAL_E) Node.Exp(exp) else Node.Pow(base, exp)
        val res = Node.Pow(base, exp)
        return context.simplifyNode(res, 0)
    }

    private fun simMulZero(collect: Map<Node, List<Node>>, context: ExprContext): WithLevel<Node> {
        // possible further check for undefined or infinity
        return WithLevel(-1, Node.ZERO)
    }

    override fun simplifyN(root: NodeN, context: ExprContext): WithLevel<Node>? {
        val children = root.children
        val collect = sortedMapOf<Node, List<Node>>(NodeOrder)
        val Q = BigFracAsQuot
        var rPart = Q.one
        var rationalCount = 0
        var simplified = false
        for (node in children) {
            if (node is NRational) {
                // do not merge rational into power
                rationalCount++
                rPart = Q.multiply(rPart, node.value)
                continue
            }
            val (base, exp) = SimUtils.toPower(node)
            val t = collect[base]
            if (t == null) {
                collect[base] = listOf(exp)
            } else {
                simplified = true
                collect[base] = t + exp
            }
        }
        if (rationalCount > 0 && Q.isZero(rPart)) {
            return simMulZero(collect, context) // special case for 0
        }
        if (rationalCount >= 2 || (rationalCount == 1 && Q.isOne(rPart))) {
            // rational part is either merged or removed
            simplified = true
        }
        if (!simplified) return null

        val addRational = rationalCount > 0 && !Q.isOne(rPart)
        val newChildren = ArrayList<Node>(collect.size + if (addRational) 1 else 0)
        if (addRational) newChildren.add(Node.Rational(rPart))
        collect.entries.mapTo(newChildren) { (base, expList) -> buildPower(base, expList, context) }
        return WithLevel(0, Node.Mul(newChildren))
        // need simplification by the rule again since the power may be added and simplified
    }
}


/**
 * ```
 * 1 * 2 * 3 -> 6
 * 1 * x -> x
 * ```
 */
object ComputeProductRational : RuleForSpecificN(Names.MUL) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Compute*")

    override val description: String
        get() = "Compute product"


    override fun simplifyN(root: NodeN, context: ExprContext): WithLevel<Node>? {
        val children = root.children
        val Q = BigFracAsQuot
        var product = Q.one
        val nodes = ArrayList<Node>(children.size)
        var count = 0
        for (node in children) {
            val r = SimUtils.asRational(node, Q)
            if (r == null) {
                nodes.add(node)
            } else {
                count++
                product = Q.multiply(product, r)
            }
        }
        if (count == 0) return null  // no rational to compute
        if (Q.isZero(product)) return WithLevel(-1, Node.ZERO)
        if (nodes.isEmpty()) return WithLevel(-1, Node.Rational(product)) // only rational
        if (count == 1 && !Q.isOne(product)) return null // only one rational that can't be simplified
        if (!Q.isOne(product)) {
            nodes.add(Node.Rational(product))
        }
        return Node.Mul(nodes).also { it[metaKeyApplied] = true }.let { WithLevel(0, it) }
    }
}

abstract class RuleForSpecific1(targetName: String) : RuleForSpecificName(targetName) {
    final override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig.forNode1(targetName)

    final override fun simplify(node: Node, context: ExprContext): WithLevel<Node>? {
        return simplifyNodeTyped<Node1>(node, context) { n, c -> simplify1(n, c) }
    }

    protected abstract fun simplify1(root: Node1, context: ExprContext): WithLevel<Node>?
}

abstract class RuleForSpecific2(targetName: String) : RuleForSpecificName(targetName) {
    final override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig.forNode2(targetName)

    final override fun simplify(node: Node, context: ExprContext): WithLevel<Node>? {
        return simplifyNodeTyped<Node2>(node, context) { n, c -> simplify2(n, c) }
    }

    protected abstract fun simplify2(root: Node2, context: ExprContext): WithLevel<Node>?
}

/**
 * ```
 * exp(exp(x,2),3) -> exp(x,6)
 * ```
 */
object FlattenPow : RuleForSpecific2(Names.POW) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Flatten^")

    override val description: String
        get() = "Flatten pow"

    /**
     * Flatten `exp(base = pow(b0, e0), exp) = pow(b0, e0*exp)`
     */
    private fun flattenPowPow(base: Node2, exp: Node): Node {
        val (baseBase, baseExp) = base
        val newExp = Node.Mul(listOf(baseExp, exp))
        return Node.Pow(baseBase, newExp)
    }

    private fun flattenPowInt(base: Node, exp: NRational, context: ExprContext): WithLevel<Node>? {
        if (BigFracAsQuot.isOne(exp.value)) return WithLevel(0, base)
        if (SimUtils.isPow(base)) {
            return WithLevel(0, flattenPowPow(base, exp))
        }
        if (SimUtils.isMul(base)) {
            val children = base.children
            val newChildren = children.map {
                if (SimUtils.isPow(it)) {
                    flattenPowPow(it, exp)
                } else {
                    Node.Pow(it, exp)
                }
            }
            return WithLevel(2, Node.Mul(newChildren))
        }
        return null
    }

    override fun simplify2(root: Node2, context: ExprContext): WithLevel<Node>? {
        val (base, exp) = root
        if (SimUtils.isInteger(exp, context)) {
            return flattenPowInt(base, exp, context)
        }
        return null
        // TODO
    }
}

/**
 * ```
 * pow(r, n) -> r^n
 * pow(r, p/q) -> pow(r^p, 1/q)
 * ```
 */
object ComputePow : RuleForSpecific2(Names.POW) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Compute^")

    override val description: String
        get() = "Compute pow"

    val MAX_BIT_LENGTH = 1_000_000

    /**
     * Computes:
     * ```
     * (-1)^(1/n) -> exp(pi*i/n) = cos(pi/n) + i*sin(pi/n)
     * ```
     */
    private fun computeNRootMinus1(exp: BigInteger, context: ExprContext): Node {
        if (exp == BigInteger.ONE) return Node.NEG_ONE
        if (context.options[ExprContext.Options.forceReal] == true) throw ArithmeticException(
            "Cannot compute the value of (-1)^(1/n) in the real mode"
        )

        if (exp == BigInteger.TWO) return Node.IMAGINARY_UNIT
        val piOverN = Node.Div(Node.PI, Node.Int(exp))
        val cos = Node.Cos(piOverN)
        val sin = Node.Sin(piOverN)
        val res = Node.Add(cos, Node.Mul(Node.IMAGINARY_UNIT, sin))
        return res // let the simplification handle the rest
    }

    private fun canExpandPow(base: BigInteger, pow: BigInteger): Boolean {
        if (pow.bitLength() > 31) return false
        val powInt = pow.intValueExact().absoluteValue
        val length = Math.multiplyFull(base.bitLength(), powInt)
        return length <= MAX_BIT_LENGTH
    }


    private fun powRational(base: BigFrac, exp: BigFrac, context: ExprContext): Node {
        with(BigFracAsQuot) {
            if (isInteger(exp)) {
                val p = asInteger(exp)
                if (canExpandPow(base.nume, p) && canExpandPow(base.deno, p)) {
                    val res = power(base, p)
                    return Node.Rational(res)
                }
                // power too big
            }
            if (isOne(base)) return Node.ONE
            if (isZero(base)) {
                if (isZero(exp)) return Node.UNDEFINED
                return Node.ZERO
            }
            val factorPow = factorizedPow(abs(base), exp)
            var rPart = one
            val nodes = ArrayList<Node>(factorPow.size)
            for ((b, e) in factorPow) {
                /* No expansion:
                if (isInteger(e)) {
                    val e1 = asInteger(e)
                    if (canExpandPow(b, e1)) {
                        val eInt = e1.intValueExact()
                        if (eInt < 0) {
                            rPart /= integers.power(b, -eInt).bfrac
                        } else {
                            rPart *= integers.power(b, eInt).bfrac
                        }
                    }else{
                        val node = Node.Pow(Node.Int(b), Node.Int(e1))
                        node[metaInfoKey] = true
                        nodes.add(node)
                    }
                }else{
                    val p = Node.Pow(Node.Int(b), Node.Rational(e))
                    p[metaInfoKey] = true
                    nodes.add(p)
                }
                 */

                val (floor, rem) = floorAndRem(e)
                if (floor.signum() != 0) {
                    if (canExpandPow(b, floor)) {
                        val floorInt = floor.intValueExact()
                        if (floorInt < 0) {
                            rPart /= integers.power(b, -floorInt).bfrac
                        } else {
                            rPart *= integers.power(b, floorInt).bfrac
                        }
                    } else {
                        val node = Node.Pow(Node.Int(b), Node.Int(floor))
                        node[metaKeyApplied] = true
                        node[NodeMetas.rational] = true
                        node[NodeMetas.positive] = true
                        nodes.add(node)
                        // power too big, cannot compute the exact value
                    }
                }
                if (!isZero(rem)) {
                    val p = Node.Pow(Node.Int(b), Node.Rational(rem))
                    p[metaKeyApplied] = true
                    nodes.add(p)
                }
            }
            if (isNegative(base) && exp.nume.isOdd()) {
                if (exp.deno.isOdd()) {
                    rPart = -rPart
                } else {
                    val p = computeNRootMinus1(exp.deno, context)
                    nodes.add(p)
                }
            }
            if (!isOne(rPart)) {
                nodes.add(Node.Rational(rPart))
            }
            return Node.Mul(nodes)
        }
    }

    private fun powFactorDecomposition(base: BigFrac, exp: Node, context: ExprContext): Node {
        TODO()
    }

    override fun simplify2(root: Node2, context: ExprContext): WithLevel<Node>? {
        val (base, exp) = root
        if (base !is NRational) return null
        if (exp is NRational) return WithLevel(Int.MAX_VALUE, powRational(base.value, exp.value, context))
        return null
    }
}


interface SimRuleMatched<T : Node> : SimRule {


    /**
     * A matcher describing the nodes that the rule can be applied to.
     */
    override val matcher: NodeMatcherT<T>

    /**
     * Simplify the matched node.
     */
    fun simplifyMatched(node: T, matchContext: MatchContext): WithLevel<Node>?


    override fun simplify(node: Node, context: ExprContext): WithLevel<Node>? {
        if(node[metaKeyApplied] == true) return null
        val matchContext = MutableMatchContext(context)
        val r = matcher.matches(node, matchContext) ?: return null
        val res= simplifyMatched(r, matchContext)
        if(res != null) return res
        node[metaKeyApplied] = true // tried but not applicable
        return null
    }
}

interface ReplacementBuilderScope : NodeBuilderScope {

    val matchContext: MatchContext

    override val context: ExprContext
        get() = matchContext.exprContext

    fun ref(name: String): Node {
        return matchContext.refMap[name] ?: throw IllegalArgumentException("No reference found for $name")
    }

    fun hasRef(name: String): Boolean {
        return matchContext.refMap.containsKey(name)
    }

    override val x: Node get() = ref("x")
    override val y: Node get() = ref("y")
    override val z: Node get() = ref("z")
    override val a: Node get() = ref("a")
    override val b: Node get() = ref("b")
    override val c: Node get() = ref("c")

    val String.ref get() = ref(this)

    companion object {
        private class ReplacementBuilderScopeImpl(override val matchContext: MatchContext) : ReplacementBuilderScope

        fun create(matchContext: MatchContext): ReplacementBuilderScope = ReplacementBuilderScopeImpl(matchContext)
    }
}
typealias RepBuilder = ReplacementBuilderScope.() -> Node

class MatcherReplaceRule(
    override val matcher: NodeMatcherT<*>,
    val replacement: RepBuilder,
    override val description: String,
    val afterDepth: Int = Int.MAX_VALUE
) : SimRule {

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey(description)

    override fun simplify(node: Node, context: ExprContext): WithLevel<Node>? {
        val matchCtx = MutableMatchContext(context)
        matcher.matches(node, matchCtx) ?: return null
        val replacementNode = ReplacementBuilderScope.create(matchCtx).replacement()
        return WithLevel(afterDepth, replacementNode)
    }

}