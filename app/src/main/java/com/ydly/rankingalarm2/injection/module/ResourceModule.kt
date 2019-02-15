package com.ydly.rankingalarm2.injection.module

import android.content.Context
import com.ydly.rankingalarm2.util.ResourceProvider
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule::class])
@Suppress("unused")
class ResourceModule {

    @Provides
    fun provideResourceProvider(context: Context): ResourceProvider {
        return ResourceProvider(context)
    }

}
