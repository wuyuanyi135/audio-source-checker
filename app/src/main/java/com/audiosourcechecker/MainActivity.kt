package com.audiosourcechecker

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var receiver: BluetoothStateReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView.movementMethod = ScrollingMovementMethod()
        receiveView.movementMethod = ScrollingMovementMethod()
        refreshButton.setOnClickListener {
            val audioRecordReport = AudioRecordReport()
            val micReport = audioRecordReport.generateReport()
            val btReport = micReport.plus(generateBluetoothAudioReport(this))
            textView.text = btReport
        }
        testPageButton.setOnClickListener {
            val intent = Intent(this, AudioTestActivity::class.java).apply {}
            startActivity(intent)
        }

        val audioManagerMapping = createIntPropertyMapping<AudioManager>()
        receiver = BluetoothStateReceiver {
            receiveView.text =audioManagerMapping[it]
        }
        registerReceiver(receiver, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onPause() {
        finish()
        super.onPause()
    }

}
