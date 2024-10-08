package io.github.ezrnest.symbolic
// created at 2024/10/05
import io.github.ezrnest.model.Multinomial
import kotlin.collections.plus



typealias Multi = Multinomial<Rational>

data class TypedKey<@Suppress("UNUSED") T>(val key: String)

fun <T> Node.addMeta(key: TypedKey<T>, value: T) {
    val k = key.key
    if (meta.isEmpty()) {
        meta = mutableMapOf(k to value)
    } else if (meta is MutableMap) {
        (meta as MutableMap)[k] = value
    } else {
        meta = meta + (k to value)
    }
}

fun <T> Node.getMeta(key: TypedKey<T>): T? = meta[key]

fun <T> Node.getMeta(key: TypedKey<T>, default: T): T = meta[key] ?: default

operator fun <T> Node.get(key: TypedKey<T>): T? = getMeta(key)

operator fun <T> Node.contains(key: TypedKey<T>): Boolean = meta.containsKey(key.key)

operator fun <T> Node.set(key: TypedKey<T>, value: T) = addMeta(key, value)

@Suppress("UNCHECKED_CAST")
operator fun <T> Map<String,Any?>.get(key: TypedKey<T>): T? = get(key.key) as T?

operator fun <V,T:V> MutableMap<String,V>.set(key: TypedKey<T>, value: T) {
    this[key.key] = value
}



object EMeta {


    val asMulti = TypedKey<Multi>("asMulti")

    val sorted = TypedKey<Boolean>("sorted")


    val positive = TypedKey<Boolean>("positive")

    val integer = TypedKey<Boolean>("integer")

    val rational = TypedKey<Boolean>("rational")

    val real = TypedKey<Boolean>("real")
}