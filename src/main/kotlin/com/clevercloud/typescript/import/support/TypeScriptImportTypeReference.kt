package com.clevercloud.typescript.import.support

import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

/**
 * Reference for type names in @import statements.
 * Resolves 'Type' in: @import { Type } from 'module-path'
 */
class TypeScriptImportTypeReference(
    element: PsiElement,
    private val typeName: String,
    private val modulePath: String,
    startOffset: Int,
    endOffset: Int
) : PsiReferenceBase<PsiElement>(element, TextRange(startOffset, endOffset)) {

    override fun resolve(): PsiElement? {
        // First, resolve the module
        val moduleRef = TypeScriptImportModuleReference(element, modulePath, 0, 0)
        val moduleFile = moduleRef.resolve() as? PsiFile
        if (moduleFile == null) {
            return null
        }

        // Search for the type definition in the resolved module
        return findTypeInFile(moduleFile, typeName)
    }

    private fun findTypeInFile(file: PsiFile, typeName: String): PsiElement? {
        var result: PsiElement? = null

        // First, try to find the type directly defined in this file
        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is TypeScriptInterface -> {
                        if (element.name == typeName) {
                            result = element
                            return
                        }
                    }
                    is TypeScriptTypeAlias -> {
                        if (element.name == typeName) {
                            result = element
                            return
                        }
                    }
                }

                if (result == null) {
                    super.visitElement(element)
                }
            }
        })

        // If found directly, return it
        if (result != null) {
            return result
        }

        // Otherwise, search through @import statements in this file (transitive imports)
        return findTypeInImports(file, typeName)
    }

    private fun findTypeInImports(file: PsiFile, typeName: String): PsiElement? {
        val imports = mutableListOf<Triple<String, String, PsiComment>>() // Triple of (alias, modulePath, comment)

        // Find all @import statements in the file
        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is PsiComment && element.text.contains("@import")) {
                    val text = element.text

                    // Extract module path
                    val modulePathMatch = ImportParsingUtils.MODULE_PATH_PATTERN.find(text)
                    if (modulePathMatch != null) {
                        val modulePath = modulePathMatch.groupValues[1]

                        // Extract imported types
                        val importsMatch = ImportParsingUtils.IMPORTS_PATTERN.find(text)
                        if (importsMatch != null) {
                            val importedTypes = ImportParsingUtils.splitImports(importsMatch.groupValues[1])

                            for (importedType in importedTypes) {
                                // Handle "Type as Alias" syntax
                                val (actualName, alias) = ImportParsingUtils.parseTypeWithAlias(importedType)
                                imports.add(Triple(alias, modulePath, element))
                            }
                        }
                    }
                }
                super.visitElement(element)
            }
        })

        // Search through the imports for our type
        for ((importedTypeName, modulePath, comment) in imports) {
            if (importedTypeName == typeName) {
                // Resolve this import's module - use the comment element so paths are resolved relative to its file
                val moduleRef = TypeScriptImportModuleReference(comment, modulePath, 0, 0)
                val importedFile = moduleRef.resolve() as? PsiFile

                if (importedFile != null) {
                    // Recursively search in the imported file
                    val result = findTypeInFile(importedFile, typeName)
                    if (result != null) {
                        return result
                    }
                }
            }
        }

        return null
    }

    override fun getVariants(): Array<Any> {
        // Could provide autocomplete suggestions of available types
        return emptyArray()
    }
}