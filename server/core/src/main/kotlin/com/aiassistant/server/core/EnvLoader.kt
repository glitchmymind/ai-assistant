package com.aiassistant.server.core

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

object EnvLoader {
    fun load(fileName: String = ".env"): Map<String, String> {
        val envFile = findFile(fileName) ?: return emptyMap()
        return parse(Files.readAllLines(envFile))
    }

    fun findFile(fileName: String): Path? {
        var directory = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize()
        repeat(8) {
            val candidate = directory.resolve(fileName)
            if (candidate.exists() && candidate.isRegularFile()) {
                return candidate
            }
            directory = directory.parent ?: return null
        }
        return null
    }

    internal fun parse(lines: List<String>): Map<String, String> {
        return lines.mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                return@mapNotNull null
            }
            val content = trimmed.removePrefix("export ").trim()
            val separator = content.indexOf('=')
            if (separator <= 0) {
                return@mapNotNull null
            }
            val key = content.substring(0, separator).trim()
            val value = unquote(content.substring(separator + 1).trim())
            key to value
        }.toMap()
    }

    private fun unquote(value: String): String {
        return when {
            value.length >= 2 && value.startsWith('"') && value.endsWith('"') ->
                value.substring(1, value.lastIndex)
            value.length >= 2 && value.startsWith('\'') && value.endsWith('\'') ->
                value.substring(1, value.lastIndex)
            else -> value
        }
    }
}
