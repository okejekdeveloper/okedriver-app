package id.okejack.okedriver.feature.news.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.okejack.okedriver.feature.news.data.remote.NewsFeatureApiService
import id.okejack.okedriver.feature.news.data.repository.NewsRepositoryImpl
import id.okejack.okedriver.feature.news.domain.repository.NewsRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NewsModule {
    @Binds
    @Singleton
    abstract fun bindNewsRepository(impl: NewsRepositoryImpl): NewsRepository

    companion object {
        @Provides
        @Singleton
        fun provideNewsFeatureApiService(retrofit: Retrofit): NewsFeatureApiService =
            retrofit.create(NewsFeatureApiService::class.java)
    }
}
