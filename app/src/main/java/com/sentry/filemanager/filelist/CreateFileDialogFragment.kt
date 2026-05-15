/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.sentry.filemanager.R
import com.sentry.filemanager.util.show

class CreateFileDialogFragment : FileNameDialogFragment() {
    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_create_file_title

    override fun onOk(name: String) {
        listener.createFile(name)
    }

    companion object {
        fun show(fragment: Fragment) {
            CreateFileDialogFragment().show(fragment)
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun createFile(name: String)
    }
}
