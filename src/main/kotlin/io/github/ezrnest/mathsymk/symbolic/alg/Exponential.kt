package io.github.ezrnest.mathsymk.symbolic.alg

import io.github.ezrnest.mathsymk.symbolic.RuleSet

// created at 2024/10/19


val RulesExponentialReduce = RuleSet {
    alg {

        rule(
            "x^a * x^b = x^(a+b)",
            target = pow(x, a) * pow(x, b),
            result = pow(x, a + b)
        )


        rule {
            name = "b^log_b(x) = x"
            target = pow(b, log(b, x))
            result = x
        }

        rule(
            "log_b(b^x) = x",
            target = log(b.where(b gtr 0.e), pow(b, x)),
            result = x
        )

    }
}
