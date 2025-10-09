package com.audio.example.ui.my_record

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.audio.example.R
import com.audio.example.core.base.BaseActivity
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MyRecordActivity : BaseActivity<ActivityMyRecordBinding>() {
    lateinit var mediaPlayer: MediaPlayer
    var arrPath = arrayListOf<String>()
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
                        saveAudioToDownload(
                            applicationContext,
                            arrPath[pos],
                            File(arrPath[pos]).name
                        )
                        showToast(R.string.download_success, Toast.LENGTH_SHORT)
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
}