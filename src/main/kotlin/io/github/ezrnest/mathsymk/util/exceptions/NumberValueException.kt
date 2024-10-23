/**
 *
 */
package io.github.ezrnest.mathsymk.util.exceptions

/**
 * An exception to indicate the value exceeds the capacity of a number model.
 * @author liyicheng
 */
class NumberValueException : RuntimeException {
    /**
     * Gets the expression that cause this exception, `null` value
     * is possible.
     * @return the expression
     */
    var expression: String? = null
        private set

    /**
     *
     */
    constructor()

    /**
     * @param message
     */
    constructor(message: String?) : super(message)


    /**
     * @param message
     * @param expr
     */
    constructor(message: String?, expr: String?) : super(message) {
        this.expression = expr
    }

    companion object {
        /**
         *
         */
        private const val serialVersionUID = 8724288820557054251L
    }
}
