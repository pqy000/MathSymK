package io.github.ezrnest.symbolic

import io.github.ezrnest.model.Multinomial
import kotlin.collections.plus



typealias Multi = Multinomial<Rational>

data class MetaKey<@Suppress("UNUSED") T>(val key: String)

fun <T> Node.addMeta(key: MetaKey<T>, value: T) {
    val k = key.key
    if (meta.isEmpty()) {
        meta = mutableMapOf(k to value)
    } else if (meta is MutableMap) {
        (meta as MutableMap)[k] = value
    } else {
        meta = meta + (k to value)
    }
}

fun <T> Node.getMeta(key: MetaKey<T>): T? {
    @Suppress("UNCHECKED_CAST")
    return meta[key.key] as T?
}

fun <T> Node.getMeta(key: MetaKey<T>, default: T): T {
    return getMeta(key) ?: default
}

operator fun <T> Node.get(key: MetaKey<T>): T? = getMeta(key)

operator fun <T> Node.contains(key: MetaKey<T>): Boolean = meta.containsKey(key.key)

operator fun <T> Node.set(key: MetaKey<T>, value: T) = addMeta(key, value)

object EMeta {


//    val asInt = MetaKey<BigInteger?>("asInt")
//    /**
//     * Whether the node is a rational number.
//     *
//     * If the node is a rational number, the value is the rational number.
//     */
//    val asRational = MetaKey<Rational?>("asRational")

    val asMulti = MetaKey<Multi>("asMulti")

    val sorted = MetaKey<Boolean>("sorted")

}