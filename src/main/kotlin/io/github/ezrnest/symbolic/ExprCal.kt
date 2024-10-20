package io.github.ezrnest.symbolic

import io.github.ezrnest.structure.Reals
import io.github.ezrnest.symbolic.sim.RulesExponentialReduce
import io.github.ezrnest.symbolic.sim.RulesTrigonometricReduce
import io.github.ezrnest.symbolic.sim.addAll


class ExprCalReal : BasicExprCal(), Reals<Node> {

    init{
        options[ExprContext.Options.forceReal] = true

        addAll(RulesTrigonometricReduce())
        addAll(RulesExponentialReduce())

    }

    override fun constantValue(name: String): Node {
        return when (name) {
            "pi", Node.Names.Symbol_PI -> Node.PI
            "e", Node.Names.Symbol_E -> Node.NATURAL_E
            else -> throw IllegalArgumentException("Unknown constant: $name")
        }
    }

    override val one: Node
        get() = Node.ONE

    override val zero: Node
        get() = Node.ZERO


    override fun contains(x: Node): Boolean = true // TODO

    override fun isEqual(x: Node, y: Node): Boolean {
        TODO("Not yet implemented")
    }

    override fun negate(x: Node): Node {
        return simplify(Node.Neg(x), 0)
    }

    override fun add(x: Node, y: Node): Node {
        return simplify(Node.Add(x, y), 0)
    }

    override fun sum(elements: List<Node>): Node {
        return simplify(Node.Add(elements), 0)
    }

    override fun multiply(x: Node, y: Node): Node {
        return simplify(Node.Mul(x, y), 0)
    }

    override fun reciprocal(x: Node): Node {
        return simplify(Node.Inv(x), 0)
    }

    override fun divide(x: Node, y: Node): Node {
        return simplify(Node.Div(x, y), 1)
    }

    override fun compare(o1: Node, o2: Node): Int {
        TODO("Not yet implemented")
    }

    override fun sqrt(x: Node): Node {
        return simplify(Node.Pow(x, Node.HALF), 0)
    }

    override fun exp(x: Node): Node {
        return simplify(Node.Exp(x), 0)
    }

    override fun ln(x: Node): Node {
        return simplify(Node.Ln(x), 0)
    }

    override fun sin(x: Node): Node {
        return simplify(Node.Sin(x), 0)
    }

    override fun cos(x: Node): Node {
        return simplify(Node.Cos(x), 0)
    }

    override fun arcsin(x: Node): Node {
        return simplify(Node.ArcSin(x), 0)
    }

    override fun tan(x: Node): Node {
        return simplify(Node.Tan(x), 0)
    }

    override fun Node.div(y: Node): Node {
        return divide(this, y)
    }

    override fun Node.times(y: Node): Node {
        return multiply(this, y)
    }

    override fun product(elements: List<Node>): Node {
        return simplify(Node.Mul(elements), 0)
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

    override fun exp(base: Node, pow: Node): Node {
        return simplify(Node.Pow(base, pow), 0)
    }

    override fun nroot(x: Node, n: Int): Node {
        return simplify(Node.Pow(x, rational(1, n)), 0)
    }

    override fun log(base: Node, x: Node): Node {
        return simplify(Node.Log(base, x), 0)
    }

    override fun cot(x: Node): Node {
        return simplify(Node.Cot(x), 0)
    }

    override fun arccos(x: Node): Node {
        return simplify(Node.ArcCos(x), 0)
    }

    override fun arctan(x: Node): Node {
        return simplify(Node.ArcTan(x), 0)
    }

    override fun arctan2(y: Node, x: Node): Node {
        TODO()
    }
}