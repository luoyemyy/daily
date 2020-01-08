package com.github.luoyemyy.daily.activity.about

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.daily.databinding.FragmentAboutBinding

class AboutFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentAboutBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentAboutBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            mBinding.entity = it
        })
        mPresenter.loadInit(arguments)
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        val data = MutableLiveData<String>()

        override fun loadData(bundle: Bundle?) {
            mApp.packageManager.getPackageInfo(mApp.packageName, 0)?.apply {
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    longVersionCode
                } else {
                    versionCode.toLong()
                }
                data.value = "v$versionName.$versionCode"
            }
        }
    }

}