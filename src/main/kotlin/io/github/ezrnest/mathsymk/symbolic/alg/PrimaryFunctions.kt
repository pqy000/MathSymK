package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.RuleSet

val RulesPrimaryFunctions = RuleSet {

    alg {
        rule {
            target = abs(x)
            result = x
            condition = x geq 0.e
        }
    }

}