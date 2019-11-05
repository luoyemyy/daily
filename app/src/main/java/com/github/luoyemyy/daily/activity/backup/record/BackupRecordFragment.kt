package com.github.luoyemyy.daily.activity.backup.record

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentBackupRecordBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupRecordRecyclerBinding

class BackupRecordFragment : OverrideMenuFragment() {
    private lateinit var mBinding: FragmentBackupRecordBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupRecordBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
            recyclerView.setHasFixedSize(true)
        }
        mPresenter.loadInit(arguments)
    }


    inner class Adapter : FixedAdapter<BackupFile, FragmentBackupRecordRecyclerBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_backup_record_recycler
        }

        override fun onItemViewClick(binding: FragmentBackupRecordRecyclerBinding, vh: VH<*>, view: View) {
            (getItem(vh.adapterPosition) as? BackupFile)?.apply {
                if (!sync) {
                    mPresenter.sync(this)
                }
            }
        }
    }

    class Presenter(mApp: Application) : AbsListPresenter(mApp) {


        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<BackupFile>? {
            return bundle?.getParcelableArrayList("files")
        }

        fun sync(backupFile: BackupFile) {

        }

        fun syncAll() {

        }
    }
}