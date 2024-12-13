package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.symbolic.*
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedContained
import io.github.ezrnest.mathsymk.symbolic.NodeScope.Companion.qualifiedContainedRep
import io.github.ezrnest.mathsymk.symbolic.alg.SymAlg.Symbols
import io.github.ezrnest.mathsymk.symbolic.logic.SymLogic
import java.math.BigInteger

interface NScopeExtAdd : SymBasic {
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


interface SymAlg : NScopeExtAdd, SymLogic,SymSets {

    val imagUnit: Node get() = SymAlg.IMAGINARY_I

    val 𝑖: Node get() = SymAlg.IMAGINARY_I
    val naturalE: Node get() = SymAlg.NATURAL_E

    val 𝑒: Node get() = SymAlg.NATURAL_E

    val pi: Node get() = SymAlg.PI
    val π: Node get() = SymAlg.PI

    val infinity: Node get() = SymAlg.Infinity



    object Symbols {
        val MUL = ESymbol("*")
        val ADD = ESymbol("+")

        val POW = ESymbol("^")

        val π = ESymbol("π")
        val Natural_e = ESymbol("𝑒")
        val Imaginary_i = ESymbol("𝑖")

        val INFINITY = ESymbol("∞")
        val POSITIVE_INFINITY = ESymbol("+∞")
        val NEGATIVE_INFINITY = ESymbol("-∞")


        val F1_ABS = ESymbol("abs")
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

    companion object Instance : SymAlg {
        val ZERO = intOf(BigInteger.ZERO)

        val ONE = intOf(BigInteger.ONE)

        val NEG_ONE = intOf(-1)

        val HALF = rationalOf(BigFracAsQuot.half)

//        val TWO = intOf(BigInteger.TWO)
//
//        val TEN = intOf(BigInteger.TEN)

        val PI = NSymbol(Symbols.π)
        val NATURAL_E = NSymbol(Symbols.Natural_e)
        val IMAGINARY_I = NSymbol(Symbols.Imaginary_i)

        val Infinity get() = NSymbol(Symbols.INFINITY)
        val POSITIVE_INFINITY get() = NSymbol(Symbols.POSITIVE_INFINITY)
        val NEGATIVE_INFINITY get() = NSymbol(Symbols.NEGATIVE_INFINITY)
    }



    fun intOf(value: Int): NRational {
        return NRational(BigFrac(value.toBigInteger(), BigInteger.ONE))
    }

    fun intOf(value: BigInteger): NRational {
        return NRational(BigFrac(value, BigInteger.ONE))
    }

    fun rational(nume: Int, deno: Int): NRational {
        val r = BigFracAsQuot.bfrac(nume, deno)
        return NRational(r)
    }

    fun rationalOf(value: BigFrac): NRational {
        return NRational(value)
    }

    val Int.e: Node get() = intOf(this)

    val Long.e: Node get() = intOf(this.toBigInteger())

    val BigInteger.e : Node get() = intOf(this)

    val BigFrac.e : Node get() = rationalOf(this)


//    override fun constant(name: String): Node? {
//        return when (name) {
//            "pi", Symbols.π.name -> SymAlg.PI
//            "e", Symbols.Natural_e.name -> SymAlg.NATURAL_E
//            "i", Symbols.Imaginary_i.name -> SymAlg.IMAGINARY_I
//            else -> {
//                super<SymLogic>.constant(name)
//            }
//        }
//    }


    operator fun Node.minus(y: Node): Node {
        return sumOf(listOf(this, negate(y)))
    }

    fun negate(x: Node): Node {
        return productOf(SymAlg.NEG_ONE, x)
    }

    operator fun Node.unaryMinus(): Node {
        return negate(this)
    }


    fun multiply(x: Node, y: Node): Node {
        return productOf(x, y)
    }

    fun productOf(vararg nodes: Node): Node {
        return productOf(nodes.asList())
    }

    fun productOf(elements: List<Node>): Node {
        if (elements.isEmpty()) return SymAlg.ONE
        if (elements.size == 1) return elements[0]
        return NodeN(Symbols.MUL, elements)
    }


    operator fun Node.times(y: Node): Node {
        return productOf(this,y)
    }

    operator fun Node.times(y: Int): Node {
        return productOf(this, intOf(y))
    }

    operator fun Int.times(y: Node): Node {
        return productOf(intOf(this), y)
    }

    fun pow(base: Node, exp: Node): Node {
        return Node2T(Symbols.POW, base, exp)
    }

    fun pow(base: Node, exp: Int): Node {
        return pow(base, intOf(exp))
    }

    fun inv(node: Node): Node {
        return pow(node, SymAlg.NEG_ONE)
    }

    fun divide(x: Node, y: Node): Node {
        return productOf(x, inv(y))
    }

    operator fun Node.div(y: Node): Node {
        return productOf(this, inv(y))
    }

    operator fun Node.div(y: Int): Node {
        return divide(this, intOf(y))
    }

    fun sqrt(x: Node): Node {
        return pow(x, SymAlg.HALF)
    }

    fun exp(x: Node): Node {
        return pow(naturalE, x)
    }

    fun log(base: Node, x: Node): Node {
        return Node2T(Symbols.F2_LOG, base, x)
    }

    fun ln(x: Node): Node {
        return log(naturalE, x)
    }

    fun sin(x: Node): Node {
        return Node1T(Symbols.F1_SIN, x)
    }

    fun cos(x: Node): Node {
        return Node1T(Symbols.F1_COS, x)
    }

    fun tan(x: Node): Node {
        return Node1T(Symbols.F1_TAN, x)
    }

    fun arcsin(x: Node): Node {
        return Node1T(Symbols.F1_ARCSIN, x)
    }

    fun arccos(x: Node): Node {
        return Node1T(Symbols.F1_ARCCOS, x)
    }

    fun arctan(x: Node): Node {
        return Node1T(Symbols.F1_ARCTAN, x)
    }

    fun abs(x: Node): Node {
        return Node1T(Symbols.F1_ABS, x)
    }


    infix fun Node.gtr(y: Node): Node {
        return Node2T(Symbols.F2_GTR, this, y)
    }

    infix fun Node.geq(y: Node): Node {
        return Node2T(Symbols.F2_GEQ, this, y)
    }

    infix fun Node.leq(y: Node): Node {
        return Node2T(Symbols.F2_LEQ, this, y)
    }

    infix fun Node.`≥`(y: Node): Node {
        return Node2T(Symbols.F2_GEQ, this, y)
    }

    infix fun Node.`≤`(y: Node): Node {
        return Node2T(Symbols.F2_LEQ, this, y)
    }



    fun NodeScope.sum(x: Node, lower: Node = SymAlg.ONE, upper: Node = x, f: Node): Node {
        val range = SymSets.intRange(lower, upper)
        return qualifiedContainedRep(Symbols.SUM, x, range, f)
    }


    fun NodeScope.sum(lower: Node, upper: Node, varName: String = "i", f: (NSymbol) -> Node): Node {
        val range = SymSets.intRange(lower, upper)
        return qualifiedContained(Symbols.SUM, varName = varName, set = range, clause = f)
    }



//        inline fun IAlgebraScope.sum(lower: Node, upper: Node, varName: String = "i", f: (NSymbol) -> Node): Node {
//            val range = SymSets.intRange(lower, upper)
//            return qualifiedContained(
//                Symbols.SUM, varName = varName, set = range, clause = f
//            )
//        }

}

//class AlgebraScope(context: EContext) : AbstractNodeScope(context), NScopeExtAlgebra, NodeScopePredefinedSymbols


//inline fun buildAlg(context: EContext = EmptyEContext, builder: AlgebraScope.() -> Node): Node {
//    return AlgebraScope(context).builder()
//}

inline fun <R> alg(builder: SymAlg.() -> R): R {
    return SymAlg.Instance.builder()
}

//class AlgScopeMatcher(context: EContext) : AbstractNodeScopeMatcher(context), NodeScopeMatcher, NScopeExtAlgebra {
//    constructor(scope: NodeScopeMatcher) : this(scope.context)
//}
//
//data class AlgScopeMatchedReferred(override val context: EContext, override val matching: Matching) :
//    NScopeExtAlgebra, NodeScopeMatched {
//    constructor(scope: NodeScopeMatched) : this(scope.context, scope.matching)
//
//    override val namedSymbols: MutableMap<String, ESymbol> = mutableMapOf()
//}
//
//inline fun RuleBuilder.matchAlg(crossinline builder: AlgScopeMatcher.() -> Node) {
//    this.match { AlgScopeMatcher(this).builder() }
//}
//
//inline fun RuleBuilder.toAlg(crossinline builder: AlgScopeMatchedReferred.() -> Node) {
//    this.to { AlgScopeMatchedReferred(this).builder() }
//}

