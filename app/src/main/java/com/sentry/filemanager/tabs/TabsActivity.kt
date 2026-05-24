/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.tabs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java8.nio.file.Path
import com.sentry.filemanager.R
import com.sentry.filemanager.app.AppActivity
import com.sentry.filemanager.filelist.FileListFragment
import com.sentry.filemanager.util.createIntent
import com.sentry.filemanager.util.extraPath
import com.sentry.filemanager.util.putArgs

class TabsActivity : AppActivity() {

    private lateinit var tabRecycler: RecyclerView
    private lateinit var tabAdapter: TabAdapter
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var btnAddTab: View

    private val tabs = mutableListOf<TabEntry>()
    private var activeTabIndex = 0

    companion object {
        private const val KEY_TABS = "tabs_state"
        private const val KEY_ACTIVE_INDEX = "active_tab_index"

        fun createIntent(context: Context, path: Path? = null): Intent =
            TabsActivity::class.createIntent().apply {
                if (path != null) extraPath = path
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        tabRecycler = findViewById(R.id.tab_recycler)
        fragmentContainer = findViewById(R.id.tab_fragment_container)
        btnAddTab = findViewById(R.id.btn_add_tab)

        tabAdapter = TabAdapter(
            onTabClick = { switchToTab(it) },
            onTabLongClick = { showTabOptions(it) },
            onTabClose = { closeTab(it) }
        )

        tabRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tabRecycler.adapter = tabAdapter

        btnAddTab.setOnClickListener { addNewTab() }

        if (savedInstanceState != null) {
            val saved = savedInstanceState.getParcelableArrayList<TabEntry>(KEY_TABS)
            if (!saved.isNullOrEmpty()) {
                tabs.addAll(saved)
                activeTabIndex = savedInstanceState.getInt(KEY_ACTIVE_INDEX, 0)
                // Fragments are restored by FragmentManager automatically
                refreshTabStrip()
                showTab(activeTabIndex)
                return
            }
        }

        // Fresh start — create first tab from intent
        val firstTab = TabsManager.createTab(
            label = TabsManager.defaultLabel(0),
            path = intent.extraPath?.toString()
        )
        tabs.add(firstTab)
        val fragment = FileListFragment().putArgs(FileListFragment.Args(intent))
        supportFragmentManager.commit {
            add(R.id.tab_fragment_container, fragment, firstTab.id)
        }
        refreshTabStrip()
        showTab(0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_TABS, ArrayList(tabs))
        outState.putInt(KEY_ACTIVE_INDEX, activeTabIndex)
    }

    // ── Tab Operations ────────────────────────────────────────────────────────

    private fun addNewTab(path: Path? = null) {
        if (tabs.size >= TabsManager.MAX_TABS) {
            android.widget.Toast.makeText(this, "Maximum ${TabsManager.MAX_TABS} tabs reached", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val index = tabs.size
        val tab = TabsManager.createTab(label = TabsManager.defaultLabel(index))
        tabs.add(tab)

        val intent = if (path != null) Intent().apply { extraPath = path } else Intent()
        val fragment = FileListFragment().putArgs(FileListFragment.Args(intent))
        supportFragmentManager.commit {
            add(R.id.tab_fragment_container, fragment, tab.id)
        }

        refreshTabStrip()
        switchToTab(tabs.lastIndex)
    }

    private fun switchToTab(index: Int) {
        if (index < 0 || index >= tabs.size) return
        activeTabIndex = index
        showTab(index)
        tabAdapter.setActiveIndex(index)
        tabRecycler.scrollToPosition(index)
    }

    private fun showTab(index: Int) {
        val activeTag = tabs[index].id
        supportFragmentManager.commit {
            tabs.forEachIndexed { i, tab ->
                val fragment = supportFragmentManager.findFragmentByTag(tab.id) ?: return@forEachIndexed
                if (i == index) show(fragment) else hide(fragment)
            }
        }
    }

    private fun closeTab(index: Int) {
        if (tabs.size <= 1) {
            // Last tab — finish activity
            finish()
            return
        }

        val tab = tabs[index]
        val fragment = supportFragmentManager.findFragmentByTag(tab.id)
        if (fragment != null) {
            supportFragmentManager.commit { remove(fragment) }
        }

        tabs.removeAt(index)

        // Determine new active index
        val newIndex = when {
            activeTabIndex >= tabs.size -> tabs.lastIndex
            activeTabIndex > index -> activeTabIndex - 1
            else -> activeTabIndex
        }.coerceAtLeast(0)

        refreshTabStrip()
        switchToTab(newIndex)
    }

    private fun showTabOptions(index: Int) {
        val tab = tabs[index]
        val options = arrayOf("Rename tab", "Duplicate tab", "Close tab")
        AlertDialog.Builder(this)
            .setTitle(tab.label)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> renameTab(index)
                    1 -> duplicateTab(index)
                    2 -> closeTab(index)
                }
            }
            .show()
    }

    private fun renameTab(index: Int) {
        val tab = tabs[index]
        val input = EditText(this).apply {
            setText(tab.label)
            selectAll()
        }
        AlertDialog.Builder(this)
            .setTitle("Rename tab")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newLabel = input.text.toString().trim()
                if (newLabel.isNotEmpty()) {
                    tabs[index] = tab.copy(label = newLabel)
                    refreshTabStrip()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun duplicateTab(index: Int) {
        if (tabs.size >= TabsManager.MAX_TABS) {
            android.widget.Toast.makeText(this, "Maximum ${TabsManager.MAX_TABS} tabs reached", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val sourcePath = tabs[index].path
        val newTab = TabsManager.createTab(
            label = "${tabs[index].label} (copy)",
            path = sourcePath
        )
        tabs.add(index + 1, newTab)
        val intent = Intent()
        val fragment = FileListFragment().putArgs(FileListFragment.Args(intent))
        supportFragmentManager.commit {
            add(R.id.tab_fragment_container, fragment, newTab.id)
        }
        refreshTabStrip()
        switchToTab(index + 1)
    }

    private fun refreshTabStrip() {
        tabAdapter.submitList(tabs.toList())
        tabAdapter.setActiveIndex(activeTabIndex)
    }

    // ── Back handling ─────────────────────────────────────────────────────────

    override fun onKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        val activeFragment = supportFragmentManager
            .findFragmentByTag(tabs.getOrNull(activeTabIndex)?.id ?: "")
            as? FileListFragment
        if (activeFragment?.onKeyShortcut(keyCode, event) == true) return true
        return super.onKeyShortcut(keyCode, event)
    }
}
