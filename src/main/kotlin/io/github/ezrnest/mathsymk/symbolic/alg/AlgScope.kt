package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.symbolic.*
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedContained
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedContainedRep
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg.Symbols
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg.Pow
import io.github.ezrnest.mathsymk.symbolic.logic.ILogicScope
import java.math.BigInteger

interface NodeScopeAdd : NodeScope {
    fun add(x: Node, y: Node): Node {
        return sumOf(x, y)
    }

    operator fun Node.plus(y: Node): Node {
        return sumOf(listOf(this, y))
    }

    fun sumOf(vararg nodes: Node): Node {
        return sumOf(nodes.asList())
    }

    fun sumOf(elements: List<Node>): Node {
        if (elements.isEmpty()) return SymAlg.ZERO
        if (elements.size == 1) return elements[0]
        return NodeN(Symbols.ADD, elements)
    }
}


interface IAlgebraScope : NodeScopeAdd, ILogicScope {

    val imagUnit: Node get() = SymAlg.IMAGINARY_I

    val 𝑖: Node get() = SymAlg.IMAGINARY_I
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
            "pi", Symbols.π.name -> SymAlg.PI
            "e", Symbols.Natural_e.name -> SymAlg.NATURAL_E
            "i", Symbols.Imaginary_i.name -> SymAlg.IMAGINARY_I
            else -> {
                super<ILogicScope>.constant(name)
            }
        }
    }


    operator fun Node.minus(y: Node): Node {
        return sumOf(listOf(this, negate(y)))
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
        return NodeN(Symbols.MUL, elements)
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
        return SymBasic.node2(Symbols.POW, base, exp)
    }

    fun sqrt(x: Node): Node {
        return pow(x, SymAlg.HALF)
    }

    fun exp(x: Node): Node {
        return pow(naturalE, x)
    }

    fun log(base: Node, x: Node): Node {
        return SymBasic.node2(Symbols.F2_LOG, base, x)
    }

    fun ln(x: Node): Node {
        return log(naturalE, x)
    }

    fun sin(x: Node): Node {
        return SymBasic.node1(Symbols.F1_SIN, x)
    }

    fun cos(x: Node): Node {
        return SymBasic.node1(Symbols.F1_COS, x)
    }

    fun tan(x: Node): Node {
        return SymBasic.node1(Symbols.F1_TAN, x)
    }

    fun arcsin(x: Node): Node {
        return SymBasic.node1(Symbols.F1_ARCSIN, x)
    }

    fun arccos(x: Node): Node {
        return SymBasic.node1(Symbols.F1_ARCCOS, x)
    }

    fun arctan(x: Node): Node {
        return SymBasic.node1(Symbols.F1_ARCTAN, x)
    }


    infix fun Node.gtr(y: Node): Node {
        return SymBasic.node2(Symbols.F2_GTR, this, y)
    }

    infix fun Node.geq(y: Node): Node {
        return SymBasic.node2(Symbols.F2_GEQ, this, y)
    }

    infix fun Node.`≥`(y: Node): Node {
        return SymBasic.node2(Symbols.F2_GEQ, this, y)
    }

    fun sum(x: Node, lower: Node = SymAlg.ONE, upper: Node = x, f: Node): Node {
        val range = SymSets.intRange(lower, upper)
        return qualifiedContainedRep(Symbols.SUM, x, range, f)
    }


    fun sum(lower: Node, upper: Node, varName: String = "i", f: (NSymbol) -> Node): Node {
        val range = SymSets.intRange(lower, upper)
        return qualifiedContained(Symbols.SUM, varName = varName, set = range, clause = f)
    }

    companion object {


//        inline fun IAlgebraScope.sum(lower: Node, upper: Node, varName: String = "i", f: (NSymbol) -> Node): Node {
//            val range = SymSets.intRange(lower, upper)
//            return qualifiedContained(
//                Symbols.SUM, varName = varName, set = range, clause = f
//            )
//        }
    }
}

class AlgebraScope(context: EContext) : AbstractNodeScope(context), IAlgebraScope, NodeScopePredefinedSymbols


inline fun buildAlg(context: EContext = EmptyEContext, builder: AlgebraScope.() -> Node): Node {
    return AlgebraScope(context).builder()
}

inline fun <R> NodeScope.alg(builder: IAlgebraScope.() -> R): R {
    return AlgebraScope(this.context).builder()
}

class AlgScopeMatcher(context: EContext) : AbstractNodeScopeMatcher(context), NodeScopeMatcher, IAlgebraScope {
    constructor(scope: NodeScopeMatcher) : this(scope.context)
}

data class AlgScopeMatchedReferred(override val context: EContext, override val matchResult: MatchResult) :
    IAlgebraScope, NodeScopeMatched {
    constructor(scope: NodeScopeMatched) : this(scope.context, scope.matchResult)

    override val namedSymbols: MutableMap<String, ESymbol> = mutableMapOf()
}

inline fun RuleBuilder.matchAlg(crossinline builder: AlgScopeMatcher.() -> Node) {
    this.match { AlgScopeMatcher(this).builder() }
}

inline fun RuleBuilder.toAlg(crossinline builder: AlgScopeMatchedReferred.() -> Node) {
    this.to { AlgScopeMatchedReferred(this).builder() }
}

