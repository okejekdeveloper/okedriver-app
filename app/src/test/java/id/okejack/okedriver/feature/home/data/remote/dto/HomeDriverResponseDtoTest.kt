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
    fun toHomeDriver_okefastFollowsEnableOkefast() {
        assertEquals(true, DriverDto(enableOkefast = true).toHomeDriver().isOkefastEnabled)
        assertEquals(false, DriverDto(enableOkefast = false).toHomeDriver().isOkefastEnabled)
        assertEquals(false, DriverDto().toHomeDriver().isOkefastEnabled)
    }

    @Test
    fun toHomeDriver_attendanceIndependentFromOkefast() {
        val attendanceOnly = DriverDto(enableDriverAttendance = true, enableOkefast = false).toHomeDriver()
        assertEquals(true, attendanceOnly.isAttendanceEnabled)
        assertEquals(false, attendanceOnly.isOkefastEnabled)

        val okefastOnly = DriverDto(enableDriverAttendance = false, enableOkefast = true).toHomeDriver()
        assertEquals(false, okefastOnly.isAttendanceEnabled)
        assertEquals(true, okefastOnly.isOkefastEnabled)
    }

    @Test
    fun driverDto_parsesEnableOkefastFromJson() {
        val json = """{"id":1,"enable_driver_attendance":true,"enable_okefast":true}"""
        val dto = com.google.gson.Gson().fromJson(json, DriverDto::class.java)

        assertEquals(true, dto.enableDriverAttendance)
        assertEquals(true, dto.enableOkefast)
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
                data = HomeQueueDataDto(
                    queueNumber = 3,
                    queueTerminal = "Terminal 1",
                    queueTerminalTotal = 12,
                    queueMessage = "Antrean aktif"
                )
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
            ),
            queueEnabled = true
        )

        assertEquals("Tetap hati-hati di jalan", info.motd)
        assertEquals(3, info.queueNumber)
        assertEquals("Terminal 1", info.queueTerminal)
        assertEquals(12, info.queueTerminalTotal)
        assertEquals("Antrean aktif", info.queueMessage)
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
            init = HomeInitResponseDto(success = false),
            queueEnabled = true
        )

        assertEquals("", info.motd)
        assertEquals(0, info.queueNumber)
        assertEquals(true, info.hasQueueInfo)
        assertEquals(false, info.hasActiveOrder)
    }
}
