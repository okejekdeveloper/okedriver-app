package id.okejack.okedriver.feature.settings.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.okejack.okedriver.feature.settings.data.remote.SettingsFeatureApiService
import id.okejack.okedriver.feature.settings.data.repository.SettingsRepositoryImpl
import id.okejack.okedriver.feature.settings.domain.repository.SettingsRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideSettingsFeatureApiService(retrofit: Retrofit): SettingsFeatureApiService =
            retrofit.create(SettingsFeatureApiService::class.java)
    }
}
