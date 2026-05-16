/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.search

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sentry.filemanager.R
import java.text.SimpleDateFormat
import java.util.*

class AdvancedSearchActivity : AppCompatActivity() {

    private lateinit var etQuery: EditText
    private lateinit var cbRegex: CheckBox
    private lateinit var cbCaseSensitive: CheckBox
    private lateinit var rgScope: RadioGroup
    private lateinit var spinnerType: Spinner
    private lateinit var etMinSize: EditText
    private lateinit var etMaxSize: EditText
    private lateinit var spinnerSizeUnit: Spinner
    private lateinit var tvDateAfter: TextView
    private lateinit var tvDateBefore: TextView
    private lateinit var cbSearchContent: CheckBox
    private lateinit var etContentQuery: EditText

    private var modifiedAfterMs: Long = -1L
    private var modifiedBeforeMs: Long = -1L
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    companion object {
        const val EXTRA_FILTER = "search_filter"
        fun createIntent(context: Context) = Intent(context, AdvancedSearchActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_search)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Advanced Search" }

        etQuery = findViewById(R.id.et_search_query)
        cbRegex = findViewById(R.id.cb_regex)
        cbCaseSensitive = findViewById(R.id.cb_case_sensitive)
        rgScope = findViewById(R.id.rg_scope)
        spinnerType = findViewById(R.id.spinner_file_type)
        etMinSize = findViewById(R.id.et_min_size)
        etMaxSize = findViewById(R.id.et_max_size)
        spinnerSizeUnit = findViewById(R.id.spinner_size_unit)
        tvDateAfter = findViewById(R.id.tv_date_after)
        tvDateBefore = findViewById(R.id.tv_date_before)
        cbSearchContent = findViewById(R.id.cb_search_content)
        etContentQuery = findViewById(R.id.et_content_query)

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("All files","Files only","Folders only","Images","Video","Audio","Documents","Archives"))
        spinnerSizeUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("KB","MB","GB"))
        spinnerSizeUnit.setSelection(1)

        cbSearchContent.setOnCheckedChangeListener { _, checked ->
            etContentQuery.visibility = if (checked) View.VISIBLE else View.GONE
        }

        tvDateAfter.setOnClickListener { pickDate(true) }
        tvDateBefore.setOnClickListener { pickDate(false) }
        findViewById<View>(R.id.btn_clear_date_after).setOnClickListener {
            modifiedAfterMs = -1L; tvDateAfter.text = "Any" }
        findViewById<View>(R.id.btn_clear_date_before).setOnClickListener {
            modifiedBeforeMs = -1L; tvDateBefore.text = "Any" }

        findViewById<View>(R.id.btn_search).setOnClickListener {
            val f = buildFilter()
            if (!f.isActive()) { Toast.makeText(this, "Enter a search query", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            setResult(RESULT_OK, Intent().putExtra(EXTRA_FILTER, f))
            finish()
        }
        findViewById<View>(R.id.btn_clear_filters).setOnClickListener { clearFilters() }
        findViewById<View>(R.id.btn_save_search).setOnClickListener {
            val f = buildFilter()
            if (!f.isActive()) { Toast.makeText(this, "Enter a query to save", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            promptSaveName(f)
        }
        findViewById<View>(R.id.btn_saved_searches).setOnClickListener { showSavedSearches() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun pickDate(isAfter: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, if (isAfter) 0 else 23, if (isAfter) 0 else 59, 59)
            if (isAfter) { modifiedAfterMs = cal.timeInMillis; tvDateAfter.text = dateFormat.format(cal.time) }
            else { modifiedBeforeMs = cal.timeInMillis; tvDateBefore.text = dateFormat.format(cal.time) }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun buildFilter(): SearchFilter {
        val mult = when (spinnerSizeUnit.selectedItemPosition) { 0 -> 1024L; 1 -> 1024L*1024L; else -> 1024L*1024L*1024L }
        val scope = when (rgScope.checkedRadioButtonId) {
            R.id.rb_scope_current -> SearchScope.CURRENT_ONLY
            R.id.rb_scope_storage -> SearchScope.ENTIRE_STORAGE
            else -> SearchScope.CURRENT_AND_SUBDIRS
        }
        return SearchFilter(
            query = etQuery.text.toString().trim(),
            useRegex = cbRegex.isChecked,
            caseSensitive = cbCaseSensitive.isChecked,
            scope = scope,
            fileTypeFilter = FileTypeFilter.values().getOrElse(spinnerType.selectedItemPosition) { FileTypeFilter.ALL },
            minSizeBytes = etMinSize.text.toString().toLongOrNull()?.times(mult) ?: -1L,
            maxSizeBytes = etMaxSize.text.toString().toLongOrNull()?.times(mult) ?: -1L,
            modifiedAfterMs = modifiedAfterMs,
            modifiedBeforeMs = modifiedBeforeMs,
            searchContent = cbSearchContent.isChecked,
            contentQuery = etContentQuery.text.toString().trim()
        )
    }

    private fun clearFilters() {
        etQuery.setText(""); cbRegex.isChecked = false; cbCaseSensitive.isChecked = false
        rgScope.check(R.id.rb_scope_subdirs); spinnerType.setSelection(0)
        etMinSize.setText(""); etMaxSize.setText(""); spinnerSizeUnit.setSelection(1)
        modifiedAfterMs = -1L; modifiedBeforeMs = -1L
        tvDateAfter.text = "Any"; tvDateBefore.text = "Any"
        cbSearchContent.isChecked = false; etContentQuery.setText("")
    }

    private fun promptSaveName(filter: SearchFilter) {
        val input = EditText(this).apply { hint = "Search name" }
        AlertDialog.Builder(this).setTitle("Save search").setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    SavedSearchManager.saveSearch(this, filter.copy(savedName = name))
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showSavedSearches() {
        val saved = SavedSearchManager.getSavedSearches(this)
        if (saved.isEmpty()) { Toast.makeText(this, "No saved searches", Toast.LENGTH_SHORT).show(); return }
        AlertDialog.Builder(this).setTitle("Saved searches")
            .setItems(saved.map { it.savedName }.toTypedArray()) { _, i -> applyFilter(saved[i]) }
            .setNeutralButton("Delete") { _, _ ->
                AlertDialog.Builder(this).setTitle("Delete saved search")
                    .setItems(saved.map { it.savedName }.toTypedArray()) { _, i ->
                        SavedSearchManager.deleteSearch(this, saved[i].savedName)
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    }.show()
            }.show()
    }

    private fun applyFilter(f: SearchFilter) {
        etQuery.setText(f.query); cbRegex.isChecked = f.useRegex; cbCaseSensitive.isChecked = f.caseSensitive
        spinnerType.setSelection(f.fileTypeFilter.ordinal)
        modifiedAfterMs = f.modifiedAfterMs; modifiedBeforeMs = f.modifiedBeforeMs
        tvDateAfter.text = if (modifiedAfterMs >= 0) dateFormat.format(Date(modifiedAfterMs)) else "Any"
        tvDateBefore.text = if (modifiedBeforeMs >= 0) dateFormat.format(Date(modifiedBeforeMs)) else "Any"
        cbSearchContent.isChecked = f.searchContent; etContentQuery.setText(f.contentQuery)
    }
}
