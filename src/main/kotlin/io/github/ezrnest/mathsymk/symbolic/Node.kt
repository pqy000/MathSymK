package io.github.ezrnest.mathsymk.symbolic
// created at 2024/10/01
import io.github.ezrnest.mathsymk.model.BigFrac
import io.github.ezrnest.mathsymk.model.BigFracAsQuot
import io.github.ezrnest.mathsymk.util.all2
import java.math.BigInteger
import kotlin.reflect.KClass

sealed interface Node {
    val name: String

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

    fun deepEquals(other: Node): Boolean


    companion object {


        val ZERO = Int(BigInteger.ZERO)

        val ONE = Int(BigInteger.ONE)

        val NEG_ONE = Int(BigInteger.ONE.negate())

        val HALF = Rational(BigFracAsQuot.half)

        val TWO = Int(BigInteger.TWO)

        val TEN = Int(BigInteger.TEN)

        val PI = Symbol(Names.Symbol_PI)

        val NATURAL_E = Symbol(Names.Symbol_E)

        val IMAGINARY_UNIT = Symbol(Names.Symbol_I)

        val UNDEFINED = NOther("undefined")

        fun Int(value: BigInteger): NRational {
            return NRational(BigFrac(value, BigInteger.ONE))
        }

        fun Int(value: Int): NRational {
            return Int(value.toBigInteger())
        }

        fun Rational(value: BigFrac): NRational {
            return NRational(value)
        }


        //
        fun Symbol(name: String): NSymbol {
            return NSymbol(name)
        }


        fun NodeN(name: String, children: List<Node>): Node {
            require(children.isNotEmpty())
            return NodeNImpl(name, children)
        }

        fun Add(nodes: List<Node>): Node {
            if (nodes.isEmpty()) return ZERO
            if (nodes.size == 1) return nodes[0]
            return NodeN(Names.ADD, nodes)
        }

        fun Add(vararg nodes: Node): Node {
            return Add(nodes.asList())
        }

        fun Mul(nodes: List<Node>): Node {
            if (nodes.isEmpty()) return ONE
            if (nodes.size == 1) return nodes[0]
            return NodeN(Names.MUL, nodes)
        }

        fun Mul(vararg nodes: Node): Node {
            return Mul(nodes.asList())
        }

        fun Div(numerator: Node, denominator: Node): Node {
            return Mul(numerator, Inv(denominator))
        }


        fun <T : Node> Node1(name: String, child: T): Node1T<T> {
            return Node1Impl(name, child)
        }

        fun <T1 : Node, T2 : Node> Node2(name: String, first: T1, second: T2): Node2T<T1, T2> {
            return Node2Impl(first, second, name)
        }

        fun <T1 : Node, T2 : Node, T3 : Node> Node3(
            name: String, first: T1, second: T2, third: T3
        ): Node3T<T1, T2, T3> {
            return Node3Impl(first, second, third, name)
        }

        fun Neg(child: Node): Node {
            return Mul(listOf(NEG_ONE, child))
        }

        fun Inv(child: Node): Node {
            return Pow(child, NEG_ONE)
        }

        fun Pow(base: Node, exp: Node): Node {
            return Node2(Names.POW, base, exp)
        }

        fun Exp(exp: Node): Node {
            return Pow(NATURAL_E, exp)
        }

        fun Sin(node: Node): Node {
            return Node1(Names.F1_SIN, node)
        }

        fun Cos(node: Node): Node {
            return Node1(Names.F1_COS, node)
        }

        fun Tan(node: Node): Node {
            return Node1(Names.F1_TAN, node)
        }

        fun Cot(node: Node): Node {
            return Node1(Names.F1_COT, node)
        }

        fun ArcSin(node: Node): Node {
            return Node1(Names.F1_ARCSIN, node)
        }

        fun ArcCos(node: Node): Node {
            return Node1(Names.F1_ARCCOS, node)
        }

        fun ArcTan(node: Node): Node {
            return Node1(Names.F1_ARCTAN, node)
        }

        fun Log(base: Node, x: Node): Node {
            return Node2(Names.F2_LOG, base, x)
        }

        fun Ln(node: Node): Node {
            return Log(NATURAL_E, node)
        }

        fun Log2(node: Node): Node {
            return Log(TWO, node)
        }

        fun Log10(node: Node): Node {
            return Log(TEN, node)
        }
    }

    object Names {
        const val MUL = "*"
        const val ADD = "+"

        //        const val NAME_DIV = "/"
        const val POW = "^"


        //        const val F1_EXP = "exp"
        const val F2_LOG = "log"


        const val Symbol_I = "𝑖"
        const val Symbol_E = "𝑒"
        const val Symbol_PI = "π"

        const val F1_SIN = "sin"
        const val F1_COS = "cos"
        const val F1_TAN = "tan"
        const val F1_COT = "cot"
        const val F1_ARCSIN = "arcsin"
        const val F1_ARCCOS = "arccos"
        const val F1_ARCTAN = "arctan"


    }
}


interface LeafNode : Node {

    override val name: String get() = ""

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


sealed class AbstractNode {
    var meta: Map<TypedKey<*>, Any?> = emptyMap()

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


data class NSymbol(val ch: String) : AbstractNode(), LeafNode {

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

data class NOther(override val name: String) : AbstractNode(), LeafNode {
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

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).append(name).append(";  ").append(meta.toString()).appendLine()
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

typealias Node1 = Node1T<*>

interface Node1T<out C : Node> : NodeChilded {
    val child: C

    override val children: List<Node>
        get() = listOf(child)

    fun <S : Node> newWithChildren(child: S): Node1T<S>

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
}

typealias Node2 = Node2T<*, *>

interface Node2T<out C1 : Node, out C2 : Node> : NodeChilded {
    val first: C1
    val second: C2

    operator fun component1() = first
    operator fun component2() = second

    override val children: List<Node>
        get() = listOf(first, second)

    fun <S1 : Node, S2 : Node> newWithChildren(first: S1, second: S2): Node2T<S1, S2>

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

typealias Node3 = Node3T<*, *, *>

interface Node3T<out C1 : Node, out C2 : Node, out C3 : Node> : NodeChilded {
    val first: C1
    val second: C2
    val third: C3

    operator fun component1() = first
    operator fun component2() = second
    operator fun component3() = third


    override val children: List<Node>
        get() = listOf(first, second, third)

    fun <S1 : Node, S2 : Node, S3 : Node> newWithChildren(first: S1, second: S2, third: S3): Node3T<S1, S2, S3>

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

    override fun newWithChildren(children: List<Node>): NodeN
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


data class Node1Impl<C : Node>(
    override val name: String,
    override val child: C
) : AbstractNode(), Node1T<C> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node1Impl<*>) return false
        return name == other.name && child == other.child
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + child.hashCode()
        return result
    }

    override fun <S : Node> newWithChildren(child: S): Node1T<S> {
        return Node1Impl(name, child)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newChild = child.recurMap(depth - 1, action)
        if (newChild === child) return action(this)
        return action(Node1Impl(name, newChild))
    }
}


class Node2Impl<C1 : Node, C2 : Node>(
    override val first: C1, override val second: C2,
    override val name: String
) : AbstractNode(), Node2T<C1, C2> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node2Impl<*, *>) return false
        return name == other.name && first == other.first && second == other.second
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + first.hashCode()
        result = 31 * result + second.hashCode()
        return result
    }

    override fun <S1 : Node, S2 : Node> newWithChildren(first: S1, second: S2): Node2T<S1, S2> {
        return Node2Impl(first, second, name)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newFirst = first.recurMap(depth - 1, action)
        val newSecond = second.recurMap(depth - 1, action)
        if (newFirst === first && newSecond === second) return action(this)
        return action(Node2Impl(newFirst, newSecond, name))
    }
}

data class Node3Impl<C1 : Node, C2 : Node, C3 : Node>(
    override val first: C1, override val second: C2, override val third: C3,
    override val name: String
) : AbstractNode(), Node3T<C1, C2, C3> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node3Impl<*, *, *>) return false
        return name == other.name && first == other.first && second == other.second && third == other.third
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + first.hashCode()
        result = 31 * result + second.hashCode()
        result = 31 * result + third.hashCode()
        return result
    }

    override fun <S1 : Node, S2 : Node, S3 : Node> newWithChildren(
        first: S1, second: S2, third: S3
    ): Node3T<S1, S2, S3> {
        return Node3Impl(first, second, third, name)
    }

    override fun recurMap(depth: Int, action: (Node) -> Node): Node {
        if (depth <= 0) return action(this)
        val newFirst = first.recurMap(depth - 1, action)
        val newSecond = second.recurMap(depth - 1, action)
        val newThird = third.recurMap(depth - 1, action)
        if (newFirst === first && newSecond === second && newThird === third) return action(this)
        return action(Node3Impl(newFirst, newSecond, newThird, name))
    }
}

data class NodeNImpl(
    override val name: String,
    override val children: List<Node>
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

    override fun newWithChildren(children: List<Node>): NodeN {
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


data class NodeSig(val name: String, val type: NType) : Comparable<NodeSig> {

    override fun toString(): String {
        return "$name:$type"
    }

    override fun compareTo(other: NodeSig): Int {
        type.compareTo(other.type).let {
            if (it != 0) return it
        }
        return name.compareTo(other.name)
    }

    /**
     * Describes the structural type of nodes.
     */
    enum class NType {
        Rational, Symbol, Leaf, Node1, Node2, Node3, NodeN;

        fun matches(node: Node): Boolean {
            return when (this) {
                Rational -> node is NRational
                Symbol -> node is NSymbol
                Leaf -> node is LeafNode
                Node1 -> node is Node1T<*>
                Node2 -> node is Node2T<*, *>
                Node3 -> node is Node3T<*, *, *>
                NodeN -> node is io.github.ezrnest.mathsymk.symbolic.NodeN
            }
        }
    }

    fun matches(node: Node): Boolean {
        return type.matches(node) && node.name == name
    }

    companion object {
        fun typeOf(node: Node): NType {
            return when (node) {
                is NRational -> NType.Rational
                is NSymbol -> NType.Symbol
                is LeafNode -> NType.Leaf
                is Node1 -> NType.Node1
                is Node2 -> NType.Node2
                is Node3 -> NType.Node3
                is NodeN -> NType.NodeN
                // all the cases are covered
            }
        }

        fun toType(type: KClass<Node>): NType {
            return when (type) {
                NRational::class -> NType.Rational
                NSymbol::class -> NType.Symbol
                LeafNode::class -> NType.Leaf
                Node1T::class -> NType.Node1
                Node2T::class -> NType.Node2
                Node3T::class -> NType.Node3
                NodeN::class -> NType.NodeN
                else -> error("Unknown type $type")
            }
        }

        fun signatureOf(node: Node): NodeSig {
            return NodeSig(node.name, typeOf(node))
        }


        val RATIONAL = NodeSig("", NType.Rational)
        val SYMBOL = NodeSig("", NType.Symbol)


        val ADD = NodeSig(Node.Names.ADD, NType.NodeN)
        val MUL = NodeSig(Node.Names.MUL, NType.NodeN)
        val POW = NodeSig(Node.Names.POW, NType.Node2)

        val F1_SIN = NodeSig(Node.Names.F1_SIN, NType.Node1)
        val F1_COS = NodeSig(Node.Names.F1_COS, NType.Node1)
        val F1_TAN = NodeSig(Node.Names.F1_TAN, NType.Node1)


    }
}

/**
 * Describes the structural signature of a node.
 */
val Node.signature get() = NodeSig.signatureOf(this)



object NodeOrder : Comparator<Node> {

    fun nodeTypeOrdinal(node: Node): Int {
        return when (node) {
            is NOther -> -1
            is NRational -> 0
            is NSymbol -> 10
            is LeafNode -> 99
            is Node1 -> 100
            is Node2 -> 110
            is Node3 -> 120
            is NodeN -> 1000
//            is NodeChilded -> 9999
        }
    }

    private fun compareRational(o1: NRational, o2: NRational): Int {
        val (n1, d1) = o1.value
        val (n2, d2) = o2.value
        n1.compareTo(n2).let {
            if (it != 0) return it
        }
        return d1.compareTo(d2)
    }


    override fun compare(o1: Node, o2: Node): Int {
        (nodeTypeOrdinal(o1) - nodeTypeOrdinal(o2)).let {
            if (it != 0) return it
        }
        o1.name.compareTo(o2.name).let {
            if (it != 0) return it
        }

        if (o1 is NRational && o2 is NRational) {
            return compareRational(o1, o2)
        }
        if (o1 is NSymbol && o2 is NSymbol) {
            return o1.ch.compareTo(o2.ch)
        }

        if (o1 is Node1 && o2 is Node1) {
            return compare(o1.child, o2.child)
        }

        if (o1 is Node2 && o2 is Node2) {
            compare(o1.first, o2.first).let {
                if (it != 0) return it
            }
            return compare(o1.second, o2.second)
        }

        if (o1 is Node3 && o2 is Node3) {
            compare(o1.first, o2.first).let {
                if (it != 0) return it
            }
            compare(o1.second, o2.second).let {
                if (it != 0) return it
            }
            return compare(o1.third, o2.third)
        }

        if (o1 is NodeN && o2 is NodeN) {
            val c1 = o1.children
            val c2 = o2.children
            c1.size.compareTo(c2.size).let {
                if (it != 0) return it
            }

            val n = c1.size
            for (i in 0 until n) {
                compare(c1[i], c2[i]).let {
                    if (it != 0) return it
                }
            }
            return 0
        }

        throw IllegalArgumentException("Cannot compare $o1 and $o2")
    }
}


interface NodeBuilderScope {

    fun symbol(name: String): Node {
        return NSymbol(name)
    }

    fun constant(name: String): Node {
        return when (name) {
            "pi", Node.Names.Symbol_PI -> Node.PI
            "e", Node.Names.Symbol_E -> Node.NATURAL_E
            "i", Node.Names.Symbol_I -> Node.IMAGINARY_UNIT
            else -> throw IllegalArgumentException("Unknown constant: $name")
        }
    }

    val x: Node get() = symbol("x")
    val y: Node get() = symbol("y")
    val z: Node get() = symbol("z")
    val a: Node get() = symbol("a")
    val b: Node get() = symbol("b")
    val c: Node get() = symbol("c")

    val imagUnit: Node get() = Node.IMAGINARY_UNIT
    val 𝑖: Node get() = Node.IMAGINARY_UNIT

    val naturalE: Node get() = Node.NATURAL_E
    val 𝑒: Node get() = Node.NATURAL_E

    val pi: Node get() = Node.PI

    val context: ExprContext
    val π: Node get() = Node.PI


    val Int.e: Node get() = Node.Int(this.toBigInteger())

    val Long.e: Node get() = Node.Int(this.toBigInteger())

    val BigInteger.e: Node get() = Node.Int(this)

    val BigFrac.e: Node get() = Node.Rational(this)

    val String.s: Node get() = symbol(this)

    fun rational(nume: Int, deno: Int): Node {
        return Node.Rational(BigFracAsQuot.bfrac(nume, deno))
    }

    operator fun Node.plus(y: Node): Node {
        return Node.Add(listOf(this, y))
    }

    operator fun Node.minus(y: Node): Node {
        return Node.Add(listOf(this, Node.Mul(listOf(Node.NEG_ONE, y))))
    }

    fun negate(x: Node): Node {
        return Node.Neg(x)
    }

    operator fun Node.unaryMinus(): Node {
        return Node.Neg(this)
    }

    fun sum(vararg nodes: Node): Node {
        return Node.Add(nodes.asList())
    }

    fun sum(elements: List<Node>): Node {
        return Node.Add(elements)
    }

    fun product(vararg nodes: Node): Node {
        return Node.Mul(nodes.asList())
    }

    fun product(elements: List<Node>): Node {
        return Node.Mul(elements)
    }


    operator fun Node.times(y: Node): Node {
        return Node.Mul(listOf(this, y))
    }

    operator fun Node.div(y: Node): Node {
        return Node.Mul(listOf(this, Node.Inv(y)))
    }

    fun pow(base: Node, exp: Node): Node {
        return Node.Node2(Node.Names.POW, base, exp)
    }

    fun sqrt(x: Node): Node {
        return pow(x, Node.HALF)
    }

    fun inv(node: Node): Node {
        return Node.Inv(node)
    }

    fun exp(x: Node): Node {
        return Node.Exp(x)
    }

    fun log(base: Node, x: Node): Node {
        return Node.Log(base, x)
    }

    fun ln(x: Node): Node {
        return Node.Ln(x)
    }

    fun sin(x: Node): Node {
        return Node.Node1(Node.Names.F1_SIN, x)
    }

    fun cos(x: Node): Node {
        return Node.Node1(Node.Names.F1_COS, x)
    }

    fun tan(x: Node): Node {
        return Node.Node1(Node.Names.F1_TAN, x)
    }


    companion object {
        private object DefaultBuilderScope : NodeBuilderScope {
            override val x: Node = symbol("x")
            override val y: Node = symbol("y")
            override val z: Node = symbol("z")
            override val a: Node = symbol("a")
            override val b: Node = symbol("b")
            override val c: Node = symbol("c")
            override val context: ExprContext
                get() = BasicExprContext()
        }

        operator fun invoke(): NodeBuilderScope {
            return DefaultBuilderScope
        }
    }
}


fun buildNode(context: NodeBuilderScope = NodeBuilderScope(), builder: NodeBuilderScope.() -> Node): Node {
    return context.builder()
}
