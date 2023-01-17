package com.github.hanlyjiang.app.recyclerview.sticky

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.hanlyjiang.app.BR
import com.github.hanlyjiang.app.R
import com.github.hanlyjiang.app.databinding.ActivityStickyItemDecorationBinding
import com.github.hanlyjiang.app.recyclerview.sticky.impl1.StickyItemDecoration

/**
 * Test recycler item decoration
 */
class StickyItemDecorationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityStickyItemDecorationBinding>(
            this,
            R.layout.activity_sticky_item_decoration
        ).apply {
            initRecyclerView(this)
        }
    }

    private fun initRecyclerView(binding: ActivityStickyItemDecorationBinding) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SectionAdapter(makeTestData())
//            addItemDecoration(SectionItemDecoration { _, viewPosition ->
            addItemDecoration(StickyItemDecoration { _, viewPosition ->
                (adapter as SectionAdapter).getItemViewType(
                    viewPosition
                ) == SectionAdapter.ViewType_Group
            })
        }
    }

    private fun makeTestData(): List<DataItem> {
        return MutableList(100) {
            DataItem(
                if (it % 13 == 0) SectionAdapter.ViewType_Group else SectionAdapter.ViewType_Child,
                if (it % 13 == 0) "年 - 202${it / 13 + 2}" else "月 - ${it % 13}"
            )
        }
    }
}

data class DataItem(val type: Int, val value: String)

class SectionViewHolder<out T : ViewDataBinding>(protected val itemViewBinding: T) :
    RecyclerView.ViewHolder(itemViewBinding.root) {

    fun bind(data: DataItem) {
        itemViewBinding.setVariable(BR.data, data)
        itemViewBinding.executePendingBindings()
    }

}

class SectionAdapter(private val dataItem: List<DataItem>) :
    RecyclerView.Adapter<SectionViewHolder<ViewDataBinding>>() {

    companion object {
        const val ViewType_Group = 1
        const val ViewType_Child = 2
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SectionViewHolder<ViewDataBinding> {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            if (viewType == ViewType_Group) R.layout.item_rv_group else R.layout.item_tv_child,
            parent,
            false
        )
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SectionViewHolder<ViewDataBinding>, position: Int) {
        holder.bind(dataItem[position])
    }

    override fun getItemViewType(position: Int): Int {
        return dataItem[position].type
    }

    override fun getItemCount(): Int {
        return dataItem.size
    }
}