package com.github.hanlyjiang.app.vm.main;

import androidx.databinding.ObservableArrayList;

import com.github.hanlyjiang.app.model.Ym;

/**
 * @author jiang.hanghang 2022/12/30 14:20
 * @version 1.0
 */
public class ListViewBean {

    /**
     * ObservableArrayList 支持被观察数据的添加移除等
     */
    public ObservableArrayList<Ym> ymList = new ObservableArrayList<>();

}
