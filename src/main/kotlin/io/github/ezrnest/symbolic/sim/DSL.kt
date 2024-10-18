package io.github.ezrnest.symbolic.sim

import io.github.ezrnest.symbolic.MatcherBuilderScope
import io.github.ezrnest.symbolic.MatcherReplaceRule
import io.github.ezrnest.symbolic.Node
import io.github.ezrnest.symbolic.NodeMatcher
import io.github.ezrnest.symbolic.NodeMatcherNPO
import io.github.ezrnest.symbolic.NodeMatcherT
import io.github.ezrnest.symbolic.NothingMatcher
import io.github.ezrnest.symbolic.RefMatcher
import io.github.ezrnest.symbolic.RepBuilder
import io.github.ezrnest.symbolic.ReplacementBuilderScope
import io.github.ezrnest.symbolic.SimRule


//data class MatcherBuilt<T : Node>(val matcher: NodeMatcher<T>) {
//
//    infix fun to(resultBuilder: RepBuilder): MatcherReplaceRule {
//        return MatcherReplaceRule(matcher, resultBuilder, "")
//    }
//}
//
//fun <T : Node> match(buildMatcher: MatcherBuilderScope.() -> NodeMatcher<T>): MatcherBuilt<T> {
//    return MatcherBuilt(buildMatcher(MatcherBuilderScope))
//}

object MatchInfix


interface RuleBuilder {

    var name: String

    fun match(buildMatcher: MatcherBuilderScope.() -> NodeMatcherT<Node>): MatchInfix

    fun to(buildReplacement: ReplacementBuilderScope.() -> Node)

    infix fun MatchInfix.to(buildReplacement: ReplacementBuilderScope.() -> Node)
}

internal class RuleBuilderImpl : RuleBuilder {

    override var name: String = "None"

    private var matcher: NodeMatcherT<Node>? = null

    private var replacement: RepBuilder? = null

    private var remMatcher: RefMatcher? = null

    override fun match(buildMatcher: MatcherBuilderScope.() -> NodeMatcherT<Node>): MatchInfix {
        val mat = buildMatcher(MatcherBuilderScope)
        if (mat is NodeMatcherNPO) {
            if (mat.remMatcher is NothingMatcher) {
                val rem = RefMatcher("_rem${mat.nodeSig.name}")
                remMatcher = rem
                mat.remMatcher = rem
            }
        }
        matcher = mat
        return MatchInfix
    }

    private fun setRep(builder: RepBuilder) {
        if (remMatcher == null) {
            replacement = builder
            return
        }
        val remName = (remMatcher as RefMatcher).name
        val sig = (matcher as NodeMatcherNPO).nodeSig
        replacement = {
            val sub = builder()
            if (!hasRef(remName)) {
                sub
            } else {
                Node.NodeN(sig.name, listOf(sub, ref(remName)))
            }
        }
    }

    override fun to(buildReplacement: ReplacementBuilderScope.() -> Node) {
        setRep(buildReplacement)
    }

    override fun MatchInfix.to(buildReplacement: ReplacementBuilderScope.() -> Node) {
        setRep(buildReplacement)
    }

    fun build(): SimRule {
        require(matcher != null && replacement != null) { "Matcher and replacement must be set" }
        return MatcherReplaceRule(matcher!!, replacement!!, name)
    }

}


fun rule(f: RuleBuilder.() -> Unit): SimRule {
    val builder = RuleBuilderImpl()
    builder.f()
    return builder.build()
}


fun main() {


}