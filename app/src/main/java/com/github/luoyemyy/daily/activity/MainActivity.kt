package com.github.luoyemyy.daily.activity

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.aclin.ext.autoCloseKeyboardAndClearFocus
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAppBarConfiguration: AppBarConfiguration

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        autoCloseKeyboardAndClearFocus(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(mBinding.toolbar)
        findNavController(R.id.nav_host_fragment).apply {
            mAppBarConfiguration = AppBarConfiguration(this.graph, mBinding.drawerLayout)
            setupActionBarWithNavController(this, mAppBarConfiguration)
            mBinding.navView.setupWithNavController(this)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(mAppBarConfiguration) || super.onSupportNavigateUp()
    }
}
