package com.audio.example.ui.record

import ai.instavision.ffmpegkit.FFmpegKit
import ai.instavision.ffmpegkit.ReturnCode
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.checkPermissions
import com.audio.example.core.extensions.dLog
import com.audio.example.core.extensions.eLog
import com.audio.example.core.extensions.goToSettings
import com.audio.example.core.extensions.goneAnim
import com.audio.example.core.extensions.handleBackLeftToRight
import com.audio.example.core.extensions.hideNavigation
import com.audio.example.core.extensions.requestPermission
import com.audio.example.core.extensions.setOnSingleClick
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.showToast
import com.audio.example.core.extensions.visible
import com.audio.example.core.extensions.visibleAnim
import com.audio.example.core.helper.StringHelper
import com.audio.example.core.utils.key.PermissionKey
import com.audio.example.core.utils.key.RequestKey
import com.audio.example.core.utils.key.ValueKey
import com.audio.example.databinding.ActivityRecordBinding
import com.audio.example.dialog.ConfirmDialog
import com.audio.example.ui.permission.PermissionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.getValue

class RecordActivity : BaseActivity<ActivityRecordBinding>() {

    private val permissionViewModel: PermissionViewModel by viewModels()
    private val viewModel: RecordViewModel by viewModels()

    override fun setViewBinding(): ActivityRecordBinding {
        return ActivityRecordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        checkRecordAudioPermission()
        viewModel.setPitch(sharePreference.getPitchShiftAudio())
        viewModel.setSpeedAudio(sharePreference.getSpeedAudio())
    }

    override fun dataObservable() {

//        durationRecoding
        lifecycleScope.launch {
            viewModel.durationRecoding.collect { duration ->
                binding.tvCountRecord.text = StringHelper.formatDuration(duration)
            }
        }

//        isPlayingOrigin
        lifecycleScope.launch {
            viewModel.isPlayingOrigin.collect { status ->
                if (!status) {
                    binding.tvPlay.text = getString(R.string.play)
                    binding.imvIcPlay.setImageResource(R.drawable.ic_play)
                    binding.imvBgPlay.setImageResource(R.drawable.img_bg_btn_play)
                } else {
                    binding.tvPlay.text = getString(R.string.stop)
                    binding.imvIcPlay.setImageResource(R.drawable.ic_stop_small)
                    binding.imvBgPlay.setImageResource(R.drawable.img_bg_btn_stop)
                }
            }
        }

//        isPlayingReverse
        lifecycleScope.launch {
            viewModel.isPlayingReverse.collect { status ->
                if (!status) {
                    binding.tvReverse.text = getString(R.string.reverse)
                    binding.imvIcReverse.setImageResource(R.drawable.ic_reverse)
                    binding.imvBgReverse.setImageResource(R.drawable.img_bg_btn_reverse)
                } else {
                    binding.tvReverse.text = getString(R.string.stop)
                    binding.imvIcReverse.setImageResource(R.drawable.ic_stop_small)
                    binding.imvBgReverse.setImageResource(R.drawable.img_bg_btn_stop)
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClickWithSound { handleBackLeftToRight() }
            btnRecord.setOnSingleClick { handleRecord() }
            btnPlay.setOnSingleClick { handlePlayOriginAudio() }
            btnReverse.setOnSingleClick { handlePlayReverseAudio() }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
            tvCenter.text = getString(R.string.record)
            tvCenter.visible()
        }
    }

    /** Kiểm tra quyền ghi âm **/
    private fun checkRecordAudioPermission() {
        if (!checkPermissions(permissionViewModel.getRecordAudioPermissions())) {
            if (permissionViewModel.needGoToSettings(
                    sharePreference, PermissionKey.RECORD_AUDIO_KEY
                )
            ) {
                goToSettings(
                    settingsDialog = { dialog -> viewModel.settingsDialog = dialog },
                    onCancelClick = { finish() })
            } else {
                requestPermission(
                    permissionViewModel.getRecordAudioPermissions(),
                    RequestKey.RECORD_AUDIO_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted =
            grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.RECORD_AUDIO_REQUEST_CODE -> permissionViewModel.updateRecordAudioGranted(
                sharePreference, granted
            )
        }
        if (!granted) checkRecordAudioPermission()
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.settingsDialog?.let {
            if (it.isShowing) {
                it.dismiss()
                hideNavigation(true)
            }
        }
        checkRecordAudioPermission()

        setUpStopRecord()
    }

    /** Đảo ngược âm thanh bằng FFmpeg **/
//    private fun reverseAudio(inputPath: String, outputPath: String, onDone: (Boolean) -> Unit) {
//        // .wav => sử dụng codec PCM 16-bit (ổn định, không nén, không lỗi)
//        val command =
//            "-y -i \"$inputPath\" -af areverse -ar 44100 -ac 2 -c:a pcm_s16le \"$outputPath\""
//
//        FFmpegKit.executeAsync(command) { session ->
//            val returnCode = session.returnCode
//            if (ReturnCode.isSuccess(returnCode)) {
//                onDone(true)
//            } else {
//                eLog("FFmpeg failed with code: $returnCode")
//                onDone(false)
//            }
//        }
//    }
    private fun reverseAudio(inputPath: String, outputPath: String, onDone: (Boolean) -> Unit) {
        // Đảm bảo định dạng WAV (PCM 16-bit, stereo, 44.1kHz)
        val command =
            "-y -i \"$inputPath\" -af areverse -ar 44100 -ac 2 -c:a pcm_s16le \"$outputPath\""

        FFmpegKit.executeAsync(command) { session ->
            val code = session.returnCode
            if (ReturnCode.isSuccess(code)) {
                onDone(true)
            } else {
                eLog("FFmpeg reverse failed with code: $code")
                onDone(false)
            }
        }
    }

    fun startRecord() {
        try {
            viewModel.params = PlaybackParams()
            val fileName = "${System.currentTimeMillis()}.wav"
            val outputDirectory = File(filesDir, ValueKey.RECORD_ALBUM)
            if (!outputDirectory.exists()) outputDirectory.mkdirs()

            viewModel.originAudioFile = File(outputDirectory, fileName)

            viewModel.mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(viewModel.originAudioFile!!.absolutePath)
                prepare()
                start()
            }
            dLog("Recording started: ${viewModel.originAudioFile!!.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Không thể bắt đầu ghi âm!")
        }
    }

    /** Dừng ghi âm và đảo ngược phát lại **/
    private fun stopRecord() {
        try {
            viewModel.mediaRecorder?.let { recorder ->
                try {
                    recorder.stop()
                } catch (e: Exception) {
                    eLog("stopRecord: stop() failed - ${e.message}")
                } finally {
                    recorder.release()
                }
            }
            viewModel.mediaRecorder = null
            viewModel.job?.cancel()

            val file = viewModel.originAudioFile
            if (file == null || !file.exists()) {
                eLog("stopRecord: no recorded file found")
                return
            }

            val outputDir = File(filesDir, ValueKey.RECORD_REVERSE_ALBUM)
            if (!outputDir.exists()) outputDir.mkdirs()

            val reversedFile = File(outputDir, "${file.nameWithoutExtension}_reversed.wav")
            viewModel.reversedAudioFile = reversedFile

            // Dùng FFmpeg đảo ngược âm thanh (.wav)
            reverseAudio(file.absolutePath, reversedFile.absolutePath) { success ->
                if (success) {
                    dLog("Reverse success: ${reversedFile.absolutePath}")
                } else {
                    eLog("Reverse failed")
                    viewModel.reversedAudioFile = null
                    showToast(getString(R.string.an_error_occurred_please_record_again))
                }
            }
        } catch (e: Exception) {
            eLog("stopRecord global error: ${e.message}")
            viewModel.reversedAudioFile = null
        }
    }

    private fun playAudio(file: File): Boolean {
        return try {
            if (viewModel.mediaPlayer?.isPlaying == true) viewModel.mediaPlayer?.stop()
            viewModel.mediaPlayer?.reset()
            viewModel.mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    viewModel.setIsPlayingReverse(false)
                    viewModel.setIsPlayingOrigin(false)
                }
                viewModel.params?.pitch = viewModel.pitchAudio.value
                viewModel.params?.speed = viewModel.speedAudio.value
                playbackParams = viewModel.params!!
                start()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(getString(R.string.an_error_occurred_please_record_again))
            false
        }
    }

    private fun stopAudio() {
        viewModel.mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
            } catch (e: Exception) {
                eLog("stopAudio error: ${e.message}")
            }
        }
    }

    private fun handleRecord() {
        if (viewModel.isPlayingOrigin.value || viewModel.isPlayingReverse.value) {
            showToast(getString(R.string.please_stop_playing_sound))
            return
        }
        if (!viewModel.isRecord.value) {
            setUpRecoding()
        } else {
            setUpStopRecord()
        }
        viewModel.setIsRecord(!viewModel.isRecord.value)
    }

    private fun setUpRecoding() {
        binding.apply {
            tvTapRecord.text = getString(R.string.tap_to_stop_record)
            imvIconRecord.setImageResource(R.drawable.ic_stop_record)
            ltPlaying.visibleAnim(300)
            startCountTime()
            startRecord()
        }
    }

    private fun startCountTime() {
        binding.apply {
            viewModel.setUpDurationRecording(0L)
            viewModel.job?.cancel()

            viewModel.job = lifecycleScope.launch {
                while (isActive) {
                    delay(1000)
                    viewModel.setUpDurationRecording(
                        viewModel.durationRecoding.value + 1000L
                    )
                }
            }
        }
    }

    private fun setUpStopRecord() {
        binding.apply {
            tvTapRecord.text = getString(R.string.tap_to_record)
            imvIconRecord.setImageResource(R.drawable.ic_mic)
            ltPlaying.goneAnim()
            stopCountTime()
            stopRecord()
        }
    }

    private fun stopCountTime() {
        viewModel.job?.cancel()
    }

    private fun handlePlayOriginAudio() {
        if (viewModel.isRecord.value) {
            showToast(getString(R.string.please_stop_recording))
            return
        }
        if (!viewModel.isPlayingOrigin.value) {
            if (viewModel.originAudioFile != null) {
                if (playAudio(viewModel.originAudioFile!!)) {
                    viewModel.setIsPlayingReverse(false)
                    viewModel.setIsPlayingOrigin(true)
                }
            } else {
                showToast(getString(R.string.please_record_the_audio))
            }

        } else {
            viewModel.setIsPlayingOrigin(false)
            stopAudio()
        }
    }

    private fun handlePlayReverseAudio() {
        if (viewModel.isRecord.value) {
            showToast(getString(R.string.please_stop_recording))
            return
        }
        if (!viewModel.isPlayingReverse.value) {
            if (viewModel.reversedAudioFile != null) {
                if (playAudio(viewModel.reversedAudioFile!!)) {
                    viewModel.setIsPlayingReverse(true)
                    viewModel.setIsPlayingOrigin(false)
                }
            } else {
                showToast(getString(R.string.please_record_the_audio))
            }

        } else {
            viewModel.setIsPlayingReverse(false)
            stopAudio()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            viewModel.mediaPlayer?.release()
            viewModel.mediaPlayer = null
        } catch (_: Exception) {
        }
    }

    override fun onStop() {
        super.onStop()

        if (viewModel.isPlayingOrigin.value || viewModel.isPlayingReverse.value) {
            try {
                stopAudio()
                viewModel.setIsPlayingOrigin(false)
                viewModel.setIsPlayingReverse(false)
                dLog("onStop: stop playing audio")
            } catch (e: Exception) {
                eLog("onStop stopAudio error: ${e.message}")
            }
        }

        if (viewModel.isRecord.value) {
            try {
                dLog("onStop: stop recording")
                stopRecord()
            } catch (e: Exception) {
                eLog("onStop stopRecord error: ${e.message}")
            } finally {
                viewModel.setIsRecord(false)
            }
        }
    }

}
