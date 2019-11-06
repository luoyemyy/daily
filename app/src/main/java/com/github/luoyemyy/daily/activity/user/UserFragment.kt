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
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentUserBinding
import com.github.luoyemyy.daily.db.entity.User
import com.github.luoyemyy.daily.util.UserInfo

class UserFragment : OverrideMenuFragment(), View.OnClickListener {

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
        mPresenter.loadInit(arguments)
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

    class Presenter(mApp: Application) : AbsPresenter(mApp) {

        val data = MutableLiveData<User>()

        override fun loadData(bundle: Bundle?) {
            data.value = UserInfo.getUser()
        }
    }

}