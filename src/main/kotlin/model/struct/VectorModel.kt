package cn.mathsymk.model.struct

/**
 * Describes the number model of a (left) module.
 */
interface ModuleModel<R, V : ModuleModel<R, V>> : AddGroupModel<V> {

    /**
     * Performs the scalar multiplication.
     */
    operator fun times(k: R): V

}

/**
 * Describe the number model for a linear space,
 */
interface VectorModel<K, V : VectorModel<K, V>> : ModuleModel<K, V> {

    /**
     * Performs the scalar multiplication.
     */
    override fun times(k: K): V

    /**
     * Performs the scalar division.
     */
    operator fun div(k: K): V


    /**
     * Determines whether this is linear relevant to [v].
     *
     * This method is optional.
     */
    fun isLinearRelevant(v: V): Boolean {
        throw UnsupportedOperationException()
    }
}

operator fun <K, V : ModuleModel<K, V>> K.times(v: ModuleModel<K, V>) = v.times(this)


interface AlgebraModel<K, V : AlgebraModel<K, V>> : VectorModel<K, V>, RingModel<V> {
}
