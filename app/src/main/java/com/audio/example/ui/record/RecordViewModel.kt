package com.audio.example.ui.record

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecordViewModel : ViewModel() {
    private val _isRecord = MutableStateFlow<Boolean>(false)
    val isRecord = _isRecord.asStateFlow()

    fun setIsRecord(status: Boolean){
        _isRecord.value = status
    }
}