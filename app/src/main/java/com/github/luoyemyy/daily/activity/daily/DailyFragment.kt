package com.github.luoyemyy.daily.activity.daily

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.ext.hideKeyboard
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentDailyBinding
import com.github.luoyemyy.daily.db.entity.Record
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.*

class DailyFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentDailyBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentDailyBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.daily, menu)
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
                setText(it.content)
                setSelection(it.content.length)
                requestFocus()
                showSoftInput(requireActivity(), this)
            }
        })
        mPresenter.title.observe(this, Observer {
            setToolbarTitle(requireActivity(), requireContext().getString(R.string.daily_title, it))
        })
        mPresenter.success.observe(this, Observer {
            findNavController().navigateUp()
        })
        mPresenter.loadInit(arguments)
    }

    class Presenter(var mApp: Application) : AbsPresenter(mApp) {

        private val recordDao = getRecordDao()
        private lateinit var record: Record

        val data = MutableLiveData<Record>()
        val title = MutableLiveData<String>()
        val success = MutableLiveData<Boolean>()

        override fun loadData(bundle: Bundle?) {
            val id = bundle?.getLong("id") ?: 0L
            val y = bundle?.getInt("y")
            val m = bundle?.getInt("m")
            val d = bundle?.getInt("d")
            if (y == null || m == null || d == null) {
                mApp.toast(R.string.daily_tip_date)
                success.postValue(true)
                return
            }
            title.value = "$y-${formatDateNum(m)}-${formatDateNum(d)}"
            runOnThread {
                record = recordDao.get(id) ?: Record(0, UserInfo.getUserId(mApp), d, m, y, "")
                data.postValue(record)
            }
        }

        fun save(content: String) {
            if (content.isEmpty()) {
                mApp.toast(R.string.daily_tip_empty_text)
                return
            }
            runOnThread {
                record.content = content
                if (record.id == 0L) {
                    val rowId = recordDao.insert(record)
                    recordDao.getByRowId(rowId)?.apply {
                        record.id = id
                    }
                } else {
                    recordDao.update(record)
                }
                backupDay(record)
                postBus(BusEvent.DAILY_SAVE, longValue = record.id)
                success.postValue(true)
            }

        }
    }

}