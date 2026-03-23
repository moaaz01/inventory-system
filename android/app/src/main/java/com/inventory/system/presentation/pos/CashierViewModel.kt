package com.inventory.system.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.system.domain.model.CartItem
import com.inventory.system.domain.model.Product
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.InvoiceRepository
import com.inventory.system.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItemDisplay(
    val productId: Int,
    val productName: String,
    val sku: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double = unitPrice * quantity
)

data class CashierUiState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItemDisplay> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val customerName: String = "",
    val lastCreatedInvoiceId: Int? = null,
    val error: String? = null
)

@HiltViewModel
class CashierViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashierUiState())
    val uiState: StateFlow<CashierUiState> = _uiState.asStateFlow()

    fun addBySku(sku: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val product = productRepository.getProductBySku(sku)
            if (product != null) {
                addItem(product)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "المنتج غير موجود: $sku") }
            }
        }
    }

    private fun addItem(product: Product) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        val existing = currentItems.find { it.productId == product.id }
        if (existing != null) {
            val idx = currentItems.indexOf(existing)
            currentItems[idx] = existing.copy(
                quantity = existing.quantity + 1,
                totalPrice = (existing.quantity + 1) * existing.unitPrice
            )
        } else {
            currentItems.add(CartItemDisplay(
                productId = product.id,
                productName = product.name,
                sku = product.sku,
                quantity = 1.0,
                unitPrice = product.retailPrice ?: 0.0
            ))
        }
        recalculate(currentItems)
    }

    fun updateQuantity(item: CartItemDisplay, newQty: Double) {
        if (newQty <= 0) { removeFromCart(item); return }
        val items = _uiState.value.cartItems.toMutableList()
        val idx = items.indexOfFirst { it.productId == item.productId && it.sku == item.sku }
        if (idx >= 0) {
            items[idx] = items[idx].copy(quantity = newQty, totalPrice = newQty * items[idx].unitPrice)
            recalculate(items)
        }
    }

    fun removeFromCart(item: CartItemDisplay) {
        val items = _uiState.value.cartItems.filterNot { it.productId == item.productId && it.sku == item.sku }
        recalculate(items)
    }

    fun applyDiscount(amount: Double) {
        recalculate(_uiState.value.cartItems, discount = amount)
    }

    private fun recalculate(items: List<CartItemDisplay>, discount: Double = _uiState.value.discount) {
        val subtotal = items.sumOf { it.totalPrice }
        val total = (subtotal - discount).coerceAtLeast(0.0)
        _uiState.update { it.copy(cartItems = items, subtotal = subtotal, discount = discount, total = total, isLoading = false) }
    }

    fun updateCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun createInvoice() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.cartItems.isEmpty()) return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            val cartItems = state.cartItems.map { CartItem(it.productId, it.productName, it.sku, it.quantity, it.unitPrice) }
            val customerName = state.customerName.trim().ifBlank { null }
            val result = invoiceRepository.createInvoice(customerName, state.discount, cartItems)
            when (result) {
                is Result.Success -> {
                    _uiState.update { CashierUiState(lastCreatedInvoiceId = result.data.id) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearCart() {
        _uiState.update { CashierUiState() }
    }
}
