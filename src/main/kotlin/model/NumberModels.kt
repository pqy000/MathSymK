package cn.mathsymk.model

import cn.mathsymk.model.struct.AddGroupModel
import cn.mathsymk.model.struct.FieldModel
import cn.mathsymk.model.struct.RingModel
import cn.mathsymk.structure.AddGroup
import cn.mathsymk.structure.Field
import cn.mathsymk.structure.Ring
import util.ModelPatterns


object NumberModels {
    /**
     * Gets a group calculator on the GroupNumberModel.
     */
    fun <T : AddGroupModel<T>> asGroup(zero: T): AddGroup<T> {
        return object : AddGroup<T> {
            override fun contains(x: T): Boolean {
                return true
            }

            override val zero: T = zero

            override fun add(x: T, y: T): T {
                return x + y
            }

            override fun negate(x: T): T {
                return -x
            }

            override fun subtract(x: T, y: T): T {
                return x - y
            }


            override fun isEqual(x: T, y: T): Boolean {
                return x == y
            }
        }
    }

    fun <T : RingModel<T>> asRing(zero: T) = object : Ring<T> {
        override fun contains(x: T): Boolean {
            return true
        }

        override val zero: T = zero

        override fun add(x: T, y: T): T {
            return x + y
        }

        override fun negate(x: T): T {
            return -x
        }

        override fun subtract(x: T, y: T): T {
            return x - y
        }

        override fun multiply(x: T, y: T): T {
            return x * y
        }

        override fun isEqual(x: T, y: T): Boolean {
            return x == y
        }
    }

    fun <T : FieldModel<T>> asField(zero: T, one: T, characteristic: Long = 0) = object : Field<T> {
        override fun contains(x: T): Boolean {
            return true
        }

        override val zero: T = zero

        override val one: T = one

        override val characteristic: Long = characteristic

        override fun add(x: T, y: T): T {
            return x + y
        }

        override fun negate(x: T): T {
            return -x
        }

        override fun subtract(x: T, y: T): T {
            return x - y
        }

        override fun multiply(x: T, y: T): T {
            return x * y
        }

        override fun isEqual(x: T, y: T): Boolean {
            return x == y
        }

        override fun divide(x: T, y: T): T {
            return x / y
        }

        override fun reciprocal(x: T): T {
            return x.inv()
        }
    }
}