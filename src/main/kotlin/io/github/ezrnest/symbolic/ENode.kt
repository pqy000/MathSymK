package io.github.ezrnest.symbolic

// created at 2024/10/01

import java.math.BigInteger

sealed interface ENode {
    val name: String

    fun treeToString(): String {
        return treeTo(StringBuilder()).toString()
    }

    fun <A : Appendable> treeTo(builder: A, level: Int = 0, indent: String = ""): A

}

interface ELeafNode : ENode

data class ESymbol(val ch: String) : ELeafNode {
    override val name get() = ch

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).appendLine(name)
        return builder
    }
}

data class EInteger(val value: BigInteger) : ELeafNode {
    override val name get() = value.toString()

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).appendLine(name)
        return builder
    }
}


interface ENodeChilded : ENode {
}

interface EFunc1 : ENodeChilded {
    val child: ENode

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).appendLine(name)
        child.treeTo(builder, level + 1, "$indent  ")
        return builder
    }
}

interface ENode2 : ENodeChilded {
    val child1: ENode
    val child2: ENode

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).appendLine(name)
        val newIndent = "$indent  "
        child1.treeTo(builder, level + 1, newIndent)
        child2.treeTo(builder, level + 1, newIndent)
        return builder
    }
}

interface ENode3 : ENodeChilded {
    val child1: ENode
    val child2: ENode
    val child3: ENode

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).appendLine(name)
        val newIndent = "$indent  "
        child1.treeTo(builder, level + 1, newIndent)
        child2.treeTo(builder, level + 1, newIndent)
        child3.treeTo(builder, level + 1, newIndent)
        return builder
    }
}

interface ENodeN : ENodeChilded {
    val children: List<ENode>

    override fun <A : Appendable> treeTo(builder: A, level: Int, indent: String): A {
        builder.append(indent).appendLine(name)
        val newIndent = "$indent  "
        children.forEach { it.treeTo(builder, level + 1, newIndent) }
        return builder
    }
}


interface EMutableNode : ENode

interface EMutableNodeChilded : ENodeChilded {
}

interface EMutableFunc1 : EFunc1, EMutableNodeChilded {
    override var child: ENode
}

interface EMutableNode2 : ENode2, EMutableNodeChilded {
    override var child1: ENode
    override var child2: ENode
}

interface EMutableNode3 : ENode3, EMutableNodeChilded {
    override var child1: ENode
    override var child2: ENode
    override var child3: ENode
}

interface EMutableNodeN : ENodeN, EMutableNodeChilded {
    override var children: List<ENode>
}


data class ENode1Impl(
    override val name: String,
    override var child: ENode
) : EMutableFunc1 {
}

data class ENode2Impl(
    override val name: String,
    override var child1: ENode, override var child2: ENode
) : EMutableNode2 {
}

data class ENode3Impl(
    override val name: String,
    override var child1: ENode, override var child2: ENode, override var child3: ENode
) : EMutableNode3 {
}

data class ENodeNImpl(
    override val name: String,
    override var children: List<ENode>
) : EMutableNodeN {
}


object NodeBuilderScope {
    val x = "x".s
    val y = "y".s
    val z = "z".s
    val a = "a".s
    val b = "b".s


    val Int.e: ENode get() = EInteger(BigInteger.valueOf(this.toLong()))

    val String.s: ENode get() = ESymbol(this)

    fun add(nodes: List<ENode>): ENode {
        return ENodeNImpl("+", nodes)
    }

    fun add(a: ENode, b: ENode): ENode {
        return add(listOf(a, b))
    }


    operator fun ENode.plus(other: ENode): ENode {
        return add(this, other)
    }

    fun mul(nodes: List<ENode>): ENode {
        return ENodeNImpl("*", nodes)
    }

    fun mul(a: ENode, b: ENode): ENode {
        return mul(listOf(a, b))
    }

    operator fun ENode.times(other: ENode): ENode {
        return mul(this, other)
    }

}

//fun main() {
//    with(NodeBuilderScope) {
//        (1.e + "x".s).treeToString().let(::println)
//    }
//}