package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.model.isOdd
import io.github.ezrnest.mathsymk.symbolic.Node.Names
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg
import io.github.ezrnest.mathsymk.util.IterUtils
import io.github.ezrnest.mathsymk.util.WithInt
import io.github.ezrnest.mathsymk.util.all2
import java.math.BigInteger
import kotlin.math.absoluteValue

// created at 2024/10/1
interface SimRule : TransRule {
    override val description: String

    /**
     * The key for marking the node as tried by the rule but not applicable.
     * This can be used to avoid trying the same rule again.
     */
    override val metaKeyApplied: TypedKey<Boolean>

    fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>?

    override fun transform(node: Node, ctx: ExprContext, cal: ExprCal): List<WithInt<Node>> {
        val res = simplify(node, ctx, cal) ?: return emptyList()
        return listOf(res)
    }

    override val matcher: NodeMatcherT<Node>
        get() = AnyMatcher


    override fun init(context: ExprCal): SimRule? {
        return this
    }
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


    override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        if (node !is NodeChilded) return null
        if (!cal.isCommutative(node.name)) return null
        return when (node) {
            is Node1 -> null
            is Node2 -> sort2(node)?.let { WithInt(0, it) }
            else -> sortN(node, ctx)?.let { WithInt(0, it) }
        }
    }
}


abstract class RuleForSpecificName(val targetName: String) : SimRule {
    protected inline fun <reified T : Node> simplifyNodeTyped(
        node: Node, nextSimplification: (T) -> WithInt<Node>?
    ): WithInt<Node>? {
        if (node.name != targetName || node !is T || node[metaKeyApplied] == true)
            return null
        val res = nextSimplification(node)
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

    final override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        return simplifyNodeTyped<NodeN>(node) { n -> simplifyN(n, ctx, cal) }
    }

    abstract fun simplifyN(root: NodeN, context: ExprContext, cal: ExprCal): WithInt<Node>?
}


class Flatten(targetName: String) : RuleForSpecificN(targetName) {
    // created at 2024/10/01
    override val description: String = "Flatten $targetName"
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Flatten${targetName}")

    override fun simplifyN(root: NodeN, context: ExprContext, cal: ExprCal): WithInt<Node>? {
        val children = root.children
        if (children.size == 1) return WithInt(0, children[0])
        if (children.all { !(it is NodeN && it.name == targetName) }) return null
        val newChildren = children.flatMap {
            if (it is NodeN && it.name == targetName) it.children else listOf(it)
        }
        val res = Node.NodeN(targetName, newChildren).also { it[metaKeyApplied] = true }
        return WithInt(0, res)
    }
}

/**
 * ```
 * a + a -> 2a
 * ```
 */
object MergeAdditionRational : RuleForSpecificN(SymAlg.Names.ADD) {
    // created at 2024/10/05


    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Merge+")


    override val description: String
        get() = "Merge addition rational"

    override fun simplifyN(root: NodeN, context: ExprContext, cal: ExprCal): WithInt<Node>? {
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
        if (collect.isEmpty()) return WithInt(-1, SymAlg.ZERO)
        if (collect.size == 1) {
            val (n, r) = collect.entries.first()
            return WithInt(0, SimUtils.createWithRational(r, n))
        }

        val newChildren = collect.entries.map { (n, r) -> SimUtils.createWithRational(r, n) }
        return WithInt(0, SymAlg.Add(newChildren))

    }

}

/**
 * ```
 * x * x -> x^2
 * 1 * x * 2 -> 2x
 * ```
 */
object MergeProduct : RuleForSpecificN(SymAlg.Names.MUL) {
    // created at 2024/10/05

    override val description: String
        get() = "Merge product"

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Merge*")


    private fun getBase(node: Node): Node {
        if (node is Node2 && node.name == SymAlg.Names.POW) {
            return node.first
        }
        return node
    }

    private fun getPower(node: Node): Node {
        if (node is Node2 && node.name == SymAlg.Names.POW) {
            return node.second
        }
        return SymAlg.ONE
    }

    private fun buildPower(base: Node, nodeList: List<Node>, context: ExprContext, cal: ExprCal): Node {
        if (nodeList.size == 1) return nodeList[0] // not merged
        var exp = SymAlg.Add(nodeList.map { getPower(it) })
        exp = cal.reduceNode(exp, context, 0)
        if (exp == SymAlg.ONE) return base
        val res = SymAlg.Pow(base, exp)
        return cal.reduceNode(res, context, 0)
    }

    private fun simMulZero(collect: Map<Node, List<Node>>, context: ExprContext): WithInt<Node> {
        // possible further check for undefined or infinity
        return WithInt(-1, SymAlg.ZERO)
    }


    override fun simplifyN(root: NodeN, context: ExprContext, cal: ExprCal): WithInt<Node>? {
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
            val base = getBase(node)
            val t = collect[base]
            if (t == null) {
                collect[base] = listOf(node)
            } else {
                simplified = true
                collect[base] = t + node
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
        if (addRational) newChildren.add(SymAlg.Rational(rPart))
        collect.entries.mapTo(newChildren) { (base, nodeList) -> buildPower(base, nodeList, context, cal) }
        return WithInt(0, SymAlg.Mul(newChildren))
        // need simplification by the rule again since the power may be added and simplified
    }
}


/**
 * ```
 * 1 * 2 * 3 -> 6
 * 1 * x -> x
 * ```
 */
object ComputeProductRational : RuleForSpecificN(SymAlg.Names.MUL) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Compute*")

    override val description: String
        get() = "Compute product"


    override fun simplifyN(root: NodeN, context: ExprContext, cal: ExprCal): WithInt<Node>? {
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
        if (Q.isZero(product)) return WithInt(-1, SymAlg.ZERO)
        if (nodes.isEmpty()) return WithInt(-1, SymAlg.Rational(product)) // only rational
        if (count == 1 && !Q.isOne(product)) return null // only one rational that can't be simplified
        if (!Q.isOne(product)) {
            nodes.add(SymAlg.Rational(product))
        }
        return SymAlg.Mul(nodes).also { it[metaKeyApplied] = true }.let { WithInt(0, it) }
    }
}

abstract class RuleForSpecific1(targetName: String) : RuleForSpecificName(targetName) {
    final override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig.forNode1(targetName)

    final override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        return simplifyNodeTyped<Node1>(node) { n -> simplify1(n, ctx, cal) }
    }

    protected abstract fun simplify1(root: Node1, context: ExprContext, cal: ExprCal): WithInt<Node>?
}

abstract class RuleForSpecific2(targetName: String) : RuleForSpecificName(targetName) {
    final override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig.forNode2(targetName)

    final override fun simplify(node: Node, ctx: ExprContext, cal: ExprCal): WithInt<Node>? {
        return simplifyNodeTyped<Node2>(node) { n -> simplify2(n, ctx, cal) }
    }

    protected abstract fun simplify2(root: Node2, context: ExprContext, cal: ExprCal): WithInt<Node>?
}

/**
 * ```
 * exp(exp(x,2),3) -> exp(x,6)
 * ```
 */
object FlattenPow : RuleForSpecific2(SymAlg.Names.POW) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Flatten^")

    override val description: String
        get() = "Flatten pow"

    /**
     * Flatten `exp(base = pow(b0, e0), exp) = pow(b0, e0*exp)`
     */
    private fun flattenPowPow(base: Node2, exp: Node): Node {
        val (baseBase, baseExp) = base
        val newExp = SymAlg.Mul(listOf(baseExp, exp))
        return SymAlg.Pow(baseBase, newExp)
    }

    private fun flattenPowInt(base: Node, exp: NRational, context: ExprContext): WithInt<Node>? {
        if (BigFracAsQuot.isOne(exp.value)) return WithInt(0, base)
        if (SimUtils.isPow(base)) {
            return WithInt(0, flattenPowPow(base, exp))
        }
        if (SimUtils.isMul(base)) {
            val children = base.children
            val newChildren = children.map {
                if (SimUtils.isPow(it)) {
                    flattenPowPow(it, exp)
                } else {
                    SymAlg.Pow(it, exp)
                }
            }
            return WithInt(2, SymAlg.Mul(newChildren))
        }
        return null
    }

    override fun simplify2(root: Node2, context: ExprContext, cal: ExprCal): WithInt<Node>? {
        val (base, exp) = root
        if (SimUtils.isInteger(exp, context)) {
            return flattenPowInt(base, exp, context)
        }
        // TODO rational power
        return null
    }
}

/**
 * ```
 * pow(r, n) -> r^n
 * pow(r, p/q) -> pow(r^p, 1/q)
 * ```
 */
object ComputePow : RuleForSpecific2(SymAlg.Names.POW) {
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
    private fun computeNRootMinus1(exp: BigInteger, context: ExprContext, cal: ExprCal): Node {
        if (exp == BigInteger.ONE) return SymAlg.NEG_ONE
        if (cal.options[ExprCal.Options.forceReal] == true) throw ArithmeticException(
            "Cannot compute the value of (-1)^(1/n) in the real mode"
        )

        if (exp == BigInteger.TWO) return SymAlg.IMAGINARY_UNIT
        return buildNode {
            val piOverN = pi / exp.e
            val cos = cos(piOverN)
            val sin = sin(piOverN)
            cos + 𝑖 * sin // let the simplification handle the rest
        }

    }

    private fun canExpandPow(base: BigInteger, pow: BigInteger): Boolean {
        if (pow.bitLength() > 31) return false
        val powInt = pow.intValueExact().absoluteValue
        val length = Math.multiplyFull(base.bitLength(), powInt)
        return length <= MAX_BIT_LENGTH
    }


    private fun powRational(base: BigFrac, exp: BigFrac, context: ExprContext, cal: ExprCal): Node {
        with(BigFracAsQuot) {
            if (isInteger(exp)) {
                val p = asInteger(exp)
                if (canExpandPow(base.nume, p) && canExpandPow(base.deno, p)) {
                    val res = power(base, p)
                    return SymAlg.Rational(res)
                }
                // power too big
            }
            if (isOne(base)) return SymAlg.ONE
            if (isZero(base)) {
                if (isZero(exp)) return Node.UNDEFINED
                return SymAlg.ZERO
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
                        val node = SymAlg.Pow(SymAlg.Int(b), SymAlg.Int(e1))
                        node[metaInfoKey] = true
                        nodes.add(node)
                    }
                }else{
                    val p = SymAlg.Pow(SymAlg.Int(b), SymAlg.Rational(e))
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
                        val node = SymAlg.Pow(SymAlg.Int(b), SymAlg.Int(floor))
                        node[metaKeyApplied] = true
                        node[NodeMetas.rational] = true
                        node[NodeMetas.positive] = true
                        nodes.add(node)
                        // power too big, cannot compute the exact value
                    }
                }
                if (!isZero(rem)) {
                    val p = SymAlg.Pow(SymAlg.Int(b), SymAlg.Rational(rem))
                    p[metaKeyApplied] = true
                    nodes.add(p)
                }
            }
            if (isNegative(base) && exp.nume.isOdd()) {
                if (exp.deno.isOdd()) {
                    rPart = -rPart
                } else {
                    val p = computeNRootMinus1(exp.deno, context, cal)
                    nodes.add(p)
                }
            }
            if (!isOne(rPart)) {
                nodes.add(SymAlg.Rational(rPart))
            }
            return SymAlg.Mul(nodes)
        }
    }

//    private fun powFactorDecomposition(base: BigFrac, exp: Node, context: ExprContext): Node {
//        TODO()
//    }

    override fun simplify2(root: Node2, context: ExprContext, cal: ExprCal): WithInt<Node>? {
        val (base, exp) = root
        if (base !is NRational) return null
        if (exp is NRational) return WithInt(Int.MAX_VALUE, powRational(base.value, exp.value, context, cal))
        return null
    }
}


object RuleExpandMul : RuleForSpecificN(SymAlg.Names.MUL) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Expand*")

    override val description: String
        get() = "Expand multiplication"

    private val defaultExpansionLimit = 100


    private fun asFactorSize(node: Node): Int {
        if (SimUtils.isAdd(node)) {
            return node.children.size
        }
        return 1
    }

    private fun asFactor(node: Node): List<Node> {
        if (SimUtils.isAdd(node)) {
            return node.children
        }
        return listOf(node)
    }

    override fun simplifyN(root: NodeN, context: ExprContext, cal: ExprCal): WithInt<Node>? {
        val children = root.children
        val resultSize = children.fold(1) { s, n -> s * asFactorSize(n) }
        if (resultSize == 1 || resultSize >= defaultExpansionLimit) return null
        val newChildren = ArrayList<Node>(resultSize)
        IterUtils.prod(children.map { asFactor(it) }).forEach { newChildren.add(SymAlg.Mul(it)) }
        val res = SymAlg.Add(newChildren)
        return WithInt(1, res)
    }
}