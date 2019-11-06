package com.github.luoyemyy.daily.activity

import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus
import com.github.luoyemyy.aclin.ext.autoCloseKeyboardAndClearFocus
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.ActivityMainBinding
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.UserInfo

class MainActivity : AppCompatActivity(), BusResult {

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
        setUserInfo()
        setBus(this, BusEvent.USER_CHANGE, this)
    }

    private fun setUserInfo(){
        mBinding.navView.getHeaderView(0)?.apply {
            findViewById<TextView>(R.id.txtName).text = UserInfo.getUser().name
            findViewById<TextView>(R.id.txtMoments).text = UserInfo.getUser().moments
        }
    }

    override fun busResult(msg: BusMsg) {
        setUserInfo()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(mAppBarConfiguration) || super.onSupportNavigateUp()
    }
}
