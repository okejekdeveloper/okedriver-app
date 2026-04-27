package id.okejack.okedriver.core.database.di

import android.content.Context
import androidx.room.Room
import id.okejack.okedriver.core.database.OkeDriverDatabase
import id.okejack.okedriver.core.database.order.OrderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OkeDriverDatabase =
        Room.databaseBuilder(
            context,
            OkeDriverDatabase::class.java,
            "okedriver.db"
        ).build()

    @Provides
    fun provideOrderDao(db: OkeDriverDatabase): OrderDao = db.orderDao()
}
