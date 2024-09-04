package cn.mathsymk.structure

/*
 * Created by liyicheng at 2021-05-06 19:32
 */


interface OrderPredicate<T> : EqualPredicate<T>, Comparator<T> {

    operator fun T.compareTo(y: T): Int = compare(this, y)
}


/**
 * Describes an Abelian group with an order.
 *
 * The order is consistent with addition, that is:
 * * `x < y`    implies    `x + c < y + c`,  for any `c`.
 *
 *
 *
 */
interface OrderedAddGroupCal<T> : AddGroup<T>, OrderPredicate<T> {

    /**
     * Compares two elements, returning a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    override fun compare(o1: T, o2: T): Int

    /**
     * Returns the absolute value `|x|` of [x].
     * If `x >= 0` then `x` is returned, otherwise `-x` is returned.
     *
     * The triangle inequality is satisfied:
     *
     *     |a + b| <= |a| + |b|
     *
     */
    fun abs(x: T): T {
        if (compare(x, zero) < 0) {
            return -x
        }
        return x
    }

    /**
     * Determines whether the number is positive. This method is equivalent to `compare(x, zero) > 0`.
     *
     * @param x a number
     * @return `x > 0`
     */
    fun isPositive(x: T): Boolean {
        return compare(x, zero) > 0
    }

    /**
     * Determines whether the number is negative. This method is equivalent to `compare(x, zero) < 0`.
     *
     * @param x a number
     * @return `x < 0`
     */
    fun isNegative(x: T): Boolean {
        return compare(x, zero) < 0
    }
}

/**
 * Describes a ring with a order relation denoted by `<, <=, >, >=`, which satisfies:
 *
 *     x < y    implies    x + a < y + a, for any a
 *     x > 0, y > 0    implies    x * y > 0
 *
 * The following properties hold:
 *
 *     x > y  and  c > 0    implies    c*x > c*y
 *     |a*b| = |a|*|b|
 *
 *
 *
 */
interface OrderedRing<T> : Ring<T>, OrderedAddGroupCal<T>

/**
 * Describes a field with a order relation denoted by `<`, which satisfies:
 *
 *     x > 0, y > 0   implies    x + y > 0,   x * y > 0
 *     x > 0    implies    -x < 0
 *
 */
interface OrderedField<T> : Field<T>, OrderedRing<T>

