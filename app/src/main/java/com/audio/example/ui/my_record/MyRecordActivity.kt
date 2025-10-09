package com.audio.example.ui.my_record

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
import com.audio.example.core.extensions.hideNavigation
import com.audio.example.core.extensions.setOnSingleClick
import com.audio.example.core.extensions.shareImagesPaths
import com.audio.example.core.extensions.showToast
import com.audio.example.core.helper.MediaHelper.getFileInternal
import com.audio.example.core.helper.MediaHelper.saveAudioToDownload
import com.audio.example.core.utils.key.ValueKey.RECORD_ALBUM
import com.audio.example.core.utils.key.ValueKey.RECORD_REVERSE_ALBUM
import com.audio.example.databinding.ActivityMyRecordBinding
import com.audio.example.dialog.DeleteDialog
import com.audio.example.dialog.DialogRename
import com.audio.example.ui.my_record.MyRecordAdapter.Companion.TYPE_DELETE
import com.audio.example.ui.my_record.MyRecordAdapter.Companion.TYPE_DOWNLOAD
import com.audio.example.ui.my_record.MyRecordAdapter.Companion.TYPE_PLAY
import com.audio.example.ui.my_record.MyRecordAdapter.Companion.TYPE_RENAME
import com.audio.example.ui.my_record.MyRecordAdapter.Companion.TYPE_REVERSE
import com.audio.example.ui.my_record.MyRecordAdapter.Companion.TYPE_SHARE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MyRecordActivity : BaseActivity<ActivityMyRecordBinding>() {
    lateinit var mediaPlayer: MediaPlayer
    var arrPath = arrayListOf<String>()
    var _pos = -1
    lateinit var params: PlaybackParams
    val adapter by lazy {
        MyRecordAdapter()
    }

    override fun setViewBinding(): ActivityMyRecordBinding {
        return ActivityMyRecordBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.rcv.itemAnimator = null
        binding.rcv.adapter = adapter
        params = PlaybackParams()
        lifecycleScope.launch(Dispatchers.IO) {
            getData()
            withContext(Dispatchers.Main) {
                checkNull()
            }
        }
    }

    fun checkNull() {
        if (arrPath.size == 0) {
            binding.llNull.visibility = View.VISIBLE
        } else {
            binding.llNull.visibility = View.GONE
        }
        adapter.submitList(arrPath)
    }

    override fun viewListener() {
        binding.apply {
            llLoading.setOnSingleClick{
                showToast(R.string.please_wait_until_the_download_is_successful, Toast.LENGTH_SHORT)
            }
            imvBack.setOnSingleClick { finish() }
            adapter.onClick = { pos, type ->
                when (type) {
                    TYPE_PLAY -> {
                        adapter.checkPlay = 0
                        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                            stopMedia()
                        } else {
                            startMedia(arrPath[pos])
                        }
                    }

                    TYPE_REVERSE -> {
                        adapter.checkPlay = 1
                        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                            stopMedia()
                        } else {
                            startMedia(arrPath[pos].replace(RECORD_ALBUM, RECORD_REVERSE_ALBUM))
                        }
                    }

                    TYPE_SHARE -> {
                        shareImagesPaths(arrayListOf(arrPath[pos]))
                    }

                    TYPE_RENAME -> {
                        var dialog = DialogRename(this@MyRecordActivity, arrPath[pos])
                        dialog.onClick = {name->
                            File(arrPath[pos]).renameTo(File("$name.wav"))
                            if(File("$name.wav").exists()){
                                showToast(R.string.rename_successful, Toast.LENGTH_SHORT)
                                arrPath[pos] = "$name.wav"
                                adapter.submitList(arrPath)
                            }else{
                                showToast(R.string.rename_failed, Toast.LENGTH_SHORT)
                            }
                        }
                        dialog.show()
                    }

                    TYPE_DOWNLOAD -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                            ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            _pos = pos
                            ActivityCompat.requestPermissions(
                                this@MyRecordActivity,
                                arrayOf(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                ),
                                1111
                            )
                        } else {
                            saveFile(pos)
                        }
                    }

                    TYPE_DELETE -> {
                        var dialog = DeleteDialog(this@MyRecordActivity)
                        dialog.onClick = {
                            File(arrPath[pos]).delete()
                            checkNull()
                        }
                        dialog.show()
                    }
                }
            }
        }
    }

    fun stopMedia() {
        mediaPlayer.pause()
        mediaPlayer.stop()
        mediaPlayer.release()
        adapter.checkForcus = -1
        adapter.submitList(arrPath)
    }

    fun startMedia(path: String) {
        mediaPlayer = MediaPlayer.create(
            applicationContext,
            path.toUri()
        )
        mediaPlayer.setOnCompletionListener {
            adapter.checkForcus = -1
            adapter.submitList(arrPath)
        }
        params.pitch = sharePreference.getPitchShiftAudio()
        mediaPlayer.playbackParams = params
        mediaPlayer.start()
    }

    override fun onStop() {
        super.onStop()
        stopMedia()
    }

    override fun initActionBar() {

    }

    fun getData() {
        arrPath.clear()
        arrPath.addAll(getFileInternal(applicationContext, RECORD_ALBUM, true))
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1111 -> {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (_pos != -1) {
                        saveFile(_pos)
                    }

                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        showDialogNotifiListener(R.string.reques_storage)
                    }

                }
            }
        }
    }

    fun saveFile(pos: Int) {
        binding.llLoading.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            saveAudioToDownload(
                applicationContext,
                arrPath[pos],
                File(arrPath[pos]).name
            )
            withContext(Dispatchers.Main) {
                delay(1000)
                binding.llLoading.visibility = View.GONE
                showToast(R.string.download_success, Toast.LENGTH_SHORT)
            }
        }

    }

    fun showDialogNotifiListener(i: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.permission)
            .setMessage(i)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                hideNavigation()
            }
            .setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
        hideNavigation()
        val negativeButton =
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val negativeButton2 =
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        negativeButton2?.setTextColor(resources.getColor(R.color.color_app))
        negativeButton?.setTextColor(resources.getColor(R.color._555555))
    }
}