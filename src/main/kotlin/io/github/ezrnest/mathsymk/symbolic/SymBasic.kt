package io.github.ezrnest.mathsymk.symbolic



interface SymBasic {

    object Symbols {

        val EMPTY_SET = ESymbol("∅")
        val UNIVERSE = ESymbol("Universe")
        val Belongs = ESymbol("∈")
        //        val CONTAINS = ESymbol("∋")
        val SUBSET = ESymbol("⊆")
        val INTERSECT = ESymbol("∩")
        val UNION = ESymbol("∪")

        val TRUE = ESymbol("true")
        val FALSE = ESymbol("false")


        val Tuple = ESymbol("")

        val Eval = ESymbol("Eval")

    }

    companion object Instance : SymBasic{
        val True = NSymbol(Symbols.TRUE)
        val False = NSymbol(Symbols.FALSE)

        val EmptySet = NSymbol(Symbols.EMPTY_SET)
        val Universe = NSymbol(Symbols.UNIVERSE)

        val Undefined = NOther("undefined")
    }


//    val True: Node
//        get() = Contants.TRUE


    infix fun Node.belongTo(set: Node): Node {
        return Node2T(Symbols.Belongs, this, set)
    }

    fun tuple(vararg nodes: Node): Node {
        return tuple(nodes.asList())
    }

    fun tuple(nodes: List<Node>): Node {
        return NodeN(Symbols.Tuple, nodes)
    }



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




    fun QualifiedConstrained(name: ESymbol, variable: NSymbol, condition: Node, expr: Node): Node {
        return QualifiedConstrained(name, listOf(variable), condition, expr)
    }

    fun QualifiedConstrained(name: ESymbol, variables: List<NSymbol>, condition: Node, expr: Node): Node {
        val nodeVars = tuple(variables)
        return node3(name, nodeVars, condition, expr)
    }

    fun eval(f : Node, vararg parameters : Node) : Node {
        return eval(f, parameters.asList())
    }

    fun eval(f : Node, parameters : List<Node>) : Node {
        return node2(Symbols.Eval, f, tuple(parameters))
    }



    fun qualified(name: ESymbol, variable: NSymbol, expr: Node): Node {
        return QualifiedConstrained(name, variable, True, expr)
    }





}