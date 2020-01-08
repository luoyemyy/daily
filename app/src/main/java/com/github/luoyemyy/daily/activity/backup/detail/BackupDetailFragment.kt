package com.github.luoyemyy.daily.activity.backup.detail

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.ext.toObject
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.activity.backup.day.BackupDay
import com.github.luoyemyy.daily.databinding.FragmentBackupDetailBinding
import com.github.luoyemyy.daily.db.entity.Record
import com.github.luoyemyy.daily.util.AesUtils
import com.github.luoyemyy.daily.util.formatDate
import com.github.luoyemyy.daily.util.getBackupDayFile
import com.github.luoyemyy.daily.util.setToolbarTitle
import java.io.FileReader

class BackupDetailFragment : OverrideMenuFragment() {

    private lateinit var mBinding: FragmentBackupDetailBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupDetailBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            if (it.result) {
                findNavController().navigateUp()
            } else {
                setToolbarTitle(requireActivity(), it.title)
                mBinding.entity = it.content
            }
        })
        mPresenter.loadInit(arguments)
    }

    class Presenter(var mApp: Application) : MvpPresenter(mApp) {

        val data = MutableLiveData<BackupDetail>()
        private val backupDetail = BackupDetail()

        override fun loadData(bundle: Bundle?) {
            bundle?.getParcelable<BackupDay>("day")?.apply {
                backupDetail.title = mApp.getString(R.string.backup_detail_title, formatDate(year, month, day))
                data.postValue(backupDetail)
                getBackupDayFile(year, month, day)?.apply {
                    if (exists()) {
                        FileReader(this).readText().let {
                            AesUtils.decrypt(it)
                        }.toObject<Record>()?.apply {
                            backupDetail.content = content
                            data.postValue(backupDetail)
                        }
                    }
                }
            } ?: kotlin.run {
                backupDetail.result = true
                return data.postValue(backupDetail)
            }

        }
    }

}