package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.managers.LocationManager
import com.example.coffeeshop.data.managers.SavedAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationState())
    val uiState: StateFlow<LocationState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().header("User-Agent", "CoffeeMarketplace/1.0").build()
            )
        }.build()

    private val backendBase = "http://10.0.2.2:8080/api/geocode"

    init { loadSavedLocation() }

    private fun loadSavedLocation() {
        _uiState.value = _uiState.value.copy(
            selectedAddress = locationManager.getSavedLocation(),
            courierComment = locationManager.getCourierComment(),
            savedAddresses = locationManager.getSavedAddresses()
        )
    }

    fun onShowAddressDialogChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showAddressDialog = show,
            editingAddressId = null,
            addressSearchQuery = "",
            detectedAddress = "",
            searchError = "",
            targetPoint = null,
            apartment = "",
            entrance = "",
            floor = "",
            intercom = "",
            savedAddresses = locationManager.getSavedAddresses()
        )
    }

    fun startEditingSavedAddress(addr: SavedAddress) {
        _uiState.value = _uiState.value.copy(
            showMyAddressesDialog = false,
            showAddressDialog = true,
            editingAddressId = addr.id,
            addressSearchQuery = addr.address,
            detectedAddress = addr.address,
            apartment = "",
            entrance = "",
            floor = "",
            intercom = "",
            searchError = "",
            targetPoint = null
        )
    }

    fun onApartmentChange(v: String) { _uiState.value = _uiState.value.copy(apartment = v) }
    fun onEntranceChange(v: String) { _uiState.value = _uiState.value.copy(entrance = v) }
    fun onFloorChange(v: String) { _uiState.value = _uiState.value.copy(floor = v) }
    fun onIntercomChange(v: String) { _uiState.value = _uiState.value.copy(intercom = v) }
    fun onCourierCommentChange(v: String) { _uiState.value = _uiState.value.copy(courierComment = v) }

    fun onAddressSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(addressSearchQuery = query, searchError = "")
        if (query.length >= 3) searchForward(query)
    }

    fun onCameraPositionChanged(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(lat = lat, lon = lon)
    }

    fun saveCurrentAddress(label: String) {
        val address = _uiState.value.detectedAddress.ifBlank { _uiState.value.selectedAddress }
        if (address.isBlank()) return
        val current = locationManager.getSavedAddresses().toMutableList()
        val newEntry = SavedAddress(
            id = UUID.randomUUID().toString(),
            label = label.ifBlank { address.take(20) },
            address = address
        )
        current.add(0, newEntry)
        locationManager.saveAddresses(current)
        _uiState.value = _uiState.value.copy(savedAddresses = current)
    }

    fun onShowMyAddressesDialogChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showMyAddressesDialog = show,
            savedAddresses = if (show) locationManager.getSavedAddresses() else _uiState.value.savedAddresses
        )
    }

    fun confirmSavedAddress(addr: SavedAddress) {
        locationManager.saveLocation(addr.address)
        _uiState.value = _uiState.value.copy(
            selectedAddress = addr.address,
            showMyAddressesDialog = false
        )
    }

    fun selectSavedAddress(addr: SavedAddress) {
        _uiState.value = _uiState.value.copy(
            detectedAddress = addr.address,
            addressSearchQuery = addr.address
        )
    }

    fun deleteSavedAddress(id: String) {
        val updated = locationManager.getSavedAddresses().filter { it.id != id }
        locationManager.saveAddresses(updated)
        _uiState.value = _uiState.value.copy(savedAddresses = updated)
    }

    fun updateAddressLabel(id: String, newLabel: String) {
        val updated = locationManager.getSavedAddresses().map {
            if (it.id == id) it.copy(label = newLabel) else it
        }
        locationManager.saveAddresses(updated)
        _uiState.value = _uiState.value.copy(savedAddresses = updated)
    }

    fun confirmLocation() {
        val base = _uiState.value.detectedAddress.ifBlank { _uiState.value.addressSearchQuery }
        if (base.isBlank()) return
        val s = _uiState.value
        val details = listOfNotNull(
            s.apartment.trim().takeIf { it.isNotBlank() }?.let { "кв. $it" },
            s.entrance.trim().takeIf { it.isNotBlank() }?.let { "под. $it" },
            s.floor.trim().takeIf { it.isNotBlank() }?.let { "эт. $it" },
            s.intercom.trim().takeIf { it.isNotBlank() }?.let { "домофон $it" }
        ).joinToString(", ")
        val fullAddress = if (details.isNotBlank()) "$base, $details" else base
        locationManager.saveLocation(fullAddress)
        locationManager.saveCourierComment(s.courierComment)

        val existing = locationManager.getSavedAddresses()
        val editingId = s.editingAddressId
        if (editingId != null) {
            locationManager.saveAddresses(
                existing.map { sa -> if (sa.id == editingId) sa.copy(address = fullAddress) else sa }
            )
        } else if (existing.none { it.address == fullAddress }) {
            val autoLabel = base.substringBefore(",").trim().take(30).ifBlank { base.take(30) }
            locationManager.saveAddresses(
                listOf(SavedAddress(UUID.randomUUID().toString(), autoLabel, fullAddress)) + existing
            )
        }

        _uiState.value = s.copy(
            selectedAddress = fullAddress,
            showAddressDialog = false,
            editingAddressId = null,
            addressSearchQuery = "",
            detectedAddress = "",
            targetPoint = null,
            apartment = "",
            entrance = "",
            floor = "",
            intercom = "",
            savedAddresses = locationManager.getSavedAddresses()
        )
    }

    private fun searchForward(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddressLoading = true, searchError = "")
            delay(800)
            if (_uiState.value.addressSearchQuery != query) return@launch
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "$backendBase/search?q=$encoded"
                val body = withContext(Dispatchers.IO) {
                    httpClient.newCall(Request.Builder().url(url).build()).execute().body?.string()
                }
                if (body.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        searchError = "Нет ответа от сервера",
                        isAddressLoading = false
                    )
                    return@launch
                }
                val arr = JSONArray(body)
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    val lat = obj.getString("lat").toDouble()
                    val lon = obj.getString("lon").toDouble()
                    val displayName = obj.optString("display_name")
                    _uiState.value = _uiState.value.copy(
                        targetPoint = lat to lon,
                        detectedAddress = displayName,
                        searchError = "",
                        isAddressLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        searchError = "Адрес не найден",
                        isAddressLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(searchError = "Ошибка поиска", isAddressLoading = false)
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class LocationState(
    val selectedAddress: String = "",
    val addressSearchQuery: String = "",
    val detectedAddress: String = "",
    val lat: Double = 55.751244,
    val lon: Double = 37.618423,
    val targetPoint: Pair<Double, Double>? = null,
    val isAddressLoading: Boolean = false,
    val showAddressDialog: Boolean = false,
    val searchError: String = "",
    val apartment: String = "",
    val entrance: String = "",
    val floor: String = "",
    val intercom: String = "",
    val courierComment: String = "",
    val savedAddresses: List<SavedAddress> = emptyList(),
    val showMyAddressesDialog: Boolean = false,
    val editingAddressId: String? = null,
    val error: String? = null
)
