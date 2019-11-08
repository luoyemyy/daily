package com.github.luoyemyy.daily.activity.user

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.ext.hideKeyboard
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentUserEditBinding
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.UserInfo
import com.github.luoyemyy.daily.util.setToolbarTitle
import com.github.luoyemyy.daily.util.showSoftInput

class UserEditFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentUserEditBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserEditBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            mPresenter.save(mBinding.edtContent.text.toString())
            requireActivity().hideKeyboard()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            if (it.result) {
                findNavController().navigateUp()
            } else {
                setToolbarTitle(requireActivity(), it.title)
                mBinding.edtContent.apply {
                    hint = it.hint
                    setText(it.content)
                    it.content?.apply {
                        setSelection(length)
                    }
                    requestFocus()
                    showSoftInput(requireActivity(), this)
                }
            }
        })
        mPresenter.loadInit(arguments)
    }

    class Presenter(var mApp: Application) : AbsPresenter(mApp) {

        val data = MutableLiveData<UserEdit>()
        private var type = 0
        private val userEdit = UserEdit()


        override fun loadData(bundle: Bundle?) {
            type = bundle?.getInt("type") ?: 0
            if (type == 0) {
                userEdit.result = true
                return data.postValue(userEdit)
            }
            when (type) {
                1 -> {
                    userEdit.content = UserInfo.getUserName(mApp)
                    userEdit.title = mApp.getString(R.string.user_edit_name)
                    userEdit.hint = mApp.getString(R.string.user_edit_name_hint)
                }
                2 -> {
                    userEdit.content = UserInfo.getUserMoments(mApp)
                    userEdit.title = mApp.getString(R.string.user_edit_moments)
                    userEdit.hint = mApp.getString(R.string.user_edit_moments_hint)
                }
            }
            data.postValue(userEdit)
        }

        fun save(content: String) {
            when (type) {
                1 -> UserInfo.setUserName(mApp, content)
                2 -> UserInfo.setUserMoments(mApp, content)
            }
            postBus(BusEvent.USER_CHANGE)
            userEdit.result = true
            data.postValue(userEdit)
        }
    }

}