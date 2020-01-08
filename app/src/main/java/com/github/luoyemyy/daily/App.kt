package com.github.luoyemyy.daily

import android.app.Application
import com.github.luoyemyy.aclin.app.AppInfo
import com.github.luoyemyy.daily.db.Db
import com.github.luoyemyy.daily.util.Profile

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Db.initDb(this)
        AppInfo.init(this, BuildConfig.BUILD_TYPE, Profile())
    }
}