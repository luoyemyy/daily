package com.github.luoyemyy.daily.activity.backup.record

import android.os.Parcel
import android.os.Parcelable
import com.github.luoyemyy.aclin.mvp.DataItem
import com.github.luoyemyy.daily.util.formatDateNum


class BackupFile() : DataItem(), Parcelable {
    var groupName: String? = null
    var name: String? = null
    var path: String? = null
    var sync: Boolean = false
    var isMonth: Boolean = false
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0

    constructor(fileName: String) : this() {
        this.path = fileName
        val time = fileName.split(".")[0].toInt()
        this.year = time / 10000
        this.month = (time - this.year * 10000) / 100
        this.day = time - this.year * 10000 - this.month * 100
        this.groupName = (year * 10000 + month * 100).toString()
        this.name = "$year-${formatDateNum(month)}-${formatDateNum(day)}"
        this.isMonth = day == 0
    }

    constructor(parcel: Parcel) : this() {
        groupName = parcel.readString()
        name = parcel.readString()
        path = parcel.readString()
        sync = parcel.readByte() != 0.toByte()
        isMonth = parcel.readByte() != 0.toByte()
        year = parcel.readInt()
        month = parcel.readInt()
        day = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(groupName)
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeByte(if (sync) 1 else 0)
        parcel.writeByte(if (isMonth) 1 else 0)
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(day)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BackupFile> {
        override fun createFromParcel(parcel: Parcel): BackupFile {
            return BackupFile(parcel)
        }

        override fun newArray(size: Int): Array<BackupFile?> {
            return arrayOfNulls(size)
        }
    }

}

