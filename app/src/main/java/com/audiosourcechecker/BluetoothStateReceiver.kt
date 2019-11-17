package com.audiosourcechecker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager



val TAG = "Bluetooth"
class BluetoothStateReceiver: BroadcastReceiver {

    private var callback: (Int)->Unit

    constructor(callback: (Int)->Unit) : super() {
        this.callback = callback
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
        callback(state!!)
    }
}