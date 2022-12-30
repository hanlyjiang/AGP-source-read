package com.github.hanlyjiang.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.github.hanlyjiang.app.databinding.ActivityMainBinding
import com.github.hanlyjiang.app.model.Ym
import com.github.hanlyjiang.app.vm.main.ListViewBean

class MainActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityMainBinding

    private var count = 1
    private lateinit var listViewBean: ListViewBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        listViewBean = listViewBean()
        dataBinding.listVm = listViewBean
        dataBinding.fabAdd.setOnClickListener {
            listViewBean.ymList.add(randomYm(count++))
        }
    }

    private fun randomYm(int: Int): Ym {
        return Ym("$int", "YM ${int}", "YM Desc $int", "", int.toFloat())
    }

    private fun listViewBean(): ListViewBean {
        return ListViewBean().apply {
//            ymList.add(randomYm(count++))
//            ymList.add(randomYm(count++))
        }
    }
}