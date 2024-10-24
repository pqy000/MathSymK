package io.github.ezrnest.mathsymk.symbolic.sim
// created at 2024/10/19
import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.model.Models
import io.github.ezrnest.mathsymk.model.isEven
import io.github.ezrnest.mathsymk.symbolic.NodeMetas
import io.github.ezrnest.mathsymk.symbolic.MatchContext
import io.github.ezrnest.mathsymk.symbolic.NRational
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.Node1
import io.github.ezrnest.mathsymk.symbolic.NodeMatcherT
import io.github.ezrnest.mathsymk.symbolic.SimRuleMatched
import io.github.ezrnest.mathsymk.symbolic.SimUtils
import io.github.ezrnest.mathsymk.symbolic.TypedKey
import io.github.ezrnest.mathsymk.symbolic.buildMatcher
import io.github.ezrnest.mathsymk.symbolic.buildNode
import io.github.ezrnest.mathsymk.symbolic.set
import io.github.ezrnest.mathsymk.util.WithInt


object TrigonometricUtils {


    val sinTable: MutableMap<BigFrac, Node> by lazy {
        val values = mutableMapOf<BigFrac, Node>()
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
            val sqrt2 = SimUtils.sqrt(2)
            val sqrt3 = SimUtils.sqrt(3)
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

        values
    }

    /**
     * Reduce the angle to [0, π) and return the quadrant.
     */
    private fun modInPiAndQuad(k: BigFrac): Pair<BigFrac, Boolean> {
        with(Models.bigFraction()) {
            val (q, r) = intDivRem(k, one)
            val pos = q.isEven()
            return r to pos
        }
    }

    fun sinRPi(k: BigFrac): Node? {
        var (r, pos) = modInPiAndQuad(k)
        with(BigFracAsQuot) {
            if (r > half) {
                // sin(π - r) = sin(r)
                r = one - r
            }
        }
        val res = sinTable[r] ?: return null
        return if (pos) res else buildNode { -res }
    }

    fun cosRPi(k: BigFrac): Node? {
        // cos(x) = sin(π/2 - x)
        with(BigFracAsQuot) {
            return sinRPi(half - k)
        }
    }


    val tanTable: MutableMap<BigFrac, Node> by lazy {
        val values = mutableMapOf<BigFrac, Node>()
        /*
        tan(0) = 0
        tan(π/6) = 1/sqrt(3)
        tan(π/4) = 1
        tan(π/3) = sqrt(3)
        tan(π/2) = Undefined
         */
        val Q = BigFracAsQuot
        with(Q) {
            val sqrt2 = SimUtils.sqrt(2)
            val sqrt3 = SimUtils.sqrt(3)
            values[ofN(0)] = Node.ZERO
            values[bfrac(1, 6)] = buildNode { pow(3.e, (-half).e) }
            values[bfrac(1, 4)] = Node.ONE
            values[bfrac(1, 3)] = sqrt3
            values[half] = Node.UNDEFINED
        }
        for (v in values.values) {
            v[NodeMetas.simplified] = true
        }
        values
    }

    fun tanRPi(k: BigFrac): Node? {
        var (r, _) = modInPiAndQuad(k) // tan(x + kπ) = tan(x)
        var pos = true
        with(BigFracAsQuot) {
            if (r > half) {
                // tan(π - r) = -tan(r)
                r = one - r
                pos = false
            }
        }
        val res = tanTable[r] ?: return null
        return if (pos) res else buildNode { -res }
    }
}


class RuleSinSpecial : SimRuleMatched<Node1> {
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("RSinSpecial")
    override val description: String = "Simplify `sin(r π)`"
    override val matcher: NodeMatcherT<Node1> = buildMatcher {
        sin(rational.named("r") * π)
    }


    override fun simplifyMatched(node: Node1, matchContext: MatchContext): WithInt<Node>? {
        val r = (matchContext.refMap["r"] as NRational).value
        val res = TrigonometricUtils.sinRPi(r) ?: return null
        return WithInt(Int.MAX_VALUE, res)
    }

}

class RuleCosSpecial : SimRuleMatched<Node1> {
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("RCosSpecial")

    override val description: String
        get() = "Simplify `cos(r π)`"

    override val matcher: NodeMatcherT<Node1> = buildMatcher {
        cos(rational.named("r") * π)
    }

    override fun simplifyMatched(node: Node1, matchContext: MatchContext): WithInt<Node>? {
        val r = (matchContext.refMap["r"] as NRational).value
        val res = TrigonometricUtils.cosRPi(r) ?: return null
        return WithInt(Int.MAX_VALUE, res)
    }
}


class RuleTanSpecial : SimRuleMatched<Node1> {
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("RTanSpecial")

    override val description: String
        get() = "Simplify `tan(r π)`"

    override val matcher: NodeMatcherT<Node1> = buildMatcher {
        tan(rational.named("r") * π)
    }

    override fun simplifyMatched(node: Node1, matchContext: MatchContext): WithInt<Node>? {
        val r = (matchContext.refMap["r"] as NRational).value
        val res = TrigonometricUtils.tanRPi(r) ?: return null
        return WithInt(Int.MAX_VALUE, res)
    }
}


class RulesTrigonometricReduce : RuleList() {

    init {
        list.add(RuleSinSpecial())
        list.add(RuleCosSpecial())
        list.add(RuleTanSpecial())
    }

    init {
        rule {
            name = "Trig: sin^2(x) + cos^2(x) = 1"
            match {
                pow(sin(x), 2.e) + pow(cos(x), 2.e)
            } to {
                1.e
            }
        }
    }
}

class RulesTrigonometricTransform : RuleList() {

    init {
//        rule {
//            name = "Trig: sin(x) = cos(π/2 - x)"
//            match {
//                sin(x)
//            } to {
//                cos(π / 2.e - x)
//            }
//        }
//        rule {
//            name = "Trig: cos(x) = sin(π/2 - x)"
//            match {
//                cos(x)
//            } to {
//                sin(π / 2.e - x)
//            }
//        }
        rule{
            name = "Trig: sin(x) = -sin(-x)"
            match {
                sin(-x)
            } to {
                -sin(x)
            }
        }
        rule{
            name = "Trig: cos(x) = cos(-x)"
            match {
                cos(-x)
            } to {
                cos(x)
            }
        }

        rule {
            name = "Trig: sin(x+y) = sin(x)cos(y) + cos(x)sin(y)"
            match {
                sin(x + y)
            } to {
                sin(x) * cos(y) + cos(x) * sin(y)
            }
        }
        rule {
            name = "Trig: sin(x)cos(y) + cos(x)sin(y) -> sin(x+y)"
            match {
                sin(x) * cos(y) + cos(x) * sin(y)
            } to {
                sin(x+y)
            }
        }
        rule {
            name = "Trig: cos(x+y) = cos(x)cos(y) - sin(x)sin(y)"
            match {
                cos(x + y)
            } to {
                cos(x) * cos(y) - sin(x) * sin(y)
            }
        }
        rule {
            name = "Trig: cos(x)cos(y) - sin(x)sin(y) -> cos(x+y) "
            match {
                cos(x) * cos(y) - sin(x) * sin(y)

            } to {
                cos(x + y)
            }
        }
    }
}