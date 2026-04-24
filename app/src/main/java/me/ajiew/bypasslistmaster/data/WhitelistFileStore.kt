package me.ajiew.bypasslistmaster.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import me.ajiew.bypasslistmaster.domain.WhitelistConfig
import java.io.File

class WhitelistFileStore(
    private val context: Context,
    private val preferences: AppPreferences,
) {
    private val whitelistEntriesFlow = MutableStateFlow<List<String>>(emptyList())

    init {
        whitelistEntriesFlow.value = loadEntries()
    }

    fun entriesFlow(): StateFlow<List<String>> = whitelistEntriesFlow.asStateFlow()

    suspend fun importWhitelist(uri: Uri): String = withContext(Dispatchers.IO) {
        val displayName = queryDisplayName(uri) ?: "已导入白名单"
        preferences.updateWhitelistConfig(
            WhitelistConfig(
                importedUri = uri.toString(),
                importedDisplayName = displayName,
            )
        )
        whitelistEntriesFlow.value = loadEntries()
        displayName
    }

    suspend fun appendPackage(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val entries = loadEntries().toMutableList()
        if (entries.contains(packageName)) {
            return@withContext false
        }
        entries.add(packageName)
        writeAll(entries)
        whitelistEntriesFlow.value = entries
        true
    }

    suspend fun exportToUri(uri: Uri) = withContext(Dispatchers.IO) {
        val content = buildExportContent(loadEntries())
        context.contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { writer ->
            writer.write(content)
        } ?: error("无法写入导出文件")
    }

    suspend fun readExportText(): String = withContext(Dispatchers.IO) {
        buildExportContent(loadEntries())
    }

    private fun loadEntries(): List<String> {
        val raw = readRawContent()
        if (raw.isBlank()) {
            ensureDefaultFileExists()
            return emptyList()
        }
        return raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { it == DEFAULT_HEADER }
            .distinct()
            .toList()
    }

    private fun writeAll(entries: List<String>) {
        val content = buildExportContent(entries)
        val config = preferences.whitelistConfigFlow().value
        val importedUri = config.importedUri?.let(Uri::parse)
        if (importedUri != null) {
            context.contentResolver.openOutputStream(importedUri, "wt")?.bufferedWriter()?.use { writer ->
                writer.write(content)
            } ?: error("无法写入导入的白名单文件")
        } else {
            ensureDefaultFileExists().writeText(content)
        }
    }

    private fun readRawContent(): String {
        val config = preferences.whitelistConfigFlow().value
        val importedUri = config.importedUri?.let(Uri::parse)
        return if (importedUri != null) {
            context.contentResolver.openInputStream(importedUri)?.bufferedReader()?.use { it.readText() }.orEmpty()
        } else {
            ensureDefaultFileExists().readText()
        }
    }

    private fun ensureDefaultFileExists(): File {
        val file = File(context.filesDir, DEFAULT_FILE_NAME)
        if (!file.exists()) {
            file.writeText("$DEFAULT_HEADER\n")
        } else if (file.readText().isBlank()) {
            file.writeText("$DEFAULT_HEADER\n")
        }
        return file
    }

    private fun buildExportContent(entries: List<String>): String {
        return buildString {
            append(DEFAULT_HEADER)
            append('\n')
            entries.forEach { entry ->
                append(entry)
                append('\n')
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else {
                    null
                }
            }
    }

    companion object {
        private const val DEFAULT_FILE_NAME = "default_whitelist.txt"
        private const val DEFAULT_HEADER = "true"
    }
}
