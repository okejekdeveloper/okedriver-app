package id.okejack.okedriver.core.network.model

import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkCallTest {
    @Test
    fun safeApiCall_returnsSuccessWhenBlockCompletes() = runBlocking {
        val response = safeApiCall { "ok" }

        assertTrue(response is ApiResponse.Success)
        assertEquals("ok", (response as ApiResponse.Success).data)
    }

    @Test
    fun safeApiCall_mapsIoExceptionToNetworkError() = runBlocking {
        val response = safeApiCall<String> { throw IOException("offline") }

        assertTrue(response is ApiResponse.Error)
        val error = response as ApiResponse.Error
        assertEquals(NETWORK_ERROR_CODE, error.code)
        assertEquals("offline", error.message)
    }
}
