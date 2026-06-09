/*
 * Copyright (c) 2024 Sentry Project
 * All Rights Reserved.
 */
package com.sentry.filemanager.filejob

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.sentry.filemanager.R
import com.sentry.filemanager.app.AppActivity

class FileJobProgressActivity : AppActivity() {

    private lateinit var titleText: TextView
    private lateinit var percentText: TextView
    private lateinit var currentFileText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var transferredSizeText: TextView
    private lateinit var totalSizeText: TextView
    private lateinit var toggleLogText: TextView
    private lateinit var logList: ListView
    private lateinit var cancelButton: Button
    private lateinit var hideButton: Button

    private val logAdapter by lazy {
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mutableListOf())
    }

    private var jobId: Int = -1
    private var logExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the activity float above whatever is open
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        setContentView(R.layout.activity_file_job_progress)

        titleText = findViewById(R.id.titleText)
        percentText = findViewById(R.id.percentText)
        currentFileText = findViewById(R.id.currentFileText)
        progressBar = findViewById(R.id.progressBar)
        transferredSizeText = findViewById(R.id.transferredSizeText)
        totalSizeText = findViewById(R.id.totalSizeText)
        toggleLogText = findViewById(R.id.toggleLogText)
        logList = findViewById(R.id.logList)
        cancelButton = findViewById(R.id.cancelButton)
        hideButton = findViewById(R.id.hideButton)

        logList.adapter = logAdapter

        toggleLogText.setOnClickListener {
            logExpanded = !logExpanded
            logList.visibility = if (logExpanded) android.view.View.VISIBLE else android.view.View.GONE
            toggleLogText.text = if (logExpanded) "▼ Hide details" else "▶ Show details"
        }

        hideButton.setOnClickListener {
            FileJobProgressActivity.userDismissed = true
            finish()
        }

        cancelButton.setOnClickListener {
            if (jobId != -1) {
                FileJobService.cancelJob(jobId)
            }
            finish()
        }

        // Dismiss automatically when job finishes
        FileJobProgressLiveData.observe(this) { event ->
            if (event == null) {
                finish()
                return@observe
            }
            jobId = event.jobId
            updateUi(event)
            if (event.isFinished) {
                finish()
            }
        }
    }

    private fun updateUi(event: FileJobProgressEvent) {
        titleText.text = event.title
        percentText.text = "${event.percent}%"
        currentFileText.text = event.currentFile
        progressBar.progress = event.percent

        transferredSizeText.text = formatSize(event.transferredSize)
        totalSizeText.text = "/ ${formatSize(event.totalSize)}"

        // Update log
        logAdapter.clear()
        logAdapter.addAll(event.logLines)
        logAdapter.notifyDataSetChanged()
        if (logExpanded && logAdapter.count > 0) {
            logList.setSelection(logAdapter.count - 1)
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576L     -> "%.1f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024L         -> "%.1f KB".format(bytes / 1_024.0)
            else                    -> "$bytes B"
        }
    }

    companion object {
        var userDismissed = false

        fun start(context: Context) {
            if (userDismissed) return
            val intent = Intent(context, FileJobProgressActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }
}
