package com.github.luoyemyy.daily.activity.backup.day

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusLiveData
import com.github.luoyemyy.aclin.bus.getBusLiveData
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
import com.github.luoyemyy.daily.activity.backup.month.BackupMonth
import com.github.luoyemyy.daily.databinding.FragmentBackupDayBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupDayRecyclerBinding
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.setToolbarTitle
import com.github.luoyemyy.daily.util.syncDay
import com.github.luoyemyy.daily.util.syncMonth

class BackupDayFragment : OverrideMenuFragment() {
    private lateinit var mBinding: FragmentBackupDayBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupDayBinding.inflate(inflater, container, false).also { mBinding = it }.root
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
        mPresenter.loadInit(arguments)
    }


    inner class Adapter : FixedAdapter<BackupDay, FragmentBackupDayRecyclerBinding>() {

        override fun getItemClickViews(binding: FragmentBackupDayRecyclerBinding): List<View> {
            return listOf(binding.txtName, binding.imgSync)
        }

        override fun bindContentViewHolder(binding: FragmentBackupDayRecyclerBinding, data: BackupDay?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentBackupDayRecyclerBinding {
            return FragmentBackupDayRecyclerBinding.inflate(layoutInflater, parent, false)
        }

        override fun onItemViewClick(binding: FragmentBackupDayRecyclerBinding, vh: VH<*>, view: View) {
            getItem(vh.adapterPosition)?.apply {
                when (view) {
                    binding.txtName -> findNavController().navigate(R.id.action_backupDay_to_backupDetail, bundleOf("day" to this))
                    binding.imgSync -> mPresenter.sync(this)
                }
            }
        }
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        val title = MutableLiveData<String>()
        val listLiveData = ListLiveData<BackupDay>()
        var busLiveData: BusLiveData? = null
        private var backupMonth: BackupMonth? = null

        override fun loadData(bundle: Bundle?) {
            bundle?.getParcelable<BackupMonth>("month")?.let {
                title.postValue(mApp.getString(R.string.backup_manager_prefix) + mApp.getString(R.string.backup_manager_month, it.year, it.month))
                backupMonth = it
                listLiveData.loadStart(it.days)
            }
        }

        fun syncAll() {
            runOnThread {
                backupMonth?.apply {
                    syncMonth(mApp, year, month).apply {
                        if (this.isNotEmpty()) {
                            updateItems(this)
                            busLiveData?.post(BusEvent.DAILY_IMPORT, extra = bundleOf("values" to this))
                            busLiveData?.post(BusEvent.IMPORT_DAY, intValue = month)
                            busLiveData?.post(BusEvent.IMPORT_MONTH, intValue = year)
                            runOnMain { mApp.toast(mApp.getString(R.string.backup_import_tip2, this.size)) }
                        } else {
                            runOnMain { mApp.toast(mApp.getString(R.string.backup_import_tip1)) }
                        }
                    }
                }
            }
        }

        fun getTipName(): String {
            val year = backupMonth?.year ?: 0
            val month = backupMonth?.month ?: 0
            return mApp.getString(R.string.backup_manager_month, year, month)
        }

        private fun updateItems(days: List<Int>) {
            listLiveData.itemChange { list, _ ->
                list?.forEach {
                    it.data?.apply {
                        if (days.contains(value())) {
                            sync = true
                            it.hasPayload()
                        }
                    }
                }
                true
            }
        }


        fun sync(backupDay: BackupDay) {
            runOnThread {
                syncDay(mApp, backupDay.year, backupDay.month, backupDay.day).apply {
                    updateItems(listOf(backupDay.value()))
                }
            }
        }
    }
}