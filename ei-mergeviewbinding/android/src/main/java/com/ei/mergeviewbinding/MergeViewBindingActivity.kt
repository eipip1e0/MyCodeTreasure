package com.ei.mergeviewbinding

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ei.mergeviewbinding.databinding.FirstTypeLayoutBinding
import com.ei.mergeviewbinding.databinding.SecondTypeLayoutBinding
import com.ei.mergeviewbinding.merge.FirstSecondTypeLayoutBindingImpl

class MergeViewBindingActivity : AppCompatActivity() {
    private lateinit var mBinding: FirstSecondTypeLayoutBindingImpl
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding =
            FirstSecondTypeLayoutBindingImpl(
                layoutInflater,
                FirstTypeLayoutBinding::class.java
            )
        setContentView(mBinding.rootView)
        mBinding.include1
    }
}