package io.github.ezrnest.symbolic

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
    val metaKeyNotApplicable: TypedKey<Boolean>

    fun simplify(node: Node, context: ExprContext): Node?
}

object RuleSort : SimRule {
    override val description: String = "Sort"

    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("sorted")

    private fun sort2(node: Node2, context: ExprContext): Node2? {
        val (first, second) = node
        if (context.nodeOrder.compare(first, second) <= 0) {
            node[metaKeyNotApplicable] = true
            return null
        }
        return Node.Node2(node.name, second, first).also { it[metaKeyNotApplicable] = true }
    }

    private fun sortN(node: NodeChilded, context: ExprContext): NodeChilded? {
        val children = node.children
        val childrenSorted = children.sortedWith(context.nodeOrder)
        if (children.all2(childrenSorted) { x, y -> x === y }) {
            node[metaKeyNotApplicable] = true
            return null
        }
        return node.newWithChildren(childrenSorted).also { it[metaKeyNotApplicable] = true }
    }


    override fun simplify(node: Node, context: ExprContext): Node? {
        if (node !is NodeChilded) return null
        if (!context.isCommutative(node.name)) return null
        return when (node) {
            is Node1 -> null
            is Node2 -> sort2(node, context)
            else -> sortN(node, context)
        }
    }

}

abstract class RuleForSpecificName(val targetName: String) : SimRule {
    protected inline fun <reified T : Node> simplifyNodeTyped(
        node: Node, context: ExprContext, nextSimplification: (T, ExprContext) -> Node?
    ): Node? {
        if (node.name != targetName || node !is T || node[metaKeyNotApplicable] == true)
            return null
        val res = nextSimplification(node, context)
        if (res != null) return res
        node[metaKeyNotApplicable] = true // tried but not applicable
        return null
    }
}


class RegularizeNodeN(targetName: String) : RuleForSpecificName(targetName) {
    override val description: String = "Regularize $targetName"

    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Reg[$targetName]")

    override fun simplify(node: Node, context: ExprContext): Node? {
        if (node.name != targetName || node !is NodeChilded || node is NodeN)
            return null
        return Node.NodeN(targetName, node.children)
        // no need for the metaKeyNotApplicable since it is always applicable
    }
}


abstract class RuleForSpecificN(targetName: String) : RuleForSpecificName(targetName) {
    final override fun simplify(node: Node, context: ExprContext): Node? {
        return simplifyNodeTyped<NodeN>(node, context) { n, c -> simplifyN(n, c) }
    }

    abstract fun simplifyN(root: NodeN, context: ExprContext): Node?
}


class Flatten(targetName: String) : RuleForSpecificN(targetName) {
    // created at 2024/10/01
    override val description: String = "Flatten${targetName}"
    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Flatten[${targetName}]")

    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        if (children.size == 1) return children[0]
        if (children.all { !(it is NodeN && it.name == targetName) }) return null
        val newChildren = children.flatMap {
            if (it is NodeN && it.name == targetName) it.children else listOf(it)
        }
        return Node.NodeN(targetName, newChildren)
    }
}

/**
 * ```
 * a + a -> 2a
 * ```
 */
object MergeAdditionRational : RuleForSpecificN(Names.ADD) {
    // created at 2024/10/05


    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Merge+")


    override val description: String
        get() = "Merge addition rational"

    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        val Q = context.rational
        val collect = sortedMapOf<Node, Rational>(context.nodeOrder)
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
        if (collect.isEmpty()) return Node.ZERO
        if (collect.size == 1) {
            val (n, r) = collect.entries.first()
            return SimUtils.createWithRational(r, n, context)
        }

        val newChildren = collect.entries.map { (n, r) -> SimUtils.createWithRational(r, n, context) }
        return Node.Add(newChildren)

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

    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Merge*")

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

    private fun simMulZero(collect: Map<Node, List<Node>>, context: ExprContext): Node {
        // possible further check for undefined or infinity
        return Node.ZERO
    }

    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        val collect = sortedMapOf<Node, List<Node>>(context.nodeOrder)
        val Q = context.rational
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
        return Node.Mul(
            newChildren
        ) // need simplification by the rule again since the power may be added and simplified
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
    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Compute*")

    override val description: String
        get() = "Compute product"


    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        val Q = context.rational
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
        if (Q.isZero(product)) return Node.ZERO
        if (nodes.isEmpty()) return Node.Rational(product) // only rational
        if (count == 1 && !Q.isOne(product)) return null // only one rational that can't be simplified
        if (!Q.isOne(product)) {
            nodes.add(Node.Rational(product))
        }
        return Node.Mul(nodes).also { it[metaKeyNotApplicable] = true }
    }
}

abstract class RuleForSpecific1(targetName: String) : RuleForSpecificName(targetName) {
    override fun simplify(node: Node, context: ExprContext): Node? {
        return simplifyNodeTyped<Node1>(node, context) { n, c -> simplify1(n, c) }
    }

    protected abstract fun simplify1(root: Node1, context: ExprContext): Node?
}

abstract class RuleForSpecific2(targetName: String) : RuleForSpecificName(targetName) {
    override fun simplify(node: Node, context: ExprContext): Node? {
        return simplifyNodeTyped<Node2>(node, context) { n, c -> simplify2(n, c) }
    }

    protected abstract fun simplify2(root: Node2, context: ExprContext): Node?
}

/**
 * ```
 * exp(exp(x,2),3) -> exp(x,6)
 * ```
 */
object FlattenPow : RuleForSpecific2(Names.POW) {
    // created at 2024/10/05
    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Flatten^")

    override val description: String
        get() = "Flatten pow"

    /**
     * Flatten `exp(base = pow(b0, e0), exp) = pow(b0, e0*exp)`
     */
    private fun flattenPowPow(base: Node2, exp: Node, context: ExprContext): Node {
        val (baseBase, baseExp) = base
        val newExp = context.simplifyNode(Node.Mul(listOf(baseExp, exp)), 0)
        return Node.Pow(baseBase, newExp)
    }

    private fun flattenPowInt(base: Node, exp: NRational, context: ExprContext): Node? {
        if (context.rational.isOne(exp.value)) return base
        if (SimUtils.isPow(base)) {
            return flattenPowPow(base, exp, context)
        }
        if (SimUtils.isMul(base)) {
            val children = base.children
            val newChildren = children.map {
                if (SimUtils.isPow(it)) {
                    flattenPowPow(it, exp, context)
                } else {
                    Node.Pow(it, exp)
                }
            }
            return Node.Mul(newChildren)
        }
        return null
    }

    override fun simplify2(root: Node2, context: ExprContext): Node? {
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
    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Compute^")

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
        return context.simplifyFull(res)
    }

    private fun canExpandPow(base: BigInteger, pow: BigInteger): Boolean {
        if (pow.bitLength() > 31) return false
        val powInt = pow.intValueExact().absoluteValue
        val length = Math.multiplyFull(base.bitLength(), powInt)
        return length <= MAX_BIT_LENGTH
    }


    private fun powRational(base: Rational, exp: Rational, context: ExprContext): Node {
        with(context.rational) {
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
                        node[metaKeyNotApplicable] = true
                        node[EMeta.rational] = true
                        node[EMeta.positive] = true
                        nodes.add(node)
                        // power too big, cannot compute the exact value
                    }
                }
                if (!isZero(rem)) {
                    val p = Node.Pow(Node.Int(b), Node.Rational(rem))
                    p[metaKeyNotApplicable] = true
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

    private fun powFactorDecomposition(base: Rational, exp: Node, context: ExprContext): Node {
        TODO()
    }

    override fun simplify2(root: Node2, context: ExprContext): Node? {
        val (base, exp) = root
        if (base !is NRational) return null
        if (exp is NRational) return powRational(base.value, exp.value, context)
        return null
    }
}
