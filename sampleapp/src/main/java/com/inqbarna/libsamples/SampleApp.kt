package com.inqbarna.libsamples

import android.app.Application
import timber.log.Timber

/**
 * @author David García (david.garcia@inqbarna.com)
 * *
 * @version 1.0 08/05/2017
 */

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
