package com.cjrodriguez.cjchatgpt.data.datasource.cache

import androidx.room.TypeConverter
import com.cjrodriguez.cjchatgpt.data.util.toByteArrayCustom
import com.cjrodriguez.cjchatgpt.data.util.toCustomString

class Converters {
    @TypeConverter
    fun fromString(value: String): ByteArray {
        return value.toByteArrayCustom()
    }

    @TypeConverter
    fun toByteArray(value: ByteArray): String {
        return value.toCustomString()
    }
}
