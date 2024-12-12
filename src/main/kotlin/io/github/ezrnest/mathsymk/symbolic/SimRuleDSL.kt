package io.github.ezrnest.mathsymk.symbolic


interface RuleSetBuildingScope : NodeScopeMatcher {

    fun rule(name : String? = null,f: SimRuleBuilderScope.() -> Unit)

    fun rule(name: String?, target: Node, result: Node, condition: Node? = null)


    fun rule(target: Node, result: Node, condition: Node? = null){
        rule(null,target,result,condition)
    }

//    fun rule(entry : Pair<Node,Node>){
//        rule(null,entry.first,entry.second,null)
//    }

    fun addRules(vararg rule: SimRule)

    fun addRules(vararg rule: SimRuleProvider)
}


interface SimRuleBuilderScope {

    var name: String?

    var target: Node?

    var result: Node?

    var condition: Node?

    fun target(f: () -> Node) {
        target = f()
    }

    fun result(f: () -> Node) {
        result = f()
    }

    fun condition(f: () -> Node) {
        condition = f()
    }

//    fun where(buildCondition: Matching.() -> Boolean)
}


fun RuleSet(f: RuleSetBuildingScope.() -> Unit): SimRuleProvider {
    return RuleSetProvider(f)
}

class RuleSetProvider(val buildingAction: RuleSetBuildingScope.() -> Unit) : SimRuleProvider {

    internal class RuleBuilderImpl : SimRuleBuilderScope {
        override var name: String? = null
        override var target: Node? = null
            set(value) {
                field = requireNotNull(value)
            }
        override var result: Node? = null
            set(value) {
                field = requireNotNull(value)
            }
        override var condition: Node? = null
            set(value) {
                field = requireNotNull(value)
            }
    }

    internal class RuleSetBuildingScopeImpl(val cal: ExprCal) :
        NodeScopeMatcher.Companion.NodeScopeMatcherImpl(cal.context),
        RuleSetBuildingScope {
        val rules = mutableListOf<SimRule>()

        override fun addRules(vararg rule: SimRuleProvider) {
            rule.forEach {
                rules.addAll(it.init(cal))
            }
        }

        override fun addRules(vararg rule: SimRule) {
            rules.addAll(rule)
        }

        override fun rule(name: String?, f: SimRuleBuilderScope.() -> Unit) {
            val builder = RuleBuilderImpl()
            builder.name = name
            builder.f()
            rule(builder.name, builder.target!!, builder.result!!, builder.condition)
        }

        override fun rule(name: String?, target: Node, result: Node, condition: Node?) {
            buildRule(name, target, result, condition)
        }

        private fun checkNodeReference(target: Node,matcher : NodeMatcher, result : Node){
            val referredSymbols = matcher.refSymbols
            val replacingSymbols = result.allSymbols()
            for (symbol in replacingSymbols) {
                if(symbol !in declaredRefs) continue // other constants not in the scope
                require(referredSymbols.contains(symbol)) {
                    "Reference to symbol $symbol in [$result] is not found in the target node $target."
                }
            }
        }

        @Suppress("NAME_SHADOWING")
        private fun buildRule(name: String?, target: Node, result: Node, condition: Node?){
            // first reduce it
            val target = cal.reduce(target)
            val result = cal.reduce(result)
            val name = name ?: "Rule: ${cal.format(target)} -> ${cal.format(result)}"

            if(cal.directEquals(target,result)) return // no need to replace

            val condition = condition?.let { cal.reduce(it) }

            var matcher = NodeScopeMatcher.buildMatcher(this, target, cal)
            if(condition != null) matcher = MatcherWithPostConditionNode(matcher,condition)
            checkNodeReference(target,matcher, result)
            val rule = MatcherNodeReplaceRule(name, matcher, result, Int.MAX_VALUE)
            rules.add(rule)
        }


    }

    override fun init(cal: ExprCal): List<SimRule> {
        val scope = RuleSetBuildingScopeImpl(cal)
        scope.buildingAction()
        return scope.rules
    }
}


/*
DSL draft:

val RulesExponentialReduce = RuleSet {

   alg{
      rule {
         name = "b^log_b(x) = x"
         target = pow(b, log(b, x))
         result = x

         target {
            pow(b, log(b, x))
         }
         result {
            x
         }
         where {
            // TODO
         }
      }

      rule("b^log_b(x) = x",
            pow(b, log(b, x)),
            x,
            where = null,
      )

      addRule(ComputePow)

      rule {
         name = "log_b(b^x) = x"
         match {
            log(b.where(b gtr 0.e), pow(b, x))
         } to {
            x
         }
         where {
            TODO()
         }
      }
   }



}





 */