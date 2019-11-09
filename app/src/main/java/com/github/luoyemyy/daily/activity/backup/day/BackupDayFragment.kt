package com.github.luoyemyy.daily.activity.backup.day

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.activity.backup.month.BackupMonth
import com.github.luoyemyy.daily.databinding.FragmentBackupDayBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupDayRecyclerBinding
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
            mPresenter.syncAll()
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
        mPresenter.loadInit(arguments)
    }


    inner class Adapter : FixedAdapter<BackupDay, FragmentBackupDayRecyclerBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_backup_day_recycler
        }

        override fun getItemClickViews(binding: FragmentBackupDayRecyclerBinding): List<View> {
            return listOf(binding.txtName, binding.imgSync)
        }

        override fun onItemViewClick(binding: FragmentBackupDayRecyclerBinding, vh: VH<*>, view: View) {
            (getItem(vh.adapterPosition) as? BackupDay)?.apply {
                when (view) {
                    binding.txtName -> findNavController().navigate(R.id.action_backupDay_to_backupDetail, bundleOf("day" to this))
                    binding.imgSync -> mPresenter.sync(this)
                }
            }
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {

        val title = MutableLiveData<String>()
        private var backupMonth: BackupMonth? = null

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<BackupDay>? {
            return bundle?.getParcelable<BackupMonth>("month")?.let {
                title.value = mApp.getString(R.string.backup_manager_month, it.year,it.month)
                backupMonth = it
                it.days
            }
        }

        fun syncAll() {
            runOnThread {
                backupMonth?.apply {
                    syncMonth(mApp, year, month)
                }
            }
        }

        fun sync(backupDay: BackupDay) {
            runOnThread {
                syncDay(mApp, backupDay.year, backupDay.month, backupDay.day).apply {
                    listLiveData.itemChange { list, _ ->
                        list?.forEach {
                            (it as? BackupDay)?.apply {
                                if (this.name() == backupDay.name()) {
                                    backupDay.sync = true
                                    backupDay.hasPayload()
                                }
                            }
                        }
                        true
                    }
                }
            }
        }
    }
}