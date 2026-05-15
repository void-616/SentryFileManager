/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.app

import android.os.AsyncTask
import android.os.Build
import android.webkit.WebView
import jcifs.context.SingletonContext
import com.sentry.filemanager.BuildConfig
import com.sentry.filemanager.coil.initializeCoil
import com.sentry.filemanager.filejob.fileJobNotificationTemplate
import com.sentry.filemanager.ftpserver.ftpServerServiceNotificationTemplate
import com.sentry.filemanager.hiddenapi.HiddenApi
import com.sentry.filemanager.provider.FileSystemProviders
import com.sentry.filemanager.settings.Settings
import com.sentry.filemanager.storage.FtpServerAuthenticator
import com.sentry.filemanager.storage.SftpServerAuthenticator
import com.sentry.filemanager.storage.SmbServerAuthenticator
import com.sentry.filemanager.storage.StorageVolumeListLiveData
import com.sentry.filemanager.storage.WebDavServerAuthenticator
import com.sentry.filemanager.theme.custom.CustomThemeHelper
import com.sentry.filemanager.theme.night.NightModeHelper
import java.util.Properties
import com.sentry.filemanager.provider.ftp.client.Client as FtpClient
import com.sentry.filemanager.provider.sftp.client.Client as SftpClient
import com.sentry.filemanager.provider.smb.client.Client as SmbClient
import com.sentry.filemanager.provider.webdav.client.Client as WebDavClient

val appInitializers = listOf(
    ::disableHiddenApiChecks,
    ::initializeWebViewDebugging,
    ::initializeCoil,
    ::initializeFileSystemProviders,
    ::upgradeApp,
    ::initializeLiveDataObjects,
    ::initializeCustomTheme,
    ::initializeNightMode,
    ::createNotificationChannels
)


private fun disableHiddenApiChecks() {
    HiddenApi.disableHiddenApiChecks()
}

private fun initializeWebViewDebugging() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)
    }
}

private fun initializeFileSystemProviders() {
    FileSystemProviders.install()
    FileSystemProviders.overflowWatchEvents = true
    // SingletonContext.init() calls NameServiceClientImpl.initCache() which connects to network.
    AsyncTask.THREAD_POOL_EXECUTOR.execute {
        SingletonContext.init(
            Properties().apply {
                setProperty("jcifs.netbios.cachePolicy", "0")
                setProperty("jcifs.smb.client.maxVersion", "SMB1")
            }
        )
    }
    FtpClient.authenticator = FtpServerAuthenticator
    SftpClient.authenticator = SftpServerAuthenticator
    SmbClient.authenticator = SmbServerAuthenticator
    WebDavClient.authenticator = WebDavServerAuthenticator
}

private fun initializeLiveDataObjects() {
    // Force initialization of LiveData objects so that it won't happen on a background thread.
    StorageVolumeListLiveData.value
    Settings.FILE_LIST_DEFAULT_DIRECTORY.value
}

private fun initializeCustomTheme() {
    CustomThemeHelper.initialize(application)
}

private fun initializeNightMode() {
    NightModeHelper.initialize(application)
}

private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannels(
            listOf(
                backgroundActivityStartNotificationTemplate.channelTemplate,
                fileJobNotificationTemplate.channelTemplate,
                ftpServerServiceNotificationTemplate.channelTemplate
            ).map { it.create(application) }
        )
    }
}
