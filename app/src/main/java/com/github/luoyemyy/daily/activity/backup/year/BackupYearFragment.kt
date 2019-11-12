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
import com.github.luoyemyy.aclin.bus.addBus
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
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
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
            recyclerView.setHasFixedSize(true)
        }
        addBus(this, BusEvent.IMPORT_MONTH, this)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        when (msg.event) {
            BusEvent.IMPORT_MONTH -> {
                mPresenter.updateItems(msg.intValue)
            }
        }
    }

    inner class Adapter : FixedAdapter<BackupYear, FragmentBackupYearRecyclerBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_backup_year_recycler
        }

        override fun onItemViewClick(binding: FragmentBackupYearRecyclerBinding, vh: VH<*>, view: View) {
            (getItem(vh.adapterPosition) as? BackupYear)?.apply {
                findNavController().navigate(R.id.action_backupYear_to_backupMonth, bundleOf("year" to this))
            }
        }
    }

    class Presenter(private var mApp: Application) : AbsListPresenter(mApp) {

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<BackupYear>? {
            return getBackupYears(mApp)
        }

        fun updateItems(year: Int) {
            listLiveData.itemChange { list, _ ->
                list?.forEach {
                    (it as? BackupYear)?.apply {
                        if (this.year == year) {
                            countNotSync = 0
                            months?.forEach { month ->
                                month.countNotSync = 0
                                month.days?.forEach { day ->
                                    day.sync = true
                                }
                            }
                            hasPayload()
                        }
                    }
                }
                true
            }
        }

    }
}