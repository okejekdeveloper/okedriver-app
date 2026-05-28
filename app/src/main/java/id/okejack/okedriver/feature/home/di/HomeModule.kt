package id.okejack.okedriver.feature.home.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.okejack.okedriver.feature.home.data.remote.HomeApiService
import id.okejack.okedriver.feature.home.data.repository.HomeRepositoryImpl
import id.okejack.okedriver.feature.home.domain.repository.HomeRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    companion object {
        @Provides
        @Singleton
        fun provideHomeApiService(retrofit: Retrofit): HomeApiService =
            retrofit.create(HomeApiService::class.java)
    }
}
