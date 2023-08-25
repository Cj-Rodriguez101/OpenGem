package com.cjrodriguez.cjchatgpt.data.di

import android.content.ClipboardManager
import android.content.Context
import com.cjrodriguez.cjchatgpt.data.datasource.cache.ChatTopicDao
import com.cjrodriguez.cjchatgpt.data.datasource.dataStore.SettingsDataStore
import com.cjrodriguez.cjchatgpt.data.datasource.network.OpenApiConfig
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.NetworkConnectivityObserver
import com.cjrodriguez.cjchatgpt.interactors.CopyTextToClipBoard
import com.cjrodriguez.cjchatgpt.interactors.DeleteTopicAndChats
import com.cjrodriguez.cjchatgpt.interactors.GetChatResponse
import com.cjrodriguez.cjchatgpt.interactors.RenameTopic
import com.cjrodriguez.cjchatgpt.data.repository.chat.ChatRepository
import com.cjrodriguez.cjchatgpt.data.repository.chat.ChatRepositoryImpl
import com.cjrodriguez.cjchatgpt.data.repository.topic.TopicRepository
import com.cjrodriguez.cjchatgpt.data.repository.topic.TopicRepositoryImpl
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
    fun provideGetChatResponse(
        baseApplication: BaseApplication,
        openApiConfig: OpenApiConfig,
        chatTopicDao: ChatTopicDao
    ): GetChatResponse {
        return GetChatResponse(
            baseApplication.applicationContext,
            openApiConfig,
            chatTopicDao,
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
    fun provideChatRepository(
        getChatResponse: GetChatResponse,
        copyTextToClipBoard: CopyTextToClipBoard,
        settingsDataStore: SettingsDataStore,
        chatTopicDao: ChatTopicDao,
    ): ChatRepository {
        return ChatRepositoryImpl(
            getChatResponse, copyTextToClipBoard,
            chatTopicDao,
            settingsDataStore
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
    fun provideLocalClipBoardManager(baseApplication: BaseApplication): ClipboardManager {
        return baseApplication.applicationContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @ViewModelScoped
    @Provides
    fun provideNetworkObserver(baseApplication: BaseApplication): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(baseApplication.applicationContext)
    }
}