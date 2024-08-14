import cn.mathsymk.IMathObject

/**
 * @author liyicheng
 * 2017-12-10 20:36
 */
object TestUtils {
    //	public static <T> org.hamcrest.Matcher<T> isTrue(Predicate<T> f,String des){
    //		return new BaseMatcher<T>() {
    //			/*
    //			 * @see org.hamcrest.Matcher#matches(java.lang.Object)
    //			 */
    //			@SuppressWarnings("unchecked")
    //			@Override
    //			public boolean matches(Object item) {
    //				try {
    //				return f.test((T)item);
    //				}catch(ClassCastException es) {
    //					return false;
    //                }
    //            }
    //
    //            @Override
    //            public void describeTo(Description description) {
    //                description.appendText(des);
    //            }
    //        };
    //    }
    //
    //    public static <T> org.hamcrest.Matcher<T> isZero(RealCalculator<T> mc) {
    //        return new BaseMatcher<T>() {
    //            /*
    //             * @see org.hamcrest.Matcher#matches(java.lang.Object)
    //             */
    //            @SuppressWarnings("unchecked")
    //            @Override
    //            public boolean matches(Object item) {
    //                try {
    //                    return mc.isZero((T) item);
    //                } catch (ClassCastException es) {
    //					return false;
    //				}
    //			}
    //
    //			@Override
    //			public void describeTo(Description description) {
    //                description.appendText("zero");
    //            }
    //        };
    //    }
    //    public static <T> void assertMathEquals(T expected, T actual, RealCalculator<T> mc) {
    //        if (!mc.isEqual(expected, actual)) {
    //            throw new AssertionError("Expected <" + expected + ">, actual <" + actual + ">");
    //        }
    //    }
    fun <T> assertValueEquals(expected: IMathObject<T>, actual: IMathObject<T>) {
        if (!expected.valueEquals(actual)) {
            throw AssertionError("Expected <$expected>, actual <$actual>")
        }
    }
}
