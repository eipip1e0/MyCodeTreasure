package com.ei.mergeviewbinding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ei.mergeviewbinding.databinding.FirstTypeLayoutBinding
import com.ei.mergeviewbinding.merge.FirstSecondTypeLayoutBinding

class MergeViewBindingActivity : AppCompatActivity() {
    private lateinit var mBinding: FirstSecondTypeLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            FirstSecondTypeLayoutBinding(
                layoutInflater,
                FirstTypeLayoutBinding::class.java
            )
        setContentView(mBinding.rootView)
    }
}