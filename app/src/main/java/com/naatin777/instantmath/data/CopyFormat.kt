package com.naatin777.instantmath.data

enum class CopyFormat(val value: Int) {
    TEXT(0),
    IMAGE(1),
    ;

    companion object {
        fun fromValue(value: Int): CopyFormat =
            entries.find { it.value == value } ?: TEXT
    }
}
