package id.okejack.okedriver.feature.order.data.remote.dto

import id.okejack.okedriver.feature.order.domain.model.OrderLineItem
import java.math.BigDecimal

data class EditShoppingItemsResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: OrderResponseDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = error.ifBlank { message.ifBlank { "Gagal menyimpan item" } }
}

data class EditShoppingItemRequestDto(
    val items: List<ShoppingItemDto>
)

data class ShoppingItemDto(
    val id: Long,
    val name: String,
    val detail: String,
    val qty: Int,
    val price: BigDecimal,
    val type: Int = TYPE_ITEM
) {
    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_PAYMENT = 1
    }
}

fun OrderLineItem.toRequestDto(): ShoppingItemDto =
    ShoppingItemDto(
        id = id,
        name = name,
        detail = detail,
        qty = quantity,
        price = price
    )
