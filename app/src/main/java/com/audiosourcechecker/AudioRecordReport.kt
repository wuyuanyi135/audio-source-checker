package com.audiosourcechecker

import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.util.ArrayList
import kotlin.reflect.KClass
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect

fun calculateSD(numArray: ByteArray): Double {
    var sum = 0.0
    var standardDeviation = 0.0
    for (num in numArray) {
        sum += num
    }
    val mean = sum / 10
    for (num in numArray) {
        standardDeviation += Math.pow(num - mean, 2.0)
    }
    return Math.sqrt(standardDeviation / 10)
}

class AudioRecordReport {

    class MicrophoneInfo {
        var id: Int = 0
        var description: String = ""
        var type: Int = -1
        var mean: Double = 0.0
        var std: Double = 0.0

        override fun toString(): String {
            val audioDeviceInfoMapping = createIntPropertyMapping<AudioDeviceInfo>()
            return "id=${id}; description=${description}; type=${audioDeviceInfoMapping[type]}; mean=${mean}; std=${std}\n"
        }
    }

    val audioSources = listOf(
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.CAMCORDER,
        MediaRecorder.AudioSource.DEFAULT,
//        MediaRecorder.AudioSource.REMOTE_SUBMIX,
//        MediaRecorder.AudioSource.UNPROCESSED,
        MediaRecorder.AudioSource.VOICE_CALL,
        MediaRecorder.AudioSource.VOICE_COMMUNICATION
//        MediaRecorder.AudioSource.VOICE_DOWNLINK,
//        MediaRecorder.AudioSource.VOICE_RECOGNITION,
//        MediaRecorder.AudioSource.VOICE_UPLINK,
    )

    val channelConfigs = listOf(
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.CHANNEL_IN_STEREO
    )

    fun getMicrophoneInfo(audioSource: Int, channelConfig: Int): ArrayList<MicrophoneInfo> {
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            44100,
            channelConfig, audioFormat
        ) * 2
        val audioRecord = AudioRecord(
            audioSource,
            44100,
            channelConfig,
            audioFormat,
            BUFFER_SIZE
        )
        val activeMicrophones = audioRecord.activeMicrophones

        val mics = ArrayList<MicrophoneInfo>()

        for (mic in activeMicrophones) {
            audioRecord.startRecording()

            val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
            val audioRecordMapping = createIntPropertyMapping<AudioRecord>()
            val result = audioRecord.read(buffer, BUFFER_SIZE)
            if (result < 0) {
                throw RuntimeException("Reading of audio buffer failed: ${audioRecordMapping[result]}")
            }

            val array = buffer.array()
            val mean = array.average()
            val std = calculateSD(array)
            audioRecord.stop()

            val element = MicrophoneInfo().apply {
                id = mic.id
                description = mic.description
                type = mic.type
                this.mean = mean
                this.std = std
            }
            mics.add(element)
        }

        return mics
    }

    fun generateReport(): String {
        val ret = StringBuffer()
        ret.append("===== Microphone info =====\n")
        for (audioSource in audioSources) {
            for (channelConfig in channelConfigs) {
                try {
                    val microphoneInfo = getMicrophoneInfo(audioSource, channelConfig)
                    for (mic in microphoneInfo) {
                        ret.append(mic.toString())
                        ret.append("\n")
                    }
                } catch (e: Exception) {
                    Log.e("AudioRecord", "Exception", e)
                }

            }
        }
        ret.append("\n\n")

        return ret.toString()
    }
}