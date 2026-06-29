package id.okejack.okedriver.feature.order.data.remote.dto

import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderResponseDtoTest {

    @Test
    fun response_isSuccessful_acceptsLegacySuccessOrBaseStatus() {
        assertTrue(OrderResponseDto(success = true).isSuccessful)
        assertTrue(OrderResponseDto(status = true).isSuccessful)
        assertFalse(OrderResponseDto().isSuccessful)
    }

    @Test
    fun toDomain_mapsCoreOrderFields() {
        val order = OrderDto(
            id = 77,
            status = DriverOrder.STATUS_ASSIGNED,
            type = DriverOrder.TYPE_FOOD,
            createdAt = "2026-06-14 10:00:00",
            originLatLng = "-6.1,106.7",
            destinationLatLng = "-6.2,106.8",
            originAddress = "Resto A",
            destinationAddress = "Rumah B",
            originAddressDetail = "Lantai 1",
            destinationAddressDetail = "Gerbang utama",
            distance = 4500,
            fee = BigDecimal("12000"),
            totalShopping = BigDecimal("32000"),
            serviceFee = BigDecimal("2000"),
            totalDiscount = BigDecimal("1000"),
            payment = OrderPaymentDto(providerName = "Tunai"),
            user = OrderUserDto(name = "Andi", contactPhone = "08123"),
            items = listOf(
                OrderItemDto(
                    id = 1,
                    name = "Nasi",
                    detail = "Pedas",
                    qty = 2,
                    price = BigDecimal("15000")
                )
            )
        ).toDomain()

        assertEquals(77L, order.id)
        assertEquals("OkeFood", order.serviceLabel)
        assertEquals("Menuju penjemputan", order.statusLabel)
        assertEquals("Resto A", order.targetAddress)
        assertEquals("-6.1,106.7", order.targetLatLng)
        assertEquals("Andi", order.customerName)
        assertEquals("Tunai", order.paymentProviderName)
        assertEquals(BigDecimal("46000"), order.totalPaid)
        assertEquals(1, order.items.size)
        assertEquals(BigDecimal("30000"), order.items.first().total)
        assertTrue(order.canCancel)
        assertTrue(order.canUpdateStatus)
    }

    @Test
    fun toDomain_usesDestinationAsTargetAfterDeparted() {
        val order = OrderDto(
            id = 88,
            status = DriverOrder.STATUS_DEPARTED,
            destinationAddress = "Tujuan",
            destinationLatLng = "-6.3,106.9"
        ).toDomain()

        assertEquals("Tujuan", order.targetAddress)
        assertEquals("-6.3,106.9", order.targetLatLng)
        assertEquals("Selesaikan", order.nextActionLabel)
    }
}
