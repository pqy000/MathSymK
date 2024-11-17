package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.symbolic.*
import io.github.ezrnest.mathsymk.symbolic.Node.Companion.Node1
import io.github.ezrnest.mathsymk.symbolic.Node.Companion.Node2
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg.Names
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg.Pow
import java.math.BigInteger

interface NodeScopeAdd : NodeScope {
    fun add(x: Node, y: Node): Node {
        return sum(x, y)
    }

    operator fun Node.plus(y: Node): Node {
        return sum(listOf(this, y))
    }

    fun sum(vararg nodes: Node): Node {
        return sum(nodes.asList())
    }

    fun sum(elements: List<Node>): Node {
        if (elements.isEmpty()) return SymAlg.ZERO
        if (elements.size == 1) return elements[0]
        return Node.NodeN(Names.ADD, elements)
    }
}


interface NodeScopeAlg : NodeScopeAdd {

    val imagUnit: Node get() = SymAlg.IMAGINARY_UNIT

    val 𝑖: Node get() = SymAlg.IMAGINARY_UNIT
    val naturalE: Node get() = SymAlg.NATURAL_E

    val 𝑒: Node get() = SymAlg.NATURAL_E

    val pi: Node get() = SymAlg.PI
    val π: Node get() = SymAlg.PI


    fun intOf(value: Int): Node {
        return NRational(BigFrac(value.toBigInteger(), BigInteger.ONE))
    }

    fun intOf(value: BigInteger): Node {
        return NRational(BigFrac(value, BigInteger.ONE))
    }

    fun rational(nume: Int, deno: Int): Node {
        val r = BigFracAsQuot.bfrac(nume, deno)
        return NRational(r)
    }

    val Int.e: Node get() = intOf(this)

    val Long.e: Node get() = SymAlg.Int(this.toBigInteger())

    val BigInteger.e: Node get() = SymAlg.Int(this)

    val BigFrac.e: Node get() = SymAlg.Rational(this)


    override fun constant(name: String): Node {
        return when (name) {
            "pi", Names.Symbol_PI -> SymAlg.PI
            "e", Names.Symbol_E -> SymAlg.NATURAL_E
            "i", Names.Symbol_I -> SymAlg.IMAGINARY_UNIT
            else -> super.constant(name)
        }
    }




    operator fun Node.minus(y: Node): Node {
        return sum(listOf(this, negate(y)))
    }

    fun negate(x: Node): Node {
        return product(SymAlg.NEG_ONE, x)
    }

    operator fun Node.unaryMinus(): Node {
        return negate(this)
    }


    fun multiply(x: Node, y: Node): Node {
        return product(x, y)
    }

    fun product(vararg nodes: Node): Node {
        return product(nodes.asList())
    }

    fun product(elements: List<Node>): Node {
        if (elements.isEmpty()) return SymAlg.ONE
        if (elements.size == 1) return elements[0]
        return Node.Companion.NodeN(Names.MUL, elements)
    }


    operator fun Node.times(y: Node): Node {
        return product(listOf(this, y))
    }

    fun inv(node: Node): Node {
        return Pow(node, SymAlg.NEG_ONE)
    }

    fun divide(x: Node, y: Node): Node {
        return product(x, inv(y))
    }

    operator fun Node.div(y: Node): Node {
        return product(this, inv(y))
    }

    fun pow(base: Node, exp: Node): Node {
        return Node2(Names.POW, base, exp)
    }

    fun sqrt(x: Node): Node {
        return pow(x, SymAlg.HALF)
    }

    fun exp(x: Node): Node {
        return pow(naturalE, x)
    }

    fun log(base: Node, x: Node): Node {
        return Node2(Names.F2_LOG, base, x)
    }

    fun ln(x: Node): Node {
        return log(naturalE, x)
    }

    fun sin(x: Node): Node {
        return Node1(Names.F1_SIN, x)
    }

    fun cos(x: Node): Node {
        return Node1(Names.F1_COS, x)
    }

    fun tan(x: Node): Node {
        return Node1(Names.F1_TAN, x)
    }

    fun arcsin(x: Node): Node {
        return Node1(Names.F1_ARCSIN, x)
    }

    fun arccos(x: Node): Node {
        return Node1(Names.F1_ARCCOS, x)
    }

    fun arctan(x: Node): Node {
        return Node1(Names.F1_ARCTAN, x)
    }

    companion object {
        operator fun invoke(context: ExprContext): NodeScopeAlg {
            return NodeScopeAlgImpl(context)
        }
    }
}

data class NodeScopeAlgImpl(override val context: ExprContext) : NodeScopeAlg, NodeScopePredefinedSymbols



inline fun buildAlg(context: ExprContext = EmptyExprContext, builder: NodeScopeAlgImpl.() -> Node): Node {
    return NodeScopeAlgImpl(context).builder()
}

inline fun NodeScope.alg(builder: NodeScopeAlg.() -> Node): Node {
    return NodeScopeAlg(this.context).builder()
}

//inline fun RuleBuilder.matchAlg(crossinline builder: NodeScopeAlg.() -> Node) {
//    this.match { alg(builder) }
//}
//
//inline fun RuleBuilder.toAlg(crossinline builder: NodeScopeAlg.() -> Node) {
//    this.to { alg(builder) }
//}

