package id.okejack.okedriver.feature.news.data.repository

import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.network.model.safeApiCall
import id.okejack.okedriver.feature.news.data.remote.NewsFeatureApiService
import id.okejack.okedriver.feature.news.data.remote.dto.toPage
import id.okejack.okedriver.feature.news.domain.model.DriverNews
import id.okejack.okedriver.feature.news.domain.model.NewsPage
import id.okejack.okedriver.feature.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsFeatureApiService,
    private val sessionDataStore: SessionDataStore
) : NewsRepository {
    override suspend fun getNews(page: Int): ApiResponse<NewsPage> {
        val token = tokenOrNull() ?: return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.getNews(token, page) }) {
            is ApiResponse.Success -> {
                val response = result.data
                if (response.isSuccessful) ApiResponse.Success(response.data.toPage(page))
                else ApiResponse.Error(400, response.errorMessage, errorCode = response.error)
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    override suspend fun getNewsDetail(id: Int): ApiResponse<DriverNews> {
        val token = tokenOrNull() ?: return ApiResponse.Error(401, "Session tidak valid")
        return when (val result = safeApiCall { api.getNewsDetail(id, token) }) {
            is ApiResponse.Success -> {
                val response = result.data
                val news = response.data.singleNews
                if (response.isSuccessful && news != null && news.id > 0) {
                    ApiResponse.Success(news.toDomain())
                } else {
                    ApiResponse.Error(404, response.errorMessage.ifBlank { "Berita tidak ditemukan" })
                }
            }
            is ApiResponse.Error -> result
            ApiResponse.Loading -> ApiResponse.Loading
        }
    }

    private suspend fun tokenOrNull(): String? =
        sessionDataStore.token.firstOrNull()?.takeIf(String::isNotBlank)
}
