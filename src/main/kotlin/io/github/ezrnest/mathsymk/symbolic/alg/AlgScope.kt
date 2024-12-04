package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.symbolic.*
import io.github.ezrnest.mathsymk.symbolic.Node.Companion.Node1
import io.github.ezrnest.mathsymk.symbolic.Node.Companion.Node2
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg.Symbols
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg.Pow
import io.github.ezrnest.mathsymk.symbolic.logic.ILogicScope
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
        return Node.NodeN(Symbols.ADD, elements)
    }
}


interface IAlgebraScope : NodeScopeAdd,ILogicScope {

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


    override fun constant(name: String): Node? {
        return when (name) {
            "pi", Symbols.Symbol_PI -> SymAlg.PI
            "e", Symbols.Symbol_E -> SymAlg.NATURAL_E
            "i", Symbols.Imaginary_i -> SymAlg.IMAGINARY_UNIT
            else -> {
                super<ILogicScope>.constant(name)
            }
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
        return Node.Companion.NodeN(Symbols.MUL, elements)
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
        return Node2(Symbols.POW, base, exp)
    }

    fun sqrt(x: Node): Node {
        return pow(x, SymAlg.HALF)
    }

    fun exp(x: Node): Node {
        return pow(naturalE, x)
    }

    fun log(base: Node, x: Node): Node {
        return Node2(Symbols.F2_LOG, base, x)
    }

    fun ln(x: Node): Node {
        return log(naturalE, x)
    }

    fun sin(x: Node): Node {
        return Node1(Symbols.F1_SIN, x)
    }

    fun cos(x: Node): Node {
        return Node1(Symbols.F1_COS, x)
    }

    fun tan(x: Node): Node {
        return Node1(Symbols.F1_TAN, x)
    }

    fun arcsin(x: Node): Node {
        return Node1(Symbols.F1_ARCSIN, x)
    }

    fun arccos(x: Node): Node {
        return Node1(Symbols.F1_ARCCOS, x)
    }

    fun arctan(x: Node): Node {
        return Node1(Symbols.F1_ARCTAN, x)
    }


    infix fun Node.gtr(y: Node): Node {
        return Node2(Symbols.F2_GTR, this, y)
    }


}

data class AlgebraScope(override val context: EContext) : IAlgebraScope, NodeScopePredefinedSymbols


inline fun buildAlg(context: EContext = EmptyEContext, builder: AlgebraScope.() -> Node): Node {
    return AlgebraScope(context).builder()
}

inline fun <R> NodeScope.alg(builder: IAlgebraScope.() -> R): R {
    return AlgebraScope(this.context).builder()
}

data class AlgScopeMatcher(override val context: EContext) : IAlgebraScope, NodeScopeMatcher{
    constructor(scope : NodeScopeMatcher) : this(scope.context)
}
data class AlgScopeMatchedReferred(override val context: EContext, override val matchResult: MatchResult) : IAlgebraScope, NodeScopeMatched{
    constructor(scope : NodeScopeMatched) : this(scope.context,scope.matchResult)
}

inline fun RuleBuilder.matchAlg(crossinline builder: AlgScopeMatcher.() -> Node) {
    this.match { AlgScopeMatcher(this).builder() }
}

inline fun RuleBuilder.toAlg(crossinline builder: AlgScopeMatchedReferred.() -> Node) {
    this.to { AlgScopeMatchedReferred(this).builder() }
}

