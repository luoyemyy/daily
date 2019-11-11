package com.github.luoyemyy.daily.activity.backup

import android.Manifest
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.ext.toast
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.AbsPresenter
import com.github.luoyemyy.aclin.mvp.getPresenter
import com.github.luoyemyy.aclin.permission.PermissionManager
import com.github.luoyemyy.aclin.permission.requestPermission
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentBackupBinding
import com.github.luoyemyy.daily.util.UserInfo

class BackupFragment : OverrideMenuFragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private lateinit var mBinding: FragmentBackupBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            mBinding.entity = it
        })
        mPresenter.verify.observe(this, Observer {
            requireActivity().toast(it)
        })
        mBinding.switchAuto.setOnCheckedChangeListener(this)
        mBinding.txtManagerBackup.setOnClickListener(this)
        mPresenter.loadInit(arguments)
    }

    override fun onClick(v: View?) {
        val permissionTip = getString(R.string.backup_permission_file)
        requestPermission(this, permissionTip).granted {
            when (v) {
                mBinding.txtManagerBackup -> findNavController().navigate(R.id.action_backup_to_backupYear)
                mBinding.txtVerifyBackup -> findNavController().navigate(R.id.action_backup_to_backupVerify)
            }
        }.denied {
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE in it) {
                PermissionManager.toSetting(this, permissionTip)
            }
        }.buildAndRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            val permissionTip = getString(R.string.backup_permission_file)
            requestPermission(this, permissionTip).granted {
                UserInfo.setAutoBackup(requireContext(), true)
            }.denied {
                UserInfo.setAutoBackup(requireContext(), false)
                mPresenter.updateAutoBackup(false)
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE in it) {
                    PermissionManager.toSetting(this, permissionTip)
                }
            }.buildAndRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            UserInfo.setAutoBackup(requireContext(), false)
        }
    }

    class Presenter(var mApp: Application) : AbsPresenter(mApp) {

        val data = MutableLiveData<BackupBean>()
        val verify = MutableLiveData<String>()
        private val backup = BackupBean()

        override fun loadData(bundle: Bundle?) {
            updateAutoBackup(UserInfo.autoBackup(mApp))
        }

        fun updateAutoBackup(auto: Boolean) {
            backup.auto = auto
            data.postValue(backup)
        }
    }

}