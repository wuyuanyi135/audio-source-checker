package com.audiosourcechecker

import android.content.Context
import android.R.string.no
import android.media.*
import java.nio.ByteBuffer


fun generateBluetoothAudioReport(ctx: Context): String {
    val SAMPLING_RATE_IN_HZ = 44100
    val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLING_RATE_IN_HZ,
        CHANNEL_CONFIG, AUDIO_FORMAT
    ) * 2

    val report = StringBuffer()
    report.append("===== Bluetooth Report ======\n")
    val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    if (!audioManager.isBluetoothScoAvailableOffCall) {
        report.append("SCO ist not available, recording is not possible\n")
        return report.toString()
    }

    if (!audioManager.isBluetoothScoOn()) {
        audioManager.startBluetoothSco();
    }

    val default = AudioRecord(
        MediaRecorder.AudioSource.DEFAULT,
        SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE
    )
    var activeMicrophones = default.activeMicrophones
    val audioDeviceInfoMapping = createIntPropertyMapping<AudioDeviceInfo>()
    report.append("DEFAULT\n")
    for (mic in activeMicrophones) {
        default.startRecording()

        val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
        val audioRecordMapping = createIntPropertyMapping<AudioRecord>()
        val result = default.read(buffer, BUFFER_SIZE)
        if (result < 0) {
            throw RuntimeException("Reading of audio buffer failed: ${audioRecordMapping[result]}")
        }

        val array = buffer.array()
        val mean = array.average()
        val std = calculateSD(array)
        default.stop()
        report.append("id=${mic.id}; type=${audioDeviceInfoMapping[mic.type]}; mean=${mean}; std=${std}\n")
    }

    val communication = AudioRecord(
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE
    )
    report.append("VOICE_COMMUNICATION\n")
    activeMicrophones = communication.activeMicrophones
    for (mic in activeMicrophones) {
        communication.startRecording()

        val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
        val audioRecordMapping = createIntPropertyMapping<AudioRecord>()
        val result = communication.read(buffer, BUFFER_SIZE)
        if (result < 0) {
            throw RuntimeException("Reading of audio buffer failed: ${audioRecordMapping[result]}")
        }

        val array = buffer.array()
        val mean = array.average()
        val std = calculateSD(array)
        communication.stop()
        report.append("id=${mic.id}; type=${audioDeviceInfoMapping[mic.type]}; mean=${mean}; std=${std}\n")
    }
    return report.toString()
}