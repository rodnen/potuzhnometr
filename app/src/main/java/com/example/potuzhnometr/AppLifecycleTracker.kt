package com.example.potuzhnometr

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

class AppLifecycleTracker : Application.ActivityLifecycleCallbacks {

    private var numStarted = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Не використовується
        Log.d("AppLifecycle", "Activity created")
    }

    override fun onActivityStarted(activity: Activity) {
        if (numStarted == 0) {
            // Додаток перейшов у foreground
            Log.d("AppLifecycle", "Activity entered foreground")
        }
        numStarted++
    }

    override fun onActivityResumed(activity: Activity) {
        // Не використовується
        Log.d("AppLifecycle", "Activity resumed")
    }

    override fun onActivityPaused(activity: Activity) {
        // Не використовується
        Log.d("AppLifecycle", "Activity paused")
    }

    override fun onActivityStopped(activity: Activity) {
        numStarted--
        if (numStarted == 0) {
            // Додаток перейшов у background (був закритий або переключений)
            Log.d("AppLifecycle", "Activity entered background (closed or switched)")
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Не використовується
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Не використовується
        Log.d("AppLifecycle", "Activity destroyed")
    }
}