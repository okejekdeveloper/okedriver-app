package id.okejack.okedriver.feature.order.data.remote.dto

import com.google.gson.Gson
import id.okejack.okedriver.feature.order.data.remote.OrderApiService
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.model.QrisPaymentPolicy
import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigDecimal

class OrderQrisDtoTest {
    private val gson = Gson()

    @Test fun providerAndStatusControlFinishGate() {
        for (status in 0..3) for (provider in listOf("qris", "cash", "okepay")) for (paymentStatus in 0..2) {
            val order = OrderDto(id = 1, status = status, payment = OrderPaymentDto(provider = provider, status = paymentStatus)).toDomain()
            assertEquals(status == DriverOrder.STATUS_DEPARTED && provider == "qris" && paymentStatus != 2, order.requiresQrisPayment)
            assertEquals(status == DriverOrder.STATUS_DEPARTED && provider == "qris" && paymentStatus == 2, order.isQrisPaidInTransit)
        }
    }

    @Test fun paymentRequiresExplicitServerConfirmation() {
        val response = gson.fromJson("""{"success":true,"data":{"status":"SUCCESS","amount":12345,"qr_code_url":"https://example.test/qr.png"}}""", OrderQrisResponseDto::class.java)
        assertTrue(response.isSuccessful)
        assertFalse(response.data!!.toDomain().isPaid)
        assertEquals(BigDecimal("12345"), response.data.toDomain().amount)
        val paid = gson.fromJson("""{"success":true,"data":{"is_paid":true}}""", OrderQrisResponseDto::class.java)
        assertTrue(paid.data!!.toDomain().isPaid)
    }

    @Test fun unknownOrBoundaryDistanceDoesNotBlockPayment() {
        assertFalse(QrisPaymentPolicy.isFarFromDestination(null))
        assertFalse(QrisPaymentPolicy.isFarFromDestination(500f))
        assertTrue(QrisPaymentPolicy.isFarFromDestination(501f))
    }

    @Test fun mutationContractsUseLegacyJsonBodies() {
        assertEquals("""{"order_id":42}""", gson.toJson(OrderIdBody(42)))
        assertEquals("""{"content":"Halo"}""", gson.toJson(ChatMessageBody("Halo")))
        mapOf("generateOrderQris" to "order/qris", "checkOrderQrisStatus" to "order/check-qris-status",
            "switchOrderToCash" to "order/switch-to-cash", "submitChatMessage" to "order/chats/{id}/submit").forEach { (name, endpoint) ->
            val method = OrderApiService::class.java.declaredMethods.single { it.name == name }
            assertEquals(endpoint, method.getAnnotation(POST::class.java).value)
            assertTrue(method.parameterAnnotations.any { annotations -> annotations.any { it is Body } })
        }
    }

    @Test fun amountMatchesLegacyFeeAndExpressRules() {
        val base = OrderDto(fee = BigDecimal("8000"), originalFee = BigDecimal("10000"), totalShopping = BigDecimal("30000"), serviceFee = BigDecimal("2000"))
        assertEquals(BigDecimal("40000"), base.toDomain().totalPaid)
        assertEquals(BigDecimal("38000"), base.copy(totalDiscount = BigDecimal("2000")).toDomain().totalPaid)
        assertEquals(BigDecimal("45000"), base.copy(type = DriverOrder.TYPE_EXPRESS, express = OrderExpressDto(BigDecimal("5000"))).toDomain().totalPaid)
        assertEquals(BigDecimal.ZERO, OrderDto(fee = BigDecimal("-100"), originalFee = BigDecimal("-100"), totalShopping = BigDecimal("-200")).toDomain().totalPaid)
    }
}
