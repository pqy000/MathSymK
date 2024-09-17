package io.github.ezrnest.model.struct

import io.github.ezrnest.structure.Group

interface FiniteGroup<T> : Group<T>,Collection<T> {
    override val size : Int

}