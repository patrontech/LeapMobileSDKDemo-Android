package com.greencopper.thuzi.eventpass

import androidx.lifecycle.ViewModel
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

internal class EventPassViewModel(private val localStorage: LocalStorage) : ViewModel() {

    internal fun getBarcodeData(): Flow<BarcodeViewData> = combine(
        localStorage.project.thuzi.qrCode.state,
        localStorage.project.thuzi.userFirstName.state
    ) { qrCode, firstName ->
        BarcodeViewData(qrCode ?: "", firstName ?: "")
    }.flowOn(Dispatchers.IO)

    internal data class BarcodeViewData(var barcodeValue: String, val userFirstName: String)
}