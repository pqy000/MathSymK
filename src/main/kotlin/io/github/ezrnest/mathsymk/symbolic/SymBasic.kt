package io.github.ezrnest.mathsymk.symbolic


object SymBasic {

    val EMPTY_SET = NSymbol(Symbols.EMPTY_SET)
    val UNIVERSE = NSymbol(Symbols.UNIVERSE)

    val TRUE = NSymbol(Symbols.True)
    val FALSE = NSymbol(Symbols.False)

    object Symbols {

        val EMPTY_SET = ESymbol("∅")
        val UNIVERSE = ESymbol("Universe")
        val BELONGS = ESymbol("∈")
//        val CONTAINS = ESymbol("∋")
        val SUBSET = ESymbol("⊆")
        val INTERSECT = ESymbol("∩")
        val UNION = ESymbol("∪")

        val True = ESymbol("true")
        val False = ESymbol("false")

        val Tuple = ESymbol("")

    }

    val UNDEFINED = NOther("undefined")


    fun <T : Node> node1(symbol: ESymbol, child: T): Node1T<T> {
        return Node1T(symbol, child)
    }

    fun <T1 : Node, T2 : Node> node2(symbol: ESymbol, first: T1, second: T2): Node2T<T1, T2> {
        return Node2T(symbol, first, second)
    }

    fun <T1 : Node, T2 : Node, T3 : Node> node3(
        symbol: ESymbol, first: T1, second: T2, third: T3
    ): Node3T<T1, T2, T3> {
        return Node3T(symbol, first, second, third)
    }


    fun nodeNFlatten(symbol: ESymbol, children: List<Node>, empty: Node): Node {
        return when (children.size) {
            0 -> empty
            1 -> children[0]
            else -> NodeN(symbol, children)
        }
    }

    fun tuple(vararg nodes: Node): Node {
        return tuple(nodes.asList())
    }

    fun tuple(nodes: List<Node>): Node {
        return NodeN(Symbols.Tuple, nodes)
    }


    fun QualifiedConstrained(name: ESymbol, variable: NSymbol, condition: Node, expr: Node): Node {
        return QualifiedConstrained(name, listOf(variable), condition, expr)
    }

    fun QualifiedConstrained(name: ESymbol, variables: List<NSymbol>, condition: Node, expr: Node): Node {
        val nodeVars = tuple(variables)
        return node3(name, nodeVars, condition, expr)
    }



//    fun qualified(name: ESymbol, variable: Node, expr: Node): Node {
//        return QualifiedConstrained(name, variable, TRUE, expr)
//    }

}