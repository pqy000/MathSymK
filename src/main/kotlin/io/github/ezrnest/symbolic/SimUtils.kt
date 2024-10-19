package io.github.ezrnest.symbolic
// created at 2024/10/1
import io.github.ezrnest.model.BigFrac
import io.github.ezrnest.model.BigFracAsQuot
import java.math.BigInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
object SimUtils {


    fun asInteger(node: Node): BigInteger? {
        val Q = BigFracAsQuot
        return if (node is NRational && Q.isInteger(node.value)) {
            Q.asInteger(node.value)
        } else {
            null
        }
    }


    fun asRational(node: Node, Q: BigFracAsQuot): BigFrac? {
        return if (node is NRational) {
            node.value
        } else {
            null
        }
    }

    fun asPositiveInt(node: Node, context: ExprContext): BigInteger? {
        return asInteger(node)?.takeIf { it > BigInteger.ZERO }
    }


    @Suppress("NOTHING_TO_INLINE")
    inline fun isInteger(node: Node, context: ExprContext): Boolean {
        contract {
            returns(true) implies (node is NRational)
        }
        return node is NRational && BigFracAsQuot.isInteger(node.value)
    }


    @Suppress("NOTHING_TO_INLINE")
    inline fun isAdd(node: Node): Boolean {
        contract {
            returns(true) implies (node is NodeN)
        }
        return (node.name == Node.Names.ADD && node is NodeN)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun isMul(node: Node): Boolean {
        contract {
            returns(true) implies (node is NodeN)
        }
        return (node.name == Node.Names.MUL && node is NodeN)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun isPow(node: Node): Boolean {
        contract {
            returns(true) implies (node is Node2)
        }
        return (node.name == Node.Names.POW && node is Node2)
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
            return WithRational(BigFracAsQuot.one, node) // not a mul node
        val children = node.children
        val Q = BigFracAsQuot
        val rational = asRational(children.first(), Q) ?: return WithRational(Q.one, node)
        return when (children.size) {
            1 -> WithRational(rational, Node.ONE)
            2 -> WithRational(rational, children[1])
            else -> WithRational(rational, Node.Mul(children.subList(1, children.size)))
        }
    }

    /**
     * Convert a node to an exponent form:
     * - If the node is a pow node, return the base and the power;
     * - If the node is an exp node, return e and the power;
     * - Otherwise, return the node and 1.
     */
    fun toPower(node: Node): Exponent {
        if (node.name == Node.Names.POW && node is Node2)
            return Exponent(node.first, node.second)
        if (node.name == Node.Names.F1_EXP && node is Node1) {
            return Exponent(Node.NATURAL_E, node.child) // e^x
        }
        return Exponent(node, Node.ONE)
    }


    fun createWithRational(r: BigFrac, n: Node): Node {
        val Q = BigFracAsQuot
        if (n === Node.ONE) return Node.Rational(r)
        if (n is NRational) return Node.Rational(Q.multiply(r, n.value))
        if (Q.isZero(r)) return Node.ZERO
        if (Q.isOne(r)) return n
        return Node.Mul(listOf(Node.Rational(r), n))
    }

    fun createMulSim(nodes: List<Node>, context: ExprContext): Node {
        if (nodes.isEmpty()) return Node.ONE
        if (nodes.size == 1) return nodes[0]
        return context.simplifyNode(Node.Mul(nodes))
    }

    data class Exponent(val base: Node, val power: Node)

    data class WithRational(val rational: BigFrac, val node: Node)
}