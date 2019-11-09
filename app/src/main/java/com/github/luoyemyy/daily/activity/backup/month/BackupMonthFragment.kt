package com.github.luoyemyy.daily.activity.backup.month

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.ext.confirm
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.activity.backup.year.BackupYear
import com.github.luoyemyy.daily.databinding.FragmentBackupMonthBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupMonthRecyclerBinding
import com.github.luoyemyy.daily.util.setToolbarTitle
import com.github.luoyemyy.daily.util.syncYear

class BackupMonthFragment : OverrideMenuFragment() {
    private lateinit var mBinding: FragmentBackupMonthBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentBackupMonthBinding.inflate(inflater, container, false).also { mBinding = it }
            .root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.backup_sync, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sync) {
            requireActivity().confirm(
                title = getString(R.string.backup_input),
                message = getString(R.string.backup_input_tip, mPresenter.getTipName()),
                ok = {
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
        mPresenter.loadInit(arguments)
    }


    inner class Adapter : FixedAdapter<BackupMonth, FragmentBackupMonthRecyclerBinding>(
        this,
        mPresenter.listLiveData
    ) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_backup_month_recycler
        }

        override fun onItemViewClick(
            binding: FragmentBackupMonthRecyclerBinding,
            vh: VH<*>,
            view: View
        ) {
            (getItem(vh.adapterPosition) as? BackupMonth)?.apply {
                findNavController().navigate(
                    R.id.action_backupMonth_to_backupDay,
                    bundleOf("month" to this)
                )
            }
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {

        val title = MutableLiveData<String>()
        private var backupYear: BackupYear? = null

        override fun loadListData(
            bundle: Bundle?,
            paging: Paging,
            loadType: LoadType
        ): List<BackupMonth>? {
            return bundle?.getParcelable<BackupYear>("year")?.let {
                title.value = mApp.getString(R.string.backup_manager_year, it.year)
                backupYear = it
                it.months
            }
        }

        fun getTipName(): String {
            return backupYear?.name() + "年"
        }

        fun syncAll() {
            runOnThread {
                backupYear?.apply {
                    syncYear(mApp, year)
                }
            }
        }
    }
}