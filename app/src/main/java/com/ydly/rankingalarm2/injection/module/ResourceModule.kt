package com.ydly.rankingalarm2.injection.module

import android.content.Context
import com.ydly.rankingalarm2.util.ResourceProvider
import dagger.Module
import dagger.Provides

@Suppress("unused")
@Module(includes = [AppModule::class])
class ResourceModule {

    @Provides
    fun provideResourceProvider(context: Context): ResourceProvider {
        return ResourceProvider(context)
    }

}
