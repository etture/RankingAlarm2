package com.ydly.rankingalarm2.base

import android.app.Application
import android.content.Context
import com.ydly.rankingalarm2.util.DaggerComponentManager
import com.ydly.rankingalarm2.injection.component.RepositoryInjector
import com.ydly.rankingalarm2.injection.component.ServiceInjector
import com.ydly.rankingalarm2.injection.component.ViewInjector
import com.ydly.rankingalarm2.injection.component.ViewModelInjector
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*

class BaseApplication: Application(), AnkoLogger {

    private lateinit var componentManager: DaggerComponentManager

    companion object {

        private var repositoryInjector: RepositoryInjector? = null
        private var viewModelInjector: ViewModelInjector? = null
        private var serviceInjector: ServiceInjector? = null
        private var viewInjector: ViewInjector? = null

        fun getRepositoryInjector() = repositoryInjector
        fun getViewModelInjector() = viewModelInjector
        fun getServiceInjector() = serviceInjector
        fun getViewInjector() = viewInjector

    }

    override fun onCreate() {
        super.onCreate()
        info("Application initialized")
        initComponentManager(this)
        initComponents()
        setUUID()
    }

    private fun initComponentManager(context: Context) {
        componentManager = DaggerComponentManager(context)
    }

    private fun initComponents() {
        initRepositoryInjector()
        initViewModelInjector()
        initServiceInjector()
        initViewInjector()
    }

    private fun initRepositoryInjector() {
        repositoryInjector = componentManager.getRepositoryInjector()
    }

    private fun initViewModelInjector() {
        viewModelInjector = componentManager.getViewModelInjector()
    }

    private fun initServiceInjector() {
        serviceInjector = componentManager.getServiceInjector()
    }

    private fun initViewInjector() {
        viewInjector = componentManager.getViewInjector()
    }

    private fun setUUID() {
        val prefs = getSharedPreferences("mainPrefs", Context.MODE_PRIVATE)
        // If the uuid is not present, then it means first-time installation
        // Set a UUID that can be used to identify the device from the server
        if (prefs.getString("installation_uuid", null) == null) {
            val editor = prefs.edit()
            editor.putString("installation_uuid", UUID.randomUUID().toString())
            editor.apply()
        }
        info("Installation UUID: ${prefs.getString("installation_uuid", null)}")
    }

}
