package com.cjrodriguez.cjchatgpt.data.di

import android.content.ClipboardManager
import android.content.Context
import com.cjrodriguez.cjchatgpt.data.datasource.audio.Player
import com.cjrodriguez.cjchatgpt.data.datasource.audio.Recorder
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStore
import com.cjrodriguez.cjchatgpt.data.datasource.network.gemini.GeminiModelApi
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.NetworkConnectivityObserver
import com.cjrodriguez.cjchatgpt.data.datasource.network.open_ai.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.repository.chat.ChatRepository
import com.cjrodriguez.cjchatgpt.data.repository.chat.ChatRepositoryImpl
import com.cjrodriguez.cjchatgpt.data.repository.settings.SettingsRepository
import com.cjrodriguez.cjchatgpt.data.repository.settings.SettingsRepositoryImpl
import com.cjrodriguez.cjchatgpt.data.repository.topic.TopicRepository
import com.cjrodriguez.cjchatgpt.data.repository.topic.TopicRepositoryImpl
import com.cjrodriguez.cjchatgpt.interactors.CopyTextToClipBoard
import com.cjrodriguez.cjchatgpt.interactors.DeleteTopicAndChats
import com.cjrodriguez.cjchatgpt.interactors.GetAndPlayAiResponse
import com.cjrodriguez.cjchatgpt.interactors.GetGeminiChatResponse
import com.cjrodriguez.cjchatgpt.interactors.GetOpenAiChatResponse
import com.cjrodriguez.cjchatgpt.interactors.GetTextFromSpeech
import com.cjrodriguez.cjchatgpt.interactors.RenameTopic
import com.cjrodriguez.cjchatgpt.interactors.SaveGeneratedImage
import com.cjrodriguez.cjchatgpt.presentation.BaseApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object InteractorsModule {

    @ViewModelScoped
    @Provides
    fun provideGetOpenAiChatResponse(
        baseApplication: BaseApplication,
        openApiConfig: OpenApiConfig,
        chatTopicDao: ChatTopicDao,
        settingsDataStore: SettingsDataStore
    ): GetOpenAiChatResponse {
        return GetOpenAiChatResponse(
            baseApplication.applicationContext,
            openApiConfig,
            chatTopicDao,
            settingsDataStore
        )
    }

    @ViewModelScoped
    @Provides
    fun provideGetGeminiChatResponse(
        baseApplication: BaseApplication,
        geminiModelApi: GeminiModelApi,
        chatTopicDao: ChatTopicDao,
        settingsDataStore: SettingsDataStore
    ): GetGeminiChatResponse {
        return GetGeminiChatResponse(
            baseApplication.applicationContext,
            geminiModelApi,
            chatTopicDao,
            settingsDataStore
        )
    }

    @ViewModelScoped
    @Provides
    fun provideGetAndPlayAiResponse(
        baseApplication: BaseApplication,
        openApiConfig: OpenApiConfig,
        geminiModelApi: GeminiModelApi,
        chatTopicDao: ChatTopicDao,
        settingsDataStore: SettingsDataStore
    ): GetAndPlayAiResponse {
        return GetAndPlayAiResponse(
            baseApplication.applicationContext,
            openApiConfig,
            geminiModelApi,
            chatTopicDao,
            settingsDataStore
        )
    }

    @ViewModelScoped
    @Provides
    fun provideDeleteTopicAndChats(
        baseApplication: BaseApplication,
        chatTopicDao: ChatTopicDao
    ): DeleteTopicAndChats {
        return DeleteTopicAndChats(
            baseApplication.applicationContext,
            chatTopicDao
        )
    }

    @ViewModelScoped
    @Provides
    fun provideRenameTopic(
        baseApplication: BaseApplication,
        chatTopicDao: ChatTopicDao
    ): RenameTopic {
        return RenameTopic(
            baseApplication.applicationContext,
            chatTopicDao
        )
    }

    @ViewModelScoped
    @Provides
    fun provideCopyTextToClipBoard(
        baseApplication: BaseApplication,
        clipboardManager: ClipboardManager
    ): CopyTextToClipBoard {
        return CopyTextToClipBoard(
            baseApplication = baseApplication,
            clipboardManager = clipboardManager
        )
    }

    @ViewModelScoped
    @Provides
    fun provideSaveGeneratedImage(
        baseApplication: BaseApplication
    ): SaveGeneratedImage {
        return SaveGeneratedImage(
            context = baseApplication,
        )
    }

    @ViewModelScoped
    @Provides
    fun provideChatRepository(
        getOpenAiChatResponse: GetOpenAiChatResponse,
        getGeminiChatResponse: GetGeminiChatResponse,
        getAndPlayAiResponse: GetAndPlayAiResponse,
        getTextFromSpeech: GetTextFromSpeech,
        copyTextToClipBoard: CopyTextToClipBoard,
        saveGeneratedImage: SaveGeneratedImage,
        settingsDataStore: SettingsDataStore,
        chatTopicDao: ChatTopicDao,
        recorder: Recorder,
        player: Player
    ): ChatRepository {
        return ChatRepositoryImpl(
            getOpenAiChatResponse,
            getGeminiChatResponse,
            getAndPlayAiResponse,
            getTextFromSpeech,
            copyTextToClipBoard,
            saveGeneratedImage,
            chatTopicDao,
            settingsDataStore,
            recorder,
            player
        )
    }

    @ViewModelScoped
    @Provides
    fun provideTopicRepository(
        chatTopicDao: ChatTopicDao,
        deleteTopicAndChats: DeleteTopicAndChats,
        renameTopic: RenameTopic
    ): TopicRepository {
        return TopicRepositoryImpl(
            chatTopicDao, deleteTopicAndChats, renameTopic
        )
    }

    @ViewModelScoped
    @Provides
    fun provideSettingsRepository(
        settingsDataStore: SettingsDataStore
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    @ViewModelScoped
    @Provides
    fun provideLocalClipBoardManager(baseApplication: BaseApplication): ClipboardManager {
        return baseApplication.applicationContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @ViewModelScoped
    @Provides
    fun provideGetTextFromSpeech(
        baseApplication: BaseApplication,
        openApiConfig: OpenApiConfig,
        settingsDataStore: SettingsDataStore
    ): GetTextFromSpeech {
        return GetTextFromSpeech(
            baseApplication.applicationContext,
            openApiConfig,
            settingsDataStore
        )
    }

    @ViewModelScoped
    @Provides
    fun provideNetworkObserver(baseApplication: BaseApplication): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(baseApplication.applicationContext)
    }
}