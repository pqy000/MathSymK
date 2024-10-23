package io.github.ezrnest.mathsymk.model.struct

import io.github.ezrnest.mathsymk.structure.Group

interface FiniteGroup<T> : Group<T>,Collection<T> {
    override val size : Int

}