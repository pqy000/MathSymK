package io.github.ezrnest.symbolic

interface SimRule {
    val description: String

    fun simplify(node: Node, context: ExprContext): Node?
}

abstract class RuleForSpecificChilded(val targetName: String) : SimRule {
    protected abstract fun simplifyChilded(node: NodeChilded, context: ExprContext): Node?

    override fun simplify(node: Node, context: ExprContext): Node? {
        if (node.name != targetName || node !is NodeChilded) return null
        return simplifyChilded(node, context)
    }
}

class RegularizeNodeN(targetName: String) : RuleForSpecificChilded(targetName) {
    override val description: String = "Regularize $targetName"

    override fun simplifyChilded(node: NodeChilded, context: ExprContext): Node? {
        if (node is NodeN) return null
        return Node.NodeN(targetName, node.children)
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

    override val description: String = "Flatten $targetName"


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

class MergeAdditionRational : RuleForSpecificN(Node.NAME_ADD) {

    val metaInfo = MetaKey<Boolean>("mergeAdditionRational")


    private fun extractRational(node: Node, context: ExprContext): Pair<Rational, Node> {
        if(node is NRational) return Pair(node.value, Node.ONE) // itself is a rational
        if (node.name != Node.NAME_MUL || node !is NodeN) return Pair(context.rational.one, node) // not a mul node
        val children = node.children
        val Q = context.rational
        val rational = SimUtils.checkRational(children.first(), Q) ?: return Pair(Q.one, node)
        return when (children.size) {
            1 -> Pair(rational, Node.ONE)
            2 -> Pair(rational, children[1])
            else -> Pair(rational, Node.Mul(children.subList(1, children.size)))
        }
    }

    override fun simplifyN(root: NodeN, context: ExprContext): Node? {
        if (root[metaInfo] == true) return null
        val children = root.children
        val Q = context.rational
        val collect = sortedMapOf<Node, Rational>(context.nodeOrder)
        var simplified = false
        for (node in children) {
            val (r, n) = extractRational(node, context)
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
        root[metaInfo] = true
        if (!simplified) return null

        if (collect.isEmpty()) return Node.ZERO
        if (collect.size == 1) {
            val (n, r) = collect.entries.first()
            return SimUtils.withRational(r, n, Q)
        }

        val newChildren = collect.entries.map { (n, r) -> SimUtils.withRational(r, n, Q) }
        return Node.Add(newChildren)

    }

    override val description: String
        get() = "Merge addition"
}



