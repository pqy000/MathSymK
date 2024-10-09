package io.github.ezrnest.symbolic

interface Matcher<T>

interface MatchResult<T> {
    val matched: T
//    val refMapping: Map<String, () -> T>
}


object MatcherBuilderScope {

//    operator fun <T,S> Matcher<T>

}