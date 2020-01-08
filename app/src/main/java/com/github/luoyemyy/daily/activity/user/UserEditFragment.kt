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
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentUserEditBinding
import com.github.luoyemyy.daily.util.AppCache
import com.github.luoyemyy.daily.util.BusEvent
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
            postBus(BusEvent.USER_CHANGE)
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

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

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
                    userEdit.content = AppCache.getUserName(mApp)
                    userEdit.title = mApp.getString(R.string.user_edit_name)
                    userEdit.hint = mApp.getString(R.string.user_edit_name_hint)
                }
                2 -> {
                    userEdit.content = AppCache.getUserMoments(mApp)
                    userEdit.title = mApp.getString(R.string.user_edit_moments)
                    userEdit.hint = mApp.getString(R.string.user_edit_moments_hint)
                }
            }
            data.postValue(userEdit)
        }

        fun save(content: String) {
            when (type) {
                1 -> AppCache.setUserName(mApp, content)
                2 -> AppCache.setUserMoments(mApp, content)
            }
            userEdit.result = true
            data.postValue(userEdit)
        }
    }

}