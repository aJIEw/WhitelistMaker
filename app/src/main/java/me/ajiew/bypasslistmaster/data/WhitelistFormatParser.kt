package me.ajiew.bypasslistmaster.data

import me.ajiew.bypasslistmaster.domain.WhitelistFileFormat

data class WhitelistParseResult(
    val entries: List<String>,
)

object WhitelistFormatParser {
    fun parse(content: String, format: WhitelistFileFormat): Result<WhitelistParseResult> {
        return when (format) {
            WhitelistFileFormat.TrueHeader -> parseTrueHeader(content)
        }
    }

    fun build(entries: List<String>, format: WhitelistFileFormat): String {
        return when (format) {
            WhitelistFileFormat.TrueHeader -> buildTrueHeader(entries)
        }
    }

    private fun parseTrueHeader(content: String): Result<WhitelistParseResult> {
        val lines = content.lineSequence()
            .map { it.trim() }
            .toList()
        val firstMeaningfulLine = lines.firstOrNull { it.isNotEmpty() }
            ?: return Result.failure(IllegalArgumentException("白名单内容为空"))
        if (firstMeaningfulLine != TRUE_HEADER) {
            return Result.failure(IllegalArgumentException("白名单文件首行必须为 true"))
        }

        val entries = lines
            .dropWhile { it.isEmpty() }
            .drop(1)
            .filter { it.isNotEmpty() }

        val invalidEntry = entries.firstOrNull { !PACKAGE_NAME_PATTERN.matches(it) }
        if (invalidEntry != null) {
            return Result.failure(IllegalArgumentException("包名格式不正确：$invalidEntry"))
        }

        return Result.success(WhitelistParseResult(entries = entries.distinct()))
    }

    private fun buildTrueHeader(entries: List<String>): String {
        return buildString {
            append(TRUE_HEADER)
            append('\n')
            entries.forEach { entry ->
                append(entry)
                append('\n')
            }
        }
    }

    private const val TRUE_HEADER = "true"
    private val PACKAGE_NAME_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
}
