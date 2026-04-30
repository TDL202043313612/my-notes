package hf.inner.notebook.page.input

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hf.inner.notebook.bean.Attachment
import hf.inner.notebook.db.repo.TagNoteRepo
import hf.inner.notebook.utils.AudioPlayer
import hf.inner.notebook.utils.AudioRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MemoInputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tagNoteRepo: TagNoteRepo,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    private var currentOutputFile: File? = null
    var currentlyPlayingPath by mutableStateOf<String?>(null)

    var uploadAttachments = mutableStateListOf<Attachment>()
    var currentAudioDuration by mutableStateOf(0)
        private set
    var currentAudioProgress by mutableStateOf(0)
        private set
    private var progressJob: Job? = null

    fun deleteResource(path: String) {
        // 如果删除了正在播放的录音，要让它停下来
        if (currentlyPlayingPath == path) {
            audioPlayer.stop()
            currentlyPlayingPath = null
        }

        uploadAttachments.remove(uploadAttachments.firstOrNull{ it.path == path })
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startRecording() {
        currentOutputFile = File(context.getExternalFilesDir(Environment.DIRECTORY_AUDIOBOOKS), "record_${System.currentTimeMillis()}.m4a")
        audioRecorder.start(currentOutputFile!!)
    }

    fun finishRecording() {
        audioRecorder.stop()
        uploadAttachments.add(
            Attachment(
                type = Attachment.Type.AUDIO,
                path = currentOutputFile!!.path,
                description = currentOutputFile!!.name,
                fileName = currentOutputFile!!.name
            )
        )
        currentOutputFile = null
    }

    fun toggleAudio(path: String) {
        if (currentlyPlayingPath == path) {
            audioPlayer.stop()
            currentlyPlayingPath = null
            stopProgressUpdate()
        } else {
            currentlyPlayingPath = path
            audioPlayer.playFile(path) {
                if (currentlyPlayingPath == path) {
                    currentlyPlayingPath = null
                    stopProgressUpdate()
                    currentAudioProgress = 0
                }
            }
            startProgressUpdate()
        }
    }

    fun seekAudio(position: Float) {
        val posInt = position.toInt()
        currentAudioProgress = posInt
        audioPlayer.seekTo(posInt)
    }
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            delay(100)
            currentAudioDuration = audioPlayer.getDuration()
            while (isActive && currentlyPlayingPath != null) {
                currentAudioProgress = audioPlayer.getCurrentPosition()
                delay(50)
            }
        }
    }
    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }
    fun cancelRecording() {
        audioRecorder.stop()
        if (currentOutputFile?.exists() == true) {
            currentOutputFile?.delete()
        }
        currentOutputFile = null
    }

    fun playAudio(path: String) {
        audioPlayer.playFile(path) {

        }
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        cancelRecording()
    }
}