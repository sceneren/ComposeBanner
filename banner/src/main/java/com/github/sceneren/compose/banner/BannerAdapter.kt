package com.github.sceneren.compose.banner

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder

class BannerAdapter<T> : BaseBannerAdapter<T>() {

    interface ItemTypeBuilder {
        fun getItemType(position: Int): Int
    }

    var itemList: List<T> = mutableListOf()
        set(value) {
            if (field == value) return
            val old = field
            field = value
        }

    var itemBuilder: (@Composable (item: T, index: Int) -> Unit)? =
        null

    var itemTypeBuilder: ItemTypeBuilder? = null

    override fun createViewHolder(
        parent: ViewGroup,
        itemView: View?,
        viewType: Int
    ): BaseViewHolder<T> {
        val context = parent.context
        val composeView = ComposeView(context)
        composeView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return ComposeRecyclerViewHolder(composeView)
    }

    override fun bindData(holder: BaseViewHolder<T>?, data: T, position: Int, pageSize: Int) {
        if (holder is ComposeRecyclerViewHolder) {
            holder.composeView.apply {
                tag = holder
                setContent {
                    if (position < itemList.size) {
                        itemBuilder?.invoke(itemList[position], position)
                    }
                }
            }
        }


    }


    override fun getLayoutId(viewType: Int): Int {
        return R.layout.banner_item
    }

    inner class ComposeRecyclerViewHolder(val composeView: ComposeView) :
        BaseViewHolder<T>(composeView)

    fun update(
        items: List<T>,
        itemBuilder: @Composable (item: T, index: Int) -> Unit,
        itemTypeBuilder: ItemTypeBuilder?
    ) {
        this.itemList = items
        this.itemBuilder = itemBuilder
        itemTypeBuilder?.let {
            this.itemTypeBuilder = it
        }
    }

}