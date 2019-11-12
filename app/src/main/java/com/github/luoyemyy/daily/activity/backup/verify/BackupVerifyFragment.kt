package com.github.luoyemyy.daily.activity.backup.verify

import android.app.Application
import android.os.Bundle
import android.view.*
import com.github.luoyemyy.aclin.ext.formatDateTime
import com.github.luoyemyy.aclin.ext.runOnMain
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentBackupVerifyBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupVerifyRecyclerBinding
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.AppCache
import com.github.luoyemyy.daily.util.verifyBackupYear
import java.util.*

class BackupVerifyFragment : OverrideMenuFragment() {
    private lateinit var mBinding: FragmentBackupVerifyBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupVerifyBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.backup_verify, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.verify) {
            mPresenter.verify()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
            recyclerView.setHasFixedSize(true)
        }
        mPresenter.loadInit(arguments)
    }


    inner class Adapter : FixedAdapter<BackupVerify, FragmentBackupVerifyRecyclerBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_backup_verify_recycler
        }

        override fun bindItemEvents(binding: FragmentBackupVerifyRecyclerBinding, vh: VH<*>) {
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                (getItem(vh.adapterPosition) as? BackupVerify)?.apply {
                    select = isChecked
                }
            }
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {


        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<BackupVerify>? {
            return getRecordDao().getListByGroupYear(AppCache.getUserId(mApp))?.map {
                BackupVerify().apply {
                    year = it.year
                    name = getName(it.year)
                    verifyTime = getVerifyTime(it.year)
                }
            }
        }

        fun verify() {
            runOnThread {
                var countSelect = 0
                listLiveData.itemChange { list, _ ->
                    val now = Date()
                    list?.forEach {
                        (it as? BackupVerify)?.apply {
                            if (this.select) {
                                val countAppend = verifyBackupYear(mApp, this.year)?.size ?: 0
                                runOnMain {
                                    if (countAppend > 0) {
                                        mApp.toast(mApp.getString(R.string.backup_verify_append, countAppend))
                                    } else {
                                        mApp.toast(R.string.backup_verify_success)
                                    }
                                }
                                now.formatDateTime()?.also { time ->
                                    it.verifyTime = mApp.getString(R.string.backup_verify_verify_time, time)
                                }
                                it.hasPayload()
                                countSelect++
                                AppCache.setVerifyTime(mApp, this.year, now)
                            }
                        }
                    }
                    true
                }
                if (countSelect == 0) {
                    runOnMain {
                        mApp.toast(R.string.backup_verify_tip1)
                    }
                }
            }
        }


        private fun getName(year: Int): String? {
            return mApp.getString(R.string.backup_verify_name, year)
        }

        private fun getVerifyTime(year: Int): String? {
            return AppCache.getVerifyTime(mApp, year).let {
                if (it > 0) {
                    Date(it).formatDateTime()?.let { time ->
                        mApp.getString(R.string.backup_verify_verify_time, time)
                    } ?: ""
                } else {
                    ""
                }
            }
        }
    }
}