package com.github.hanlyjiang.app.vm.bindingadapter;

import android.content.Context;
import android.util.Log;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.hanlyjiang.app.model.Ym;
import com.github.hanlyjiang.app.vm.main.ListItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ListBindAdapter
 *
 * @author jiang.hanghang 2022/12/30 14:12
 * @version 1.0
 */
public class ListBindAdapter {

    public static final String TAG = ListBindAdapter.class.getSimpleName();

    /**
     * 添加自定义的BindingAdapter
     *
     * @param recyclerView
     * @param oldData
     * @param newData
     */
    @BindingAdapter("android:data")
    public static void setRecyclerViewData(RecyclerView recyclerView, List<Ym> oldData, List<Ym> newData) {
        Log.d(TAG, "setRecyclerViewData：oldData = " + oldData.size() + "; newData = " + newData.size());
        if (recyclerView.getAdapter() == null) {
            initRecyclerView(recyclerView);
        }
        if (recyclerView.getAdapter() instanceof ListItemAdapter) {
            ((ListItemAdapter) recyclerView.getAdapter()).updateData(newData);
        }
    }

    private static void initRecyclerView(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        ListItemAdapter listItemAdapter = new ListItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(listItemAdapter);
    }

}
