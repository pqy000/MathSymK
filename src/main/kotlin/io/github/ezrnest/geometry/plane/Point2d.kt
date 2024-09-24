package io.github.ezrnest.geometry.plane

import io.github.ezrnest.structure.*
import io.github.ezrnest.structure.eval

data class Point2d<T>(val x: T, val y: T, val model: EqualPredicate<T>) {

    private fun of(x: T, y: T): Point2d<T> {
        return Point2d(x, y, model)
    }

    fun distSq(other: Point2d<T>): T {
        val model = model as Ring<T>
        return model.eval {
            (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)
        }
    }

    fun dist(other: Point2d<T>): T {
        val model = model as Reals<T>
        return model.sqrt(distSq(other))
    }

    fun middle(other: Point2d<T>): Point2d<T> {
        val model = model as Field<T>
        return model.eval { of(divideLong(x + other.x,2L), divideLong(y + other.y,2L)) }
    }

    /**
     * Returns the point that divides the line segment between this point and the other point
     * into the ratio `k:1`.
     */
    fun proportional(other: Point2d<T>, k: T): Point2d<T> {
        val model = model as Field<T>
        with(model) {
            val d = one + k
            val x = (x + k * other.x) / d
            val y = (y + k * other.y) / d
            return of(x, y)
        }
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}