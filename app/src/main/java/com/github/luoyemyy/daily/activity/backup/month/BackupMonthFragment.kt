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
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.activity.backup.year.BackupYear
import com.github.luoyemyy.daily.databinding.FragmentBackupMonthBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupMonthRecyclerBinding
import com.github.luoyemyy.daily.util.syncYear
import com.github.luoyemyy.daily.util.verifyBackupYear

class BackupMonthFragment : OverrideMenuFragment() {
    private lateinit var mBinding: FragmentBackupMonthBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupMonthBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.backup_sync_verify, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sync) {
            requireActivity().confirm(title = getString(R.string.backup_input), message = getString(R.string.backup_input_tip, mPresenter.getTipName()), ok = {
                mPresenter.syncAll()
            })

        } else if (item.itemId == R.id.verify) {
            requireActivity().confirm(title = getString(R.string.backup_verify), message = getString(R.string.backup_verify_tip, mPresenter.getTipName()), ok = {
                mPresenter.verifyAll()
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
        mPresenter.verify.observe(this, Observer {
            requireActivity().toast(it)
        })
        mPresenter.loadInit(arguments)
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

        private var backupYear: BackupYear? = null
        val verify = MutableLiveData<String>()

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<BackupMonth>? {
            return bundle?.getParcelable<BackupYear>("year")?.let {
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

        fun verifyAll() {
            runOnThread {
                backupYear?.apply {
                    val list = verifyBackupYear(mApp, year)
                    if (list.isNullOrEmpty()) {
                        verify.postValue(mApp.getString(R.string.backup_verify_success, getTipName()))
                    } else {
                        verify.postValue(mApp.getString(R.string.backup_verify_append, list.size))
                    }
                }
            }
        }
    }
}