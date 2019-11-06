package com.github.luoyemyy.daily.activity.user

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter
import com.github.luoyemyy.daily.databinding.FragmentUserBinding
import com.github.luoyemyy.daily.databinding.FragmentUserEditBinding
import com.github.luoyemyy.daily.db.entity.User
import com.github.luoyemyy.daily.util.UserInfo

class UserEditFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentUserEditBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserEditBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
        })
        mPresenter.loadInit(arguments)
    }

    class Presenter(mApp: Application) : AbsPresenter(mApp) {

        val data = MutableLiveData<User>()

        override fun loadData(bundle: Bundle?) {
            data.value = UserInfo.getUser()
        }
    }

}