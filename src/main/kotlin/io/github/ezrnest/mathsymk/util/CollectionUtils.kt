package io.github.ezrnest.mathsymk.util


inline fun <T1,T2> Iterable<T1>.all2(other : Iterable<T2>, predicate : (T1,T2) -> Boolean) : Boolean {
    val it1 = this.iterator()
    val it2 = other.iterator()
    while(it1.hasNext() && it2.hasNext()){
        if(!predicate(it1.next(),it2.next())) return false
    }
    return !it1.hasNext() && !it2.hasNext()
}