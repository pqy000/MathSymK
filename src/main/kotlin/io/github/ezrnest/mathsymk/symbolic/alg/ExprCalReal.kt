package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.structure.Reals
import io.github.ezrnest.mathsymk.symbolic.BasicExprCal
import io.github.ezrnest.mathsymk.symbolic.ExprCal
import io.github.ezrnest.mathsymk.symbolic.Node


class ExprCalReal : BasicExprCal(), Reals<Node>, IAlgebraScope {

//    private fun addAllReduce(rules: RuleList) {
//        rules.list.forEach { addReduceRule(it) }
//    }


    init {
        options[ExprCal.Options.forceReal] = true

        registerReduceRuleAll(RulesTrigonometricReduce())
        registerReduceRuleAll(RulesExponentialReduce())

    }

    override fun constantValue(name: String): Node {
        return when (name) {
            "pi", SymAlg.Names.Symbol_PI -> SymAlg.PI
            "e", SymAlg.Names.Symbol_E -> SymAlg.NATURAL_E
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
        return reduce(super<IAlgebraScope>.sum(x, y), 0)
    }

    override fun sum(elements: List<Node>): Node {
        return reduce(super<IAlgebraScope>.sum(elements), 0)
    }

    override fun multiply(x: Node, y: Node): Node {
        return reduce(super.multiply(x, y), 0)
    }

    override fun product(elements: List<Node>): Node {
        return super<IAlgebraScope>.product(elements).also { reduce(it, 0) }
    }

    override fun reciprocal(x: Node): Node {
        return super<IAlgebraScope>.inv(x).also { reduce(it, 0) }
    }

    override fun divide(x: Node, y: Node): Node {
        return super<IAlgebraScope>.divide(x, y).also { reduce(it, 1) }
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
        return super<IAlgebraScope>.sqrt(x).also { reduce(it, 0) }
    }

    override fun exp(x: Node): Node {
        return super<IAlgebraScope>.exp(x).also { reduce(it, 0) }
    }

    override fun exp(base: Node, pow: Node): Node {
        return super<IAlgebraScope>.pow(base, pow).also { reduce(it, 0) }
    }

    override fun pow(base: Node, exp: Node): Node {
        return super<IAlgebraScope>.pow(base, exp).also { reduce(it, 0) }
    }

    override fun nroot(x: Node, n: Int): Node {
        return pow(x, rational(1, n))
    }

    override fun ln(x: Node): Node {
        return super<IAlgebraScope>.ln(x).also { reduce(it, 0) }
    }

    override fun log(base: Node, x: Node): Node {
        return super<IAlgebraScope>.log(base, x).also { reduce(it, 0) }
    }

    override fun sin(x: Node): Node {
        return super<IAlgebraScope>.sin(x).also { reduce(it, 0) }
    }

    override fun cos(x: Node): Node {
        return super<IAlgebraScope>.cos(x).also { reduce(it, 0) }
    }

    override fun tan(x: Node): Node {
        return super<IAlgebraScope>.tan(x).also { reduce(it, 0) }
    }

    //    override fun cot(x: Node): Node {
//        return super<NodeScopeAlg>.cot(x).also { reduce(it, 0) }
//    }

    override fun arcsin(x: Node): Node {
        return super<IAlgebraScope>.arcsin(x).also { reduce(it, 0) }
    }

    override fun arccos(x: Node): Node {
        return super<IAlgebraScope>.arccos(x).also { reduce(it, 0) }
    }

    override fun arctan(x: Node): Node {
        return super<IAlgebraScope>.arctan(x).also { reduce(it, 0) }
    }

    override fun arctan2(y: Node, x: Node): Node {
        TODO()
    }

    override fun compare(o1: Node, o2: Node): Int {
        TODO("Not yet implemented")
    }
}