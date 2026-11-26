package id.okejack.okedriver.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import id.okejack.okedriver.core.database.order.OrderDao
import id.okejack.okedriver.core.database.order.OrderEntity

@Database(
    entities = [OrderEntity::class],
    version = 1,
    exportSchema = true
)
abstract class OkeDriverDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
}
