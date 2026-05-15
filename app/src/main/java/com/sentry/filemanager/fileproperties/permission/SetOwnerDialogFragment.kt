/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.fileproperties.permission

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import com.sentry.filemanager.R
import com.sentry.filemanager.file.FileItem
import com.sentry.filemanager.filejob.FileJobService
import com.sentry.filemanager.provider.common.PosixFileAttributes
import com.sentry.filemanager.provider.common.PosixPrincipal
import com.sentry.filemanager.provider.common.PosixUser
import com.sentry.filemanager.provider.common.toByteString
import com.sentry.filemanager.util.SelectionLiveData
import com.sentry.filemanager.util.putArgs
import com.sentry.filemanager.util.show
import com.sentry.filemanager.util.viewModels

class SetOwnerDialogFragment : SetPrincipalDialogFragment() {
    override val viewModel: SetPrincipalViewModel by viewModels { { SetOwnerViewModel() } }

    @StringRes
    override val titleRes: Int = R.string.file_properties_permission_set_owner_title

    override fun createAdapter(selectionLiveData: SelectionLiveData<Int>): PrincipalListAdapter =
        UserListAdapter(selectionLiveData)

    override val PosixFileAttributes.principal: PosixPrincipal
        get() = owner()!!

    override fun setPrincipal(path: Path, principal: PrincipalItem, recursive: Boolean) {
        val owner = PosixUser(principal.id, principal.name?.toByteString())
        FileJobService.setOwner(path, owner, recursive, requireContext())
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            SetOwnerDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }
}
