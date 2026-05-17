package com.sentry.filemanager.automation
import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.sentry.filemanager.R
import java.io.File

class FileWatcherService : Service() {
    private val observers = mutableListOf<FileObserver>()
    private val CHANNEL_ID = "sentry_file_watcher"
    private val NOTIF_ID = 9001

    companion object {
        const val ACTION_START = "com.sentry.filemanager.START_WATCHER"
        const val ACTION_STOP = "com.sentry.filemanager.STOP_WATCHER"
        const val ACTION_REFRESH = "com.sentry.filemanager.REFRESH_WATCHER"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            else -> refreshWatchers()
        }
        return START_STICKY
    }

    @Suppress("DEPRECATION")
    private fun refreshWatchers() {
        observers.forEach { it.stopWatching() }
        observers.clear()
        val mask = FileObserver.CREATE or FileObserver.MODIFY or FileObserver.DELETE
        AutomationRuleStore.getRules(this)
            .filter { it.enabled && it.trigger in listOf(RuleTrigger.FILE_ADDED, RuleTrigger.FILE_MODIFIED, RuleTrigger.FILE_DELETED) && it.watchPath.isNotEmpty() }
            .map { it.watchPath }.distinct()
            .filter { File(it).exists() }
            .forEach { path ->
                val obs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    object : FileObserver(File(path), mask) {
                        override fun onEvent(event: Int, rel: String?) { rel ?: return; handle(event, "$path/$rel") }
                    }
                } else {
                    object : FileObserver(path, mask) {
                        override fun onEvent(event: Int, rel: String?) { rel ?: return; handle(event, "$path/$rel") }
                    }
                }
                obs.startWatching(); observers.add(obs)
            }
        updateNotification("Watching ${observers.size} folder(s)")
    }

    private fun handle(event: Int, filePath: String) {
        val trigger = when (event and FileObserver.ALL_EVENTS) {
            FileObserver.CREATE -> RuleTrigger.FILE_ADDED
            FileObserver.MODIFY -> RuleTrigger.FILE_MODIFIED
            FileObserver.DELETE -> RuleTrigger.FILE_DELETED
            else -> return
        }
        AutomationEngine.processEvent(this, trigger, filePath)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "File Watcher", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun buildNotification(text: String = "Watching for file changes"): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sentry File Manager")
            .setContentText(text)
            .setSmallIcon(R.mipmap.launcher_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true).build()

    private fun updateNotification(text: String) =
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(text))

    override fun onDestroy() {
        observers.forEach { it.stopWatching() }
        observers.clear()
        super.onDestroy()
    }
}
