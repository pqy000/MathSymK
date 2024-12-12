package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.structure.Reals
import io.github.ezrnest.mathsymk.symbolic.BasicExprCal
import io.github.ezrnest.mathsymk.symbolic.ExprCal
import io.github.ezrnest.mathsymk.symbolic.Node


class ExprCalReal : BasicExprCal(), Reals<Node>, NScopeExtAlgebra {

//    private fun addAllReduce(rules: RuleList) {
//        rules.list.forEach { addReduceRule(it) }
//    }




    init {
        options[ExprCal.Options.forceReal] = true

        registerReduceRule(RulesExponentialReduce)
        registerReduceRule(RulesTrigonometricReduce)
//        registerReduceRuleAll(RulesExponentialReduce())

    }

    override fun constantValue(name: String): Node {
        return when (name) {
            "pi", SymAlg.Symbols.π.name -> SymAlg.PI
            "e", SymAlg.Symbols.Natural_e.name -> SymAlg.NATURAL_E
            else -> throw IllegalArgumentException("Unknown constant: $name")
        }
    }

    override val one: Node
        get() = SymAlg.ONE

    override val zero: Node
        get() = SymAlg.ZERO


    override fun contains(x: Node): Boolean = true // TODO

    override fun isEqual(x: Node, y: Node): Boolean {
        TODO("Not yet implemented")
    }

    override fun negate(x: Node): Node {
        return reduce(super.negate(x), 0)
    }

    override fun add(x: Node, y: Node): Node {
        return reduce(super<NScopeExtAlgebra>.sumOf(x, y), 0)
    }

    override fun sumOf(elements: List<Node>): Node {
        return reduce(super<NScopeExtAlgebra>.sumOf(elements), 0)
    }

    override fun multiply(x: Node, y: Node): Node {
        return reduce(super.multiply(x, y), 0)
    }

    override fun product(elements: List<Node>): Node {
        return reduce(super<NScopeExtAlgebra>.product(elements), 0)
    }

    override fun inv(node: Node): Node {
        return reduce(super<NScopeExtAlgebra>.inv(node), 0)
    }

    override fun reciprocal(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.inv(x), 0)
    }

    override fun divide(x: Node, y: Node): Node {
        return reduce(super<NScopeExtAlgebra>.divide(x, y), 1)
    }

    override fun Node.div(y: Node): Node {
        return divide(this, y)
    }

    override fun Node.times(y: Node): Node {
        return multiply(this, y)
    }

    override fun Node.minus(y: Node): Node {
        return subtract(this, y)
    }

    override fun Node.unaryMinus(): Node {
        return negate(this)
    }

    override fun Node.plus(y: Node): Node {
        return add(this, y)
    }


    override fun sqrt(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.sqrt(x), 0)
    }

    override fun exp(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.exp(x), 0)
    }

    override fun exp(base: Node, pow: Node): Node {
        return reduce(super<NScopeExtAlgebra>.pow(base, pow), 0)
    }

    override fun pow(base: Node, exp: Node): Node {
        return reduce(super<NScopeExtAlgebra>.pow(base, exp), 0)
    }

    override fun nroot(x: Node, n: Int): Node {
        return pow(x, rational(1, n))
    }

    override fun ln(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.ln(x), 0)
    }

    override fun log(base: Node, x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.log(base, x), 0)
    }

    override fun sin(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.sin(x), 0)
    }

    override fun cos(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.cos(x), 0)
    }

    override fun tan(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.tan(x), 0)
    }

    //    override fun cot(x: Node): Node {
//        return super<NodeScopeAlg>.cot(x).let { reduce(it, 0) }
//    }

    override fun arcsin(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.arcsin(x), 0)
    }

    override fun arccos(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.arccos(x), 0)
    }

    override fun arctan(x: Node): Node {
        return reduce(super<NScopeExtAlgebra>.arctan(x), 0)
    }

    override fun arctan2(y: Node, x: Node): Node {
        TODO()
    }

    override fun compare(o1: Node, o2: Node): Int {
        TODO("Not yet implemented")
    }
}