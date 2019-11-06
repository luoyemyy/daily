package com.github.luoyemyy.daily.activity.user

import android.app.Application
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.ext.hideKeyboard
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentUserEditBinding
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.UserInfo

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
            mBinding.edtContent.apply {
                setText(it)
                setSelection(it.length)
                requestFocus()
                showSoftInput(this)
            }
        })
        mPresenter.title.observe(this, Observer {
            requireActivity().title = it
        })
        mPresenter.success.observe(this, Observer {
            findNavController().navigateUp()
        })
        mPresenter.loadInit(arguments)
    }

    private fun showSoftInput(view: View) {
        requireActivity().getSystemService(InputMethodManager::class.java)?.apply {
            showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    class Presenter(var mApp: Application) : AbsPresenter(mApp) {

        var type = 0
        val data = MutableLiveData<String>()
        val title = MutableLiveData<String>()
        val success = MutableLiveData<Boolean>()

        override fun loadData(bundle: Bundle?) {
            type = bundle?.getInt("type") ?: 0
            if (type == 0) {
                return success.postValue(true)
            }
            data.value = when (type) {
                1 -> UserInfo.getUser().name
                2 -> UserInfo.getUser().moments
                else -> null
            }
        }

        fun save(content: String) {
            runOnThread {
                when (type) {
                    1 -> UserInfo.getUser().name = content
                    2 -> UserInfo.getUser().moments = content
                }
                postBus(BusEvent.USER_CHANGE)
                success.postValue(true)
            }

        }
    }

}