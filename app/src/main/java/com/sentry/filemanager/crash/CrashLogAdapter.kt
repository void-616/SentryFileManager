/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.crash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R

class CrashLogAdapter(
    private val onCopy: (CrashLogEntry) -> Unit,
    private val onShare: (CrashLogEntry) -> Unit,
    private val onDelete: (CrashLogEntry) -> Unit
) : ListAdapter<CrashLogEntry, CrashLogAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crash_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timestamp: TextView = itemView.findViewById(R.id.crash_timestamp)
        private val exception: TextView = itemView.findViewById(R.id.crash_exception)
        private val message: TextView = itemView.findViewById(R.id.crash_message)
        private val stackTrace: TextView = itemView.findViewById(R.id.crash_stacktrace)
        private val btnExpand: TextView = itemView.findViewById(R.id.btn_expand)
        private val btnCopy: View = itemView.findViewById(R.id.btn_copy)
        private val btnShare: View = itemView.findViewById(R.id.btn_share)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete)
        private var expanded = false

        fun bind(entry: CrashLogEntry) {
            timestamp.text = entry.timestamp
            exception.text = entry.exceptionType
            message.text = entry.message
            stackTrace.text = entry.fullText
            stackTrace.visibility = View.GONE
            btnExpand.text = "Show full trace"
            expanded = false

            btnExpand.setOnClickListener {
                expanded = !expanded
                stackTrace.visibility = if (expanded) View.VISIBLE else View.GONE
                btnExpand.text = if (expanded) "Hide" else "Show full trace"
            }

            btnCopy.setOnClickListener { onCopy(entry) }
            btnShare.setOnClickListener { onShare(entry) }
            btnDelete.setOnClickListener { onDelete(entry) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CrashLogEntry>() {
            override fun areItemsTheSame(a: CrashLogEntry, b: CrashLogEntry) = a.fileName == b.fileName
            override fun areContentsTheSame(a: CrashLogEntry, b: CrashLogEntry) = a == b
        }
    }
}
