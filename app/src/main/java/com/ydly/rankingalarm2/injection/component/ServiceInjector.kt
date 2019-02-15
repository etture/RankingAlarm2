package com.ydly.rankingalarm2.injection.component

import android.app.Service
import com.ydly.rankingalarm2.injection.module.AppModule
import com.ydly.rankingalarm2.injection.module.RepositoryModule
import com.ydly.rankingalarm2.injection.module.ResourceModule
import com.ydly.rankingalarm2.injection.scope.ServiceScope
import com.ydly.rankingalarm2.service.TimeUpdateService
import dagger.Component

@ServiceScope
@Component(modules = [AppModule::class, RepositoryModule::class, ResourceModule::class], dependencies = [RepositoryInjector::class])
interface ServiceInjector {

    fun inject(timeUpdateService: TimeUpdateService)

    @Component.Builder
    interface Builder {
        fun build(): ServiceInjector
        fun appModule(appModule: AppModule): Builder
        fun repositoryInjector(repositoryInjector: RepositoryInjector): Builder
    }

}