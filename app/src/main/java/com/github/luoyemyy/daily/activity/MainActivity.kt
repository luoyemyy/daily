package com.github.luoyemyy.daily.activity

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.aclin.ext.autoCloseKeyboardAndClearFocus
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        autoCloseKeyboardAndClearFocus(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mBinding.toolbar)
        mBinding.toolbar.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }
}
