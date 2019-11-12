package com.github.luoyemyy.daily.activity.backup.month

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.addBus
import com.github.luoyemyy.aclin.bus.postBus
import com.github.luoyemyy.aclin.ext.confirm
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
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
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
            recyclerView.setHasFixedSize(true)
        }
        mPresenter.title.observe(this, Observer {
            setToolbarTitle(requireActivity(), it)
        })
        addBus(this, BusEvent.IMPORT_DAY, this)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        when (msg.event) {
            BusEvent.IMPORT_DAY -> {
                mPresenter.updateItems(msg.intValue)
            }
        }
    }

    inner class Adapter : FixedAdapter<BackupMonth, FragmentBackupMonthRecyclerBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_backup_month_recycler
        }

        override fun onItemViewClick(binding: FragmentBackupMonthRecyclerBinding, vh: VH<*>, view: View) {
            (getItem(vh.adapterPosition) as? BackupMonth)?.apply {
                findNavController().navigate(R.id.action_backupMonth_to_backupDay, bundleOf("month" to this))
            }
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {

        val title = MutableLiveData<String>()
        private var backupYear: BackupYear? = null

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<BackupMonth>? {
            return bundle?.getParcelable<BackupYear>("year")?.let {
                title.postValue(mApp.getString(R.string.backup_manager_prefix) + mApp.getString(R.string.backup_manager_year, it.year))
                backupYear = it
                it.months
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
                    (it as? BackupMonth)?.apply {
                        if (condition(this)) {
                            countNotSync = 0
                            days?.forEach { day ->
                                day.sync = true
                            }
                            hasPayload()
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
                            postBus(BusEvent.DAILY_IMPORT, extra = bundleOf("values" to this))
                            postBus(BusEvent.IMPORT_MONTH, intValue = year)
                            runOnMain { mApp.toast(mApp.getString(R.string.backup_import_tip2, this.size)) }
                        } else {
                            runOnMain { mApp.toast(mApp.getString(R.string.backup_import_tip1)) }
                        }
                    }
                }
            }
        }
    }
}