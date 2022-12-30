package com.github.hanlyjiang.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.github.hanlyjiang.app.databinding.ActivityMainBinding
import com.github.hanlyjiang.app.model.Ym
import com.github.hanlyjiang.app.vm.main.ListViewBean

class MainActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        dataBinding.listVm = listViewBean()
    }

    private fun listViewBean():ListViewBean {
        return ListViewBean().apply {
            ymList.add(Ym("1", "YM111", "YM Desc 11111", "", 1.0f))
            ymList.add(Ym("2", "YM222", "YM Desc 22222", "", 1.0f))
        }
    }
}