package com.application.okedriver.ui.model

/**
 * Represents a single driver order entry in the history list.
 */
data class OrderHistoryModel(
    val id: String,
    val serviceType: String,        // e.g. "Oke Car", "Ride"
    val vehicleType: VehicleType,
    val distanceKm: Int,
    val date: String,               // e.g. "2026/4/13"
    val pickupAddress: String,
    val dropAddress: String,
    val price: Double,
    val serviceFee: Double = 50_000.0,
    val merchantPayment: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val status: OrderStatus
)

enum class VehicleType   { CAR, MOTORBIKE }
enum class OrderStatus   { IN_PROGRESS, FINISHED }
enum class PaymentMethod { CASH, TRANSFER, OVO, DANA }

/**
 * In-memory sample data. Replace with repository / API calls in production.
 */
object OrderHistorySampleData {
    val orders = listOf(
        OrderHistoryModel(
            id            = "3503543",
            serviceType   = "Oke Car",
            vehicleType   = VehicleType.CAR,
            distanceKm    = 11,
            date          = "2026/4/13",
            pickupAddress = "Jl. C3 No.831, Bekasi Barat, Jawa Barat",
            dropAddress   = "Jl. Raya Kampung Melayu, Jakarta Timur",
            price         = 154_600.0,
            serviceFee    = 50_000.0,
            merchantPayment = 0.0,
            status        = OrderStatus.FINISHED
        ),
        OrderHistoryModel(
            id            = "3503544",
            serviceType   = "Ride",
            vehicleType   = VehicleType.MOTORBIKE,
            distanceKm    = 2,
            date          = "2026/4/12",
            pickupAddress = "Mall Grand Indonesia Lt. 3, Jakarta Pusat",
            dropAddress   = "Stasiun Gambir, Jakarta Pusat",
            price         = 9_500.0,
            serviceFee    = 2_000.0,
            merchantPayment = 0.0,
            status        = OrderStatus.FINISHED
        ),
        OrderHistoryModel(
            id            = "3503540",
            serviceType   = "Oke Car",
            vehicleType   = VehicleType.CAR,
            distanceKm    = 7,
            date          = "2026/4/11",
            pickupAddress = "SCBD Lot 1, Jl. Jend. Sudirman, Jakarta Selatan",
            dropAddress   = "Kemang Village Mall, Jakarta Selatan",
            price         = 87_300.0,
            serviceFee    = 30_000.0,
            merchantPayment = 0.0,
            status        = OrderStatus.FINISHED
        ),
        OrderHistoryModel(
            id            = "3503548",
            serviceType   = "Ride",
            vehicleType   = VehicleType.MOTORBIKE,
            distanceKm    = 3,
            date          = "2026/4/14",
            pickupAddress = "Blok M Square, Jakarta Selatan",
            dropAddress   = "Gandaria City Mall, Jakarta Selatan",
            price         = 22_000.0,
            serviceFee    = 5_000.0,
            merchantPayment = 0.0,
            status        = OrderStatus.IN_PROGRESS
        ),
        OrderHistoryModel(
            id            = "3503535",
            serviceType   = "Oke Car",
            vehicleType   = VehicleType.CAR,
            distanceKm    = 15,
            date          = "2026/4/10",
            pickupAddress = "Bandara Soekarno-Hatta Terminal 3, Tangerang",
            dropAddress   = "Senayan City Mall, Jakarta Pusat",
            price         = 210_000.0,
            serviceFee    = 50_000.0,
            merchantPayment = 0.0,
            status        = OrderStatus.FINISHED
        )
    )

    fun findById(id: String): OrderHistoryModel? = orders.find { it.id == id }
}
