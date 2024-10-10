package io.github.ezrnest.symbolic
// created at 2024/10/05
import io.github.ezrnest.model.Multinomial
import java.util.*


typealias Multi = Multinomial<Rational>

/**
 * Defines a typed key for meta data.
 */
data class TypedKey<T>(val name: String) {
    override fun toString(): String {
        return name
    }
}

fun <T> Node.addMeta(key: TypedKey<T>, value: T) {
    val meta = meta
    if (meta is MutableMap) {
        meta[key] = value
        return
    }
    val mutableMeta: MutableMap<TypedKey<*>, Any?> =
        if (meta.isEmpty()) {
            IdentityHashMap<TypedKey<*>, Any?>(4)
        } else {
            IdentityHashMap<TypedKey<*>, Any?>(meta)
        }
    mutableMeta[key] = value
    this.meta = mutableMeta
}

fun <T> Node.getMeta(key: TypedKey<T>): T? = meta.getTyped(key)

fun <T> Node.getMeta(key: TypedKey<T>, default: T): T = meta.getTyped(key, default)

operator fun <T> Node.get(key: TypedKey<T>): T? = getMeta(key)

operator fun <T> Node.contains(key: TypedKey<T>): Boolean = meta.containsKey(key)

operator fun <T> Node.set(key: TypedKey<T>, value: T) = addMeta(key, value)

@Suppress("UNCHECKED_CAST")
fun <T> Map<TypedKey<*>, Any?>.getTyped(key: TypedKey<T>): T? = get(key) as T?

@Suppress("UNCHECKED_CAST")
fun <T> Map<TypedKey<*>, Any?>.getTyped(key: TypedKey<T>, default: T): T = getOrDefault(key, default) as T



object EMeta {

    val asMulti = TypedKey<Multi>("asMulti")

    val sorted = TypedKey<Boolean>("sorted")


    val positive = TypedKey<Boolean>("positive")

    val integer = TypedKey<Boolean>("integer")

    val rational = TypedKey<Boolean>("rational")

    val real = TypedKey<Boolean>("real")
}