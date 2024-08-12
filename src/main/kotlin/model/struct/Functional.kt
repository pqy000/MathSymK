package cn.mathsymk.model.struct

import java.util.function.Function


/**
 * Describes objects that can be composed. For example, two functions can be composed to
 * a new function. The composing may be not commutative, so there are two kinds of composing:
 * [.compose] and [.andThen], which correspond to the method
 * [Function.compose] and [Function.andThen].
 *
 *
 * A composable type is naturally associative to the operation of composing. Therefore, for any composable
 * object, a semigroup can be defined.
 *
 * @author liyicheng
 * 2018-03-02 21:00
 * @see Function
 */
interface Composable<S : Composable<S>> {
    /**
     * Compose `this` and `before` as<br></br>
     * `this•before`
     *
     * @param before the object composed in right
     * @see Function.compose
     * @see .andThen
     */
    fun compose(before: S): S

    /**
     * Compose `after` and `this` as<br></br>
     * `after•this`
     *
     * @param after the object composed in left
     * @see Function.andThen
     * @see .compose
     */
    fun andThen(after: S): S
}

/**
 * 2018-03-02
 */

/**
 * Invertible describes functions, matrices and other objects that can be inverted.
 * It is required that `inv.inverse().inverse().equals(inv)`.
 *
 *
 *
 *
 * @author liyicheng
 * 2018-03-02 21:13
 */
interface Invertible<out S : Invertible<S>> {
    /**
     * Returns the inverse of `this`.
     *
     * @return `this<sup>-1</sup>`
     */
    fun inverse(): S
}