package io.github.ezrnest.geometry.space

import io.github.ezrnest.structure.*
import java.util.function.Predicate


/**
 * A class representing a line segment in 3D space.
 *
 * @param T The type of the coordinates.
 * @property start The starting point of the segment.
 * @property end The ending point of the segment.
 * @property model The model used for equality and arithmetic operations.
 */
@JvmRecord
data class Segment3d<T>(val start: Point3d<T>, val end: Point3d<T>, val model: EqualPredicate<T>) {

    private fun of(start: Point3d<T>, end: Point3d<T>): Segment3d<T> {
        return Segment3d(start, end, model)
    }

    fun lengthSq(): T {
        return start.distSq(end)
    }

    fun length(): T {
        return start.dist(end)
    }

    fun middle(): Point3d<T> {
        return start.middle(end)
    }

    fun proportional(k: T): Point3d<T> {
        return start.proportional(end, k)
    }

    override fun toString(): String {
        return "[$start, $end]"
    }

    fun contains(point: Point3d<T>): Boolean {
        val model = model as OrderedField<T>
        val (x, y, z) = point
        val (x1, y1, z1) = start
        val (x2, y2, z2) = end
        return model.eval {
            isEqual((x - x1) * (y2 - y1), (x2 - x1) * (y - y1)) &&
                    isEqual((x - x1) * (z2 - z1), (x2 - x1) * (z - z1)) &&
                    min(x1, x2) <= x && x <= max(x1, x2) &&
                    min(y1, y2) <= y && y <= max(y1, y2) &&
                    min(z1, z2) <= z && z <= max(z1, z2)
        }
    }

    fun containsPredicate(): Predicate<Point3d<T>> {
        // use a cache to speed up the evaluation
        val model = model as OrderedField<T>
        with(model) {
            val (dx, dy, dz) = end - start
            val d2 = dx * dx + dy * dy + dz * dz
            return Predicate { point ->
                val (px, py, pz) = point - start
                val dot = dx * px + dy * py + dz * pz
                val t = dot / d2
                t >= zero && t <= one &&
                        isEqual(px * dy, py * dx)
                        && isEqual(px * dz, pz * dx)
                        && isEqual(py * dz, pz * dy)
            }
        }
    }
}