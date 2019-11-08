package com.github.luoyemyy.daily.activity.backup.year

import android.os.Parcel
import android.os.Parcelable
import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.daily.activity.backup.month.BackupMonth

class BackupYear() : DataItem(), Parcelable {
    var year: Int = 0
    var countNotSync: Int = 0
    var months: List<BackupMonth>? = null

    constructor(parcel: Parcel) : this() {
        year = parcel.readInt()
        countNotSync = parcel.readInt()
        months = parcel.createTypedArrayList(BackupMonth)
    }

    fun name(): String {
        return "$year"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(year)
        parcel.writeInt(countNotSync)
        parcel.writeTypedList(months)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BackupYear> {
        override fun createFromParcel(parcel: Parcel): BackupYear {
            return BackupYear(parcel)
        }

        override fun newArray(size: Int): Array<BackupYear?> {
            return arrayOfNulls(size)
        }
    }
}
