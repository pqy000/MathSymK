package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.RuleSet


val RulesSummationReduce = RuleSet {


    alg {
        rule {
            target = sum(1.e, infinity) { x -> pow(x, -2) }
            result = pow(pi, 2) / 6
        }
    }


}