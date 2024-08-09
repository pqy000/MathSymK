package cn.mathsymk.model

interface Group<T : Any> {

    fun contains(x: T): Boolean

    fun isEqual(x: T, y: T): Boolean

    fun apply(x: T, y: T): T

}