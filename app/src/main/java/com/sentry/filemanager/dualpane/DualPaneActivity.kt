/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.dualpane

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.commit
import java8.nio.file.Path
import com.sentry.filemanager.R
import com.sentry.filemanager.app.AppActivity
import com.sentry.filemanager.filelist.FileListFragment
import com.sentry.filemanager.util.createIntent
import com.sentry.filemanager.util.extraPath
import com.sentry.filemanager.util.putArgs
import android.view.DragEvent
import androidx.appcompat.app.AlertDialog

class DualPaneActivity : AppActivity() {

    private lateinit var dualPaneLayout: DualPaneLayout
    private lateinit var pane1Container: FrameLayout
    private lateinit var pane2Container: FrameLayout
    private lateinit var dividerView: View

    private lateinit var pane1Fragment: FileListFragment
    private lateinit var pane2Fragment: FileListFragment

    // Which pane is currently active (receives keyboard shortcuts etc.)
    private var activePane: Int = 1


    private fun setupCrossPaneDrag() {
        pane2Container.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> { pane2Container.setBackgroundColor(0x331A73E8.toInt()); true }
                DragEvent.ACTION_DRAG_EXITED -> { updateActivePaneIndicator(); true }
                DragEvent.ACTION_DROP -> { updateActivePaneIndicator(); showCopyMoveDialog(fromPane = 1); true }
                DragEvent.ACTION_DRAG_ENDED -> { updateActivePaneIndicator(); true }
                else -> true
            }
        }
        pane1Container.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> { pane1Container.setBackgroundColor(0x331A73E8.toInt()); true }
                DragEvent.ACTION_DRAG_EXITED -> { updateActivePaneIndicator(); true }
                DragEvent.ACTION_DROP -> { updateActivePaneIndicator(); showCopyMoveDialog(fromPane = 2); true }
                DragEvent.ACTION_DRAG_ENDED -> { updateActivePaneIndicator(); true }
                else -> true
            }
        }
    }

    fun showCopyMoveDialog(fromPane: Int) {
        val sourceFragment = if (fromPane == 1) pane1Fragment else pane2Fragment
        val targetFragment = if (fromPane == 1) pane2Fragment else pane1Fragment
        val pasteState = sourceFragment.getDualPanePasteState()
        if (pasteState.files.isEmpty()) {
            android.widget.Toast.makeText(this, "Select files first then drag", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val targetPath = targetFragment.getDualPaneCurrentPath() ?: return
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Move or copy?")
            .setMessage("${pasteState.files.size} item(s) to ${targetPath.toFile().name}")
            .setPositiveButton("Move") { _, _ ->
                sourceFragment.addToDualPanePasteState(false)
                targetFragment.pasteFilesTo(targetPath)
            }
            .setNegativeButton("Copy") { _, _ ->
                sourceFragment.addToDualPanePasteState(true)
                targetFragment.pasteFilesTo(targetPath)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    companion object {
        private const val PANE1_TAG = "dual_pane_1"
        private const val PANE2_TAG = "dual_pane_2"
        private const val KEY_ACTIVE_PANE = "active_pane"

        fun createIntent(context: Context, path: Path? = null): Intent =
            DualPaneActivity::class.createIntent().apply {
                if (path != null) extraPath = path
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dual_pane)

        dualPaneLayout = findViewById(R.id.dual_pane_layout)
        pane1Container = findViewById(R.id.pane1_container)
        pane2Container = findViewById(R.id.pane2_container)
        dividerView = findViewById(R.id.pane_divider)

        // Restore split ratio
        dualPaneLayout.splitRatio = DualPaneManager.getSplitRatio(this)

        if (savedInstanceState == null) {
            // Fresh start — use intent path for pane 1, home for pane 2
            pane1Fragment = FileListFragment().putArgs(FileListFragment.Args(intent))
            pane2Fragment = FileListFragment().putArgs(FileListFragment.Args(Intent()))

            supportFragmentManager.commit {
                add(R.id.pane1_container, pane1Fragment, PANE1_TAG)
                add(R.id.pane2_container, pane2Fragment, PANE2_TAG)
            }
        } else {
            pane1Fragment = supportFragmentManager.findFragmentByTag(PANE1_TAG) as FileListFragment
            pane2Fragment = supportFragmentManager.findFragmentByTag(PANE2_TAG) as FileListFragment
            activePane = savedInstanceState.getInt(KEY_ACTIVE_PANE, 1)
        }

        pane1Container.setOnClickListener { setActivePane(1) }
        pane2Container.setOnClickListener { setActivePane(2) }

        updateActivePaneIndicator()
        setupCrossPaneDrag()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_ACTIVE_PANE, activePane)
        DualPaneManager.saveSplitRatio(this, dualPaneLayout.splitRatio)
    }

    override fun onStop() {
        super.onStop()
        DualPaneManager.saveSplitRatio(this, dualPaneLayout.splitRatio)
    }

    fun setActivePane(pane: Int) {
        activePane = pane
        updateActivePaneIndicator()
    }

    fun getActiveFragment(): FileListFragment =
        if (activePane == 1) pane1Fragment else pane2Fragment

    fun getInactiveFragment(): FileListFragment =
        if (activePane == 1) pane2Fragment else pane1Fragment

    private fun updateActivePaneIndicator() {
        val activeColor = getColor(R.color.dual_pane_active_border)
        val inactiveColor = android.graphics.Color.TRANSPARENT

        pane1Container.setBackgroundColor(if (activePane == 1) activeColor else inactiveColor)
        pane2Container.setBackgroundColor(if (activePane == 2) activeColor else inactiveColor)
    }

}
