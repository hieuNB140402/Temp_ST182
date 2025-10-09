package com.audio.example.ui.home

import androidx.lifecycle.ViewModel
import com.audio.example.data.local.MusicDAO
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class DataViewModel @Inject constructor(private val musicDao: MusicDAO): ViewModel() {

}