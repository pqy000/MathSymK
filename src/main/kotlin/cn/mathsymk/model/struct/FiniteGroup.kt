package cn.mathsymk.model.struct

import cn.mathsymk.structure.Group

interface FiniteGroup<T:Any> : Group<T>,Collection<T> {
    override val size : Int

}