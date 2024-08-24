package cn.mathsymk.util.exceptions

class NotInvertibleExeception(message : String) : ArithmeticException(message) {
    constructor() : this("The object is not invertible.")
}
