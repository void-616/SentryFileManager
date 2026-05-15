/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.storage

import com.sentry.filemanager.provider.ftp.client.Authenticator
import com.sentry.filemanager.provider.ftp.client.Authority
import com.sentry.filemanager.settings.Settings
import com.sentry.filemanager.util.valueCompat

object FtpServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<FtpServer>()

    override fun getPassword(authority: Authority): String? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is FtpServer && it.authority == authority
        } as FtpServer?
        return server?.password
    }

    fun addTransientServer(server: FtpServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: FtpServer) {
        synchronized(transientServers) { transientServers -= server }
    }
}
