/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.terminal

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import com.sentry.filemanager.R

class TerminalFragment : Fragment() {

    private lateinit var terminalView: TerminalView
    private lateinit var tvTitle: TextView
    private var terminalSession: TerminalSession? = null
    private var startDirectory: String = "/"

    companion object {
        private const val ARG_PATH = "start_path"

        fun newInstance(path: String): TerminalFragment {
            return TerminalFragment().apply {
                arguments = Bundle().apply { putString(ARG_PATH, path) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startDirectory = arguments?.getString(ARG_PATH) ?: "/"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_terminal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        terminalView = view.findViewById(R.id.terminal_view)
        tvTitle = view.findViewById(R.id.terminal_title)

        view.findViewById<View>(R.id.btn_terminal_paste).setOnClickListener { pasteFromClipboard() }
        setupKeyBar(view)
        view.findViewById<View>(R.id.btn_terminal_close).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        startSession()
    }

    private fun startSession() {
        val shell = getShellPath()
        val client = SentryTerminalClient(
            onUpdate = { terminalView.onScreenUpdated() },
            onTitleChange = { title -> activity?.runOnUiThread { tvTitle.text = title } }
        )

        terminalSession = TerminalSession(
            shell,
            startDirectory,
            arrayOf(),
            buildEnvironment(),
            2000,
            client
        )

        terminalView.attachSession(terminalSession)
        terminalView.setTerminalViewClient(object : TerminalViewClient {
            override fun onScale(scale: Float): Float = 1f
            override fun onSingleTapUp(e: MotionEvent) {}
            override fun shouldBackButtonBeMappedToEscape(): Boolean = false
            override fun shouldEnforceCharBasedInput(): Boolean = false
            override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
            override fun isTerminalViewSelected(): Boolean = true
            override fun copyModeChanged(copyMode: Boolean) {}
            override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession?): Boolean = false
            override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean = false
            override fun onLongPress(event: MotionEvent): Boolean = false
            override fun readControlKey(): Boolean = false
            override fun readAltKey(): Boolean = false
            override fun readShiftKey(): Boolean = false
            override fun readFnKey(): Boolean = false
            override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean = false
            override fun onEmulatorSet() {}
            override fun logError(tag: String?, message: String?) {}
            override fun logWarn(tag: String?, message: String?) {}
            override fun logInfo(tag: String?, message: String?) {}
            override fun logDebug(tag: String?, message: String?) {}
            override fun logVerbose(tag: String?, message: String?) {}
            override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
            override fun logStackTrace(tag: String?, e: Exception?) {}
        })
    }

    private fun setupKeyBar(view: android.view.View) {
        // ESC
        view.findViewById<android.view.View>(R.id.key_esc).setOnClickListener {
            terminalSession?.write("")
        }
        // TAB
        view.findViewById<android.view.View>(R.id.key_tab).setOnClickListener {
            terminalSession?.write("	")
        }
        // CTRL toggle — sends next char as ctrl combo
        view.findViewById<android.view.View>(R.id.key_ctrl).setOnClickListener {
            terminalSession?.write("") // Ctrl+C as default, will improve later
        }
        // Arrows
        view.findViewById<android.view.View>(R.id.key_arrow_up).setOnClickListener {
            terminalSession?.write("[A")
        }
        view.findViewById<android.view.View>(R.id.key_arrow_down).setOnClickListener {
            terminalSession?.write("[B")
        }
        view.findViewById<android.view.View>(R.id.key_arrow_right).setOnClickListener {
            terminalSession?.write("[C")
        }
        view.findViewById<android.view.View>(R.id.key_arrow_left).setOnClickListener {
            terminalSession?.write("[D")
        }
        // Home / End
        view.findViewById<android.view.View>(R.id.key_home).setOnClickListener {
            terminalSession?.write("[H")
        }
        view.findViewById<android.view.View>(R.id.key_end).setOnClickListener {
            terminalSession?.write("[F")
        }
        // Page Up / Down
        view.findViewById<android.view.View>(R.id.key_pgup).setOnClickListener {
            terminalSession?.write("[5~")
        }
        view.findViewById<android.view.View>(R.id.key_pgdn).setOnClickListener {
            terminalSession?.write("[6~")
        }
    }

    private fun getShellPath(): String {
        return listOf("/system/bin/sh", "/bin/sh", "/system/bin/bash")
            .firstOrNull { java.io.File(it).exists() } ?: "/system/bin/sh"
    }

    private fun buildEnvironment(): Array<String> {
        val home = requireContext().filesDir.absolutePath
        return arrayOf(
            "TERM=xterm-256color",
            "HOME=$home",
            "PATH=/system/bin:/system/xbin:/bin",
            "SHELL=${getShellPath()}",
            "TMPDIR=${requireContext().cacheDir.absolutePath}"
        )
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: return
        terminalSession?.write(text)
    }

    override fun onDestroy() {
        super.onDestroy()
        terminalSession?.finishIfRunning()
    }
}
