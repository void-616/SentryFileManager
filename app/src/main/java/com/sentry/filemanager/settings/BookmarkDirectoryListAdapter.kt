/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.settings

import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import com.sentry.filemanager.compat.foregroundCompat
import com.sentry.filemanager.compat.isTransformedTouchPointInViewCompat
import com.sentry.filemanager.databinding.BookmarkDirectoryItemBinding
import com.sentry.filemanager.filelist.toUserFriendlyString
import com.sentry.filemanager.navigation.BookmarkDirectory
import com.sentry.filemanager.ui.SimpleAdapter
import com.sentry.filemanager.util.layoutInflater

class BookmarkDirectoryListAdapter(
    private val listener: Listener
) : SimpleAdapter<BookmarkDirectory, BookmarkDirectoryListAdapter.ViewHolder>(),
    DraggableItemAdapter<BookmarkDirectoryListAdapter.ViewHolder> {
    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            BookmarkDirectoryItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkDirectory = getItem(position)
        val binding = holder.binding
        // Need to remove the ripple before it's drawn onto the bitmap for dragging.
        binding.root.foregroundCompat!!.mutate().setVisible(!holder.dragState.isActive, false)
        binding.root.setOnClickListener { listener.editBookmarkDirectory(bookmarkDirectory) }
        binding.nameText.text = bookmarkDirectory.name
        binding.pathText.text = bookmarkDirectory.path.toUserFriendlyString()
    }

    override fun onCheckCanStartDrag(holder: ViewHolder, position: Int, x: Int, y: Int): Boolean =
        (holder.binding.root as ViewGroup).isTransformedTouchPointInViewCompat(
            x.toFloat(), y.toFloat(), holder.binding.dragHandleView, null
        )

    override fun onGetItemDraggableRange(holder: ViewHolder, position: Int): ItemDraggableRange? =
        null

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean = true

    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) {
            return
        }
        listener.moveBookmarkDirectory(fromPosition, toPosition)
    }

    class ViewHolder(val binding: BookmarkDirectoryItemBinding) : AbstractDraggableItemViewHolder(
        binding.root
    )

    interface Listener {
        fun editBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
        fun moveBookmarkDirectory(fromPosition: Int, toPosition: Int)
    }
}
