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
    /** Whole kilometres, as the order API sends it; 0 means unknown. Format with formatOrderDistance. */
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
    val shoppingEditedAt: String,
    val paymentProvider: String = "",
    val paymentStatus: Int = 0,
    val expressFee: BigDecimal = BigDecimal.ZERO,
    val originalTotalShopping: BigDecimal = BigDecimal.ZERO,
    val encodedRoute: String = "",
    val customerScore: CustomerScore? = null,
    val cancellation: OrderCancellation? = null,
    val merchant: OrderMerchant? = null,
    val packageInfo: OrderPackageInfo? = null,
    val additionalPayments: List<OrderCharge> = emptyList(),
    val expressVerificationCode: String = ""
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

    val totalFee: BigDecimal
        get() = (if (totalDiscount > BigDecimal.ZERO) fee else originalFee)
            .add(if (type == TYPE_EXPRESS) expressFee else BigDecimal.ZERO)
            .coerceAtLeast(BigDecimal.ZERO)

    val totalPaid: BigDecimal
        get() = totalFee.add(totalShopping).coerceAtLeast(BigDecimal.ZERO)

    val canCancel: Boolean
        get() = status == STATUS_PENDING

    val canEditShopping: Boolean
        get() = status in listOf(STATUS_PENDING, STATUS_ASSIGNED, STATUS_DEPARTED) && shoppingEditedAt.isBlank()

    val canUpdateStatus: Boolean
        get() = status == STATUS_PENDING || status == STATUS_ASSIGNED || status == STATUS_DEPARTED

    val nextActionLabel: String
        get() = if (status == STATUS_DEPARTED) "Selesaikan" else "Mulai"

    val requiresQrisPayment: Boolean
        get() = status == STATUS_DEPARTED && paymentProvider == "qris" && paymentStatus != PAYMENT_SUCCESS

    val isQrisPaidInTransit: Boolean
        get() = status == STATUS_DEPARTED && paymentProvider == "qris" && paymentStatus == PAYMENT_SUCCESS

    companion object {
        const val PAYMENT_SUCCESS = 2
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
        const val CANCEL_REASON_IGNORED = 99
        const val CANCEL_REASON_SHOP_CLOSED = 101
        const val CANCEL_REASON_CUSTOMER_CANCEL = 102
        const val CANCEL_REASON_LOCATION_UNREACHABLE = 103

        val currentOrderCancelReasons: List<CancelReasonOption> = listOf(
            CancelReasonOption("Customer tidak dapat dihubungi", CANCEL_REASON_NO_CONTACT),
            CancelReasonOption("Toko tutup", CANCEL_REASON_SHOP_CLOSED),
            CancelReasonOption("Customer membatalkan", CANCEL_REASON_CUSTOMER_CANCEL),
            CancelReasonOption("Lokasi tidak dapat dijangkau", CANCEL_REASON_LOCATION_UNREACHABLE)
        )
    }
}

data class CancelReasonOption(
    val label: String,
    val code: Int
)

data class OrderLineItem(
    val id: Long,
    val name: String,
    val detail: String,
    val quantity: Int,
    val price: BigDecimal,
    val type: Int = EditableItem.TYPE_ITEM
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

data class CustomerScore(val score: Int, val review: String)
data class OrderCancellation(val actor: Int, val reason: String, val cancelledAt: String)
data class OrderMerchant(val name: String, val address: String, val phone: String)
data class OrderPackageInfo(val name: String, val description: String, val duration: Int, val overtime: Int, val overtimeFee: BigDecimal)
data class OrderCharge(val title: String, val detail: String, val fee: BigDecimal)

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
    val isFeeType: Boolean get() = type == TYPE_PAYMENT || type == TYPE_PARKING

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_PAYMENT = 1
        const val TYPE_PARKING = 2
    }
}

fun OrderLineItem.toEditable(): EditableItem =
    EditableItem(id = id, name = name, detail = detail, quantity = quantity, price = price, type = type)
