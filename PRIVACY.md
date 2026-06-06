# Privacy Policy — Sentry File Manager

**Last updated:** June 2026

---

## Overview

Sentry File Manager is a local, privacy-focused file manager. We do not collect, store, transmit, or sell any personal data. This policy explains what permissions the app uses and why.

---

## Data Collection

**Sentry File Manager collects no personal data.**

- No analytics
- No crash reporting sent to external servers
- No advertising
- No user accounts
- No tracking of any kind

All file operations happen entirely on your device.

---

## Permissions

### `MANAGE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`
Required to browse, read, copy, move, delete, and manage files on your device storage. Files are never uploaded or transmitted unless you explicitly connect to a remote server (FTP, SFTP, SMB, WebDAV) that you configure yourself.

### `INTERNET`
Used only for:
- Connecting to remote servers (FTP, SFTP, SMB, WebDAV) that **you** configure
- Checking for app updates via the GitHub API (`api.github.com`)

No personal data is included in update check requests. Only the latest release version tag is fetched.

### `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC`
Used to keep file transfer operations (copy, move, extract) running in the background without being interrupted by the system.

### `POST_NOTIFICATIONS`
Used to show progress notifications for file operations running in the background.

### `WAKE_LOCK`
Used to prevent the device from sleeping during long file transfer operations.

### `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE`
Used to detect network availability when connecting to remote servers.

### `REQUEST_INSTALL_PACKAGES`
Used to allow installing APK files directly from the file manager.

### `QUERY_ALL_PACKAGES`
Used to show app icons and names when browsing APK files.

---

## Remote Servers

If you choose to connect to a remote server (FTP, SFTP, SMB, WebDAV), your connection credentials (host, username, password) are stored **locally on your device only** using Android's SharedPreferences. They are never transmitted to us or any third party.

---

## Update Checks

When you tap "Check for Updates" in the About page, the app makes a single request to:

```
https://api.github.com/repos/void-616/SentryFileManager/releases/latest
```

This request contains no personal data. It only retrieves the latest release version number to compare with the installed version.

---

## Third-Party Libraries

Sentry File Manager uses open-source libraries. None of these libraries collect personal data. A full list of libraries and their licenses is available in the app under **About → Open Source Licenses**.

---

## Children's Privacy

Sentry File Manager does not knowingly collect any data from anyone, including children under the age of 13.

---

## Changes to This Policy

If this policy is updated, the "Last updated" date at the top will be changed. Continued use of the app after changes constitutes acceptance of the updated policy.

---

## Contact

If you have any questions about this privacy policy, contact us via Telegram:

**t.me/SentryFileManager**

Or open an issue on GitHub:

**github.com/void-616/SentryFileManager/issues**

---

*Sentry File Manager is open source software licensed under the GNU General Public License v3.0.*
