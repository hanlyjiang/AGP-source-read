package com.github.hanlyjiang.app.vm.main

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.hanlyjiang.app.model.Ym
import com.github.hanlyjiang.app.vm.bindingadapter.ListBindAdapter

/**
 * ListItemViewHolder
 * @author jiang.hanghang 2022/12/30 14:33
 * @version 1.0
 *
 */
class ListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text1: TextView
    val text2: TextView

    init {
        text1 = itemView.findViewById(android.R.id.text1)
        text2 = itemView.findViewById(android.R.id.text2)
    }
}

/**
 * ListItemAdapter
 * @author jiang.hanghang 2022/12/30 14:33
 * @version 1.0
 *
 */
class ListItemAdapter(data: List<Ym>) : RecyclerView.Adapter<ListItemViewHolder>() {

    companion object {
        val TAG = ListItemAdapter::class.simpleName
    }

    private var listData: MutableList<Ym> = ArrayList()

    init {
        listData = ArrayList()
        listData.addAll(data)
    }

    /**
     * 更新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(data: List<Ym>) {
        Log.d(TAG, "updateData")
        listData.clear()
        listData.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ListItemViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        val item = listData[position]
        holder.text1.text = item.title
        holder.text2.text = item.desc
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}