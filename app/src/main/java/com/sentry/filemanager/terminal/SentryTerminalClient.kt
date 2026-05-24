/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.terminal

import android.graphics.Bitmap
import android.graphics.Color
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

class SentryTerminalClient(
    private val onUpdate: () -> Unit,
    private val onTitleChange: (String) -> Unit
) : TerminalSessionClient {

    override fun onTextChanged(changedSession: TerminalSession) {
        onUpdate()
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        onTitleChange(changedSession.title ?: "Terminal")
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        onUpdate()
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {}

    override fun onPasteTextFromClipboard(session: TerminalSession?) {}

    override fun onBell(session: TerminalSession) {}

    override fun onColorsChanged(session: TerminalSession) {
        onUpdate()
    }

    override fun onTerminalCursorStateChange(state: Boolean) {}

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}

    override fun getTerminalCursorStyle(): Int = 0

    override fun logError(tag: String?, message: String?) {}
    override fun logWarn(tag: String?, message: String?) {}
    override fun logInfo(tag: String?, message: String?) {}
    override fun logDebug(tag: String?, message: String?) {}
    override fun logVerbose(tag: String?, message: String?) {}
    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
    override fun logStackTrace(tag: String?, e: Exception?) {}
}
