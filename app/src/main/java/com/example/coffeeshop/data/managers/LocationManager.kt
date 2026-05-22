package com.example.coffeeshop.data.managers

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedAddress(
    val id: String,
    val label: String,
    val address: String
)

class LocationManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val locationKey = "selected_location"
    private val commentKey = "courier_comment"
    private val savedAddressesKey = "saved_addresses"

    fun saveLocation(location: String) {
        sharedPreferences.edit().putString(locationKey, location).apply()
    }

    fun getSavedLocation(): String =
        sharedPreferences.getString(locationKey, "Москва, Россия") ?: "Москва, Россия"

    fun saveCourierComment(comment: String) {
        sharedPreferences.edit().putString(commentKey, comment).apply()
    }

    fun getCourierComment(): String =
        sharedPreferences.getString(commentKey, "") ?: ""

    fun clearLocation() {
        sharedPreferences.edit().remove(locationKey).apply()
    }

    fun getSavedAddresses(): List<SavedAddress> {
        val json = sharedPreferences.getString(savedAddressesKey, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                SavedAddress(
                    id = obj.getString("id"),
                    label = obj.getString("label"),
                    address = obj.getString("address")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    fun saveAddresses(addresses: List<SavedAddress>) {
        val arr = JSONArray()
        addresses.forEach { sa ->
            arr.put(JSONObject().apply {
                put("id", sa.id)
                put("label", sa.label)
                put("address", sa.address)
            })
        }
        sharedPreferences.edit().putString(savedAddressesKey, arr.toString()).apply()
    }
}
