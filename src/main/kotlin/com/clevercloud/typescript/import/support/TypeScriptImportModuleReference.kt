package com.clevercloud.typescript.import.support

import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Reference for the module path in @import statements.
 * Resolves 'module-path' in: @import { Type } from 'module-path'
 */
class TypeScriptImportModuleReference(
    element: PsiElement,
    private val modulePath: String,
    startOffset: Int,
    endOffset: Int
) : PsiReferenceBase<PsiElement>(element, TextRange(startOffset, endOffset)) {

    companion object {
        private val LOG = Logger.getInstance(TypeScriptImportModuleReference::class.java)
    }

    override fun resolve(): PsiElement? {
        LOG.debug("Resolving module: $modulePath")
        val project = element.project
        val containingFile = element.containingFile?.virtualFile
        if (containingFile == null) {
            LOG.debug("No containing file")
            return null
        }
        val containingDir = containingFile.parent
        if (containingDir == null) {
            LOG.debug("No containing directory")
            return null
        }

        // Resolve relative paths
        if (modulePath.startsWith("./") || modulePath.startsWith("../")) {
            val resolvedFile = resolveRelativePath(containingDir, modulePath)
            if (resolvedFile != null) {
                LOG.debug("Resolved $modulePath to ${resolvedFile.path}")
                return PsiManager.getInstance(project).findFile(resolvedFile)
            } else {
                LOG.warn("Failed to resolve relative path: $modulePath from ${containingFile.path}")
            }
        }

        // Try to resolve as node_modules or absolute path
        LOG.debug("Attempting node_modules resolution for $modulePath")
        return resolveNodeModules(project, modulePath)
    }

    private fun resolveRelativePath(baseDir: VirtualFile, path: String): VirtualFile? {
        var currentDir = baseDir
        val pathParts = path.split("/").filter { it.isNotEmpty() }

        // Navigate through directory structure
        for (i in pathParts.indices) {
            val part = pathParts[i]

            when (part) {
                "." -> continue // ignore current directory
                ".." -> {
                    currentDir = currentDir.parent ?: return null
                }
                else -> {
                    // Check if this is the last part (filename)
                    if (i == pathParts.size - 1) {
                        val file = ImportParsingUtils.resolveFileWithExtensions(currentDir, part)
                        if (file != null) {
                            return file
                        }
                        LOG.debug("File not found: $part in ${currentDir.path}")
                        return null
                    } else {
                        // Navigate into subdirectory
                        val subDir = currentDir.findChild(part)
                        if (subDir == null || !subDir.isDirectory) {
                            LOG.debug("Subdirectory not found: $part in ${currentDir.path}")
                            return null
                        }
                        currentDir = subDir
                    }
                }
            }
        }

        return null
    }

    private fun resolveNodeModules(project: com.intellij.openapi.project.Project, modulePath: String): PsiElement? {
        val fileName = modulePath.substringAfterLast('/')

        // Search for files with the given name
        for (ext in ImportParsingUtils.FILE_EXTENSIONS) {
            if (ext.isEmpty()) continue // Skip empty extension for node_modules search

            val files = FilenameIndex.getVirtualFilesByName(
                "$fileName$ext",
                GlobalSearchScope.allScope(project)
            )

            for (virtualFile in files) {
                val file = PsiManager.getInstance(project).findFile(virtualFile)
                if (file is JSFile) {
                    return file
                }
            }
        }

        return null
    }

    override fun getVariants(): Array<Any> {
        // Could provide autocomplete suggestions here
        return emptyArray()
    }
}