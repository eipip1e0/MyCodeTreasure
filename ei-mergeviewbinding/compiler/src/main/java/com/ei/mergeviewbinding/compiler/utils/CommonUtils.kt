package com.ei.mergeviewbinding.compiler.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

private val VIEW_BINDING_CLASS_NAME = ClassName("androidx.viewbinding", "ViewBinding")
fun KSClassDeclaration.isChildOfViewBinding(): Boolean {
    return this.superTypes.find { it.resolve().toClassName() == VIEW_BINDING_CLASS_NAME } != null
}