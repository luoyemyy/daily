package com.github.luoyemyy.daily.activity

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.addBus
import com.github.luoyemyy.aclin.ext.autoCloseKeyboardAndClearFocus
import com.github.luoyemyy.aclin.ext.hideKeyboard
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.ActivityMainBinding
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.AppCache

class MainActivity : AppCompatActivity(), BusResult, View.OnClickListener {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private var mHeadView: View? = null

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        autoCloseKeyboardAndClearFocus(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mNavController = findNavController(R.id.nav_host_fragment)
        mAppBarConfiguration = AppBarConfiguration(mNavController.graph, mBinding.drawerLayout)
        setSupportActionBar(mBinding.toolbar)
        setupActionBarWithNavController(mNavController, mAppBarConfiguration)
        mBinding.navView.setupWithNavController(mNavController)
        mHeadView = mBinding.navView.getHeaderView(0)
        mHeadView?.setOnClickListener(this)
        mBinding.drawerLayout.addDrawerListener(drawerListener)
        setUserInfo()
        addBus(this, BusEvent.USER_CHANGE, this)
    }

    private val drawerListener = object : DrawerLayout.SimpleDrawerListener() {
        private var checkKeyboard: Boolean = true
        override fun onDrawerStateChanged(newState: Int) {
            if (newState == DrawerLayout.STATE_IDLE) {
                checkKeyboard = true
            } else if (newState == DrawerLayout.STATE_DRAGGING) {
                if (checkKeyboard) {
                    checkKeyboard = false
                    hideKeyboard()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        mNavController.navigate(R.id.action_global_user)
        mBinding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun setUserInfo() {
        mHeadView?.apply {
            findViewById<TextView>(R.id.txtName).text = AppCache.getUserName(this@MainActivity)
            findViewById<TextView>(R.id.txtMoments).text = AppCache.getUserMoments(this@MainActivity)
        }
    }

    override fun busResult(msg: BusMsg) {
        setUserInfo()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(mAppBarConfiguration) || super.onSupportNavigateUp()
    }
}
