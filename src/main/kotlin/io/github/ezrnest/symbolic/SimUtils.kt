package io.github.ezrnest.symbolic
// created at 2024/10/1
import io.github.ezrnest.model.BigFractionAsQuotients
import java.math.BigInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object SimUtils {

    fun asInteger(node: Node, Q: BigFractionAsQuotients): BigInteger? {
        return if (node is NRational && Q.isInteger(node.value)) {
            Q.asInteger(node.value)
        } else {
            null
        }
    }


    fun asRational(node: Node, Q: BigFractionAsQuotients): Rational? {
        return if (node is NRational) {
            node.value
        } else {
            null
        }
    }

    fun asPositiveInt(node: Node, context: ExprContext): BigInteger? {
        val Q = context.rational
        return asInteger(node, Q)?.takeIf { it > BigInteger.ZERO }
    }


    @OptIn(ExperimentalContracts::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun isAdd(node: Node): Boolean {
        contract {
            returns(true) implies (node is NodeN)
        }
        return (node.name == Node.Names.ADD && node is NodeN)
    }

    @OptIn(ExperimentalContracts::class)
    @Suppress("NOTHING_TO_INLINE")
    inline fun isMul(node: Node): Boolean {
        contract {
            returns(true) implies (node is NodeN)
        }
        return (node.name == Node.Names.MUL && node is NodeN)
    }


    /**
     * Extract the rational part of a node, if it is a rational times something.
     *
     * Special cases:
     * - If the node itself is a rational, return the rational and 1.
     * - If the node is not a mul node, return 1 and the node.
     */
    fun extractRational(node: Node, context: ExprContext): WithRational {
        if (node is NRational) return WithRational(node.value, Node.ONE) // itself is a rational
        if (!isMul(node))
            return WithRational(context.rational.one, node) // not a mul node
        val children = node.children
        val Q = context.rational
        val rational = asRational(children.first(), Q) ?: return WithRational(Q.one, node)
        return when (children.size) {
            1 -> WithRational(rational, Node.ONE)
            2 -> WithRational(rational, children[1])
            else -> WithRational(rational, context.Mul(children.subList(1, children.size)))
        }
    }


    fun asPower(node: Node, context: ExprContext): Exponent {
        if (node.name == Node.Names.POW && node is Node2)
            return Exponent(node.first, node.second)
        return Exponent(node, Node.ONE)
    }


    fun createWithRational(r: Rational, n: Node, context: ExprContext): Node {
        val Q = context.rational
        if (n === Node.ONE) return Node.Rational(r)
        if (n is NRational) return Node.Rational(Q.multiply(r, n.value))
        if (Q.isZero(r)) return Node.ZERO
        if (Q.isOne(r)) return n
        return context.Mul(listOf(Node.Rational(r), n))
    }

    fun createMulSim(nodes: List<Node>, context: ExprContext): Node {
        if (nodes.isEmpty()) return Node.ONE
        if (nodes.size == 1) return nodes[0]
        return context.simplifyNode(context.Mul(nodes))
    }

    data class Exponent(val base: Node, val power: Node)

    data class WithRational(val rational: Rational, val node: Node)
}