package com.github.luoyemyy.daily.activity.read

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.LoadParams
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupLinear
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentReadBinding
import com.github.luoyemyy.daily.databinding.FragmentReadRecyclerBinding
import com.github.luoyemyy.daily.databinding.FragmentReadSeekRecyclerBinding
import com.github.luoyemyy.daily.db.RecordDao
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.AppCache
import com.github.luoyemyy.daily.util.setToolbarTitle
import java.util.*

class ReadFragment : OverrideMenuFragment() {
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
        Adapter().also {
            it.setup(this, mPresenter.listLiveData)
            mBinding.recyclerView.setupLinear(it)
        }
        SeekAdapter().also {
            it.setup(this, mPresenter.seekLiveData)
            mBinding.recyclerViewSeek.setupLinear(it)
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
        mPresenter.setTitle()
        mPresenter.loadInit(arguments)
        mPresenter.seekLiveData.loadStart()
    }

    inner class Adapter : FixedAdapter<ReadDay, FragmentReadRecyclerBinding>() {
        override fun bindContentViewHolder(binding: FragmentReadRecyclerBinding, data: ReadDay?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentReadRecyclerBinding {
            return FragmentReadRecyclerBinding.inflate(layoutInflater, parent, false)
        }
    }

    inner class SeekAdapter : FixedAdapter<SeekIndex, FragmentReadSeekRecyclerBinding>() {
        override fun bindContentViewHolder(binding: FragmentReadSeekRecyclerBinding, data: SeekIndex?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentReadSeekRecyclerBinding {
            return FragmentReadSeekRecyclerBinding.inflate(layoutInflater, parent, false)
        }

        override fun onItemViewClick(binding: FragmentReadSeekRecyclerBinding, vh: VH<*>, view: View) {
            getItem(vh.adapterPosition)?.apply {
                val position = mPresenter.findIndexByMonth(this.month)
                if (position >= 0) {
                    mBinding.recyclerView.scrollToPosition(position)
                }
            }
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        private val recordDao: RecordDao = getRecordDao()
        private var year = Calendar.getInstance().get(Calendar.YEAR)
        var title = MutableLiveData<String>()
        var selectYear = MutableLiveData<List<Int>>()
        val listLiveData = object : ListLiveData<ReadDay>() {
            override fun getData(loadParams: LoadParams): List<ReadDay>? {
                return getDatas()
            }
        }
        var seekLiveData = object : ListLiveData<SeekIndex>() {
            override fun getData(loadParams: LoadParams): List<SeekIndex>? {
                return getMonths()
            }
        }

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart()
        }


        fun findIndexByMonth(month: Int): Int {
            listLiveData.value?.items?.forEachIndexed { index, dataItem ->
                if ((dataItem.data is ReadDay) && dataItem.data?.month == month) {
                    return index
                }
            }
            return -1
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
            seekLiveData.loadRefresh()
        }

        private fun getMonths(): List<SeekIndex>? {
            return recordDao.getMonthByYear(AppCache.getUserId(mApp), year)?.let { records ->
                records.map { SeekIndex(it.month) }
            }
        }

        private fun getDatas(): List<ReadDay>? {
            return recordDao.getByYear(AppCache.getUserId(mApp), year)?.let { records ->
                records.map { ReadDay(it.year, it.month, it.day, it.content) }
            }
        }
    }
}