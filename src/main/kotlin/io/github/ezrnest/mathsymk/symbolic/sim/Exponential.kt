package io.github.ezrnest.mathsymk.symbolic.sim
// created at 2024/10/19

class RulesExponentialReduce : RuleList() {

    init {
        rule {
            name = "b^log_b(x) = x"
            match {
                pow(b, log(b, x))
            } to {
                x
            }
        }

        rule{
            name = "log_b(b^x) = x"
            match{
                log(b, pow(b, x))
            } to {
                x
            }
            condition {
                TODO()
            }
        }

    }
}