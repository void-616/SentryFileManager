/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import com.sentry.filemanager.app.AppActivity
import com.sentry.filemanager.util.args
import com.sentry.filemanager.util.putArgs

class EditDocumentTreeDialogActivity : AppActivity() {
    private val args by args<EditDocumentTreeDialogFragment.Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = EditDocumentTreeDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, EditDocumentTreeDialogFragment::class.java.name)
            }
        }
    }
}
