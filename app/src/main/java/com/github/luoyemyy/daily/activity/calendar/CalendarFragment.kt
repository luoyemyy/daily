package com.github.luoyemyy.daily.activity.calendar

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.aclin.bus.BusMsg
import com.github.luoyemyy.aclin.bus.BusResult
import com.github.luoyemyy.aclin.bus.setBus
import com.github.luoyemyy.aclin.ext.runOnThread
import com.github.luoyemyy.aclin.fragment.OverrideMenuFragment
import com.github.luoyemyy.aclin.mvp.*
import com.github.luoyemyy.daily.R
import com.github.luoyemyy.daily.databinding.FragmentCalendarBinding
import com.github.luoyemyy.daily.databinding.FragmentCalendarWeekBinding
import com.github.luoyemyy.daily.db.RecordDao
import com.github.luoyemyy.daily.db.entity.Record
import com.github.luoyemyy.daily.db.getRecordDao
import com.github.luoyemyy.daily.util.AppCache
import com.github.luoyemyy.daily.util.BusEvent
import com.github.luoyemyy.daily.util.dayValue
import java.util.*
import kotlin.collections.set

class CalendarFragment : OverrideMenuFragment(), BusResult {
    private lateinit var mBinding: FragmentCalendarBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentCalendarBinding.inflate(inflater, container, false).also { mBinding = it }.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calendar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.read) {
            findNavController().navigate(R.id.action_calendar_to_read)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mBinding.apply {
            recyclerView.setupLinear(Adapter())
            recyclerView.setHasFixedSize(true)
        }
        setBus(this, BusEvent.DAILY_SAVE, this)
        setBus(this, BusEvent.DAILY_IMPORT, this)
        mPresenter.loadInit(arguments)
    }

    override fun busResult(msg: BusMsg) {
        if (msg.event == BusEvent.DAILY_SAVE) {
            mPresenter.updateDaily(msg.longValue)
        } else if (msg.event == BusEvent.DAILY_IMPORT) {
            mPresenter.importDaily(msg.extra?.getIntegerArrayList("values") ?: listOf())
        }
    }

    inner class Adapter : ReversedAdapter<Week, FragmentCalendarWeekBinding>(this, mPresenter.listLiveData) {
        override fun getContentLayoutId(viewType: Int): Int {
            return R.layout.fragment_calendar_week
        }

        override fun pageSize(): Int {
            return 1
        }

        override fun getItemClickViews(binding: FragmentCalendarWeekBinding): List<View> {
            return listOf(binding.monday.root, binding.tuesday.root, binding.wednesday.root, binding.saturday.root, binding.friday.root, binding.thursday.root, binding.sunday.root)
        }

        override fun onItemViewClick(binding: FragmentCalendarWeekBinding, vh: VH<*>, view: View) {
            val week = getItem(vh.adapterPosition) as? Week ?: return
            val day = when (view) {
                binding.monday.root -> week.monday
                binding.tuesday.root -> week.tuesday
                binding.wednesday.root -> week.wednesday
                binding.thursday.root -> week.thursday
                binding.friday.root -> week.friday
                binding.saturday.root -> week.saturday
                binding.sunday.root -> week.sunday
                else -> return
            }
            findNavController().navigate(R.id.action_calendar_to_daily, bundleOf("id" to day.id, "y" to day.year, "m" to day.month, "d" to day.day))
        }
    }

    class Presenter(var mApp: Application) : AbsListPresenter(mApp) {

        private var today: Triple<Int, Int, Int>
        private val recordDao: RecordDao = getRecordDao()
        private val calendar = Calendar.getInstance().apply {
            today = Triple(get(Calendar.YEAR), get(Calendar.MONTH) + 1, get(Calendar.DAY_OF_MONTH))
            set(Calendar.DAY_OF_MONTH, 1) //设置为当月1号
            add(Calendar.MONTH, 1)
        }

        override fun loadListData(bundle: Bundle?, paging: Paging, loadType: LoadType): List<Week>? {
            return getDatas()
        }

        fun importDaily(values: List<Int>) {
            runOnThread {
                values.forEach {
                    val year = it / 10000
                    val month = it % 10000 / 100
                    val day = it % 10000 % 100
                    recordDao.getByDate(AppCache.getUserId(mApp), year, month, day)?.apply {
                        updateBase(this)
                    }
                }
            }
        }

        private fun updateBase(record: Record) {
            val value = dayValue(record)
            listLiveData.itemChange { list, _ ->
                val week = list?.find {
                    (it as? Week)?.let { week ->
                        !week.isTitle && value >= week.min && value <= week.max
                    } ?: false
                } as? Week
                if (week != null) {
                    week.findDay(value)?.apply {
                        this.id = record.id
                        this.hasDaily = true
                    }
                    week.hasPayload()
                    true
                } else {
                    false
                }
            }
        }

        fun updateDaily(id: Long) {
            runOnThread {
                recordDao.get(id)?.apply {
                    updateBase(this)
                }
            }
        }

        private fun countDays(year: Int, month: Int): List<Int> {
            val count = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)[month - 1]
            val offsetBefore: Int
            val offsetAfter: Int
            val isLeapYear: Boolean
            Calendar.getInstance().let {
                //这个月的1号是星期几
                var dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
                if (dayOfWeek == Calendar.SUNDAY) {
                    dayOfWeek = 8
                }
                offsetBefore = dayOfWeek - 2
                it.set(year, 2, 1)
                it.add(Calendar.DAY_OF_MONTH, -1)
                isLeapYear = it.get(Calendar.DAY_OF_MONTH) == 29
            }
            val validDays = if (month == 2 && isLeapYear) count + 1 else count
            offsetAfter = 7 - ((offsetBefore + validDays) % 7).let { if (it == 0) 7 else it }
            val days = mutableListOf<Int>()
            (0 until offsetBefore).forEach { _ ->
                days.add(0)
            }
            (0 until validDays).forEach {
                days.add(it + 1)
            }
            (0 until offsetAfter).forEach { _ ->
                days.add(0)
            }
            return days
        }

        private fun getDatas(): List<Week> {
            val curr = getRecordsByMonth()
            val prev = getRecordsByMonth()
            val prev2 = getRecordsByMonth()
            prev2.addAll(prev)
            prev2.addAll(curr)
            return prev2
        }

        private fun getRecordsByMonth(): MutableList<Week> {
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH) + 1
            val map = mutableMapOf<Int, Record>()
            recordDao.getListByMonthSortDay(AppCache.getUserId(mApp), y, m)?.also { list ->
                list.forEach {
                    map[it.day] = it
                }
            }
            val weeks = mutableListOf<Week>()
            var week = Week(true)
            week.thursday = Day().also {
                it.flag = 2
                it.year = y
                it.month = m
            }
            weeks.add(week)

            countDays(y, m).forEachIndexed { index, i ->
                val j = index % 7
                if (j == 0) {
                    week = Week()
                }
                if (j == 6) {
                    weeks.add(week)
                }
                val record = map[i]
                val day = if (i == 0) {
                    Day()
                } else {
                    Day(record?.id ?: 0L, record != null, y, m, i).also {
                        it.flag = 1
                        it.today(today.first, today.second, today.third)
                    }
                }
                when (j) {
                    0 -> week.monday = day
                    1 -> week.tuesday = day
                    2 -> week.wednesday = day
                    3 -> week.thursday = day
                    4 -> week.friday = day
                    5 -> week.saturday = day
                    6 -> week.sunday = day
                }
            }
            weeks.forEach {
                it.setValue()
            }
            calendar.add(Calendar.MONTH, -1)
            return weeks
        }
    }
}