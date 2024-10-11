package io.github.ezrnest.symbolic

//created at 2024/10/10

interface StructuredRule<T : Node> : SimRule {

    fun simplifyTyped(node: T, context: ExprContext): Node?
}

interface MatchContext{
    val exprContext: ExprContext

    val refMap : Map<String, Node>
}

interface MutableMatchContext : MatchContext {
    override val refMap: MutableMap<String, Node>

    fun copy(): MutableMatchContext
}

sealed interface NodeMatcher<T : Node> {

    /**
     * Tries to match the given node with the pattern represented by this matcher, modifying the match context if the match is successful.
     */
    fun matches(node: Node, matchContext: MutableMatchContext): T?

    val refNames : Set<String>
}

interface TransparentNodeMatcher<T : Node> : NodeMatcher<T> {
    val child: NodeMatcher<T>

}

interface LeafMatcher<T: Node> : NodeMatcher<T>{

}

interface NodeMatcher1<C : Node> : NodeMatcher<Node1T<C>> {
    val child: NodeMatcher<C>
}

interface NodeMatcher2<C1 : Node, C2 : Node> : NodeMatcher<Node2T<C1, C2>> {
    val child1: NodeMatcher<C1>
    val child2: NodeMatcher<C2>
}

interface NodeMatcher3<C1 : Node, C2 : Node, C3 : Node> : NodeMatcher<Node3T<C1, C2, C3>> {
    val child1: NodeMatcher<C1>
    val child2: NodeMatcher<C2>
    val child3: NodeMatcher<C3>
}

interface MatcherN : NodeMatcher<NodeN> {

}

class AnyMatcher : LeafMatcher<Node>{
    override fun matches(node: Node, matchContext: MutableMatchContext): Node? {
        return node
    }

    override val refNames: Set<String> get() = emptySet()
}

class RefMatcher(val name: String) : LeafMatcher<Node> {
    override fun matches(node: Node, matchContext: MutableMatchContext): Node? {
        val ref = matchContext.refMap[name]
        if(ref == null){
            matchContext.refMap[name] = node
            return node
        }
        return if(ref == node) node else null
    }

    override val refNames: Set<String> get() = setOf(name)
}

class NamedMatcher

//interface MatchResult<T> {
//    val matched: T
//    val refMapping: Map<String, T>
//}


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
            res.second
        }
    }
}