package id.okejack.okedriver.feature.order.data.remote.dto

import com.google.gson.Gson
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.toEditable
import org.junit.Assert.*
import org.junit.Test

class OrderParityTest {
    private val gson = Gson()

    @Test fun existingFeeAndParkingTypesSurviveEditingRoundTrip() {
        val dto = gson.fromJson("""{"id":42,"status":1,"items":[{"id":1,"name":"Barang","type":0,"qty":2,"price":10000},{"id":2,"name":"Tol","type":1,"qty":1,"price":5000},{"id":3,"name":"Parkir","type":2,"qty":1,"price":2000}]}""", OrderDto::class.java)
        val items = dto.toDomain().items.map { it.toEditable() }
        val request = EditShoppingItemRequestDto(items.map { ShoppingItemDto(it.id, it.name, it.detail, it.quantity, it.price, it.type) })
        val sent = gson.toJsonTree(request).asJsonObject["items"].asJsonArray
        assertEquals(listOf(0, 1, 2), sent.map { it.asJsonObject["type"].asInt })
        assertEquals(listOf(false, true, true), items.map { it.isFeeType })
    }

    @Test fun detailPreservesCustomerReviewCancellationMerchantPackageAndExpressCode() {
        val dto = gson.fromJson("""{"id":42,"status":3,"score":{"score":2,"review":"Cepat"},"cancel":{"actor":2,"reason":"Customer membatalkan","cancelled_at":"2026-10-28"},"food_vendor":{"id":9,"name":"Resto","address":"Jl A","phone":"08123"},"package":{"name":"Paket A","description":"Layanan"},"duration":60,"overtime":10,"overtime_fee":5000,"payments":[{"title":"Tol","detail":"Gerbang A","fee":10000}],"express":{"fee":12000,"verification_code":"123456"},"route":"encoded"}""", OrderDto::class.java).toDomain()
        assertEquals("Cepat", dto.customerScore?.review)
        assertEquals("Customer membatalkan", dto.cancellation?.reason)
        assertEquals("08123", dto.merchant?.phone)
        assertEquals(60, dto.packageInfo?.duration)
        assertEquals("Tol", dto.additionalPayments.single().title)
        assertEquals("123456", dto.expressVerificationCode)
        assertEquals("encoded", dto.encodedRoute)
    }

    @Test fun onlyPendingOrdersCanBeCancelledAndEmptyOrdersCanBeEdited() {
        for (status in listOf(-1, 0, 1, 2, 3, 160)) {
            val order = OrderDto(id = 42, status = status).toDomain()
            assertEquals(status == DriverOrder.STATUS_PENDING, order.canCancel)
            assertEquals(status in 0..2, order.canEditShopping)
            assertFalse(order.copy(shoppingEditedAt = "2026-10-28").canEditShopping)
        }
    }
}
