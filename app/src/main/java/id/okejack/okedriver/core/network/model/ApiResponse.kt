package id.okejack.okedriver.core.network.model

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String, val errorCode: String? = null) : ApiResponse<Nothing>()
    data object Loading : ApiResponse<Nothing>()
}

/** Wrapper JSON standar dari server OkeJack */
data class BaseResponse<T>(
    val status: Boolean = false,
    val message: String = "",
    val data: T? = null,
    val success: Boolean = false,
    val error: String = ""
) {
    val isSuccessful: Boolean get() = success || status
}
