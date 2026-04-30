package hf.inner.notebook.utils

import android.media.MediaPlayer
import javax.inject.Inject

class AudioPlayer @Inject constructor() {
    private var mediaPlayer: MediaPlayer? = null

    fun playFile(path: String, onCompletion: () -> Unit = {}) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
                start()
                // 监听播放完成事件
                setOnCompletionListener {
                    stop()
                    onCompletion()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onCompletion()
            }
        }
    }

    /**
     * 获取总时长（毫秒）
     */
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    /**
     * 获取当前播放进度
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    /**
     * 进度跳转
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release() // 🌟 极其重要：播放器极其消耗底层资源，用完必须 release
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }
}