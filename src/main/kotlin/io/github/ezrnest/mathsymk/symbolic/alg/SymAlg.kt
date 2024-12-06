package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.symbolic.*
import java.math.BigInteger

object SymAlg {
    val ZERO = Int(BigInteger.ZERO)

    val ONE = Int(BigInteger.ONE)

    val NEG_ONE = Int(BigInteger.ONE.negate())

    val HALF = Rational(BigFracAsQuot.half)

    val TWO = Int(BigInteger.TWO)

    val TEN = Int(BigInteger.TEN)

    val PI = NSymbol(Symbols.π)
    val NATURAL_E = NSymbol(Symbols.Natural_e)
    val IMAGINARY_I = NSymbol(Symbols.Imaginary_i)

    val INFINITY get() = NSymbol(Symbols.Infinity)
    val POSITIVE_INFINITY get() = NSymbol(Symbols.POSITIVE_INFINITY)
    val NEGATIVE_INFINITY get() = NSymbol(Symbols.NEGATIVE_INFINITY)



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
        return NodeN(Symbols.ADD, nodes)
    }

    fun Add(vararg nodes: Node): Node {
        return Add(nodes.asList())
    }

    fun Mul(nodes: List<Node>): Node {
        if (nodes.isEmpty()) return ONE
        if (nodes.size == 1) return nodes[0]
        return NodeN(Symbols.MUL, nodes)
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
        return Node2(Symbols.POW, base, exp)
    }

    fun Exp(exp: Node): Node {
        return Pow(NATURAL_E, exp)
    }

    fun Sin(node: Node): Node {
        return Node1(Symbols.F1_SIN, node)
    }

    fun Cos(node: Node): Node {
        return Node1(Symbols.F1_COS, node)
    }

    fun Tan(node: Node): Node {
        return Node1(Symbols.F1_TAN, node)
    }

    fun Cot(node: Node): Node {
        return Node1(Symbols.F1_COT, node)
    }

    fun ArcSin(node: Node): Node {
        return Node1(Symbols.F1_ARCSIN, node)
    }

    fun ArcCos(node: Node): Node {
        return Node1(Symbols.F1_ARCCOS, node)
    }

    fun ArcTan(node: Node): Node {
        return Node1(Symbols.F1_ARCTAN, node)
    }

    fun Log(base: Node, x: Node): Node {
        return Node2(Symbols.F2_LOG, base, x)
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

    object Symbols {
        val MUL = ESymbol("*")
        val ADD = ESymbol("+")

        val POW = ESymbol("^")

        val π = ESymbol("π")
        val Natural_e = ESymbol("𝑒")
        val Imaginary_i = ESymbol("𝑖")

        val Infinity = ESymbol("∞")
        val POSITIVE_INFINITY = ESymbol("+∞")
        val NEGATIVE_INFINITY = ESymbol("-∞")


        val F1_SIN = ESymbol("sin")
        val F1_COS = ESymbol("cos")
        val F1_TAN = ESymbol("tan")
        val F1_COT = ESymbol("cot")
        val F1_ARCSIN = ESymbol("arcsin")
        val F1_ARCCOS = ESymbol("arccos")
        val F1_ARCTAN = ESymbol("arctan")

        val F2_LOG = ESymbol("log")

        val F2_GEQ = ESymbol("≥")
        val F2_GTR = ESymbol(">")
        val F2_LEQ = ESymbol("≤")
        val F2_LSS = ESymbol("<")


        val SUM = ESymbol("∑")

    }

}

