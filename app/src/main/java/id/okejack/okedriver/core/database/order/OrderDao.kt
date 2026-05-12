package id.okejack.okedriver.core.database.order

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM active_orders ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM active_orders WHERE id = :orderId")
    fun getById(orderId: Long): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(order: OrderEntity)

    @Query("DELETE FROM active_orders WHERE id = :orderId")
    suspend fun delete(orderId: Long)

    @Query("DELETE FROM active_orders")
    suspend fun clearAll()
}
