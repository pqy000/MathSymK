package cn.mathsymk.model.struct

/**
 * Describes the number model of a (left) module.
 */
interface ModuleModel<R, V : ModuleModel<R, V>> : AddGroupModel<V> {


    /**
     * Multiplies this by a scalar `k`.
     */
    fun scalarMul(k: R): V

    /**
     * The scalar multiplication.
     */
    operator fun times(k: R): V{
        return scalarMul(k)
    }

}

/**
 * Describe the number model for a linear space,
 */
interface VectorModel<K, V : VectorModel<K, V>> : ModuleModel<K, V> {

    /**
     * The scalar division: `this / k`.
     */
    fun scalarDiv(k: K): V

    /**
     * The scalar division `this / k`, a shorthand for [scalarDiv].
     *
     * @see scalarDiv
     */
    operator fun div(k: K): V {
        return scalarDiv(k)
    }


    /**
     * Determines whether this is linear relevant to [v].
     *
     * This method is optional.
     */
    fun isLinearRelevant(v: V): Boolean {
        throw UnsupportedOperationException()
    }
}

// since there is no type constrain on K, introducing this method would cause pollution
//operator fun <K, V : ModuleModel<K, V>> K.times(v: ModuleModel<K, V>) = v.times(this)


/**
 * A model class for an algebra.
 *
 * @see [cn.mathsymk.structure.Algebra]
 */
interface AlgebraModel<K, V : AlgebraModel<K, V>> : VectorModel<K, V>, RingModel<V> {
}
