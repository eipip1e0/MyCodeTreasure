package com.ei.mergeviewbinding.compiler

import com.ei.mergeviewbinding.annotation.Merge
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class MergeSymbolProcessor(
    private val env: SymbolProcessorEnvironment
): SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mapperTypes =
            resolver.getSymbolsWithAnnotation(Merge::class.java.name)
                .filterIsInstance<KSClassDeclaration>()
                .filter { ClassKind.INTERFACE == it.classKind }
                .toSet()

        MergeGenerator(env).generate(resolver, mapperTypes)

        env.logger.warn("mapperTypes: ${mapperTypes.joinToString { it.simpleName.asString() }}")

        return emptyList()
    }
}