package com.github.luoyemyy.daily.activity.backup.group

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.activity.backup.record.BackupFile
import com.github.luoyemyy.daily.databinding.FragmentBackupGroupBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupGroupRecyclerBinding
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.UserInfo
import com.github.luoyemyy.daily.util.getBackupDir

class BackupGroupFragment : OverrideMenuFragment() {
    private lateinit var mBinding: FragmentBackupGroupBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupGroupBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
            recyclerView.setHasFixedSize(true)
        }
        mPresenter.loadInit(arguments)
    }


    inner class Adapter : FixedAdapter<BackupGroup, FragmentBackupGroupRecyclerBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_backup_group_recycler
        }

        override fun onItemViewClick(binding: FragmentBackupGroupRecyclerBinding, vh: VH<*>, view: View) {
            (getItem(vh.adapterPosition) as? BackupGroup)?.apply {
                findNavController().navigate(R.id.action_backupGroupFragment_to_backupRecordFragment, bundleOf("files" to files))
            }
        }
    }

    class Presenter(mApp: Application) : AbsListPresenter(mApp) {

        private val recordDao = getRecordDao()

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<BackupGroup>? {
            return getBackupDir()?.let { dir ->
                dir.list()?.map {
                    BackupFile(it).apply {
                        sync = checkBackupFileSync(this)
                    }
                }?.groupBy { it.groupName!! }?.map {
                    BackupGroup(it.key, it.value).apply {
                        sync = checkBackupGroupSync(this)
                    }
                }?.sortedBy { it.year * 100 + it.month }
            }
        }

        private fun checkBackupFileSync(backupFile: BackupFile): Boolean {
            return if (backupFile.isMonth) {
                false
            } else {
                recordDao.countByDate(UserInfo.getUser().id, backupFile.year, backupFile.month, backupFile.day) > 0L
            }
        }

        private fun checkBackupGroupSync(backupGroup: BackupGroup): Boolean {
            return backupGroup.files?.let { files ->
                files.all { it.sync }.apply {
                    files.filter { it.isMonth }.forEach {
                        it.sync = this
                    }
                }
            } ?: false
        }
    }
}