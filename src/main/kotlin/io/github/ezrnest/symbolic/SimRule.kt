package io.github.ezrnest.symbolic

import io.github.ezrnest.symbolic.Node.Names

// created at 2024/10/1

interface SimRule {
    val description: String

    val metaInfoKey: MetaKey<Boolean>
        get() = MetaKey<Boolean>(description)

    fun simplify(node: Node, context: ExprContext): Node?
}

object RuleSort : SimRule {
    override val description: String = "Sort"

    override val metaInfoKey: MetaKey<Boolean>
        get() = MetaKey("sorted")

    private fun sort2(node: Node2, context: ExprContext): Node2 {
        val (first, second) = node
        if (context.nodeOrder.compare(first, second) <= 0) return node
        return Node.Node2(node.name, second, first)
    }


    override fun simplify(node: Node, context: ExprContext): Node {
        if (node !is NodeChilded) return node
        if (!context.isCommutative(node.name)) return node
        return when (node) {
            is Node1 -> node
            is Node2 -> sort2(node, context)
            else -> {
                val childrenSorted = node.children.sortedWith(context.nodeOrder)
                node.newWithChildren(childrenSorted)
            }
        }
    }

}

abstract class RuleForSpecificChilded(val targetName: String) : SimRule {


    protected abstract fun simplifyChilded(node: NodeChilded, context: ExprContext): Node?

    override fun simplify(node: Node, context: ExprContext): Node? {
        if (node.name != targetName || node !is NodeChilded || node[metaInfoKey] == true)
            return null
        node[metaInfoKey] = true
        return simplifyChilded(node, context)
    }
}

class RegularizeNodeN(targetName: String) : RuleForSpecificChilded(targetName) {
    override val description: String = "Regularize $targetName"

    override val metaInfoKey: MetaKey<Boolean> = MetaKey("Reg${targetName}")

    override fun simplifyChilded(node: NodeChilded, context: ExprContext): Node? {
        if (node is NodeN) return null
        return Node.NodeN(targetName, node.children).also {
            it[metaInfoKey] = true
        }
    }
}


abstract class RuleForSpecificN(targetName: String) : RuleForSpecificChilded(targetName) {

    final override fun simplifyChilded(node: NodeChilded, context: ExprContext): Node? {
        if (node !is NodeN) return null
        return simplifyN(node, context)
    }

    protected abstract fun simplifyN(root: NodeN, context: ExprContext): Node?
}


class Flatten(targetName: String) : RuleForSpecificN(targetName) {
    // created at 2024/10/01
    override val description: String = "Flatten${targetName}"


    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        if (children.size == 1) return children[0]
        if (children.all { !(it is NodeN && it.name == targetName) }) return null
        val newChildren = children.flatMap {
            if (it is NodeN && it.name == targetName) it.children else listOf(it)
        }
        return Node.NodeN(targetName, newChildren)
    }
}

/**
 * ```
 * a + a -> 2a
 * ```
 */
class MergeAdditionRational : RuleForSpecificN(Names.ADD) {
    // created at 2024/10/05


    override val metaInfoKey: MetaKey<Boolean> = MetaKey("Merge+")


    override val description: String
        get() = "Merge addition rational"

    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        val Q = context.rational
        val collect = sortedMapOf<Node, Rational>(context.nodeOrder)
        var simplified = false
        for (node in children) {
            val (r, n) = SimUtils.extractRational(node, context)
            val t = collect[n]
            if (t == null) {
                collect[n] = r
            } else {
                simplified = true
                val newR = Q.add(t, r)
                if (Q.isZero(newR)) {
                    collect.remove(n)
                } else {
                    collect[n] = newR
                }
            }
        }
        if (!simplified) return null
        if (collect.isEmpty()) return Node.ZERO
        if (collect.size == 1) {
            val (n, r) = collect.entries.first()
            return SimUtils.createWithRational(r, n, context)
        }

        val newChildren = collect.entries.map { (n, r) -> SimUtils.createWithRational(r, n, context) }
        return Node.Add(newChildren)

    }

}

/**
 * ```
 * x * x -> x^2
 * ```
 */
class MergeProduct : RuleForSpecificN(Names.MUL) {
    // created at 2024/10/05

    override val description: String
        get() = "Merge product"

    override val metaInfoKey: MetaKey<Boolean> = MetaKey("Merge*")

    private fun buildPower(base: Node, expList: List<Node>, context: ExprContext): Node {
        var exp = if (expList.size == 1) {
            expList[0]
        } else {
            Node.Add(expList)
        }
        exp = context.simplifyNode(exp, 0)
        if (exp == Node.ONE) return base
        val res = if (base == Node.NATURAL_E) Node.Exp(exp) else Node.Pow(base, exp)
        return context.simplifyNode(res, 0)
    }

    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        val collect = sortedMapOf<Node, List<Node>>(context.nodeOrder)
        var simplified = false
        for (node in children) {
            val (base, exp) = SimUtils.asPower(node, context)
            val t = collect[base]
            if (t == null) {
                collect[base] = listOf(exp)
            } else {
                simplified = true
                collect[base] = t + exp
            }
        }
        if (!simplified) return null
        if (collect.size == 1) {
            val (base, expList) = collect.entries.first()
            return buildPower(base, expList, context)
        }
        val newChildren = collect.entries.map { (base, expList) -> buildPower(base, expList, context) }
        return Node.Mul(newChildren)
    }

}


/**
 * ```
 * 1 * 2 * 3 -> 6
 * 1 * x -> x
 * ```
 */
object ComputeProduct : RuleForSpecificN(Names.MUL) {
    // created at 2024/10/05
    override val metaInfoKey: MetaKey<Boolean> = MetaKey("Compute*")

    override val description: String
        get() = "Compute product"


    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        val children = root.children
        val Q = context.rational
        var product = Q.one
        val nodes = ArrayList<Node>(children.size)
        var count = 0
        for (node in children) {
            val r = SimUtils.asRational(node, Q)
            if (r == null) {
                nodes.add(node)
            } else {
                count++
                product = Q.multiply(product, r)
            }
        }
        if (count == 0) return null  // no rational to compute
        if (Q.isZero(product)) return Node.ZERO
        if (nodes.isEmpty()) return Node.Rational(product) // only rational
        if (count == 1 && !Q.isOne(product)) return null // only one rational that can't be simplified
        if (!Q.isOne(product)) {
            nodes.add(Node.Rational(product))
        }
        return Node.Mul(nodes).also { it[metaInfoKey] = true }
    }

}

abstract class RuleForSpecific2(targetName: String) : RuleForSpecificChilded(targetName) {
    final override fun simplifyChilded(node: NodeChilded, context: ExprContext): Node? {
        if (node !is Node2) return null
        return simplify2(node, context)
    }

    protected abstract fun simplify2(root: Node2, context: ExprContext): Node?
}

/**
 * ```
 * exp(exp(x,2),3) -> exp(x,6)
 * ```
 */
object FlattenPow : RuleForSpecific2(Names.POW) {
    // created at 2024/10/05
    override val metaInfoKey: MetaKey<Boolean> = MetaKey("FlattenPow")

    override val description: String
        get() = "Flatten pow"

    override fun simplify2(root: Node2, context: ExprContext): Node? {
        TODO()
    }
}

/**
 * ```
 * pow(r, n) -> r^n
 * pow(r, p/q) -> pow(r^p, 1/q)
 * ```
 */
object ComputePow : RuleForSpecific2(Names.POW) {
    // created at 2024/10/05
    override val metaInfoKey: MetaKey<Boolean> = MetaKey("ComputePow")

    override val description: String
        get() = "Compute pow"

    private fun simplifyPow(base: Rational, exp: Rational, context: ExprContext): Node {
        val Q = context.rational
        if (Q.isInteger(exp)) {
            val p = Q.asInteger(exp)
            val res = Q.power(base, p)
            return Node.Rational(res)
        }
        val (nume,deno) = exp
        TODO()
    }

    override fun simplify2(root: Node2, context: ExprContext): Node? {
        val (base, exp) = root
        if (base !is NRational || exp !is NRational) return null
        return simplifyPow(base.value, exp.value, context)
    }
}