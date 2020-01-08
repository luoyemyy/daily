package com.github.luoyemyy.daily.activity.backup.month

import android.os.Parcel
import android.os.Parcelable
import com.github.luoyemyy.aclin.mvp.core.MvpData
import com.github.luoyemyy.daily.activity.backup.day.BackupDay
import com.github.luoyemyy.daily.util.formatDateNum

class BackupMonth() : MvpData(), Parcelable {
    var year: Int = 0
    var month: Int = 0
    var countNotSync: Int = 0
    var days: List<BackupDay>? = null

    fun name(): String {
        return "$year-${formatDateNum(month)}"
    }

    constructor(parcel: Parcel) : this() {
        year = parcel.readInt()
        month = parcel.readInt()
        countNotSync = parcel.readInt()
        days = parcel.createTypedArrayList(BackupDay)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(countNotSync)
        parcel.writeTypedList(days)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BackupMonth> {
        override fun createFromParcel(parcel: Parcel): BackupMonth {
            return BackupMonth(parcel)
        }

        override fun newArray(size: Int): Array<BackupMonth?> {
            return arrayOfNulls(size)
        }
    }
}
