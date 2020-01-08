package com.github.luoyemyy.daily.activity.backup.month

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.*
import com.github.luoyemyy.aclin.ext.confirm
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.activity.backup.year.BackupYear
import com.github.luoyemyy.daily.databinding.FragmentBackupMonthBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupMonthRecyclerBinding
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.setToolbarTitle
import com.github.luoyemyy.daily.util.syncYear

class BackupMonthFragment : OverrideMenuFragment(), BusResult {
    private lateinit var mBinding: FragmentBackupMonthBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupMonthBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.backup_sync, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sync) {
            requireActivity().confirm(title = getString(R.string.backup_import), message = getString(R.string.backup_import_tip, mPresenter.getTipName()), ok = {
                mPresenter.syncAll()
            })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.busLiveData = getBusLiveData()
        Adapter().also {
            it.setup(this, mPresenter.listLiveData)
            mBinding.recyclerView.apply {
                adapter = it
                setHasFixedSize(true)
            }
        }
        mPresenter.title.observe(this, Observer {
            setToolbarTitle(requireActivity(), it)
        })
        setBus(this, BusEvent.IMPORT_DAY)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        when (msg.event) {
            BusEvent.IMPORT_DAY -> {
                mPresenter.updateItems(msg.intValue)
            }
        }
    }

    inner class Adapter : FixedAdapter<BackupMonth, FragmentBackupMonthRecyclerBinding>() {

        override fun bindContentViewHolder(binding: FragmentBackupMonthRecyclerBinding, data: BackupMonth?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentBackupMonthRecyclerBinding {
            return FragmentBackupMonthRecyclerBinding.inflate(layoutInflater, parent, false)
        }

        override fun onItemViewClick(binding: FragmentBackupMonthRecyclerBinding, vh: VH<*>, view: View) {
            getItem(vh.adapterPosition)?.apply {
                findNavController().navigate(R.id.action_backupMonth_to_backupDay, bundleOf("month" to this))
            }
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        val listLiveData = ListLiveData<BackupMonth>()
        var busLiveData: BusLiveData? = null
        val title = MutableLiveData<String>()
        private var backupYear: BackupYear? = null

        override fun loadData(bundle: Bundle?) {
            bundle?.getParcelable<BackupYear>("year")?.also {
                title.postValue(mApp.getString(R.string.backup_manager_prefix) + mApp.getString(R.string.backup_manager_year, it.year))
                backupYear = it
                listLiveData.loadStart(it.months)
            }
        }

        fun getTipName(): String {
            return mApp.getString(R.string.backup_manager_year, backupYear?.year ?: 0)
        }

        fun updateItems(month: Int) {
            updateItems { it.month == month }
        }

        private fun updateItems(condition: (BackupMonth) -> Boolean) {
            listLiveData.itemChange { list, _ ->
                list?.forEach {
                    it.data?.apply {
                        if (condition(this)) {
                            countNotSync = 0
                            days?.forEach { day ->
                                day.sync = true
                            }
                            it.hasPayload()
                        }
                    }
                }
                true
            }
        }

        fun syncAll() {
            runOnThread {
                backupYear?.apply {
                    syncYear(mApp, year).apply {
                        if (this.isNotEmpty()) {
                            updateItems { it.countNotSync > 0 }
                            runOnMain {
                                busLiveData?.post(BusEvent.DAILY_IMPORT, extra = bundleOf("values" to this))
                                busLiveData?.post(BusEvent.IMPORT_MONTH, intValue = year)
                                mApp.toast(mApp.getString(R.string.backup_import_tip2, this.size))
                            }
                        } else {
                            runOnMain { mApp.toast(mApp.getString(R.string.backup_import_tip1)) }
                        }
                    }
                }
            }
        }
    }
}