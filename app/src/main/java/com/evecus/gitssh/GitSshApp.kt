package com.evecus.gitssh

import android.app.Application
import com.evecus.gitssh.data.AppContainer

class GitSshApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
    }

    companion object {
        lateinit var instance: GitSshApp
            private set
    }
}
