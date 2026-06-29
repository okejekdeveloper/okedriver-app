package id.okejack.okedriver.feature.order.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.okejack.okedriver.feature.order.data.remote.OrderApiService
import id.okejack.okedriver.feature.order.data.repository.OrderRepositoryImpl
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrderModule {

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    companion object {
        @Provides
        @Singleton
        fun provideOrderFeatureApiService(retrofit: Retrofit): OrderApiService =
            retrofit.create(OrderApiService::class.java)
    }
}
