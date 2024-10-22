package io.github.ezrnest.symbolic.sim

import io.github.ezrnest.symbolic.*


interface RuleBuilder {

    var name: String

    fun matcher(buildMatcher: MatcherBuilderScope.() -> NodeMatcherT<Node>)

    fun match(buildMatch: NodeBuilderScope.() -> Node)

    fun to(buildReplacement: ReplacementScope.() -> Node)

    infix fun Unit.to(buildReplacement: ReplacementScope.() -> Node)


    fun condition(buildCondition: MatchContext.() -> Boolean) {
        //TODO
    }
}

internal class RuleBuilderImpl : RuleBuilder {

    override var name: String = "None"

    var afterRuleDepth: Int = Int.MAX_VALUE

    private var matcher: NodeMatcherT<Node>? = null
    private var matchNodeBuilder: (NodeBuilderScope.() -> Node)? = null

    private var replacement: RepBuilder? = null

    private var remMatcher: MatcherRef? = null

    override fun match(buildMatch: NodeBuilderScope.() -> Node) {
        matchNodeBuilder = buildMatch
    }


    override fun matcher(buildMatcher: MatcherBuilderScope.() -> NodeMatcherT<Node>): Unit {
        val mat = buildMatcher(MatcherBuilderScope)
        matcher = mat
    }

    private fun setRep(builder: RepBuilder) {
        replacement = builder
    }

    override fun to(buildReplacement: ReplacementScope.() -> Node) {
        setRep(buildReplacement)
    }

    override fun Unit.to(buildReplacement: ReplacementScope.() -> Node) {
        setRep(buildReplacement)
    }

    fun build(): SimRule {
        val matcher = this.matcher
        val matchNodeBuilder = this.matchNodeBuilder
        val replacement = this.replacement
        require(matcher != null || matchNodeBuilder != null) { "Matcher or match node must be set" }
        require(replacement != null) { "Replacement must be set" }
        return if (matcher != null) {
            NodeBuilderForMatch.warpPartialMatcherReplace(matcher, replacement, name, afterRuleDepth)
        } else {
            MatchNodeReplaceRule(matchNodeBuilder!!, replacement, name, afterRuleDepth)
        }
    }

}


open class RuleList {

    val list: MutableList<SimRule> = mutableListOf()

    fun rule(f: RuleBuilder.() -> Unit) {
        val builder = RuleBuilderImpl()
        builder.f()
        list.add(builder.build())
    }
}

fun rule(f: RuleBuilder.() -> Unit): SimRule {
    val builder = RuleBuilderImpl()
    builder.f()
    return builder.build()
}

fun BasicExprCal.addAll(rules : RuleList) {
    rules.list.forEach { addReduceRule(it) }
}