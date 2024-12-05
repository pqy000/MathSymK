package io.github.ezrnest.mathsymk.symbolic
// created at 2024/10/01
import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.symbolic.alg.SymSets
import io.github.ezrnest.mathsymk.util.all2
import java.math.BigInteger
import kotlin.reflect.KClass

class ESymbol(
    /**
     * The display name of the symbol.
     */
    val name: String
) : Comparable<ESymbol> {
    override fun toString(): String {
        return if (displayHash) {
            // use first four digits of the hash code
            "$name@${hashCode().toString(16).take(4)}"
        } else name
    }

    override fun compareTo(other: ESymbol): Int {
        val nameComp = name.compareTo(other.name)
        if (nameComp != 0) return nameComp
        return hashCode().compareTo(other.hashCode())
    }

    companion object {
        var displayHash = false
    }
}

sealed interface Node {
    var meta: Map<TypedKey<*>, Any?>


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


    fun replace(src: Node, dest: Node): Node {
        return recurMap { if (it == src) dest else it }
    }

    fun deepEquals(other: Node): Boolean


    companion object {


    }
}


sealed interface LeafNode : Node {

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
        builder.append(indent).append(plainToString()).append(";  ").append(meta.toString()).appendLine()
        return builder
    }

}


sealed class AbstractNode : Node{
    override var meta: Map<TypedKey<*>, Any?> = emptyMap()

    override fun toString(): String {
        return plainToString()
    }
}


data class NRational(val value: BigFrac) : AbstractNode(), LeafNode {

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
        val (nume, deno) = value
        if (deno == BigInteger.ONE) return nume.toString()
        return "$nume/$deno"
    }
}


data class NSymbol(
    val symbol: ESymbol
) : AbstractNode(), LeafNode {

    override fun plainToString(): String {
        return symbol.toString()
    }

    override fun deepEquals(other: Node): Boolean {
        return this === other || other is NSymbol && symbol == other.symbol
    }
}

data class NOther(val name: String) : AbstractNode(), LeafNode {
    override fun plainToString(): String {
        return name
    }

    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is NOther) return false
        return name == other.name
    }
}

sealed interface NodeChilded : Node {
    val children: List<Node>
    val childCount: Int
    val symbol: ESymbol

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).append(symbol.toString()).append(";  ").append(meta.toString()).appendLine()
        val newIndent = "$indent|  "
        children.forEach { it.treeTo(builder, level + 1, newIndent) }
        return builder
    }

    override fun plainToString(): String {
        return symbol.toString() + children.joinToString(prefix = "(", postfix = ")") { it.plainToString() }
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
        if (symbol != other.symbol) return false
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

typealias Node1 = Node1T<*>

sealed interface Node1T<out C : Node> : NodeChilded {
    val child: C

    override val children: List<Node>
        get() = listOf(child)

    override val childCount: Int
        get() = 1

    fun <S : Node> newWithChildren(child: S): Node1T<S>

    override fun newWithChildren(children: List<Node>): NodeChilded {
        require(children.size == 1)
        return newWithChildren(children[0])
    }


    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is Node1) return false
        if (symbol != other.symbol) return false
        return child.deepEquals(other.child)
    }

    companion object {
        @JvmStatic
        operator fun <C : Node> invoke(symbol: ESymbol, child: C): Node1T<C> {
            return Node1Impl(child, symbol)
        }
    }
}

typealias Node2 = Node2T<*, *>

sealed interface Node2T<out C1 : Node, out C2 : Node> : NodeChilded {
    val first: C1
    val second: C2

    operator fun component1() = first
    operator fun component2() = second

    override val children: List<Node>
        get() = listOf(first, second)

    override val childCount: Int get() = 2

    fun <S1 : Node, S2 : Node> newWithChildren(first: S1, second: S2): Node2T<S1, S2>

    override fun newWithChildren(children: List<Node>): NodeChilded {
        require(children.size == 2)
        return newWithChildren(children[0], children[1])
    }


    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is Node2) return false
        if (symbol != other.symbol) return false
        return first.deepEquals(other.first) && second.deepEquals(other.second)
    }

    companion object{
        @JvmStatic
        operator fun <C1 : Node, C2 : Node> invoke(symbol: ESymbol, first: C1, second: C2): Node2T<C1, C2> {
            return Node2Impl(first, second, symbol)
        }
    }
}

typealias Node3 = Node3T<*, *, *>

data class Node3T<out C1 : Node, out C2 : Node, out C3 : Node>(
    val first: C1, val second: C2, val third: C3,
    override val symbol: ESymbol
) : AbstractNode(), NodeChilded {

    override fun toString(): String {
        return plainToString()
    }

    override val children: List<Node>
        get() = listOf(first, second, third)

    override val childCount: Int get() = 3

    override fun newWithChildren(children: List<Node>): NodeChilded {
        require(children.size == 3)
        return newWithChildren(children[0], children[1], children[2])
    }

    override fun deepEquals(other: Node): Boolean {
        if (this === other) return true
        if (other !is Node3) return false
        if (symbol != other.symbol) return false
        return first.deepEquals(other.first) && second.deepEquals(other.second) && third.deepEquals(other.third)
    }



    fun <S1 : Node, S2 : Node, S3 : Node> newWithChildren(first: S1, second: S2, third: S3): Node3T<S1, S2, S3> {
        return Node3T(first, second, third, symbol)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newFirst = first.recurMap(depth - 1, action)
        val newSecond = second.recurMap(depth - 1, action)
        val newThird = third.recurMap(depth - 1, action)
        if (newFirst === first && newSecond === second && newThird === third) return action(this)
        return action(Node3T(newFirst, newSecond, newThird, symbol))
    }

}

sealed interface NodeN : NodeChilded {
    override val children: List<Node>

    override val childCount: Int get() = children.size

    override fun newWithChildren(children: List<Node>): NodeN


    companion object {
        operator fun invoke(symbol: ESymbol, children: List<Node>): NodeN {
            return NodeNImpl(symbol, children)
        }
    }
}


internal data class Node1Impl<C : Node>(
    override val child: C,
    override val symbol: ESymbol
) : AbstractNode(), Node1T<C> {

    override fun toString(): String {
        return plainToString()
    }

    override fun <S : Node> newWithChildren(child: S): Node1T<S> {
        return Node1Impl(child, symbol)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newChild = child.recurMap(depth - 1, action)
        if (newChild === child) return action(this)
        return action(Node1Impl(newChild, symbol))
    }
}


internal data class Node2Impl<C1 : Node, C2 : Node>(
    override val first: C1, override val second: C2,
    override val symbol: ESymbol
) : AbstractNode(), Node2T<C1, C2> {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is Node2Impl<*, *>) return false
//        return name == other.name && first == other.first && second == other.second
//    }
//
//    override fun hashCode(): Int {
//        var result = name.hashCode()
//        result = 31 * result + first.hashCode()
//        result = 31 * result + second.hashCode()
//        return result
//    }

    override fun <S1 : Node, S2 : Node> newWithChildren(first: S1, second: S2): Node2T<S1, S2> {
        return Node2Impl(first, second, symbol)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newFirst = first.recurMap(depth - 1, action)
        val newSecond = second.recurMap(depth - 1, action)
        if (newFirst === first && newSecond === second) return action(this)
        return action(Node2Impl(newFirst, newSecond, symbol))
    }

    override fun toString(): String {
        return plainToString()
    }
}

//internal data class Node3Impl<C1 : Node, C2 : Node, C3 : Node>(
//    override
//) : AbstractNode(), Node3T<C1, C2, C3> {
//
//}

data class NodeNImpl(
    override val symbol: ESymbol,
    override val children: List<Node>
) : AbstractNode(), NodeN {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NodeNImpl) return false
        return symbol == other.symbol && children == other.children
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }

    override fun toString(): String {
        return plainToString()
    }

    override fun newWithChildren(children: List<Node>): NodeN {
        return NodeNImpl(symbol, children)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        var changed = false
        val newChildren = children.map { it.recurMap(depth - 1, action).also { if (it !== children) changed = true } }
        if (!changed) return action(this)
        return action(NodeNImpl(symbol, newChildren))
    }
}

