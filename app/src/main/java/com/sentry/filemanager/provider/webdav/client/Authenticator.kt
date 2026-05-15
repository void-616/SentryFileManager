/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.provider.webdav.client

interface Authenticator {
    fun getAuthentication(authority: Authority): Authentication?
}
