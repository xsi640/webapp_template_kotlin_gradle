package com.suyang.exceptions

enum class ErrorType private constructor(val value: Int) {
    API(400), System(500), Database(600);

    override fun toString(): String {
        return this.value.toString()
    }

    companion object {

        fun valueOf(value: Int): ErrorType {
            var result = API
            when (value) {
                400 -> result = ErrorType.API
                500 -> result = ErrorType.System
                600 -> result = ErrorType.Database
            }
            return result
        }
    }
}
