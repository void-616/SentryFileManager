/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.storage

import com.sentry.filemanager.provider.webdav.client.Authentication
import com.sentry.filemanager.provider.webdav.client.Authenticator
import com.sentry.filemanager.provider.webdav.client.Authority
import com.sentry.filemanager.settings.Settings
import com.sentry.filemanager.util.valueCompat

object WebDavServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<WebDavServer>()

    override fun getAuthentication(authority: Authority): Authentication? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is WebDavServer && it.authority == authority
        } as WebDavServer?
        return server?.authentication
    }

    fun addTransientServer(server: WebDavServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: WebDavServer) {
        synchronized(transientServers) { transientServers -= server }
    }
}
