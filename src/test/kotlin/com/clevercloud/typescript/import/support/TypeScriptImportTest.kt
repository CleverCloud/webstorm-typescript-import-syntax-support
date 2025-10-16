package com.clevercloud.typescript.import.support

import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.psi.PsiReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TypeScriptImportTest : BasePlatformTestCase() {

    /**
     * Test that a single @import statement creates proper references
     */
    fun testSingleImport() {
        // Create a type definition file
        myFixture.configureByText(
            "types.d.ts",
            """
            export interface User {
                name: string;
                age: number;
            }
            """.trimIndent()
        )

        // Create a JS file with @import
        myFixture.configureByText(
            "main.js",
            """
            /**
             * @import {Us<caret>er} from './types.d.ts'
             */
            """.trimIndent()
        )

        val reference = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Should have reference at caret", reference)
    }

    /**
     * Test multiple @import statements in a single comment block
     */
    fun testMultipleImportsInOneComment() {
        // Create type files
        myFixture.addFileToProject(
            "user.types.d.ts",
            """
            export interface User {
                name: string;
            }
            """.trimIndent()
        )

        myFixture.addFileToProject(
            "product.types.d.ts",
            """
            export type Product = {
                id: string;
                price: number;
            }
            """.trimIndent()
        )

        // Create JS file with multiple imports - test first type
        myFixture.configureByText(
            "main.js",
            """
            /**
             * @import {Us<caret>er} from './user.types.d.ts'
             * @import {Product} from './product.types.d.ts'
             */
            """.trimIndent()
        )

        val reference1 = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Should have reference for User", reference1)

        // Test second type
        val text = myFixture.editor.document.text
        val offset = text.indexOf("Product") + 2
        myFixture.editor.caretModel.moveToOffset(offset)

        val reference2 = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Should have reference for Product", reference2)
    }

    /**
     * Test navigation to type definition
     */
    fun testNavigationToType() {
        // Create type file
        myFixture.configureByText(
            "types.d.ts",
            """
            export interface User {
                name: string;
            }
            """.trimIndent()
        )

        // Create JS file with import
        myFixture.configureByText(
            "main.js",
            """
            /**
             * @import {Us<caret>er} from './types.d.ts'
             */
            """.trimIndent()
        )

        // Navigate to definition
        val elementAtCaret = myFixture.elementAtCaret
        assertNotNull("Should find element at caret", elementAtCaret)

        if (elementAtCaret is TypeScriptInterface || elementAtCaret is TypeScriptTypeAlias) {
            assertEquals("User", elementAtCaret.name)
        }
    }

    /**
     * Test navigation to file from module path
     */
    fun testNavigationToFile() {
        // Create type file
        val typeFile = myFixture.configureByText(
            "types.d.ts",
            """
            export interface User {
                name: string;
            }
            """.trimIndent()
        )

        // Create JS file with import, caret on module path
        myFixture.configureByText(
            "main.js",
            """
            /**
             * @import {User} from './types.d<caret>.ts'
             */
            """.trimIndent()
        )

        // Navigate to file
        val references = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Should have reference", references)

        val resolved = references?.resolve()
        assertNotNull("Should resolve to file", resolved)
        assertEquals("Should resolve to types.d.ts", typeFile, resolved)
    }

    /**
     * Test relative path resolution - parent directory
     */
    fun testParentDirectoryImport() {
        // Create type file in parent directory
        myFixture.addFileToProject(
            "common.types.d.ts",
            """
            export interface IconModel {
                content: string;
            }
            """.trimIndent()
        )

        // Create JS file in subdirectory
        myFixture.addFileToProject(
            "components/badge.js",
            """
            /**
             * @import {IconModel} from '../common.types.d.ts'
             */
            """.trimIndent()
        )

        myFixture.configureFromTempProjectFile("components/badge.js")

        // Move caret to IconModel
        val text = myFixture.editor.document.text
        val offset = text.indexOf("IconModel") + 2
        myFixture.editor.caretModel.moveToOffset(offset)

        val elementAtCaret = myFixture.elementAtCaret
        assertNotNull("Should find IconModel type", elementAtCaret)
    }

    /**
     * Test transitive imports (type re-exported from another file)
     */
    fun testTransitiveImports() {
        // Create base type file
        myFixture.addFileToProject(
            "base.types.d.ts",
            """
            export interface BaseType {
                id: string;
            }
            """.trimIndent()
        )

        // Create intermediate file that re-exports
        myFixture.addFileToProject(
            "intermediate.types.d.ts",
            """
            /**
             * @import {BaseType} from './base.types.d.ts'
             */
            """.trimIndent()
        )

        // Create main file that imports from intermediate
        myFixture.addFileToProject(
            "main.js",
            """
            /**
             * @import {BaseType} from './intermediate.types.d.ts'
             */
            """.trimIndent()
        )

        myFixture.configureFromTempProjectFile("main.js")

        // Move caret to BaseType
        val text = myFixture.editor.document.text
        val offset = text.indexOf("BaseType") + 2
        myFixture.editor.caretModel.moveToOffset(offset)

        val elementAtCaret = myFixture.elementAtCaret
        assertNotNull("Should find BaseType through transitive import", elementAtCaret)
    }

    /**
     * Test import with type alias
     */
    fun testTypeAliasImport() {
        // Create type file with type alias
        myFixture.configureByText(
            "types.d.ts",
            """
            export type Status = 'active' | 'inactive' | 'pending';
            """.trimIndent()
        )

        // Create JS file with import
        myFixture.configureByText(
            "main.js",
            """
            /**
             * @import {Stat<caret>us} from './types.d.ts'
             */
            """.trimIndent()
        )

        val elementAtCaret = myFixture.elementAtCaret
        assertNotNull("Should find Status type alias", elementAtCaret)

        if (elementAtCaret is TypeScriptTypeAlias) {
            assertEquals("Status", elementAtCaret.name)
        }
    }

    /**
     * Test multiple types from same module
     */
    fun testMultipleTypesFromSameModule() {
        // Create type file
        myFixture.addFileToProject(
            "types.d.ts",
            """
            export interface User {
                name: string;
            }

            export type Role = 'admin' | 'user';
            """.trimIndent()
        )

        // Create JS file with multiple imports from same file - test User
        myFixture.configureByText(
            "main.js",
            """
            /**
             * @import {Us<caret>er, Role} from './types.d.ts'
             */
            """.trimIndent()
        )

        val reference1 = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Should have reference for User", reference1)

        // Test Role
        val text = myFixture.editor.document.text
        val offset = text.indexOf("Role") + 2
        myFixture.editor.caretModel.moveToOffset(offset)

        val reference2 = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Should have reference for Role", reference2)
    }

    /**
     * Test import with 'as' alias syntax
     */
    fun testImportWithAlias() {
        // Create type file
        myFixture.addFileToProject(
            "types.d.ts",
            """
            export interface User {
                name: string;
            }
            """.trimIndent()
        )

        // Create JS file with aliased import
        myFixture.configureByText(
            "main.js",
            """
            /**
             * @import {Us<caret>er as UserType} from './types.d.ts'
             */
            """.trimIndent()
        )

        // Should create reference for the original name 'User'
        val reference = myFixture.getReferenceAtCaretPosition()
        assertNotNull("Should have reference for User", reference)
    }

    /**
     * Test relative path with subdirectory
     */
    fun testSubdirectoryImport() {
        // Create type file in subdirectory
        myFixture.addFileToProject(
            "types/user/user.types.d.ts",
            """
            export interface User {
                name: string;
            }
            """.trimIndent()
        )

        // Create JS file
        myFixture.addFileToProject(
            "main.js",
            """
            /**
             * @import {User} from './types/user/user.types.d.ts'
             */
            """.trimIndent()
        )

        myFixture.configureFromTempProjectFile("main.js")

        val text = myFixture.editor.document.text
        val offset = text.indexOf("User") + 2
        myFixture.editor.caretModel.moveToOffset(offset)

        val elementAtCaret = myFixture.elementAtCaret
        assertNotNull("Should find User type in subdirectory", elementAtCaret)
    }

    override fun getTestDataPath() = "src/test/testData"
}
