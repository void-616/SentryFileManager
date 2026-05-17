package com.sentry.filemanager.automation
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.*

object RuleActionExecutor {
    data class ActionResult(val success: Boolean, val message: String)

    fun execute(action: RuleAction, file: File): ActionResult = try {
        when (action.type) {
            ActionType.MOVE -> moveFile(file, action.targetPath)
            ActionType.COPY -> copyFile(file, action.targetPath)
            ActionType.DELETE ->
                if (if (file.isDirectory) file.deleteRecursively() else file.delete())
                    ActionResult(true, "Deleted ${file.name}")
                else ActionResult(false, "Delete failed")
            ActionType.RENAME -> renameFile(file, action.parameter)
            ActionType.COMPRESS_ZIP -> compressToZip(file, action.targetPath)
            ActionType.EXTRACT -> ActionResult(false, "Extract requires Archive plugin")
            ActionType.SHELL_COMMAND -> runShell(action.parameter, file)
        }
    } catch (e: Exception) { ActionResult(false, e.message ?: "Unknown error") }

    private fun moveFile(file: File, targetDir: String): ActionResult {
        val dest = File(targetDir, file.name).also { File(targetDir).mkdirs() }
        return if (file.renameTo(dest)) ActionResult(true, "Moved to $targetDir")
        else { file.copyTo(dest, overwrite = true); file.delete(); ActionResult(true, "Moved to $targetDir") }
    }

    private fun copyFile(file: File, targetDir: String): ActionResult {
        File(targetDir).mkdirs()
        file.copyTo(File(targetDir, file.name), overwrite = true)
        return ActionResult(true, "Copied to $targetDir")
    }

    private fun renameFile(file: File, pattern: String): ActionResult {
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val time = SimpleDateFormat("HHmmss", Locale.US).format(Date())
        val newName = pattern
            .replace("{name}", file.nameWithoutExtension)
            .replace("{ext}", file.extension)
            .replace("{date}", date)
            .replace("{time}", time)
            .ifEmpty { file.name }
        return if (file.renameTo(File(file.parent, newName)))
            ActionResult(true, "Renamed to $newName")
        else ActionResult(false, "Rename failed")
    }

    private fun compressToZip(file: File, targetDir: String): ActionResult {
        File(targetDir).mkdirs()
        val zipFile = File(targetDir, "${file.nameWithoutExtension}.zip")
        ZipOutputStream(zipFile.outputStream().buffered()).use { zos ->
            if (file.isDirectory) {
                file.walkTopDown().filter { it.isFile }.forEach { f ->
                    zos.putNextEntry(ZipEntry(f.relativeTo(file.parentFile!!).path))
                    f.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            } else {
                zos.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return ActionResult(true, "Compressed to ${zipFile.name}")
    }

    private fun runShell(command: String, file: File): ActionResult {
        val cmd = command
            .replace("{file}", file.absolutePath)
            .replace("{name}", file.name)
            .replace("{dir}", file.parent ?: "")
        val proc = ProcessBuilder("/system/bin/sh", "-c", cmd)
            .redirectErrorStream(true).start()
        val out = proc.inputStream.bufferedReader().readText()
        val exit = proc.waitFor()
        return ActionResult(exit == 0, out.take(200).ifEmpty { "Exit $exit" })
    }
}
