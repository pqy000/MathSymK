package io.github.ezrnest.mathsymk.symbolic
// created at 2024/10/1
import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.model.MTerm
import io.github.ezrnest.mathsymk.model.TermChs
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg
import io.github.ezrnest.mathsymk.symbolic.alg.alg
import io.github.ezrnest.mathsymk.util.MathUtils
import java.math.BigInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
object SimUtils {

    @Suppress("NOTHING_TO_INLINE")
    inline fun isBelongs(node : Node) : Boolean{
        contract {
            returns(true) implies (node is Node2)
        }
        return node is Node2 && node.symbol == SymBasic.Symbols.Belongs
    }

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

    fun asPositiveInt(node: Node, context: EContext): BigInteger? {
        return asInteger(node)?.takeIf { it > BigInteger.ZERO }
    }


    @Suppress("NOTHING_TO_INLINE")
    inline fun isInteger(node: Node, context: EContext): Boolean {
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
        return (node is NodeN && node.symbol == SymAlg.Symbols.ADD)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun isMul(node: Node): Boolean {
        contract {
            returns(true) implies (node is NodeN)
        }
        return (node is NodeN && node.symbol == SymAlg.Symbols.MUL)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun isPow(node: Node): Boolean {
        contract {
            returns(true) implies (node is Node2)
        }
        return (node is Node2 && node.symbol == SymAlg.Symbols.POW)
    }


    fun asMonomial(node : Node) : MTerm<BigFrac>?{
        if (NodeMetas.asMonomial in node){
            return node[NodeMetas.asMonomial]
        }
        if (node is NRational){
            return MTerm(BigFracAsQuot.one, TermChs(emptyArray()))
        }
        TODO()
    }

    fun asMultinomial(node: Node): Multi? {
        if (NodeMetas.asMulti in node) {
            return node[NodeMetas.asMulti]
        }
        TODO()

    }


    /**
     * Extract the rational part of a node, if it is a rational times something.
     *
     * Special cases:
     * - If the node itself is a rational, return the rational and 1.
     * - If the node is not a mul node, return 1 and the node.
     */
    fun extractRational(node: Node, context: EContext): WithRational {
        if (node is NRational) return WithRational(node.value, SymAlg.ONE) // itself is a rational
        if (!isMul(node))
            return WithRational(BigFracAsQuot.one, node) // not a mul node
        val children = node.children
        val Q = BigFracAsQuot
        val rational = asRational(children.first(), Q) ?: return WithRational(Q.one, node)
        return when (children.size) {
            1 -> WithRational(rational, SymAlg.ONE)
            2 -> WithRational(rational, children[1])
            else -> WithRational(rational, SymAlg.productOf(children.subList(1, children.size)))
        }
    }


    fun createWithRational(r: BigFrac, n: Node): Node {
        val Q = BigFracAsQuot
        if (n === SymAlg.ONE) return SymAlg.rationalOf(r)
        if (n is NRational) return SymAlg.rationalOf(Q.multiply(r, n.value))
        if (Q.isZero(r)) return SymAlg.ZERO
        if (Q.isOne(r)) return n
        return alg { r.e * n }
//        return SymAlg.Mul(listOf(SymAlg.rationalOf(r), n))
    }

    fun createMulSim(nodes: List<Node>, context: EContext, cal: ExprCal): Node {
        if (nodes.isEmpty()) return SymAlg.ONE
        if (nodes.size == 1) return nodes[0]
        return cal.reduceNode(SymAlg.productOf(nodes), context)
    }


    fun sqrt(n: Int): Node {
        if (n == 0) return SymAlg.ZERO
        if (n < 0) return alg { sqrt(n.e) }

        MathUtils.sqrtInt(n.toLong()).let {
            if (it * it == n.toLong()) {
                return alg { it.e }
            }
        }
        return alg { sqrt(n.e) }.also { it[NodeMetas.simplified] = true }

    }


    data class WithRational(val rational: BigFrac, val node: Node)
}