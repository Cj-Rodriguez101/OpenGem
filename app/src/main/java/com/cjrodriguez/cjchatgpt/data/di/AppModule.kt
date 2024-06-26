package com.cjrodriguez.cjchatgpt.data.di

import android.content.Context
import com.cjrodriguez.cjchatgpt.data.datasource.audio.Player
import com.cjrodriguez.cjchatgpt.data.datasource.audio.PlayerImpl
import com.cjrodriguez.cjchatgpt.data.datasource.audio.Recorder
import com.cjrodriguez.cjchatgpt.data.datasource.audio.RecorderImpl
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatDatabase
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatDatabaseInitializer
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStoreInitializer
import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStore
import com.cjrodriguez.cjchatgpt.data.datasource.network.gemini.GeminiModelApi
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
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
    fun provideOpenApiConfig(): OpenApiConfig {
        return OpenApiConfig
    }

    @Singleton
    @Provides
    fun provideGeminiModelApi(): GeminiModelApi {
        return GeminiModelApi
    }

    @Singleton
    @Provides
    fun provideChatDatabase(@ApplicationContext app: Context): ChatDatabase {
        return ChatDatabaseInitializer().create(app)
    }

    @Singleton
    @Provides
    fun provideSongDao(chatDatabase: ChatDatabase): ChatTopicDao {
        return chatDatabase.chatTopicDao()
    }

    @Singleton
    @Provides
    fun provideSettingsDatastore(baseApplication: BaseApplication): SettingsDataStore {
        return SettingsDataStoreInitializer().create(baseApplication)
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    @Singleton
    @Provides
    fun provideMediaRecorder(
        baseApplication: BaseApplication
    ): Recorder {
        return RecorderImpl(baseApplication)
    }

    @Singleton
    @Provides
    fun provideMediaPlayer(
    ): Player {
        return PlayerImpl
    }
}