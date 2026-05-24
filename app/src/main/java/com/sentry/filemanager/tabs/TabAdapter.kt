/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R

class TabAdapter(
    private val onTabClick: (Int) -> Unit,
    private val onTabLongClick: (Int) -> Unit,
    private val onTabClose: (Int) -> Unit
) : ListAdapter<TabEntry, TabAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var activeIndex: Int = 0

    fun setActiveIndex(index: Int) {
        val old = activeIndex
        activeIndex = index
        notifyItemChanged(old)
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == activeIndex)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val label: TextView = itemView.findViewById(R.id.tab_label)
        private val closeBtn: View = itemView.findViewById(R.id.tab_close)
        private val indicator: View = itemView.findViewById(R.id.tab_active_indicator)

        fun bind(entry: TabEntry, isActive: Boolean) {
            label.text = entry.label
            indicator.visibility = if (isActive) View.VISIBLE else View.INVISIBLE
            itemView.isSelected = isActive

            itemView.setOnClickListener { onTabClick(bindingAdapterPosition) }
            itemView.setOnLongClickListener {
                onTabLongClick(bindingAdapterPosition)
                true
            }
            closeBtn.setOnClickListener { onTabClose(bindingAdapterPosition) }
            // Hide close button if only one tab
            closeBtn.visibility = if (currentList.size > 1) View.VISIBLE else View.INVISIBLE
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TabEntry>() {
            override fun areItemsTheSame(a: TabEntry, b: TabEntry) = a.id == b.id
            override fun areContentsTheSame(a: TabEntry, b: TabEntry) = a == b
        }
    }
}
