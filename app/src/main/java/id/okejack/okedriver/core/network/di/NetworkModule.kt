package id.okejack.okedriver.core.network.di

import id.okejack.okedriver.core.network.retrofit.AuthInterceptor
import id.okejack.okedriver.core.network.retrofit.buildOkHttpClient
import id.okejack.okedriver.core.network.retrofit.buildRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
}
