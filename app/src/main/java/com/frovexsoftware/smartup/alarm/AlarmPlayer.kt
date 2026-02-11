package com.frovexsoftware.smartup.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

/**
 * Simple singleton to play/stop alarm ringtone with fallbacks.
 */
object AlarmPlayer {
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null

    private val alarmAudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    fun start(context: Context) {
        val appContext = context.applicationContext
        if (ringtone?.isPlaying == true || mediaPlayer?.isPlaying == true) return

        val uri = pickAlarmUri()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone = RingtoneManager.getRingtone(appContext, uri)?.apply {
                audioAttributes = alarmAudioAttributes
                isLooping = true
                play()
            }
        } else {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(alarmAudioAttributes)
                    setDataSource(appContext, uri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                mediaPlayer?.release()
                mediaPlayer = null
                ringtone = RingtoneManager.getRingtone(appContext, uri)?.apply {
                    audioAttributes = alarmAudioAttributes
                    play()
                }
            }
        }
    }

    fun stop() {
        ringtone?.stop()
        ringtone = null
        mediaPlayer?.run {
            stop()
            release()
        }
        mediaPlayer = null
    }

    private fun pickAlarmUri(): Uri {
        val alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarm != null) return alarm

        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (notification != null) return notification

        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }
}
