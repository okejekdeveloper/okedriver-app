package id.okejack.okedriver.feature.wallet.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance

interface WalletRepository {
    suspend fun getBalance(): ApiResponse<WalletBalance>
}
