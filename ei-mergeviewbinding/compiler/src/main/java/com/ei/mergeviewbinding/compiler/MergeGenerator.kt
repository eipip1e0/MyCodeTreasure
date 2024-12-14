package com.ei.mergeviewbinding.compiler

import com.ei.mergeviewbinding.annotation.MergeConfig
import com.ei.mergeviewbinding.compiler.model.MergeClassData
import com.ei.mergeviewbinding.compiler.model.MergePropData
import com.ei.mergeviewbinding.compiler.model.getChildMergeClassName
import com.ei.mergeviewbinding.compiler.utils.isChildOfViewBinding
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
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

/**
 * 踩坑 onEach 需要有结尾操作符类似 flow, 因为这也是一种流
 */
class MergeGenerator(private val env: SymbolProcessorEnvironment) {

    fun generate(resolver: Resolver, mapperTypes: Set<KSClassDeclaration>) {
        mapperTypes.forEach classForEach@{ mapperClass: KSClassDeclaration ->
            // 获取注解类中的所有方法
            // 获取方法注解 name 作为名称
            // 获取方法的参数类型
            // 指定binding merge 操作
            mapperClass.getDeclaredFunctions().forEach { funDec ->
                val mergeConfigAnnotation =
                    funDec.annotations
                        .find { it.shortName.asString() == MergeConfig::class.simpleName }
                val customMergeBindingName =
                    mergeConfigAnnotation
                        ?.arguments
                        ?.find { it.name?.asString() == "name" }
                        ?.value as? String

                val rootClassNameStr = customMergeBindingName
                // 注解没有定义的话就取所有参与融合的 Binding 的前 3 个字符拼接后作为 MergeBinding 的名称
                    ?: (funDec.parameters
                        .map { it.type.resolve().declaration.simpleName.asString() }
                        .map { it.substring(0, 3) }
                        .toList().joinToString(separator = "") + "MergeBinding")
                val rootClassName = ClassName(funDec.packageName.asString(), rootClassNameStr)
                val rootMergeClassData = MergeClassData("", rootClassName)
                // 融合方法生成所需的属性
                // key: 父类名称 + idName
                // value: ClassName 用于 when 语句判断
                val mergeClassMetaMap =
                    HashMap<MergeClassData, HashSet<MergePropData>>()
                // key: idName value: 包含此 id 的 root binding
                val allRootProps = HashSet<MergePropData>()
                val allRootBindings = HashSet<ClassName>()
                mergeClassMetaMap[rootMergeClassData] = allRootProps

                // 遍历参数中的所有 ViewBinding 递归查找 Binding 放入集合中
                funDec.parameters
                    .map {
                        it.type.resolve().declaration as KSClassDeclaration
                    }.onEach { rootBindingDec ->
                        // 记录所有的 root binding
                        val rootBindingClassName = rootBindingDec.toClassName()
                        allRootBindings.add(rootBindingClassName)
                        allRootProps.add(
                            MergePropData(
                            rootBindingClassName,
                            rootBindingDec.getDeclaredProperties().map { it.simpleName.asString() },
                            rootBindingDec.getDeclaredProperties()
                                .filter { (it.type.resolve().declaration as KSClassDeclaration).isChildOfViewBinding() }
                                .map { MergeClassData(it.simpleName.asString(), rootClassName) }
                            )
                        )

                        env.logger.warn("==> onEach parameter getDeclaredProperties${rootBindingDec.getDeclaredProperties().map { it.simpleName.asString() }.toCollection(ArrayList())}")
                        recursiveFindMergeClassMetaData(
                            rootBindingClassName,
                            rootClassName,
                            mergeClassMetaMap,
                            rootBindingDec.getDeclaredProperties()
                        )
                    }

                env.logger.warn("==> merge bindings metadata $mergeClassMetaMap")

                val defaultBindingSimpleNameStr =
                    funDec.parameters[0].type.resolve().toClassName()
                for (entry in mergeClassMetaMap) {
                    genBindingMerge(
                        allRootBindings,
                        entry.key,
                        entry.value,
                        defaultBindingSimpleNameStr
                    )
                }
            }
        }
    }

    private fun recursiveFindMergeClassMetaData(
        rootBindingClassName: ClassName, // root binding 在递归时不会变
        fatherClassName: ClassName, // father 参数类名
        // id + fatherClassName 类名
        mergeClassMetaMap: HashMap<MergeClassData, HashSet<MergePropData>>,
        fatherProps: Sequence<KSPropertyDeclaration> // fatherClassName 对应类的属性
    ) {
        fatherProps.forEach { prop ->
            // 属性类型是否是 ViewBinding 的子类
            val fatherPropClassDec = prop.type.resolve().declaration as KSClassDeclaration

            if (fatherPropClassDec.isChildOfViewBinding()) {
                // 如果是则加入需要生成 merge class 的集合中，并递归查询子属性
                val idName = prop.simpleName.asString()
                val childMergeClassData = MergeClassData(
                    idName = idName,
                    fatherClassName
                )
                // 获取类
                mergeClassMetaMap
                    .getOrPut(childMergeClassData) {
                        hashSetOf()
                    }.add(MergePropData(
                        rootBindingClassName,
                        fatherPropClassDec.getDeclaredProperties().map { it.simpleName.asString() },
                        fatherPropClassDec.getDeclaredProperties()
                            .filter { (it.type.resolve().declaration as KSClassDeclaration).isChildOfViewBinding() }
                            .map { MergeClassData(it.simpleName.asString(), fatherClassName) }
                    ))

                recursiveFindMergeClassMetaData(
                    rootBindingClassName,
                    getChildMergeClassName(childMergeClassData),
                    mergeClassMetaMap,
                    fatherPropClassDec.getDeclaredProperties()
                )
            }
        }
    }

    private fun genBindingMerge(
        allRootBindings: HashSet<ClassName>,
        // 类元
        mergeClassData: MergeClassData,
        // 类中的属性以及包含属性的 root binding ClassName
        mergePropDataSet: HashSet<MergePropData>,
        defaultBindingClassName: ClassName
    ) {
        // 文件生成器
        val fileSpecBuilder = FileSpec.builder(
            getChildMergeClassName(mergeClassData).packageName,
            getChildMergeClassName(mergeClassData).simpleName
        )
        // 类生成器
        val typeSpecBuilder = TypeSpec.classBuilder(getChildMergeClassName(mergeClassData))
        val constructBuilder = FunSpec.constructorBuilder()
        if (mergeClassData.idName.isEmpty()) {
            constructBuilder.addParameter("inflater", ClassName("android.view", "LayoutInflater"))
        } else {
            constructBuilder.addParameter("parent", mergeClassData.fatherClassName)
        }
        constructBuilder.addParameter("type", Class::class.asClassName().parameterizedBy(STAR))
        typeSpecBuilder.primaryConstructor(constructBuilder.build())
        // 遍历出所有的 binding 集合，以 R id 为 key，binding KSClassDeclaration 为 value
        // 执行 merge 方法

        // 1. 获取所有的参数
        // 2. 遍历参数将 idName 和对应的类型名称存入集合中,并记住第一个参数名用于 else 判断
        // 3. 根据属性生成代码，如果是普通类型则直接 when 判断
        //      否则是肯定是 binding 类型，则直接调用 merge 类的


        // 类 lazy 属性生成
        allRootBindings.forEach { rootBindingClassName ->
            // 导入依赖
            fileSpecBuilder.addImport(
                rootBindingClassName.packageName,
                rootBindingClassName.simpleName
            )
            if (mergePropDataSet.find { it.rootBindingClassName == rootBindingClassName} == null) {
                return@forEach
            }

            val lazyBlock = CodeBlock.builder().beginControlFlow("lazy")

            if (mergeClassData.idName.isEmpty()) {
                lazyBlock.addStatement("android.util.Log.i(\"KSP Merge\", \"init layout $rootBindingClassName\")".replace(" ", "·"))
                lazyBlock.addStatement("${rootBindingClassName.simpleName}.inflate(inflater)")
            } else {
                lazyBlock.addStatement("parent._get${rootBindingClassName.simpleName}().${mergeClassData.idName}")
            }
            lazyBlock.endControlFlow()
            val initialProp = PropertySpec.builder("_m${rootBindingClassName.simpleName}", NOTHING)
                .addModifiers(KModifier.PRIVATE)
                .delegate(lazyBlock.build())
            typeSpecBuilder.addProperty(initialProp.build())
            typeSpecBuilder.addFunction(
                FunSpec.builder("_get${rootBindingClassName.simpleName}")
                    .addModifiers(KModifier.INTERNAL)
                    .addCode("return _m${rootBindingClassName.simpleName}")
                    .build()
            )
        }

        // rootBindingChildId key value 映射反转
        val normalIdToClassNameMap = HashMap<String, HashSet<ClassName>>()
        val bindingIdSet = HashSet<MergeClassData>()
        for (mergePropData in mergePropDataSet) {
            for (idName in mergePropData.idNameSeq) {
                // 不是 binding
                val bindingId = mergePropData.bindingIdSeq.find { it.idName == idName }
                if (bindingId == null) {
                    val set = normalIdToClassNameMap.getOrPut(idName) { hashSetOf() }
                    set.add(mergePropData.rootBindingClassName)
                } else {
                    bindingIdSet.add(bindingId)
                }
            }
        }

        normalIdToClassNameMap.forEach { (idNameStr, classNameSet) ->
            val propSpecBuilder = PropertySpec.builder(idNameStr, NOTHING)

            val idNameStr = if (idNameStr == "rootView") "root" else idNameStr
            val whenBlock = CodeBlock.builder()
                .beginControlFlow("when (type)")
            for (eachTypeName in allRootBindings) {
                // 属性中包含此 idNameStr 的 binding 则调用此 binding 的属性
                // 不包含的则直接返回 null
                if (classNameSet.contains(eachTypeName)) {
                    whenBlock.addStatement("${eachTypeName.simpleName}::class.java -> _m${eachTypeName.simpleName}.$idNameStr")
                } else {
                    whenBlock.addStatement("${eachTypeName.simpleName}::class.java -> null")
                }
            }

            // binding merge 的第一个方案参数类型中的属性，如果包含了此 idName，那么 else 就使用此类型
            // 否则为 null
            if (classNameSet.contains(defaultBindingClassName)) {
                whenBlock.addStatement("else -> _m${defaultBindingClassName.simpleName}.$idNameStr")
            } else {
                whenBlock.addStatement("else -> null")
            }
            whenBlock.endControlFlow()

            propSpecBuilder.initializer(whenBlock.build())
            typeSpecBuilder.addProperty(propSpecBuilder.build())
        }

        bindingIdSet.forEach { bindingId ->
            typeSpecBuilder.addProperty(
                PropertySpec.builder(bindingId.idName, getChildMergeClassName(bindingId))
                    .initializer("${getChildMergeClassName(bindingId).simpleName}(this, type)")
                    .build()
            )
        }

        val text = fileSpecBuilder
            .addType(typeSpecBuilder.build())
            .build().toString().replace(": Nothing", "")

        val outputStream = env.codeGenerator.createNewFile(
            Dependencies(false),
            getChildMergeClassName(mergeClassData).packageName,
            getChildMergeClassName(mergeClassData).simpleName
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