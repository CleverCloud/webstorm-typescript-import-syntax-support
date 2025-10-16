package com.clevercloud.typescript.import.support

import com.intellij.openapi.diagnostic.Logger
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

/**
 * Provides references for TypeScript @import syntax.
 * Example: @import { MyType } from './types'
 * Note: TypeScript @import is not a JSDoc tag, it's a special comment-based import syntax
 */
class TypeScriptImportReferenceProvider : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Match all comment elements to find @import syntax
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiComment::class.java),
            ImportCommentReferenceProvider()
        )
    }
}

class ImportCommentReferenceProvider : PsiReferenceProvider() {
    companion object {
        private val LOG = Logger.getInstance(ImportCommentReferenceProvider::class.java)
    }

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        // Only process comments
        if (element !is PsiComment) return PsiReference.EMPTY_ARRAY

        val text = element.text

        // Check if this comment contains @import
        if (!text.contains("@import")) return PsiReference.EMPTY_ARRAY

        LOG.debug("Processing @import comment")

        val references = mutableListOf<PsiReference>()

        // Find ALL @import statements in the comment (there may be multiple)
        val matches = ImportParsingUtils.IMPORT_PATTERN.findAll(text)

        for (match in matches) {
            val imports = match.groupValues[1]
            val modulePath = match.groupValues[2]

            LOG.debug("Found @import: $imports from $modulePath")

            // Create reference for the module path
            val modulePathStart = match.range.start + text.substring(match.range.start).indexOf(modulePath)
            references.add(
                TypeScriptImportModuleReference(
                    element,
                    modulePath,
                    modulePathStart,
                    modulePathStart + modulePath.length
                )
            )

            // Create references for each imported type
            val importList = ImportParsingUtils.splitImports(imports)
            for (importName in importList) {
                // Handle "Type as Alias" syntax
                val (actualName, _) = ImportParsingUtils.parseTypeWithAlias(importName)

                // Find the position of this specific type name within the match
                val nameStart = match.range.start + match.value.indexOf(actualName)
                references.add(
                    TypeScriptImportTypeReference(
                        element,
                        actualName,
                        modulePath,
                        nameStart,
                        nameStart + actualName.length
                    )
                )
            }
        }

        LOG.debug("Created ${references.size} references")
        return references.toTypedArray()
    }
}