package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.util.WithInt
import io.github.ezrnest.mathsymk.util.all2

// created at 2024/10/1
interface SimRule : TransRule {
    override val description: String

    /**
     * The key for marking the node as tried by the rule but not applicable.
     * This can be used to avoid trying the same rule again.
     */
    override val metaKeyApplied: TypedKey<Boolean>

    fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>?

    override fun transform(node: Node, ctx: EContext, cal: ExprCal): List<WithInt<Node>> {
        val res = simplify(node, ctx, cal) ?: return emptyList()
        return listOf(res)
    }

    override val matcher: NodeMatcherT<Node>
        get() = AnyMatcher


    override fun init(cal: ExprCal): SimRule? {
        return this
    }
}


class RuleSort(val targetSym: ESymbol) : SimRule {

    override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig(targetSym)

    override val description: String = "Sort"

    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("sorted")

    private fun sort2(node: Node2): Node2? {
        val (first, second) = node
        if (NodeOrder.compare(first, second) <= 0) {
            node[metaKeyApplied] = true
            return null
        }
        return node.newWithChildren(second, first).also { it[metaKeyApplied] = true }
    }

    private fun sortN(node: NodeChilded, context: EContext): NodeChilded? {
        val children = node.children
        val childrenSorted = children.sortedWith(NodeOrder)
        if (children.all2(childrenSorted) { x, y -> x === y }) {
            node[metaKeyApplied] = true
            return null
        }
        return node.newWithChildren(childrenSorted).also { it[metaKeyApplied] = true }
    }


    override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        if (node !is NodeChilded) return null
        if (!cal.isCommutative(node.symbol)) return null
        return when (node) {
            is Node1 -> null
            is Node2 -> sort2(node)?.let { WithInt(0, it) }
            else -> sortN(node, ctx)?.let { WithInt(0, it) }
        }
    }
}


abstract class RuleForSpecificName(val targetSym: ESymbol) : SimRule {
    protected inline fun <reified T : NodeChilded> simplifyNodeTyped(
        node: Node, nextSimplification: (T) -> WithInt<Node>?
    ): WithInt<Node>? {
        if (node !is T || node.symbol != targetSym ||  node[metaKeyApplied] == true)
            return null
        val res = nextSimplification(node)
        if (res != null) return res
        node[metaKeyApplied] = true // tried but not applicable
        return null
    }
}


//class RegularizeNodeN(targetName: String) : RuleForSpecificName(targetName) {
//    override val description: String = "Regularize $targetName"
//
//    override val metaKeyNotApplicable: TypedKey<Boolean> = TypedKey("Reg[$targetName]")
//
//    override fun simplify(node: Node, context: ExprContext): Node? {
//        if (node.name != targetName || node !is NodeChilded || node is NodeN)
//            return null
//        return Node.NodeN(targetName, node.children)
//        // no need for the metaKeyNotApplicable since it is always applicable
//    }
//}


abstract class RuleForSpecific1(targetName: ESymbol) : RuleForSpecificName(targetName) {
    final override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig(targetName)

    final override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        return simplifyNodeTyped<Node1>(node) { n -> simplify1(n, ctx, cal) }
    }

    protected abstract fun simplify1(root: Node1, context: EContext, cal: ExprCal): WithInt<Node>?
}

abstract class RuleForSpecific2(targetName: ESymbol) : RuleForSpecificName(targetName) {
    final override val matcher: NodeMatcherT<Node> = LeafMatcherFixSig(targetName)

    final override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        return simplifyNodeTyped<Node2>(node) { n -> simplify2(n, ctx, cal) }
    }

    protected abstract fun simplify2(root: Node2, context: EContext, cal: ExprCal): WithInt<Node>?
}



abstract class RuleForSpecificN(targetName: ESymbol) : RuleForSpecificName(targetName) {

    final override val matcher: NodeMatcherT<NodeChilded> = LeafMatcherFixSig(targetName)

    final override fun simplify(node: Node, ctx: EContext, cal: ExprCal): WithInt<Node>? {
        return simplifyNodeTyped<NodeN>(node) { n -> simplifyN(n, ctx, cal) }
    }

    abstract fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>?
}


class Flatten(targetName: ESymbol) : RuleForSpecificN(targetName) {
    // created at 2024/10/01
    override val description: String = "Flatten $targetName"
    override val metaKeyApplied: TypedKey<Boolean> = TypedKey("Flatten${targetName}")

    override fun simplifyN(root: NodeN, context: EContext, cal: ExprCal): WithInt<Node>? {
        val children = root.children
        if (children.size == 1) return WithInt(0, children[0])
        if (children.all { !(it is NodeN && it.symbol == targetSym) }) return null
        val newChildren = children.flatMap {
            if (it is NodeN && it.symbol == targetSym) it.children else listOf(it)
        }
        val res = NodeN(targetSym, newChildren).also { it[metaKeyApplied] = true }
        return WithInt(0, res)
    }
}

