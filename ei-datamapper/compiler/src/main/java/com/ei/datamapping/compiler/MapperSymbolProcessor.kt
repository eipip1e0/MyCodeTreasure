package com.ei.datamapping.compiler

import com.ei.datamapping.annotation.Mapper
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class MapperSymbolProcessor(
    private val env: SymbolProcessorEnvironment
): SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mapperTypes =
            resolver.getSymbolsWithAnnotation(Mapper::class.java.name)
                .filterIsInstance<KSClassDeclaration>()
                .filter { ClassKind.INTERFACE == it.classKind }
                .toSet()

        MapperGenerator(env).generate(resolver, mapperTypes)

        env.logger.warn("mapperTypes: ${mapperTypes.joinToString { it.simpleName.asString() }}")

        return emptyList()
    }
}