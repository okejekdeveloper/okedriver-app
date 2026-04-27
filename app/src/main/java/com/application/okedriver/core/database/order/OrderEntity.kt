package id.okejack.okedriver.core.database.order

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_orders")
data class OrderEntity(
    @PrimaryKey val id: Long,
    val orderCode: String,
    val status: String,
    val customerName: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val deliveryLat: Double,
    val deliveryLng: Double,
    val totalAmount: Double,
    val driverEarning: Double,
    val serviceType: String,
    val notes: String,
    val updatedAt: Long = System.currentTimeMillis()
)
