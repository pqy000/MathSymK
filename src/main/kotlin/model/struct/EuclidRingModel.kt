package cn.mathsymk.model.struct

import cn.ancono.math.exceptions.ExceptionUtil
import cn.ancono.math.numberModels.api.RingNumberModel
import cn.ancono.math.numberModels.api.times


@Suppress("NOTHING_TO_INLINE")
inline operator fun <T : EuclidRingNumberModel<T>> EuclidRingNumberModel<T>.rem(y: T) = this.remainder(y)
