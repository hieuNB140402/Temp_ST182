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
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.checkPermissions
import com.audio.example.core.extensions.dLog
import com.audio.example.core.extensions.goToSettings
import com.audio.example.core.extensions.handleBackLeftToRight
import com.audio.example.core.extensions.hideNavigation
import com.audio.example.core.extensions.requestPermission
import com.audio.example.core.extensions.setOnSingleClickWithSound
import com.audio.example.core.extensions.visible
import com.audio.example.core.utils.key.PermissionKey
import com.audio.example.core.utils.key.RequestKey
import com.audio.example.core.utils.key.ValueKey
import com.audio.example.databinding.ActivityRecordBinding
import com.audio.example.dialog.ConfirmDialog
import com.audio.example.ui.permission.PermissionViewModel
import java.io.File
import kotlin.getValue

class RecordActivity : BaseActivity<ActivityRecordBinding>() {
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var settingsDialog: ConfirmDialog? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var outputFile: File? = null

    private var params: PlaybackParams? = null
    var pitch = 1f
    override fun setViewBinding(): ActivityRecordBinding {
        return ActivityRecordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        checkRecordAudioPermission()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClickWithSound { handleBackLeftToRight() }
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

    private fun checkRecordAudioPermission() {
        if (!checkPermissions(permissionViewModel.getRecordAudioPermissions())) {
            if (permissionViewModel.needGoToSettings(sharePreference, PermissionKey.RECORD_AUDIO_KEY)) {
                goToSettings(
                    settingsDialog = { dialog ->
                    settingsDialog = dialog
                }, onCancelClick = {
                    finish()
                })
            } else {
                requestPermission(permissionViewModel.getRecordAudioPermissions(), RequestKey.RECORD_AUDIO_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.RECORD_AUDIO_REQUEST_CODE -> permissionViewModel.updateRecordAudioGranted(
                sharePreference, granted
            )
        }
        if (!granted) {
            checkRecordAudioPermission()
        }
    }

    override fun onRestart() {
        super.onRestart()
        settingsDialog?.let {
            if (it.isShowing) {
                it.dismiss()
                hideNavigation(true)
            }
        }
        checkRecordAudioPermission()
    }

    fun reverseAudio(inputPath: String, outputPath: String, onDone: (Boolean) -> Unit) {
        val command = "-i $inputPath -af areverse $outputPath"

        FFmpegKit.executeAsync(command) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                onDone(true)
            } else {
                val failLog = session.allLogsAsString
                val failTrace = session.failStackTrace
                dLog("reverseAudio: ${failLog}")
                dLog("reverseAudio__: ${failTrace?.toString()}")
                onDone(false)
            }
        }
    }


    fun startRecord(){
        params = PlaybackParams()
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        val fileName = System.currentTimeMillis()
        val outputDirectory = File(filesDir, ValueKey.RECORD_ALBUM)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        outputFile = File(filesDir, "$fileName.wav")
        mediaRecorder?.setOutputFile(outputFile)
        mediaRecorder?.prepare();
        mediaRecorder?.start();
    }

    fun stopRecord(){
        if (outputFile != null) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder?.release()
                outputFile = File(
                    outputFile!!.path.replace(
                        outputFile!!.name,
                        "reverse${outputFile!!.name}"
                    )
                )
                reverseAudio(outputFile!!.path, outputFile!!.path) {
                    if (it) {
                        mediaPlayer = MediaPlayer.create(
                            applicationContext,
                            outputFile!!.path.toUri()
                        )
                        mediaPlayer!!.setOnCompletionListener {

                        }
                        params!!.pitch = pitch
                        mediaPlayer!!.playbackParams = params!!
                        mediaPlayer!!.start()
                    }

                }
            } catch (e: Exception) {

            }

        }
    }
}