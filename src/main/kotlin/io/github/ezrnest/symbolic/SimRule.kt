package io.github.ezrnest.symbolic

import io.github.ezrnest.symbolic.Node.Names

// created at 2024/10/1

interface SimRule {
    val description: String

    val metaInfoKey: MetaKey<Boolean>
        get() = MetaKey<Boolean>(description)

    fun simplify(node: Node, context: ExprContext): Node?
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
        return context.NodeN(targetName, node.children).also {
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
        return context.NodeN(targetName, newChildren)
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
        return context.Add(newChildren)

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
        if (expList.size == 1) {
            val exp = expList[0]
            if (exp == Node.ONE) return base
            return context.simplifyNode(context.Pow(base, exp), 0)
        }
        with(context) {
            val exp = simplifyNode(Add(expList), 0)
            return simplifyNode(Pow(base, exp), 0)
        }
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
        return context.Mul(newChildren)
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
        if(count == 1 && !Q.isOne(product)) return null // only one rational that can't be simplified
        if(!Q.isOne(product)){
            nodes.add(Node.Rational(product))
        }
        val res = context.Mul(nodes)
        res[metaInfoKey] = true
        return res
//        return context.Mul(nodes).also {
//            it[metaInfoKey] = true
//        }
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
object FlattenExp : RuleForSpecific2(Names.POW) {
    // created at 2024/10/05
    override val metaInfoKey: MetaKey<Boolean> = MetaKey("FlattenExp")

    override val description: String
        get() = "Flatten exp"

    override fun simplify2(root: Node2, context: ExprContext): Node? {
        TODO()
    }
}