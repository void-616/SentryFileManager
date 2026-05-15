/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: /home/sentry/android-sdk/build-tools/37.0.0/aidl -p/home/sentry/android-sdk/platforms/android-36/framework.aidl -I/home/sentry/SentryFileManager/app/src/main/aidl /home/sentry/SentryFileManager/app/src/main/aidl/com/sentry/filemanager/provider/remote/IRemoteFileService.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package com.sentry.filemanager.provider.remote;
public interface IRemoteFileService extends android.os.IInterface
{
  /** Default implementation for IRemoteFileService. */
  public static class Default implements com.sentry.filemanager.provider.remote.IRemoteFileService
  {
    @Override public com.sentry.filemanager.provider.remote.IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(java.lang.String scheme) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.sentry.filemanager.provider.remote.IRemoteFileSystem getRemoteFileSystemInterface(com.sentry.filemanager.provider.remote.ParcelableObject fileSystem) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.sentry.filemanager.provider.remote.IRemotePosixFileStore getRemotePosixFileStoreInterface(com.sentry.filemanager.provider.remote.ParcelableObject fileStore) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.sentry.filemanager.provider.remote.IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(com.sentry.filemanager.provider.remote.ParcelableObject attributeView) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.sentry.filemanager.provider.remote.IRemoteFileService
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.sentry.filemanager.provider.remote.IRemoteFileService interface,
     * generating a proxy if needed.
     */
    public static com.sentry.filemanager.provider.remote.IRemoteFileService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.sentry.filemanager.provider.remote.IRemoteFileService))) {
        return ((com.sentry.filemanager.provider.remote.IRemoteFileService)iin);
      }
      return new com.sentry.filemanager.provider.remote.IRemoteFileService.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(DESCRIPTOR);
      }
      switch (code)
      {
        case TRANSACTION_getRemoteFileSystemProviderInterface:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          com.sentry.filemanager.provider.remote.IRemoteFileSystemProvider _result = this.getRemoteFileSystemProviderInterface(_arg0);
          reply.writeNoException();
          reply.writeStrongInterface(_result);
          break;
        }
        case TRANSACTION_getRemoteFileSystemInterface:
        {
          com.sentry.filemanager.provider.remote.ParcelableObject _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.sentry.filemanager.provider.remote.ParcelableObject.CREATOR);
          com.sentry.filemanager.provider.remote.IRemoteFileSystem _result = this.getRemoteFileSystemInterface(_arg0);
          reply.writeNoException();
          reply.writeStrongInterface(_result);
          break;
        }
        case TRANSACTION_getRemotePosixFileStoreInterface:
        {
          com.sentry.filemanager.provider.remote.ParcelableObject _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.sentry.filemanager.provider.remote.ParcelableObject.CREATOR);
          com.sentry.filemanager.provider.remote.IRemotePosixFileStore _result = this.getRemotePosixFileStoreInterface(_arg0);
          reply.writeNoException();
          reply.writeStrongInterface(_result);
          break;
        }
        case TRANSACTION_getRemotePosixFileAttributeViewInterface:
        {
          com.sentry.filemanager.provider.remote.ParcelableObject _arg0;
          _arg0 = _Parcel.readTypedObject(data, com.sentry.filemanager.provider.remote.ParcelableObject.CREATOR);
          com.sentry.filemanager.provider.remote.IRemotePosixFileAttributeView _result = this.getRemotePosixFileAttributeViewInterface(_arg0);
          reply.writeNoException();
          reply.writeStrongInterface(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static final class Proxy implements com.sentry.filemanager.provider.remote.IRemoteFileService
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public final java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public com.sentry.filemanager.provider.remote.IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(java.lang.String scheme) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.sentry.filemanager.provider.remote.IRemoteFileSystemProvider _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(scheme);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRemoteFileSystemProviderInterface, _data, _reply, 0);
          _reply.readException();
          _result = com.sentry.filemanager.provider.remote.IRemoteFileSystemProvider.Stub.asInterface(_reply.readStrongBinder());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.sentry.filemanager.provider.remote.IRemoteFileSystem getRemoteFileSystemInterface(com.sentry.filemanager.provider.remote.ParcelableObject fileSystem) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.sentry.filemanager.provider.remote.IRemoteFileSystem _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, fileSystem, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRemoteFileSystemInterface, _data, _reply, 0);
          _reply.readException();
          _result = com.sentry.filemanager.provider.remote.IRemoteFileSystem.Stub.asInterface(_reply.readStrongBinder());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.sentry.filemanager.provider.remote.IRemotePosixFileStore getRemotePosixFileStoreInterface(com.sentry.filemanager.provider.remote.ParcelableObject fileStore) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.sentry.filemanager.provider.remote.IRemotePosixFileStore _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, fileStore, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRemotePosixFileStoreInterface, _data, _reply, 0);
          _reply.readException();
          _result = com.sentry.filemanager.provider.remote.IRemotePosixFileStore.Stub.asInterface(_reply.readStrongBinder());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public com.sentry.filemanager.provider.remote.IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(com.sentry.filemanager.provider.remote.ParcelableObject attributeView) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.sentry.filemanager.provider.remote.IRemotePosixFileAttributeView _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, attributeView, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRemotePosixFileAttributeViewInterface, _data, _reply, 0);
          _reply.readException();
          _result = com.sentry.filemanager.provider.remote.IRemotePosixFileAttributeView.Stub.asInterface(_reply.readStrongBinder());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_getRemoteFileSystemProviderInterface = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getRemoteFileSystemInterface = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getRemotePosixFileStoreInterface = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getRemotePosixFileAttributeViewInterface = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.sentry.filemanager.provider.remote.IRemoteFileService";
  public com.sentry.filemanager.provider.remote.IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(java.lang.String scheme) throws android.os.RemoteException;
  public com.sentry.filemanager.provider.remote.IRemoteFileSystem getRemoteFileSystemInterface(com.sentry.filemanager.provider.remote.ParcelableObject fileSystem) throws android.os.RemoteException;
  public com.sentry.filemanager.provider.remote.IRemotePosixFileStore getRemotePosixFileStoreInterface(com.sentry.filemanager.provider.remote.ParcelableObject fileStore) throws android.os.RemoteException;
  public com.sentry.filemanager.provider.remote.IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(com.sentry.filemanager.provider.remote.ParcelableObject attributeView) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
