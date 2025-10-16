package com.clevercloud.typescript.import.support

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.psi.PsiElement

/**
 * Provides documentation for TypeScript types referenced in @import statements.
 * Shows type definitions and documentation when hovering over imported types.
 */
class TypeScriptImportDocumentationProvider : AbstractDocumentationProvider() {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        return when (element) {
            is TypeScriptInterface, is TypeScriptTypeAlias -> generateTypeDoc(element)
            else -> null
        }
    }

    private fun generateTypeDoc(element: PsiElement): String {
        val text = element.text ?: ""
        return buildString {
            append("<div class='definition'><pre>")
            append(text)
            append("</pre></div>")
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null

        return when (element) {
            is TypeScriptInterface -> "interface ${element.name}"
            is TypeScriptTypeAlias -> "type ${element.name}"
            else -> null
        }
    }
}