package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.RuleList

// created at 2024/10/19

class RulesExponentialReduce : RuleList() {

    init {
        rule {
            name = "b^log_b(x) = x"
            match {
                alg {
                    pow(b, log(b, x))
                }
            } to {
                x
            }
        }

        rule {
            name = "log_b(b^x) = x"
            match {
                alg {
                    log(b, pow(b, x))
                }
            } to {
                x
            }
            condition {
                TODO()
            }
        }

    }
}