package com.cjrodriguez.cjchatgpt.data.di

import android.content.Context
import androidx.room.Room
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatDatabase
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStore
import com.cjrodriguez.cjchatgpt.data.datasource.network.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.util.CHAT_DB
import com.cjrodriguez.cjchatgpt.presentation.BaseApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): BaseApplication {
        return app as BaseApplication
    }

    @Singleton
    @Provides
    fun provideOpenApiConfig(): OpenApiConfig{
        return OpenApiConfig
    }

    @Singleton
    @Provides
    fun provideChatDatabase(@ApplicationContext app: Context): ChatDatabase {
        return Room.databaseBuilder(
            app, ChatDatabase::class.java, CHAT_DB
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideSongDao(chatDatabase: ChatDatabase): ChatTopicDao{
        return chatDatabase.chatTopicDao()
    }

    @Singleton
    @Provides
    fun provideSettingsDatastore(baseApplication: BaseApplication): SettingsDataStore {
        return SettingsDataStore(baseApplication)
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

}