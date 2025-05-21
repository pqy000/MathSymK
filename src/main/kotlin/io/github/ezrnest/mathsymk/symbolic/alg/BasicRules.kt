package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.model.isOdd
import io.github.ezrnest.mathsymk.symbolic.*
import io.github.ezrnest.mathsymk.util.IterUtils
import io.github.ezrnest.mathsymk.util.WithInt
import java.math.BigInteger
import kotlin.math.absoluteValue


/**
 * ```
 * a + a -> 2a
 * ```
 */
object MergeAdditionRational : RuleForSpecificN(SymAlg.Symbols.ADD) {
    // created at 2024/10/05


    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Merge+")


    override val description: String
        get() = "Merge addition rational"

    override fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>? {
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
        return WithInt(0, SymAlg.sumOf(newChildren))

    }

}

/**
 * ```
 * x * x -> x^2
 * 1 * x * 2 -> 2x
 * ```
 */
object MergeProduct : RuleForSpecificN(SymAlg.Symbols.MUL) {
    // created at 2024/10/05

    override val description: String
        get() = "Merge product"

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Merge*")


    private fun getBase(node: Node): Node {
        if (node is Node2 && node.symbol == SymAlg.Symbols.POW) {
            return node.first
        }
        return node
    }

    private fun getPower(node: Node): Node {
        if (node is Node2 && node.symbol == SymAlg.Symbols.POW) {
            return node.second
        }
        return SymAlg.ONE
    }

    private fun buildPower(base: Node, nodeList: List<Node>, context: EContext, cal: ExprCal): Node {
        if (nodeList.size == 1) return nodeList[0] // not merged
        var exp = SymAlg.sumOf(nodeList.map { getPower(it) })
        exp = cal.reduceNode(exp, context, 0)
        if (exp == SymAlg.ONE) return base
        val res = SymAlg.pow(base, exp)
        return cal.reduceNode(res, context, 0)
    }

    private fun simMulZero(collect: Map<Node, List<Node>>, context: EContext): WithInt<Node> {
        // possible further check for undefined or infinity
        return WithInt(-1, SymAlg.ZERO)
    }


    override fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>? {
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
        if (addRational) newChildren.add(SymAlg.rationalOf(rPart))
        collect.entries.mapTo(newChildren) { (base, nodeList) -> buildPower(base, nodeList, context, cal) }
        return WithInt(0, SymAlg.productOf(newChildren))
        // need simplification by the rule again since the power may be added and simplified
    }
}


/**
 * ```
 * 1 * 2 * 3 -> 6
 * 1 * x -> x
 * ```
 */
object ComputeProductRational : RuleForSpecificN(SymAlg.Symbols.MUL) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Compute*")

    override val description: String
        get() = "Compute product"


    override fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>? {
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
        if (nodes.isEmpty()) return WithInt(-1, SymAlg.rationalOf(product)) // only rational
        if (count == 1 && !Q.isOne(product)) return null // only one rational that can't be simplified
        if (!Q.isOne(product)) {
            nodes.add(SymAlg.rationalOf(product))
        }
        return SymAlg.productOf(nodes).also { it[metaKeyApplied] = true }.let { WithInt(0, it) }
    }
}


/**
 * ```
 * exp(exp(x,2),3) -> exp(x,6)
 * ```
 */
object FlattenPow : RuleForSpecific2(SymAlg.Symbols.POW) {
    // created at 2024/10/05
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Flatten^")

    override val description: String
        get() = "Flatten pow"

    /**
     * Flatten `exp(base = pow(b0, e0), exp) = pow(b0, e0*exp)`
     */
    private fun flattenPowPow(base: Node2, exp: Node): Node {
        val (_, baseBase, baseExp) = base
        return alg {
            pow(baseBase, (baseExp * exp))
        }
//        val newExp = SymAlg.product(baseExp, exp)
//        return SymAlg.Pow(baseBase, newExp)
    }

    private fun flattenPowInt(base: Node, exp: NRational, context: EContext): WithInt<Node>? {
        if (BigFracAsQuot.isOne(exp.value)) return WithInt(0, base)
        if (SimUtils.isPow(base)) {
            return WithInt(1, flattenPowPow(base, exp))
        }
        if (SimUtils.isMul(base)) {
            val children = base.children
            val newChildren = children.map {
                if (SimUtils.isPow(it)) {
                    flattenPowPow(it, exp)
                } else {
                    SymAlg.pow(it, exp)
                }
            }
            return WithInt(2, SymAlg.productOf(newChildren))
        }
        return null
    }

    override fun simplify2(root: Node2, context: EContext, cal: ExprCal): WithInt<Node>? {
        val (_, base, exp) = root
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
object ComputePow : RuleForSpecific2(SymAlg.Symbols.POW) {
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
    private fun computeNRootMinus1(exp: BigInteger, context: EContext, cal: ExprCal): Node {
        if (exp == BigInteger.ONE) return SymAlg.NEG_ONE
        if (cal.options[ExprCal.Options.forceReal] == true) throw ArithmeticException(
            "Cannot compute the value of (-1)^(1/n) in the real mode"
        )

        if (exp == BigInteger.TWO) return SymAlg.IMAGINARY_I
        return buildNode(context) {
            alg {
                val piOverN = pi / exp.e
                val cos = cos(piOverN)
                val sin = sin(piOverN)
                cos + 𝑖 * sin // let the simplification handle the rest
            }
        }

    }

    private fun canExpandPow(base: BigInteger, pow: BigInteger): Boolean {
        if (pow.bitLength() > 31) return false
        val powInt = pow.intValueExact().absoluteValue
        val length = Math.multiplyFull(base.bitLength(), powInt)
        return length <= MAX_BIT_LENGTH
    }


    private fun powRational(base: BigFrac, exp: BigFrac, context: EContext, cal: ExprCal): Node {
        with(BigFracAsQuot) {
            if (isInteger(exp)) {
                val p = asInteger(exp)
                if (canExpandPow(base.nume, p) && canExpandPow(base.deno, p)) {
                    val res = power(base, p)
                    return SymAlg.rationalOf(res)
                }
                // power too big
            }
            if (isOne(base)) return SymAlg.ONE
            if (isZero(base)) {
                if (isZero(exp)) return SymBasic.Undefined
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
                        val node = alg {
                            pow(b.e, floor.e)
                        }
                        node[metaKeyApplied] = true
                        node[NodeMetas.rational] = true
                        node[NodeMetas.positive] = true
                        nodes.add(node)
                        // power too big, cannot compute the exact value
                    }
                }
                if (!isZero(rem)) {
                    val p = alg {
                        pow(b.e, rem.e)
                    }
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
                nodes.add(SymAlg.rationalOf(rPart))
            }
            return SymAlg.productOf(nodes)
        }
    }

//    private fun powFactorDecomposition(base: BigFrac, exp: Node, context: ExprContext): Node {
//        TODO()
//    }

    override fun simplify2(root: Node2, context: EContext, cal: ExprCal): WithInt<Node>? {
        val (_, base, exp) = root
        if (base !is NRational) return null
        if (exp is NRational) return WithInt(Int.MAX_VALUE, powRational(base.value, exp.value, context, cal))
        return null
    }
}


object RuleExpandMul : RuleForSpecificN(SymAlg.Symbols.MUL) {
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

    override fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>? {
        val children = root.children
        val resultSize = children.fold(1) { s, n -> s * asFactorSize(n) }
        if (resultSize == 1 || resultSize >= defaultExpansionLimit) return null
        alg {
            val newChildren = ArrayList<Node>(resultSize)
            IterUtils.prod(children.map { asFactor(it) }).forEach {
                newChildren.add(productOf(it))
            }
            val res = sumOf(newChildren)
            return WithInt(1, res)
        }

    }
}

/**
 * ```
 * x + 0 -> x
 * 0 + x -> x
 * ```
 */
object RemoveAddZero : RuleForSpecificN(SymAlg.Symbols.ADD) {
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Add0")

    override val description: String
        get() = "Remove zero in addition"

    override fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>? {
        val children = root.children
        val Q = BigFracAsQuot
        var changed = false
        val newChildren = ArrayList<Node>(children.size)
        for (c in children) {
            val keep = if (c is NRational) !Q.isZero(c.value) else c != SymAlg.ZERO
            if (keep) newChildren.add(c) else changed = true
        }
        if (!changed) return null
        if (newChildren.isEmpty()) return WithInt(-1, SymAlg.ZERO)
        if (newChildren.size == 1) return WithInt(0, newChildren[0])
        return WithInt(0, SymAlg.sumOf(newChildren))
    }
}

/**
 * ```
 * x * 1 -> x
 * 1 * x -> x
 * ```
 */
object RemoveMulOne : RuleForSpecificN(SymAlg.Symbols.MUL) {
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Mul1")

    override val description: String
        get() = "Remove one in multiplication"

    override fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>? {
        val children = root.children
        val Q = BigFracAsQuot
        var changed = false
        val newChildren = ArrayList<Node>(children.size)
        for (c in children) {
            val keep = if (c is NRational) !Q.isOne(c.value) else c != SymAlg.ONE
            if (keep) newChildren.add(c) else changed = true
        }
        if (!changed) return null
        if (newChildren.isEmpty()) return WithInt(-1, SymAlg.ONE)
        if (newChildren.size == 1) return WithInt(0, newChildren[0])
        return WithInt(0, SymAlg.productOf(newChildren))
    }
}

/**
 * Simplify power with zero exponent.
 *
 * ```
 * x^0 -> 1
 * 0^0 -> undefined
 * ```
 */
object PowExponentZero : RuleForSpecific2(SymAlg.Symbols.POW) {
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Pow0")

    override val description: String
        get() = "Simplify exponent zero"

    override fun simplify2(root: Node2, context: EContext, cal: ExprCal): WithInt<Node>? {
        val (_, base, exp) = root
        if (exp !is NRational) return null
        if (!BigFracAsQuot.isZero(exp.value)) return null
        if (base is NRational && BigFracAsQuot.isZero(base.value)) {
            return WithInt(Int.MAX_VALUE, SymBasic.Undefined)
        }
        return WithInt(Int.MAX_VALUE, SymAlg.ONE)
    }
}