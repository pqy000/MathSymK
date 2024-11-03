package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.NRational
import io.github.ezrnest.mathsymk.symbolic.Node
import io.github.ezrnest.mathsymk.symbolic.NodeSig

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.symbolic.Node.Companion.Node1
import io.github.ezrnest.mathsymk.symbolic.Node.Companion.Node2
import io.github.ezrnest.mathsymk.symbolic.Node.Companion.Symbol
import io.github.ezrnest.mathsymk.symbolic.NodeSig.NType
import java.math.BigInteger

object SymAlg {
    val ZERO = Int(BigInteger.ZERO)

    val ONE = Int(BigInteger.ONE)

    val NEG_ONE = Int(BigInteger.ONE.negate())

    val HALF = Rational(BigFracAsQuot.half)

    val TWO = Int(BigInteger.TWO)

    val TEN = Int(BigInteger.TEN)

    val PI = Symbol(Names.Symbol_PI)

    val NATURAL_E = Symbol(Names.Symbol_E)

    val IMAGINARY_UNIT = Symbol(Names.Symbol_I)


    fun Int(value: BigInteger): NRational {
        return NRational(BigFrac(value, BigInteger.ONE))
    }

    fun Int(value: Int): NRational {
        return Int(value.toBigInteger())
    }

    fun Rational(value: BigFrac): NRational {
        return NRational(value)
    }

    fun Add(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return ZERO
        if (nodes.size == 1) return nodes[0]
        return Node.Companion.NodeN(Names.ADD, nodes)
    }

    fun Add(vararg nodes: Node): Node {
        return Add(nodes.asList())
    }

    fun Mul(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return ONE
        if (nodes.size == 1) return nodes[0]
        return Node.Companion.NodeN(Names.MUL, nodes)
    }

    fun Mul(vararg nodes: Node): Node {
        return Mul(nodes.asList())
    }

    fun Div(numerator: Node, denominator: Node): Node {
        return Mul(numerator, Inv(denominator))
    }


    fun Neg(child: Node): Node {
        return Mul(listOf(NEG_ONE, child))
    }

    fun Inv(child: Node): Node {
        return Pow(child, NEG_ONE)
    }

    fun Pow(base: Node, exp: Node): Node {
        return Node2(Names.POW, base, exp)
    }

    fun Exp(exp: Node): Node {
        return Pow(NATURAL_E, exp)
    }

    fun Sin(node: Node): Node {
        return Node1(Names.F1_SIN, node)
    }

    fun Cos(node: Node): Node {
        return Node1(Names.F1_COS, node)
    }

    fun Tan(node: Node): Node {
        return Node1(Names.F1_TAN, node)
    }

    fun Cot(node: Node): Node {
        return Node1(Names.F1_COT, node)
    }

    fun ArcSin(node: Node): Node {
        return Node1(Names.F1_ARCSIN, node)
    }

    fun ArcCos(node: Node): Node {
        return Node1(Names.F1_ARCCOS, node)
    }

    fun ArcTan(node: Node): Node {
        return Node1(Names.F1_ARCTAN, node)
    }

    fun Log(base: Node, x: Node): Node {
        return Node2(Names.F2_LOG, base, x)
    }

    fun Ln(node: Node): Node {
        return Log(NATURAL_E, node)
    }

    fun Log2(node: Node): Node {
        return Log(TWO, node)
    }

    fun Log10(node: Node): Node {
        return Log(TEN, node)
    }

    object Names {
        const val MUL = "*"
        const val ADD = "+"

        //        const val NAME_DIV = "/"
        const val POW = "^"


        //        const val F1_EXP = "exp"
        const val F2_LOG = "log"


        const val Symbol_I = "𝑖"
        const val Symbol_E = "𝑒"
        const val Symbol_PI = "π"

        const val F1_SIN = "sin"
        const val F1_COS = "cos"
        const val F1_TAN = "tan"
        const val F1_COT = "cot"
        const val F1_ARCSIN = "arcsin"
        const val F1_ARCCOS = "arccos"
        const val F1_ARCTAN = "arctan"

    }

    object Signatures {

        val RATIONAL = NodeSig("", NType.Rational)

        val ADD = NodeSig(Names.ADD, NType.NodeN)
        val MUL = NodeSig(Names.MUL, NType.NodeN)
        val POW = NodeSig(Names.POW, NType.Node2)

        val F1_SIN = NodeSig(Names.F1_SIN, NType.Node1)
        val F1_COS = NodeSig(Names.F1_COS, NType.Node1)
        val F1_TAN = NodeSig(Names.F1_TAN, NType.Node1)

    }
}

