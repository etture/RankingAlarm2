package com.ydly.rankingalarm2.base

import android.app.Application
import android.content.Context
import com.ydly.rankingalarm2.util.DaggerComponentManager
import com.ydly.rankingalarm2.injection.component.RepositoryInjector
import com.ydly.rankingalarm2.injection.component.ServiceInjector
import com.ydly.rankingalarm2.injection.component.ViewInjector
import com.ydly.rankingalarm2.injection.component.ViewModelInjector
import com.ydly.rankingalarm2.util.ResourceProvider
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

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

}
