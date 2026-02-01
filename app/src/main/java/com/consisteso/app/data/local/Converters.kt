package com.consisteso.app.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Room type converters for complex types
 */
class Converters {
    private val gson = Gson()
    
    // List<String> converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // List<Int> converters
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return gson.toJson(value ?: emptyList<Int>())
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // List<DayOfWeek> converters
    @TypeConverter
    fun fromDayOfWeekList(value: List<DayOfWeek>?): String {
        return gson.toJson(value?.map { it.value } ?: emptyList<Int>())
    }
    
    @TypeConverter
    fun toDayOfWeekList(value: String): List<DayOfWeek> {
        val listType = object : TypeToken<List<Int>>() {}.type
        val intList: List<Int> = gson.fromJson(value, listType) ?: emptyList()
        return intList.map { DayOfWeek.of(it) }
    }
    
    // LocalTime converters
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }
}
