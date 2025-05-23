package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.symbolic.alg.SymSets


/**
 * `NodeScope` defines a set of operations for building symbolic expressions.
 */
interface NodeScope {

    val context: EContext

    val namedSymbols: MutableMap<String, ESymbol>

    fun symbolNode(name: String): Node {
        val sym = getSymbol(name)
        return NSymbol(sym)
    }

    fun getSymbol(name: String): ESymbol{
        return namedSymbols.getOrPut(name) { ESymbol(name) }
    }



    fun constant(name: String): Node? {
        return context.constant(name)
    }

    val String.s: Node get() = symbolNode(this)


    companion object {

        internal open class NodeScopeImpl(
            override val context: EContext,
            override val namedSymbols: MutableMap<String, ESymbol> = mutableMapOf()
        ) : NodeScope {
            override fun symbolNode(name: String): Node {
                return context.symbol(name)
            }
        }

        operator fun invoke(
            context: EContext = EmptyEContext, namedSymbols: MutableMap<String, ESymbol> = mutableMapOf()
        ): NodeScope {
            return NodeScopeImpl(context, namedSymbols)
        }


        inline fun NodeScope.qualifiedConditioned(
            nodeName: ESymbol, varName_: String? = null, condition: (NSymbol) -> Node, clause: (NSymbol) -> Node
        ): Node {
            val varName = varName_ ?: getUnusedName("x")
            val varSymbol = ESymbol(varName)
            val varNode = NSymbol(varSymbol)
            val conditionNode = condition(varNode)
            val clauseNode = clause(varNode)
            return SymBasic.QualifiedConstrained(nodeName, varNode, conditionNode, clauseNode)
        }

        inline fun NodeScope.qualified(nodeName: ESymbol, varName: String?, clause: (NSymbol) -> Node): Node {
            return qualifiedConditioned(nodeName, varName, condition = { SymBasic.True }, clause)
        }

        inline fun NodeScope.qualifiedContained(
            nodeName: ESymbol, varName: String?, set: Node, clause: (NSymbol) -> Node
        ): Node {
            return qualifiedConditioned(nodeName, varName, condition = { x -> SymSets.belongs(x, set) }, clause)
        }

        fun NodeScope.qualifiedContainedRep(nodeName: ESymbol, varNode: Node, set: Node, clause: Node): Node {
            require(varNode is NSymbol)
            return qualifiedContained(nodeName, varNode.symbol.name, set) { x -> clause.replaceSymbol(varNode, x) }
        }

        fun NodeScope.qualifiedRep(nodeName: ESymbol, varNode: Node, clause: Node, replaceVar: Boolean = true): Node {
            return qualifiedConditionedRep(nodeName, varNode, condition = null, clause, replaceVar)
        }

        fun NodeScope.qualifiedConditionedRep(
            nodeName: ESymbol, varNode: Node, condition: Node?, clause: Node, replaceVar: Boolean = true
        ): Node {
            require(varNode is NSymbol)
            return qualifiedConditioned(
                nodeName, varNode.symbol.name,
                condition = { x ->
                    if (condition == null) return@qualifiedConditioned SymBasic.True
                    if (replaceVar) condition.replaceSymbol(varNode, x) else condition
                },
                clause = { x ->
                    if (replaceVar) clause.replaceSymbol(varNode, x) else clause
                }
            )
//            if (!replaceVar) {
//                return SymBasic.QualifiedConstrained(nodeName, varNode, condition ?: SymBasic.TRUE, clause)
//            }
//            val varName = varNode.symbol.name
//            val varSymbol = ESymbol(varName) // reusing the name, but creating a new symbol
//            val newVarNode = NSymbol(varSymbol)
//            val clauseNode = clause.replace(varNode, newVarNode)
//            val conditionNode = condition?.replace(varNode, newVarNode) ?: SymBasic.TRUE
//            return SymBasic.QualifiedConstrained(nodeName, newVarNode, conditionNode, clauseNode)
        }


        fun NodeScope.getUnusedName(prefix: String = "#"): String {
            var i = 0
            while (true) {
                val name = "$prefix$i"
                if (namedSymbols.containsKey(name)) {
                    i++
                } else {
                    return name
                }
            }
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
    override val x: Node get() = symbolNode("x")
    override val y: Node get() = symbolNode("y")
    override val z: Node get() = symbolNode("z")
    override val w: Node get() = symbolNode("w")
    override val a: Node get() = symbolNode("a")
    override val b: Node get() = symbolNode("b")
    override val c: Node get() = symbolNode("c")
}


inline fun buildNode(context: EContext = EmptyEContext, builder: NodeScope.() -> Node): Node {
    return NodeScope(context).builder()
}
