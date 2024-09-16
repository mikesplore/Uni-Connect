package com.mike.uniadmin.model.localDatabase


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mike.uniadmin.model.courses.AcademicYear

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun fromAcademicYearList(value: List<AcademicYear>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toAcademicYearList(value: String): List<AcademicYear> {
        val listType = object : TypeToken<List<AcademicYear>>() {}.type
        return Gson().fromJson(value, listType)
    }
}