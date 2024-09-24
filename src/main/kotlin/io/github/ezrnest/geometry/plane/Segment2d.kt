package io.github.ezrnest.geometry.plane

import io.github.ezrnest.structure.*

data class Segment2d<T>(val start: Point2d<T>, val end: Point2d<T>) {


    override fun toString(): String {
        return "[$start, $end]"
    }
//    }

    //    private fun of(start: Point2d<T>, end: Point2d<T>): Segment2d<T> {
//        return Segment2d(start, end, model)
//    }
//
//    fun lengthSq(): T {
//        return start.distSq(end)
//    }
//
//    fun length(): T {
//        return start.dist(end)
//    }
//
//    fun middle(): Point2d<T> {
//        return start.middle(end)
//    }
//
//    /**
//     * Returns the point that divides the line segment between this point and the other point
//     * into the ratio `k:1`.
//     */
//    fun proportional(k: T): Point2d<T> {
//        return start.proportional(end, k)


//    fun contains(point: Point2d<T>): Boolean {
//        val model = model as OrderedField<T>
//        val (x,y) = point
//        val (x1,y1) = start
//        val (x2,y2) = end
//        return model.eval {
//            isEqual((x - x1) * (y2 - y1), (x2 - x1) * (y - y1))
//            min(x1, x2) <= x && x <= max(x1, x2) &&
//            min(y1, y2) <= y && y <= max(y1, y2)
//        }
//    }
}