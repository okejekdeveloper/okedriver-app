package id.okejack.okedriver.feature.order.domain.model

import java.math.BigDecimal

data class DriverOrder(
    val id: Long,
    val status: Int,
    val type: Int,
    val createdAt: String,
    val originLatLng: String,
    val destinationLatLng: String,
    val originAddress: String,
    val destinationAddress: String,
    val originAddressDetail: String,
    val destinationAddressDetail: String,
    val distance: Int,
    val fee: BigDecimal,
    val originalFee: BigDecimal,
    val totalDiscount: BigDecimal,
    val totalShopping: BigDecimal,
    val serviceFee: BigDecimal,
    val paymentProviderName: String,
    val customerName: String,
    val customerPhone: String,
    val driverInfo: String,
    val courier: CourierInfo?,
    val items: List<OrderLineItem>,
    val shoppingEditedAt: String
) {
    val serviceLabel: String
        get() = when (type) {
            TYPE_RIDE -> "OkeRide"
            TYPE_DELIVERY -> "OkeSend"
            TYPE_SHOPPING -> "OkeShop"
            TYPE_FOOD -> "OkeFood"
            TYPE_CAR -> "OkeCar"
            TYPE_MART -> "OkeMart"
            TYPE_TRIKE -> "OkeTrike"
            TYPE_TRIKE_COURIER -> "OkeTrike Courier"
            TYPE_EXPRESS -> "OkeFast"
            TYPE_MASSAGE -> "OkeMassage"
            TYPE_RENTAL_CAR -> "Rental Mobil"
            else -> "Order"
        }

    val statusLabel: String
        get() = when (status) {
            STATUS_CANCELLED -> "Dibatalkan"
            STATUS_PENDING -> "Menunggu driver"
            STATUS_ASSIGNED -> "Menuju penjemputan"
            STATUS_DEPARTED -> "Dalam perjalanan"
            STATUS_ARRIVED -> "Selesai"
            STATUS_EXPRESS_PROGRESS -> "Dalam proses"
            else -> "Status $status"
        }

    val targetAddress: String
        get() = if (status == STATUS_PENDING || status == STATUS_ASSIGNED) {
            originAddress
        } else {
            destinationAddress
        }

    val targetAddressDetail: String
        get() = if (status == STATUS_PENDING || status == STATUS_ASSIGNED) {
            originAddressDetail
        } else {
            destinationAddressDetail
        }

    val targetLatLng: String
        get() = if (status == STATUS_PENDING || status == STATUS_ASSIGNED) {
            originLatLng
        } else {
            destinationLatLng
        }

    val totalPaid: BigDecimal
        get() = fee.add(totalShopping).add(serviceFee).coerceAtLeast(BigDecimal.ZERO)

    val canCancel: Boolean
        get() = status == STATUS_PENDING || status == STATUS_ASSIGNED

    val canUpdateStatus: Boolean
        get() = status == STATUS_PENDING || status == STATUS_ASSIGNED || status == STATUS_DEPARTED

    val nextActionLabel: String
        get() = if (status == STATUS_DEPARTED) "Selesaikan" else "Mulai"

    companion object {
        const val TYPE_RIDE = 0
        const val TYPE_DELIVERY = 1
        const val TYPE_SHOPPING = 2
        const val TYPE_FOOD = 3
        const val TYPE_CAR = 4
        const val TYPE_MASSAGE = 10
        const val TYPE_RENTAL_CAR = 20
        const val TYPE_MART = 100
        const val TYPE_TRIKE = 102
        const val TYPE_TRIKE_COURIER = 103
        const val TYPE_EXPRESS = 110

        const val STATUS_CANCELLED = -1
        const val STATUS_PENDING = 0
        const val STATUS_ASSIGNED = 1
        const val STATUS_DEPARTED = 2
        const val STATUS_ARRIVED = 3
        const val STATUS_EXPRESS_PROGRESS = 160

        const val CANCEL_REASON_NO_CONTACT = 100
        const val CANCEL_REASON_REJECTED = 98
    }
}

data class OrderLineItem(
    val id: Long,
    val name: String,
    val detail: String,
    val quantity: Int,
    val price: BigDecimal
) {
    val total: BigDecimal
        get() = price.multiply(BigDecimal(quantity))
}

data class CourierInfo(
    val itemDetail: String,
    val senderName: String,
    val senderPhone: String,
    val recipientName: String,
    val recipientPhone: String
)

fun BigDecimal.coerceAtLeast(minimumValue: BigDecimal): BigDecimal =
    if (this < minimumValue) minimumValue else this

data class EditableItem(
    val id: Long = 0,
    val name: String,
    val detail: String = "",
    val quantity: Int = 1,
    val price: BigDecimal = BigDecimal.ZERO,
    val type: Int = TYPE_ITEM
) {
    val total: BigDecimal get() = price.multiply(BigDecimal(quantity))
    val isFeeType: Boolean get() = type == TYPE_PAYMENT

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_PAYMENT = 1
    }
}

fun OrderLineItem.toEditable(): EditableItem =
    EditableItem(id = id, name = name, detail = detail, quantity = quantity, price = price)
