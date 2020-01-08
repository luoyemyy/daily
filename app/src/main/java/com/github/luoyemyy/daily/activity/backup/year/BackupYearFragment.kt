package com.github.luoyemyy.daily.activity.backup.year

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.adapter.FixedAdapter
import com.github.luoyemyy.aclin.mvp.core.ListLiveData
import com.github.luoyemyy.aclin.mvp.core.LoadParams
import com.github.luoyemyy.aclin.mvp.core.MvpPresenter
import com.github.luoyemyy.aclin.mvp.core.VH
import com.github.luoyemyy.aclin.mvp.ext.getPresenter
import com.github.luoyemyy.aclin.mvp.ext.setupLinear
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentBackupYearBinding
import com.github.luoyemyy.daily.databinding.FragmentBackupYearRecyclerBinding
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.getBackupYears

class BackupYearFragment : OverrideMenuFragment(), BusResult {
    private lateinit var mBinding: FragmentBackupYearBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBackupYearBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        Adapter().also {
            it.setup(this, mPresenter.listLiveData)
            mBinding.recyclerView.apply {
                setupLinear(it)
                setHasFixedSize(true)
            }
        }
        setBus(this, BusEvent.IMPORT_MONTH)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        when (msg.event) {
            BusEvent.IMPORT_MONTH -> {
                mPresenter.updateItems(msg.intValue)
            }
        }
    }

    inner class Adapter : FixedAdapter<BackupYear, FragmentBackupYearRecyclerBinding>() {
        override fun bindContentViewHolder(binding: FragmentBackupYearRecyclerBinding, data: BackupYear?, viewType: Int, position: Int) {
            binding.apply {
                entity = data
                executePendingBindings()
            }
        }

        override fun getContentBinding(viewType: Int, parent: ViewGroup): FragmentBackupYearRecyclerBinding {
            return FragmentBackupYearRecyclerBinding.inflate(layoutInflater, parent, false)
        }


        override fun onItemViewClick(binding: FragmentBackupYearRecyclerBinding, vh: VH<*>, view: View) {
            getItem(vh.adapterPosition)?.apply {
                findNavController().navigate(R.id.action_backupYear_to_backupMonth, bundleOf("year" to this))
            }
        }
    }

    class Presenter(private var mApp: Application) : MvpPresenter(mApp) {

        val listLiveData = object : ListLiveData<BackupYear>() {
            override fun getData(loadParams: LoadParams): List<BackupYear>? {
                return getBackupYears(mApp)
            }
        }

        override fun loadData(bundle: Bundle?) {
            listLiveData.loadStart()
        }

        fun updateItems(year: Int) {
            listLiveData.itemChange { list, _ ->
                list?.forEach {
                    it.data?.apply {
                        if (this.year == year) {
                            countNotSync = 0
                            months?.forEach { month ->
                                month.countNotSync = 0
                                month.days?.forEach { day ->
                                    day.sync = true
                                }
                            }
                            it.hasPayload()
                        }
                    }
                }
                true
            }
        }

    }
}