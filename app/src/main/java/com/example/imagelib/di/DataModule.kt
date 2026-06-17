package com.example.imagelib.di

import com.example.imagelib.data.cache.ImageLoaderCacheManager
import com.example.imagelib.data.remote.HttpImageApi
import com.example.imagelib.data.remote.ImageApi
import com.example.imagelib.data.repository.ImageRepositoryImpl
import com.example.imagelib.domain.repository.ImageCacheManager
import com.example.imagelib.domain.repository.ImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindImageApi(impl: HttpImageApi): ImageApi

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindImageCacheManager(impl: ImageLoaderCacheManager): ImageCacheManager
}