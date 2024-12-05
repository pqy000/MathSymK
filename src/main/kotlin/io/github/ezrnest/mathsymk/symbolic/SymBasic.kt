package io.github.ezrnest.mathsymk.symbolic


object SymBasic {

    val EMPTY_SET = NSymbol(Symbols.EMPTY_SET)
    val UNIVERSE = NSymbol(Symbols.UNIVERSE)

        val TRUE = NSymbol(Symbols.TRUE)
    val FALSE = NSymbol(Symbols.FALSE)

    object Symbols{

        val EMPTY_SET = ESymbol("∅")
        val UNIVERSE = ESymbol("Universe")
        val BELONGS = ESymbol("∈")
        val CONTAINS = ESymbol("∋")
        val SUBSET = ESymbol("⊆")
        val INTERSECT = ESymbol("∩")
        val UNION = ESymbol("∪")

        val TRUE = ESymbol("true")
        val FALSE = ESymbol("false")

        val LIST = ESymbol("list")

    }

    val UNDEFINED = NOther("undefined")


    fun <T : Node> Node1(symbol: ESymbol, child: T): Node1T<T> {
        return Node1Impl(child, symbol)
    }

    fun <T1 : Node, T2 : Node> Node2(symbol: ESymbol, first: T1, second: T2): Node2T<T1, T2> {
        return Node2Impl(first, second, symbol)
    }

    fun <T1 : Node, T2 : Node, T3 : Node> Node3(
        symbol: ESymbol, first: T1, second: T2, third: T3
    ): Node3T<T1, T2, T3> {
        return Node3T(first, second, third, symbol)
    }

    fun NodeN(symbol: ESymbol, children: List<Node>): Node {
        require(children.isNotEmpty())
        return NodeNImpl(symbol, children)
    }

    fun NodeNFlatten(symbol: ESymbol, children: List<Node>, empty: Node): Node {
        return when (children.size) {
            0 -> empty
            1 -> children[0]
            else -> NodeNImpl(symbol, children)
        }
    }

    fun List(vararg nodes: Node): Node {
        return List(nodes.asList())
    }

    fun List(nodes: List<Node>): Node {
        return NodeN(Symbols.LIST, nodes)
    }


    fun Qualified3(qualifierSymbol : ESymbol,variable: NSymbol, condition: Node, expr: Node): Node {
        return Qualified3(qualifierSymbol,listOf(variable), condition, expr)
    }

    fun Qualified3(qualifierSymbol : ESymbol,variables: List<NSymbol>, condition: Node, expr: Node): Node {
        val nodeVars = List(variables)
        return Node3(qualifierSymbol, nodeVars, condition, expr)
    }
}