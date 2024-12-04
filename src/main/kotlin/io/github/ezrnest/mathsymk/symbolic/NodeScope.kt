package io.github.ezrnest.mathsymk.symbolic



/**
 * `NodeScope` defines a set of operations for building symbolic expressions.
 */
interface NodeScope {

    val context: EContext

    val namedSymbols: MutableMap<String, ESymbol>

    fun symbol(name: String): Node {
        val sym = namedSymbols.getOrPut(name) { ESymbol(name) }
        return NSymbol(sym)
    }

    fun constant(name: String): Node? {
        return context.constant(name)
    }

    val String.s: Node get() = symbol(this)


    companion object {

        internal class NodeScopeImpl(
            override val context: EContext,
            override val namedSymbols: MutableMap<String, ESymbol> = mutableMapOf()
        ) : NodeScope {
            override fun symbol(name: String): Node {
                return context.symbol(name)
            }
        }

        operator fun invoke(context: EContext = EmptyEContext, namedSymbols: MutableMap<String, ESymbol> = mutableMapOf()): NodeScope {
            return NodeScopeImpl(context, namedSymbols)
        }


    }
}

abstract class AbstractNodeScope(final override val context: EContext) : NodeScope {
    final override val namedSymbols: MutableMap<String, ESymbol> = mutableMapOf()
}

interface NodeScopeWithPredefined : NodeScope {
    val x: Node
    val y: Node
    val z: Node
    val w: Node

    val a: Node
    val b: Node
    val c: Node
}

interface NodeScopePredefinedSymbols : NodeScopeWithPredefined {
    override val x: Node get() = symbol("x")
    override val y: Node get() = symbol("y")
    override val z: Node get() = symbol("z")
    override val w: Node get() = symbol("w")
    override val a: Node get() = symbol("a")
    override val b: Node get() = symbol("b")
    override val c: Node get() = symbol("c")
}


inline fun buildNode(context: EContext = EmptyEContext, builder: NodeScope.() -> Node): Node {
    return NodeScope(context).builder()
}
