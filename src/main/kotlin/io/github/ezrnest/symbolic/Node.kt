package io.github.ezrnest.symbolic
// created at 2024/10/01
import io.github.ezrnest.model.BigFraction
import io.github.ezrnest.util.all2
import java.math.BigInteger

sealed interface Node {
    val name: String

    var meta: Map<String, Any?>


    fun plainToString(): String

    fun treeToString(): String {
        return treeTo(StringBuilder()).toString()
    }

    fun <A : Appendable> treeTo(builder: A, level: Int = 0, indent: String = ""): A


    fun traverse(depth: Int = Int.MAX_VALUE, action: (Node) -> Unit)

    fun traversePostOrder(depth: Int = Int.MAX_VALUE, action: (Node) -> Unit)

    fun traverseLeveled(depth: Int = Int.MAX_VALUE, level: Int = 0, action: (Node, Int) -> Unit)

    fun traversePostOrderLeveled(depth: Int = Int.MAX_VALUE, level: Int = 0, action: (Node, Int) -> Unit)

    fun recurMap(depth: Int = Int.MAX_VALUE, action: (Node) -> Node): Node

    fun deepEquals(other: Node): Boolean


    companion object {


        val ZERO = Int(BigInteger.ZERO)

        val ONE = Int(BigInteger.ONE)

        val NEG_ONE = Int(BigInteger.ONE.negate())

        val PI = Symbol(Names.Symbol_PI)

        val NATURAL_E = Symbol(Names.Symbol_E)

        val IMAGINARY_UNIT = Symbol(Names.Symbol_I)

        fun Int(value: BigInteger): NRational {
            return NRational(BigFraction(value, BigInteger.ONE))
        }

        fun Rational(value: Rational): NRational {
            return NRational(value)
        }

        //
        fun Symbol(name: String): Node {
            return NSymbol(name)
        }
    }

    object Names {
        const val MUL = "*"
        const val ADD = "+"
        const val NAME_DIV = "/"
        const val POW = "^"


        const val Symbol_I = "𝑖"
        const val Symbol_E = "𝑒"
        const val Symbol_PI = "π"

    }
}


sealed interface LeafNode : Node {

    override val name: String
        get() = ""

    override fun traverse(depth: Int, action: (Node) -> Unit) {
        action(this)
    }

    override fun traversePostOrder(depth: Int, action: (Node) -> Unit) {
        action(this)
    }

    override fun traverseLeveled(depth: Int, level: Int, action: (Node, Int) -> Unit) {
        action(this, level)
    }

    override fun traversePostOrderLeveled(depth: Int, level: Int, action: (Node, Int) -> Unit) {
        action(this, level)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        return action(this)
    }

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).append(plainToString()).append(";  Meta: ").append(meta.toString()).appendLine()
        return builder
    }

}


sealed class AbstractNode : Node {
    final override var meta: Map<String, Any?> = emptyMap()
}


typealias Rational = BigFraction

class NRational(val value: Rational) : AbstractNode(), LeafNode {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NRational) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is NRational) return false
        return value == other.value
    }


    override fun plainToString(): String {
        if (value.deno == BigInteger.ONE) return value.nume.toString()
        return value.toString()
    }
}


class NSymbol(val ch: String) : AbstractNode(), LeafNode {

    override fun plainToString(): String {
        return ch
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NSymbol) return false
        return ch == other.ch
    }

    override fun hashCode(): Int {
        return ch.hashCode()
    }

    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is NSymbol) return false
        return ch == other.ch
    }
}


sealed interface NodeChilded : Node {
    val children: List<Node>

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).append(name).append(";  Meta: ").append(meta.toString()).appendLine()
        val newIndent = "$indent|  "
        children.forEach { it.treeTo(builder, level + 1, newIndent) }
        return builder
    }

    override fun plainToString(): String {
        return name + children.joinToString(prefix = "(", postfix = ")") { it.plainToString() }
    }

    override fun traverse(depth: Int, action: (Node) -> Unit) {
        action(this)
        if (depth > 0) {
            children.forEach { it.traverse(depth - 1, action) }
        }
    }

    override fun traversePostOrder(depth: Int, action: (Node) -> Unit) {
        if (depth > 0) {
            children.forEach { it.traversePostOrder(depth - 1, action) }
        }
        action(this)
    }

    override fun traverseLeveled(depth: Int, level: Int, action: (Node, Int) -> Unit) {
        action(this, level)
        if (depth > 0) {
            children.forEach { it.traverseLeveled(depth - 1, level + 1, action) }
        }
    }

    override fun traversePostOrderLeveled(depth: Int, level: Int, action: (Node, Int) -> Unit) {
        if (depth > 0) {
            children.forEach { it.traversePostOrderLeveled(depth - 1, level + 1, action) }
        }
        action(this, level)
    }

    fun newWithChildren(children: List<Node>): NodeChilded

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val res = newWithChildren(children.map { it.recurMap(depth - 1, action) })
        return action(res)
    }

    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is NodeChilded) return false
        if (name != other.name) return false
        val children = children
        val otherChildren = other.children
        if (children.size != otherChildren.size) return false
        return children.all2(otherChildren) { a, b -> a.deepEquals(b) }
    }

//    override fun sortWith(order: Comparator<Node>): Node {
//        val newChildren = children.map { it.sortWith(order) }.sortedWith(order)
//        return newWithChildren(newChildren).also { it.meta = meta }
//    }
}

interface Node1 : NodeChilded {
    val child: Node

    override val children: List<Node>
        get() = listOf(child)

    fun newWithChildren(child: Node): Node1

    override fun newWithChildren(children: List<Node>): NodeChilded {
        require(children.size == 1)
        return newWithChildren(children[0])
    }


    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is Node1) return false
        if (name != other.name) return false
        return child.deepEquals(other.child)
    }

//    override fun sortWith(order: Comparator<Node>): Node {
//        return newWithChildren(child.sortWith(order))
//    }
}

interface Node2 : NodeChilded {
    val first: Node
    val second: Node

    operator fun component1() = first
    operator fun component2() = second

    override val children: List<Node>
        get() = listOf(first, second)

    fun newWithChildren(first: Node, second: Node): Node2

    override fun newWithChildren(children: List<Node>): NodeChilded {
        require(children.size == 2)
        return newWithChildren(children[0], children[1])
    }


    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is Node2) return false
        if (name != other.name) return false
        return first.deepEquals(other.first) && second.deepEquals(other.second)
    }

}

interface Node3 : NodeChilded {
    val first: Node
    val second: Node
    val third: Node

    operator fun component1() = first
    operator fun component2() = second
    operator fun component3() = third


    override val children: List<Node>
        get() = listOf(first, second, third)

    fun newWithChildren(first: Node, second: Node, third: Node): Node3

    override fun newWithChildren(children: List<Node>): NodeChilded {
        require(children.size == 3)
        return newWithChildren(children[0], children[1], children[2])
    }

    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is Node3) return false
        if (name != other.name) return false
        return first.deepEquals(other.first) && second.deepEquals(other.second) && third.deepEquals(other.third)
    }
}

interface NodeN : NodeChilded {
    override val children: List<Node>

}


//interface EMutableNode : ENode
//
//interface EMutableNodeChilded : ENodeChilded, EMutableNode {
//}
//
//interface EMutableNode1 : ENode1, EMutableNodeChilded {
//    override var child: ENode
//}
//
//interface EMutableNode2 : ENode2, EMutableNodeChilded {
//    override var first: ENode
//    override var second: ENode
//}
//
//interface EMutableNode3 : ENode3, EMutableNodeChilded {
//    override var first: ENode
//    override var second: ENode
//    override var third: ENode
//}
//
//interface EMutableNodeN : ENodeN, EMutableNodeChilded {
//    override var children: List<ENode>
//}


data class Node1Impl(
    override val name: String,
    override var child: Node
) : AbstractNode(), Node1 {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node1Impl) return false
        return name == other.name && child == other.child
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + child.hashCode()
        return result
    }

    override fun newWithChildren(child: Node): Node1 {
        return Node1Impl(name, child)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newChild = child.recurMap(depth - 1, action)
        if (newChild === child) return action(this)
        return action(Node1Impl(name, newChild))
    }
}

//data class EFraction(val nume)

data class Node2Impl(
    override val name: String,
    override var first: Node, override var second: Node
) : AbstractNode(), Node2 {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node2Impl) return false
        return name == other.name && first == other.first && second == other.second
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + first.hashCode()
        result = 31 * result + second.hashCode()
        return result
    }

    override fun newWithChildren(first: Node, second: Node): Node2 {
        return Node2Impl(name, first, second)
    }


    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newFirst = first.recurMap(depth - 1, action)
        val newSecond = second.recurMap(depth - 1, action)
        if (newFirst === first && newSecond === second) return action(this)
        return action(Node2Impl(name, newFirst, newSecond))
    }
}

data class Node3Impl(
    override val name: String,
    override var first: Node, override var second: Node, override var third: Node
) : AbstractNode(), Node3 {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node3Impl) return false
        return name == other.name && first == other.first && second == other.second && third == other.third
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + first.hashCode()
        result = 31 * result + second.hashCode()
        result = 31 * result + third.hashCode()
        return result
    }

    override fun newWithChildren(first: Node, second: Node, third: Node): Node3 {
        return Node3Impl(name, first, second, third)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newFirst = first.recurMap(depth - 1, action)
        val newSecond = second.recurMap(depth - 1, action)
        val newThird = third.recurMap(depth - 1, action)
        if (newFirst === first && newSecond === second && newThird === third) return action(this)
        return action(Node3Impl(name, newFirst, newSecond, newThird))
    }
}

data class NodeNImpl(
    override val name: String,
    override var children: List<Node>
) : AbstractNode(), NodeN {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NodeNImpl) return false
        return name == other.name && children == other.children
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }

    override fun newWithChildren(children: List<Node>): NodeChilded {
        return NodeNImpl(name, children)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        var changed = false
        val newChildren = children.map { it.recurMap(depth - 1, action).also { if (it !== children) changed = true } }
        if (!changed) return action(this)
        return action(NodeNImpl(name, newChildren))
    }
}

object NodeBuilderScope {
    val x = "x".s
    val y = "y".s
    val z = "z".s
    val a = "a".s
    val b = "b".s

    val context = TestExprContext


    val Int.e: Node get() = Node.Int(BigInteger.valueOf(this.toLong()))

    val String.s: Node get() = NSymbol(this)


    operator fun Node.plus(other: Node): Node {
        return context.Add(listOf(this, other))
    }

    operator fun Node.minus(other: Node): Node {
        return context.Add(listOf(this, context.Mul(listOf(Node.NEG_ONE, other))))
    }


    operator fun Node.times(other: Node): Node {
        return context.Mul(listOf(this, other))
    }

    fun exp(base: Node, exp: Node): Node {
        return context.Node2(Node.Names.POW, base, exp)
    }

    fun exp(x: Node): Node {
        return exp(Node.NATURAL_E, x)
    }

    fun sin(node: Node): Node {
        return context.Node1("sin", node)
    }

    fun cos(node: Node): Node {
        return context.Node1("cos", node)
    }

}

