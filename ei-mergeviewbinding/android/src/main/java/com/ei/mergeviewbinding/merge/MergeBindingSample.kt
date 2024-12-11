package com.ei.mergeviewbinding.merge

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.ei.mergeviewbinding.databinding.FirstTypeLayoutBinding
import com.ei.mergeviewbinding.databinding.SecondTypeLayoutBinding

/**
 * TODO: 解决 binding 嵌套的问题
 */
class MergeBindingSample(inflater: LayoutInflater, type: Class<in ViewBinding>) {

    private val mFirstTypeLayoutBinding by lazy { FirstTypeLayoutBinding.inflate(inflater) }
    private val mSecondTypeLayoutBinding by lazy { SecondTypeLayoutBinding.inflate(inflater) }

    // idName + 类型名
    // else 用第一个就行
    private val mMap = HashMap<String, List<String>>()
    /**
     * 便利所有的属性存到 Map 集合中
     * 
     * data class (
     *  type: KSClassDeclaration
     *  
     * )
     */
    private val rootView = when (type) {
        SecondTypeLayoutBinding::class.java -> mSecondTypeLayoutBinding.root
        else -> mFirstTypeLayoutBinding.root
    }
    val btn1 = when (type) {
        SecondTypeLayoutBinding::class.java -> mSecondTypeLayoutBinding.btn1
        else -> mFirstTypeLayoutBinding.btn1
    }
    val include1 = when (type) {
        SecondTypeLayoutBinding::class.java -> mSecondTypeLayoutBinding.include1
        else -> mFirstTypeLayoutBinding.include1
    }
    val innerBtn1 = when (type) {
        SecondTypeLayoutBinding::class.java -> mSecondTypeLayoutBinding.innerBtn1
        else -> mFirstTypeLayoutBinding.innerBtn1
    }
    val innerTv1 = when (type) {
        SecondTypeLayoutBinding::class.java -> mSecondTypeLayoutBinding.innerTv1
        else -> mFirstTypeLayoutBinding.innerTv1
    }
    val tv1 = when (type) {
        SecondTypeLayoutBinding::class.java -> mSecondTypeLayoutBinding.tv1
        else -> mFirstTypeLayoutBinding.tv1
    }
}