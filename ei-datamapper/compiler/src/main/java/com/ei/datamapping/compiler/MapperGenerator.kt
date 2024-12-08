package com.ei.datamapping.compiler

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class MapperGenerator(private val env: SymbolProcessorEnvironment) {

    fun generate(resolver: Resolver, mapperTypes: Set<KSClassDeclaration>) {

        // 1. 获取注解类遍历
        //      2. 获取所有的方法遍历
        //          3. 获取入参类型和出参类型
        //          4. 生成实现类方法
        mapperTypes.forEach { mapperClass: KSClassDeclaration ->

            val implClassName = "${mapperClass.simpleName.asString()}Impl"
            // 文件生成器
            val fileSpecBuilder = FileSpec.builder(
                mapperClass.packageName.asString(),
                implClassName
            )

            // 类生成器
            val typeSpecBuilder = TypeSpec.classBuilder(implClassName)

            // 添加默认的非法值声明
            val companionObjectTypeSpec: TypeSpec = TypeSpec.companionObjectBuilder()
                .addProperty(
                    PropertySpec.builder("INVALID_INT", Int::class.asTypeName())
                        .initializer(Int.MIN_VALUE.toString())
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("INVALID_FLOAT", Float::class.asTypeName())
                        .initializer(Float.MIN_VALUE.toString() + "F")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("INVALID_SHORT", Int::class.asTypeName())
                        .initializer(Short.MIN_VALUE.toString())
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("INVALID_BYTE", Int::class.asTypeName())
                        .initializer(Byte.MIN_VALUE.toString())
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("INVALID_STRING", String::class.asTypeName())
                        .initializer("\"null\"")
                        .build()
                )
                .build()

            typeSpecBuilder.addType(companionObjectTypeSpec)

            val mapperFunctions = mapperClass.getDeclaredFunctions()
            mapperFunctions.forEach { convertFun ->
                val srcParam = convertFun.parameters[0]
                val dstType = convertFun.returnType!!

                recurseFunGen(convertFun.simpleName.asString(), typeSpecBuilder, srcParam.type, dstType)
            }

            // 给文件添加类
            fileSpecBuilder.addType(typeSpecBuilder.build())
            fileSpecBuilder.build().writeTo(env.codeGenerator, false)
        }
    }

    // 递归生成对应的转换方法
    // 遍历类中的
    private fun recurseFunGen(
        funName: String,
        typeSpecBuilder: TypeSpec.Builder,
        srcKSTypeRef: KSTypeReference, // Kotlin / Java
        dstKSTypeRef: KSTypeReference, // Kotlin data class only，TODO: @eipip1e0 添加 data class 检查
    ) {
        if (isPrimitiveType(srcKSTypeRef) || isPrimitiveType(dstKSTypeRef)) {
            env.logger.warn("${isPrimitiveType(srcKSTypeRef)} + ${isPrimitiveType(dstKSTypeRef)}")
            return
        }

        val srcDeclaration = srcKSTypeRef.resolve().declaration
        val dstDeclaration = dstKSTypeRef.resolve().declaration
        // 检测非基础数据类型递归生成
        if (srcDeclaration is KSClassDeclaration && dstDeclaration is KSClassDeclaration) {
            // private val mDefaultDstClass = DstClass()
            typeSpecBuilder.addProperty(
                PropertySpec.builder(
                    "mDefault${dstDeclaration.simpleName.asString()}",
                    dstKSTypeRef.toTypeName()
                ).initializer("${dstDeclaration.simpleName.asString()}()").build()
            )

            val dstParams = dstDeclaration.primaryConstructor!!.parameters
            // 方法构建
            typeSpecBuilder.addFunction(
                mapperFunGen(
                    funName,
                    srcKSTypeRef,
                    dstKSTypeRef,
                    dstParams
                )
            )

            val srcProp = srcDeclaration.getAllProperties()
            dstParams.forEach {dstParamType->
                // ksp cannot get the property default value
                val srcType = srcProp.find { it.simpleName == dstParamType.name }
                // 不能为空，为空则说明结构有问题
                recurseFunGen(funName, typeSpecBuilder, srcType!!.type, dstParamType.type)
            }
        }
    }

    private fun mapperFunGen(
        funName: String,
        srcType: KSTypeReference,
        dstType: KSTypeReference,
        dstParams: List<KSValueParameter>
    ): FunSpec {
        // 方法生成器
        // fun funName(src: srcType): dstType
        val funSpecBuilder = FunSpec.builder(funName)
            // 入参
            .addParameter("src", srcType.toTypeName())
            // 出参
            .returns(dstType.toTypeName())

        val dstTypeNameStr = dstType.resolve().declaration.simpleName.asString()

        dstParams.forEach {ksValueParameter ->
            val paramNameStr = ksValueParameter.name!!.asString()
            val paramClassStr = ksValueParameter.type.resolve().declaration.simpleName.asString()
            when (paramClassStr) {
                "Byte" -> {
                    funSpecBuilder.addStatement(
                        replaceDefaultValueToInvalid(dstTypeNameStr, paramNameStr, "INVALID_BYTE")
                    )
                }
                "Short" -> {
                    funSpecBuilder.addStatement(
                        replaceDefaultValueToInvalid(dstTypeNameStr, paramNameStr, "INVALID_SHORT")
                    )
                }
                "Int" -> {
                    funSpecBuilder.addStatement(
                        replaceDefaultValueToInvalid(dstTypeNameStr, paramNameStr, "INVALID_INT")
                    )
                }
                "Float" -> {
                    funSpecBuilder.addStatement(
                        replaceDefaultValueToInvalid(dstTypeNameStr, paramNameStr, "INVALID_FLOAT")
                    )
                }
                "String" -> {
                    funSpecBuilder.addStatement(
                        replaceDefaultValueToInvalid(dstTypeNameStr, paramNameStr, "INVALID_STRING")
                    )
                }
                else -> {
                    if (!isPrimitiveType(ksValueParameter.type)) {
                        funSpecBuilder.addStatement(
                            """
                                val $paramNameStr = if (src.$paramNameStr == null) {
                                  $paramClassStr()
                                } else {
                                  $funName(src.$paramNameStr)
                                }
                            """.trimIndent()
                        )
                    }
                }
            }
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append(
            """
                val dst = $dstTypeNameStr(

            """.trimIndent()
        )
        dstParams.forEach {ksValueParameter->
            val paramNameStr = ksValueParameter.name!!.asString()
            stringBuilder.append("$paramNameStr = $paramNameStr,\n")
        }
        stringBuilder.append(")")

        funSpecBuilder.addStatement(
            stringBuilder.toString()
        )
        funSpecBuilder.addStatement("""
            return dst
        """.trimIndent())
        return funSpecBuilder.build()
    }

    private fun replaceDefaultValueToInvalid(
        dstTypeNameStr: String,
        paramNameStr: String,
        invalidValueStr: String
    ): String {
        return """
            val $paramNameStr = if (src.$paramNameStr == null || src.$paramNameStr == mDefault$dstTypeNameStr.$paramNameStr) {
              $invalidValueStr
            } else {
              src.$paramNameStr
            }
        """.trimIndent()
    }

    private fun isPrimitiveType(typeReference: KSTypeReference): Boolean {

        val type: KSType = typeReference.resolve()
        val declaration: KSDeclaration = type.declaration
        val simpleName = declaration.simpleName.asString()
        return simpleName in listOf("Byte", "Short", "Int", "Long", "Float", "Double", "Boolean", "Char", "String")
        // TODO: @eipip1e0 测试 JAVA 是否可以正常使用
    }
}