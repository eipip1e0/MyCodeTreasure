package com.ei.treasure

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ei.treasure.adapter.ActivityAdapter
import com.ei.treasure.databinding.ActivityMainBinding
import com.ei.treasure.utils.GetActivityClazzListUtil

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 获取 App 中所有的 Activity 并实现跳转功能
        val activityClassNameList = GetActivityClazzListUtil.getActivityClazzList(this).apply {
            remove(MainActivity::class.java.name)
            Log.i(TAG, "onCreate, all activities without MainActivity: $this")
        }
        mBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ActivityAdapter(activityClassNameList)
        }
    }
}