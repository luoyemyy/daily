package com.github.luoyemyy.daily.activity.read

import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentReadBinding
import com.github.luoyemyy.daily.databinding.FragmentReadRecyclerBinding
import com.github.luoyemyy.daily.db.RecordDao
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.AppCache
import com.github.luoyemyy.daily.util.setToolbarTitle
import java.util.*

class ReadFragment : OverrideMenuFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentReadBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentReadBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.read, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.select) {
            mPresenter.selectYearDialog()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
        }
        mPresenter.title.observe(this, androidx.lifecycle.Observer {
            setToolbarTitle(requireActivity(), it)
        })
        mPresenter.selectYear.observe(this, androidx.lifecycle.Observer { list ->
            list?.apply {
                AlertDialog.Builder(requireContext()).setItems(list.map { getString(R.string.year, it) }.toTypedArray()) { _, which ->
                    mPresenter.selectYear(list[which])
                }.show()
            }
        })
        mBinding.seek.seekLayout.forEach {
            it.setOnClickListener(this)
        }
        mPresenter.setTitle()
        mPresenter.loadInit(arguments)
    }

    override fun onClick(v: View?) {
        (v as? TextView)?.text?.toString()?.toIntOrNull()?.apply {
            val position = mPresenter.findIndexByMonth(this)
            if (position >= 0) {
                mBinding.recyclerView.scrollToPosition(position)
            }
        }
    }

    inner class Adapter : FixedAdapter<ReadDay, FragmentReadRecyclerBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_read_recycler
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {

        var title = MutableLiveData<String>()
        var selectYear = MutableLiveData<List<Int>>()
        private val recordDao: RecordDao = getRecordDao()
        private var year = Calendar.getInstance().get(Calendar.YEAR)

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<ReadDay>? {
            return getDatas()
        }

        fun findIndexByMonth(month: Int): Int {
            var position = -1
            listLiveData.value?.data?.forEachIndexed { index, dataItem ->
                if ((dataItem is ReadDay) && dataItem.month == month) {
                    position = index
                    return@forEachIndexed
                }
            }
            return position
        }

        fun setTitle() {
            title.value = mApp.getString(R.string.read_title, year)
        }

        fun selectYearDialog() {
            runOnThread {
                selectYear.postValue(recordDao.getGroupYears(AppCache.getUserId(mApp))?.map { it.year })
            }
        }

        fun selectYear(year: Int) {
            this.year = year
            setTitle()
            listLiveData.loadRefresh()
        }

        private fun getDatas(): List<ReadDay>? {
            return recordDao.getByYear(AppCache.getUserId(mApp), year)?.let { records ->
                records.map { ReadDay(it.year, it.month, it.day, it.content) }
            }
        }
    }
}