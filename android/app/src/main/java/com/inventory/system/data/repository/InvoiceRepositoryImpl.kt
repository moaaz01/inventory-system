package com.inventory.system.data.repository

import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.data.remote.dto.CreateInvoiceDto
import com.inventory.system.data.remote.dto.InvoiceDto
import com.inventory.system.data.remote.dto.InvoiceItemCreateDto
import com.inventory.system.domain.model.CartItem
import com.inventory.system.domain.model.Invoice
import com.inventory.system.domain.model.InvoiceItem
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.repository.InvoiceRepository
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val api: InventoryApiService
) : InvoiceRepository {

    override suspend fun createInvoice(customerName: String?, discount: Double, items: List<CartItem>): Result<Invoice> = safeApiCall {
        val dto = api.createInvoice(
            CreateInvoiceDto(
                customer_name = customerName,
                discount = discount,
                items = items.map { InvoiceItemCreateDto(it.productId, it.quantity, it.unitPrice) }
            )
        )
        dto.toDomain()
    }

    override suspend fun getInvoices(skip: Int, limit: Int, startDate: String?, endDate: String?, search: String?): Result<List<Invoice>> = safeApiCall {
        api.getInvoices(skip, limit, startDate, endDate, search).map { it.toDomain() }
    }

    override suspend fun getInvoice(id: Int): Result<Invoice> = safeApiCall {
        api.getInvoice(id).toDomain()
    }
}

fun InvoiceDto.toDomain() = Invoice(
    id = id,
    invoiceNumber = invoice_number,
    customerName = customer_name,
    subtotal = subtotal,
    discount = discount,
    total = total,
    status = status,
    createdAt = created_at,
    items = items.map { item ->
        InvoiceItem(
            id = item.id,
            productId = item.product_id,
            productName = item.product_name,
            productSku = item.product_sku,
            quantity = item.quantity,
            unitPrice = item.unit_price,
            totalPrice = item.total_price
        )
    }
)
