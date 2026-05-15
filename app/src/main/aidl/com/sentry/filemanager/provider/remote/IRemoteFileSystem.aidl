package com.sentry.filemanager.provider.remote;

import com.sentry.filemanager.provider.remote.ParcelableException;

interface IRemoteFileSystem {
    void close(out ParcelableException exception);
}
