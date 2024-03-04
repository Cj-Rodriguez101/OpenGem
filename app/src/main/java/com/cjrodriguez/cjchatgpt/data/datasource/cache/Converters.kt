package com.cjrodriguez.cjchatgpt.data.datasource.cache

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun convertListToString(stringList: List<String>): String {
        return if (stringList.isEmpty()) "" else stringList.joinToString(",")
    }

    @TypeConverter
    fun convertStringToList(string: String): List<String> {
        return if (string.isEmpty()) listOf() else string.split(",")
    }
}