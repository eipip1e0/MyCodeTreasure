package com.ei.mergeviewbinding.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class MergeConfig(
    val name: String = "" // 为 "" 时默认选取Merge + 第一个参数类型名作为生成的 class 的名称
)
