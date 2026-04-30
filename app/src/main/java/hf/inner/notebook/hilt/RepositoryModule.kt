package hf.inner.notebook.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hf.inner.notebook.db.AppDatabase
import hf.inner.notebook.db.repo.TagNoteRepo
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Module
    @InstallIn(SingletonComponent::class)
    class RepositoryModule {
        @Provides
        @Singleton
        fun provideNoteRepository(
            appDatabase: AppDatabase
        ) = TagNoteRepo(appDatabase.getNoteDao(), appDatabase.getTagNoteDao(), appDatabase.getTagDao(), appDatabase
            .getNoteTagCrossRefDao())
    }
}