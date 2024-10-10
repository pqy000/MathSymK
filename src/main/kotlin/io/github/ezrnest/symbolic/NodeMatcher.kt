package io.github.ezrnest.symbolic

//created at 2024/10/10

interface StructuredRule<T : Node> : SimRule {

    fun simplifyTyped(node: T, context: ExprContext): Node?
}

interface MatchContext : ExprContext{

    val refMap : Map<String, Node>
}

interface MutableMatchContext : MatchContext {
    override val refMap: MutableMap<String, Node>
}

interface NodeMatcher<T> {

    fun matches(node: Node, matchContext: MatchContext): MatchResult<T>?
}

interface MatchResult<T> {
    val matched: T
    val refMapping: Map<String, T>
}


object MatcherBuilderScope {

    fun <T : Node, S : Node> pow(base: NodeMatcher<T>, exp: NodeMatcher<S>): NodeMatcher<Node2T<T, S>> = TODO()

    fun <T : Node> sin(x: NodeMatcher<T>): NodeMatcher<Node1T<T>> = TODO()


    val x: NodeMatcher<NSymbol> get() = TODO()
    val y: NodeMatcher<NSymbol> get() = TODO()

    val integer: NodeMatcher<NRational> get() = TODO()

    val rational: NodeMatcher<NRational> get() = TODO()
}

fun main() {
    with(MatcherBuilderScope) {
        val m1 = pow(pow(x, y), integer)
        val node = Node.ONE
        val res = m1.matches(node, TODO())
        if (res != null) {
            val n = res.matched
            n.second
        }
    }
}