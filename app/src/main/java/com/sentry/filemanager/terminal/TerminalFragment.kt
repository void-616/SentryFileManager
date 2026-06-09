/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

@file:Suppress("DEPRECATION")
package com.sentry.filemanager.terminal


import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
                arguments = Bundle().apply {
                    putString(ARG_PATH, path)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startDirectory = arguments?.getString(ARG_PATH) ?: "/"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_terminal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        terminalView = view.findViewById(R.id.terminal_view)
        tvTitle = view.findViewById(R.id.terminal_title)

        view.findViewById<View>(R.id.btn_terminal_paste)
            .setOnClickListener { pasteFromClipboard() }

        view.findViewById<View>(R.id.btn_terminal_close)
            .setOnClickListener {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(terminalView.windowToken, 0)
                parentFragmentManager.popBackStack("terminal", androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

        setupKeyBar(view)

        view.post {
            startSession()
        }
    }

    private fun startSession() {
        val shell = getShellPath()

        val client = SentryTerminalClient(
            onUpdate = { terminalView.onScreenUpdated() },
            onTitleChange = { title ->
                activity?.runOnUiThread { tvTitle.text = title }
            }
        )

        terminalSession = TerminalSession(
            shell,
            startDirectory,
            arrayOf(),
            buildEnvironment(),
            2000,
            client
        )

        terminalView.setTextSize(24)
        terminalView.setTerminalViewClient(object : TerminalViewClient {
            override fun onScale(scale: Float) = 1f
            override fun onSingleTapUp(e: MotionEvent) {
                terminalView.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(terminalView, InputMethodManager.SHOW_FORCED)
            }
            override fun shouldBackButtonBeMappedToEscape() = false
            override fun shouldEnforceCharBasedInput() = false
            override fun shouldUseCtrlSpaceWorkaround() = false
            override fun isTerminalViewSelected() = true
            override fun copyModeChanged(copyMode: Boolean) {}
            override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession?) = false
            override fun onKeyUp(keyCode: Int, e: KeyEvent) = false
            override fun onLongPress(event: MotionEvent) = false
            override fun readControlKey() = false
            override fun readAltKey() = false
            override fun readShiftKey() = false
            override fun readFnKey() = false
            override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?) = false
            override fun onEmulatorSet() {}
            override fun logError(tag: String?, message: String?) {}
            override fun logWarn(tag: String?, message: String?) {}
            override fun logInfo(tag: String?, message: String?) {}
            override fun logDebug(tag: String?, message: String?) {}
            override fun logVerbose(tag: String?, message: String?) {}
            override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
            override fun logStackTrace(tag: String?, e: Exception?) {}
        })

        terminalView.attachSession(terminalSession)

        terminalView.post {
            terminalView.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(terminalView, InputMethodManager.SHOW_FORCED)
        }


    }

    private fun setupKeyBar(view: View) {
        view.findViewById<View>(R.id.key_tab).setOnClickListener { terminalSession?.write("\t") }
        view.findViewById<View>(R.id.key_ctrl).setOnClickListener { terminalSession?.write("\u0003") }
        view.findViewById<View>(R.id.key_esc).setOnClickListener { terminalSession?.write("\u001b") }
        view.findViewById<View>(R.id.key_arrow_up).setOnClickListener { terminalSession?.write("\u001b[A") }
        view.findViewById<View>(R.id.key_arrow_down).setOnClickListener { terminalSession?.write("\u001b[B") }
        view.findViewById<View>(R.id.key_arrow_left).setOnClickListener { terminalSession?.write("\u001b[D") }
        view.findViewById<View>(R.id.key_arrow_right).setOnClickListener { terminalSession?.write("\u001b[C") }
        view.findViewById<View>(R.id.key_home).setOnClickListener { terminalSession?.write("\u001b[H") }
        view.findViewById<View>(R.id.key_end).setOnClickListener { terminalSession?.write("\u001b[F") }
        view.findViewById<View>(R.id.key_pgup).setOnClickListener { terminalSession?.write("\u001b[5~") }
        view.findViewById<View>(R.id.key_pgdn).setOnClickListener { terminalSession?.write("\u001b[6~") }
    }

    private fun getShellPath(): String {
        return listOf(
            "/system/bin/sh",
            "/bin/sh",
            "/system/bin/bash"
        ).firstOrNull { java.io.File(it).exists() } ?: "/system/bin/sh"
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
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val text = clipboard.primaryClip
            ?.getItemAt(0)
            ?.text
            ?.toString()
            ?: return

        terminalSession?.write(text)
    }

    override fun onDestroy() {
        super.onDestroy()
        terminalSession?.finishIfRunning()
    }
}
