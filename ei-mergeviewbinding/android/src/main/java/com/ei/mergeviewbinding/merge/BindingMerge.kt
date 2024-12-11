package com.ei.mergeviewbinding.merge

import com.ei.mergeviewbinding.annotation.Merge
import com.ei.mergeviewbinding.annotation.MergeConfig
import com.ei.mergeviewbinding.databinding.FirstTypeLayoutBinding
import com.ei.mergeviewbinding.databinding.SecondTypeLayoutBinding

@Merge
interface BindingMerge {
    @MergeConfig(name = "FirstSecondTypeLayoutBinding")
    fun merge(first: FirstTypeLayoutBinding, second: SecondTypeLayoutBinding)
}