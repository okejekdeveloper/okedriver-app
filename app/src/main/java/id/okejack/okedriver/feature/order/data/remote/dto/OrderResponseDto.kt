package id.okejack.okedriver.feature.order.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.order.domain.model.CourierInfo
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.OrderLineItem
import java.math.BigDecimal
import id.okejack.okedriver.feature.order.domain.model.CustomerScore
import id.okejack.okedriver.feature.order.domain.model.OrderCancellation
import id.okejack.okedriver.feature.order.domain.model.OrderMerchant
import id.okejack.okedriver.feature.order.domain.model.OrderPackageInfo
import id.okejack.okedriver.feature.order.domain.model.OrderCharge

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
        get() = ServerErrors.message(error, message, "Request order gagal")
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
    @SerializedName("shopping_edited_at") val shoppingEditedAt: String? = null,
    val express: OrderExpressDto? = null,
    @SerializedName("original_total_shopping") val originalTotalShopping: BigDecimal? = null,
    val route: String? = null,
    val score: OrderScoreDto? = null,
    val cancel: OrderCancellationDto? = null,
    @SerializedName("food_vendor") val foodVendor: OrderMerchantDto? = null,
    @SerializedName("package") val orderPackage: OrderPackageDto? = null,
    val duration: Int? = null,
    val overtime: Int? = null,
    @SerializedName("overtime_fee") val overtimeFee: BigDecimal? = null,
    val payments: List<OrderChargeDto>? = null
)

data class OrderPaymentDto(
    @SerializedName("provider_name") val providerName: String? = null,
    val provider: String? = null,
    val amount: Int? = null,
    val status: Int? = null
)

data class OrderExpressDto(val fee: BigDecimal? = null, @SerializedName("verification_code") val verificationCode: String? = null)
data class OrderScoreDto(val score: Int? = null, val review: String? = null)
data class OrderCancellationDto(val actor: Int? = null, val reason: String? = null, @SerializedName("cancelled_at") val cancelledAt: String? = null)
data class OrderMerchantDto(val id: Long? = null, val name: String? = null, val address: String? = null, val phone: String? = null)
data class OrderPackageDto(val name: String? = null, val description: String? = null)
data class OrderChargeDto(val title: String? = null, val detail: String? = null, val fee: BigDecimal? = null)

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
    val price: BigDecimal? = null,
    val type: Int? = null
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
        shoppingEditedAt = shoppingEditedAt.orEmpty(),
        paymentProvider = payment?.provider.orEmpty(),
        paymentStatus = payment?.status ?: 0,
        expressFee = express?.fee ?: BigDecimal.ZERO,
        originalTotalShopping = originalTotalShopping ?: totalShopping ?: BigDecimal.ZERO,
        encodedRoute = route.orEmpty(),
        customerScore = score?.let { CustomerScore(it.score ?: 0, it.review.orEmpty()) },
        cancellation = cancel?.let { OrderCancellation(it.actor ?: 0, it.reason.orEmpty(), it.cancelledAt.orEmpty()) },
        merchant = foodVendor?.takeIf { (it.id ?: 0) > 0 }?.let { OrderMerchant(it.name.orEmpty(), it.address.orEmpty(), it.phone.orEmpty()) },
        packageInfo = orderPackage?.let { OrderPackageInfo(it.name.orEmpty(), it.description.orEmpty(), duration ?: 0, overtime ?: 0, overtimeFee ?: BigDecimal.ZERO) },
        additionalPayments = payments.orEmpty().map { OrderCharge(it.title.orEmpty(), it.detail.orEmpty(), it.fee ?: BigDecimal.ZERO) },
        expressVerificationCode = express?.verificationCode.orEmpty()
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
        price = price ?: BigDecimal.ZERO,
        type = type ?: 0
    )

private fun String?.orValidLatLng(): String =
    this?.takeIf { it.contains(",") } ?: "0.0,0.0"
