package id.okejack.okedriver.feature.wallet.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.okejack.okedriver.feature.wallet.data.remote.WalletApiService
import id.okejack.okedriver.feature.wallet.data.repository.WalletRepositoryImpl
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    companion object {
        @Provides
        @Singleton
        fun provideWalletApiService(retrofit: Retrofit): WalletApiService =
            retrofit.create(WalletApiService::class.java)
    }
}
