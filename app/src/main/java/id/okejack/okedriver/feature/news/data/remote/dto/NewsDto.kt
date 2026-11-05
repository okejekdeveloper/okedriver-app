package id.okejack.okedriver.feature.news.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.news.domain.model.DriverNews
import id.okejack.okedriver.feature.news.domain.model.NewsPage

data class NewsResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: NewsDataDto = NewsDataDto()
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Berita tidak dapat dimuat")
}

data class NewsDataDto(
    val news: List<NewsItemDto> = emptyList(),
    @SerializedName("single_news") val singleNews: NewsItemDto? = null,
    val pagination: NewsPaginationDto = NewsPaginationDto()
)

data class NewsPaginationDto(
    @SerializedName("current_page") val currentPage: Int = 0,
    @SerializedName("last_page") val lastPage: Int = 0
)

data class NewsItemDto(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val summary: String = "",
    val image: String = "",
    val url: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    val pinned: Boolean = false
) {
    fun toDomain() = DriverNews(
        id = id,
        title = title,
        content = content,
        summary = summary,
        imageUrl = image,
        actionUrl = url,
        createdAt = createdAt,
        isPinned = pinned
    )
}

fun NewsDataDto.toPage(requestedPage: Int): NewsPage = NewsPage(
    items = news.map { it.toDomain() },
    currentPage = pagination.currentPage.takeIf { it > 0 } ?: requestedPage,
    lastPage = pagination.lastPage
)
