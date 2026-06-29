package id.okejack.okedriver.feature.order.data.remote.dto

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.order.domain.model.CourierInfo
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.OrderLineItem
import java.math.BigDecimal

data class OrderResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: OrderResponseDataDto? = null
) {
    val isSuccessful: Boolean
        get() = success || status

    val errorMessage: String
        get() = error.ifBlank { message.ifBlank { "Request order gagal" } }
}

data class OrderResponseDataDto(
    val order: OrderDto? = null
)

data class OrderDto(
    val id: Long? = null,
    val status: Int? = null,
    @SerializedName("driver_id") val driverId: Long? = null,
    @SerializedName("user_id") val userId: Long? = null,
    val type: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("origin_latlng") val originLatLng: String? = null,
    @SerializedName("destination_latlng") val destinationLatLng: String? = null,
    @SerializedName("origin_address") val originAddress: String? = null,
    @SerializedName("destination_address") val destinationAddress: String? = null,
    @SerializedName("origin_address_detail") val originAddressDetail: String? = null,
    @SerializedName("destination_address_detail") val destinationAddressDetail: String? = null,
    val distance: Int? = null,
    val fee: BigDecimal? = null,
    @SerializedName("original_fee") val originalFee: BigDecimal? = null,
    @SerializedName("total_discount") val totalDiscount: BigDecimal? = null,
    @SerializedName("total_shopping") val totalShopping: BigDecimal? = null,
    @SerializedName("service_fee") val serviceFee: BigDecimal? = null,
    @SerializedName("payment_provider_name") val paymentProviderName: String? = null,
    @SerializedName("driver_info") val driverInfo: String? = null,
    val payment: OrderPaymentDto? = null,
    val user: OrderUserDto? = null,
    val courier: OrderCourierDto? = null,
    val items: List<OrderItemDto>? = null,
    @SerializedName("shopping_edited_at") val shoppingEditedAt: String? = null
)

data class OrderPaymentDto(
    @SerializedName("provider_name") val providerName: String? = null,
    val provider: String? = null,
    val amount: Int? = null,
    val status: Int? = null
)

data class OrderUserDto(
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null,
    @SerializedName("contact_phone") val contactPhone: String? = null
)

data class OrderCourierDto(
    @SerializedName("item_detail") val itemDetail: String? = null,
    @SerializedName("sender_name") val senderName: String? = null,
    @SerializedName("sender_phone") val senderPhone: String? = null,
    @SerializedName("recipient_name") val recipientName: String? = null,
    @SerializedName("recipient_phone") val recipientPhone: String? = null
)

data class OrderItemDto(
    val id: Long? = null,
    val name: String? = null,
    val detail: String? = null,
    val qty: Int? = null,
    val price: BigDecimal? = null
)

fun OrderDto.toDomain(): DriverOrder =
    DriverOrder(
        id = id ?: 0,
        status = status ?: DriverOrder.STATUS_PENDING,
        type = type ?: DriverOrder.TYPE_RIDE,
        createdAt = createdAt.orEmpty(),
        originLatLng = originLatLng.orValidLatLng(),
        destinationLatLng = destinationLatLng.orValidLatLng(),
        originAddress = originAddress.orEmpty(),
        destinationAddress = destinationAddress.orEmpty(),
        originAddressDetail = originAddressDetail.orEmpty(),
        destinationAddressDetail = destinationAddressDetail.orEmpty(),
        distance = distance ?: 0,
        fee = fee ?: BigDecimal.ZERO,
        originalFee = originalFee ?: fee ?: BigDecimal.ZERO,
        totalDiscount = totalDiscount ?: BigDecimal.ZERO,
        totalShopping = totalShopping ?: BigDecimal.ZERO,
        serviceFee = serviceFee ?: BigDecimal.ZERO,
        paymentProviderName = payment?.providerName
            .orEmpty()
            .ifBlank { paymentProviderName.orEmpty() }
            .ifBlank { "Tunai" },
        customerName = user?.name.orEmpty(),
        customerPhone = user?.contactPhone.orEmpty(),
        driverInfo = driverInfo.orEmpty(),
        courier = courier?.toDomain(),
        items = items.orEmpty().map { it.toDomain() },
        shoppingEditedAt = shoppingEditedAt.orEmpty()
    )

fun OrderCourierDto.toDomain(): CourierInfo =
    CourierInfo(
        itemDetail = itemDetail.orEmpty(),
        senderName = senderName.orEmpty(),
        senderPhone = senderPhone.orEmpty(),
        recipientName = recipientName.orEmpty(),
        recipientPhone = recipientPhone.orEmpty()
    )

fun OrderItemDto.toDomain(): OrderLineItem =
    OrderLineItem(
        id = id ?: 0,
        name = name.orEmpty(),
        detail = detail.orEmpty(),
        quantity = qty ?: 1,
        price = price ?: BigDecimal.ZERO
    )

private fun String?.orValidLatLng(): String =
    this?.takeIf { it.contains(",") } ?: "0.0,0.0"
