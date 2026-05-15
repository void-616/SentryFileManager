package com.sentry.filemanager.provider.remote;

import com.sentry.filemanager.provider.common.ParcelableFileTime;
import com.sentry.filemanager.provider.common.ParcelablePosixFileMode;
import com.sentry.filemanager.provider.common.PosixGroup;
import com.sentry.filemanager.provider.common.PosixUser;
import com.sentry.filemanager.provider.remote.ParcelableException;
import com.sentry.filemanager.provider.remote.ParcelableObject;

interface IRemotePosixFileAttributeView {
    ParcelableObject readAttributes(out ParcelableException exception);

    void setTimes(
        in ParcelableFileTime lastModifiedTime,
        in ParcelableFileTime lastAccessTime,
        in ParcelableFileTime createTime,
        out ParcelableException exception
    );

    void setOwner(in PosixUser owner, out ParcelableException exception);

    void setGroup(in PosixGroup group, out ParcelableException exception);

    void setMode(in ParcelablePosixFileMode mode, out ParcelableException exception);

    void setSeLinuxContext(in ParcelableObject context, out ParcelableException exception);

    void restoreSeLinuxContext(out ParcelableException exception);
}
