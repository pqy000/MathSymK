package io.github.ezrnest.symbolic.sim

import io.github.ezrnest.symbolic.MatcherBuilderScope
import io.github.ezrnest.symbolic.Node
import io.github.ezrnest.symbolic.NodeBuilderScope
import io.github.ezrnest.symbolic.NodeMatcher
import io.github.ezrnest.symbolic.SimRuleMatched
import io.github.ezrnest.symbolic.named


data class MatcherBuilt<T : Node>(val matcher: NodeMatcher<T>) {

    infix fun to(resultBuilder: NodeBuilderScope.() -> Node): SimRuleMatched<T> {
        TODO()
    }
}

fun <T : Node> match(buildMatcher: MatcherBuilderScope.() -> NodeMatcher<T>): MatcherBuilt<T> {
    return MatcherBuilt(buildMatcher(MatcherBuilderScope))
}


fun main() {
    match {
        pow(pow(x, y), integer.named(z))
    } to {
        pow(x, y * z)
    }
}