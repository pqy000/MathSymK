package cn.mathsymk.structure

interface OrderPredicate<T> : EqualPredicate<T>, Comparator<T> {

    operator fun T.compareTo(y: T): Int = compare(this, y)
}

