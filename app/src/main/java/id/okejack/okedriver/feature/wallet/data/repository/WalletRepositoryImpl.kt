package id.okejack.okedriver.feature.wallet.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.wallet.data.remote.WalletApiService
import id.okejack.okedriver.feature.wallet.data.remote.dto.toDomain
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance
import id.okejack.okedriver.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val api: WalletApiService,
    private val sessionDataStore: SessionDataStore
) : WalletRepository {

    override suspend fun getBalance(): ApiResponse<WalletBalance> {
        val apiToken = sessionDataStore.token.firstOrNull().orEmpty()
        if (apiToken.isBlank()) return ApiResponse.Error(401, "Session tidak valid")

        return when (val result = safeApiCall { api.balance(apiToken) }) {
            is ApiResponse.Success -> {
                val dto = result.data
                if (dto.isSuccessful && dto.data != null) {
                    ApiResponse.Success(dto.data.toDomain())
                } else {
                    ApiResponse.Error(400, dto.errorMessage)
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }
}
