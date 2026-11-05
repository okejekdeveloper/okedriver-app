package id.okejack.okedriver.feature.news.domain.repository

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.news.domain.model.DriverNews
import id.okejack.okedriver.feature.news.domain.model.NewsPage

interface NewsRepository {
    suspend fun getNews(page: Int): ApiResponse<NewsPage>
    suspend fun getNewsDetail(id: Int): ApiResponse<DriverNews>
}
