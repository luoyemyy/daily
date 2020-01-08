package com.github.luoyemyy.daily.activity.user

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentUserBinding
import com.github.luoyemyy.daily.util.AppCache
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.User

class UserFragment : OverrideMenuFragment(), View.OnClickListener, BusResult {

    private lateinit var mBinding: FragmentUserBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            mBinding.entity = it
        })
        mBinding.txtName.setOnClickListener(this)
        mBinding.txtMoments.setOnClickListener(this)
        setBus(this, BusEvent.USER_CHANGE)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        mPresenter.fillData()
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.txtName -> bundleOf("type" to 1)
            mBinding.txtMoments -> bundleOf("type" to 2)
            else -> null
        }?.apply {
            findNavController().navigate(R.id.action_user_to_userEdit, this)
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        val data = MutableLiveData<User>()

        override fun loadData(bundle: Bundle?) {
            fillData()
        }

        fun fillData() {
            data.value = AppCache.getUser(mApp)
        }
    }

}