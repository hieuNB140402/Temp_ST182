package com.audio.example.ui.record

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import androidx.lifecycle.ViewModel
import com.audio.example.dialog.ConfirmDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class RecordViewModel : ViewModel() {
    private val _isRecord = MutableStateFlow<Boolean>(false)
    val isRecord = _isRecord.asStateFlow()

    private val _durationRecoding = MutableStateFlow<Long>(0L)
    val durationRecoding = _durationRecoding.asStateFlow()

    private val _isPlayingOrigin = MutableStateFlow<Boolean>(false)
    val isPlayingOrigin = _isPlayingOrigin.asStateFlow()

    private val _isPlayingReverse = MutableStateFlow<Boolean>(false)
    val isPlayingReverse = _isPlayingReverse.asStateFlow()

    var settingsDialog: ConfirmDialog? = null
    var mediaRecorder: MediaRecorder? = null
    var mediaPlayer: MediaPlayer? = null
    var originAudioFile: File? = null
    var reversedAudioFile: File? = null
    var params: PlaybackParams? = null

    private val _pitchAudio = MutableStateFlow<Float>(1f)
    val pitchAudio = _pitchAudio.asStateFlow()

    private val _speedAudio = MutableStateFlow<Float>(1f)
    val speedAudio = _speedAudio.asStateFlow()

    var job: Job? = null

    fun setIsRecord(status: Boolean){
        _isRecord.value = status
    }

    fun setUpDurationRecording(duration: Long){
        _durationRecoding.value = duration
    }

    fun setIsPlayingOrigin(status: Boolean){
        _isPlayingOrigin.value = status
    }

    fun setIsPlayingReverse(status: Boolean){
        _isPlayingReverse.value = status
    }

    fun setPitch(pitch: Float){
        _pitchAudio.value = pitch
    }

    fun setSpeedAudio(speed: Float){
        _speedAudio.value = speed
    }
}