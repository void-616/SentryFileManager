package com.sentry.filemanager.provider.remote;

import com.sentry.filemanager.provider.remote.ParcelableException;
import com.sentry.filemanager.util.RemoteCallback;

interface IRemotePathObservable {
    void addObserver(in RemoteCallback observer);

    void close(out ParcelableException exception);
}
