package com.github.hanlyjiang.eink

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import androidx.databinding.DataBindingUtil
import com.github.hanlyjiang.eink.R
import com.github.hanlyjiang.eink.data.BindData
import com.github.hanlyjiang.eink.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    private val bindData: BindData = BindData()

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainBinding.data = bindData
        postFrame()
    }

    private fun postFrame() {
        Choreographer.getInstance().postFrameCallback {
            Log.d(TAG, "postFrameCallback call")
            bindData.timeStr.set(getTimeStr())
            postFrame()
        }
    }

    private fun getTimeStr(): String {
        return SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Date())
    }

}