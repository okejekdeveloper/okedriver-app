package id.okejack.okedriver.core.network.di

import id.okejack.okedriver.core.network.retrofit.AuthInterceptor
import id.okejack.okedriver.core.network.retrofit.buildOkHttpClient
import id.okejack.okedriver.core.network.retrofit.buildRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.okejack.okedriver.core.network.api.BalanceApiService
import id.okejack.okedriver.core.network.api.CommonApiService
import id.okejack.okedriver.core.network.api.DriverApiService
import id.okejack.okedriver.core.network.api.FcmApiService
import id.okejack.okedriver.core.network.api.NewsApiService
import id.okejack.okedriver.core.network.api.OrderApiService
import id.okejack.okedriver.core.network.api.SettingsApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        buildOkHttpClient(authInterceptor)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        buildRetrofit(okHttpClient)

    @Provides
    @Singleton
    fun provideCommonApiService(retrofit: Retrofit): CommonApiService =
        retrofit.create(CommonApiService::class.java)

    @Provides
    @Singleton
    fun provideDriverApiService(retrofit: Retrofit): DriverApiService =
        retrofit.create(DriverApiService::class.java)

    @Provides
    @Singleton
    fun provideOrderApiService(retrofit: Retrofit): OrderApiService =
        retrofit.create(OrderApiService::class.java)

    @Provides
    @Singleton
    fun provideBalanceApiService(retrofit: Retrofit): BalanceApiService =
        retrofit.create(BalanceApiService::class.java)

    @Provides
    @Singleton
    fun provideFcmApiService(retrofit: Retrofit): FcmApiService =
        retrofit.create(FcmApiService::class.java)

    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService =
        retrofit.create(NewsApiService::class.java)

    @Provides
    @Singleton
    fun provideSettingsApiService(retrofit: Retrofit): SettingsApiService =
        retrofit.create(SettingsApiService::class.java)
}
