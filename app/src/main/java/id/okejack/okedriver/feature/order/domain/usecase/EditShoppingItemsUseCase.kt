package id.okejack.okedriver.feature.order.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.EditableItem
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import javax.inject.Inject

class EditShoppingItemsUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: Long,
        items: List<EditableItem>
    ): ApiResponse<Unit> = repository.editShoppingItems(orderId, items)
}
