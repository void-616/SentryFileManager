package com.sentry.filemanager.provider.remote;

import com.sentry.filemanager.provider.remote.IRemoteFileSystem;
import com.sentry.filemanager.provider.remote.IRemoteFileSystemProvider;
import com.sentry.filemanager.provider.remote.IRemotePosixFileAttributeView;
import com.sentry.filemanager.provider.remote.IRemotePosixFileStore;
import com.sentry.filemanager.provider.remote.ParcelableObject;

interface IRemoteFileService {
    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String scheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableObject fileSystem);

    IRemotePosixFileStore getRemotePosixFileStoreInterface(in ParcelableObject fileStore);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
        in ParcelableObject attributeView
    );
}
