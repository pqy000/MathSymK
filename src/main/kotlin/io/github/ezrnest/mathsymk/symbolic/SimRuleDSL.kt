package io.github.ezrnest.mathsymk.symbolic


interface RuleBuilder {

    var name: String

    fun matcher(buildMatcher: MatcherBuilderScope.() -> NodeMatcherT<Node>)

    fun match(buildMatch: NodeScopeMatcher.() -> Node)

    fun to(buildReplacement: NodeScopeMatched.() -> Node)

    infix fun Unit.to(buildReplacement: NodeScopeMatched.() -> Node)


    fun where(buildCondition: MatchResult.() -> Boolean) {
        //TODO
    }
}

internal class RuleBuilderImpl : RuleBuilder {

    override var name: String = "Unnamed"

    var afterRuleDepth: Int = Int.MAX_VALUE

    private var matcher: NodeMatcherT<Node>? = null
    private var matchNodeBuilder: (NodeScopeMatcher.() -> Node)? = null

    private var replacement: RepBuilder? = null

    private var remMatcher: MatcherRef? = null

    override fun match(buildMatch: NodeScopeMatcher.() -> Node) {
        matchNodeBuilder = buildMatch
    }


    override fun matcher(buildMatcher: MatcherBuilderScope.() -> NodeMatcherT<Node>): Unit {
        val mat = buildMatcher(MatcherScopeAlg)
        matcher = mat
    }

    private fun setRep(builder: RepBuilder) {
        replacement = builder
    }

    override fun to(buildReplacement: NodeScopeMatched.() -> Node) {
        setRep(buildReplacement)
    }

    override fun Unit.to(buildReplacement: NodeScopeMatched.() -> Node) {
        setRep(buildReplacement)
    }

    fun build(): BuilderSimRule {
        val matcher = this.matcher
        val matchNodeBuilder = this.matchNodeBuilder
        val replacement = this.replacement
        require(matcher != null || matchNodeBuilder != null) { "Matcher or match node must be set" }
        require(replacement != null) { "Replacement must be set" }
        return if (matcher != null) { BuilderSimRule { NodeScopeMatcher.warpPartialMatcherReplace(matcher, replacement, name, afterRuleDepth) }
        } else {
            BuilderMatchNodeReplaceRule(matchNodeBuilder!!, replacement, name, afterRuleDepth)
        }
    }

}


open class RuleList {

    val list: MutableList<BuilderSimRule> = mutableListOf()

    fun rule(f: RuleBuilder.() -> Unit) {
        val builder = RuleBuilderImpl()
        builder.f()
        list.add(builder.build())
    }
}

fun rule(f: RuleBuilder.() -> Unit): BuilderSimRule {
    val builder = RuleBuilderImpl()
    builder.f()
    return builder.build()
}

fun BasicExprCal.addAllRules(rules: RuleList) {
    rules.list.forEach { registerRule(it) }
}