package pt.ipt.dam2025.nocrastination

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoCrastinationApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any global components here
    }
}