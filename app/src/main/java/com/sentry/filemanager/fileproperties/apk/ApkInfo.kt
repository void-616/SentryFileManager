/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.fileproperties.apk

import android.content.pm.PackageInfo

class ApkInfo(
    val packageInfo: PackageInfo,
    val label: String,
    val signingCertificateDigests: List<String>,
    val pastSigningCertificateDigests: List<String>
)
