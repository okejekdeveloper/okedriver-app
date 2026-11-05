package id.okejack.okedriver.feature.profile.data.remote.dto

import id.okejack.okedriver.feature.profile.domain.model.BankInfo
import id.okejack.okedriver.feature.profile.domain.model.EditProfileForm
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileDtoTest {

    @Test
    fun response_isSuccessful_acceptsSuccessOrStatus() {
        assertTrue(ProfileResponseDto(success = true).isSuccessful)
        assertTrue(ProfileResponseDto(status = true).isSuccessful)
        assertFalse(ProfileResponseDto().isSuccessful)
    }

    @Test
    fun driverDto_mapsProfileAndBankFields() {
        val driver = ProfileDriverDto(
            id = 7,
            referralId = "DRV7",
            referralIdUrl = "https://okejack.id/ref/DRV7",
            name = "Arvan",
            email = "driver@example.com",
            phone = "08123",
            imageUrl = "https://cdn.example/avatar.jpg",
            vehiclePlate = "BK 1234 OK",
            vehicleBrand = "Honda",
            balance = BigDecimal("100000"),
            bank = BankDto(
                bankName = "BCA",
                bankAccount = "123456",
                bankAccountHolder = "Arvan"
            )
        ).toDomain()

        assertEquals(7L, driver.id)
        assertEquals("DRV7", driver.referralId)
        assertEquals("https://cdn.example/avatar.jpg", driver.avatarUrl)
        assertEquals("BK 1234 OK", driver.vehiclePlate)
        assertEquals("Honda", driver.vehicleBrand)
        assertEquals(BigDecimal("100000"), driver.balance)
        assertEquals("BCA", driver.bank.bankName)
        assertEquals("123456", driver.bank.bankAccount)
        assertEquals("Arvan", driver.bank.bankAccountHolder)
    }

    @Test
    fun editProfileForm_mapsLegacyRequestShape() {
        val request = EditProfileForm(
            email = "",
            password = "secret",
            bank = BankInfo(
                bankName = "Mandiri",
                bankAccount = "888",
                bankAccountHolder = "Driver"
            )
        ).toRequestDto()

        assertEquals("", request.email)
        assertEquals("secret", request.password)
        assertEquals("Mandiri", request.bank["bank_name"])
        assertEquals("888", request.bank["bank_account"])
        assertEquals("Driver", request.bank["bank_account_holder"])
    }
}
