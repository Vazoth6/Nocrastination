package pt.ipt.dam2025.nocrastination

import android.app.Application
import android.content.Context
import pt.ipt.dam2025.nocrastination.data.datasource.remote.ApiClient

class NoCrastinationApplication : Application() {

    companion object {
        lateinit var instance: NoCrastinationApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize API client
        ApiClient.initialize(this)
    }

    fun getAppContext(): Context = applicationContext
}