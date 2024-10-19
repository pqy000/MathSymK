package io.github.ezrnest.symbolic.sim
// created at 2024/10/19
import io.github.ezrnest.model.BigFrac
import io.github.ezrnest.model.BigFracAsQuot
import io.github.ezrnest.model.Models
import io.github.ezrnest.symbolic.NodeMetas
import io.github.ezrnest.symbolic.ExprContext
import io.github.ezrnest.symbolic.MatchContext
import io.github.ezrnest.symbolic.NRational
import io.github.ezrnest.symbolic.Node
import io.github.ezrnest.symbolic.Node1
import io.github.ezrnest.symbolic.NodeMatcherT
import io.github.ezrnest.symbolic.SimRuleMatched
import io.github.ezrnest.symbolic.TypedKey
import io.github.ezrnest.symbolic.WithLevel
import io.github.ezrnest.symbolic.buildMatcher
import io.github.ezrnest.symbolic.buildNode
import io.github.ezrnest.symbolic.set
import java.math.BigInteger

// trigonometric functions

class RuleSinSpecial : SimRuleMatched<Node1> {
    override val matcher: NodeMatcherT<Node1> = buildMatcher {
        sin(rational.named("r") * π)
    }

    private val values: MutableMap<BigFrac, Node> = mutableMapOf()

    init {
        /*
        sin(0) = 0
        sin(π/2) = 1
        sin(π/3) = sqrt(3)/2
        sin(π/4) = sqrt(2)/2
        sin(π/6) = 1/2
        sin(π/12) = (sqrt(3) - 1) / (2 * sqrt(2))
         */
        val Q = BigFracAsQuot
        with(Q) {
            values[ofN(0)] = Node.ZERO
            val half = half
            values[half] = Node.ONE
            val sqrt2 = buildNode { sqrt(2.e) }
            val sqrt3 = buildNode { sqrt(3.e) }
            values[bfrac(1, 3)] = buildNode { half.e * sqrt3 }
            values[bfrac(1, 4)] = buildNode { half.e * sqrt2 }
            values[bfrac(1, 6)] = Node.Rational(half)
            values[bfrac(1, 12)] = buildNode {
                product(bfrac(1, 4).e, sqrt2, sum(Node.NEG_ONE, sqrt3))
            }
        }
        for (v in values.values) {
            v[NodeMetas.simplified] = true
        }
    }

    private fun sinRpi(k: BigFrac, ctx: ExprContext): Node? {
        val Q = Models.bigFraction()

        with(Q) {
            var negate = false
            var r = intRem(k, BigInteger.TWO) // mod 2 pi
            if (isNegative(r)) {
                r = -r
                negate = !negate
            }
            if (r >= one) {
                r -= one
                negate = !negate
            }
            val res = values[r]
            if (res != null) {
                return if (negate) {
                    buildNode { -res }
                } else {
                    res
                }
            }
        }
        return null
    }

    override fun simplifyMatched(node: Node1, matchContext: MatchContext): WithLevel<Node>? {
        val r = (matchContext.refMap["r"] as NRational).value
        val res = sinRpi(r, matchContext.exprContext) ?: return null
        return WithLevel(Int.MAX_VALUE, res)
    }

    override val description: String = "Simplify `sin(r π)`"
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("RSinSpecial")
}