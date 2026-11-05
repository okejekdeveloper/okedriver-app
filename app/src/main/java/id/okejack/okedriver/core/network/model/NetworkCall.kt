package id.okejack.okedriver.core.network.model

import java.io.IOException
import retrofit2.HttpException
import kotlinx.coroutines.CancellationException

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResponse<T> {
    return try {
        ApiResponse.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        ApiResponse.Error(e.code(), e.message())
    } catch (e: IOException) {
        ApiResponse.Error(NETWORK_ERROR_CODE, e.message ?: "Koneksi gagal")
    } catch (e: Exception) {
        ApiResponse.Error(UNKNOWN_ERROR_CODE, e.message ?: "Terjadi kesalahan")
    }
}

const val NETWORK_ERROR_CODE = -1
const val UNKNOWN_ERROR_CODE = -2
