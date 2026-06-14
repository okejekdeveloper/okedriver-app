package id.okejack.okedriver.feature.home.data.remote.dto

import id.okejack.okedriver.feature.auth.data.remote.dto.DriverDto
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeDriverResponseDtoTest {

    @Test
    fun toHomeDriver_mapsLegacyDriverFields() {
        val dto = DriverDto(
            id = 7,
            name = "Budi",
            email = "budi@example.com",
            phone = "081234",
            imageUrl = "https://example.com/avatar.jpg",
            vehiclePlate = "B 1234 OK",
            vehicleType = 2,
            cityId = 10,
            driverType = 3,
            balance = BigDecimal("15000"),
            withdrawableBalance = BigDecimal("12000"),
            online = true
        )

        val driver = dto.toHomeDriver()

        assertEquals(7, driver.id)
        assertEquals("Budi", driver.name)
        assertEquals("081234", driver.phone)
        assertEquals("https://example.com/avatar.jpg", driver.avatarUrl)
        assertEquals("B 1234 OK", driver.vehiclePlate)
        assertEquals(BigDecimal("15000"), driver.balance)
        assertEquals(BigDecimal("12000"), driver.withdrawableBalance)
        assertEquals(true, driver.isOnline)
    }

    @Test
    fun toHomeDriver_usesNullAvatarWhenImageFieldsAreBlank() {
        val driver = DriverDto().toHomeDriver()

        assertNull(driver.avatarUrl)
    }

    @Test
    fun response_isSuccessful_acceptsLegacySuccessOrBaseStatus() {
        assertEquals(true, HomeDriverResponseDto(success = true).isSuccessful)
        assertEquals(true, HomeDriverResponseDto(status = true).isSuccessful)
        assertEquals(false, HomeDriverResponseDto().isSuccessful)
    }

    @Test
    fun toHomeSupportInfo_mapsMotdAndQueueWhenSuccessful() {
        val info = toHomeSupportInfo(
            motd = HomeMotdResponseDto(
                success = true,
                data = HomeMotdDataDto(motd = "Tetap hati-hati di jalan")
            ),
            queue = HomeQueueResponseDto(
                success = true,
                data = HomeQueueDataDto(queueNumber = 3, queueTerminal = "Terminal 1")
            ),
            currentOrder = HomeCurrentOrderResponseDto(
                success = true,
                data = HomeCurrentOrderDataDto(order = HomeCurrentOrderDto(id = 99))
            ),
            init = HomeInitResponseDto(
                success = true,
                data = HomeInitDataDto(
                    initSettings = HomeInitSettingsDto(
                        currentAppVersionCode = 5000,
                        currentAppVersion = "5.0.0",
                        paymentBiddingRange = listOf(1000, 2000),
                        distanceBiddingRange = listOf(1, 2)
                    )
                )
            )
        )

        assertEquals("Tetap hati-hati di jalan", info.motd)
        assertEquals(3, info.queueNumber)
        assertEquals("Terminal 1", info.queueTerminal)
        assertEquals(true, info.hasQueueInfo)
        assertEquals(99L, info.activeOrderId)
        assertEquals(true, info.hasActiveOrder)
        assertEquals(5000, info.latestVersionCode)
        assertEquals("5.0.0", info.latestVersionName)
        assertEquals(true, info.hasAppUpdate(4999))
        assertEquals(false, info.hasAppUpdate(5000))
    }

    @Test
    fun toHomeSupportInfo_ignoresFailedPartialResponses() {
        val info = toHomeSupportInfo(
            motd = HomeMotdResponseDto(success = false, data = HomeMotdDataDto(motd = "hidden")),
            queue = HomeQueueResponseDto(success = true, data = HomeQueueDataDto(queueNumber = 0)),
            currentOrder = HomeCurrentOrderResponseDto(success = true),
            init = HomeInitResponseDto(success = false)
        )

        assertEquals("", info.motd)
        assertEquals(0, info.queueNumber)
        assertEquals(true, info.hasQueueInfo)
        assertEquals(false, info.hasActiveOrder)
    }
}
