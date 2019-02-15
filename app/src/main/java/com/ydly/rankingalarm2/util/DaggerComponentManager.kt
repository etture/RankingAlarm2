package com.ydly.rankingalarm2.util

import android.content.Context
import com.ydly.rankingalarm2.injection.component.*
import com.ydly.rankingalarm2.injection.module.AppModule
import com.ydly.rankingalarm2.injection.module.RoomModule

class DaggerComponentManager(context: Context) {

    private val mContext: Context = context.applicationContext
    private val appModule: AppModule = AppModule(mContext)

    private var repositoryInjector: RepositoryInjector? = null
    private var viewModelInjector: ViewModelInjector? = null
    private var serviceInjector: ServiceInjector? = null
    private var viewInjector: ViewInjector? = null

    fun getRepositoryInjector(): RepositoryInjector? {
        if(repositoryInjector == null) {
            repositoryInjector = DaggerRepositoryInjector.builder()
                .appModule(appModule)
                .build()
        }
        return repositoryInjector
    }

    fun getViewModelInjector(): ViewModelInjector? {
        if(viewModelInjector == null) {
            viewModelInjector = DaggerViewModelInjector.builder()
                .appModule(appModule)
                .repositoryInjector(repositoryInjector!!)
                .build()
        }
        return viewModelInjector
    }

    fun getServiceInjector(): ServiceInjector? {
        if(serviceInjector == null) {
            serviceInjector = DaggerServiceInjector.builder()
                .appModule(appModule)
                .repositoryInjector(repositoryInjector!!)
                .build()
        }
        return serviceInjector
    }

    fun getViewInjector(): ViewInjector? {
        if(viewInjector == null) {
            viewInjector = DaggerViewInjector.builder()
                .appModule(appModule)
                .build()
        }
        return viewInjector
    }

}
