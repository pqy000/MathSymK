package io.github.ezrnest.symbolic
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
        return context.NodeN(targetName, node.children)
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

class MergeAdditionRational : RuleForSpecificN(Node.Names.ADD) {
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

class MergeProduct : RuleForSpecificN(Node.Names.MUL) {
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

