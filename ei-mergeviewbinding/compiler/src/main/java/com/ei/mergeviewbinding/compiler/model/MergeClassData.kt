package com.ei.mergeviewbinding.compiler.model

import com.squareup.kotlinpoet.ClassName

/**
 * 融合方法生成所需的属性
 *
 * @param idName MergeBindings 的 id
 * @param fatherClassName 父类名称
 *
 */
data class MergeClassData(
    val idName: String, // 类型为 ViewBinding 的 id 名
    val fatherClassName: ClassName // 此 id 拥有者的名称
)

data class MergePropData(
    val rootBindingClassName: ClassName, // 类型为 ViewBinding 的 id 名
    val idNameSeq: Sequence<String>, // 此 id 拥有者的名称
    val bindingIdSeq: Sequence<MergeClassData>, // 是 binding 的 id
)

/**
 * 父类名与 idName 可以作为子类的为一名称，此外 layout 不存在循环依赖问题，因为 inflate 不会成功
 * 因此递归查找也无需考虑循环依赖导致的无穷递归
 *
 * TODO: @eipip1e0 确认一下 inflate 是否可以重复 include 的 layout 里面是否可以 include 自己 （循环依赖）
 */
fun getChildMergeClassName(mergeClassData: MergeClassData): ClassName {
    val idName = mergeClassData.idName
    if (idName.isEmpty()) {
        // 是根节点，直接返回名称
        return mergeClassData.fatherClassName
    }

    val fatherClassName = mergeClassData.fatherClassName
    return ClassName(
        fatherClassName.packageName,
        fatherClassName.simpleName.replace(
            "Merge_",
            ""
        ) + idName.replaceFirstChar(Char::titlecase) + "Merge_"
    )
}