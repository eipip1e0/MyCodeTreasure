package com.ei

import com.bennyhuo.kotlin.compiletesting.extensions.module.KotlinModule
import com.bennyhuo.kotlin.compiletesting.extensions.module.checkResult
import com.bennyhuo.kotlin.compiletesting.extensions.source.FileBasedModuleInfoLoader
import com.bennyhuo.kotlin.compiletesting.extensions.source.SourceModuleInfo
import com.ei.datamapping.compiler.MapperSymbolProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
class MainTest {

    private val testCaseDirCommon = File("testData")

    private val extensions = arrayOf("", ".kt", ".txt")
    @Test
    fun doTest() {
        val testCaseFile = listOf(testCaseDirCommon).firstNotNullOf { findTestCaseFile(it, "DataMapper") }

        val loader = FileBasedModuleInfoLoader(testCaseFile.path)
        loader.loadSourceModuleInfos().map(::createKotlinModule)
            .checkResult(
                loader.loadExpectModuleInfos(),
                checkExitCode = false,
                checkGeneratedFiles = true,
                checkCompilerOutput = true
            )
    }

    private fun createKotlinModule(moduleInfo: SourceModuleInfo): KotlinModule {
        return KotlinModule(moduleInfo, symbolProcessorProviders = listOf(MapperSymbolProcessorProvider()))
    }

    private fun findTestCaseFile(parentDir: File, name: String): File? {
        return extensions.firstNotNullOfOrNull { extension ->
            File(parentDir, "$name$extension").takeIf {
                it.exists()
            } ?: File(
                parentDir, "${name.capitalizeAsciiOnly()}$extension"
            ).takeIf {
                it.exists()
            }
        }
    }
}