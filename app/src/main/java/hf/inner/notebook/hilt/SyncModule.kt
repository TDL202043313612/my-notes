package hf.inner.notebook.hilt

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hf.inner.notebook.backup.SyncManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun providesSyncManager(
        @ApplicationContext context: Context
    ) = SyncManager(context)
}