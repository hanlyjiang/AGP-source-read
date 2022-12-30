package com.github.hanlyjiang.app.vm.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.hanlyjiang.app.model.Ym

/**
 * YmListViewModel
 * @author jiang.hanghang 2022/12/30 14:01
 * @version 1.0
 *
 */
class YmListViewModel : ViewModel() {

    val ymList: LiveData<List<Ym>> = MutableLiveData<List<Ym>>()


}