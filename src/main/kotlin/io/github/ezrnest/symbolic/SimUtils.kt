package io.github.ezrnest.symbolic

import io.github.ezrnest.model.BigFractionAsQuotients
import java.math.BigInteger

object SimUtils {

    fun checkInteger(node: Node, Q: BigFractionAsQuotients): BigInteger? {
        return if (node is NRational && Q.isInteger(node.value)) {
            Q.asInteger(node.value)
        } else {
            null
        }
    }


    fun checkRational(node: Node, Q: BigFractionAsQuotients): Rational? {
        return if (node is NRational) {
            node.value
        } else {
            null
        }
    }


    fun withRational(r: Rational, n: Node, Q: BigFractionAsQuotients): Node {
        if (n === Node.ONE) return Node.Rational(r)
        if (n is NRational) return Node.Rational(Q.multiply(r, n.value))
        return if (Q.isOne(r)) n else Node.Mul(listOf(Node.Rational(r), n))
    }
}