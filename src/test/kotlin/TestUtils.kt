import cn.mathsymk.IMathObject
import cn.mathsymk.structure.EqualPredicate
import kotlin.test.asserter

/**
 * @author liyicheng
 * 2017-12-10 20:36
 */
object TestUtils {
    fun <T> assertValueEquals(expected: IMathObject<T>, actual: IMathObject<T>) {
        asserter.assertTrue({
            "Expected <$expected>, actual <$actual>"
        }, expected.valueEquals(actual))
    }

    /**
     * Asserts that the two objects are equal in terms of [this].
     */
    fun <T> EqualPredicate<T>.assertEquals(expected: T, actual: T) {
        asserter.assertTrue({
            "Expected <$expected>, actual <$actual>"
        }, this.isEqual(expected, actual))
    }
}
