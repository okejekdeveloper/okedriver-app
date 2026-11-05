package id.okejack.okedriver.feature.news.domain.usecase

import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.news.domain.model.DriverNews
import id.okejack.okedriver.feature.news.domain.repository.NewsRepository
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(private val repository: NewsRepository) {
    suspend operator fun invoke(page: Int) = repository.getNews(page.coerceAtLeast(1))
}

class GetNewsDetailUseCase @Inject constructor(private val repository: NewsRepository) {
    suspend operator fun invoke(id: Int): ApiResponse<DriverNews> =
        if (id > 0) repository.getNewsDetail(id)
        else ApiResponse.Error(400, "ID berita tidak valid")
}
