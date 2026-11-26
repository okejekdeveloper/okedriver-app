package id.okejack.okedriver.feature.order.presentation.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.EditableItem
import id.okejack.okedriver.feature.order.domain.model.toEditable
import id.okejack.okedriver.feature.order.domain.usecase.EditShoppingItemsUseCase
import id.okejack.okedriver.feature.order.domain.usecase.GetOrderDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class EditShoppingItemsViewModel @Inject constructor(
    private val getOrderDetailUseCase: GetOrderDetailUseCase,
    private val editShoppingItemsUseCase: EditShoppingItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditShoppingItemsUiState())
    val state: StateFlow<EditShoppingItemsUiState> = _state

    fun loadOrder(orderId: Long) {
        if (_state.value.isLoading || _state.value.isSubmitting) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getOrderDetailUseCase(orderId)) {
                is ApiResponse.Success -> {
                    val order = result.data
                    _state.update {
                        it.copy(
                            orderId = order.id,
                            items = order.items.map { item -> item.toEditable() },
                            isAlreadyEdited = !order.canEditShopping,
                            isLoading = false
                        )
                    }
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun addItem(name: String, detail: String, quantity: Int, price: BigDecimal) {
        if (!canModify() || name.isBlank() || quantity < 1 || price < BigDecimal.ZERO) return
        _state.update {
            it.copy(
                items = it.items + EditableItem(
                    name = name, detail = detail, quantity = quantity, price = price
                ),
                isDirty = true
            )
        }
    }

    fun addFeeItem(name: String, price: BigDecimal) {
        if (!canModify() || name.isBlank() || price <= BigDecimal.ZERO) return
        _state.update {
            it.copy(
                items = it.items + EditableItem(
                    name = name,
                    quantity = 1,
                    price = price,
                    type = EditableItem.TYPE_PAYMENT
                ),
                isDirty = true
            )
        }
    }

    fun updateItem(index: Int, name: String, detail: String, quantity: Int, price: BigDecimal) {
        if (!canModify() || name.isBlank() || quantity < 1 || price < BigDecimal.ZERO) return
        val items = _state.value.items.toMutableList()
        if (index < 0 || index >= items.size) return
        items[index] = items[index].copy(
            name = name, detail = detail, quantity = quantity, price = price
        )
        _state.update { it.copy(items = items, isDirty = true) }
    }

    fun updateFeeItem(index: Int, price: BigDecimal) {
        if (!canModify() || price <= BigDecimal.ZERO) return
        val items = _state.value.items.toMutableList()
        if (index < 0 || index >= items.size) return
        items[index] = items[index].copy(price = price)
        _state.update { it.copy(items = items, isDirty = true) }
    }

    fun removeItem(index: Int) {
        if (!canModify()) return
        val items = _state.value.items.toMutableList()
        if (index < 0 || index >= items.size) return
        items.removeAt(index)
        _state.update { it.copy(items = items, isDirty = true) }
    }

    fun submitItems(onSuccess: () -> Unit) {
        if (!canModify()) return
        val s = _state.value
        if (s.items.isEmpty()) {
            _state.update { it.copy(errorMessage = "Item tidak boleh kosong") }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = editShoppingItemsUseCase(s.orderId, s.items)) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(isSubmitting = false, isDirty = false, isAlreadyEdited = true) }
                    onSuccess()
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }

    private fun canModify(): Boolean = _state.value.let {
        it.orderId > 0 && !it.isLoading && !it.isSubmitting && !it.isAlreadyEdited
    }
}

data class EditShoppingItemsUiState(
    val orderId: Long = 0L,
    val items: List<EditableItem> = emptyList(),
    val isAlreadyEdited: Boolean = true,
    val isDirty: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
