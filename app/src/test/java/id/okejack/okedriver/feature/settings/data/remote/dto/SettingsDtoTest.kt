package id.okejack.okedriver.feature.settings.data.remote.dto

import com.google.gson.Gson
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDtoTest {
    @Test
    fun `maps driver bid and connected account`() {
        val response = Gson().fromJson(
            """
            {
              "success": true,
              "data": {
                "driver": {
                  "id": 12,
                  "driver_type": 0,
                  "driver_bid": {
                    "preferred_distance": 10,
                    "preferred_payments": 250000,
                    "prefer_type_ride": true,
                    "prefer_type_delivery": false,
                    "prefer_type_shopping": true,
                    "prefer_type_food": true
                  },
                  "user_id": 91,
                  "user": {
                    "id": 91,
                    "name": "Customer Oke",
                    "email": "customer@example.com",
                    "contact_phone": "08123456789"
                  }
                }
              }
            }
            """.trimIndent(),
            SettingsResponseDto::class.java
        )

        val settings = response.data.driver.toDomain()
        assertEquals(250_000, settings.bid.preferredPayment)
        assertFalse(settings.bid.deliveryEnabled)
        assertEquals("Customer Oke", settings.connectedUser?.name)
    }

    @Test
    fun `maps bid preferences to legacy integer payload`() {
        val request = DriverBidPreferences(
            preferredPayment = 80_000,
            preferredDistance = 6,
            rideEnabled = true,
            deliveryEnabled = false,
            shoppingEnabled = true,
            foodEnabled = false
        ).toRequestDto()

        assertEquals(1, request.rideEnabled)
        assertEquals(0, request.deliveryEnabled)
        assertTrue(request.preferredDistance == 6)
    }
}
