package io.github.ezrnest.mathsymk.geometry.space

/**
 * A class representing a point in 3D space.
 *
 * @param T The type of the coordinates.
 * @property x The x-coordinate of the point.
 * @property y The y-coordinate of the point.
 * @property z The z-coordinate of the point.
 */
@JvmRecord
data class Point3d<T>(val x: T, val y: T, val z: T) {

    override fun toString(): String {
        return "($x, $y, $z)"
    }

//    private fun of(x: T, y: T, z: T): Point3d<T> {
//        return Point3d(x, y, z, model)
//    }
//
//    /**
//     * Calculates the squared distance between this point and another point.
//     *
//     *
//     * @param other The other point.
//     * @return The squared distance.
//     */
//    fun distSq(other: Point3d<T>): T {
//        val model = model as Ring<T>
//        return model.eval {
//            (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z)
//        }
//    }
//
//    fun dist(other: Point3d<T>): T {
//        val model = model as Reals<T>
//        return model.sqrt(distSq(other))
//    }
//
//    fun middle(other: Point3d<T>): Point3d<T> {
//        val model = model as Field<T>
//        return model.eval { of(divideLong(x + other.x, 2L), divideLong(y + other.y, 2L), divideLong(z + other.z, 2L)) }
//    }
//
//    fun proportional(other: Point3d<T>, k: T): Point3d<T> {
//        val model = model as Field<T>
//        with(model) {
//            val d = one + k
//            val x = (x + k * other.x) / d
//            val y = (y + k * other.y) / d
//            val z = (z + k * other.z) / d
//            return of(x, y, z)
//        }
//    }
//

//
//    operator fun minus(other: Point3d<T>): Point3d<T> {
//        val model = model as AddGroup<T>
//        return model.eval { of(x - other.x, y - other.y, z - other.z) }
//    }
}
