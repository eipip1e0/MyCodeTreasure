package com.ei.mergeviewbinding.compiler

import com.ei.mergeviewbinding.annotation.MergeConfig
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.OutputStreamWriter

class MergeGenerator(private val env: SymbolProcessorEnvironment) {

    fun generate(resolver: Resolver, mapperTypes: Set<KSClassDeclaration>) {
        mapperTypes.forEach classForEach@{ mapperClass: KSClassDeclaration ->
            // 获取注解类中的所有方法
            // 获取方法注解 name 作为名称
            // 获取方法的参数类型
            // 指定binding merge 操作
            mapperClass.getDeclaredFunctions().forEach { funDec ->
                genBindingMerge(mapperClass.packageName.asString(), funDec)
            }
        }
    }

    private fun genBindingMerge(packageName: String, funDec: KSFunctionDeclaration) {
        val mergeConfigKSAnnotation =
            funDec.annotations.find { it.shortName.asString() == MergeConfig::class.simpleName }
                ?: return
        val interfaceName =
            mergeConfigKSAnnotation.arguments.find { it.name?.asString() == "name" }?.value as? String
                ?: return
        val genClassName = interfaceName + "Impl"
        // 文件生成器
        val fileSpecBuilder = FileSpec.builder(
            packageName,
            genClassName
        )
        // 类生成器
        val typeSpecBuilder = TypeSpec.classBuilder(genClassName)
        val constructBuilder = FunSpec.constructorBuilder()
        constructBuilder.addParameter("inflater", ClassName("android.view", "LayoutInflater"))
        constructBuilder.addParameter("type", Class::class.asClassName().parameterizedBy(STAR))
        typeSpecBuilder.primaryConstructor(constructBuilder.build())
        // 1. 获取所有的参数
        // 2. 遍历参数将 idName 和对应的类型名称存入集合中,并记住第一个参数名用于 else 判断
        // 3. 根据属性生成代码

        val firstParamTypeNameStr =
            funDec.parameters[0].type.resolve().declaration.simpleName.asString()

        env.logger.warn("MAP CONTENT START")
        val totalTypeNameSet = HashSet<String>()
        val idToTypeNameMap = HashMap<String, HashSet<String>>()
        funDec.parameters.forEach {
            val typeDec = it.type.resolve().declaration as KSClassDeclaration
            val typeNameStr = typeDec.simpleName.asString()
            totalTypeNameSet.add(typeNameStr)
            typeDec.getDeclaredProperties().forEach { propDec ->
                val set = idToTypeNameMap.getOrPut(propDec.simpleName.asString()) {
                    hashSetOf()
                }
                set.add(typeNameStr)
            }

            // 导入依赖
            fileSpecBuilder.addImport(typeDec.packageName.asString(), typeDec.simpleName.asString())

            val lazyBlock = CodeBlock.builder()
                .add("lazy { ")
                .add("$typeNameStr.inflate(inflater) }")
            val initialProp = PropertySpec.builder("m$typeNameStr", typeDec.toClassName())
                .addModifiers(KModifier.PRIVATE)
                .delegate(lazyBlock.build())
            typeSpecBuilder.addProperty(initialProp.build())

            env.logger.warn(typeDec.getAllProperties().joinToString { "," })
        }

        env.logger.warn("MAP CONTENT")
        env.logger.warn(idToTypeNameMap.toString())

        idToTypeNameMap.forEach { idNameStr, typeNameStrList ->
            val propSpecBuilder = PropertySpec.builder(idNameStr, NOTHING)

            val idNameStr = if (idNameStr == "rootView") "root" else idNameStr
            val whenBlock = CodeBlock.builder()
                .beginControlFlow("when (type)")
            for (eachTypeName in totalTypeNameSet) {
                if (typeNameStrList.contains(eachTypeName)) {
                    whenBlock.addStatement("$eachTypeName::class.java -> m$eachTypeName.$idNameStr")
                } else {
                    whenBlock.addStatement("$eachTypeName::class.java -> null")
                }
            }
            if (typeNameStrList.contains(firstParamTypeNameStr)) {
                whenBlock.addStatement("else -> m$firstParamTypeNameStr.$idNameStr")
            } else {
                whenBlock.addStatement("else -> null")
            }
            whenBlock.endControlFlow()

            propSpecBuilder.initializer(whenBlock.build())
            typeSpecBuilder.addProperty(propSpecBuilder.build())
        }

        val text = fileSpecBuilder
            .addType(typeSpecBuilder.build())
            .build().toString().replace(": Nothing", "")

        val outputStream = env.codeGenerator.createNewFile(
            Dependencies(false),
            packageName,
            genClassName
        )
        val writer = OutputStreamWriter(outputStream)
        writer.write(text)
        writer.close()
    }

    private fun isPrimitiveType(typeReference: KSTypeReference): Boolean {
        val type: KSType = typeReference.resolve()
        val declaration: KSDeclaration = type.declaration
        val simpleName = declaration.simpleName.asString()
        return simpleName in listOf(
            "Byte",
            "Short",
            "Int",
            "Long",
            "Float",
            "Double",
            "Boolean",
            "Char",
            "String"
        )
    }
}