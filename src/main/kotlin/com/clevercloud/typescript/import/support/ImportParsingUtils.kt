package com.clevercloud.typescript.import.support

import com.intellij.openapi.vfs.VirtualFile

/**
 * Utility functions and constants for parsing TypeScript @import statements.
 */
object ImportParsingUtils {
    /**
     * Regex pattern to match @import statements.
     * Pattern: @import { Type1, Type2 } from 'module-path'
     */
    val IMPORT_PATTERN = Regex("""@import\s+\{([^}]+)\}\s+from\s+['"]([^'"]+)['"]""")

    /**
     * Regex pattern to extract module path from import statement.
     */
    val MODULE_PATH_PATTERN = Regex("""from\s+['"]([^'"]+)['"]""")

    /**
     * Regex pattern to extract imported types from import statement.
     */
    val IMPORTS_PATTERN = Regex("""\{([^}]+)\}""")

    /**
     * Common TypeScript/JavaScript file extensions in order of precedence.
     */
    val FILE_EXTENSIONS = listOf("", ".ts", ".tsx", ".d.ts", ".js", ".jsx")

    /**
     * Data class representing a parsed import statement.
     */
    data class ImportInfo(
        val typeName: String,
        val alias: String,
        val modulePath: String
    )

    /**
     * Parses a type name that may include "as" alias syntax.
     * @param importName The import name string (e.g., "Type as Alias" or just "Type")
     * @return Pair of (actualName, alias)
     */
    fun parseTypeWithAlias(importName: String): Pair<String, String> {
        val trimmed = importName.trim()
        return if (trimmed.contains(" as ")) {
            val actualName = trimmed.substringBefore(" as ").trim()
            val alias = trimmed.substringAfter(" as ").trim()
            actualName to alias
        } else {
            trimmed to trimmed
        }
    }

    /**
     * Splits a comma-separated list of imports and trims each entry.
     * @param importsString The string containing comma-separated imports
     * @return List of trimmed import names
     */
    fun splitImports(importsString: String): List<String> {
        return importsString.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Parses all imports from an import statement string.
     * @param importsString The string containing comma-separated imports
     * @param modulePath The module path for these imports
     * @return List of ImportInfo objects
     */
    fun parseImports(importsString: String, modulePath: String): List<ImportInfo> {
        return splitImports(importsString).map { importName ->
            val (actualName, alias) = parseTypeWithAlias(importName)
            ImportInfo(actualName, alias, modulePath)
        }
    }

    /**
     * Attempts to resolve a file with various extensions.
     * @param directory The directory to search in
     * @param baseName The base filename (may or may not have extension)
     * @return The resolved VirtualFile or null if not found
     */
    fun resolveFileWithExtensions(directory: VirtualFile, baseName: String): VirtualFile? {
        // Try to find the file directly
        var file = directory.findChild(baseName)
        if (file != null && file.exists() && !file.isDirectory) {
            return file
        }

        // Try without extension if it has one
        val nameWithoutExt = if (baseName.contains('.')) {
            baseName.substringBeforeLast('.')
        } else {
            baseName
        }

        // Try adding common extensions
        for (ext in FILE_EXTENSIONS) {
            val testName = if (baseName.contains('.') && ext.isEmpty()) {
                baseName // Already has extension, use as-is
            } else {
                "$nameWithoutExt$ext"
            }

            file = directory.findChild(testName)
            if (file != null && file.exists() && !file.isDirectory) {
                return file
            }
        }

        return null
    }
}
