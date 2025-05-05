package com.github.hanlyjiang.testmode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Choreographer

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        postFrame()
    }

    private fun postFrame() {
        Choreographer.getInstance().postFrameCallback {
            Log.d(TAG, "postFrameCallback call")
            postFrame()
        }
    }

}