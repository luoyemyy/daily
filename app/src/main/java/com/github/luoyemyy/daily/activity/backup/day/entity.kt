package com.github.luoyemyy.daily.activity.backup.day

import android.os.Parcel
import android.os.Parcelable
import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.daily.util.formatDate
import com.github.luoyemyy.daily.util.formatDateNum


class BackupDay() : DataItem(), Parcelable {

    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var sync: Boolean = false
    var path: String? = null

    fun name(): String {
        return formatDate(year, month, day)
    }

    constructor(parcel: Parcel) : this() {
        year = parcel.readInt()
        month = parcel.readInt()
        day = parcel.readInt()
        sync = parcel.readByte() != 0.toByte()
        path = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(day)
        parcel.writeByte(if (sync) 1 else 0)
        parcel.writeString(path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BackupDay> {
        override fun createFromParcel(parcel: Parcel): BackupDay {
            return BackupDay(parcel)
        }

        override fun newArray(size: Int): Array<BackupDay?> {
            return arrayOfNulls(size)
        }
    }
}

